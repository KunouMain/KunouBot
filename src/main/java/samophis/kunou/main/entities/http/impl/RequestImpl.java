package samophis.kunou.main.entities.http.impl;

import com.jsoniter.any.Any;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import samophis.kunou.main.entities.http.InnerRequest;
import samophis.kunou.main.entities.http.Request;
import samophis.kunou.main.entities.http.Route;
import samophis.kunou.main.exceptions.RequestException;
import samophis.kunou.main.modules.IApplicationModule;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class RequestImpl implements Request {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestImpl.class);
    private final Route route;
    private final IApplicationModule module;
    private final String data;
    public RequestImpl(@Nonnull Route route, @Nonnull IApplicationModule module, @Nullable String data) {
        this.route = Objects.requireNonNull(route);
        this.module = Objects.requireNonNull(module);
        this.data = data;
    }
    @Override
    public Route getRoute() {
        return route;
    }
    @Override
    public IApplicationModule getApplicationModule() {
        return module;
    }
    @Nullable
    @Override
    public String getData() {
        return data;
    }
    @Nullable
    @Override
    public Any get() {
        AtomicReference<Any> reference = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        queue(data -> {
            reference.set(data);
            latch.countDown();
        }, error -> {
            latch.countDown();
            throw new RequestException(error);
        });
        try {
            latch.await();
        } catch (InterruptedException exc) {
            LOGGER.error("Application interrupted Global Rate Limit Delay!", exc);
            Thread.currentThread().interrupt();
            throw new RequestException(exc);
        }
        return reference.get();
    }
    @Override
    public void queue(@Nullable Consumer<Any> onSuccess, @Nullable Consumer<Throwable> onFailure) {
        InnerRequest request = new InnerRequestImpl(route, this, onSuccess, onFailure);
        route.getRequester().submitRequest(request);
    }
    @Override
    public void queue(@Nullable Consumer<Any> onSuccess) {
        queue(onSuccess, null);
    }
    @Override
    public void queue() {
        queue(null);
    }
}
