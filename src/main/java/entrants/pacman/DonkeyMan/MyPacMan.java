package entrants.pacman.DonkeyMan;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;
import pacman.controllers.PacmanController;
import pacman.game.Constants;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/*
 * This is the class you need to modify for your entry. In particular, you need to
 * fill in the getMove() method. Any additional classes you write should either
 * be placed in this package or sub-packages (e.g., entrants.pacman.username).
 */
public class MyPacMan extends PacmanController {

    private MOVE myMove = MOVE.NEUTRAL;

    MCTSNode root;
    boolean firstTime = true;
    int currentLevel = -1;

    public MyPacMan() {
     
    }
    int lastIndex =0 ;
 @Override
    public MOVE getMove(Game game, long timeDue) {
       
     
        // clone game
        String strGameState = game.getGameState();
        Game gameX = new Game(0);
        gameX.setGameState(strGameState);
        
//        if (gameX.wasPacManEaten()){
//           System.out.println("DIE DIE DIE !!!!");
//            return MOVE.NEUTRAL;
//        }
        
            {
                root = new MCTSNode();
                root.init(gameX );
                root.createEntireTree(root, 0);
                MCTSNode.currentTactic = 0;
            }
        
            
         
        int numActivePill = gameX.getNumberOfActivePills();
        
        // get timeOfEidibleGhost
        int ghostTimeInit = 0;
        int numOfGhostInRange = 0;
        boolean isGhostInRange = false;
        EnumMap<GHOST, Integer> listEdibleGhost = new EnumMap<>(GHOST.class);

        
        for( GHOST ghost : GHOST.values()){
            
               int time = gameX.getGhostEdibleTime(ghost);                
               time = Integer.max(0,time);
               double len ;
               if (time!=0){
                   len = gameX.getDistance(gameX.getPacmanCurrentNodeIndex(), gameX.getGhostCurrentNodeIndex(ghost), Constants.DM.PATH);
                   if (len < time) isGhostInRange = true;
               }
               
               listEdibleGhost.put(ghost, time);
        }
        
        
        // RUN MCTS
        while (System.currentTimeMillis() < (timeDue -3)) {
            root.init(gameX);
            MCTSNode.runMCTS(root, numActivePill,listEdibleGhost);
        }
        
        // re-check tactic
         MCTSNode.currentTactic = 0;
         if ((root.maxViValue[0]) > MCTSNode.NOMAL_MIN_SURVIVAL)  
             if (isGhostInRange)
                MCTSNode.currentTactic = 2;
             else 
                 MCTSNode.currentTactic = 1;
         
         
 
        MOVE nextMove = root.selectBestMove(gameX);
     //   MCTSNode.print(root);
//        System.out.println("");
//        if (System.currentTimeMillis() < timeDue) System.out.println("OK");
//        else System.out.println("FAIL");
//    
       
        
        return nextMove;
        
        
        

    }
}
