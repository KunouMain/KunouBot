package samophis.kunou.main.entities.http;

import javax.annotation.Nonnull;

/**
 * A requester class, one created for each unique Discord API {@link Route Route} (not including "compiled parameters").
 * <br><p>Each requester has two threads: one to constantly poll a queue for incoming requests, and another handle rate limiting.</p>
 *
 * @author SamOphis
 * @since 0.1
 */

public interface Requester {
    /**
     * Submits an {@link InnerRequest InnerRequest} to the internal request queue for later processing.
     * @param request The <b>not-null</b> {@link InnerRequest InnerRequest} to submit.
     * @throws NullPointerException If {@code 'request'} is {@code null}.
     */
    void submitRequest(@Nonnull InnerRequest request);

    /**
     * Used <b>after a call to {@link Requester#shutdown()}</b> to re-start the thread pool.
     * <br><p>Used in conjunction with KunouCore to provide a rebootable, modular experience.
     * Additionally, this <b>MUST</b> be used after being shutdown -- the constructor already starts the thread pool up.</p>
     */
    void startup();

    /**
     * Stops the loop which accepts queued requests (essentially shutting down this Requester).
     * <br><p>This also shuts down the internal thread pool backing the Requester.</p>
     * @throws IllegalStateException If the thread pool has already been shut down.
     */
    void shutdown();

    /**
     * Returns the amount of remaining requests that can be made before Discord enforces a rate limit.
     * @return The amount of remaining requests that can be made before Discord enforces a rate limit.
     */
    int getRemainingRequests();

    /**
     * Returns the amount of requests that can be made within a rate limit "window".
     * @return The amount of requests that can be made within a rate limit "window".
     */
    int getRequestLimit();

    /**
     * Returns the reset time of the rate limit window local to the application.
     * @return The reset time of the rate limit window local to the application.
     */
    long getFutureResetTimeMs();

    /**
     * Returns the last reset time of the rate limit window local to the application.
     * @return The last reset time of the rate limit window local to the application.
     */
    long getLastResetTimeMs();
}