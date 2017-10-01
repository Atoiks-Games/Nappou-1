package org.atoiks.seihou;

import com.ymcmp.jine.Game;
import com.ymcmp.jine.Java2DEnv;
import com.ymcmp.jine.Environment;
import com.ymcmp.jine.ThreadedRunner;

public class App {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Environment env = new Java2DEnv();

        Game game = new ProjectSeihouGame();
        env.attachGame(game);
        ThreadedRunner.getInstance().start(env);
    }
}
