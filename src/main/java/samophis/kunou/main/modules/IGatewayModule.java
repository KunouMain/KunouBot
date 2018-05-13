package samophis.kunou.main.modules;

import samophis.kunou.core.modules.Module;
import samophis.kunou.main.entities.gateway.IdentifyLimiter;
import samophis.kunou.main.entities.gateway.events.EventListener;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.List;

/**
 * Module Declaration which covers the Discord Gateway so that users can make their own implementations without having to waste performance by parsing messages.
 * <br><p>It's strongly recommended to maximize performance here while making sure you monitor/manage state and active code.
 * Once ordered to shutdown, your Gateway Module <b>must</b> shutdown <b>ALL</b> running code, set its state properly and be designed so that it can re-initialize later.
 * <br><b>Note: The Gateway Module depends on an {@link IApplicationModule IApplicationModule} in order to gather information like shard count, tokens, etc.</b>
 *
 * @author SamOphis
 * @since 0.1
 */

public interface IGatewayModule extends Module {
    /**
     * Fetches the {@link IApplicationModule IApplicationModule} that manages this module which contains initial start-up information.
     * @return The {@link IApplicationModule IApplicationModule} attached to this module.
     */
    IApplicationModule getApplicationModule();

    /**
     * Fetches the {@link IRestModule IRestModule} used to send the initial request via the Discord REST API for Shard Count and Gateway Endpoints.
     * @return The {@link IRestModule IRestModule} attached to this module.
     */
    IRestModule getRestModule();

    /**
     * Fetches the {@link IdentifyLimiter IdentifyLimiter} used to safely rate limit IDENTIFY payloads.
     * @return The {@link IdentifyLimiter IdentifyLimiter} used by all shards attached to this module.
     */
    IdentifyLimiter getIdentifyRateLimiter();

    /**
     * Fetches an unmodifiable view of all the {@link EventListener EventListeners} attached to this Gateway Module.
     * @return An unmodifiable view of all the {@link EventListener EventListeners} this Gateway Module is aware of.
     */
    List<EventListener> getEventListeners();

    /**
     * Gets the Gateway Endpoint, as determined by the response from the {@link IRestModule#getGatewayRequest()} request.
     * @return The Gateway Endpoint to connect shards to.
     */
    String getGatewayEndpoint();

    /**
     * Gets the number of recommended shards, as determined by the response from the {@link IRestModule#getGatewayRequest()}} request.
     * @return The number of shards to run, as recommended by Discord.
     */
    int getGatewayShards();
    /**
     * Should send text data through an active/open WebSocket connection to the Discord Gateway (on a specific shard).
     * <br><p>For more convenience -- and if your version of Kunou only runs on one shard -- see the method overload that automatically sends data to Shard #0.</p>
     * @param shard The <b>not-negative</b> number/index of the shard to send data to.
     * @param data The <b>not-null</b> data to send to a shard.
     * @throws IllegalArgumentException If {@code 'shard'} is negative or if it doesn't correlate to a pre-existing {@link samophis.kunou.main.entities.gateway.WebSocketShard WebSocketShard}.
     * @throws NullPointerException If {@code 'data'} is {@code null}.
     * @throws samophis.kunou.core.exceptions.ModuleException If the module or its contents are not in the right state to send data to.
     * @see IGatewayModule#sendText(String)
     */
    void sendText(@Nonnegative int shard, @Nonnull String data);
    /**
     * Should send text data through an active/open WebSocket connection to the Discord Gateway (on Shard #0).
     * <br><p>This will assume the shard you're trying to send data to is Shard #0. See the method overload in order to specify a specific shard.</p>
     * @param data The <b>not-null</b> data to send to Shard #0.
     * @throws NullPointerException If {@code 'data'} is {@code null}.
     * @throws samophis.kunou.core.exceptions.ModuleException If the module or its contents are not in the right state to send data to.
     * @see IGatewayModule#sendText(int, String)
     */
    void sendText(@Nonnull String data);

    /**
     * Adds a {@link EventListener EventListener} to this module.
     * <br><p>Whenever any {@link samophis.kunou.main.entities.gateway.WebSocketShard WebSocketShard} attached to this module receives an
     * {@link samophis.kunou.main.entities.gateway.events.Event Event}, it is broadcast to each listener also attached to this module.
     * @param listener The <b>not-null</b> {@link EventListener EventListener} to add to this module.
     * @throws NullPointerException If {@code 'listener'} is null.
     */
    void addListener(@Nonnull EventListener listener);
}