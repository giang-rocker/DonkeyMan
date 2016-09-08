/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package examples.poPacMan;

import entrants.pacman.DonkeyMan.MyPacMan;
import examples.commGhosts.POCommGhosts;
import pacman.Executor;

/**
 *
 * @author giang-rocker
 */
public class MainTestPO {
    
       public static void main(String[] args) {

        Executor executor = new Executor(true, true);
        POPacMan X = new POPacMan();
        
        executor.runGameTimed(X, new POCommGhosts(50), true);
      
    }
    
}
