package org.atoiks.games.nappou1.enemies;

import org.atoiks.games.nappou1.GameComponent;
import org.atoiks.games.nappou1.PlayerManager;
import org.atoiks.games.nappou1.Utils;

/**
 *
 * @author YTENG
 */
public abstract class Enemy extends GameComponent {

    public final float COLLISION_RADIUS;
    public final int SCORE;
    public float x;
    public float y;

    protected final PlayerManager player;
    protected int hp;

    public Enemy(float collisionRadius, int score, int hp, PlayerManager player) {
        this.COLLISION_RADIUS = collisionRadius;
        this.SCORE = score;
        this.hp = hp;
        this.player = player;
    }

    @Override
    public final void update(float dt) {
        if (Utils.circlesCollide(x, y, COLLISION_RADIUS,
                player.getX(), player.getY(), PlayerManager.PLAYER_RADIUS)) {
            if (player.gotHit(-1)) {
                // if the player died, there is no point of more processing
                return;
            }
        }
        onUpdate(dt);
    }

    public final int reduceHp(int delta) {
        return hp -= delta;
    }

    public abstract void onUpdate(float dt);
}
