package org.atoiks.seihou;

public class App {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ThreadedRunner.getInstance().run(new ProjectSeihouGame());
    }
}