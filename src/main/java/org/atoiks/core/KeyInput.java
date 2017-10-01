package org.atoiks.core;

/**
 *
 * @author YTENG
 */
public interface KeyInput {

    /**
     * Test if the key is pressed down at the moment
     *
     * @param keycode The key code of the key
     * @return true if key is pressed down, false if key is released or key does
     * not exist
     */
    public abstract boolean isKeyDown(int keycode);

    /**
     * Test if the key is released at the moment
     *
     * @param keycode The key code of the key
     * @return true if key is released, false if key is pressed down or key does
     * not exist
     */
    public abstract boolean isKeyUp(int keycode);

    /**
     * Test if the key is pressed
     *
     * @param keycode The key code of the key
     * @return true if key is pressed, false if key is released of key does not
     * exist
     */
    public abstract boolean isKeyPressed(final int keycode);
}
