package samophis.kunou.main.entities.gateway;

import javax.annotation.Nonnull;

/**
 * Represents a <b>blocking</b> IDENTIFY Rate Limiter. This will manually block the calling thread until the rate limit is over.
 * <br><p>Furthermore, the method <b>SHOULD BE synchronized</b> meaning only one Thread can identify at a time, stopping abuse.
 * If you don't know what you're doing, simply use the default IDENTIFY Rate Limiter.</p>
 *
 * @author SamOphis
 * @since 0.1
 */

public interface IdentifyLimiter {
    /** The length of a "rate limit window", in milliseconds. */
    int TIME_WINDOW = 5000;
    /** The amount of requests you can send in a "rate limit window". */
    int LIMIT = 1;

    /**
     * Should be a <b>synchronized</b> method that will wait if necessary to safely IDENTIFY without Discord terminating our session.
     * <br><p>To get better load-up times, keep track of when the "rate limit window" is refreshed so that in some cases you won't need to
     * wait the full {@value TIME_WINDOW} milliseconds.</p>
     * @param shard The <b>not-null</b> {@link WebSocketShard WebSocketShard} to IDENTIFY.
     * @throws NullPointerException If {@code 'shard'} is null.
     */
    void attemptIdentify(@Nonnull WebSocketShard shard);

    /**
     * Shuts down the internal thread pool used to reset the rate limit.
     * <br><p>Custom implementations should shutdown their resources their own way if they differ from the default implementation.</p>
     */
    void shutdown();
}
