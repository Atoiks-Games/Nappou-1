package com.ymcmp.seihou.enemies;

import com.ymcmp.seihou.GameComponent;

/**
 *
 * @author YTENG
 */
public abstract class Enemy extends GameComponent {

    public final float COLLISION_RADIUS;
    public final int SCORE;
    public float x;
    public float y;

    public Enemy(float collisionRadius, int score) {
        this.COLLISION_RADIUS = collisionRadius;
        this.SCORE = score;
    }
}
