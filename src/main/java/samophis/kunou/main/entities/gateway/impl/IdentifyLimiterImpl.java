package samophis.kunou.main.entities.gateway.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import samophis.kunou.main.entities.gateway.IdentifyLimiter;
import samophis.kunou.main.entities.gateway.WebSocketShard;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class IdentifyLimiterImpl implements IdentifyLimiter {
    private static final Logger LOGGER = LoggerFactory.getLogger(IdentifyLimiterImpl.class);
    private final ScheduledExecutorService pool;
    private long lastResetTime;
    public IdentifyLimiterImpl() {
        this.pool = Executors.newSingleThreadScheduledExecutor();
        pool.scheduleAtFixedRate(() -> lastResetTime = System.currentTimeMillis(), 0, IdentifyLimiter.TIME_WINDOW, TimeUnit.MILLISECONDS);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (!pool.isShutdown())
                pool.shutdownNow();
        }));
    }
    @Override
    public void shutdown() {
        if (pool.isShutdown())
            throw new IllegalStateException("IdentifyLimiter already shut-down!");
        lastResetTime = 0;
        pool.shutdown();
    }
    @Override
    public synchronized void attemptIdentify(@Nonnull WebSocketShard shard) {
        Objects.requireNonNull(shard);
        long result = System.currentTimeMillis() - lastResetTime;
        int window = IdentifyLimiter.TIME_WINDOW;
        if (result < window) {
            try {
                Thread.sleep(window - result);
            } catch (InterruptedException exc) {
                LOGGER.warn("Thread interrupted while waiting to IDENTIFY -- wait terminated, IDENTIFY wasn't sent!");
                Thread.currentThread().interrupt();
                throw new RuntimeException(exc);
            }
        }
        shard.sendIdentify();
    }
}
