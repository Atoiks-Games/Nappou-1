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

import org.atoiks.games.nappou1.GameComponent;
import org.atoiks.games.nappou1.PlayerManager;
import org.atoiks.games.nappou1.Utils;

/**
 *
 * @author YTENG
 */
public abstract class Enemy extends GameComponent {

    public final float COLLISION_RADIUS;
    public final int SCORE;
    public float x;
    public float y;

    protected final PlayerManager player;
    protected int hp;

    public Enemy(float collisionRadius, int score, int hp, PlayerManager player) {
        this.COLLISION_RADIUS = collisionRadius;
        this.SCORE = score;
        this.hp = hp;
        this.player = player;
    }

    @Override
    public final void update(float dt) {
        if (Utils.circlesCollide(x, y, COLLISION_RADIUS,
                player.getX(), player.getY(), PlayerManager.PLAYER_RADIUS)) {
            if (player.gotHit(-1)) {
                // if the player died, there is no point of more processing
                return;
            }
        }
        onUpdate(dt);
    }

    public final int reduceHp(int delta) {
        return hp -= delta;
    }

    public abstract void onUpdate(float dt);
}
