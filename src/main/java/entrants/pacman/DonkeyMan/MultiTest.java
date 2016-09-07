/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entrants.pacman.DonkeyMan;


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

    public static void main(String[] args) {
        Game gameX = new Game(0);
         Executor executor = new Executor(true, true);

        int move = 0;
        int DELAY = 40;
        int numOfGame = 100;

        for (int i = 0; i < numOfGame; i++) {

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
            System.out.println("Game " + (i+1) + " is finished at " + (gameX.getCurrentLevel()+1) + " level(s) ;"
                    + gameX.getTotalTime() + " time step(s);"
                    + " Score : " + gameX.getScore()
                    + " Time : " + (((endTime - startTime) / 1000) / 60) + " minutes "
            );

            gameX = new Game(0);
            move = 0;
        }
    }
}
