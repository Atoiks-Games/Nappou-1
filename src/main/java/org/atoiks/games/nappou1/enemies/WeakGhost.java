/**
 *  Nappou-1
 *  Copyright (C) 2017-2018  Atoiks-Games <atoiks-games@outlook.com>

 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.

 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.

 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.atoiks.games.nappou1.enemies;

import org.atoiks.games.nappou1.BulletManager;
import org.atoiks.games.nappou1.PlayerManager;
import org.atoiks.games.nappou1.NappouGame;

import java.awt.Color;
import java.awt.Graphics;

import java.util.Random;

public class WeakGhost extends Enemy {

    public static final float SPEED = 50;
    public static final float FIRE_RATE = 2;
    private static final Random RND = new Random();

    private final int DIRECTION_X;
    private final int DIRECTION_Y;
    private final float Y_LIMIT;
    private final BulletManager BULLET_POOL;

    private float fireTimer;

    public WeakGhost(float x, float y, float limY, BulletManager manager, PlayerManager player) {
        super(3f, 1, 1, player);
        this.x = x;
        this.y = y;
        this.Y_LIMIT = limY;
        this.DIRECTION_X = (int) Math.signum(NappouGame.GAME_CANVAS_WIDTH / 2 - x);
        this.DIRECTION_Y = y < limY ? 1 : -1;
        this.BULLET_POOL = manager;
        this.fireTimer = RND.nextFloat() * FIRE_RATE;
    }

    @Override
    public void onUpdate(float dt) {
        // +------------------+
        // | x   PATH         |
        // | |   FLIPPED BY Y | If at center, go straight down
        // | \--------------> |
        // +------------------+
        if (x < -COLLISION_RADIUS || x - COLLISION_RADIUS > NappouGame.GAME_CANVAS_WIDTH) {
            this.destroy();
            return;
        }
        if (y > NappouGame.FRAME_HEIGHT) {
            this.destroy();
            return;
        }

        if ((fireTimer += dt) >= FIRE_RATE) {
            fireTimer = 0;
            BULLET_POOL.addBullet(x, y, COLLISION_RADIUS, player.getX() - x, player.getY() - y);
        }

        final float dv = dt * SPEED;
        if (DIRECTION_X != 0
                && DIRECTION_Y * y > DIRECTION_Y * Y_LIMIT) {
            x += DIRECTION_X * dv;
            return;
        }
        y += DIRECTION_Y * dv;
    }

    @Override
    public void render(Graphics g) {
        g.setColor(Color.magenta);
        g.drawOval((int) (x - COLLISION_RADIUS), (int) (y - COLLISION_RADIUS), (int) (COLLISION_RADIUS * 2), (int) (COLLISION_RADIUS * 2));
    }
}
