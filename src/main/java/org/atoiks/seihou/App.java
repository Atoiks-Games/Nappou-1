package org.atoiks.seihou;

import com.ymcmp.jine.Game;
import com.ymcmp.jine.Environment;
import com.ymcmp.jine.GameRunner;
import com.ymcmp.jine.environments.OpenGL;
import com.ymcmp.jine.runners.FixedRateRunner;

public class App {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        final GameRunner runner = new FixedRateRunner();
        final Environment env = new OpenGL(false);
        final Game game = new ProjectSeihouGame();

        env.attachGame(game);
        runner.start(env);
    }
}
