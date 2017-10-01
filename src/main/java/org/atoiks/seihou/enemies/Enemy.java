package org.atoiks.seihou.enemies;

import org.atoiks.seihou.GameComponent;

/**
 *
 * @author YTENG
 */
public abstract class Enemy extends GameComponent {

    public final float COLLISION_RADIUS;
    public final int SCORE;
    public float x;
    public float y;
    protected int hp;

    public Enemy(float collisionRadius, int score, int hp) {
        this.COLLISION_RADIUS = collisionRadius;
        this.SCORE = score;
        this.hp = hp;
    }

    public int reduceHp(int delta) {
        return hp -= delta;
    }
}
