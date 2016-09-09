package entrants.pacman.DonkeyMan;

import static java.lang.Integer.max;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import static java.util.Objects.isNull;
import java.util.Random;
import jdk.nashorn.internal.runtime.regexp.joni.EncodingHelper;
import pacman.controllers.PacmanController;
import pacman.game.Constants;
import static pacman.game.Constants.EDIBLE_TIME;
import static pacman.game.Constants.EDIBLE_TIME_REDUCTION;
import pacman.game.Constants.GHOST;
import static pacman.game.Constants.LEVEL_RESET_REDUCTION;
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
   // ExtractorForm extractor;
    public MyPacMan() {
        simmulateTime = 0;
        moves = 0;
        lastStage = new Game(0);
           doseReachNewMaze = false;
        currentMazeIndex = -1;
    //    extractor =  new ExtractorForm(lastStage);
    //    extractor.setVisible(true);
    }

    int Maxlen = 0;
    
    boolean listPillRecord [];
    
    String mergeGameState(Game game) {
        Random R = new Random();
        // set new maze
        if (doseReachNewMaze) {
            lastStage = new Game(0, currentMazeIndex);
            listPillRecord = new boolean[lastStage.getNumberOfPills()];
            Arrays.fill(listPillRecord, false);
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
            
            int edibleTime = game.getGhostEdibleTime(ghost);
            int lastEdibleTime = lastStage.getGhostEdibleTime(ghost);
            
            if ( edibleTime< 0 && lastEdibleTime>0  ) edibleTime = lastEdibleTime;
            
            if (game.wasPowerPillEaten()) edibleTime = 50;
            
            
           
            
 

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
         
        int count = 0;
        for (int i = 0; i < game.getActivePillsIndices().length; i++) {
            int pillIndex = game.getPillIndex(game.getActivePillsIndices()[i]);
             listPillRecord[pillIndex] = true;
               
        }
        
        if ( lastStage.getPillIndex(lastStage.getPacmanCurrentNodeIndex()) != -1 ) 
        listPillRecord[ lastStage.getPillIndex(lastStage.getPacmanCurrentNodeIndex()) ] = false;
         
        for (int i =0; i < listPillRecord.length; i ++)
            if (listPillRecord[i]==true &&  lastStage.isPillStillAvailable(i)== true ) sb.append("1");
            else sb.append("0");

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
        
       //   extractor.game = lastStage;
       //   extractor.listPill = listPillRecord;
        
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
              
                numOfGhostInRange++;
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
            if (isGhostInRange && root.maxViValue[2]-0.001 >0) {
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

    //    System.out.println(root.new_visitedCount +"" );//" " + lastStage.getActivePillsIndices().length );
   //   System.out.println(MCTSNode.currentTactic + " value  " + root.maxViValue[MCTSNode.currentTactic]);
        //  moves++;
        //  simmulateTime+=root.new_visitedCount;
        
        GameInfo X = new GameInfo(lastIndex);
        Game XX = new Game(0);
        XX.getGameFromInfo(X);
        
        return nextMove;
        }
    }
}
