package org.atoiks.seihou.enemies;

import org.atoiks.seihou.BulletManager;
import org.atoiks.seihou.ProjectSeihouGame;

import java.awt.Color;
import java.awt.Graphics;

import java.util.Random;

public class RadialGhost extends Enemy {

    public static final float SPEED = 60;
    public static final float FIRE_RATE = 4;
    private static final Random RND = new Random();

    private final int DIRECTION;
    private final BulletManager BULLET_POOL;

    private float fireTimer;

    public RadialGhost(float x, float y, BulletManager manager) {
        super(3f, 2, 2);
        this.x = x;
        this.y = y;
        this.DIRECTION = (int) Math.signum(ProjectSeihouGame.GAME_CANVAS_WIDTH / 2 - x);
        this.BULLET_POOL = manager;
        this.fireTimer = RND.nextFloat() * FIRE_RATE;
    }

    @Override
    public void update(float dt) {
        if (x < -COLLISION_RADIUS || x - COLLISION_RADIUS > ProjectSeihouGame.GAME_CANVAS_WIDTH) {
            this.destroy();
            return;
        }
        if (y > ProjectSeihouGame.FRAME_HEIGHT) {
            this.destroy();
            return;
        }

        if ((fireTimer += dt) >= FIRE_RATE) {
            fireTimer = 0;
            BULLET_POOL.firePatternRadial(x, y, (float) Math.toRadians(30), 0,
                    (float) Math.PI * 2f, COLLISION_RADIUS / 2, 20);
        }

        final float dv = dt * SPEED;
        x += DIRECTION * dv;
        y += dv;
    }

    @Override
    public void render(Graphics g) {
        g.setColor(Color.magenta);
        g.drawOval((int) (x - COLLISION_RADIUS), (int) (y - COLLISION_RADIUS), (int) (COLLISION_RADIUS * 2), (int) (COLLISION_RADIUS * 2));
    }
}
