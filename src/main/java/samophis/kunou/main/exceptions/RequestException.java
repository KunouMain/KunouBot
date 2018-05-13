package samophis.kunou.main.exceptions;

/**
 * Represents an exception thrown by or during a HTTP Request.
 *
 * @author SamOphis
 * @since 0.1
 */
public class RequestException extends RuntimeException {
    /**
     * Creates a new RequestException with a custom message.
     * @param message The message to attach to this RequestException.
     */
    public RequestException(String message) {
        super(message);
    }

    /**
     * Creates a new RequestException using a Throwable as a base.
     * @param throwable The Throwable to use as a base.
     */
    public RequestException(Throwable throwable) {
        super(throwable);
    }
}
