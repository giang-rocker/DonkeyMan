package examples.poPacMan;

import examples.commGhosts.POCommGhosts;
import examples.poPacMan.ExtractForm;
import examples.poPacMan.MCTSNode;
import examples.poPacMan.POPacMan;
import java.awt.Color;
import static java.lang.Thread.sleep;
import java.util.Random;
import pacman.Executor;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import static java.lang.Thread.sleep;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Giang
 */
public class MainTestMCTS {

    static public Game gameX;
    static public MCTSNode root;
    static public ExtractForm extractForm;
   
    public static void main(String[] args) throws InterruptedException {

        gameX = new Game(0);
        extractForm = new ExtractForm(gameX);

        extractForm.setSize(113 * 4 + 50, 600);
        extractForm.setVisible(true);
        extractForm.setBackground(Color.black);
        extractForm.setForeground(Color.white);
        extractForm.updateGameInformation(gameX);
        extractForm.randomPosition();
        
       
       
    }

}
