package org.atoiks.seihou.enemies;

import org.atoiks.seihou.PlayerManager;

public class BossOrbitalGhost extends AbstractOrbital {

    private final PlayerManager BOSS_MGR;

    public BossOrbitalGhost(PlayerManager bossMgr, float r, float theta, float size) {
        super(r, theta, size);
        this.BOSS_MGR = bossMgr;
    }

    @Override
    protected float centerX() {
        return this.BOSS_MGR.getX();
    }

    @Override
    protected float centerY() {
        return this.BOSS_MGR.getY();
    }
}
