package com.ymcmp.seihou;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author YTENG
 */
public class BulletManager {

    private final List<Float> x;
    private final List<Float> y;
    private final List<Float> r;
    private final List<Float> dx;
    private final List<Float> dy;

    private int x1;
    private int y1;
    private int x2;
    private int y2;

    public BulletManager(int cap, int x1, int y1, int x2, int y2) {
        this.x = new ArrayList<>(cap);
        this.y = new ArrayList<>(cap);
        this.r = new ArrayList<>(cap);
        this.dx = new ArrayList<>(cap);
        this.dy = new ArrayList<>(cap);

        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    public BulletManager(int cap) {
        this(cap, 0, 0, 0, 0);
    }

    public void addBullet(float x, float y, float r, float dx, float dy) {
        this.x.add(x);
        this.y.add(y);
        this.r.add(r);
        this.dx.add(dx);
        this.dy.add(dy);
    }

    public void clear() {
        x.clear();
        y.clear();
        r.clear();
        dx.clear();
        dy.clear();
    }

    public void changeBox(int x1, int y1, int x2, int y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    public boolean testCollision(float x, float y, float r) {
        for (int i = 0; i < this.r.size(); ++i) {
            // Prevent awkard "The bullet for sure did not touch me scenarios"
            // the radius of the player is made smaller
            if (Utils.circlesCollide(this.x.get(i), this.y.get(i), this.r.get(i),
                    x, y, r)) {
                this.r.remove(i);
                this.x.remove(i);
                this.y.remove(i);
                this.dx.remove(i);
                this.dy.remove(i);
                return true;
            }
        }
        return false;
    }

    public void updatePosition(final float dt) {
        for (int i = 0; i < r.size(); ++i) {
            final float radius = r.get(i);
            final float newX = x.get(i) + dx.get(i) * dt;
            if (newX + radius > x1 && newX - radius < x2) {
                final float newY = y.get(i) + dy.get(i) * dt;
                if (newY + radius > y1 && newY - radius < y2) {
                    x.set(i, newX);
                    y.set(i, newY);
                    continue;
                }
            }

            // Reaching here means bullet went out of bounds, destroy
            r.remove(i);
            x.remove(i);
            y.remove(i);
            dx.remove(i);
            dy.remove(i);
            --i;
        }
    }

    public void render(Graphics g) {
        try {
            for (int i = 0; i < r.size(); ++i) {
                final float radius = r.get(i);
                g.drawOval((int) (x.get(i) - radius), (int) (y.get(i) - radius),
                        (int) (radius * 2), (int) (radius * 2));
            }
        } catch (IndexOutOfBoundsException ex) {
        }
    }
}
