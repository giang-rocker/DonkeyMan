/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entraints.pacman.DonkeyManv2;

import entrants.pacman.DonkeyMan.*;
import entrants.pacman.DonkeyMan.Junction.POSITION;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import pacman.game.Constants;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 *
 * @author Giang
 */
public class MCTSNode {

    /*=====================================VARIABLES==========================================*/
    // SIZE GAME : 36X36 NODES
    // ALL DISTANCE CANCULATE BY 
    Constants.DM measureMethod = Constants.DM.PATH;

    // some threshole
    final static int LIMIT_LENGHT = 40;
    final static int LIMIT_MOVE = 60;
    final static int MIN_VISITED = 5;
    final static double riskConner = 0.8;
    final static double epsilon = 0.2;
    final static double DECREASE_DECAY = 0.8;
    final static double DECREASE_CHANGEMOVE = 0.8;
    final static double DECREASE_WASTEMOVE = 0.8;

    double MIN_SURVIVAL;
    final static double NOMAL_MIN_SURVIVAL = 0.6;
    final static double DANGER_MIN_SURVIVAL = 0.8;

    final int MAX_LENGHT = 36 + 36; // by game size and Mahattan distance

    double winRate; // based on current tactic
    int new_visitedCount;
    //   double old_visitedCount;

    int nodeIndex; // node index respective to conjunction node || Current position of pacman

    MCTSNode parentNode;// node index respective to parent node
    ArrayList<MCTSNode> listChild; // MCTS Child Node

    MOVE moveToReach;

    Game game; // currentGame

    // sum value over the child and itself
    double new_sumReward[];
    //  double old_sumReward[];

    double new_avgReward[];

    // max value over the child 
    double maxViValue[];
    double rewardPlayout[];

    double rewardByCurrentTactic;

    MCTSNode bestChild;

    int nearestPill;// nomolized by 36+36 = 72 (width+hight) || incause reward pill or ghost  ==0
    int lenghtToParent;

    boolean isExplored;
    boolean isConner;
    boolean isEndNode;
    boolean isFirstChoise = true;

    public static int currentTactic; // 0 : survival | 1 : pill  |  2 : ghost

    /*=====================================FUCNTIONS==========================================*/
    // instruction
    // CONSTRUCTOR
    public MCTSNode() {

        winRate = 0;
//        old_visitedCount = 0;
        new_visitedCount = 0;
        nodeIndex = -1;

        listChild = null;
        parentNode = null;
        lenghtToParent = Integer.MAX_VALUE;

        //    old_sumReward = new double[3];
        new_sumReward = new double[3];
        new_avgReward = new double[3];

        // max value
        maxViValue = new double[3];

        // by current sittuation . Turn to DANGER_SURVIAL when pacman has just 1 live left
        MIN_SURVIVAL = NOMAL_MIN_SURVIVAL;

        isExplored = false;
        isConner = false;
        moveToReach = MOVE.NEUTRAL;

        listChild = new ArrayList<>();

        rewardPlayout = new double[3];
        isEndNode = false;
        listChild = new ArrayList<>();
        isFirstChoise = true;

    }

    // reset each time re-built tree with game
    // init with game
    public void init(Game _game) {
        this.game = _game.copy();
        this.nodeIndex = this.game.getPacmanCurrentNodeIndex();
        lenghtToParent = 0;
        this.moveToReach = this.game.getPacmanLastMoveMade();

    }

    public void setGame(Game _game) {
        this.game = _game.copy();
    }

    boolean inBound(int len) {
        return (len < LIMIT_LENGHT);
    }

    boolean isLeaf() {
        return ((this.listChild == null || this.listChild.isEmpty()));

    }

    void setChild(int lenX) {
        //  debug("setChild at " + this.nodeIndex);
        Queue<MCTSNode> queue = new LinkedList<MCTSNode>();
        MOVE listPosibleMove[];
        listPosibleMove = this.game.getPossibleMoves(this.nodeIndex, moveToReach);
        boolean isCoor = false;
        //   debug("POSSIBLE move at " + this.nodeIndex + " is " + listPosibleMove.length);

        for (MOVE move : listPosibleMove) {
            if (move == MOVE.NEUTRAL) {
                continue;
            }

            int tempNode = this.nodeIndex;
            int len = 0;
            isCoor = false;

            do {
                tempNode = this.game.getNeighbour(tempNode, move);
                len = len + 1;
                isCoor = isConner(this.game, tempNode, move);
            } while (!(isCoor || this.game.isJunction(tempNode)));

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
                child.lenghtToParent = len;
                child.isExplored = false;
                child.isEndNode = false;
                listChild.add(child);

            }

        }

    }

   

    // 2 possible moves but no currentMove;
    public static boolean isConner(Game gameX, int nodeIndex, MOVE currentMove) {
        int numOfPossibleMove = gameX.getPossibleMoves(nodeIndex).length;
        return (gameX.getNeighbour(nodeIndex, currentMove) == -1 && numOfPossibleMove == 2);

    }

    // 2 possible no opposiite
    public static boolean isConner(Game gameX, int nodeIndex) {
        MOVE[] possibleMove = gameX.getPossibleMoves(nodeIndex);

        return (possibleMove.length == 2 && possibleMove[0] != possibleMove[1].opposite());

    }

    boolean isUnclearEdge(int fromNode, int toNode, MOVE _move) {
        // make sure that the game is saftified 
        int currentNode = fromNode;
        while (currentNode != -1 && currentNode != toNode) {
            currentNode = this.game.getNeighbour(currentNode, _move);
            if (currentNode == -1) {
                break;
            }
            if (this.game.getPillIndex(currentNode) != -1) {
                if (this.game.isPillStillAvailable(this.game.getPillIndex(currentNode))) {
                    return true;
                }
            }
        }
        return false;
    }

    public double[] playOutPhase(int initialPill) {
        int steps = LIMIT_MOVE;
        int move = 0;

        double rewardX[] = new double[3]; // 0 : survival 1: Pill reward 2 : ghost reward

        Game simulatedGame = this.game.copy();

        int oldScore = simulatedGame.getScore();
        int currentLevel = simulatedGame.getCurrentLevel();
     
        while (move <= steps && !simulatedGame.wasPacManEaten()) {

            SimulatePacmanMove pacMove = new SimulatePacmanMove();
            MOVE nextMove = pacMove.getMove(simulatedGame);

            SimulateGhostMove ghostsMove = new SimulateGhostMove();
            EnumMap<GHOST, MOVE> listGhostMove = new EnumMap<>(GHOST.class);
            listGhostMove = ghostsMove.getMove(simulatedGame);
            simulatedGame.advanceGame(nextMove, listGhostMove);

//            EnumMap<GHOST, MOVE> listDangerGhostMove = new EnumMap<>(GHOST.class);
//            for (GHOST ghost : GHOST.values()) {
//                listDangerGhostMove.put(ghost, this.game.getNextMoveTowardsTarget(this.game.getPacmanCurrentNodeIndex(), this.game.getGhostCurrentNodeIndex(ghost), measureMethod));
//            }
//            simulatedGame.advanceGame(nextMove, listDangerGhostMove);
            
 
            
            move++;

        }

        //set reward reward
        if (!simulatedGame.wasPacManEaten()) {
            rewardX[0] = 1;
        } else {
            
          //  System.out.println("DIE PLAYOUT " + this.nodeIndex );
            rewardX[0] = 0;
        }

        int currentPill = simulatedGame.getNumberOfActivePills();

        if (initialPill < currentPill) {
            currentPill = 0;
            rewardX[0] = 1;

        }

        rewardX[1] = (1.0 * (initialPill - currentPill) / initialPill);
        rewardX[2] = (simulatedGame.getScore() - oldScore);

        return rewardX;
        //debug("reward PlayOut :" +simulatedGame.getScore()+" - "+ this.reward  );
    }

    boolean treePhase(int initialPill, MCTSNode currentBestChild) {
      
        Game simulatedGame = this.game.copy();
        int nodeTaget = currentBestChild.nodeIndex;

        int currentPacManNode = simulatedGame.getPacmanCurrentNodeIndex(); // pacman should stay at currentNode;

        //   debug("TREE PHASE FROM " + this.nodeIndex + " (check "+ currentPacManNode +") + to " + nodeTaget +" by move " +currentBestChild.moveToReach);
        MOVE nextMove = currentBestChild.moveToReach;
       
        while (!simulatedGame.wasPacManEaten() && currentPacManNode != nodeTaget) {

            SimulateGhostMove ghostsMove = new SimulateGhostMove();
            EnumMap<GHOST, MOVE> listGhostMove = new EnumMap<>(GHOST.class);
            listGhostMove = ghostsMove.getMove(simulatedGame);
            simulatedGame.advanceGame(nextMove, listGhostMove);

//            EnumMap<GHOST, MOVE> listDangerGhostMove = new EnumMap<>(GHOST.class);
//            for (GHOST ghost : GHOST.values()) {
//                listDangerGhostMove.put(ghost, this.game.getNextMoveTowardsTarget(this.game.getPacmanCurrentNodeIndex(), this.game.getGhostCurrentNodeIndex(ghost), measureMethod));
//            }
//            simulatedGame.advanceGame(nextMove, listDangerGhostMove);
            
       
           

            currentPacManNode = simulatedGame.getPacmanCurrentNodeIndex();

        }
        int currentPill = simulatedGame.getNumberOfActivePills();

        this.isEndNode = (currentPill > initialPill);

        boolean isSurvival = (!simulatedGame.wasPacManEaten()) || this.isEndNode;

        if (isSurvival) {
            currentBestChild.game = simulatedGame.copy();
        } else {
         
       //     System.out.println("DIE TREE PHASE FROM " + this.nodeIndex + " TO  "+ nodeTaget +" by MOVE " + currentBestChild.moveToReach );
        }

        return isSurvival;

    }

    // GHOST STRATERGY
    void getGhostMoveStrategy(Game gameX) {

    }

    public static void createEntireTree(MCTSNode root, int len) {
        if (len < LIMIT_LENGHT) {
            if ((root.listChild == null || root.listChild.isEmpty())) {
                root.setChild(len);

            }

            //    if (!root.isExplored)  
            for (MCTSNode child : root.listChild) {
                createEntireTree(child, len + child.lenghtToParent);
            }

        }
    }

    public static void resetMaxVi(MCTSNode root) {

        if (root.isLeaf()) {
            return;
        }

        root.maxViValue[0] = 0;
        root.maxViValue[1] = 0;
        root.maxViValue[2] = 0;

        for (MCTSNode child : root.listChild) {
            resetMaxVi(child);
        }

    }

     public static void runMCTS(MCTSNode root, int originPill) {

        if (root.isLeaf() || root.isEndNode) {

            double reward[] = new double[3];

            if (root.isEndNode) {
                reward[0] = 1;
                reward[1] = 1;
                reward[2] = 1;
            } else {
                reward = root.playOutPhase(originPill); // roi sao nua?????
            }

            root.updatStats(reward);

            root.maxViValue[0] = root.new_sumReward[0] / root.new_visitedCount;
            root.maxViValue[1] = root.new_sumReward[1] / root.new_visitedCount;
            root.maxViValue[2] = root.new_sumReward[2] / root.new_visitedCount;

            return;

        } else {
            //    debug("CHECK TREE PHASE");
            double currentVisited = root.new_visitedCount;
            double maxUCT = 0;
            double UCTValue[] = new double[3];
            MCTSNode currentBestChild = null;

            for (MCTSNode child : root.listChild) {

                if (child.new_visitedCount < MIN_VISITED) {
                    //  currentBestChild = child;

                    Random R = new Random();
                    currentBestChild = root.listChild.get(R.nextInt(root.listChild.size()));

                    break;
                }

                double Vi[] = new double[3];
                Vi[0] = child.maxViValue[0];
                Vi[1] = child.maxViValue[1] * Vi[0];
                Vi[2] = child.maxViValue[2] * Vi[0];

                UCTValue[0] = Vi[0]
                        + Math.sqrt(Math.log(currentVisited) / (child.new_visitedCount));

                UCTValue[1] = Vi[1]
                        + Math.sqrt(Math.log(currentVisited) / (child.new_visitedCount));

                UCTValue[2] = Vi[2]
                        + Math.sqrt(Math.log(currentVisited) / (child.new_visitedCount));

                if (UCTValue[MCTSNode.currentTactic] >= maxUCT) {
                    maxUCT = UCTValue[MCTSNode.currentTactic];
                    currentBestChild = child;
                }

            }

            boolean isReachChild = root.treePhase(originPill, currentBestChild);

            if (!isReachChild) {
                //     debug("DIE IN TREE PHASE");
                double reward[] = new double[3];
                reward = root.playOutPhase(originPill);
                root.updatStats(reward);
//               currentBestChild.new_visitedCount++;

            root.maxViValue[0] = root.new_sumReward[0] / root.new_visitedCount;
            root.maxViValue[1] = root.new_sumReward[1] / root.new_visitedCount;
            root.maxViValue[2] = root.new_sumReward[2] / root.new_visitedCount;


            } else {
                runMCTS(currentBestChild, originPill);
                
                for(MCTSNode child : root.listChild)
                for (int i = 0; i < 3; i++) 
                    if (root.maxViValue[i] < child.maxViValue[i]) 
                        root.maxViValue[i] = child.maxViValue[i];
                    
            if (currentBestChild.game.getNumberOfActivePills() == root.game.getNumberOfActivePills())
                currentBestChild.rewardPlayout[1]*=DECREASE_WASTEMOVE;
                
                root.updatStats(currentBestChild.rewardPlayout);

            }

        }

    }

    void updatStats(double reward[]) {
        this.new_sumReward[0] += reward[0];
        this.new_sumReward[1] += (reward[1] );
        this.new_sumReward[2] += (reward[2]);
        this.new_visitedCount++;
        this.rewardPlayout = reward;

    }

    /*
   public static void updateDecreseDecay ( double oldValue[], double newValue[] ) {
    int size = oldValue.length;
                
    for (int i =0; i < size; i++){
        oldValue[i] = DECREASE_DECAY*(oldValue[i] + newValue[i]);
        newValue[i] = 0;
    }

    }
   
   static void updateDecay (MCTSNode root) {
       if (!root.isLeaf())
           for (MCTSNode child : root.listChild) {
           
               updateDecreseDecay(child.old_sumReward, child.new_sumReward);
               
               root.old_visitedCount = DECREASE_DECAY*(root.old_visitedCount + root.new_visitedCount);
               root.new_visitedCount = 0;
               
               updateDecay(child);
           }
   
   }
     */
    public MOVE selectBestMove(Game gameX) {

        double max = -Double.MAX_VALUE;
        MOVE nextMove = MOVE.NEUTRAL;
        MOVE expectedMove = MOVE.NEUTRAL;
        double maxReward = 0;
        int tagetNode = -1;
        if (MCTSNode.currentTactic == 0) {
            for (MCTSNode child : this.listChild) {
                if (child.maxViValue[0] > max || (child.maxViValue[0] == max && child.maxViValue[0] * child.maxViValue[1] > maxReward)) {
                    max = child.maxViValue[0];
                    maxReward = child.maxViValue[1] * max;
                    tagetNode = child.nodeIndex;
                    expectedMove = child.moveToReach;
                    this.bestChild = child;
                }

            }
            //      System.out.println("NOT SAFE");
        } else {
            for (MCTSNode child : this.listChild) {
                if (child.maxViValue[0] > NOMAL_MIN_SURVIVAL) {
                    if (child.maxViValue[0] * child.maxViValue[MCTSNode.currentTactic] > max) {
                        max = child.maxViValue[MCTSNode.currentTactic] * child.maxViValue[0];
                        tagetNode = child.nodeIndex;
                        expectedMove = child.moveToReach;
                        this.bestChild = child;
                    }
                }

            }
            //     System.out.println("SAFE");
        }
        
       
            nextMove = expectedMove;
           return nextMove;
    }

    static public void print(MCTSNode currentNode) {
        System.out.println("");
        if (currentNode == null) {
            System.out.println("");
            return;
        }

        if (!currentNode.listChild.isEmpty()) {
            System.out.print(currentNode.nodeIndex + " " + currentNode.maxViValue[MCTSNode.currentTactic] + "  visited  " + currentNode.new_visitedCount + " SURVIVAL RATE " + currentNode.maxViValue[0]);
        } else {
            System.out.print("L " + currentNode.nodeIndex + " " + currentNode.maxViValue[MCTSNode.currentTactic] + "  visited  " + currentNode.new_visitedCount + " SURVIVAL RATE " + currentNode.maxViValue[0]);
        }

        if (currentNode.isLeaf()) {
            return;
        }

        System.out.print("   " + currentNode.listChild.size() + " childs");
        for (MCTSNode child : currentNode.listChild) {
            if (child != null) {
                System.out.print(" " + child.nodeIndex + "-" + child.lenghtToParent + " - last Move " + child.moveToReach);
            }
        }

        for (MCTSNode child : currentNode.listChild) {
            print(child);
        }

    }

    static boolean debugMode = false;

    public static void debug(String s) {
        if (debugMode) {
            System.out.println(s);
        }

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

    // BFS with Expected Move
    static boolean safeMoveCheck(Game gameX, MOVE expectedMove, int nodeTaget) {
       
        Game simulateGame = gameX.copy();
        int currentLevel = gameX.getCurrentLevel();
        
        while (currentLevel == simulateGame.getCurrentLevel() &&  !simulateGame.wasPacManEaten() &&  simulateGame.getPacmanCurrentNodeIndex() !=nodeTaget ) {
            
         EnumMap<GHOST, MOVE> listGhostMove = new EnumMap<>(GHOST.class);
        for (GHOST ghost :GHOST.values()) {
            MOVE move = simulateGame.getGhostLastMoveMade(ghost);
            if (move != null) {
                listGhostMove.put(ghost, move);
            }
        }
          
        simulateGame.advanceGame(expectedMove, listGhostMove);
        
        }
        
        if ( simulateGame.getPacmanCurrentNodeIndex() ==nodeTaget || currentLevel != simulateGame.getCurrentLevel()) return true;
        
        return false;
    }

    public static void printLeaf(MCTSNode currentNode) {

        if (currentNode.isLeaf()) {
            System.out.println(+currentNode.nodeIndex + " " + currentNode.maxViValue[MCTSNode.currentTactic] + "visited " + currentNode.new_visitedCount + " SURVIVAL RATE " + currentNode.maxViValue[0]);
        } else {
            for (MCTSNode child : currentNode.listChild) {
                printLeaf(child);
            }
        }
    }
}
