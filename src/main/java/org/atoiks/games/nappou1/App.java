package org.atoiks.games.nappou1;

import com.ymcmp.jine.Game;
import com.ymcmp.jine.GameRunner;
import com.ymcmp.jine.Environment;
import com.ymcmp.jine.environments.Java2D;
import com.ymcmp.jine.runners.FixedRateRunner;

public class App {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        final GameRunner runner = new FixedRateRunner();
        final Environment env = new Java2D();
        final Game game = new NappouGame();

        env.attachGame(game);
        runner.start(env);
    }
}
