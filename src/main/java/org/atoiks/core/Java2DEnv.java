package org.atoiks.core;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;

import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;

import java.util.BitSet;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author YTENG
 */
public class Java2DEnv implements Frame, KeyInput, MouseInput, GameCanvas {

    protected final JFrame frame;
    protected final JPanel canvas;

    private boolean runFlag;
    private BitSet keybuf;
    private BitSet mousebuf;

    private Game attachedGame;

    public Java2DEnv() {
        this("");
    }

    public Java2DEnv(String title) {
        this.frame = new JFrame(title);
        this.canvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Java2DEnv.this.attachedGame.render(g);
            }
        };
        this.keybuf = new BitSet(256);
        this.mousebuf = new BitSet(Math.max(java.awt.MouseInfo.getNumberOfButtons(), 0));

        this.frame.setContentPane(canvas);
    }

    @Override
    public void attachGame(Game game) {
        if (this.attachedGame == null) {
            this.attachedGame = game;
        }
    }

    @Override
    public boolean isKeyDown(int keycode) {
        if (keycode < keybuf.length()) {
            return keybuf.get(keycode);
        }
        return false;
    }

    @Override
    public boolean isKeyUp(int keycode) {
        if (keycode < keybuf.length()) {
            return !keybuf.get(keycode);
        }
        return true;
    }

    @Override
    public boolean isKeyPressed(int keycode) {
        if (isKeyDown(keycode)) {
            keybuf.clear(keycode);
            return true;
        }
        return false;
    }

    @Override
    public boolean isMouseDown(int btn) {
        if (btn < mousebuf.length()) {
            return mousebuf.get(btn);
        }
        return false;
    }

    @Override
    public boolean isMouseUp(int btn) {
        if (btn < mousebuf.length()) {
            return !mousebuf.get(btn);
        }
        return false;
    }

    @Override
    public boolean isMouseClicked(int btn) {
        if (isMouseDown(btn)) {
            mousebuf.clear(btn);
            return true;
        }
        return false;
    }

    @Override
    public boolean isCursorIn(int x1, int y1, int x2, int y2) {
        final Point pt = this.frame.getMousePosition();
        return pt.x >= x1 && pt.x <= x2 && pt.y >= y1 && pt.y <= y2;
    }

    @Override
    public int getCursorX() {
        return this.frame.getMousePosition().x;
    }

    @Override
    public int getCursorY() {
        return this.frame.getMousePosition().y;
    }

    @Override
    public final void initEnv() {
        this.runFlag = true;
        this.frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Java2DEnv.this.runFlag = false;
            }
        });
        this.frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() < Java2DEnv.this.keybuf.size()) {
                    Java2DEnv.this.keybuf.set(e.getKeyCode());
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() < Java2DEnv.this.keybuf.size()) {
                    Java2DEnv.this.keybuf.clear(e.getKeyCode());
                }
            }
        });
        this.frame.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Java2DEnv.this.mousebuf.set(e.getButton());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                Java2DEnv.this.mousebuf.clear(e.getButton());
            }
        });
        this.attachedGame.keyboard = this;
        this.attachedGame.mouse = this;
        this.attachedGame.canvas = this;
        this.attachedGame.frame = this;
        this.attachedGame.init();
    }

    @Override
    public void destroyEnv() {
        this.attachedGame.destroy();
        frame.dispose();
    }

    @Override
    public boolean isRunning() {
        return runFlag;
    }

    @Override
    public void abort() {
        runFlag = false;
    }

    @Override
    public void invokeUpdate(long deltaT) {
        this.attachedGame.update(deltaT);
    }

    @Override
    public void invokeRender() {
        frame.repaint();
    }

    @Override
    public int getWidth() {
        return canvas.getWidth();
    }

    @Override
    public int getHeight() {
        return canvas.getHeight();
    }

    @Override
    public void setSize(int width, int height) {
        frame.setSize(width, height);
    }

    @Override
    public void setResizable(boolean resizability) {
        frame.setResizable(resizability);
    }

    @Override
    public void setVisible(boolean visibility) {
        frame.setVisible(visibility);
    }

    @Override
    public void moveToCenter() {
        frame.setLocationRelativeTo(null);
    }

    @Override
    public void moveTo(int x, int y) {
        frame.setLocation(x, y);
    }

    @Override
    public void setIcon(Image img) {
        frame.setIconImage(img);
    }

    @Override
    public void setTitle(String title) {
        frame.setTitle(title);
    }
}
