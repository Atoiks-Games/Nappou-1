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

public class BossOrbitalGhost extends AbstractOrbital {

    private final PlayerManager BOSS_MGR;

    public BossOrbitalGhost(PlayerManager bossMgr, float r, float theta, float size, PlayerManager player) {
        super(r, theta, size, player);
        this.BOSS_MGR = bossMgr;
    }

    @Override
    protected float centerX() {
        return this.BOSS_MGR.getX();
    }

    @Override
    protected float centerY() {
        return this.BOSS_MGR.getY();
    }
}
