package com.ymcmp.seihou;

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
public class AbstractGame implements Game {

    protected final JFrame frame;
    protected final JPanel canvas;

    private boolean runFlag;
    private BitSet keybuf;

    public AbstractGame() {
        this("");
    }

    public AbstractGame(String title) {
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

    protected boolean isKeyDown(int keycode) {
        if (keycode < keybuf.length()) {
            return keybuf.get(keycode);
        }
        return false;
    }

    protected boolean isKeyUp(int keycode) {
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
                AbstractGame.this.runFlag = false;
            }
        });
        this.frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() < AbstractGame.this.keybuf.size()) {
                    AbstractGame.this.keybuf.set(e.getKeyCode());
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() < AbstractGame.this.keybuf.size()) {
                    AbstractGame.this.keybuf.clear(e.getKeyCode());
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

    public void render(Graphics g) {
    }
}
