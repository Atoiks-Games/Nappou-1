package com.ymcmp.seihou;

/**
 *
 * @author YTENG
 */
public final class BossData {

    public final float[] bulletSeq;
    public final int hp;
    public final int timeout;

    public BossData(float[] bulletSeq, int hp) {
        this(bulletSeq, hp, -1);
    }

    public BossData(float[] bulletSeq, int hp, int timeout) {
        this.bulletSeq = bulletSeq;
        this.hp = hp;
        this.timeout = timeout;
    }
}
