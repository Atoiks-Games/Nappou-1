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
public final class State {

    public static final byte INIT = 0;
    public static final byte PLAYING = 1;
    public static final byte HIT_ANIM = 2;
    public static final byte PAUSE = 3;
    public static final byte LOSE = 4;
    public static final byte WIN = 5;
    public static final byte ADVANCE = 6;
    
    public static final byte STORY_MODE = 0;
    public static final byte ENDLESS_MODE = 1;

    private State() {
    }
}
