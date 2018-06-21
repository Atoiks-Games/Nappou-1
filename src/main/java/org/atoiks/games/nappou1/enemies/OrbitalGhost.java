package org.atoiks.games.nappou1.enemies;

import org.atoiks.games.nappou1.PlayerManager;

public class OrbitalGhost extends AbstractOrbital {

    private final float CENTER_X;
    private final float CENTER_Y;

    public OrbitalGhost(float x, float y, float r, float theta, float size, PlayerManager player) {
        super(r, theta, size, player);
        this.CENTER_X = x;
        this.CENTER_Y = y;
    }

    @Override
    protected float centerX() {
        return this.CENTER_X;
    }

    @Override
    protected float centerY() {
        return this.CENTER_Y;
    }
}
