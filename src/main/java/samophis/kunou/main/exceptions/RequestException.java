package samophis.kunou.main.exceptions;

/**
 * Represents an exception thrown by or during a HTTP Request.
 *
 * @author SamOphis
 * @since 0.1
 */
public class RequestException extends RuntimeException {
    public RequestException(String message) {
        super(message);
    }
    public RequestException(Throwable throwable) {
        super(throwable);
    }
}
