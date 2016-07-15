package entrants.pacman.DonkeyMan;


import entrants.pacman.DonkeyMan.ExtractForm;
import entrants.pacman.DonkeyMan.MCTSNode;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
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
public class TestMCTS {

    static public Game gameX;
    static public MCTSNode root;
    static public ExtractForm extractForm;
   
    public static void main(String[] args) throws InterruptedException {
          gameX = new Game(0);
        
        MCTSNode mctsNode = new MCTSNode();
        mctsNode.init(gameX);
        
        extractForm = new ExtractForm(mctsNode.game);
        extractForm.setSize(113 * 4 + 50, 600);
        extractForm.setVisible(true);
        extractForm.setBackground(Color.black);
        extractForm.setForeground(Color.white);
        extractForm.MCTSTree = mctsNode;
        extractForm.updateGameInformation(mctsNode.game);
        extractForm.currentNodeCheckJunction = 386;
        
   //     while (extractForm.autoUpdate());
            
            
       
    }
 
  
    
}
