package org.atoiks.seihou;

import org.atoiks.core.Game;
import org.atoiks.core.Java2DEnv;
import org.atoiks.core.Environment;
import org.atoiks.core.ThreadedRunner;

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
