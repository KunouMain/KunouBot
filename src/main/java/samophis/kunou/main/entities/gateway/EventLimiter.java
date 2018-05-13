package samophis.kunou.main.entities.gateway;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

/**
 * Represents a <b>not-blocking</b> rate limiter for events sent by the client to Discord.
 * <br><p>This rate limiter <b>does not block</b>, unlike the {@link IdentifyLimiter IdentifyLimiter}.
 * Instead, all events should be added to a queue and steadily sent following the rate limits.</p>
 *
 * @author SamOphis
 * @since 0.1
 */

public interface EventLimiter {
    /** The length of a "rate limit window", in milliseconds. */
    int TIME_WINDOW = 60000;
    /** The amount of events which can be sent in a "rate limit window". */
    int LIMIT = 120;

    /**
     * Submits an event along with its handler to the internal queue, ready for processing.
     * @param data The <b>not-null</b> data to send to Discord.
     * @param handler The <b>not-null</b> callback to use in order to "handle" the data, such as by sending it to a socket which this callback specifies.
     * @throws NullPointerException If {@code 'data'} or {@code 'handler'} are null.
     */
    void submitEvent(@Nonnull String data, @Nonnull Consumer<String> handler);

    /**
     * Shuts down the internal thread pool responsible for processing requests and resetting the rate limit.
     * <br><p>Custom implementations should shutdown resources their own way if they differ from the default implementation.</p>
     */
    void shutdown();
}