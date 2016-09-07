/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package examples.poPacMan;

import java.awt.Choice;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 *
 * @author Giang
 */
public class MCTSNode {

    // SIZE GAME : 36X36 NODES
    // ALL DISTANCE CANCULATE BY 
    DM measureMethod = DM.PATH;
    // some threshole
    final int LIMIT_LENGHT = 15 * 4;
    final int LIMIT_MOVE = 200;
    final int MIN_VISITED = 1;
    final int DEPTH_LIMIT = 2;
     
    double MIN_SURVIVAL;

    final double NOMAL_MIN_SURVIVAL = 0.6;
    final double DANGER_MIN_SURVIVAL = 0.8;
    ;
    final int MAX_LENGHT = 36 + 36;

    public double winRate; // [0,1] need to be nomolized
    public int visitedCount;

    public MCTSNode parentNode;// node index respective to parent node
    public int nodeIndex; // node index respective to conjunction node
    public int numOfChild;
    public int childNodeIndex[];
    public MCTSNode listChild[]; // MCTS Child Node
    public int selectedChild; // selected child 0 or 1;

    MOVE moveToReach;

    public Game game; // currentGame
    // total reward incase
    public double reward[]; // 0 : survival 1 : pill reward 2 : ghost reward

    // sum value over the child and itself
    public double sum_rewardSurvival;
    public double sum_rewardGhost;
    public double sum_rewardPills;

    public double avg_rewardSurvival;
    public double avg_rewardPills;

    // max value over the child 
    public double max_rewardSurvival;
    public double max_rewardGhost;
    public double max_rewardPills;

    public boolean isConner;
    public double riskConner = 0.9;

    int nearestPill;// nomolized by 36+36 = 72 (width+hight)

    int lenghtToRoot;

///    static double epsilon = 1e-6;
    public void setGame(Game g) {
        this.game = new Game(0);
        this.game = g.copy();
    }

// CONSTRUCTOR
    public MCTSNode() {
        winRate = 0;
        visitedCount = 0;
        numOfChild = 0;
        nodeIndex = -1;

        listChild = null;
        childNodeIndex = null;

        parentNode = null;
        reward = new double[3];

        selectedChild = -1;
        lenghtToRoot = Integer.MAX_VALUE;

        isConner = false;

        // sum value
        sum_rewardSurvival = 0;
        sum_rewardGhost = 0;
        sum_rewardPills = 0;

        // max value
        max_rewardSurvival = 0;
        max_rewardGhost = 0;
        max_rewardPills = 0;
        MIN_SURVIVAL = NOMAL_MIN_SURVIVAL;
    }

//
    public void init() {

        this.nodeIndex = this.game.getPacmanCurrentNodeIndex();
        lenghtToRoot = 0;

        if (this.game.isJunction(this.nodeIndex)) {
            moveToReach = this.game.getPacmanLastMoveMade();
        } else {
            moveToReach = MOVE.NEUTRAL;
        }

    }

// SET 2 CHILDS. NEED TO OPTIMIZED
    public void setChild() {

        Queue<MCTSNode> queue = new LinkedList<MCTSNode>();
        MOVE listPosibleMove[];
        listPosibleMove = this.game.getPossibleMoves(this.nodeIndex, moveToReach);
        boolean isCoor = false;
        //    System.out.println("POSSIBLE move at " + this.nodeIndex + " is " + listPosibleMove.length);

        for (MOVE move : listPosibleMove) {
            if (move == MOVE.NEUTRAL) {
                continue;
            }

            int tempNode = this.nodeIndex;
            int len = this.lenghtToRoot;
            isCoor = false;

            do {
                tempNode = this.game.getNeighbour(tempNode, move);
                len = len + 1;
                isCoor = isConner(this.game, tempNode, move);
            } while (!(isCoor || this.game.isJunction(tempNode)) && len < LIMIT_LENGHT);

            if (this.game.isJunction(tempNode) || isCoor) {
                if (isCoor) {
                    isConner = true;
                }
                MCTSNode child = new MCTSNode();
                child.nodeIndex = tempNode;
                child.setGame(this.game);
                child.moveToReach = move;
                child.parentNode = new MCTSNode();
                child.parentNode = this;
                child.lenghtToRoot = len;
                queue.add(child);

            }

        }
        // coppy to this child
        this.listChild = new MCTSNode[queue.size()];
        this.childNodeIndex = new int[queue.size()];
        this.numOfChild = queue.size();
        int index = 0;
        while (!queue.isEmpty()) {
            this.listChild[index] = queue.peek();
            this.childNodeIndex[index++] = queue.poll().nodeIndex;
        }

    }
    static int treePhaseTimce = 0;
// TREE PHASE

    public double[] treePhase(int initialPill) {
        treePhaseTimce++;
        int steps = LIMIT_LENGHT;
        int move = 0;
        double reward[] = new double[3]; // 0 : survival 1: Pill reward 2 : ghost reward
        Game simulatedGame = new Game(0);
        simulatedGame = this.game.copy();
        int oldScore = simulatedGame.getScore();
        int nodeTaget = this.listChild[this.selectedChild].nodeIndex;

        int currentPacManNode = simulatedGame.getPacmanCurrentNodeIndex(); // pacman should stay at currentNode;

        while (!simulatedGame.wasPacManEaten() && currentPacManNode != nodeTaget) {
            currentPacManNode = simulatedGame.getPacmanCurrentNodeIndex();

            MOVE nextMove = null;

            nextMove = simulatedGame.getNextMoveTowardsTarget(currentPacManNode, nodeTaget, measureMethod);
            //  nextMove =  this.listChild[this.selectedChild].moveToReach;

            EnumMap<GHOST, MOVE> listGhostMove = new EnumMap<GHOST, MOVE>(GHOST.class);

            for (GHOST ghost : GHOST.values()) {
                // if ghost require a move
                MOVE dangerMove = this.game.getNextMoveTowardsTarget(this.game.getPacmanCurrentNodeIndex(), this.game.getGhostCurrentNodeIndex(ghost), measureMethod);

                if (this.game.doesGhostRequireAction(ghost)) {
                    listGhostMove.put(ghost, dangerMove);
                } else {
                    listGhostMove.put(ghost, simulatedGame.getGhostLastMoveMade(ghost));
                }

            }

            simulatedGame.advanceGame(nextMove, listGhostMove);

            move++;

            //   print ("TREE PHASE RUNNING");
        }

        //set reward reward
        if (!simulatedGame.wasPacManEaten()) {
            reward[0] = 1;
            this.listChild[this.selectedChild].game = simulatedGame.copy();
        } else {
            //    System.out.println("DIE WHEN TREE FPHASE");
            reward[0] = 0;
        }

        reward[1] = (1.0 * (initialPill - simulatedGame.getActivePillsIndices().length) / initialPill);
        reward[2] = (simulatedGame.getScore() - oldScore);

        return reward;

    }

// PLAYOUT PHASE
    public double[] playOutPhase(int initialPill) {
        int steps = LIMIT_MOVE;
        int move = 0;

        double reward[] = new double[3]; // 0 : survival 1: Pill reward 2 : ghost reward

//        Game simulatedGame = new Game(0);
//        simulatedGame = this.game.copy();
        Game simulatedGame = this.game.copy();

        int oldScore = simulatedGame.getScore();

        while (move <= steps && !simulatedGame.wasPacManEaten()) {
            EnumMap<GHOST, MOVE> listGhostMove = new EnumMap<GHOST, MOVE>(GHOST.class);

            // get Ghost current Move
            for (GHOST ghost : GHOST.values()) {
                // if ghost require a move
                MOVE dangerMove = this.game.getNextMoveTowardsTarget(this.game.getPacmanCurrentNodeIndex(), this.game.getGhostCurrentNodeIndex(ghost), measureMethod);
                MOVE safeMove = this.game.getNextMoveAwayFromTarget(this.game.getPacmanCurrentNodeIndex(), this.game.getGhostCurrentNodeIndex(ghost), measureMethod);

//                if (this.game.doesGhostRequireAction(ghost)) {
//                    if (this.game.isGhostEdible(ghost))
//                        listGhostMove.put(ghost, safeMove);
//                    else
//                    listGhostMove.put(ghost, dangerMove);
//                } else 
                {
                    listGhostMove.put(ghost, simulatedGame.getGhostLastMoveMade(ghost));
                }

            }
            // Keep currentMove F ghost
            MOVE pacmanMove[] = simulatedGame.getPossibleMoves(simulatedGame.getPacmanCurrentNodeIndex(), simulatedGame.getPacmanLastMoveMade());
            Random R = new Random();
            simulatedGame.advanceGame(pacmanMove[R.nextInt(pacmanMove.length)], listGhostMove);
            // simulatedGame.advanceGame(simulateMove(simulatedGame), listGhostMove);
            move++;

        }

        //set reward reward
        if (!simulatedGame.wasPacManEaten()) {
            reward[0] = 1;

        } else {
            //    System.out.println("DIE WHEN PLAY OUT");
            reward[0] = 0;
        }

        reward[1] = (1.0 * (initialPill - simulatedGame.getActivePillsIndices().length) / initialPill);
        reward[2] = (simulatedGame.getScore() - oldScore);

        this.reward = reward;
        return reward;
        // System.out.println("reward PlayOut :" +simulatedGame.getScore()+" - "+ this.reward  );
    }

    // GHOST PLAY-OUT STRATEGY
    /* 1ST STRATEGY GREEDY E = 0.2 */
    EnumMap<GHOST, MOVE> getMoveGhostPlayOutStrategy(Game simulatedGame) {
        Random R = new Random();
        EnumMap<GHOST, MOVE> listGhostMove = new EnumMap<GHOST, MOVE>(GHOST.class);

        // greeddy epx
        float ep = R.nextFloat();
        ep = ep - (int) ep;
        // RANDOM MOVE
        if (ep < 0.2f) {

            for (GHOST ghost : GHOST.values()) {
                // If it is > 0 then it is visible so no more PO checks
                MOVE possibleMove[] = simulatedGame.getPossibleMoves(simulatedGame.getGhostCurrentNodeIndex(ghost));
                listGhostMove.put(ghost, possibleMove[R.nextInt(possibleMove.length)]);
            }
        } else {
            //GHOST BASE ON RULE

        }

        return listGhostMove;
    }

// SELECT BY UCB
    public void select() {
         
        int selectedSurvial = 0;
        int selectedReward = 0;
        double bestValueSurvival = Double.MIN_VALUE;
        double bestValueReward = Double.MIN_VALUE;
        int index = 0;
        boolean isSafe = true;

        index = 0;
        for (MCTSNode child : listChild) {

            if (child.visitedCount < MIN_VISITED ) {
                this.selectedChild = index;
                return;
            }

            double uctValue = (child.avg_rewardSurvival)
                    + Math.sqrt(Math.log(this.visitedCount) / (child.visitedCount));

            // small random number to break ties randomly in unexpanded nodes
            if (uctValue > bestValueSurvival) {
                selectedSurvial = index;
                bestValueSurvival = uctValue;
            }

            index++;
        }

        index = 0;

        for (MCTSNode child : listChild) {
            double uctValue =  child.avg_rewardPills*child.avg_rewardSurvival
                    + Math.sqrt(Math.log(this.visitedCount) / (child.visitedCount));

            // small random number to break ties randomly in unexpanded nodes
            if (uctValue > bestValueReward) {
                selectedReward = index;
                bestValueReward = uctValue;
            }

            index++;

        }
        
        if (bestValueSurvival >=MIN_SURVIVAL && bestValueReward == 0) {

            bestValueReward = Double.MIN_VALUE;
            selectedReward = 0;
            index = 0;
            for (MCTSNode child : this.listChild) {

                int nearestPill = (child.game.getClosestNodeIndexFromNodeIndex(child.game.getPacmanCurrentNodeIndex(), child.game.getActivePillsIndices(), DM.MANHATTAN));

                double pathRate;
                pathRate = (MAX_LENGHT - child.game.getManhattanDistance(child.nodeIndex, nearestPill) * 1.0) / MAX_LENGHT;

                if ((child.avg_rewardSurvival * pathRate) > bestValueReward) {
                    bestValueReward = child.avg_rewardSurvival * pathRate;
                    selectedReward = index;
                }
                index++;

            }
        }

        if (bestValueSurvival >= MIN_SURVIVAL) {
            this.selectedChild = selectedReward;
        } else {
            this.selectedChild = selectedSurvial;
        }
        
        
        return;
    }

// UPDATE VALUE    
    public void updateStats(double[] value) {
        double tempReward[] = new double[3];
        if (this.isConner) {
            tempReward[0] = value[0] * riskConner;
            tempReward[1] = value[1] ;
            tempReward[2] = value[2]  ;
        } else {
            tempReward[0] = value[0] ;
            tempReward[1] = value[1] ;
            tempReward[2] = value[2];
        }
        this.sum_rewardSurvival += tempReward[0];
        this.sum_rewardPills += tempReward[1];
        this.sum_rewardGhost += tempReward[2];
        this.visitedCount++;
        // NEED TO BE MODIFIED LATER

        this.avg_rewardSurvival = (1.0 * this.sum_rewardSurvival) / (this.visitedCount);
        this.avg_rewardPills = (1.0 * this.sum_rewardPills) / (this.visitedCount);

    }

    public boolean isInBound() {
        return (this.lenghtToRoot != Integer.MAX_VALUE && this.lenghtToRoot < LIMIT_LENGHT);
    }

   
// LNR

    public void createEntireTree(MCTSNode currentNode, int depth) {
        if (depth >= DEPTH_LIMIT ) {
            return;
        }

        currentNode.setChild();
        for (MCTSNode child : currentNode.listChild) {
            if (child != null) {
                if (child.isInBound() ) {
                    createEntireTree(child, depth + 1);
                }
            }
        }

    }

       
    
    public boolean isLeaf() {
        return (this.listChild == null || this.listChild.length == 0);

    }

    public static void runMCTS(MCTSNode root, int initialPill) {
        double tempReward[] = new double[3];
        //        System.out.println("runMCTSX");

        if (!root.isLeaf()) {
            root.select();
            //        System.out.println("\nSelect " + root.selectedChild);

            tempReward = root.treePhase(initialPill);
            if (tempReward[0] == 1) {
                //      System.out.println("SELECT " + root.listChild[root.selectedChild].nodeIndex +" to move" );
                runMCTS(root.listChild[root.selectedChild], initialPill);
            } else {
                root.listChild[root.selectedChild].updateStats(tempReward);
                //      System.out.println("DIE TREE Phase from middle of tree when run from " +root.nodeIndex + " to " +root.listChild[root.selectedChild].nodeIndex );
                root.playOutPhase(initialPill);
                root.updateStats(root.reward);
                 

            }
        } else if (root.parentNode != null) {
            root.playOutPhase(initialPill);
            //         System.out.println("reward playout at " + root.nodeIndex + " :  " +root.reward[0]+"  - "+ root.game.getActivePillsIndices().length );
            root.updateStats(root.reward);
        }

        if (root.parentNode != null) {
            root.parentNode.updateStats(root.reward);
        }

    }

    public MOVE selectBestMove(Game gameX, boolean isCreateMCTS) {
        // for the unexpected case

        if (this.listChild.length == 0) {
            return MOVE.NEUTRAL;
        }

        double maxRewardPill = Double.MIN_VALUE;
        double maxSurvival = Double.MIN_VALUE;
         double minSurvival = Double.MAX_VALUE;
        int selecSurvial = 0;
        int selecReward = 0;
        int index = 0;

        boolean isSafe = true;
        boolean foundPill = false;

        for (MCTSNode child : this.listChild) {
            if (((child.avg_rewardPills*child.avg_rewardSurvival ) > maxRewardPill)) {
                maxRewardPill = child.avg_rewardPills*child.avg_rewardSurvival ;
                selecReward = index;
            }
            
            
            index++;

        }

        index = 0;
        for (MCTSNode child : this.listChild) {

            if ((child.avg_rewardSurvival) > maxSurvival) {
                maxSurvival = child.avg_rewardSurvival;
                selecSurvial = index;
            }
            index++;

        }

        //  String survivalRate = String.format("%.02f", max * 100);
        if (isCreateMCTS) {
            if (maxSurvival <= 0.5) {
                System.out.println("\033[31mNOT SAFE           MAX: " + (int) (this.listChild[selecSurvial].avg_rewardSurvival * 100) + "%  ");
            } else if (maxSurvival > 0.5 && maxSurvival <= 0.8) {
                System.out.println("NOT SAFE           MAX: " + (int) (this.listChild[selecSurvial].avg_rewardSurvival * 100) + "%  ");
            } else {
                System.out.println("\033[32mSAFE               MAX: " + (int) (this.listChild[selecSurvial].avg_rewardSurvival * 100) + "%  ");
            }
        }

        if (maxSurvival >=MIN_SURVIVAL && maxRewardPill == 0) {

            maxRewardPill = Double.MIN_VALUE;
            selecSurvial = 0;
            index = 0;
            for (MCTSNode child : this.listChild) {

                
                int nearestPill = (child.game.getClosestNodeIndexFromNodeIndex(child.game.getPacmanCurrentNodeIndex(), child.game.getActivePillsIndices(), DM.MANHATTAN));
                double pathRate;
                pathRate = (MAX_LENGHT - child.game.getManhattanDistance(child.nodeIndex, nearestPill) * 1.0) / MAX_LENGHT;


                if ((child.avg_rewardSurvival * pathRate) > maxRewardPill) {
                    maxRewardPill = child.avg_rewardSurvival * pathRate;
                    selecSurvial = index;
                }
                index++;

            }
        }

        MOVE nextMove;

        int selectedChildX = 0;
        if (maxSurvival >= MIN_SURVIVAL) {
            selectedChildX = selecReward;
        } else {
            selectedChildX = selecSurvial;
        }
         
        nextMove = findMovebyBFSWithGhostCheck(gameX, this.childNodeIndex[selectedChildX], this.listChild[selectedChildX].moveToReach);
        this.selectedChild = selectedChildX;
      //   System.out.println("MOVE " + nextMove +" to " + this.listChild[selectedChildX].nodeIndex);
        return nextMove;

    }

    public void print(MCTSNode currentNode) {
        System.out.println("");
        if (currentNode == null) {
            return;
        }

        System.out.print(currentNode.nodeIndex + " - winrate : " + currentNode.avg_rewardSurvival);

        if (currentNode.isLeaf()) {
            return;
        }

        System.out.print("   " + currentNode.childNodeIndex.length + " childs");
        for (MCTSNode child : currentNode.listChild) {
            if (child != null) {
                System.out.print(" " + child.nodeIndex + "-" + child.lenghtToRoot + " - last Move " + child.moveToReach);
            }
        }

        for (MCTSNode child : currentNode.listChild) {
            print(child);
        }

    }

    // BFS free move
    MOVE findMovebyBFSWithGhostCheck(Game gameX, int des) {
        MOVE nextMove = null;

        int listGhostIndex[] = new int[4];
        int index = 0;

        Queue<Integer> queue = new LinkedList<Integer>();
        boolean[] check = new boolean[5000];
        int[] parrent = new int[5000];

        queue.add(gameX.getPacmanCurrentNodeIndex());
        check[gameX.getPacmanCurrentNodeIndex()] = true;
        int currentNodeIndex = queue.poll();
        {
            int neighbour[] = new int[gameX.getNeighbouringNodes(currentNodeIndex).length];
            neighbour = gameX.getNeighbouringNodes(currentNodeIndex);

            for (int nextNode : neighbour) {
                if (check[nextNode] == false) {
                    boolean f = false;

                    for (GHOST ghost : GHOST.values()) {
                        if (currentNodeIndex == gameX.getGhostCurrentNodeIndex(ghost) && !gameX.isGhostEdible(ghost)) {
                            f = true;
                            break;
                        }
                    }

                    if (f) {
                        continue;
                    }

                    queue.add(nextNode);
                    parrent[nextNode] = nextNode;
                    check[nextNode] = true;
                }
            }
        }

        while (!queue.isEmpty()) {
            currentNodeIndex = queue.poll();

            if (currentNodeIndex == des) {

                return gameX.getMoveToMakeToReachDirectNeighbour(gameX.getPacmanCurrentNodeIndex(), parrent[currentNodeIndex]);
            } else {
                {
                    int neighbour[] = new int[gameX.getNeighbouringNodes(currentNodeIndex).length];
                    neighbour = gameX.getNeighbouringNodes(currentNodeIndex);

                    for (int nextNode : neighbour) {
                        if (check[nextNode] == false) {
                            boolean f = false;
                            for (GHOST ghost : GHOST.values()) {
                                if (currentNodeIndex == gameX.getGhostCurrentNodeIndex(ghost) && !gameX.isGhostEdible(ghost)) {
                                    f = true;
                                    break;
                                }
                            }

                            if (f) {
                                continue;
                            }

                            queue.add(nextNode);
                            parrent[nextNode] = parrent[currentNodeIndex];
                            check[nextNode] = true;
                        }
                    }
                }

            }

        }

        return MOVE.NEUTRAL;
    }

    // BFS with Expected Move
    MOVE findMovebyBFSWithGhostCheck(Game gameX, int des, MOVE expectedMove) {
        MOVE nextMove = null;

        int listGhostIndex[] = new int[GHOST.values().length];
        int index = 0;
        MOVE tempBestMove = MOVE.NEUTRAL;

        Queue<Integer> queue = new LinkedList<Integer>();
        boolean[] check = new boolean[5000];
        int[] parrent = new int[5000];

        for (GHOST ghost : GHOST.values()) {
            listGhostIndex[index++] = gameX.getGhostCurrentNodeIndex(ghost);
        }

        queue.add(gameX.getPacmanCurrentNodeIndex());
        check[gameX.getPacmanCurrentNodeIndex()] = true;
        int currentNodeIndex = queue.poll();
        {
            int neighbour[] = new int[gameX.getNeighbouringNodes(currentNodeIndex).length];
            neighbour = gameX.getNeighbouringNodes(currentNodeIndex);

            for (int nextNode : neighbour) {
                if (check[nextNode] == false) {
                    boolean f = false;

                    for (GHOST ghost : GHOST.values()) {
                        if (currentNodeIndex == gameX.getGhostCurrentNodeIndex(ghost) && !gameX.isGhostEdible(ghost)) {
                            f = true;
                            break;
                        }
                    }

                    if (f) {
                        continue;
                    }

                    queue.add(nextNode);
                    parrent[nextNode] = nextNode;
                    check[nextNode] = true;
                }
            }
        }

        while (!queue.isEmpty()) {
            currentNodeIndex = queue.poll();

            if (currentNodeIndex == des) {

                if (gameX.getMoveToMakeToReachDirectNeighbour(gameX.getPacmanCurrentNodeIndex(), parrent[currentNodeIndex]) == expectedMove) {
                    return expectedMove;
                } else {
                    tempBestMove = gameX.getMoveToMakeToReachDirectNeighbour(gameX.getPacmanCurrentNodeIndex(), parrent[currentNodeIndex]);
                }

            } else {
                {
                    int neighbour[] = new int[gameX.getNeighbouringNodes(currentNodeIndex).length];
                    neighbour = gameX.getNeighbouringNodes(currentNodeIndex);

                    for (int nextNode : neighbour) {
                        if (check[nextNode] == false) {
                            boolean f = false;
                            for (GHOST ghost : GHOST.values()) {
                                if (currentNodeIndex == gameX.getGhostCurrentNodeIndex(ghost) && !gameX.isGhostEdible(ghost)) {
                                    f = true;
                                    break;
                                }
                            }

                            if (f) {
                                continue;
                            }

                            queue.add(nextNode);
                            parrent[nextNode] = parrent[currentNodeIndex];
                            check[nextNode] = true;
                        }
                    }
                }

            }

        }

        return tempBestMove;
    }

    // 2 possible moves but no currentMove;
    public static boolean isConner(Game gameX, int nodeIndex, MOVE currentMove) {
        int numOfPossibleMove = gameX.getPossibleMoves(nodeIndex).length;
        return (gameX.getNeighbour(nodeIndex, currentMove) == -1 && numOfPossibleMove == 2);

    }

}
