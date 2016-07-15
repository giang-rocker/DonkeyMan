/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entraints.pacman.DonkeyManv2;

import entrants.pacman.DonkeyMan.*;
import examples.commGhosts.POCommGhosts;
import java.util.EnumMap;
import pacman.controllers.Controller;
import pacman.controllers.examples.po.POGhosts;
import pacman.game.Constants;
import static pacman.game.Constants.DELAY;
import pacman.game.Constants.MOVE;

import pacman.game.Game;

/**
 *
 * @author Giang
 */
public class MultiTest {
    
    public static void main(String[] args) {
       Game gameX = new Game(0);
        Controller<MOVE> pacManController = new MyPacMan();
        Controller<EnumMap<Constants.GHOST, MOVE>> ghostController = new POCommGhosts(50);
        
        new Thread(pacManController).start();
        new Thread(ghostController).start();
        int move =0 ;
        int DELAY = 40;
        int numOfGame = 10;
        
        for (int i =0; i < numOfGame; i ++) {
            double startTime = System.currentTimeMillis();
        while (!gameX.gameOver()) {
            pacManController.update(gameX.copy(), System.currentTimeMillis() + DELAY);
            ghostController.update(gameX.copy(), System.currentTimeMillis() + DELAY);

            try {
                Thread.sleep(DELAY);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            gameX.advanceGame(pacManController.getMove(), ghostController.getMove());
            
            move ++;
             
        }
        
        double endTime = System.currentTimeMillis();
        System.out.println("Game " + i + " is finished at " + gameX.getCurrentLevel() + " level(s) ;"
                                                            + move +" move(s);"
                                                            + " Score : "  + gameX.getScore()
                                                            + " Time : "  + (((endTime - startTime) / 1000) / 60) +" minutes "
        
                                                                                );
         
        gameX = new Game(0);
        move=0;
        }
    }
    
}
