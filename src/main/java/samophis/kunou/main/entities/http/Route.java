package samophis.kunou.main.entities.http;

import samophis.kunou.main.modules.IApplicationModule;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents a route pointing to some URL with information attached to it.
 *
 * @author SamOphis
 * @since 0.1
 */

public interface Route {
    /**
     * Retrieves the endpoint attached to this route.
     * @return The endpoint attached to this route.
     */
    String getUrl();

    /**
     * Retrieves the {@link RequestType RequestType} that describes this route.
     * @return The {@link RequestType RequestType} attached to this route.
     */
    RequestType getType();

    /**
     * Retrieves the {@link Requester Requester} that takes in and executes requests, handling rate limits, etc.
     * @return The {@link Requester Requester} attached to this route.
     */
    Requester getRequester();

    /**
     * Forms a {@link Request Request} ready to be executed.
     * @param module The <b>not-null</b> {@link IApplicationModule IApplicationModule} used to gain authorization information.
     * @param data The <b>possibly-null</b> JSON Data to send.
     * @return A new {@link Request Request} object which can then be executed.
     * @throws NullPointerException If {@code 'module'} is {@code 'null'}
     */
    Request asRequest(@Nonnull IApplicationModule module, @Nullable String data);

    /**
     * Formats the Route Endpoint and creates a new one using the same {@link Requester Requester} and {@link RequestType RequestType}.
     * @param params The <b>non-null</b> parameters to format the original endpoint with.
     * @return A nearly-identical Route object with a formatted endpoint.
     * @throws NullPointerException If {@code 'params'} is {@code 'null'}.
     */
    Route format(@Nonnull Object... params);
}