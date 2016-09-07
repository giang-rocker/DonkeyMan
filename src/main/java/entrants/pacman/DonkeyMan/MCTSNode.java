/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entrants.pacman.DonkeyMan;

import entrants.pacman.DonkeyMan.Junction.POSITION;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import pacman.game.Constants;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 *
 * @author Giang
 */
public class MCTSNode {

    /*=====================================VARIABLES==========================================*/
    // SIZE GAME : 36X36 NODES 25 x 3?
    // ALL DISTANCE CANCULATE BY 
    Constants.DM measureMethod = Constants.DM.PATH;

    // some threshole
    final static int LIMIT_LENGHT = 40;
    final static int LIMIT_MOVE = 60;
    final static int MIN_VISITED = 5;
    final static double riskConner = 0.8;
    final static double epsilon = 0.2;
    final static double esp = 0.000001;
    final static double DECREASE_DECAY = 0.8;
    final static double DECREASE_CHANGEMOVE = 0.8;
    final static double DECREASE_WASTEMOVE = 0.5;

    double MIN_SURVIVAL;
    final static double NOMAL_MIN_SURVIVAL = 0.7;
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
    boolean hasJustEattenGhost;
    boolean isFirstChoise = true;

    double decreaseWasteMove;

    public static int currentTactic; // 0 : survival | 1 : pill  |  2 : ghost
    public static boolean hasJustChangeMove = false ;

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
        decreaseWasteMove = 1;
        hasJustEattenGhost = false;
         

    }

    // reset each time re-built tree with game
    // init with game
    public void init(Game _game) {
        this.parentNode = null;
        this.game = _game.copy();
        this.nodeIndex = this.game.getPacmanCurrentNodeIndex();
        lenghtToParent = 0;

//        if (!_game.isJunction(_game.getPacmanCurrentNodeIndex()) && !MCTSNode.isConner(_game, _game.getPacmanCurrentNodeIndex())) {
//            this.moveToReach = MOVE.NEUTRAL;
//        } else 
        if (currentTactic == 2 && !hasJustChangeMove) {
            hasJustChangeMove = true;
            this.moveToReach = MOVE.NEUTRAL;
        } else {
            this.moveToReach = this.game.getPacmanLastMoveMade();
        }

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

    private static final int MIN_DISTANCE = 20;
    private Random random = new Random();

    public MOVE PacmanGetMove(Game game) {

        // Should always be possible as we are PacMan
        int current = game.getPacmanCurrentNodeIndex();

        // Strategy 1: Adjusted for PO
        for (GHOST ghost : GHOST.values()) {
            // If can't see these will be -1 so all fine there
            if (game.getGhostEdibleTime(ghost) == 0 && game.getGhostLairTime(ghost) == 0) {
                int ghostLocation = game.getGhostCurrentNodeIndex(ghost);
                if (ghostLocation != -1) {
                    if (game.getShortestPathDistance(current, ghostLocation) < MIN_DISTANCE) {
                        return game.getNextMoveAwayFromTarget(current, ghostLocation, DM.PATH);
                    }
                }
            }
        }

        /// Strategy 2: Find nearest edible ghost and go after them
        int minDistance = Integer.MAX_VALUE;
        GHOST minGhost = null;
        for (GHOST ghost : GHOST.values()) {
            // If it is > 0 then it is visible so no more PO checks
            if (game.getGhostEdibleTime(ghost) > 0) {
                int distance = game.getShortestPathDistance(current, game.getGhostCurrentNodeIndex(ghost));

                if (distance < minDistance) {
                    minDistance = distance;
                    minGhost = ghost;
                }
            }
        }

        if (minGhost != null) {
            return game.getNextMoveTowardsTarget(current, game.getGhostCurrentNodeIndex(minGhost), DM.PATH);
        }

        // Strategy 3: Go after the pills and power pills that we can see
        int[] pills = game.getPillIndices();
        int[] powerPills = game.getPowerPillIndices();

        ArrayList<Integer> targets = new ArrayList<Integer>();

        for (int i = 0; i < pills.length; i++) {
            //check which pills are available
            Boolean pillStillAvailable = game.isPillStillAvailable(i);
            if (pillStillAvailable == null) {
                continue;
            }
            if (game.isPillStillAvailable(i)) {
                targets.add(pills[i]);
            }
        }

        for (int i = 0; i < powerPills.length; i++) {            //check with power pills are available
            Boolean pillStillAvailable = game.isPillStillAvailable(i);
            if (pillStillAvailable == null) {
                continue;
            }
            if (game.isPowerPillStillAvailable(i)) {
                targets.add(powerPills[i]);
            }
        }

        if (!targets.isEmpty()) {
            int[] targetsArray = new int[targets.size()];        //convert from ArrayList to array

            for (int i = 0; i < targetsArray.length; i++) {
                targetsArray[i] = targets.get(i);
            }
            //return the next direction once the closest target has been identified
            return game.getNextMoveTowardsTarget(current, game.getClosestNodeIndexFromNodeIndex(current, targetsArray, DM.PATH), DM.PATH);
        }

        // Strategy 4: New PO strategy as now S3 can fail if nothing you can see
        // Going to pick a random action here
        MOVE[] moves = game.getPossibleMoves(current, game.getPacmanLastMoveMade());
        if (moves.length > 0) {
            return moves[random.nextInt(moves.length)];
        }
        // Must be possible to turn around
        return game.getPacmanLastMoveMade().opposite();

    }

    public EnumMap<GHOST, MOVE> GhostGetRandomMove(Game game) {
        EnumMap<GHOST, MOVE> myMoves = new EnumMap<GHOST, MOVE>(GHOST.class);
        Random rnd = new Random ();
        for (GHOST ghost : GHOST.values()) {
            if (game.getGhostCurrentNodeIndex(ghost)==game.getGhostInitialNodeIndex()) continue;
                
            if (rnd.nextInt(100) <10) {
             myMoves.put(ghost, game.getGhostLastMoveMade(ghost));
             continue;
            }
            
              MOVE[] possibleMoves = game.getPossibleMoves(game.getGhostCurrentNodeIndex(ghost), game.getGhostLastMoveMade(ghost));
              
          //    System.out.println(ghost + " " + game.getGhostCurrentNodeIndex(ghost) +" " + game.getGhostLastMoveMade(ghost));
              
              if (possibleMoves.length==0) continue;
              MOVE nextMove =  possibleMoves[rnd.nextInt(possibleMoves.length)];
              myMoves.put(ghost, nextMove);
        }
            

        return myMoves;

    }

    public double[] playOutPhase(int initialPill, EnumMap<GHOST, Integer> originEdibleGhost) {
        int steps = LIMIT_MOVE;
        int move = 0;

        int totalEdibleTime = 0;

        for (GHOST ghost : GHOST.values()) {
            totalEdibleTime += originEdibleGhost.get(ghost);
        }

        double rewardX[] = new double[3]; // 0 : survival 1: Pill reward 2 : ghost reward
        rewardX[2] = this.rewardPlayout[2];

        //Game simulatedGame = this.game.copy();
        Game simulatedGame = this.game;

        int oldScore = simulatedGame.getScore();
        int currentLevel = simulatedGame.getCurrentLevel();
        double rewardGhost = 0;

     
        while (move <= steps && !simulatedGame.wasPacManEaten()) {
            
            if (totalEdibleTime!=0 && simulatedGame.wasPowerPillEaten() ) break;
            
        EnumMap<GHOST, Integer> currentEdibleGhost = new EnumMap<>(GHOST.class);;
            MOVE nextMove = PacmanGetMove(simulatedGame);

            SimulateGhostMove ghostsMove = new SimulateGhostMove();
            EnumMap<GHOST, MOVE> listGhostMove = new EnumMap<>(GHOST.class);
            //STRATEGY MOVE
          //  listGhostMove = ghostsMove.getMove(simulatedGame);
           
            // RANDOM MOVES
                listGhostMove = GhostGetRandomMove (simulatedGame);
                
            
//               for (GHOST ghost : GHOST.values()) 
//                if (simulatedGame.doesGhostRequireAction(ghost))
//                listGhostMove.put(ghost, simulatedGame.getNextMoveTowardsTarget(simulatedGame.getPacmanCurrentNodeIndex(),simulatedGame.getGhostCurrentNodeIndex(ghost), measureMethod));
//                else 
//                listGhostMove.put(ghost, simulatedGame.getGhostLastMoveMade(ghost));
        
            
            // get current EdibleTime
            for (GHOST ghost : GHOST.values()) {
                int time = simulatedGame.getGhostEdibleTime(ghost);
                time = Integer.max(0, time);
                currentEdibleGhost.put(ghost, time);

            }

            simulatedGame.advanceGame(nextMove, listGhostMove);

//            EnumMap<GHOST, MOVE> listDangerGhostMove = new EnumMap<>(GHOST.class);
//            for (GHOST ghost : GHOST.values()) {
//                listDangerGhostMove.put(ghost, this.game.getNextMoveTowardsTarget(this.game.getPacmanCurrentNodeIndex(), this.game.getGhostCurrentNodeIndex(ghost), measureMethod));
//            }
//            simulatedGame.advanceGame(nextMove, listDangerGhostMove);
            // no take any power pill
            if (currentEdibleGhost.size() > 0 && simulatedGame.wasPowerPillEaten()) {
                break;
            }

            // check was ghost eatten
            for (GHOST ghost : GHOST.values()) {
                if (simulatedGame.wasGhostEaten(ghost)) {
                    rewardGhost += (currentEdibleGhost.get(ghost));
                }
            }
            
            
            move++;

        }

        //set reward reward
        // check if the pacman was blocked all of avalable way
        if (!simulatedGame.wasPacManEaten()) {

            rewardX[0] = 1;

        } else {

            //       System.out.println("DIE PLAYOUT " + this.nodeIndex );
            rewardX[0] = 0;
        }

        int currentPill = simulatedGame.getNumberOfActivePills();

        if (initialPill < currentPill) {
            currentPill = 0;
            rewardX[0] = 1;

        }

        rewardX[1] = (1.0 * (initialPill - currentPill) / initialPill);
        rewardX[2] += rewardGhost;
        
        rewardX[2] = (rewardX[2]  * 1.0) / totalEdibleTime;

        return rewardX;
        //debug("reward PlayOut :" +simulatedGame.getScore()+" - "+ this.reward  );
    }

    boolean treePhase(int initialPill, MCTSNode currentBestChild, EnumMap<GHOST, Integer> originEdibleGhost) {

        Game simulatedGame = this.game.copy();
        int nodeTaget = currentBestChild.nodeIndex;

        int totalEdibleTime = 0;

        for (GHOST ghost : GHOST.values()) {
            totalEdibleTime += originEdibleGhost.get(ghost);
        }

        double rewardGhost = 0;

         
        int currentPacManNode = simulatedGame.getPacmanCurrentNodeIndex(); // pacman should stay at currentNode;

        //   debug("TREE PHASE FROM " + this.nodeIndex + " (check "+ currentPacManNode +") + to " + nodeTaget +" by move " +currentBestChild.moveToReach);
        MOVE nextMove = currentBestChild.moveToReach;

        while (!simulatedGame.wasPacManEaten() && currentPacManNode != nodeTaget) {
        EnumMap<GHOST, Integer> currentEdibleGhost = new EnumMap<>(GHOST.class);;
            // get current EdibleTime
            for (GHOST ghost : GHOST.values()) {
                int time = simulatedGame.getGhostEdibleTime(ghost);
                time = Integer.max(0, time);
                currentEdibleGhost.put(ghost, time);

            }
          
            SimulateGhostMove ghostsMove = new SimulateGhostMove();
            EnumMap<GHOST, MOVE> listGhostMove = new EnumMap<>(GHOST.class);
            
            // STATERGY MOVES
             //  listGhostMove = ghostsMove.getMove(simulatedGame.copy());
            
            // RANDOM MOVES
                listGhostMove = GhostGetRandomMove (simulatedGame);
            
            
                     
    // DANGER MOVES

//            for (GHOST ghost : GHOST.values()) 
//                if (simulatedGame.doesGhostRequireAction(ghost))
//                listGhostMove.put(ghost, simulatedGame.getNextMoveTowardsTarget(simulatedGame.getPacmanCurrentNodeIndex(),simulatedGame.getGhostCurrentNodeIndex(ghost), measureMethod));
//                else 
//                listGhostMove.put(ghost, simulatedGame.getGhostLastMoveMade(ghost));
            
            
            simulatedGame.advanceGame(nextMove, listGhostMove);

            currentPacManNode = simulatedGame.getPacmanCurrentNodeIndex();

            // check was ghost eatten
            for (GHOST ghost : GHOST.values()) {
                if (simulatedGame.wasGhostEaten(ghost)) {
                    rewardGhost += (currentEdibleGhost.get(ghost));
                }
            }
            if(totalEdibleTime-0> esp && simulatedGame.wasPowerPillEaten()) break;
        }

        if (rewardGhost > 0) {
            currentBestChild.rewardPlayout[2] = rewardGhost;
            currentBestChild.hasJustEattenGhost = true;
        }

        int currentPill = simulatedGame.getNumberOfActivePills();

        currentBestChild.isEndNode = (currentPill > initialPill);

        boolean isSurvival = (currentPacManNode == nodeTaget) || currentBestChild.isEndNode;

        if (isSurvival) {
            currentBestChild.game = simulatedGame.copy();
        } else {

            //        System.out.println("DIE TREE PHASE FROM " + this.nodeIndex + " TO  "+ nodeTaget +" by MOVE " + currentBestChild.moveToReach );
        }
        return isSurvival;

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

    public static void runMCTS(MCTSNode root, int originPill, EnumMap<GHOST, Integer> originEdibleGhost) {

        if (root.isLeaf() || root.hasJustEattenGhost) {

            root.rewardPlayout = root.playOutPhase(originPill, originEdibleGhost); // roi sao nua?????

            root.updatStats(root.rewardPlayout);

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
            int chosenChild = -1;
            //  System.out.println("Start chose ");
            for (MCTSNode child : root.listChild) {
                //   System.out.println("selected child");
                if (child.new_visitedCount < MIN_VISITED) {
                    Random R = new Random();
                    chosenChild = R.nextInt(root.listChild.size());
                    //       System.out.println("CHOSE " + chosenChild +  " " +root.listChild.get(chosenChild).nodeIndex + " FROM " + root.listChild.size());
                    currentBestChild = root.listChild.get(chosenChild);
                    break;
                }

                double Vi[] = new double[3];
                Vi[0] = child.maxViValue[0];
                Vi[1] = child.maxViValue[1] * Vi[0];
                Vi[2] = child.maxViValue[2] * Vi[0];

                UCTValue[MCTSNode.currentTactic] = Vi[MCTSNode.currentTactic]
                        + Math.sqrt(Math.log(currentVisited) / (child.new_visitedCount));

                //    System.out.println("UCT VALIE  : " + child.nodeIndex + " Mi  "  +child.maxViValue[MCTSNode.currentTactic] + "  Vi : "  + UCTValue[MCTSNode.currentTactic] + " parrent visited  " + currentVisited);
                if (UCTValue[MCTSNode.currentTactic] > maxUCT || Math.abs(UCTValue[MCTSNode.currentTactic] - maxUCT) < esp) {
                    maxUCT = UCTValue[MCTSNode.currentTactic];
                    currentBestChild = child;
                    //     System.out.println("CHANGE BY UCT");

                }

            }

//              if (currentBestChild==null) {
//                  System.out.println("CURRENT TACTIC " + MCTSNode.currentTactic + " current chossen index   = " + chosenChild);
//                   System.out.println(maxUCT);
//                   System.out.println("ROOR CHILD" + root.listChild.size());
//                    for (MCTSNode child : root.listChild)
//                        System.out.println("CHILD " + child.nodeIndex + " vi  " + child.maxViValue[0] + "visited " + child.new_visitedCount);
//              }
            boolean isReachChild = root.treePhase(originPill, currentBestChild, originEdibleGhost);

            if (!isReachChild) {
                // child of root
                if (root.parentNode == null) {
                    root.new_visitedCount++;
                    return;
                }

                double reward[] = new double[3];
                reward = root.playOutPhase(originPill, originEdibleGhost);
                root.updatStats(reward);

                root.maxViValue[0] = root.new_sumReward[0] / root.new_visitedCount;
                root.maxViValue[1] = root.new_sumReward[1] / root.new_visitedCount;
                root.maxViValue[2] = root.new_sumReward[2] / root.new_visitedCount;
                return;

            } else {

                if (currentBestChild.isEndNode) {
                    //    System.out.println("COME TO END NODE");
                    currentBestChild.rewardPlayout = new double[]{1, 1, 0};
                    currentBestChild.updatStats(currentBestChild.rewardPlayout);
                    currentBestChild.maxViValue[0] = 1;
                    currentBestChild.maxViValue[1] = 1;
                    currentBestChild.maxViValue[2] = 0;
                } else {
                    runMCTS(currentBestChild, originPill, originEdibleGhost);
                }

                for (MCTSNode child : root.listChild) {

                    if (root.maxViValue[0] < child.maxViValue[0]) {
                        root.maxViValue[0] = child.maxViValue[0];
                    }

                    if (child.maxViValue[0] > NOMAL_MIN_SURVIVAL) {
                        if (root.maxViValue[1] < child.maxViValue[0] * child.maxViValue[1]) {
                            root.maxViValue[1] = child.maxViValue[0] * child.maxViValue[1];
                        }
                    }

                    if (child.maxViValue[0] > NOMAL_MIN_SURVIVAL) {
                        if (root.maxViValue[2] < child.maxViValue[0] * child.maxViValue[2]) {
                            root.maxViValue[2] = child.maxViValue[0] * child.maxViValue[2];
                        }
                    }

                }

                if (root.game.getScore() == currentBestChild.game.getScore()) {
                    if (MCTSNode.currentTactic != 0) {
                        currentBestChild.rewardPlayout[MCTSNode.currentTactic] *= DECREASE_WASTEMOVE;
                    }
                }

                root.updatStats(currentBestChild.rewardPlayout);

            }

        }

    }

    void updatStats(double reward[]) {
        this.new_sumReward[0] += reward[0];
        this.new_sumReward[1] += (reward[1]);
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

        double max = -1;
        MOVE nextMove = MOVE.NEUTRAL;
        MOVE expectedMove = MOVE.NEUTRAL;
        double maxReward = -1;
        int tagetNode = -1;
        if (MCTSNode.currentTactic == 0) {
            for (MCTSNode child : this.listChild) {
                if (child.new_visitedCount > MIN_VISITED) {
                    if (child.maxViValue[0] > max || (Math.abs(child.maxViValue[0] - max) < esp && child.moveToReach == gameX.getPacmanLastMoveMade())) {
                        max = child.maxViValue[0];
                        //    maxReward = child.maxViValue[1] * max;
                        tagetNode = child.nodeIndex;
                        expectedMove = child.moveToReach;
                        this.bestChild = child;
                    }

                }
            }
            //      System.out.println("NOT SAFE");
        } else {
            //if (gameX.isJunction(gameX.getPacmanCurrentNodeIndex())) System.out.println("JUNTION");
             for (MCTSNode child : this.listChild) {
                 if (child.new_visitedCount > MIN_VISITED) {

                    if (child.maxViValue[MCTSNode.currentTactic] > max || (Math.abs(child.maxViValue[MCTSNode.currentTactic] - max) < esp && child.moveToReach == gameX.getPacmanLastMoveMade())) {
                        max = child.maxViValue[MCTSNode.currentTactic];
                        tagetNode = child.nodeIndex;
                        expectedMove = child.moveToReach;
                        this.bestChild = child;
                    }
         //           System.out.println(" MOVE " + child.moveToReach + " value " + child.maxViValue[MCTSNode.currentTactic]);
                }
            }
            //     System.out.println("SAFE");
        }
         nextMove = expectedMove;
      // System.out.println("SELECT " + nextMove);
        
        if (!safeMoveCheck(gameX, expectedMove, tagetNode)) {
         //   for (MOVE move : gameX.getPossibleMoves(gameX.getPacmanCurrentNodeIndex())){
          //  if (safeMoveCheck(gameX, move)) return move;
          //  }
          return nextMove.opposite();
        }

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

    // BFS with Expected Move
    static boolean safeMoveCheck(Game gameX, MOVE expectedMove, int nodeTaget) {

        Game simulateGame = gameX.copy();
        int currentLevel = gameX.getCurrentLevel();
        EnumMap<GHOST, MOVE> listGhostMove = new EnumMap<>(GHOST.class);
        while (currentLevel == gameX.getCurrentLevel() && !gameX.wasPacManEaten() && gameX.getPacmanCurrentNodeIndex() != nodeTaget) {

            for (GHOST ghost : GHOST.values()) {
                MOVE move = gameX.getGhostLastMoveMade(ghost);
                if (move != null) {
                    listGhostMove.put(ghost, move);
                }
            }

            gameX.advanceGame(expectedMove, listGhostMove);

        }
      
        
//         
//        if(!simulateGame.wasPacManEaten() && simulateGame.getPacmanCurrentNodeIndex() == nodeTaget)
//         simulateGame.advanceGame(expectedMove, listGhostMove);
        
        if (!gameX.wasPacManEaten() && (gameX.getPacmanCurrentNodeIndex() == nodeTaget || currentLevel != gameX.getCurrentLevel())) {
            return true;
        }

        return false;
    }

    
     static boolean safeMoveCheck(Game gameX, MOVE expectedMove) {

   Game simulateGame = gameX.copy();
        int currentLevel = gameX.getCurrentLevel();
        EnumMap<GHOST, MOVE> listGhostMove = new EnumMap<>(GHOST.class);
        while (currentLevel == gameX.getCurrentLevel() && !gameX.wasPacManEaten() && gameX.isJunction(gameX.getPacmanCurrentNodeIndex()) ) {

            for (GHOST ghost : GHOST.values()) {
                MOVE move = gameX.getGhostLastMoveMade(ghost);
                if (move != null) {
                    listGhostMove.put(ghost, move);
                }
            }

            gameX.advanceGame(expectedMove, listGhostMove);

        }
      
        
//         
//        if(!simulateGame.wasPacManEaten() && simulateGame.getPacmanCurrentNodeIndex() == nodeTaget)
//         simulateGame.advanceGame(expectedMove, listGhostMove);
        
        if (!gameX.wasPacManEaten() && (gameX.isJunction(gameX.getPacmanCurrentNodeIndex()) || currentLevel != gameX.getCurrentLevel())) {
            return true;
        }

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
