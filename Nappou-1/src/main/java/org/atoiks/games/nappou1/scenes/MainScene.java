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

import org.atoiks.games.framework2d.GameScene;
import org.atoiks.games.framework2d.IGraphics;

import org.atoiks.games.nappou1.App;
import org.atoiks.games.nappou1.State;
import org.atoiks.games.nappou1.Utils;
import org.atoiks.games.nappou1.BossData;
import org.atoiks.games.nappou1.GameComponent;
import org.atoiks.games.nappou1.PlayerManager;
import org.atoiks.games.nappou1.BulletManager;

import org.atoiks.games.nappou1.enemies.*;

import java.awt.Font;
import java.awt.Color;
import java.awt.Image;
import java.awt.event.KeyEvent;

import java.util.List;
import java.util.Random;
import java.util.ArrayList;

import java.util.concurrent.atomic.AtomicInteger;

import javax.sound.sampled.Clip;

/**
 *
 * @author YTENG
 */
public class MainScene extends GameScene {

    private static final Color LIGHT_GRAY_SHADER = new Color(192, 192, 192, 100);

    private static final int PLAYER_FAST_V = 140;
    private static final int PLAYER_SLOW_V = 85;

    private static final int PLAYER_BULLET_V = 170;
    private static final int PLAYER_BULLET_R = 4;
    private static final float PLAYER_BULLET_RATE = 0.25f;
    private static final float PROTECTION_RATE = 3f;

    private static final byte MASK_BULLET_DFWD = 0b01000;
    private static final byte MASK_BULLET_DSPR = 0b10000;
    private static final byte MASK_BULLET_MIN = 0;
    private static final byte MASK_BULLET_MAX = MASK_BULLET_DFWD | MASK_BULLET_DSPR;

    // Game mode
    private byte gameMode = State.STORY_MODE;

    // Story mode data: Scripted attacks
    private final BossData[] BULLET_PATTERNS = new BossData[7];
    private float[] patternFrame = {};
    private int patternFrameIdx = 0;
    private int patternIdx = 0;

    private final AtomicInteger state = new AtomicInteger(State.INIT);
    private boolean protectionFlag = false;

    private final PlayerManager boss = new PlayerManager() {
        @Override
        public boolean gotHit(int deltaHp) {
            if (boss.deltaHp(-1) <= 0) {
                player.deltaScore(boss.getScore());
                advanceStage();
                return true;
            }
            return false;
        }
    };
    private float bossDx = 0f;
    private float bossDy = 0f;
    private int timeLimit = 0;

    private final PlayerManager player = new PlayerManager() {
        @Override
        public boolean gotHit(int deltaHp) {
            if (!protectionFlag) {
                if (player.deltaHp(deltaHp) <= 0) {
                    state.set(State.LOSE);
                    return true;
                }

                enableRespawnProtection();
                hitAnimTimer = 0f;
                state.set(State.HIT_ANIM);
            }
            return false;
        }
    };
    private byte atkType = MASK_BULLET_MIN;
    private byte ultCounter = 2;

    private float ultCooldownTimer = 0f;
    private float playerFireTimer = 0f;
    private float bossFireTimer = 0f;
    private float gameTimer = 0f;

    private float respawnProtectionTimer = 0f;
    private float hitAnimTimer = 0f;

    private int initOptSel = 0;

    private final BulletManager enemyBullets = new BulletManager(64);

    private final List<Float> playerBulletX = new ArrayList<>(20);
    private final List<Float> playerBulletY = new ArrayList<>(20);
    private final List<Float> playerBulletDx = new ArrayList<>(20);

    private static final Random POWUP_GEN = new Random();
    private static final float POWUP_SPEED = 15f;
    private static final int POWUP_SIZE = 6;
    private final List<Float> powupX = new ArrayList<>();
    private final List<Float> powupY = new ArrayList<>();

    // bosses do not count as enemies (mini-bosses do though)
    private final List<Enemy> enemies = new ArrayList<>(32);

    private final Clip[] musics = new Clip[8];

    public static final int GAME_CANVAS_WIDTH = 380;
    public static final int INFO_CANVAS_WIDTH = 120;
    public static final int CANVAS_WIDTH = App.WIDTH;

    public static final int FRAME_HEIGHT = App.HEIGHT;

    private static final Font TITLE_FONT = App.SANS_FONT.deriveFont(Font.PLAIN, 40f);

    @Override
    public void init() {
        musics[0] = (Clip) scene.resources().get("title_screen.wav");
        musics[1] = (Clip) scene.resources().get("tutorial.wav");
        musics[2] = (Clip) scene.resources().get("unnamed.wav");
        musics[3] = (Clip) scene.resources().get("ding_around.wav");
        musics[4] = (Clip) scene.resources().get("13s_ring.wav");
        musics[5] = (Clip) scene.resources().get("Hymn-Of-The-Arena.wav");
        musics[6] = (Clip) scene.resources().get("ding_around_rev.wav");
        musics[7] = (Clip) scene.resources().get("0418.wav");

        BULLET_PATTERNS[0] = new BossData((float[]) scene.resources().get("0.spa"), 30, 0);
        BULLET_PATTERNS[1] = new BossData((float[]) scene.resources().get("1.spa"), 45, 200, 60);
        BULLET_PATTERNS[2] = new BossData((float[]) scene.resources().get("2.spa"), 45, 180, 60);
        BULLET_PATTERNS[3] = new BossData((float[]) scene.resources().get("3.spa"), 45, 200, 80);
        BULLET_PATTERNS[4] = new BossData((float[]) scene.resources().get("4.spa"), 45, 200, 80);
        BULLET_PATTERNS[5] = new BossData((float[]) scene.resources().get("5.spa"), 150, 350, 115);
        BULLET_PATTERNS[6] = new BossData((float[]) scene.resources().get("6.spa"), 125, 200, 100);

        enemyBullets.changeBox(0, 0, GAME_CANVAS_WIDTH, FRAME_HEIGHT);
    }

    @Override
    public void enter(int from) {
        reset();
    }

    private void reset() {
        player.setPosition(GAME_CANVAS_WIDTH / 2f, FRAME_HEIGHT - 20);
        player.resetHp();
        player.deltaHp(5);

        playerFireTimer = 0f;
        ultCooldownTimer = 0f;
        bossFireTimer = 0f;
        gameTimer = 0f;
        patternIdx = 0;

        boss.setPosition(GAME_CANVAS_WIDTH / 2f, 30);
        boss.resetHp();
        boss.resetScore();
        bossDx = 0;
        bossDy = 0;
        timeLimit = 0;

        enemyBullets.clear();

        playerBulletX.clear();
        playerBulletY.clear();
        playerBulletDx.clear();

        powupX.clear();
        powupY.clear();

        enemies.clear();
    }

    @Override
    public boolean update(float dt) {
        switch (state.get()) {
            case State.PLAYING: {
                if (gameMode == State.STORY_MODE
                        && patternFrameIdx + 1 < musics.length
                        && !musics[patternFrameIdx + 1].isRunning()) {
                    musics[patternFrameIdx + 1].setMicrosecondPosition(0);
                    musics[patternFrameIdx + 1].start();
                }

                if (scene.keyboard().isKeyDown(KeyEvent.VK_ESCAPE)) {
                    state.set(State.PAUSE);
                    return true;
                }

                procPlayerMovement(dt);
                procUserFire(dt);
                updateSpawnProtectionTime(dt);
                if (procGenericUpdate(dt)) {
                    return true;
                }
                break;
            }
            case State.HIT_ANIM: {
                if (procGenericUpdate(dt)) {
                    return true;
                }

                if ((hitAnimTimer += dt) < 0.25) {
                    return true;
                }

                // reduce attack type to the level before
                atkType >>>= 1;

                state.set(State.PLAYING);
                break;
            }
            case State.INIT:
                if (!musics[0].isRunning()) {
                    musics[0].setMicrosecondPosition(0);
                    musics[0].start();

                    player.resetScore();
                    protectionFlag = false;
                    // Modify this line to test a particular level directly
                    patternFrameIdx = 0;
                    atkType = MASK_BULLET_MIN;
                    ultCounter = 2;
                }

                if (scene.keyboard().isKeyPressed(KeyEvent.VK_UP)) {
                    --initOptSel;
                }
                if (scene.keyboard().isKeyPressed(KeyEvent.VK_DOWN)) {
                    ++initOptSel;
                }
                if (scene.keyboard().isKeyPressed(KeyEvent.VK_ENTER)) {
                    switch (initOptSel) {
                        case 0:
                            doAdvance();
                            musics[0].stop();
                            gameMode = State.STORY_MODE;
                            state.set(State.PLAYING);
                            return true;
                        case 1:
                            doAdvance();
                            musics[0].stop();
                            gameMode = State.ENDLESS_MODE;
                            state.set(State.PLAYING);
                            return true;
                        case 2:
                            scene.gotoNextScene();
                            return true;
                        case 3:
                            musics[0].stop();
                            return false;
                    }
                }
                break;
            case State.ADVANCE: {
                doAdvance();
            }
            case State.PAUSE:
                if (patternFrameIdx + 1 < musics.length) {
                    musics[patternFrameIdx + 1].stop();
                }
                if (scene.keyboard().isKeyDown(KeyEvent.VK_ENTER)) {
                    if (gameMode == State.STORY_MODE && patternFrameIdx + 1 < musics.length) {
                        // continue from after pause (rely on State.PLAYING
                        // rewinds the entire track)
                        musics[patternFrameIdx + 1].start();
                    }
                    state.set(State.PLAYING);
                    return true;
                }
                if (scene.keyboard().isKeyDown(KeyEvent.VK_Q)) {
                    state.set(State.INIT);
                    return true;
                }
                break;
            case State.LOSE:
            case State.WIN:
                if (patternFrameIdx + 1 < musics.length) {
                    musics[patternFrameIdx + 1].stop();
                }
                if (scene.keyboard().isKeyPressed(KeyEvent.VK_ENTER)) {
                    state.set(State.INIT);
                    return true;
                }
                if (scene.keyboard().isKeyDown(KeyEvent.VK_Q)) {
                    return false;
                }
                break;
            default:
        }
        return true;
    }

    private boolean procGenericUpdate(final float dt) {
        if (updateTimerLimit(dt)) {
            return true;
        }
        updateBossBehaviour(dt);
        updateEnemyBehaviour(dt);
        updateBulletPos(dt);
        updatePlayerBulletPos(dt);
        return upateBullet() || updatePlayerBullet();
    }

    private void updateSpawnProtectionTime(float deltaT) {
        if (protectionFlag && ((respawnProtectionTimer += deltaT) > PROTECTION_RATE)) {
            protectionFlag = false;
        }
    }

    private boolean upateBullet() {
        if (!protectionFlag) {
            // Prevent awkard "The bullet for sure did not touch me scenarios"
            // the radius of the player is made smaller
            if (enemyBullets.testCollision(player.getX(), player.getY(), PlayerManager.PLAYER_RADIUS / 2)) {
                player.gotHit(-1);
                return true;
            }
        }
        return false;
    }

    private void updatePlayerBulletPos(final float dt) {
        for (int i = 0; i < playerBulletY.size(); ++i) {
            final float x = playerBulletX.get(i);
            final float y = playerBulletY.get(i);
            final float dx = playerBulletDx.get(i);
            if (y <= PLAYER_BULLET_R || x < -PLAYER_BULLET_R
                    || x > GAME_CANVAS_WIDTH + PLAYER_BULLET_R) {
                removePlayerBulletIdx(i);
                --i;
                continue;
            }

            playerBulletX.set(i, x + dx * dt);
            playerBulletY.set(i, y - PLAYER_BULLET_V * dt);
        }
    }

    private void removePlayerBulletIdx(int i) {
        playerBulletX.remove(i);
        playerBulletY.remove(i);
        playerBulletDx.remove(i);
    }

    private void updateBulletPos(final float dt) {
        enemyBullets.updatePosition(dt);
    }

    private void procUserFire(final float dt) {
        if ((playerFireTimer += dt) >= PLAYER_BULLET_RATE) {
            if (scene.keyboard().isKeyDown(KeyEvent.VK_Z)) {
                playerFireTimer = 0f;

                if ((atkType & MASK_BULLET_DFWD) == MASK_BULLET_DFWD) {
                    newPlayerBullet(player.getX() - 8, player.getY(), 0);
                    newPlayerBullet(player.getX() + 8, player.getY(), 0);
                } else {
                    newPlayerBullet(player.getX(), player.getY(), 0);
                }
                if ((atkType & MASK_BULLET_DSPR) == MASK_BULLET_DSPR) {
                    newPlayerBullet(player.getX() - 8, player.getY(), -32);
                    newPlayerBullet(player.getX() + 8, player.getY(), 32);
                }
            }
        }
        if ((ultCooldownTimer += dt) >= PLAYER_BULLET_RATE) {
            if (scene.keyboard().isKeyDown(KeyEvent.VK_X)) {
                if (ultCounter > 0) {
                    ultCooldownTimer = 0;
                    --ultCounter;
                    enableRespawnProtection();
                    enemyBullets.clear();
                }
            }
        }
    }

    private void enableRespawnProtection() {
        respawnProtectionTimer = 0L;
        protectionFlag = true;
    }

    private void newPlayerBullet(float x, float y, float dx) {
        playerBulletX.add(x);
        playerBulletY.add(y);
        playerBulletDx.add(dx);
    }

    private final Random generator = new Random();
    private byte randomCounter = Byte.MAX_VALUE - 30;

    private void updateBossBehaviour(final float dt) {
        bossFireTimer += dt;
        if (gameMode == State.ENDLESS_MODE) {
            // Endless mode has no bosses, we hijack this method
            // to spawn enemies
            if (enemies.size() < 10 && bossFireTimer >= 0.5) {
                // Fire timer to prevent a *chunk* of enemies
                bossFireTimer = 0;
                enemies.add(new WeakGhost(
                        generator.nextInt(GAME_CANVAS_WIDTH - 100) + 50,
                        generator.nextInt(2) * (FRAME_HEIGHT - 5),
                        player.getY(), enemyBullets, player));
                if (generator.nextBoolean()) {
                    enemyBullets.firePatternRadial(GAME_CANVAS_WIDTH - 50, 60,
                            (float) Math.PI / 10,
                            randomCounter += 10,
                            (float) Math.PI * 2,
                            5,
                            generator.nextFloat() * 85);
                }
            }
            return;
        }

        while (bossFireTimer >= patternFrame[patternIdx]) {
            switch ((int) patternFrame[++patternIdx]) {
                case 0:
                    break;
                case 1:
                    enemyBullets.firePatternRadial(boss.getX(), boss.getY(),
                            (float) Math.toRadians(patternFrame[++patternIdx]),
                            (float) Math.toRadians(patternFrame[++patternIdx]),
                            (float) Math.PI * 2f,
                            patternFrame[++patternIdx], patternFrame[++patternIdx]);
                    break;
                case 7:
                    enemyBullets.firePatternRadial(boss.getX(), boss.getY(),
                            (float) Math.toRadians(patternFrame[++patternIdx]),
                            (float) Math.toRadians(patternFrame[++patternIdx]),
                            (float) Math.toRadians(patternFrame[++patternIdx]),
                            patternFrame[++patternIdx], patternFrame[++patternIdx]);
                    break;
                case 2:
                    enemyBullets.firePatternRadial(patternFrame[++patternIdx], patternFrame[++patternIdx],
                            (float) Math.toRadians(patternFrame[++patternIdx]),
                            (float) Math.toRadians(patternFrame[++patternIdx]),
                            (float) Math.toRadians(patternFrame[++patternIdx]),
                            patternFrame[++patternIdx], patternFrame[++patternIdx]);
                    break;
                case 3:
                    boss.setPosition(patternFrame[++patternIdx], patternFrame[++patternIdx]);
                    break;
                case 4:
                    boss.translateX(patternFrame[++patternIdx]);
                    boss.translateY(patternFrame[++patternIdx]);
                    break;
                case 5:
                    bossDx = patternFrame[++patternIdx];
                    bossDy = patternFrame[++patternIdx];
                    break;
                case 6:
                    bossDx += patternFrame[++patternIdx];
                    bossDy += patternFrame[++patternIdx];
                    break;
                case 9:
                    patternIdx = (int) patternFrame[patternIdx + 1] - 1;
                    break;
                case 8:
                    enemies.add(new WeakGhost(patternFrame[++patternIdx],
                            patternFrame[++patternIdx],
                            patternFrame[++patternIdx],
                            enemyBullets, player));
                    break;
                case 10:
                    enemies.add(new RadialGhost(patternFrame[++patternIdx],
                            patternFrame[++patternIdx],
                            enemyBullets, player));
                    break;
                case 11:
                    enemies.add(new SpiralGhost(patternFrame[++patternIdx],
                            patternFrame[++patternIdx],
                            enemyBullets, player));
                    break;
                case 12:
                    enemies.add(new OrbitalGhost(patternFrame[++patternIdx],
                            patternFrame[++patternIdx],
                            patternFrame[++patternIdx],
                            (float) Math.toRadians(patternFrame[++patternIdx]),
                            patternFrame[++patternIdx], player));
                    break;
                case 13:
                    enemies.add(new BossOrbitalGhost(boss,
                            patternFrame[++patternIdx],
                            (float) Math.toRadians(patternFrame[++patternIdx]),
                            patternFrame[++patternIdx], player));
                    break;
                case 14:
                    enemies.add(new OrbitalGhost(player.getX(),
                            player.getY(),
                            patternFrame[++patternIdx],
                            (float) Math.toRadians(patternFrame[++patternIdx]),
                            patternFrame[++patternIdx], player));
                    break;
                case 15:
                    enemies.add(new BossOrbitalGhost(player,
                            patternFrame[++patternIdx],
                            (float) Math.toRadians(patternFrame[++patternIdx]),
                            patternFrame[++patternIdx], player));
                    break;
            }
            bossFireTimer = 0f;
            if (++patternIdx >= patternFrame.length) {
                patternIdx = 0;
            }
        }

        boss.translateX(bossDx * dt);
        boss.translateY(bossDy * dt);
    }

    private void updateEnemyBehaviour(final float dt) {
        for (int i = 0; i < enemies.size(); ++i) {
            final GameComponent gcmp = enemies.get(i);
            gcmp.update(dt);
            if (gcmp.isAlive()) {
                continue;
            }
            if (player.getHp() < 1) {
                // player is dead, no point of iterating through the remaining
                // enemies and powerups
                return;
            }
            enemies.remove(i--);
        }

        for (int i = 0; i < powupY.size(); ++i) {
            final float y = powupY.get(i) + POWUP_SPEED * dt;
            if (y > FRAME_HEIGHT) {
                powupX.remove(i);
                powupY.remove(i);
                --i;
                continue;
            }
            powupY.set(i, y);
        }
    }

    private boolean updateTimerLimit(final float dt) {
        if (gameMode != State.ENDLESS_MODE
                && timeLimit > 0 && (gameTimer += dt) >= timeLimit) {
            // No timeouts in endless mode
            advanceStage();
            return true;
        }
        return false;
    }

    private boolean updatePlayerBullet() {
        playerBulletLoop:
        for (int i = 0; i < playerBulletX.size(); ++i) {
            final float bulletX = playerBulletX.get(i);
            final float bulletY = playerBulletY.get(i);

            if (gameMode != State.ENDLESS_MODE) {
                // Endless mode has no boss
                if (Utils.circlesCollide(bulletX, bulletY, PLAYER_BULLET_R,
                        boss.getX(), boss.getY(), PlayerManager.PLAYER_RADIUS)) {
                    removePlayerBulletIdx(i);
                    if (boss.gotHit(-1)) {
                        return true;
                    }
                    --i;
                    continue;
                }
            }

            for (int j = 0; j < enemies.size(); ++j) {
                final Enemy gcmp = enemies.get(j);
                if (Utils.circlesCollide(bulletX, bulletY, PLAYER_BULLET_R,
                        gcmp.x, gcmp.y, gcmp.COLLISION_RADIUS)) {
                    removePlayerBulletIdx(i);
                    if (gcmp.reduceHp(1) <= 0) {
                        player.deltaScore(gcmp.SCORE);
                        enemies.remove(j);

                        if (POWUP_GEN.nextBoolean()) {
                            powupX.add(bulletX);
                            powupY.add(bulletY);
                        }
                    }
                    --i;
                    continue playerBulletLoop;
                }
            }
        }
        return false;
    }

    private void procPlayerMovement(final float dt) {
        final float dv = (scene.keyboard().isKeyDown(KeyEvent.VK_SHIFT) ? PLAYER_SLOW_V : PLAYER_FAST_V) * dt;

        if (scene.keyboard().isKeyDown(KeyEvent.VK_RIGHT)) {
            if (player.getX() + dv < GAME_CANVAS_WIDTH) {
                player.translateX(dv);
            }
        }
        if (scene.keyboard().isKeyDown(KeyEvent.VK_LEFT)) {
            if (player.getX() > dv) {
                player.translateX(-dv);
            }
        }
        if (scene.keyboard().isKeyDown(KeyEvent.VK_DOWN)) {
            if (player.getY() + dv < FRAME_HEIGHT) {
                player.translateY(dv);
            }
        }
        if (scene.keyboard().isKeyDown(KeyEvent.VK_UP)) {
            if (player.getY() > dv) {
                player.translateY(-dv);
            }
        }

        for (int i = 0; i < powupY.size(); ++i) {
            if (Utils.circlesCollide(player.getX(), player.getY(), PlayerManager.PLAYER_RADIUS,
                    powupX.get(i), powupY.get(i), POWUP_SIZE)) {
                powupX.remove(i);
                powupY.remove(i);
                --i;

                if (++atkType > MASK_BULLET_MAX) {
                    atkType = MASK_BULLET_MAX;
                }
            }
        }
    }

    private void advanceStage() {
        state.set((++patternFrameIdx < BULLET_PATTERNS.length) ? State.ADVANCE : State.WIN);
        // 0 is the title screen music
        if (patternFrameIdx < musics.length) {
            musics[patternFrameIdx].stop();
        }
    }

    private void doAdvance() {
        reset();
        final BossData data = BULLET_PATTERNS[patternFrameIdx];
        patternFrame = data.bulletSeq;
        boss.deltaHp(data.hp);
        boss.deltaScore(data.score);
        timeLimit = data.timeout;
    }

    @Override
    public void render(IGraphics g) {
        g.setClearColor(Color.black);
        g.clearGraphics();
        g.setFont(App.SANS_FONT);

        switch (state.get()) {
            case State.INIT:
                g.setColor(Color.cyan);
                g.setFont(TITLE_FONT);
                g.drawString("Project Nappou", 130, 40);
                g.setFont(App.SANS_FONT);
                g.drawString("STORY", CANVAS_WIDTH / 2 - 16, 82);
                g.drawString("ENDLESS", CANVAS_WIDTH / 2 - 24, 104);
                g.drawString("HELP", CANVAS_WIDTH / 2 - 12, 126);
                g.drawString("QUIT", CANVAS_WIDTH / 2 - 12, 164);
                g.drawString("Made with love by Atoiks Games", 240, 288);
                g.drawString("Visit us at http://atoiks-games.github.io", 240, 300);

                switch (initOptSel) {
                    case 0:
                        g.drawLine(CANVAS_WIDTH / 2 - 10, 82, CANVAS_WIDTH / 2 + 10, 82);
                        break;
                    case 1:
                        g.drawLine(CANVAS_WIDTH / 2 - 10, 104, CANVAS_WIDTH / 2 + 10, 104);
                        break;
                    case 2:
                        g.drawLine(CANVAS_WIDTH / 2 - 10, 126, CANVAS_WIDTH / 2 + 10, 126);
                        break;
                    case 3:
                        g.drawLine(CANVAS_WIDTH / 2 - 10, 164, CANVAS_WIDTH / 2 + 10, 164);
                        break;
                    default:
                        if (initOptSel < 0) {
                            initOptSel = 3;
                        }
                        initOptSel %= 4;
                        break;
                }
                break;
            case State.PLAYING:
                drawGame(g, true);
                drawInfo(g, true);
                break;
            case State.HIT_ANIM:
                drawGame(g, false);
                drawInfo(g, true);
                break;
            case State.PAUSE:
                drawGame(g, true);
                drawInfo(g, true);
                g.setColor(LIGHT_GRAY_SHADER);
                g.fillRect(0, 0, GAME_CANVAS_WIDTH, FRAME_HEIGHT);
                g.setColor(Color.cyan);
                g.drawString("PAUSED", GAME_CANVAS_WIDTH / 2 - 24, 60);
                g.drawString("(ENTER OR Q)", GAME_CANVAS_WIDTH / 2 - 40, 240);
                break;
            case State.LOSE:
                drawInfo(g, true);
                g.setColor(Color.cyan);
                g.drawString("GAME OVER", GAME_CANVAS_WIDTH / 2 - 32, 60);
                g.drawString("BACK", GAME_CANVAS_WIDTH / 2 - 12, 240);
                g.drawLine(GAME_CANVAS_WIDTH / 2 - 10, 240, GAME_CANVAS_WIDTH / 2 + 10, 240);
                break;
            case State.WIN:
                drawInfo(g, true);
                g.setColor(Color.cyan);
                g.drawString("YOU WIN", GAME_CANVAS_WIDTH / 2 - 24, 60);
                g.drawString("CONTINUE", GAME_CANVAS_WIDTH / 2 - 28, 240);
                g.drawLine(GAME_CANVAS_WIDTH / 2 - 10, 240, GAME_CANVAS_WIDTH / 2 + 10, 240);
                break;
            case State.ADVANCE:
                drawInfo(g, false);
                g.setColor(Color.cyan);
                g.drawString("NEXT LEVEL", GAME_CANVAS_WIDTH / 2 - 32, 60);
                g.drawString("CONTINUE", GAME_CANVAS_WIDTH / 2 - 28, 240);
                g.drawLine(GAME_CANVAS_WIDTH / 2 - 10, 240, GAME_CANVAS_WIDTH / 2 + 10, 240);
                break;
            default:
        }
    }

    private void drawGame(IGraphics g, boolean drawPlayer) {
        g.setColor(Color.red);
        try {
            for (int i = 0; i < powupY.size(); ++i) {
                final float x = powupX.get(i);
                final float y = powupY.get(i);
                g.fillRect(x - POWUP_SIZE / 2, y - POWUP_SIZE / 2, x + POWUP_SIZE / 2, y + POWUP_SIZE / 2);
            }
        } catch (IndexOutOfBoundsException ex) {
        }

        g.setColor(Color.white);
        try {
            for (int i = 0; i < playerBulletX.size(); ++i) {
                g.fillCircle(playerBulletX.get(i).intValue(), playerBulletY.get(i).intValue(), PLAYER_BULLET_R);
            }
        } catch (IndexOutOfBoundsException ex) {
        }

        if (drawPlayer) {
            if (protectionFlag) {
                g.setColor(Color.green);
            }
            g.fillCircle((int) player.getX(), (int) player.getY(), PlayerManager.PLAYER_RADIUS);
        }

        g.setColor(Color.yellow);
        if (gameMode != State.ENDLESS_MODE) {
            // endless mode does not have a boss
            g.fillCircle((int) boss.getX(), (int) boss.getY(), PlayerManager.PLAYER_RADIUS);
        }
        enemyBullets.render(g);

        try {
            for (int i = 0; i < enemies.size(); ++i) {
                enemies.get(i).render(g);
            }
        } catch (IndexOutOfBoundsException ex) {
        }
    }

    public void drawInfo(IGraphics g, boolean withStats) {
        // Overlay (side info bar)
        g.setColor(Color.black);
        g.fillRect(GAME_CANVAS_WIDTH, 0, CANVAS_WIDTH, FRAME_HEIGHT);
        g.setColor(Color.red);
        g.drawLine(GAME_CANVAS_WIDTH, 0, GAME_CANVAS_WIDTH, FRAME_HEIGHT);

        g.setColor(Color.lightGray);
        g.drawString("Time limit:", GAME_CANVAS_WIDTH + 14, 20);
        if (withStats) {
            if (gameMode == State.ENDLESS_MODE) {
                g.drawString("endless", GAME_CANVAS_WIDTH + 14, 32);
            } else if (timeLimit > 0) {
                g.drawString(Integer.toString(timeLimit - (int) gameTimer), GAME_CANVAS_WIDTH + 14, 32);
            } else {
                g.drawString("unlimited", GAME_CANVAS_WIDTH + 14, 32);
            }
        }

        if (gameMode != State.ENDLESS_MODE) {
            g.drawString("Enemy:", GAME_CANVAS_WIDTH + 14, 44);
            if (withStats) {
                g.drawString(Integer.toString(boss.getHp()), GAME_CANVAS_WIDTH + 14 + 50, 44);
            }
        }

        g.drawString("Deny:", GAME_CANVAS_WIDTH + 14, 68);
        g.drawString(Integer.toString(ultCounter), GAME_CANVAS_WIDTH + 14 + 50, 68);

        g.drawString("HP:", GAME_CANVAS_WIDTH + 14, 80);
        if (withStats) {
            g.drawString(Integer.toString(Math.max(0, player.getHp())), GAME_CANVAS_WIDTH + 14 + 50, 80);
        }

        if (withStats && protectionFlag) {
            g.drawString("PROTECTED", GAME_CANVAS_WIDTH + 14, 92);
        }

        g.drawString("Score:", GAME_CANVAS_WIDTH + 14, 114);
        g.drawString(Long.toString(player.getScore() * 20L), GAME_CANVAS_WIDTH + 14, 126);
    }

    @Override
    public void resize(int w, int h) {
        // Screen is fixed
    }
}
