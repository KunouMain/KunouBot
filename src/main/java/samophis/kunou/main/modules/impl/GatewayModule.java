package samophis.kunou.main.modules.impl;

import com.jsoniter.any.Any;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import samophis.kunou.core.modules.AbstractModuleBase;
import samophis.kunou.core.modules.Module;
import samophis.kunou.core.modules.ModuleLoader;
import samophis.kunou.core.modules.State;

import samophis.kunou.main.entities.gateway.IdentifyLimiter;
import samophis.kunou.main.entities.gateway.WebSocketShard;
import samophis.kunou.main.entities.gateway.events.EventListener;
import samophis.kunou.main.entities.gateway.impl.EventLimiterImpl;
import samophis.kunou.main.entities.gateway.impl.IdentifyLimiterImpl;

import samophis.kunou.main.modules.IApplicationModule;
import samophis.kunou.main.modules.IGatewayModule;
import samophis.kunou.main.modules.IRestModule;
import samophis.kunou.main.util.Asserter;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class GatewayModule extends AbstractModuleBase implements IGatewayModule {
    public static GatewayModule getInstance(@Nonnull ModuleLoader loader, @Nonnull IApplicationModule application, @Nonnull IRestModule rest) {
        return new GatewayModule(loader, application, rest);
    }
    private final IApplicationModule application;
    private final IRestModule rest;
    private final List<EventListener> listeners;
    private IdentifyLimiter limiter;
    private Int2ObjectMap<WebSocketShard> shards;
    private String gatewayEndpoint;
    private int shardCount;
    private GatewayModule(@Nonnull ModuleLoader loader, @Nonnull IApplicationModule application, @Nonnull IRestModule rest) {
        super(loader);
        this.application = Objects.requireNonNull(application);
        this.rest = Objects.requireNonNull(rest);
        this.listeners = new ObjectArrayList<>();
    }
    @SuppressWarnings("ConstantConditions")
    @Override
    public void onStart(@Nullable String... args) {
        try {
            state = State.STARTING;
            Any data = rest.getGatewayRequest().get();
            this.gatewayEndpoint = data.get("url").toString();
            this.shardCount = data.get("shards").toInt();
            this.limiter = new IdentifyLimiterImpl();
            this.shards = new Int2ObjectOpenHashMap<>(shardCount);
            state = State.STARTED;
            for (int id = 0; id < shardCount; id++)
                shards.put(id, new WebSocketShard(this, new EventLimiterImpl(), id));
            state = State.READY;
        } catch (Throwable thr) {
            thr.printStackTrace();
        }
    }
    @Override
    public void onMessage(@Nonnull String... args) {}
    @Override
    public void onDeath() {
        state = State.SHUTTING_DOWN;
        limiter.shutdown();
        shards.values().forEach(WebSocketShard::shutdown);
        state = State.DEAD;
    }
    @Override
    public void sendText(@Nonnull String data) {
        sendText(0, data);
    }
    @Override
    public void sendText(@Nonnegative int shard, @Nonnull String data) {
        if (state != State.READY)
            throw new IllegalStateException("GatewayModule != READY");
        WebSocketShard sh = shards.get(Asserter.requireNonNegative(shard));
        if (sh == null)
            throw new IllegalArgumentException(String.format("Shard #%d doesn't exist!", shard));
        Module.runAsync(() -> sh.getEventLimiter().submitEvent(data, ignored -> sh.getSocket().sendText(data)));
    }
    @Override
    public String getName() {
        return "Gateway Module";
    }
    @Override
    public String getVersion() {
        return "v0.1";
    }
    @Override
    public String getAuthor() {
        return "SamOphis";
    }
    @Override
    public String getUrl() {
        return "https://github.com/KunouMain/KunouBot";
    }
    @Override
    public IApplicationModule getApplicationModule() {
        return application;
    }

    @Override
    public IRestModule getRestModule() {
        return rest;
    }
    @Override
    public List<EventListener> getEventListeners() {
        return Collections.unmodifiableList(listeners);
    }
    @Override
    public IdentifyLimiter getIdentifyRateLimiter() {
        return limiter;
    }
    @Override
    public String getGatewayEndpoint() {
        return gatewayEndpoint;
    }
    @Override
    public int getGatewayShards() {
        return shardCount;
    }
    @Override
    public void addListener(@Nonnull EventListener listener) {
        listeners.add(Objects.requireNonNull(listener));
    }
}
