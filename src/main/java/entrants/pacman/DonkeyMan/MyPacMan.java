package entrants.pacman.DonkeyMan;

import pacman.controllers.PacmanController;
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
        
 
    
        
            {
                root = new MCTSNode();
                root.init(gameX );
                root.createEntireTree(root, 0);
                MCTSNode.currentTactic = 0;
            }
 
         
        int numActivePill = gameX.getNumberOfActivePills();
        
        while (System.currentTimeMillis() < (timeDue -10)) {
            root.init(gameX);
            MCTSNode.runMCTS(root, numActivePill);
        }
        
         MCTSNode.currentTactic = 0;
         if ((root.maxViValue[0]) > MCTSNode.NOMAL_MIN_SURVIVAL)  
                MCTSNode.currentTactic = 1;
         
         
 
        MOVE nextMove = root.selectBestMove(gameX);
      //  System.out.println(root.new_visitedCount);
        
//       if (System.currentTimeMillis() < timeDue) System.out.println("OK");
//        else System.out.println("FAIL");
    
       
        
        return nextMove;
        
        
        

    }
}
