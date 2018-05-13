package samophis.kunou.main.entities.gateway.events;

import com.jsoniter.any.Any;
import samophis.kunou.main.modules.IApplicationModule;
import samophis.kunou.main.modules.IGatewayModule;

/**
 * Represents an event sent to the client from the Discord Gateway.
 *
 * @author SamOphis
 * @since 0.1
 */

public interface Event {
    /**
     * Fetches the {@link IApplicationModule IApplicationModule} associated with this Event.
     * @return The {@link IApplicationModule IApplicationModule} associated with this Event.
     */
    IApplicationModule getApplicationModule();

    /**
     * Fetches the {@link IGatewayModule IGatewayModule} associated with this Event.
     * @return The {@link IGatewayModule IGatewayModule} associated with this Event.
     */
    IGatewayModule getGatewayModule();

    /**
     * Fetches the name of the Event, as provided by Discord.
     * <br><p>The name is not edited whatsoever, so a Message Create Event would have the name MESSAGE_CREATE.</p>
     * @return The <b>unedited</b> name of the Event.
     */

    String getName();
    /**
     * Fetches the JSON Data that was sent to the client.
     * @return The JSON Data of the Event.
     */
    Any getData();

    /**
     * Fetches The order in which this Event was fired.
     * @return The order in which this Event was fired.
     */
    int getEventOrder();

    /**
     * Fetches the number/ID/order of the Shard that this Event was sent to.
     * @return The ID of the Shard that this Event was sent to.
     */
    int getShardNumber();
}