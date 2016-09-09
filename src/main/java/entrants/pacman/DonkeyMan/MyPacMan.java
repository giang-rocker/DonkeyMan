package entrants.pacman.DonkeyMan;

import static java.lang.Integer.max;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;
import static java.util.Objects.isNull;
import java.util.Random;
import jdk.nashorn.internal.runtime.regexp.joni.EncodingHelper;
import pacman.controllers.PacmanController;
import pacman.game.Constants;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import pacman.game.info.GameInfo;
import pacman.game.internal.Ghost;
import pacman.game.internal.POType;

/*
 * This is the class you need to modify for your entry. In particular, you need to
 * fill in the getMove() method. Any additional classes you write should either
 * be placed in this package or sub-packages (e.g., entrants.pacman.username).
 */
public class MyPacMan extends PacmanController {

    private MOVE myMove = MOVE.NEUTRAL;

    Game lastStage;

    MCTSNode root;
    boolean firstTime = true;
    int currentMazeIndex;
    public boolean doseReachNewMaze;
    int lastIndex = 0;
    public int simmulateTime = 0;
    public int moves = 0;

    public MyPacMan() {
        simmulateTime = 0;
        moves = 0;
        lastStage = new Game(0);
        lastStage.PO_TYPE = POType.RADIUS;
           lastStage.SIGHT_LIMIT = 100000;
        doseReachNewMaze = false;
        currentMazeIndex = -1;
    }

    int Maxlen = 0;

    boolean convertTrueFalse(boolean f) {
        if (!isNull(f)) {
            return f;
        }

        return false;
    }

    String mergeGameState(Game game) {
        Random R = new Random();
        // set new maze
        if (doseReachNewMaze) {
            lastStage = new Game(0, currentMazeIndex);
         }

        //synconize
        // ghost Position
        StringBuilder sb = new StringBuilder();

        sb.append(game.getMazeIndex() + "," + game.getTotalTime() + "," + game.getScore() + "," + game.getCurrentLevelTime() + "," + game.getCurrentLevel() + ","
                + game.getPacmanCurrentNodeIndex() + "," + game.getPacmanLastMoveMade() + "," + game.getPacmanNumberOfLivesRemaining() + "," + (game.getPacmanNumberOfLivesRemaining() > 0) + ",");
        boolean isRandomPosition = false;
        for (GHOST ghost : GHOST.values()) {
            int ghostPosition = game.getGhostCurrentNodeIndex(ghost);
            int lastPosition = lastStage.getGhostCurrentNodeIndex(ghost);

            if (ghostPosition == -1 && lastPosition != -1) {
                int esp = R.nextInt(100);
                if (esp>20)
                ghostPosition = lastPosition;
                else {
                  ghostPosition = R.nextInt(game.getCurrentMaze().graph.length);
                  isRandomPosition = true;
                }
            } else if ((ghostPosition == -1 && lastPosition == -1)) {
                ghostPosition = R.nextInt(game.getCurrentMaze().graph.length);
                isRandomPosition = true;
            }
          
            
            if (game.wasPacManEaten() || game.wasGhostEaten(ghost)) {
                ghostPosition = game.getGhostInitialNodeIndex();
            }

            int edibleTime  = max (game.getGhostEdibleTime(ghost), lastStage.getGhostEdibleTime(ghost));

           // move
            MOVE ghostLastMove = game.getGhostLastMoveMade(ghost);
            MOVE ghostLastLastMove = lastStage.getGhostLastMoveMade(ghost);

            if ((ghostLastMove == null && ghostLastLastMove != null)) {
                if (!isRandomPosition)
                ghostLastMove = ghostLastLastMove;
                else 
                 ghostLastMove = MOVE.NEUTRAL;
                
                   
            } else if ((ghostLastMove == null && ghostLastLastMove == null)) {
                ghostLastMove = MOVE.NEUTRAL;
            }
            
            if (game.wasPacManEaten() || game.wasGhostEaten(ghost)) {
                ghostLastMove = MOVE.NEUTRAL;
            }
            
            
            //   System.out.println(ghostLastMove +"FF");
            sb.append(ghostPosition + "," + edibleTime + "," + game.getGhostLairTime(ghost) + "," + ghostLastMove + ",");
        }

        for (int i = 0; i < game.getNumberOfPills(); i++) {

            Boolean isPillAvailable = game.isPillStillAvailable(i);
            if (isNull(isPillAvailable)) 
                isPillAvailable = false;
            

            Boolean isLastPillAvailable = lastStage.isPillStillAvailable(i);
            if (isNull(isLastPillAvailable)) 
                isLastPillAvailable = false;
            

            if (isPillAvailable || (!isPillAvailable && isLastPillAvailable)) {
                sb.append("1");
            } else {
                sb.append("0");
            }
        }

        sb.append(",");

        for (int i = 0; i < game.getNumberOfPowerPills(); i++) {
            Boolean isPowerPillAvailable = game.isPowerPillStillAvailable(i) ;
                    
            if(isNull(isPowerPillAvailable))
                  isPowerPillAvailable=  false;
            
            Boolean isPowerLastPillAvailable = lastStage.isPowerPillStillAvailable(i);
            
            if (isNull(isPowerLastPillAvailable))
               isPowerLastPillAvailable= false ;

            if (isPowerPillAvailable || (!isPowerPillAvailable && isPowerLastPillAvailable)) {
                sb.append("1");
            } else {
                sb.append("0");
            }
        }

        sb.append(",");
        sb.append(game.getTimeOfLastGlobalReversal());
        sb.append(",");
        sb.append(game.wasPacManEaten());
        sb.append(",");

        for (GHOST ghost : GHOST.values()) {
            sb.append(game.wasGhostEaten(ghost));
            sb.append(",");
        }

        sb.append(game.wasPillEaten());
        sb.append(",");
        sb.append(game.wasPowerPillEaten());

        return sb.toString();

    }

    @Override
    public MOVE getMove(Game game, long timeDue) {
 {
     if (timeDue==-1) timeDue = System.currentTimeMillis() + Constants.DELAY;
        
        
        // check new maze reach
        if (currentMazeIndex != game.getMazeIndex()) {
            currentMazeIndex = game.getMazeIndex();
            doseReachNewMaze = true;
        } else {
            doseReachNewMaze = false;
        }

        String megGameStageX ="";
        megGameStageX = mergeGameState(game);

        Game gameX = new Game(0);
    
          
        gameX.setGameState(megGameStageX);
        gameX.updateGame();
        
        lastStage.setGameState(megGameStageX);
        lastStage.updateGame();
    
        if (gameX.wasPowerPillEaten()) {
            MCTSNode.hasJustChangeMove = false;
        }

        for (GHOST ghost : GHOST.values()) {
            if (gameX.wasGhostEaten(ghost)) {
                MCTSNode.hasJustChangeMove = false;
            }
        }

        {
            root = new MCTSNode();
            root.init(gameX);
            root.createEntireTree(root, 0);
            MCTSNode.currentTactic = 0;
        }

        int numActivePill = gameX.getNumberOfActivePills();

        // get timeOfEidibleGhost
            
        int ghostTimeInit = 0;
        int numOfGhostInRange = 0;
        boolean isGhostInRange = false;
        EnumMap<GHOST, Integer> listEdibleGhost = new EnumMap<>(GHOST.class);
        
         
         for (GHOST ghost : GHOST.values()) {
               
            int time = gameX.getGhostEdibleTime(ghost);
             
            time = Integer.max(0, time);
            
             double len;
            if (time != 0) {
               
                     isGhostInRange = true;
              
           //     numOfGhostInRange++;
            }

            listEdibleGhost.put(ghost, time);

        }
         
    //     System.out.println("\nEldible Ghost " + numOfGhostInRange);
      
        // RUN MCTS
        while (System.currentTimeMillis() < (timeDue - 2)) {
            root.init(gameX);
            MCTSNode.runMCTS(root, numActivePill, listEdibleGhost);
        }

        // re-check tactic
        MCTSNode.currentTactic = 0;
        if ((root.maxViValue[0]) > MCTSNode.NOMAL_MIN_SURVIVAL) {
            if (isGhostInRange && root.maxViValue[2] - 0 > 0.000001) {
                MCTSNode.currentTactic = 2;
            } else if (root.maxViValue[1] - 0 > 0.000001) {
                MCTSNode.currentTactic = 1;
            }
        }

        MOVE nextMove = root.selectBestMove(gameX);

       SimulateGhostMove ghostsMove = new SimulateGhostMove();
        EnumMap<GHOST, MOVE> listGhostMove = new EnumMap<>(GHOST.class);

        // STATERGY MOVESopy
        listGhostMove = ghostsMove.getMove(lastStage);

        lastStage.advanceGame(nextMove, listGhostMove);

     //   System.out.println(root.new_visitedCount);
       //System.out.println(MCTSNode.currentTactic + " value  " + root.maxViValue[MCTSNode.currentTactic]);
        //  moves++;
        //  simmulateTime+=root.new_visitedCount;
           
        return nextMove;
        }
    }
}
