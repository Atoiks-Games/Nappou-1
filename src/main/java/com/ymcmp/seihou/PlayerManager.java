package com.ymcmp.seihou;

/**
 *
 * @author YTENG
 */
public class PlayerManager {

    private float x;
    private float y;
    private int hp;
    private int score;

    public void resetHp() {
        hp = 0;
    }

    public void resetScore() {
        score = 0;
    }

    public int deltaHp(int delta) {
        return hp += delta;
    }

    public int deltaScore(int delta) {
        return score += delta;
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }

    public int getHp() {
        return hp;
    }

    public int getScore() {
        return score;
    }

    public void translateX(float dx) {
        x += dx;
    }

    public void translateY(float dy) {
        y += dy;
    }
}
