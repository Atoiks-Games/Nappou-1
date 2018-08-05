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

import java.io.IOException;

import java.awt.Font;
import java.awt.FontFormatException;

import org.atoiks.games.framework2d.FrameInfo;
import org.atoiks.games.framework2d.swing.Frame;

import org.atoiks.games.nappou1.scenes.*;

public class App {

    public static final Font SANS_FONT;

    static {
        Font local = null;
        try {
            local = Font.createFont(Font.PLAIN, App.class.getResourceAsStream("/Being_Human.ttf"));
        } catch (IOException | FontFormatException ex) {
            // Fallback to using a generic SansSerif font
            local = new Font("SansSerif", Font.PLAIN, 16);
        } finally {
            // SANS_FONT is initialized as size 13 plain
            SANS_FONT = local.deriveFont(Font.PLAIN, 13f);
        }
    }

    public static final int WIDTH = 500;
    public static final int HEIGHT = 350;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        final FrameInfo info = new FrameInfo()
                .setTitle("Atoiks Games - Nappou 1")
                .setResizable(false)
                .setSize(WIDTH, HEIGHT)
                .setScenes(new LoadingScene(), new NappouGame());
        try (final Frame frame = new Frame(info)) {
            frame.init();
            frame.loop();
        }
    }
}
