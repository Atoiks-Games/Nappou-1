package org.atoiks.games.nappou1;

/**
 *
 * @author YTENG
 */
public final class Utils {

    public static boolean circlesCollide(float x1, float y1, float r1, float x2, float y2, float r2) {
        final float ir = r1 + r2;
        final float dx = x2 - x1;
        final float dy = y2 - y1;
        return ir * ir > dx * dx + dy * dy;
    }

    private Utils() {
    }
}
