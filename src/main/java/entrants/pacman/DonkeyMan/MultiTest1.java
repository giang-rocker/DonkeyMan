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
import pacman.controllers.HumanController;
import pacman.controllers.examples.po.POGhosts;
import pacman.game.Constants;
import static pacman.game.Constants.DELAY;
import pacman.game.Constants.MOVE;

import pacman.game.Game;
import pacman.game.GameView;
import pacman.game.comms.BasicMessenger;
import pacman.game.comms.Messenger;
import pacman.game.util.Stats;

/**
 *
 * @author Giang
 */
public class MultiTest1 {

    public Stats[] runExperiment(Controller<MOVE> pacManController, Controller<EnumMap<Constants.GHOST, MOVE>> ghostController, int trials, String description, int tickLimit, boolean visual) {
        Stats stats = new Stats(description);
        Stats ticks = new Stats(description + " Ticks");
        Random rnd = new Random(0);
        Game game;
        Messenger messenger;
        messenger = new BasicMessenger(0, 1, 1);

        Long startTime = System.currentTimeMillis();
        for (int i = 0; i < trials;) {

            try {
                game = new Game(rnd.nextLong(), messenger.copy());

                GameView gv = null;

                if (visual) {
                    gv = new GameView(game).showGame();
                    gv.getFrame().setLocation(450, 600);
                    gv.getFrame().setTitle("Experiment for MyPacMan1");

                    if (pacManController instanceof HumanController) {
//                System.out.println("Here");
                        gv.setFocusable(true);
                        gv.requestFocus();
                        gv.setPO(true);
                        gv.addKeyListener(((HumanController) pacManController).getKeyboardInput());
//                System.out.println("KeyListener added");
                    }
                }

                while (!game.gameOver()) {
                    if (tickLimit != -1 && tickLimit < game.getCurrentLevelTime()) {
                        break;
                    }
                    game.advanceGame(
                            pacManController.getMove(game.copy((true) ? Constants.GHOST.values().length + 1 : -1), System.currentTimeMillis() + DELAY),
                            ghostController.getMove(game.copy(), System.currentTimeMillis() + DELAY));

                    if (visual) {
                        gv.repaint();
                    }
                }
                stats.add(game.getScore());
                ticks.add(game.getCurrentLevelTime());
                i++;
                System.out.println("Game finished: " + i + "  Score: " + game.getScore() + " Maze: " + game.getCurrentLevel() + " Time: " + game.getTotalTime());

                gv.getFrame().dispose();

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        long timeTaken = System.currentTimeMillis() - startTime;
        stats.setMsTaken(timeTaken);
        ticks.setMsTaken(timeTaken);

        return new Stats[]{stats, ticks};
    }

    public static void main(String[] args) throws IOException {
        System.out.println("START EXPERIEMNT MY PACMAN 1 - GHOST GENERATE MOVE - EDIBLE GHOST RESET");

        int numOfGame = Integer.parseInt(args[0]);
        System.out.println("RUN " + numOfGame + " games");

        MultiTest1 mt = new MultiTest1();

        Stats stats[] = mt.runExperiment(new MyPacMan1(), new POCommGhosts(50), numOfGame, " DONE ", -1, true);
        for (int i = 0; i < stats.length; i++) {
            System.out.println(stats[i]);
        }

        System.out.println("END");

    }

}
