package samophis.kunou.main.entities.http.impl;

import samophis.kunou.main.entities.http.Request;
import samophis.kunou.main.entities.http.RequestType;
import samophis.kunou.main.entities.http.Requester;
import samophis.kunou.main.entities.http.Route;
import samophis.kunou.main.modules.IApplicationModule;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class RouteImpl implements Route {
    private final String endpoint;
    private final RequestType type;
    private final Requester requester;
    public RouteImpl(@Nonnull String endpoint, @Nonnull RequestType type, @Nullable Requester requester) {
        this.endpoint = Objects.requireNonNull(endpoint);
        this.type = Objects.requireNonNull(type);
        this.requester = requester == null ? new RequesterImpl() : requester;
    }
    @Override
    public RequestType getType() {
        return type;
    }
    @Override
    public String getUrl() {
        return endpoint;
    }
    @Override
    public Requester getRequester() {
        return requester;
    }
    @Override
    public Request asRequest(@Nonnull IApplicationModule module, @Nullable String data) {
        return new RequestImpl(this, module, data);
    }
    @Override
    public Route format(@Nonnull Object... params) {
        return new RouteImpl(String.format(endpoint, params), type, requester);
    }
}
