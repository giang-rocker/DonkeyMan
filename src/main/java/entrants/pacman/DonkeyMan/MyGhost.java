/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entrants.pacman.DonkeyMan;
 
import java.util.Random;
import pacman.game.Constants;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import pacman.game.internal.Ghost;


/**
 *
 * @author giang-rocker
 */
public class MyGhost {
    
    public int currentNodeIndex, edibleTime, lairTime;
    public Constants.GHOST type;
    public Constants.MOVE lastMoveMade;
    int lastTimeSeen;
    int lastPositionSeen;
    boolean isRealGhost;
    final int DEFAULT_EDIBLETIME = 50;
    
    public MyGhost () {
    lastTimeSeen= -1;
    isRealGhost = false;
    }
    
    public MyGhost(Constants.GHOST type, int currentNodeIndex, int edibleTime, int lairTime, Constants.MOVE lastMoveMade) {
        this.type = type;
        this.currentNodeIndex = currentNodeIndex;
        this.edibleTime = edibleTime;
        this.lairTime = lairTime;
        this.lastMoveMade = lastMoveMade;
         
    }
    
   // generate new GHOST at random node from NUmOfNode
   public void generateGhost (Constants.GHOST type, boolean isEdible, int numOfNode) {
        this.type = type;
        Random R = new Random ();
        this.currentNodeIndex = R.nextInt(numOfNode);
        
        if (isEdible)
        this.edibleTime = DEFAULT_EDIBLETIME;
        else 
        this.edibleTime = 0;
        
        this.lairTime = 0;
        this.lastMoveMade = lastMoveMade;
        this.lastTimeSeen =0 ;
        this.isRealGhost = false;
        this.lastPositionSeen = -1;
   
   }
   
   // generate Ghost from current unseen ghost
   public void simmulateGhost (Constants.GHOST type,boolean isEdible, Game game) {
         int numOfNode = game.getNumberOfNodes();
        this.type = type;
        Random R = new Random(0);
        //random position base on last time since & last position
        double distince = 0;
        int randomPosition  = -1;
        while (distince < lastTimeSeen) {
        randomPosition = R.nextInt(numOfNode);
            distince = game.getDistance(lastPositionSeen, randomPosition, Constants.DM.MANHATTAN);
        
        }
        
        this.currentNodeIndex = randomPosition;
        
        
        if (this.edibleTime<0 && isEdible)
            this.edibleTime = DEFAULT_EDIBLETIME;
        
        this.lairTime = 0;
        MOVE [] possibleMove = game.getPossibleMoves(this.currentNodeIndex);
        this.lastMoveMade = possibleMove[R.nextInt(possibleMove.length)];
        
        this.lastTimeSeen ++ ;
        isRealGhost = false;
   
   }
   
   
    
    
}
