package org.atoiks.core;

import java.awt.Graphics;

import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;

import java.util.BitSet;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author YTENG
 */
public abstract class Java2DGame implements Game, GameCanvas, KeyInput {

    protected final JFrame frame;
    protected final JPanel canvas;

    private boolean runFlag;
    private BitSet keybuf;

    public Java2DGame() {
        this("");
    }

    public Java2DGame(String title) {
        this.frame = new JFrame(title);
        this.canvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                render(g);
            }
        };
        this.keybuf = new BitSet(256);

        this.frame.setContentPane(canvas);
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
    public void init() {
        this.runFlag = true;
        this.frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Java2DGame.this.runFlag = false;
            }
        });
        this.frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() < Java2DGame.this.keybuf.size()) {
                    Java2DGame.this.keybuf.set(e.getKeyCode());
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() < Java2DGame.this.keybuf.size()) {
                    Java2DGame.this.keybuf.clear(e.getKeyCode());
                }
            }
        });
    }

    @Override
    public void destroy() {
        frame.dispose();
    }

    @Override
    public boolean isRunning() {
        return runFlag;
    }

    protected void abort() {
        runFlag = false;
    }

    @Override
    public void update(long deltaT) {
    }

    @Override
    public final void render() {
        frame.repaint();
    }

    @Override
    public void render(Graphics g) {
    }

    @Override
    public int getHeight() {
        return canvas.getHeight();
    }

    @Override
    public int getWidth() {
        return canvas.getWidth();
    }
}
