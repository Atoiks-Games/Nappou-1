package com.ymcmp.seihou;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;

import java.util.List;
import java.util.ArrayList;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author YTENG
 */
public class ProjectSeihouGame extends AbstractGame {

    private static final Color LIGHT_GRAY_SHADER = new Color(192, 192, 192, 100);

    private static final int PLAYER_R = 8;
    private static final float PLAYER_FAST_V = 100;
    private static final float PLAYER_SLOW_V = 45;

    private static final float PB_V = 120;
    private static final float PB_RATE = 0.25f;

    private final List<BossData> BULLET_PATTERNS = new ArrayList<>();
    private float[] patternFrame = {};
    private int patternFrameIdx = 0;
    private int patternIdx = 0;

    private final AtomicInteger state = new AtomicInteger(State.LOADING);
    private final AtomicBoolean fireFlag = new AtomicBoolean(true);

    private float bossX = 0f;
    private float bossY = 0f;
    private float bossDx = 0f;
    private float bossDy = 0f;
    private int bossHp = 0;

    private float playerX = 0f;
    private float playerY = 0f;
    private int playerHp = 5;

    private float pbTimer = 0f;
    private float bfTimer = 0f;
    private long daTimer = 0L;

    // These *must* have the same size
    private final List<Float> bulletX = new ArrayList<>(64);
    private final List<Float> bulletY = new ArrayList<>(64);
    private final List<Float> bulletR = new ArrayList<>(64);
    private final List<Float> bulletDx = new ArrayList<>(64);
    private final List<Float> bulletDy = new ArrayList<>(64);

    private final List<Float> pbX = new ArrayList<>(20);
    private final List<Float> pbY = new ArrayList<>(20);
    private final List<Float> pbR = new ArrayList<>(20);

    @Override
    public void init() {
        super.init();
        frame.setTitle("Project Seihou");
        frame.setSize(500, 350);
        frame.setResizable(false);
        frame.setVisible(true);

        reset();
        System.out.println("GAME STARTED");
    }

    private void reset() {
        playerX = canvas.getWidth() / 2f;
        playerY = canvas.getHeight() - 20;
        playerHp = 5;

        pbTimer = 0f;
        bfTimer = 0f;
        fireFlag.compareAndSet(false, true);
        patternIdx = 0;

        bossX = canvas.getWidth() / 2f;
        bossY = 30;
        bossDx = 0;
        bossDy = 0;
        bossHp = 0;

        pbX.clear();
        pbY.clear();
        pbR.clear();
        bulletX.clear();
        bulletY.clear();
        bulletR.clear();
        bulletDx.clear();
        bulletDy.clear();
    }

    @Override
    public void update(long deltaT) {
        switch (state.get()) {
        case State.LOADING:
            BULLET_PATTERNS.add(new BossData(BulletPatternAssembler.assembleFromStream(
                    this.getClass().getResourceAsStream("/com/ymcmp/seihou/patterns/Tutorial.spa")
            ), 10));
            BULLET_PATTERNS.add(new BossData(BulletPatternAssembler.assembleFromStream(
                    this.getClass().getResourceAsStream("/com/ymcmp/seihou/patterns/Boss1.spa")
            ), 125));
            state.set(State.INIT);
            break;
        case State.PLAYING:
            if (this.isKeyDown(KeyEvent.VK_ESCAPE)) {
                state.set(State.PAUSE);
                return;
            }

            final float playerV = this.isKeyDown(KeyEvent.VK_SHIFT) ? PLAYER_SLOW_V : PLAYER_FAST_V;
            final float dt = deltaT / 1000f;
            final float dv = playerV * dt;
            if (this.isKeyDown(KeyEvent.VK_RIGHT)) {
                if (playerX + dv < canvas.getWidth()) {
                    playerX += dv;
                }
            }
            if (this.isKeyDown(KeyEvent.VK_LEFT)) {
                if (playerX - dv > 0) {
                    playerX -= dv;
                }
            }
            if (this.isKeyDown(KeyEvent.VK_DOWN)) {
                if (playerY + dv < canvas.getHeight()) {
                    playerY += dv;
                }
            }
            if (this.isKeyDown(KeyEvent.VK_UP)) {
                if (playerY - dv > 0) {
                    playerY -= dv;
                }
            }

            bfTimer += dt;
            while (bfTimer >= patternFrame[patternIdx]) {
                switch ((int) patternFrame[++patternIdx]) {
                case 0:
                    break;
                case 1:
                    bulletPatternRadial(bossX, bossY,
                                        (float) Math.toRadians(patternFrame[++patternIdx]),
                                        (float) Math.toRadians(patternFrame[++patternIdx]),
                                        (float) Math.PI * 2f,
                                        patternFrame[++patternIdx], patternFrame[++patternIdx]);
                    break;
                case 7:
                    bulletPatternRadial(bossX, bossY,
                                        (float) Math.toRadians(patternFrame[++patternIdx]),
                                        (float) Math.toRadians(patternFrame[++patternIdx]),
                                        (float) Math.toRadians(patternFrame[++patternIdx]),
                                        patternFrame[++patternIdx], patternFrame[++patternIdx]);
                    break;
                case 2:
                    bulletPatternRadial(patternFrame[++patternIdx], patternFrame[++patternIdx],
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
                }
                bfTimer = 0f;
                if (++patternIdx >= patternFrame.length) {
                    patternIdx = 0;
                }
            }

            bossX += bossDx * dt;
            bossY += bossDy * dt;

            if ((pbTimer += dt) >= PB_RATE) {
                fireFlag.set(true);
            }
            if (fireFlag.get() && this.isKeyDown(KeyEvent.VK_Z)) {
                fireFlag.set(false);
                pbTimer = 0f;
                pbX.add(playerX);
                pbY.add(playerY);
                pbR.add(4f);
            }

            for (int i = 0; i < bulletR.size(); ++i) {
                final float r = bulletR.get(i);
                final float newX = bulletX.get(i) + bulletDx.get(i) * dt;
                if (newX + r > 0 && newX - r < canvas.getWidth()) {
                    final float newY = bulletY.get(i) + bulletDy.get(i) * dt;
                    if (newY + r > 0 && newY - r < canvas.getHeight()) {
                        bulletX.set(i, newX);
                        bulletY.set(i, newY);
                        continue;
                    }
                }

                // Reaching here means bullet went out of bounds, destroy
                bulletR.remove(i);
                bulletX.remove(i);
                bulletY.remove(i);
                bulletDx.remove(i);
                bulletDy.remove(i);
                --i;
            }

            for (int i = 0; i < pbR.size(); ++i) {
                if (pbY.get(i) - pbR.get(i) <= 0) {
                    pbR.remove(i);
                    pbX.remove(i);
                    pbY.remove(i);
                    --i;
                    continue;
                }

                pbY.set(i, pbY.get(i) - PB_V * dt);
            }

            for (int i = 0; i < bulletR.size(); ++i) {
                // Prevent awkard "The bullet for sure did not touch me scenarios"
                // the radius of the player is made smaller (by 1 pixel)
                if (circlesCollide(bulletX.get(i), bulletY.get(i), bulletR.get(i),
                                   playerX, playerY, PLAYER_R - 1)) {
                    bulletR.remove(i);
                    bulletX.remove(i);
                    bulletY.remove(i);
                    bulletDx.remove(i);
                    bulletDy.remove(i);
                    daTimer = 0L;
                    state.set((--playerHp <= 0) ? State.LOSE : State.DIE_ANIM);
                    return;
                }
            }

            for (int i = 0; i < pbR.size(); ++i) {
                if (circlesCollide(pbX.get(i), pbY.get(i), pbR.get(i),
                                   bossX, bossY, PLAYER_R)) {
                    pbR.remove(i);
                    pbX.remove(i);
                    pbY.remove(i);
                    if ((bossHp -= 1) <= 0) {
                        state.set((++patternFrameIdx < BULLET_PATTERNS.size()) ? State.ADVANCE : State.WIN);
                        return;
                    }
                    break;
                }
            }
            break;

        case State.DIE_ANIM:
            if ((daTimer += deltaT) < 1000) {
                return;
            }
            state.set(State.PLAYING);
            break;
        case State.INIT:
            patternFrameIdx = 0;
        // FALLTHROUGH
        case State.ADVANCE: {
            reset();
            final BossData data = BULLET_PATTERNS.get(patternFrameIdx);
            patternFrame = data.bulletSeq;
            bossHp = data.hp;
            // FALLTHROUGH
        }
        case State.PAUSE:
            if (this.isKeyDown(KeyEvent.VK_ENTER)) {
                state.set(State.PLAYING);
                return;
            }
            if (this.isKeyDown(KeyEvent.VK_Q)) {
                abort();
            }
            break;
        case State.LOSE:
        case State.WIN:
            if (this.isKeyDown(KeyEvent.VK_ENTER)) {
                while (this.isKeyDown(KeyEvent.VK_ENTER)) {
                    try {
                        Thread.sleep(0);
                    } catch (InterruptedException ex) {
                    }
                }
                state.set(State.INIT);
            }
            if (this.isKeyDown(KeyEvent.VK_Q)) {
                abort();
            }
            break;
        default:
        }
    }

    private static boolean circlesCollide(float x1, float y1, float r1,
                                          float x2, float y2, float r2) {
        return (r1 + r2) > Math.hypot(x2 - x1, y2 - y1);
    }

    private void bulletPatternRadial(float originX, float originY,
                                     float spacing, float tilt, float upperBound,
                                     float size, float speed) {
        // o -> apply [radial]
        //
        // \ | /
        // - o -   (angle between is spacing (rad), angle offset is tilt (rad))
        // / | \

        if (size == 0) {
            return;
        }

        for (float counter = 0; counter < upperBound; counter += spacing) {
            bulletR.add(size);
            bulletX.add(originX);
            bulletY.add(originY);

            final float actAngle = counter + tilt;
            bulletDx.add((float) Math.cos(actAngle) * speed);
            bulletDy.add((float) Math.sin(actAngle) * speed);
        }
    }

    @Override
    public void render(Graphics g) {
        g.setColor(Color.black);
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        switch (state.get()) {
        case State.LOADING:
            g.setColor(Color.gray);
            g.drawString("Now loading...", 0, 12);
            break;
        case State.INIT:
            g.setColor(Color.blue);
            g.drawString("Project Seihou", canvas.getWidth() / 2 - 40, 60);
            g.drawString("[ENTER]", canvas.getWidth() / 2 - 24, 82);
            break;
        case State.PLAYING:
            drawGame(g);
            break;
        case State.DIE_ANIM:
            drawGame(g);
            g.setColor(LIGHT_GRAY_SHADER);
            g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
            g.setColor(Color.red);
            g.drawString("HP - 1", canvas.getWidth() / 2 - 16, 60);
            break;
        case State.PAUSE:
            drawGame(g);
            g.setColor(LIGHT_GRAY_SHADER);
            g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
            g.setColor(Color.blue);
            g.drawString("PAUSED", canvas.getWidth() / 2 - 24, 60);
            break;
        case State.LOSE:
            g.setColor(Color.BLUE);
            g.drawString("GAME OVER", canvas.getWidth() / 2 - 32, 60);
            break;
        case State.WIN:
            g.setColor(Color.BLUE);
            g.drawString("YOU WIN", canvas.getWidth() / 2 - 24, 60);
            break;
        case State.ADVANCE:
            g.setColor(Color.BLUE);
            g.drawString("NEXT LEVEL", canvas.getWidth() / 2 - 32, 60);
            break;
        default:
        }
    }

    private void drawGame(Graphics g) {
        g.setColor(Color.white);
        g.fillOval((int) playerX - PLAYER_R, (int) playerY - PLAYER_R, PLAYER_R * 2, PLAYER_R * 2);
        try {
            for (int i = 0; i < pbR.size(); ++i) {
                final float r = pbR.get(i);
                g.drawOval((int) (pbX.get(i) - r), (int) (pbY.get(i) - r), (int) (r * 2), (int) (r * 2));
            }
        } catch (IndexOutOfBoundsException ex) {
        }

        g.setColor(Color.yellow);
        g.fillOval((int) bossX - PLAYER_R, (int) bossY - PLAYER_R, PLAYER_R * 2, PLAYER_R * 2);
        try {
            for (int i = 0; i < bulletX.size(); ++i) {
                final float r = bulletR.get(i);
                g.drawOval((int) (bulletX.get(i) - r), (int) (bulletY.get(i) - r), (int) (r * 2), (int) (r * 2));
            }
        } catch (IndexOutOfBoundsException ex) {
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        System.out.println("GAME ENDED");
    }
}
