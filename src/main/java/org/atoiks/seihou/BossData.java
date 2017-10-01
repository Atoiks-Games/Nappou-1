package org.atoiks.seihou;

/**
 *
 * @author YTENG
 */
public final class BossData {

    public final float[] bulletSeq;
    public final int hp;
    public final int timeout;
    public final int score;

    public BossData(float[] bulletSeq, int hp, int score) {
        this(bulletSeq, hp, -1, score);
    }

    public BossData(float[] bulletSeq, int hp, int timeout, int score) {
        this.bulletSeq = bulletSeq;
        this.hp = hp;
        this.timeout = timeout;
        this.score = score;
    }
}
