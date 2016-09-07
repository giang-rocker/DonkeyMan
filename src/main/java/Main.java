import entrants.pacman.DonkeyMan.MyPacMan;
import examples.commGhosts.POCommGhosts;
import pacman.Executor;
import examples.poPacMan.POPacMan;
 
/**
 * Created by pwillic on 06/05/2016.
 */
public class Main {

    public static void main(String[] args) {

        Executor executor = new Executor(true, true);
        MyPacMan X = new MyPacMan();
        
        executor.runGameTimed(X, new POCommGhosts(50), true);
      
    }
}
