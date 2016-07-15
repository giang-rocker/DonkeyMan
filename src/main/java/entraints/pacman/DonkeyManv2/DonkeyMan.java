package entraints.pacman.DonkeyManv2;

import entrants.pacman.DonkeyMan.*;
import pacman.controllers.PacmanController;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/*
 * This is the class you need to modify for your entry. In particular, you need to
 * fill in the getMove() method. Any additional classes you write should either
 * be placed in this package or sub-packages (e.g., entrants.pacman.username).
 */
public class DonkeyMan extends PacmanController {

    private MOVE myMove = MOVE.NEUTRAL;

    MCTSNode root;
    boolean firstTime = true;
    int currentLevel = -1;

    public DonkeyMan() {

    }
    int lastIndex = 0;
    boolean isChangeMove = false;

    @Override
    public MOVE getMove(Game game, long timeDue) {
        MOVE nextMove = MOVE.NEUTRAL;
        isChangeMove = false;
        //Place your game logic here to play the game as Ms Pac-Man
        int currentPacMan = game.getPacmanCurrentNodeIndex();

        // clone game
        String strGameState = game.getGameState();
        Game gameX = new Game(0);
        gameX.setGameState(strGameState);

        if (root == null || currentLevel != gameX.getCurrentLevel()) {
             
            root = new MCTSNode();
            root.init(gameX);
            root.createEntireTree(root, 0);
            MCTSNode.currentTactic = 0;
            nextMove = MOVE.NEUTRAL;
        } else if (  gameX.getPossibleMoves(gameX.getPacmanCurrentNodeIndex()).length==2 && (gameX.getPossibleMoves(gameX.getPacmanCurrentNodeIndex())[0].opposite() == gameX.getPossibleMoves(gameX.getPacmanCurrentNodeIndex())[1])  ) {
        
            boolean isSafeMove = MCTSNode.safeMoveCheck(gameX, root.bestChild.moveToReach, root.bestChild.nodeIndex);

            if (!isSafeMove) {
                MOVE currentMove = root.bestChild.moveToReach;
               
                root = new MCTSNode();
                root.init(gameX);
                 
                root.createEntireTree(root, 0);
                MCTSNode.currentTactic = 0;

                nextMove = currentMove.opposite();
            } else {
             //   System.out.println("xx");
                nextMove = root.bestChild.moveToReach;
      
            }

        } else if (gameX.getPacmanCurrentNodeIndex() == root.bestChild.nodeIndex  || MCTSNode.isConner(gameX, gameX.getPacmanCurrentNodeIndex()) ) {
            root = root.bestChild;
            nextMove = root.selectBestMove(gameX);
            
                root = new MCTSNode();
                root.init(gameX);
                root.createEntireTree(root, 0);
                MCTSNode.currentTactic = 0;

           
        }

        while (System.currentTimeMillis() < (timeDue - 3)) {
            root.init(gameX);
            MCTSNode.runMCTS(root, gameX.getNumberOfActivePills());
        }

        if ((root.maxViValue[0]) > MCTSNode.NOMAL_MIN_SURVIVAL) {
            MCTSNode.currentTactic = 1;
        } else {
            MCTSNode.currentTactic = 0;
        }

        currentLevel = gameX.getCurrentLevel();
        //     System.out.println(nextMove);
        
        if (root.listChild.size()==1) root.bestChild = root.listChild.get(0);
        else 
        root.selectBestMove(gameX);
        

        return nextMove;

    }
}
