package org.atoiks.core;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
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
    public void start(Environment g) {
        final AtomicBoolean cleanFlag = new AtomicBoolean(true);
        try {
            g.initEnv();
            pool.submit(() -> {
                try {
                    while (g.isRunning() && cleanFlag.get()) {
                        g.invokeRender();
                    }
                } catch (RuntimeException ex) {
                    cleanFlag.set(false);
                }
            });

            long lastTime = System.currentTimeMillis();
            while (g.isRunning() && cleanFlag.get()) {
                final long current = System.currentTimeMillis();
                g.invokeUpdate(current - lastTime);
                lastTime = current;
                try {
                    // Prevent YouTube videos from glitching
                    Thread.sleep(15);
                } catch (InterruptedException ex) {
                }
            }
        } finally {
            this.pool.shutdown();
            g.destroyEnv();
        }
    }

    public static GameRunner getInstance() {
        return INSTANCE;
    }
}
