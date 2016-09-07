/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entrants.pacman.DonkeyMan;
import examples.commGhosts.POCommGhosts;

import pacman.Executor;

/**
 *
 * @author Giang
         */
public class MainTestMyDonKeyMan {
 

    public static void main(String[] args) {

        Executor executor = new Executor(true, true);
        MyPacMan X = new MyPacMan();
        
        executor.runGameTimed(X, new POCommGhosts(50), true);
       
    }
  
}

 