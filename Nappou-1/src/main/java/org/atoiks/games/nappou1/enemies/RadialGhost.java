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
import org.atoiks.games.nappou1.NappouGame;

import java.awt.Color;

import java.util.Random;

import org.atoiks.games.framework2d.IGraphics;

public class RadialGhost extends Enemy {

    public static final float SPEED = 60;
    public static final float FIRE_RATE = 4;
    private static final Random RND = new Random();

    private final int DIRECTION;
    private final BulletManager BULLET_POOL;

    private float fireTimer;

    public RadialGhost(float x, float y, BulletManager manager, PlayerManager player) {
        super(3f, 2, 2, player);
        this.x = x;
        this.y = y;
        this.DIRECTION = (int) Math.signum(NappouGame.GAME_CANVAS_WIDTH / 2 - x);
        this.BULLET_POOL = manager;
        this.fireTimer = RND.nextFloat() * FIRE_RATE;
    }

    @Override
    public void onUpdate(float dt) {
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
            BULLET_POOL.firePatternRadial(x, y, (float) Math.toRadians(30), 0,
                    (float) Math.PI * 2f, COLLISION_RADIUS / 2, 20);
        }

        final float dv = dt * SPEED;
        x += DIRECTION * dv;
        y += dv;
    }

    @Override
    public void render(IGraphics g) {
        g.setColor(Color.magenta);
        g.drawCircle((int) x, (int) y, (int) COLLISION_RADIUS);
    }
}
