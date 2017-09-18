package com.ymcmp.seihou;

/**
 *
 * @author YTENG
 */
public final class State {

    public static final byte LOADING = -1;
    public static final byte INIT = 0;
    public static final byte PLAYING = 1;
    public static final byte DIE_ANIM = 2;
    public static final byte PAUSE = 3;
    public static final byte LOSE = 4;
    public static final byte WIN = 5;

    private State() {
    }
}
