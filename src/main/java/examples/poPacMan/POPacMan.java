package examples.poPacMan;

import java.awt.Color;
import pacman.controllers.PacmanController;
import pacman.game.Game;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import pacman.game.Constants;

import static pacman.game.Constants.*;
import pacman.game.info.GameInfo;
import pacman.game.internal.Node;

/**
 * Created by Piers on 15/02/2016.
 */
public class POPacMan extends PacmanController {

    private static final int MIN_DISTANCE = 20;
    private Random random = new Random();
    ExtractForm extractForm;
    Game myGame;
    int currentLive = -1;

    boolean isExtractForm = false;

    public POPacMan() {
        if (isExtractForm) {
            extractFormInit();
        }

    }

    public void extractFormInit() {
        extractForm = new ExtractForm();
        extractForm.setSize(113 * 4 + 50, 130 * 4 + 50);
        extractForm.setVisible(true);
        extractForm.setBackground(Color.black);
        extractForm.setForeground(Color.white);
    }

    /*
    @Override
    public MOVE getMove(Game game, long timeDue) {
        
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
            if (pillStillAvailable == null) continue;
            if (game.isPillStillAvailable(i)) {
                targets.add(pills[i]);
            }
        }

        for (int i = 0; i < powerPills.length; i++) {            //check with power pills are available
            Boolean pillStillAvailable = game.isPillStillAvailable(i);
            if (pillStillAvailable == null) continue;
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
     */
    @Override
    public MOVE getMove(Game game, long timeDue) {

        return simulateMove(game);

    }
    MCTSNode root;
    boolean firstTime = true;

    public MOVE simulateMove(Game game) {

        int currentPacMan = game.getPacmanCurrentNodeIndex();
        MOVE currentMove = game.getPacmanLastMoveMade();

        double startTime = System.nanoTime();

        // clone game
        String strGameState = game.getGameState();
        Game gameX = new Game(0);
        gameX.setGameState(strGameState);

        if (isExtractForm) {
            extractForm.game = gameX;
            extractForm.updateGameInformation(game);
        }

         
        boolean isCreateMCTS;
        isCreateMCTS = false;
        
        // DEBUG
        
        if (game.wasPacManEaten())
            System.out.println("\033[31mDIEEE");
        
        
        
        if (root == null || game.wasPacManEaten() || gameX.isJunction(currentPacMan) || MCTSNode.isConner(gameX, currentPacMan, currentMove)) {

            boolean isSafe = false;
            isCreateMCTS = true;
            
//           if (root!=null)
//            root.select();
//            if ( !game.wasPacManEaten() && root!=null && root.listChild!=null && root.listChild[root.selectedChild].listChild != null && root.listChild[root.selectedChild].childNodeIndex.length > 1 && (game.getTimeOfLastGlobalReversal() != (game.getCurrentLevelTime() - 1))) {
//                 
//                 
//                root = root.listChild[root.selectedChild];
//                root.parentNode = null;
//            //    root.createEntireTree(root, 0,true);
//              
//            } else 
            {
               
                root = new MCTSNode();
                root.parentNode = null;
                root.setGame(gameX);
                root.init();
                root.createEntireTree(root, 0);

               
            }
            
            for (int i = 0; i < 80; i++) {
                    root.setGame(gameX);
                    MCTSNode.runMCTS(root, root.game.getNumberOfActivePills());
                }
        }
        

        MOVE nextMove = root.selectBestMove(gameX, isCreateMCTS);
        gameX.setGameState(strGameState);

//         double endTime =  System.nanoTime();
//         double thinkingTime =   (endTime-startTime)/1000000.0;
//         if (thinkingTime!=0){
//        System.out.print("thinkingTime : " + thinkingTime);
//         if (thinkingTime>39)
//             System.out.println(" FAIL");
//         else System.out.println("");
//         }
        return nextMove;

    }

}
