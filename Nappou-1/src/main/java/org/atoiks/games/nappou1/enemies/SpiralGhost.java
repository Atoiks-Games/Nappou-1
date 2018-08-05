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

import org.atoiks.games.nappou1.PlayerManager;
import org.atoiks.games.nappou1.BulletManager;

import org.atoiks.games.nappou1.scenes.GameScene;

import java.awt.Color;

import org.atoiks.games.framework2d.IGraphics;

public class SpiralGhost extends Enemy {

    public static final float SPEED = 25;
    public static final float FIRE_RATE = 3;

    private final int DIRECTION;
    private final BulletManager BULLET_POOL;

    private float fireTimer;
    private float fireAngle;

    public SpiralGhost(float x, float y, BulletManager manager, PlayerManager player) {
        super(5f, 6, 5, player);
        this.x = x;
        this.y = y;
        this.DIRECTION = (int) Math.signum(GameScene.GAME_CANVAS_WIDTH / 2 - x);
        this.BULLET_POOL = manager;
        this.fireTimer = 0f;
        this.fireAngle = 0f;
    }

    @Override
    public void onUpdate(float dt) {
        if (x < -COLLISION_RADIUS || x - COLLISION_RADIUS > GameScene.GAME_CANVAS_WIDTH) {
            this.destroy();
            return;
        }
        if (y > GameScene.FRAME_HEIGHT) {
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
    public void render(IGraphics g) {
        g.setColor(Color.magenta);
        g.drawCircle((int) x, (int) y, (int) COLLISION_RADIUS);
    }
}
