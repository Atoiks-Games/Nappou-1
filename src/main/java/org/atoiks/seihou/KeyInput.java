package org.atoiks.seihou;

/**
 *
 * @author YTENG
 */
public interface KeyInput {

    public abstract boolean isKeyDown(int keycode);

    public abstract boolean isKeyUp(int keycode);

    public default boolean isKeyPressed(final int kc) {
        if (this.isKeyDown(kc)) {
            while (this.isKeyDown(kc)) {
                try {
                    Thread.sleep(0);
                } catch (InterruptedException ex) {
                }
            }
            return true;
        }
        return false;
    }
}
