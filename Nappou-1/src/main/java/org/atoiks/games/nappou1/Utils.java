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
public final class Utils {

    public static boolean circlesCollide(float x1, float y1, float r1, float x2, float y2, float r2) {
        final float ir = r1 + r2;
        final float dx = x2 - x1;
        final float dy = y2 - y1;
        return ir * ir > dx * dx + dy * dy;
    }

    private Utils() {
    }
}
