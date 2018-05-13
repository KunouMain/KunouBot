package samophis.kunou.main.entities.gateway.events;

import samophis.kunou.main.entities.gateway.WebSocketShard;
import javax.annotation.Nonnull;

/**
 * Represents a listener which listens for any and all {@link Event Events}.
 *
 * @author SamOphis
 * @since 0.1
 */

public interface EventListener {
    /**
     * A method which is fired for every {@link Event Event} a {@link WebSocketShard WebSocketShard} sends it.
     * @param event The <b>not-null</b> {@link Event Event} in question.
     */
    void onEvent(@Nonnull Event event);
}