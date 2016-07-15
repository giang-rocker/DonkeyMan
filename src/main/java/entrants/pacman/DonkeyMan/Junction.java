/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entrants.pacman.DonkeyMan;

import pacman.game.Constants.MOVE;

/**
 *
 * @author Giang
 */
public class Junction {

    int nodeIndex;
    int nodeParrent;
    MOVE moveToReach; // from currentPacman Position
    int distanceFromParent;
    double distanceByEngine;

    int pacmanPosition; // just for refferente
    boolean isSafe;

    public static enum POSITION {
        FRONT ,
        BACK,
        BESIDE
    };
    
    POSITION position ; // refer to currentpacman position , related to moveToReach
    
    
    boolean isFront () {
        return (this.position == POSITION.FRONT);
    }
     boolean isBack () {
        return (this.position == POSITION.BACK);
    }
      boolean isBeside () {
        return (this.position == POSITION.BESIDE);
    }
      
      Junction(){
      nodeIndex = -1;
      moveToReach = MOVE.NEUTRAL;
      distanceFromParent = 0;
      pacmanPosition = -1;
      isSafe = false;
      
      }
}
