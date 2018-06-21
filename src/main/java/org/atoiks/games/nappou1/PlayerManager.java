package org.atoiks.games.nappou1;

/**
 *
 * @author YTENG
 */
public abstract class PlayerManager {

    public static final int PLAYER_RADIUS = 8;

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

    /**
     *
     * @param deltaHp
     * @return true if now the player is considered dead (hp &le; 0), false
     * otherwise
     */
    public abstract boolean gotHit(int deltaHp);

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
