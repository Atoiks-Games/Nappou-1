package org.atoiks.seihou;

/**
 *
 * @author YTENG
 */
public final class Utils {

    public static boolean circlesCollide(float x1, float y1, float r1, float x2, float y2, float r2) {
        return (r1 + r2) > Math.hypot(x2 - x1, y2 - y1);
    }

    private Utils() {
    }
}
