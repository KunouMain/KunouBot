package samophis.kunou.main.entities.gateway.events.impl;

import com.jsoniter.any.Any;
import samophis.kunou.main.entities.gateway.events.Event;
import samophis.kunou.main.modules.IApplicationModule;
import samophis.kunou.main.modules.IGatewayModule;
import samophis.kunou.main.util.Asserter;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.Objects;

public class EventImpl implements Event {
    private final IApplicationModule application;
    private final IGatewayModule gateway;
    private final String name;
    private final Any data;
    private final int shardNumber, eventOrder;
    public EventImpl(@Nonnull IGatewayModule gateway, @Nonnull String name,
                     @Nonnull Any data, @Nonnegative int shardNumber, @Nonnegative int eventOrder) {
        this.application = Objects.requireNonNull(gateway).getApplicationModule();
        this.gateway = gateway;
        this.name = Objects.requireNonNull(name);
        this.data = Objects.requireNonNull(data);
        this.shardNumber = Asserter.requireNonNegative(shardNumber);
        this.eventOrder = Asserter.requireNonNegative(eventOrder);
    }
    @Override
    public IApplicationModule getApplicationModule() {
        return application;
    }
    @Override
    public IGatewayModule getGatewayModule() {
        return gateway;
    }
    @Override
    public String getName() {
        return name;
    }
    @Override
    public Any getData() {
        return data;
    }
    @Override
    public int getShardNumber() {
        return shardNumber;
    }
    @Override
    public int getEventOrder() {
        return eventOrder;
    }
}
