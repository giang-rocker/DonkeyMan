/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entrants.pacman.DonkeyMan;


import examples.poPacMan.POPacMan;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.EnumMap;
import pacman.Executor;
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

    public static void main(String[] args) throws IOException  {
        
        File log = new File ("log4.txt");
        
        String header="Game,Level,Score,TimeStep,TimeMinute\n";
        FileWriter fileWriter = new FileWriter(log, true);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        
        // write describle
        
        bufferedWriter.write(header);
            bufferedWriter.write("NEW EDIT 3 DONKEYMAN,,,,\n");
    
       
             bufferedWriter.close();
             fileWriter.close();
        Game gameX = new Game(0);
         Executor executor = new Executor(true, true);

        int move = 0;
        int DELAY = 40;
        int numOfGame = 50;

        for (int i = 0; i < numOfGame; i++) {
          fileWriter = new FileWriter(log, true);
        bufferedWriter = new BufferedWriter(fileWriter);
            System.out.print("Running " + (i + 1) + "/" + numOfGame);

            double startTime = System.currentTimeMillis();
            int currentLevel = -1;
            while (!gameX.gameOver()) {
                Controller<MOVE> pacManController = new MyPacMan();
                SimulateGhostMove ghostController = new SimulateGhostMove(50);
                
                 gameX.advanceGame(pacManController.getMove(gameX.copy(), System.currentTimeMillis() + DELAY), ghostController.getMove(gameX.copy()));

                move++;

                if (currentLevel != gameX.getCurrentLevel()) {
                    System.out.print(" " + gameX.getCurrentLevel());
                    currentLevel = gameX.getCurrentLevel();
                }

           //     pacManController.terminate();
            }
            System.out.println("");
            double endTime = System.currentTimeMillis();
            
          String result = ( Integer.toString(i)+","+Integer.toString(gameX.getCurrentLevel()+1)+","+Integer.toString(gameX.getScore())+","+Integer.toString(gameX.getTotalTime())+","+Double.toString(((endTime - startTime) / 1000) / 60)+"\n");
            bufferedWriter.write(result);
            System.out.println("Game " + (i+1) + " is finished at " + (gameX.getCurrentLevel()+1) + " level(s) ;"
                     + " Score : " + gameX.getScore() + " "
                    + gameX.getTotalTime() + " time step(s);"
                   + " Time : " + (((endTime - startTime) / 1000) / 60) + " minutes "
            );
            
          
            bufferedWriter.close();
            fileWriter.close();
            gameX = new Game(0);
            move = 0;
        }
       
    }
}
