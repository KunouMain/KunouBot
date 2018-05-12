package samophis.kunou.main.entities.http;

import com.jsoniter.any.Any;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * Represents the true inner request submitted to the {@link Requester Requesters}.
 * <br><p>This holds callbacks and general references to the original {@link Route Route} and {@link Request Request}.</p>
 *
 * @author SamOphis
 * @since 0.1
 */

public interface InnerRequest {
    /** The Logger Object used for the default error callback */
    Logger LOGGER = LoggerFactory.getLogger(InnerRequest.class);
    /** The default success callback */
    Consumer<Any> DEFAULT_SUCCESS = obj -> {};
    /** The default error callback */
    Consumer<Throwable> DEFAULT_ERROR = thr -> LOGGER.error("Error occurred due to or during a HTTP Request!", thr);

    /**
     * Retrieves the original {@link Route Route} object attached to this request.
     * @return The original {@link Route Route} associated with this request.
     */
    Route getRoute();

    /**
     * Retrieves the original {@link Request Request} object attached to this (inner) request.
     * @return The original {@link Request Request} object attached to this (inner) request.
     */
    Request getRequest();

    /**
     * Returns the success callback executed upon success.
     * <br><p>The success callback automatically becomes the {@link InnerRequest#DEFAULT_SUCCESS DEFAULT_SUCCESS} callback if it was specified as {@code 'null'} during construction.</p>
     * @return The success callback attached to this request.
     */
    Consumer<Any> getSuccessCallback();

    /**
     * Returns the error callback executed upon error.
     * <br><p>The error callback automatically becomes the {@link InnerRequest#DEFAULT_ERROR DEFAULT_ERROR} callback if it was specified as {@code 'null'} during construction.</p>
     * @return The error callback attached to this request.
     */
    Consumer<Throwable> getFailureCallback();
}