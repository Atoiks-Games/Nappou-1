package org.atoiks.games.nappou1.enemies;

import java.awt.Color;
import java.awt.Graphics;

import org.atoiks.games.nappou1.PlayerManager;

/**
 *
 * @author YTENG
 */
public abstract class AbstractOrbital extends Enemy {

    protected final float ORBITAL_R;
    protected float theta;

    public AbstractOrbital(float r, float theta, float size, PlayerManager player) {
        super(size, (int) (0.2 * size), (int) size, player);
        this.ORBITAL_R = r;
        this.theta = theta;
    }

    @Override
    public void onUpdate(float dt) {
        this.x = this.centerX() + this.ORBITAL_R * (float) Math.cos(this.theta);
        this.y = this.centerY() + this.ORBITAL_R * (float) Math.sin(this.theta);
        this.theta += Math.PI / 3 * dt;
    }

    @Override
    public void render(Graphics g) {
        g.setColor(Color.magenta);
        g.drawOval((int) (x - COLLISION_RADIUS), (int) (y - COLLISION_RADIUS), (int) (COLLISION_RADIUS * 2), (int) (COLLISION_RADIUS * 2));
    }

    protected abstract float centerX();

    protected abstract float centerY();
}
