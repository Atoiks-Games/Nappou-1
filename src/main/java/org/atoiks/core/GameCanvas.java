package org.atoiks.core;

import java.awt.Graphics;

/**
 *
 * @author YTENG
 */
public interface GameCanvas {

    public abstract int getWidth();

    public abstract int getHeight();

    public abstract void render(Graphics g);
}
