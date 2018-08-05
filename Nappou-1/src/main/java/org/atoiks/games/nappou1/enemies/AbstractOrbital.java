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

import java.awt.Color;

import org.atoiks.games.framework2d.IGraphics;

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
    public void render(IGraphics g) {
        g.setColor(Color.magenta);
        g.drawCircle((int) x, (int) y, (int) COLLISION_RADIUS);
    }

    protected abstract float centerX();

    protected abstract float centerY();
}
