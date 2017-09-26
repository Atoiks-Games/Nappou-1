package com.ymcmp.seihou.enemies;

import com.ymcmp.seihou.BulletManager;
import com.ymcmp.seihou.PlayerManager;
import com.ymcmp.seihou.ProjectSeihouGame;

import java.awt.Color;
import java.awt.Graphics;

import java.util.Random;

public class SpiralGhost extends Enemy {

    public static final float SPEED = 50;
    public static final float FIRE_RATE = 4;
    private static final Random RND = new Random();

    private final int DIRECTION;
    private final BulletManager BULLET_POOL;
    private final PlayerManager PLAYER;

    private float fireTimer;

    public SpiralGhost(float x, float y, BulletManager manager, PlayerManager player) {
        super(3f, 1);
        this.x = x;
        this.y = y;
        this.DIRECTION = (int) Math.signum(ProjectSeihouGame.GAME_CANVAS_WIDTH / 2 - x);
        this.BULLET_POOL = manager;
        this.PLAYER = player;
        this.fireTimer = RND.nextFloat() * FIRE_RATE;
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
