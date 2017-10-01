package org.atoiks.core;

import java.awt.Graphics;

/**
 *
 * @author YTENG
 */
public abstract class Game {

    protected KeyInput keyboard;
    protected MouseInput mouse;
    protected GameCanvas canvas;
    protected Frame frame;

    public abstract void init();

    public abstract void destroy();

    public abstract void update(long dt);

    public abstract void render(Graphics g);
}
