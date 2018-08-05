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

package org.atoiks.games.nappou1.scenes;

import java.awt.Font;
import java.awt.Color;
import java.awt.event.KeyEvent;

import org.atoiks.games.framework2d.Scene;
import org.atoiks.games.framework2d.IGraphics;

import static org.atoiks.games.nappou1.App.SANS_FONT;
import static org.atoiks.games.nappou1.scenes.GameScene.CANVAS_WIDTH;

public class HelpScene extends Scene {

    public static final Font TITLE_FONT = SANS_FONT.deriveFont(24f);
    public static final Font INFO_FONT = SANS_FONT.deriveFont(18f);

    private static final String[][] CTRL_MSG = {
        { "Enter", "- Continue" },
        { "Arrow keys", "- Movement" },
        { "Shift", "- Reduced speed" },
        { "Z", "- Fire" },
        { "X", "- Deny (clears bullets on screen)" }
    };

    @Override
    public void render(IGraphics g) {
        g.setClearColor(Color.black);
        g.clearGraphics();
        g.setColor(Color.cyan);

        g.setFont(TITLE_FONT);
        g.drawString("Help Page", 20, 30);

        g.setFont(INFO_FONT);
        g.setColor(Color.cyan);
        for (int i = 0; i < CTRL_MSG.length; ++i) {
            final int h = 60 + i * (INFO_FONT.getSize() + 8);
            g.drawString(CTRL_MSG[i][0], 40, h);
            g.drawString(CTRL_MSG[i][1], 180, h);
        }
        g.drawString("or slowly wait for the timer to run out...", 40, 260);
        g.drawString("(not available in first stage or endless mode)", 40, 280);

        g.setColor(Color.gray);
        g.drawString("KILL THE OTHERS BEFORE THEY KILL YOU", 40, 220);

        g.setFont(SANS_FONT);
        g.setColor(Color.cyan);
        g.drawString("BACK", CANVAS_WIDTH / 2 - 12, 320);
        g.drawLine(CANVAS_WIDTH / 2 - 10, 320, CANVAS_WIDTH / 2 + 10, 320);
    }

    @Override
    public boolean update(float dt) {
        if (scene.keyboard().isKeyPressed(KeyEvent.VK_ENTER)
            || scene.keyboard().isKeyPressed(KeyEvent.VK_ESCAPE)) {
            scene.switchToScene(1);
            return true;
        }
        return true;
    }

    @Override
    public void resize(int w, int h) {
        // Screen size is fixed
    }
}