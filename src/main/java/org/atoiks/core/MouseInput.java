package org.atoiks.core;

/**
 *
 * @author YTENG
 */
public interface MouseInput {

    public abstract boolean isMouseUp(int btn);

    public abstract boolean isMouseDown(int btn);

    public abstract boolean isMouseClicked(int btn);

    public abstract boolean isCursorIn(int x1, int y1, int x2, int y2);

    public abstract int getCursorX();

    public abstract int getCursorY();
}
