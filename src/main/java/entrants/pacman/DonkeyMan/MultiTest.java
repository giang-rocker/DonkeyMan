/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entrants.pacman.DonkeyMan;

import examples.commGhosts.POCommGhosts;
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
import static pacman.game.Constants.DELAY;
import pacman.game.Constants.MOVE;

import pacman.game.Game;
import pacman.game.util.Stats;

/**
 *
 * @author Giang
 */
public class MultiTest {

    public static void main(String[] args) throws IOException {
        System.out.println("START EXPERIEMNT POPACMAN");
         
        
        Executor executor = new Executor(true,true);
        
       Stats stats[]= executor.runExperiment(new MyPacMan(), new POCommGhosts(50), 500," DONE ");
        
       for (int i =0; i < stats.length; i ++)
            System.out.println(stats[i]);
        
        /*
        
        File log = new File("DonKeyManExperiment.txt");

        String header = "Game,Score\n";
        FileWriter fileWriter = new FileWriter(log, true);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

        // write describle
        bufferedWriter.write(header);
         bufferedWriter.write("PLAYER,POPACMAN\n"); 
      

        bufferedWriter.close();
        fileWriter.close();
        Game gameX = new Game(0);
        Executor executor = new Executor(true, true);

        int move = 0;
        int DELAY = 40;
        int numOfGame = 50;
       
        fileWriter = new FileWriter(log, true);
        bufferedWriter = new BufferedWriter(fileWriter);
        for (int i = 0; i < numOfGame; i++) {
            MyPacMan X  = new MyPacMan();
            //Controller ghostController = ;
            System.out.print("Running game " +(i+1) + "/" + numOfGame +" Score ");
           
            int currentScore = executor.runGame(X, new POCommGhosts(50), true,40);
            
            System.out.println(currentScore);
            String info = (i + "," + currentScore + "\n");
            

            bufferedWriter.write(info);
            X.terminate();
          
            bufferedWriter.close();
            fileWriter.close();
            gameX = new Game(0);

        }
        */
        System.out.println("END");

    }

}
