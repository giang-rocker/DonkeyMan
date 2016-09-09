/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entrants.pacman.DonkeyMan;

import examples.commGhosts.POCommGhosts;
import examples.poPacMan.POPacMan;
import java.io.BufferedWriter;
import java.io.FileWriter;

import pacman.Executor;
import pacman.controllers.Controller;
import pacman.controllers.examples.po.POGhosts;
import pacman.game.Constants;
import pacman.game.Game;
import pacman.game.util.Stats;

/**
 *
 * @author giang-rocker
 */
public class TestRecord {
    
    public static void main(String[] args) {
     
        Executor executor = new Executor(true, true);
         POPacMan X = new POPacMan();
        
         int time = 0;
         
         String fileName ="record.txt";
         
         
         for (int i = 0; i < 1000; i++) {
         
           Game  gameX = new Game(0);
          
            while (!gameX.gameOver()) {
                Controller<Constants.MOVE> pacManController = new POPacMan();
                SimulateGhostMove ghostController = new SimulateGhostMove(50);
                
                 gameX.advanceGame(pacManController.getMove(gameX.copy(), System.currentTimeMillis() + 40), ghostController.getMove(gameX.copy()));

               
            
            }
              
        }
         
         executor.replayGame(fileName, true);
         
         System.out.println("END");
    }
    
}
