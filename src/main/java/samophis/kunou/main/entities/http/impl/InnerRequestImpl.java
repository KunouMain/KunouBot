package samophis.kunou.main.entities.http.impl;

import com.jsoniter.any.Any;
import samophis.kunou.main.entities.http.InnerRequest;
import samophis.kunou.main.entities.http.Request;
import samophis.kunou.main.entities.http.Route;

import java.util.Objects;
import java.util.function.Consumer;

public class InnerRequestImpl implements InnerRequest {
    private final Route route;
    private final Request request;
    private final Consumer<Any> success;
    private final Consumer<Throwable> error;
    public InnerRequestImpl(Route route, Request request, Consumer<Any> success, Consumer<Throwable> error) {
        this.route = Objects.requireNonNull(route);
        this.request = Objects.requireNonNull(request);
        this.success = success == null ? InnerRequest.DEFAULT_SUCCESS : success;
        this.error = error == null ? InnerRequest.DEFAULT_ERROR : error;
    }
    @Override
    public Route getRoute() {
        return route;
    }
    @Override
    public Request getRequest() {
        return request;
    }
    @Override
    public Consumer<Any> getSuccessCallback() {
        return success;
    }
    @Override
    public Consumer<Throwable> getFailureCallback() {
        return error;
    }
}
