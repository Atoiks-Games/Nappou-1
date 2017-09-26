package com.ymcmp.seihou;

import com.ymcmp.seihou.enemies.*;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

import java.io.IOException;

import java.util.List;
import java.util.ArrayList;

import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;

/**
 *
 * @author YTENG
 */
public class ProjectSeihouGame extends Java2DGame {

    private static final Color LIGHT_GRAY_SHADER = new Color(192, 192, 192, 100);

    private static final int PLAYER_R = 8;
    private static final float PLAYER_FAST_V = 140;
    private static final float PLAYER_SLOW_V = 85;

    private static final float PLAYER_BULLET_V = 170;
    private static final float PLAYER_BULLET_RATE = 0.25f;
    private static final float PROTECTION_RATE = 3f;

    private final List<BossData> BULLET_PATTERNS = new ArrayList<>();
    private float[] patternFrame = {};
    private int patternFrameIdx = 0;
    private int patternIdx = 0;

    private final AtomicInteger state = new AtomicInteger(State.LOADING);
    private boolean fireFlag = true;
    private boolean protectionFlag = false;

    private float bossX = 0f;
    private float bossY = 0f;
    private float bossDx = 0f;
    private float bossDy = 0f;
    private int bossHp = 0;
    private int bossPts = 0;
    private int timeLimit = 0;

    private final PlayerManager player = new PlayerManager();

    private float pbTimer = 0f;
    private float bfTimer = 0f;
    private float pgTimer = 0f;
    private long daTimer = 0L;
    private long pTimer = 0L;

    private int initOptSel = 0;

    private BufferedImage imgName;
    private BufferedImage imgInstructions;

    private final BulletManager enemyBullets = new BulletManager(64);

    private final List<Float> playerBulletX = new ArrayList<>(20);
    private final List<Float> playerBulletY = new ArrayList<>(20);
    private final List<Float> playerBulletR = new ArrayList<>(20);

    // bosses do not count as enemies (mini-bosses do though)
    private final List<Enemy> enemies = new ArrayList<>(32);

    public static final int GAME_CANVAS_WIDTH = 380;
    public static final int INFO_CANVAS_WIDTH = 120;
    public static final int CANVAS_WIDTH = GAME_CANVAS_WIDTH + INFO_CANVAS_WIDTH;

    // This is not the same as canvas.getHeight() (slightly bigger)
    public static final int FRAME_HEIGHT = 350;

    @Override
    public void init() {
        super.init();
        try {
            frame.setIconImage(ImageIO.read(
                    this.getClass().getResourceAsStream("/com/ymcmp/seihou/icon.png")
            ));
        } catch (IOException ex) {
            abort();
            return;
        }

        frame.setTitle("Project Seihou");
        frame.setSize(CANVAS_WIDTH, FRAME_HEIGHT);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        enemyBullets.changeBox(0, 0, GAME_CANVAS_WIDTH, canvas.getHeight());

        reset();
    }

    private void reset() {
        player.setPosition(GAME_CANVAS_WIDTH / 2f, canvas.getHeight() - 20);
        player.resetHp();
        player.deltaHp(5);

        pbTimer = 0f;
        bfTimer = 0f;
        pgTimer = 0f;
        fireFlag = true;
        patternIdx = 0;

        bossX = GAME_CANVAS_WIDTH / 2f;
        bossY = 30;
        bossDx = 0;
        bossDy = 0;
        bossHp = 0;
        bossPts = 0;
        timeLimit = 0;

        enemyBullets.clear();

        playerBulletX.clear();
        playerBulletY.clear();
        playerBulletR.clear();

        enemies.clear();
    }

    @Override
    public void update(long deltaT) {
        switch (state.get()) {
            case State.LOADING:
                try {
                    BULLET_PATTERNS.add(new BossData(BulletPatternAssembler.assembleFromStream(
                            this.getClass().getResourceAsStream("/com/ymcmp/seihou/patterns/Tutorial.spa")
                    ), 30, 0));
                    BULLET_PATTERNS.add(new BossData(BulletPatternAssembler.assembleFromStream(
                            this.getClass().getResourceAsStream("/com/ymcmp/seihou/patterns/Boss1.spa")
                    ), 125, 200, 100));
                    imgName = ImageIO.read(
                            this.getClass().getResourceAsStream("/com/ymcmp/seihou/name.bmp")
                    );
                    imgInstructions = ImageIO.read(
                            this.getClass().getResourceAsStream("/com/ymcmp/seihou/instructions.bmp")
                    );
                } catch (IOException ex) {
                    abort();
                    return;
                }
                state.set(State.INIT);
                break;
            case State.PLAYING: {
                if (this.isKeyDown(KeyEvent.VK_ESCAPE)) {
                    state.set(State.PAUSE);
                    return;
                }

                final float dt = deltaT / 1000f;

                procPlayerMovement(dt);
                if (updateTimerLimit(dt)) {
                    return;
                }
                updateBossBehaviour(dt);
                updateEnemyBehaviour(dt);
                procUserFire(dt);
                updateBulletPos(dt);
                updatePlayerBulletPos(dt);
                updateSpawnProtectionTime(deltaT);
                if (upateBullet() || updatePlayerBullet()) {
                    return;
                }
                break;
            }
            case State.DIE_ANIM: {
                final float dt = deltaT / 1000f;

                if (updateTimerLimit(dt)) {
                    return;
                }
                updateBossBehaviour(dt);
                updateEnemyBehaviour(dt);
                updateBulletPos(dt);
                updatePlayerBulletPos(dt);
                if (updatePlayerBullet()) {
                    return;
                }

                if ((daTimer += deltaT) < 250) {
                    return;
                }
                daTimer = 0;
                protectionFlag = true;
                state.set(State.PLAYING);
                break;
            }
            case State.INIT:
                player.resetScore();
                patternFrameIdx = 0;
                if (this.isKeyPressed(KeyEvent.VK_UP)) {
                    --initOptSel;
                }
                if (this.isKeyPressed(KeyEvent.VK_DOWN)) {
                    ++initOptSel;
                }
                if (this.isKeyPressed(KeyEvent.VK_ENTER)) {
                    switch (initOptSel) {
                        case 0:
                            doAdvance();
                            state.set(State.PLAYING);
                            return;
                        case 1:
                            state.set(State.HELP);
                            return;
                        case 2:
                            abort();
                            return;
                    }
                }
                break;
            case State.ADVANCE: {
                doAdvance();
            }
            case State.PAUSE:
                if (this.isKeyDown(KeyEvent.VK_ENTER)) {
                    state.set(State.PLAYING);
                    return;
                }
                if (this.isKeyDown(KeyEvent.VK_Q)) {
                    state.set(State.INIT);
                    return;
                }
                break;
            case State.LOSE:
            case State.WIN:
            case State.HELP:
                if (this.isKeyPressed(KeyEvent.VK_ENTER)) {
                    state.set(State.INIT);
                }
                if (this.isKeyDown(KeyEvent.VK_Q)) {
                    abort();
                }
                break;
            default:
        }
    }

    private void updateSpawnProtectionTime(long deltaT) {
        if (protectionFlag) {
            if ((pTimer += deltaT) > PROTECTION_RATE * 1000) {
                pTimer = 0L;
                protectionFlag = false;
            }
        }
    }

    private boolean upateBullet() {
        if (!protectionFlag) {
            if (enemyBullets.testCollision(player.getX(), player.getY(), PLAYER_R / 2)) {
                daTimer = 0L;
                state.set((player.deltaHp(-1) <= 0) ? State.LOSE : State.DIE_ANIM);
                return true;
            }
        }
        return false;
    }

    private void updatePlayerBulletPos(final float dt) {
        for (int i = 0; i < playerBulletR.size(); ++i) {
            if (playerBulletY.get(i) - playerBulletR.get(i) <= 0) {
                playerBulletR.remove(i);
                playerBulletX.remove(i);
                playerBulletY.remove(i);
                --i;
                continue;
            }

            playerBulletY.set(i, playerBulletY.get(i) - PLAYER_BULLET_V * dt);
        }
    }

    private void updateBulletPos(final float dt) {
        enemyBullets.updatePosition(dt);
    }

    private void procUserFire(final float dt) {
        if ((pbTimer += dt) >= PLAYER_BULLET_RATE) {
            fireFlag = true;
        }
        if (fireFlag && this.isKeyDown(KeyEvent.VK_Z)) {
            fireFlag = false;
            pbTimer = 0f;
            playerBulletX.add(player.getX());
            playerBulletY.add(player.getY());
            playerBulletR.add(4f);
        }
    }

    private void updateBossBehaviour(final float dt) {
        bfTimer += dt;
        while (bfTimer >= patternFrame[patternIdx]) {
            switch ((int) patternFrame[++patternIdx]) {
                case 0:
                    break;
                case 1:
                    enemyBullets.firePatternRadial(bossX, bossY,
                            (float) Math.toRadians(patternFrame[++patternIdx]),
                            (float) Math.toRadians(patternFrame[++patternIdx]),
                            (float) Math.PI * 2f,
                            patternFrame[++patternIdx], patternFrame[++patternIdx]);
                    break;
                case 7:
                    enemyBullets.firePatternRadial(bossX, bossY,
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
                    bossX = patternFrame[++patternIdx];
                    bossY = patternFrame[++patternIdx];
                    break;
                case 4:
                    bossX += patternFrame[++patternIdx];
                    bossY += patternFrame[++patternIdx];
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
            }
            bfTimer = 0f;
            if (++patternIdx >= patternFrame.length) {
                patternIdx = 0;
            }
        }

        bossX += bossDx * dt;
        bossY += bossDy * dt;
    }

    private void updateEnemyBehaviour(final float dt) {
        for (int i = 0; i < enemies.size(); ++i) {
            final GameComponent gcmp = enemies.get(i);
            gcmp.update(dt);
            if (gcmp.aliveFlag) {
                continue;
            }
            enemies.remove(i--);
        }
    }

    private boolean updateTimerLimit(final float dt) {
        if (timeLimit > 0 && (pgTimer += dt) >= timeLimit) {
            advanceStage();
            return true;
        }
        return false;
    }

    private boolean updatePlayerBullet() {
        playerBulletLoop:
        for (int i = 0; i < playerBulletR.size(); ++i) {
            final float bulletX = playerBulletX.get(i);
            final float bulletY = playerBulletY.get(i);
            final float bulletR = playerBulletR.get(i);

            if (Utils.circlesCollide(bulletX, bulletY, bulletR, bossX, bossY, PLAYER_R)) {
                playerBulletR.remove(i);
                playerBulletX.remove(i);
                playerBulletY.remove(i);
                if ((bossHp -= 1) <= 0) {
                    player.deltaScore(bossPts);
                    advanceStage();
                    return true;
                }
                --i;
                continue;
            }

            for (int j = 0; j < enemies.size(); ++j) {
                final Enemy gcmp = enemies.get(j);
                if (Utils.circlesCollide(bulletX, bulletY, bulletR, gcmp.x, gcmp.y, gcmp.COLLISION_RADIUS)) {
                    playerBulletR.remove(i);
                    playerBulletX.remove(i);
                    playerBulletY.remove(i);
                    enemies.remove(j);
                    player.deltaScore(gcmp.SCORE);
                    --i;
                    continue playerBulletLoop;
                }
            }
        }
        return false;
    }

    private void procPlayerMovement(final float dt) {
        final float dv = (this.isKeyDown(KeyEvent.VK_SHIFT) ? PLAYER_SLOW_V : PLAYER_FAST_V) * dt;

        if (this.isKeyDown(KeyEvent.VK_RIGHT)) {
            if (player.getX() + dv < GAME_CANVAS_WIDTH) {
                player.translateX(dv);
            }
        }
        if (this.isKeyDown(KeyEvent.VK_LEFT)) {
            if (player.getX() - dv > 0) {
                player.translateX(-dv);
            }
        }
        if (this.isKeyDown(KeyEvent.VK_DOWN)) {
            if (player.getY() + dv < canvas.getHeight()) {
                player.translateY(dv);
            }
        }
        if (this.isKeyDown(KeyEvent.VK_UP)) {
            if (player.getY() - dv > 0) {
                player.translateY(-dv);
            }
        }
    }

    private void advanceStage() {
        state.set((++patternFrameIdx < BULLET_PATTERNS.size()) ? State.ADVANCE : State.WIN);
    }

    private void doAdvance() {
        reset();
        final BossData data = BULLET_PATTERNS.get(patternFrameIdx);
        patternFrame = data.bulletSeq;
        bossHp = data.hp;
        bossPts = data.score;
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
                g.drawImage(imgName, 22, 0, null);
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
            case State.DIE_ANIM:
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
        g.setColor(Color.white);
        try {
            for (int i = 0; i < playerBulletR.size(); ++i) {
                final float r = playerBulletR.get(i);
                g.drawOval((int) (playerBulletX.get(i) - r), (int) (playerBulletY.get(i) - r), (int) (r * 2), (int) (r * 2));
            }
        } catch (IndexOutOfBoundsException ex) {
        }

        if (drawPlayer) {
            if (protectionFlag) {
                g.setColor(Color.green);
            }
            g.fillOval((int) player.getX() - PLAYER_R, (int) player.getY() - PLAYER_R, PLAYER_R * 2, PLAYER_R * 2);
        }

        g.setColor(Color.yellow);
        g.fillOval((int) bossX - PLAYER_R, (int) bossY - PLAYER_R, PLAYER_R * 2, PLAYER_R * 2);
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
            g.drawString(timeLimit > 0 ? Integer.toString(timeLimit - (int) pgTimer) : "unlimited", GAME_CANVAS_WIDTH + 14, 32);
        }

        g.drawString("Enemy:", GAME_CANVAS_WIDTH + 14, 44);
        if (withStats) {
            g.drawString(Integer.toString(bossHp), GAME_CANVAS_WIDTH + 14 + 50, 44);
        }

        g.drawString("HP:", GAME_CANVAS_WIDTH + 14, 80);
        if (withStats) {
            g.drawString(Integer.toString(player.getHp()), GAME_CANVAS_WIDTH + 14 + 30, 80);
        }

        if (withStats && protectionFlag) {
            g.drawString("PROTECTED", GAME_CANVAS_WIDTH + 14, 92);
        }

        g.drawString("Score:", GAME_CANVAS_WIDTH + 14, 114);
        g.drawString(Long.toString(player.getScore() * 20L), GAME_CANVAS_WIDTH + 14, 126);
    }

    @Override
    public void destroy() {
        super.destroy();
    }
}
