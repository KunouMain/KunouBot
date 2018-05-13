package samophis.kunou.main.exceptions;

/**
 * Represents a RuntimeException caused by a {@link samophis.kunou.main.entities.gateway.WebSocketShard WebSocketShard}.
 *
 * @author SamOphis
 * @since 0.1
 */

public class SocketException extends RuntimeException {
    /**
     * Creates a new SocketException using a Throwable as a base.
     * @param throwable The Throwable to use as a base.
     */
    public SocketException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Creates a new SocketException with a custom message.
     * @param message The message to attach to this exception.
     */
    public SocketException(String message) {
        super(message);
    }
}
