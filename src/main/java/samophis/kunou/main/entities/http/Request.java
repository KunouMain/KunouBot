package samophis.kunou.main.entities.http;

import com.jsoniter.any.Any;
import samophis.kunou.main.modules.IApplicationModule;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Represents an actual request object with data attached and methods to easily submit to a {@link Requester Requester}.
 *
 * @author SamOphis
 * @since 0.1
 */

public interface Request {
    /**
     * Returns the {@link Route Route} that made this request -- contains endpoint data, the {@link Requester Requester}, etc.
     * @return The {@link Route Route} that made this request.
     */
    Route getRoute();

    /**
     * Returns the {@link IApplicationModule IApplicationModule} this request uses for authorization.
     * @return the {@link IApplicationModule IApplicationModule} this request uses for authorization.
     */
    IApplicationModule getApplicationModule();

    /**
     * Returns the <b>possibly-null</b> JSON Data attached to this request, which is sent upon being executed.
     * @return The <b>possibly-null</b> JSON Data attached to this request.
     */
    @Nullable String getData();

    /**
     * Blocks the current thread until either a response is received, or the HTTP Request failed/was cancelled.
     * <br><p>Returns <b>possibly-null</b> lazily-parsed JSON Data from a successful request.
     * This internally calls the #queue methods but blocks the calling thread <b>on-top of the internal {@link Requester Requester} threads</b> until completion.
     * Any caught exceptions caused by the request will be thrown up to the caller as {@link samophis.kunou.main.exceptions.RequestException RequestExceptions}.
     * @return <b>Possibly-null</b> lazily-parsed JSON Data from a successful request.
     * @throws samophis.kunou.main.exceptions.RequestException If any Exception occurs during a HTTP Request.
     * @see Request#queue(Consumer, Consumer)
     * @see Request#queue(Consumer)
     * @see Request#queue
     */
    @Nullable Any get();

    /**
     * Queues the request asynchronously with optional callbacks to handle responses.
     * @param onSuccess The <b>possibly-null</b> on-success callback.
     * @param onFailure The <b>possibly-null</b> on-failure callback.
     * @see Request#queue(Consumer)
     * @see Request#queue()
     * @see Request#get()
     */
    void queue(@Nullable Consumer<Any> onSuccess, @Nullable Consumer<Throwable> onFailure);

    /**
     * Queues the request asynchronously with an optional success callback to handle responses, providing {@code 'null'} for the failure callback.
     * @param onSuccess The <b>possibly-null</b> on-success callback.
     * @see Request#queue(Consumer, Consumer)
     * @see Request#queue
     * @see Request#get
     */
    void queue(@Nullable Consumer<Any> onSuccess);

    /**
     * Queues the request asynchronously with no callbacks provided to handle any responses.
     * <br><p>This option is usually pretty dangerous as <b>all</b> responses -- successful or not -- are swallowed.</p>
     * @see Request#queue(Consumer, Consumer)
     * @see Request#queue(Consumer)
     * @see Request#get
     */
    void queue();
}