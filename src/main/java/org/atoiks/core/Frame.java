package org.atoiks.core;

import java.awt.Image;

/**
 *
 * @author YTENG
 */
public interface Frame extends Environment {

    /**
     * Sets the size of the frame
     *
     * @param width
     * @param height
     */
    public abstract void setSize(int width, int height);

    /**
     * Sets the frame's ability to resize
     *
     * @param resizability
     */
    public abstract void setResizable(boolean resizability);

    /**
     * Sets the frame's visibility
     *
     * @param visibility
     */
    public abstract void setVisible(boolean visibility);

    /**
     * Moves frame to center of the screen
     */
    public abstract void moveToCenter();

    /**
     * Moves frame to specified location
     *
     * @param x
     * @param y
     */
    public abstract void moveTo(int x, int y);

    /**
     * Sets the icon of the frame
     *
     * @param img
     */
    public abstract void setIcon(Image img);

    /**
     * Sets the title of the frame
     *
     * @param name
     */
    public abstract void setTitle(String name);

    /**
     * Stop the game
     */
    public abstract void abort();
}
