package entrants.pacman.DonkeyMan;

import java.util.EnumMap;
import pacman.controllers.PacmanController;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import pacman.game.info.GameInfo;
import pacman.game.internal.Ghost;
import pacman.game.internal.PacMan;

/*
 * This is the class you need to modify for your entry. In particular, you need to
 * fill in the getMove() method. Any additional classes you write should either
 * be placed in this package or sub-packages (e.g., entrants.pacman.username).
 */
public class MyPacMan extends PacmanController {

    private MOVE myMove = MOVE.NEUTRAL;
    GameInfo lastInfo;
    Game lastStage;

    MCTSNode root;
    boolean firstTime = true;
    int currentLevel;
    public int simmulateTime = 0;
    public int moves = 0;

  //  ExtractorForm extractor;

    public MyPacMan() {
        simmulateTime = 0;
        moves = 0;
        lastStage = new Game(0);
        currentLevel = -1;
    //   extractor = new ExtractorForm();
     //   extractor.setVisible(true);
    
    }

    int Maxlen = 0;

    // Game info
    // pill, ghost, power pill
    public void getNewGameInfo(Game game) {
        // set pill
        int listActivePillIndice [] = game.getActivePillsIndices();
        for (int i = 0; i < listActivePillIndice.length; i++) {
            lastInfo.setPillAtIndex(game.getPillIndex(listActivePillIndice[i]), true);
        }

        // set Power pill
        int listActivePowerPillIndice [] = game.getActivePowerPillsIndices();
        for (int i = 0; i < listActivePowerPillIndice.length; i++) {
            lastInfo.setPowerPillAtIndex(game.getPowerPillIndex(listActivePowerPillIndice[i]), true);
        }

        //set Pacman
        lastInfo.setPacman(new PacMan(game.getPacmanCurrentNodeIndex(), game.getPacmanLastMoveMade(), game.getPacmanNumberOfLivesRemaining(), false));

        // set Ghost
        for (GHOST ghost : GHOST.values()) {
            // seen ghost -1 : out of vision
            int ghostIndex = game.getGhostCurrentNodeIndex(ghost);
            if (ghostIndex != -1) {
                lastInfo.setGhostIndex(ghost, new Ghost(ghost, ghostIndex, game.getGhostEdibleTime(ghost), game.getGhostLairTime(ghost), game.getGhostLastMoveMade(ghost)));
            } else {
                lastInfo.setGhostIndex(ghost, new Ghost(ghost, game.getCurrentMaze().lairNodeIndex, -1, -1, MOVE.NEUTRAL));
            }

        }

    }
 Game gameX;
    // DEFAULT INFORMATION FROM GAME - REMEMBER PILL & POWER PILL ONLY
    @Override
    public MOVE getMove(Game game, long timeDue) {
        {
           
         
                  
            // check new Maze was reached
            if (currentLevel != game.getCurrentLevel() || game.getScore()==0 ) {
                currentLevel = game.getCurrentLevel();
                lastInfo = game.getBlankGameInfo();
           
            }

            getNewGameInfo(game);
            gameX = game.getGameFromInfo(lastInfo);
            
        //    System.out.println(game.getActivePillsIndices().length + " " + gameX.getActivePillsIndices().length );
            
     //       lastStage = game.getGameFromInfo(lastInfo);
          //  System.out.println(gameX.getMazeIndex() +"Y");
         //   extractor.gameX = gameX.copy(false);

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
             int totalEdilbeTime = 0 ;
            boolean isGhostInRange = false;
            EnumMap<GHOST, Integer> listEdibleGhost = new EnumMap<>(GHOST.class);

            for (GHOST ghost : GHOST.values()) {

                int time = gameX.getGhostEdibleTime(ghost);

                time = Integer.max(0, time);

                double len;
                if (time != 0) {

                    isGhostInRange = true;

                   
                }

                totalEdilbeTime +=time;
                

            }

            //     System.out.println("\nEldible Ghost " + numOfGhostInRange);
            // RUN MCTS
            while (System.currentTimeMillis() < (timeDue - 2)) {
                root.init(gameX);
                MCTSNode.runMCTS(root, numActivePill, totalEdilbeTime);
             }
             // re-check tactic
            MCTSNode.currentTactic = 0;
            if ((root.maxViValue[0]) > MCTSNode.NOMAL_MIN_SURVIVAL) {
                if (isGhostInRange && root.maxViValue[2] - 0.001 > 0) {
                    MCTSNode.currentTactic = 2;
                } else if (root.maxViValue[1] - 0 > 0.000001) {
                    MCTSNode.currentTactic = 1;
                }
            }
 
            
           
            MOVE nextMove = root.selectBestMove(gameX.copy(false));
     
             SimulateGhostMove ghostsMove = new SimulateGhostMove();
            EnumMap<GHOST, MOVE> listGhostMove = new EnumMap<>(GHOST.class);
             
            listGhostMove = ghostsMove.getMove(gameX);
            gameX.advanceGame(nextMove, listGhostMove);
            
            
            //   System.out.println(MCTSNode.currentTactic + " value  " + root.maxViValue[MCTSNode.currentTactic]);
            //  moves++;
            //  simmulateTime+=root.new_visitedCount;
            
            return nextMove;
        }
    }
}
