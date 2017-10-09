package org.atoiks.seihou;

import org.atoiks.seihou.enemies.*;

import com.ymcmp.jine.Game;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

import java.io.BufferedInputStream;
import java.io.IOException;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;

import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;

import javax.sound.sampled.Clip;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 *
 * @author YTENG
 */
public class ProjectSeihouGame extends Game {

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

    private final BossData[] BULLET_PATTERNS = new BossData[7];
    private float[] patternFrame = {};
    private int patternFrameIdx = 0;
    private int patternIdx = 0;

    private final AtomicInteger state = new AtomicInteger(State.LOADING);
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
                if (player.deltaHp(-1) <= 0) {
                    state.set(State.LOSE);
                    return true;
                }

                enableRespawnProtection();
                hitAnimTimer = 0L;
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

    private long respawnProtectionTimer = 0L;
    private long hitAnimTimer = 0L;

    private int initOptSel = 0;

    private BufferedImage imgName;
    private BufferedImage imgInstructions;

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

    private final Clip[] musics = new Clip[2];

    public static final int GAME_CANVAS_WIDTH = 380;
    public static final int INFO_CANVAS_WIDTH = 120;
    public static final int CANVAS_WIDTH = GAME_CANVAS_WIDTH + INFO_CANVAS_WIDTH;

    // This is not the same as canvas.getHeight() (slightly bigger)
    public static final int FRAME_HEIGHT = 350;

    @Override
    public void init() {
        try {
            frame.setIcon(ImageIO.read(this.getClass().getResourceAsStream("/org/atoiks/seihou/icon.png")));

            loadMusic(0, "title_screen.wav");
            loadMusic(1, "tutorial.wav");
        } catch (IOException | UnsupportedAudioFileException | LineUnavailableException ex) {
            frame.abort(ex.toString());
            return;
        }

        frame.setTitle("Project Seihou");
        frame.setSize(CANVAS_WIDTH, FRAME_HEIGHT);
        frame.setResizable(false);
        frame.moveToCenter();
        frame.setVisible(true);

        enemyBullets.changeBox(0, 0, GAME_CANVAS_WIDTH, canvas.getHeight());

        reset();
    }

    private void loadMusic(int slot, String name) throws IOException, LineUnavailableException, UnsupportedAudioFileException {
        try (AudioInputStream in = AudioSystem.getAudioInputStream(
                new BufferedInputStream(
                        this.getClass().getResourceAsStream("/org/atoiks/seihou/music/" + name)
                )
        )) {
            Clip clip = AudioSystem.getClip();
            clip.open(in);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.stop();
            musics[slot] = clip;
        }
    }

    private void reset() {
        player.setPosition(GAME_CANVAS_WIDTH / 2f, canvas.getHeight() - 20);
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
    public void update(long deltaT) {
        switch (state.get()) {
            case State.LOADING:
                try {
                    BULLET_PATTERNS[0] = new BossData(BulletPatternAssembler.assembleFromStream(
                            this.getClass().getResourceAsStream("/org/atoiks/seihou/patterns/0.spa")
                    ), 30, 0);
                    BULLET_PATTERNS[1] = new BossData(BulletPatternAssembler.assembleFromStream(
                            this.getClass().getResourceAsStream("/org/atoiks/seihou/patterns/1.spa")
                    ), 45, 200, 60);
                    BULLET_PATTERNS[2] = new BossData(BulletPatternAssembler.assembleFromStream(
                            this.getClass().getResourceAsStream("/org/atoiks/seihou/patterns/2.spa")
                    ), 45, 180, 60);
                    BULLET_PATTERNS[3] = new BossData(BulletPatternAssembler.assembleFromStream(
                            this.getClass().getResourceAsStream("/org/atoiks/seihou/patterns/3.spa")
                    ), 45, 200, 80);
                    BULLET_PATTERNS[4] = new BossData(BulletPatternAssembler.assembleFromStream(
                            this.getClass().getResourceAsStream("/org/atoiks/seihou/patterns/4.spa")
                    ), 45, 200, 80);
                    BULLET_PATTERNS[5] = new BossData(BulletPatternAssembler.assembleFromStream(
                            this.getClass().getResourceAsStream("/org/atoiks/seihou/patterns/5.spa")
                    ), 150, 350, 115);
                    BULLET_PATTERNS[6] = new BossData(BulletPatternAssembler.assembleFromStream(
                            this.getClass().getResourceAsStream("/org/atoiks/seihou/patterns/6.spa")
                    ), 125, 200, 100);

                    imgName = ImageIO.read(
                            this.getClass().getResourceAsStream("/org/atoiks/seihou/name.bmp")
                    );
                    imgInstructions = ImageIO.read(
                            this.getClass().getResourceAsStream("/org/atoiks/seihou/instructions.bmp")
                    );
                } catch (IOException ex) {
                    frame.abort(ex.toString());
                    return;
                }
                state.set(State.INIT);
                break;
            case State.PLAYING: {
                if (patternFrameIdx + 1 < musics.length && !musics[patternFrameIdx + 1].isRunning()) {
                    musics[patternFrameIdx + 1].setMicrosecondPosition(0);
                    musics[patternFrameIdx + 1].start();
                }

                if (keyboard.isKeyDown(KeyEvent.VK_ESCAPE)) {
                    state.set(State.PAUSE);
                    return;
                }

                final float dt = deltaT / 1000f;

                procPlayerMovement(dt);
                procUserFire(dt);
                updateSpawnProtectionTime(deltaT);
                if (procGenericUpdate(dt)) {
                    return;
                }
                break;
            }
            case State.HIT_ANIM: {
                final float dt = deltaT / 1000f;

                if (procGenericUpdate(dt)) {
                    return;
                }

                if ((hitAnimTimer += deltaT) < 250) {
                    return;
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
                    // Modify this line to test a particular level directly
                    patternFrameIdx = 0;
                    atkType = MASK_BULLET_MIN;
                    ultCounter = 2;
                }

                if (keyboard.isKeyPressed(KeyEvent.VK_UP)) {
                    --initOptSel;
                }
                if (keyboard.isKeyPressed(KeyEvent.VK_DOWN)) {
                    ++initOptSel;
                }
                if (keyboard.isKeyPressed(KeyEvent.VK_ENTER)) {
                    switch (initOptSel) {
                        case 0:
                            doAdvance();
                            musics[0].stop();
                            state.set(State.PLAYING);
                            return;
                        case 1:
                            state.set(State.HELP);
                            return;
                        case 2:
                            musics[0].stop();
                            frame.abort();
                            return;
                    }
                }
                break;
            case State.ADVANCE: {
                doAdvance();
            }
            case State.PAUSE:
                if (keyboard.isKeyDown(KeyEvent.VK_ENTER)) {
                    state.set(State.PLAYING);
                    return;
                }
                if (keyboard.isKeyDown(KeyEvent.VK_Q)) {
                    state.set(State.INIT);
                    if (patternFrameIdx + 1 < musics.length) {
                        musics[patternFrameIdx + 1].stop();
                    }
                    return;
                }
                break;
            case State.HELP:
            case State.LOSE:
            case State.WIN:
                if (keyboard.isKeyPressed(KeyEvent.VK_ENTER)) {
                    state.set(State.INIT);
                }
                if (keyboard.isKeyDown(KeyEvent.VK_Q)) {
                    frame.abort();
                }
                break;
            default:
        }
    }

    private boolean procGenericUpdate(final float dt) {
        if (updateTimerLimit(dt)) {
            return true;
        }
        updateBossBehaviour(dt);
        updateEnemyBehaviour(dt);
        updateBulletPos(dt);
        updatePlayerBulletPos(dt);
        if (upateBullet() || updatePlayerBullet()) {
            return true;
        }
        return false;
    }

    private void updateSpawnProtectionTime(long deltaT) {
        if (protectionFlag && ((respawnProtectionTimer += deltaT) > PROTECTION_RATE * 1000)) {
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
            if (keyboard.isKeyDown(KeyEvent.VK_Z)) {
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
            if (keyboard.isKeyDown(KeyEvent.VK_X)) {
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

    private void updateBossBehaviour(final float dt) {
        bossFireTimer += dt;
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
            if (gcmp.aliveFlag) {
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
        if (timeLimit > 0 && (gameTimer += dt) >= timeLimit) {
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

            if (Utils.circlesCollide(bulletX, bulletY, PLAYER_BULLET_R,
                    boss.getX(), boss.getY(), PlayerManager.PLAYER_RADIUS)) {
                removePlayerBulletIdx(i);
                if (boss.gotHit(-1)) {
                    return true;
                }
                --i;
                continue;
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
        final float dv = (keyboard.isKeyDown(KeyEvent.VK_SHIFT) ? PLAYER_SLOW_V : PLAYER_FAST_V) * dt;

        if (keyboard.isKeyDown(KeyEvent.VK_RIGHT)) {
            if (player.getX() + dv < GAME_CANVAS_WIDTH) {
                player.translateX(dv);
            }
        }
        if (keyboard.isKeyDown(KeyEvent.VK_LEFT)) {
            if (player.getX() > dv) {
                player.translateX(-dv);
            }
        }
        if (keyboard.isKeyDown(KeyEvent.VK_DOWN)) {
            if (player.getY() + dv < canvas.getHeight()) {
                player.translateY(dv);
            }
        }
        if (keyboard.isKeyDown(KeyEvent.VK_UP)) {
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
    public void render(Graphics g) {
        g.setColor(Color.black);
        g.fillRect(0, 0, CANVAS_WIDTH, canvas.getHeight());
        switch (state.get()) {
            case State.LOADING:
                g.setColor(Color.gray);
                g.drawString("Now loading...", 0, 12);
                break;
            case State.INIT:
                g.drawImage(imgName, 18, 0, null);
                g.setColor(Color.cyan);
                g.drawString("START", CANVAS_WIDTH / 2 - 16, 82);
                g.drawString("HELP", CANVAS_WIDTH / 2 - 12, 104);
                g.drawString("QUIT", CANVAS_WIDTH / 2 - 12, 136);
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
                        g.drawLine(CANVAS_WIDTH / 2 - 10, 136, CANVAS_WIDTH / 2 + 10, 136);
                        break;
                    default:
                        if (initOptSel < 0) {
                            initOptSel = 2;
                        }
                        initOptSel %= 3;
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
                g.fillRect(0, 0, GAME_CANVAS_WIDTH, canvas.getHeight());
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
            case State.HELP:
                g.setColor(Color.cyan);
                g.drawImage(imgInstructions, 0, 0, null);
                g.drawString("BACK", CANVAS_WIDTH / 2 - 12, 240);
                g.drawLine(CANVAS_WIDTH / 2 - 10, 240, CANVAS_WIDTH / 2 + 10, 240);
                break;
            default:
        }
    }

    private void drawGame(Graphics g, boolean drawPlayer) {
        g.setColor(Color.red);
        try {
            for (int i = 0; i < powupY.size(); ++i) {
                final float x = powupX.get(i);
                final float y = powupY.get(i);
                g.fillRect((int) x - POWUP_SIZE / 2, (int) y - POWUP_SIZE / 2, POWUP_SIZE, POWUP_SIZE);
            }
        } catch (IndexOutOfBoundsException ex) {
        }

        g.setColor(Color.white);
        try {
            for (int i = 0; i < playerBulletX.size(); ++i) {
                g.drawOval((int) (playerBulletX.get(i) - PLAYER_BULLET_R),
                        (int) (playerBulletY.get(i) - PLAYER_BULLET_R),
                        PLAYER_BULLET_R * 2,
                        PLAYER_BULLET_R * 2);
            }
        } catch (IndexOutOfBoundsException ex) {
        }

        if (drawPlayer) {
            if (protectionFlag) {
                g.setColor(Color.green);
            }
            g.fillOval((int) player.getX() - PlayerManager.PLAYER_RADIUS, (int) player.getY() - PlayerManager.PLAYER_RADIUS, PlayerManager.PLAYER_RADIUS * 2, PlayerManager.PLAYER_RADIUS * 2);
        }

        g.setColor(Color.yellow);
        g.fillOval((int) boss.getX() - PlayerManager.PLAYER_RADIUS, (int) boss.getY() - PlayerManager.PLAYER_RADIUS, PlayerManager.PLAYER_RADIUS * 2, PlayerManager.PLAYER_RADIUS * 2);
        enemyBullets.render(g);

        try {
            for (int i = 0; i < enemies.size(); ++i) {
                enemies.get(i).render(g);
            }
        } catch (IndexOutOfBoundsException ex) {
        }
    }

    public void drawInfo(Graphics g, boolean withStats) {
        // Overlay (side info bar)
        g.setColor(Color.black);
        g.fillRect(GAME_CANVAS_WIDTH, 0, CANVAS_WIDTH, canvas.getHeight());
        g.setColor(Color.red);
        g.drawLine(GAME_CANVAS_WIDTH, 0, GAME_CANVAS_WIDTH, canvas.getHeight());

        g.setColor(Color.lightGray);
        g.drawString("Time limit:", GAME_CANVAS_WIDTH + 14, 20);
        if (withStats) {
            g.drawString(timeLimit > 0 ? Integer.toString(timeLimit - (int) gameTimer) : "unlimited", GAME_CANVAS_WIDTH + 14, 32);
        }

        g.drawString("Enemy:", GAME_CANVAS_WIDTH + 14, 44);
        if (withStats) {
            g.drawString(Integer.toString(boss.getHp()), GAME_CANVAS_WIDTH + 14 + 50, 44);
        }

        g.drawString("Deny:", GAME_CANVAS_WIDTH + 14, 68);
        g.drawString(Integer.toString(ultCounter), GAME_CANVAS_WIDTH + 14 + 50, 68);

        g.drawString("HP:", GAME_CANVAS_WIDTH + 14, 80);
        if (withStats) {
            g.drawString(Integer.toString(player.getHp()), GAME_CANVAS_WIDTH + 14 + 50, 80);
        }

        if (withStats && protectionFlag) {
            g.drawString("PROTECTED", GAME_CANVAS_WIDTH + 14, 92);
        }

        g.drawString("Score:", GAME_CANVAS_WIDTH + 14, 114);
        g.drawString(Long.toString(player.getScore() * 20L), GAME_CANVAS_WIDTH + 14, 126);
    }

    @Override
    public void destroy() {
        for (int i = 0; i < musics.length; ++i) {
            if (musics[i] != null) {
                musics[i].close();
            }
        }
    }
}
