package org.atoiks.seihou;

/**
 *
 * @author YTENG
 */
public final class State {

    public static final byte LOADING = -1;
    public static final byte INIT = 0;
    public static final byte PLAYING = 1;
    public static final byte HIT_ANIM = 2;
    public static final byte PAUSE = 3;
    public static final byte LOSE = 4;
    public static final byte WIN = 5;
    public static final byte ADVANCE = 6;
    public static final byte HELP = 7;
    
    public static final byte STORY_MODE = 0;
    public static final byte ENDLESS_MODE = 1;

    private State() {
    }
}
