package samophis.kunou.main.modules.impl;

import samophis.kunou.core.modules.AbstractModuleBase;
import samophis.kunou.core.modules.ModuleLoader;
import samophis.kunou.core.modules.State;
import samophis.kunou.main.entities.http.Request;
import samophis.kunou.main.entities.http.RequestType;
import samophis.kunou.main.entities.http.Route;
import samophis.kunou.main.entities.http.impl.RouteImpl;
import samophis.kunou.main.modules.IApplicationModule;
import samophis.kunou.main.modules.IRestModule;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class RestModule extends AbstractModuleBase implements IRestModule {
    public static RestModule getInstance(@Nonnull ModuleLoader loader, @Nonnull IApplicationModule application) {
        return new RestModule(loader, application);
    }
    private static final Route GATEWAY_ROUTE = new RouteImpl("https://discordapp.com/api/v6/gateway/bot", RequestType.GET, null);
    private final IApplicationModule application;
    private final Request request;
    private volatile boolean afterDeath;
    private RestModule(@Nonnull ModuleLoader loader, @Nonnull IApplicationModule application) {
        super(loader);
        this.application = Objects.requireNonNull(application);
        this.request = GATEWAY_ROUTE.asRequest(application, null);
    }

    @Override
    public void onStart(@Nullable String... args) {
        if (afterDeath)
            request.getRoute().getRequester().startup();
        afterDeath = false;
    }

    @Override
    public void onDeath() {
        state = State.SHUTTING_DOWN;
        request.getRoute().getRequester().shutdown();
        state = State.DEAD;
        afterDeath = true;
    }
    @Override
    public IApplicationModule getApplicationModule() {
        return application;
    }
    @Override
    public Request getGatewayRequest() {
        return request;
    }
    @Override
    public String getName() {
        return "Kunou REST Module";
    }
    @Override
    public String getAuthor() {
        return "SamOphis";
    }
    @Override
    public String getVersion() {
        return "v0.1";
    }
    @Override
    public String getUrl() {
        return "https://github.com/KunouMain/KunouBot";
    }
}
