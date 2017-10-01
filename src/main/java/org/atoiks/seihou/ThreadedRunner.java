package org.atoiks.seihou;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @author YTENG
 */
public final class ThreadedRunner implements GameRunner {

    private static final GameRunner INSTANCE = new ThreadedRunner();

    private final ExecutorService pool;

    private ThreadedRunner() {
        this.pool = Executors.newFixedThreadPool(1);
    }

    @Override
    public void run(Game g) {
        final AtomicBoolean cleanFlag = new AtomicBoolean(true);
        try {
            g.init();
            pool.submit(() -> {
                try {
                    while (g.isRunning() && cleanFlag.get()) {
                        g.render();
                    }
                } catch (RuntimeException ex) {
                    cleanFlag.set(false);
                }
            });

            long lastTime = System.currentTimeMillis();
            while (g.isRunning() && cleanFlag.get()) {
                final long current = System.currentTimeMillis();
                g.update(current - lastTime);
                lastTime = current;
                try {
                    // Prevent YouTube videos from glitching
                    Thread.sleep(15);
                } catch (InterruptedException ex) {
                }
            }
        } finally {
            this.pool.shutdown();
            g.destroy();
        }
    }

    public static GameRunner getInstance() {
        return INSTANCE;
    }
}
