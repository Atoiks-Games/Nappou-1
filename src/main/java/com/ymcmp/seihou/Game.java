package com.ymcmp.seihou;

/**
 *
 * @author YTENG
 */
public interface Game {

    /**
     * Called when the game begins
     */
    public abstract void init();

    /**
     * Called when the game ends
     */
    public abstract void destroy();

    /**
     * Called on each update
     *
     * @param deltaT Amount of time passed from last update in milliseconds
     */
    public abstract void update(long deltaT);

    /**
     * Called on each rendering update
     */
    public abstract void render();

    /**
     * Checks if the application is still running
     *
     * @return true if still runnning, false otherwise
     */
    public abstract boolean isRunning();
}
