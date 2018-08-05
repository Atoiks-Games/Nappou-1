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

import java.io.IOException;
import java.io.BufferedInputStream;

import java.awt.Font;
import java.awt.Color;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import javax.imageio.ImageIO;

import javax.sound.sampled.Clip;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.atoiks.games.framework2d.Scene;
import org.atoiks.games.framework2d.IGraphics;

import org.atoiks.games.nappou1.BulletPatternAssembler;

import static org.atoiks.games.nappou1.App.SANS_FONT;

public class LoadingScene extends Scene {

    private enum LoadState {
        WAITING, LOADING, DONE, NO_RES;
    }

    private static final Font LOADING_FONT = SANS_FONT.deriveFont(32f);

    private static final String[] LOADING_MSG = {
        "Loading •....", "Loading .•...", "Loading ..•..", "Loading ...•.", "Loading ....•"
    };

    private final ExecutorService loader = Executors.newSingleThreadExecutor();

    private LoadState loaded = LoadState.WAITING;

    private int cycle;

    @Override
    public void render(IGraphics g) {
        g.setClearColor(Color.black);
        g.clearGraphics();
        g.setColor(Color.white);
        g.setFont(LOADING_FONT);
        g.drawString(LOADING_MSG[cycle / 2000 % LOADING_MSG.length], 50, 50);
    }

    @Override
    public boolean update(float dt) {
        ++cycle;
        switch (loaded) {
            case NO_RES:
                return false;
            case LOADING:
                break;
            case DONE:
                loader.shutdown();
                scene.gotoNextScene();
                break;
            case WAITING:
                loaded = LoadState.LOADING;
                loader.submit(() -> {
                    try {
                        loadBulletPattern("0.spa");
                        loadBulletPattern("1.spa");
                        loadBulletPattern("2.spa");
                        loadBulletPattern("3.spa");
                        loadBulletPattern("4.spa");
                        loadBulletPattern("5.spa");
                        loadBulletPattern("6.spa");

                        loadImage("name.png");

                        loadMusic("title_screen.wav");
                        loadMusic("tutorial.wav");
                        loadMusic("unnamed.wav");
                        loadMusic("ding_around.wav");
                        loadMusic("13s_ring.wav");
                        loadMusic("Hymn-Of-The-Arena.wav");
                        loadMusic("ding_around_rev.wav");
                        loadMusic("0418.wav");

                        loaded = LoadState.DONE;
                    } catch (NullPointerException | IOException | UnsupportedAudioFileException | LineUnavailableException ex) {
                        loaded = LoadState.NO_RES;
                    }
                });
                break;
        }

        return true;
    }

    private void loadBulletPattern(String name) throws IOException {
        scene.resources().put(name, BulletPatternAssembler.assembleFromStream(
                this.getClass().getResourceAsStream("/patterns/" + name)
        ));
    }

    private void loadImage(String name) throws IOException {
        scene.resources().put(name, ImageIO.read(this.getClass().getResourceAsStream('/' + name)));
    }

    private void loadMusic(String name) throws IOException, LineUnavailableException, UnsupportedAudioFileException {
        try (AudioInputStream in = AudioSystem.getAudioInputStream(
                new BufferedInputStream(
                        this.getClass().getResourceAsStream("/music/" + name)
                )
        )) {
            final Clip clip = AudioSystem.getClip();
            clip.open(in);
            clip.stop();
            scene.resources().put(name, clip);
        }
    }

    @Override
    public void resize(int w, int h) {
        // Screen size is fixed
    }
}
