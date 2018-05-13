package samophis.kunou.main.entities.gateway.events;

import com.jsoniter.any.Any;
import samophis.kunou.main.entities.gateway.events.impl.EventImpl;
import samophis.kunou.main.modules.IGatewayModule;
import samophis.kunou.main.util.Asserter;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * A builder which builds {@link Event Events} with the provided information.
 *
 * @author SamOphis
 * @since 0.1
 */

public class EventBuilder {
    private final IGatewayModule gateway;
    private String name;
    private Any data;
    private int shardNumber, eventOrder;
    public EventBuilder(IGatewayModule gateway) {
        this.gateway = gateway;
    }
    public EventBuilder setName(@Nonnull String name) {
        this.name = name;
        return this;
    }
    public EventBuilder setData(@Nonnull Any data) {
        this.data = data;
        return this;
    }
    public EventBuilder setShardNumber(@Nonnegative int shardNumber) {
        this.shardNumber = Asserter.requireNonNegative(shardNumber);
        return this;
    }
    public EventBuilder setEventOrder(@Nonnegative int eventOrder) {
        this.eventOrder = Asserter.requireNonNegative(eventOrder);
        return this;
    }
    public Event build() {
        return new EventImpl(gateway, name, data, shardNumber, eventOrder);
    }
}
