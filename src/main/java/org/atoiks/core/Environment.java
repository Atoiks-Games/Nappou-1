package org.atoiks.core;

/**
 *
 * @author YTENG
 */
public interface Environment {

    /**
     * Called when the environment starts up
     */
    public abstract void initEnv();

    /**
     * Called when the environment shuts down
     */
    public abstract void destroyEnv();

    /**
     * Called on each update
     *
     * @param deltaT Amount of time passed from last update in milliseconds
     */
    public abstract void invokeUpdate(long deltaT);

    /**
     * Called on each rendering update
     */
    public abstract void invokeRender();

    /**
     * Checks if the application is still running
     *
     * @return true if still running, false otherwise
     */
    public abstract boolean isRunning();

    /**
     * Attaches a game to an environment
     *
     * @param game
     */
    void attachGame(Game game);

}
