package com.ymcmp.seihou.enemies;

import com.ymcmp.seihou.BulletManager;
import com.ymcmp.seihou.ProjectSeihouGame;

import java.awt.Color;
import java.awt.Graphics;

public class SpiralGhost extends Enemy {

    public static final float SPEED = 25;
    public static final float FIRE_RATE = 3;

    private final int DIRECTION;
    private final BulletManager BULLET_POOL;

    private float fireTimer;
    private float fireAngle;

    public SpiralGhost(float x, float y, BulletManager manager) {
        super(5f, 6, 5);
        this.x = x;
        this.y = y;
        this.DIRECTION = (int) Math.signum(ProjectSeihouGame.GAME_CANVAS_WIDTH / 2 - x);
        this.BULLET_POOL = manager;
        this.fireTimer = 0f;
        this.fireAngle = 0f;
    }

    @Override
    public void update(float dt) {
        // +------------------+
        // | x   PATH         |
        // | |   FLIPPED BY Y | If at center, go straight down
        // | \--------------> |
        // +------------------+
        if (x < -COLLISION_RADIUS || x - COLLISION_RADIUS > ProjectSeihouGame.GAME_CANVAS_WIDTH) {
            this.destroy();
            return;
        }
        if (y > ProjectSeihouGame.FRAME_HEIGHT) {
            this.destroy();
            return;
        }

        final float magnitude = Math.abs(fireAngle);
        if (magnitude > 0 && magnitude < Math.PI * 2) {
            fireAngle += DIRECTION * Math.toRadians(15);
            BULLET_POOL.addBullet(x, y, COLLISION_RADIUS / 2,
                    (float) Math.sin(fireAngle) * 15,
                    (float) Math.cos(fireAngle) * 15);
            return;
        }

        if ((fireTimer += dt) >= FIRE_RATE) {
            fireTimer = 0;
            fireAngle = 0.001f;
        }

        y += dt * SPEED;
    }

    @Override
    public void render(Graphics g) {
        g.setColor(Color.magenta);
        g.drawOval((int) (x - COLLISION_RADIUS), (int) (y - COLLISION_RADIUS), (int) (COLLISION_RADIUS * 2), (int) (COLLISION_RADIUS * 2));
    }
}
