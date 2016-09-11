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
import java.util.Random;
import pacman.Executor;
import pacman.controllers.Controller;
import pacman.controllers.examples.po.POGhosts;
import pacman.game.Constants;
import static pacman.game.Constants.DELAY;
import pacman.game.Constants.MOVE;

import pacman.game.Game;
import pacman.game.util.Stats;

/**
 *
 * @author Giang
 */
public class MultiTest1 {

     public static void main(String[] args) throws IOException {
        System.out.println("START EXPERIEMNT MY PACMAN - ORIGINAL INFORMATION - PILL MEMORIESED");
         
        int numOfGame = Integer.parseInt(args[0]);
         System.out.println("RUN " + numOfGame + " games");
        
        Executor executor = new Executor(true,true);
        
       Stats stats[]= executor.runExperiment(new MyPacMan1(), new POCommGhosts(50), numOfGame," DONE ");
        
       for (int i =0; i < stats.length; i ++)
            System.out.println(stats[i]);
        
       
        System.out.println("END");

    }

}
