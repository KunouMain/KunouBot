package samophis.kunou.main.entities.gateway.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import samophis.kunou.main.entities.gateway.EventLimiter;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class EventLimiterImpl implements EventLimiter {
    private static final Logger LOGGER = LoggerFactory.getLogger(EventLimiterImpl.class);
    private final Queue<DataPair> queue;
    private final ScheduledExecutorService pool;
    private final AtomicInteger remaining;
    private volatile boolean isActive;
    public EventLimiterImpl() {
        this.queue = new ConcurrentLinkedQueue<>();
        this.pool = Executors.newScheduledThreadPool(2);
        this.remaining = new AtomicInteger(EventLimiter.LIMIT);
        this.isActive = true;
        pool.scheduleAtFixedRate(() -> remaining.set(EventLimiter.LIMIT), 0, EventLimiter.TIME_WINDOW, TimeUnit.MILLISECONDS);
        pool.submit(() -> {
            do {
                if (remaining.get() == 0)
                    continue;
                DataPair pair = queue.poll();
                if (pair == null)
                    continue;
                remaining.decrementAndGet();
                try {
                    pair.handler.accept(pair.data);
                } catch (Throwable throwable) {
                    LOGGER.error("Error when executing Event Handler!", throwable);
                }
            } while (isActive);
        });
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (!pool.isShutdown())
                pool.shutdownNow();
        }));
    }
    @Override
    public void submitEvent(@Nonnull String data, @Nonnull Consumer<String> handler) {
        queue.add(new DataPair(Objects.requireNonNull(data), Objects.requireNonNull(handler)));
    }
    @Override
    public void shutdown() {
        if (!isActive)
            throw new IllegalStateException("EventLimiter already shut-down!");
        isActive = false;
        pool.shutdown();
        queue.clear();
    }
    private class DataPair {
        private String data;
        private Consumer<String> handler;
        private DataPair(String data, Consumer<String> handler) {
            this.data = data;
            this.handler = handler;
        }
    }
}
