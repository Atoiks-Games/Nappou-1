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

package org.atoiks.games.nappou1;

/**
 *
 * @author YTENG
 */
public abstract class PlayerManager {

    public static final int PLAYER_RADIUS = 8;

    private float x;
    private float y;
    private int hp;
    private int score;

    public void resetHp() {
        hp = 0;
    }

    public void resetScore() {
        score = 0;
    }

    /**
     *
     * @param deltaHp
     * @return true if now the player is considered dead (hp &le; 0), false
     * otherwise
     */
    public abstract boolean gotHit(int deltaHp);

    public int deltaHp(int delta) {
        return hp += delta;
    }

    public int deltaScore(int delta) {
        return score += delta;
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }

    public int getHp() {
        return hp;
    }

    public int getScore() {
        return score;
    }

    public void translateX(float dx) {
        x += dx;
    }

    public void translateY(float dy) {
        y += dy;
    }
}
