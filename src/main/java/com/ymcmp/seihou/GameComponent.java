package com.ymcmp.seihou;

import java.awt.Graphics;

/**
 *
 * @author YTENG
 */
public abstract class GameComponent {

    boolean aliveFlag = true;

    public abstract void update(float dt);

    public abstract void render(Graphics g);

    public final void destroy() {
        aliveFlag = false;
    }
}
