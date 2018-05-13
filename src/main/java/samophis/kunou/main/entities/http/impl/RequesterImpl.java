package samophis.kunou.main.entities.http.impl;

import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.*;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import samophis.kunou.main.entities.http.*;
import samophis.kunou.main.exceptions.RequestException;
import samophis.kunou.main.modules.IApplicationModule;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class RequesterImpl implements Requester {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequesterImpl.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.RFC_1123_DATE_TIME;
    private static final CloseableHttpAsyncClient CLIENT = HttpAsyncClients.createDefault();
    private static long offset = Long.MAX_VALUE;
    private final Queue<InnerRequest> requests;
    private final AtomicInteger remaining;
    private volatile ScheduledExecutorService pool;
    private volatile int limit;
    private volatile long lastReset, currentReset;
    private volatile boolean isActive, isWaiting;
    public RequesterImpl() {
        System.out.println("requester created!");
        this.pool = Executors.newScheduledThreadPool(2);
        this.requests = new ConcurrentLinkedQueue<>();
        this.remaining = new AtomicInteger(1);
        this.limit = 1;
        this.lastReset = 0;
        this.currentReset = 0;
        this.isWaiting = false;
        startup();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (!pool.isShutdown())
                pool.shutdownNow();
        }));
    }
    @Override
    public int getRemainingRequests() {
        return remaining.get();
    }
    @Override
    public int getRequestLimit() {
        return limit;
    }
    @Override
    public long getLastResetTimeMs() {
        return lastReset;
    }
    @Override
    public long getFutureResetTimeMs() {
        return currentReset;
    }
    @Override
    public void submitRequest(@Nonnull InnerRequest request) {
        requests.add(Objects.requireNonNull(request));
    }
    @Override
    public void startup() {
        if (!pool.isShutdown() && isActive)
            throw new IllegalStateException("This Requester instance is already alive!");
        isActive = true;
        pool = Executors.newScheduledThreadPool(2);
        pool.submit(() -> {
            do {
                InnerRequest request = requests.poll();
                if (request == null || remaining.get() == 0 || Globals.isLocked())
                    continue;
                remaining.decrementAndGet();
                processRequest(request);
            } while (isActive);
        });
    }
    @Override
    public void shutdown() {
        if (pool.isShutdown())
            throw new IllegalStateException("This Requester instance is already shutdown!");
        isActive = false;
        offset = Long.MAX_VALUE;
        requests.clear();
        pool.shutdown();
    }
    @SuppressWarnings("ConstantConditions") /* -- getting rid of nullable json data stuff -- */
    private void processRequest(InnerRequest request) {
        Route route = request.getRoute();
        Request req = request.getRequest();
        String url = route.getUrl();
        HttpRequestBase base;
        switch (route.getType()) {
            case GET:
                base = new HttpGet(url);
                break;
            case PUT:
                HttpPut put = new HttpPut(url);
                put.setEntity(new StringEntity(req.getData(), ContentType.APPLICATION_JSON));
                base = put;
                break;
            case PATCH:
                HttpPatch patch = new HttpPatch(url);
                patch.setEntity(new StringEntity(req.getData(), ContentType.APPLICATION_JSON));
                base = patch;
                break;
            case POST:
                HttpPost post = new HttpPost(url);
                post.setEntity(new StringEntity(req.getData(), ContentType.APPLICATION_JSON));
                base = post;
                break;
            case DELETE:
                base = new HttpDelete(url);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported request type? This should be impossible!");
        }
        IApplicationModule module = req.getApplicationModule();
        base.setHeader("Authorization", "Bot " + module.getToken());
        base.setHeader("User-Agent", String.format("DiscordBot (%s, %s)", module.getVersion(), module.getUrl()));
        FutureCallback<HttpResponse> callback = new FutureCallback<HttpResponse>() {
            @Override
            public void completed(HttpResponse result) {
                if (offset == Long.MAX_VALUE)
                    offset = Instant.from(FORMATTER.parse(result.getFirstHeader("Date").getValue())).toEpochMilli() - System.currentTimeMillis();
                Header rem = result.getFirstHeader("X-RateLimit-Remaining");
                Header res = result.getFirstHeader("X-RateLimit-Reset");
                Header lim = result.getFirstHeader("X-RateLimit-Limit");
                if (rem != null)
                    remaining.set(Integer.parseUnsignedInt(rem.getValue()));
                if (lim != null)
                    limit = Integer.parseUnsignedInt(lim.getValue());
                if (res != null) {
                    if (currentReset != 0)
                        lastReset = currentReset;
                    currentReset = Integer.parseUnsignedInt(res.getValue());
                    if (!isWaiting) {
                        isWaiting = true;
                        pool.schedule(() -> {
                            isWaiting = false;
                            remaining.set(limit);
                        }, ((currentReset * 1000) + offset) - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
                    }
                }
                String text;
                try {
                    text = EntityUtils.toString(result.getEntity());
                } catch (IOException exc) {
                    EntityUtils.consumeQuietly(result.getEntity());
                    request.getFailureCallback().accept(new RequestException(exc));
                    return;
                }
                int code = result.getStatusLine().getStatusCode();
                if (code == 429) {
                    Any data = JsonIterator.deserialize(text);
                    Any _ret = data.get("retry_after");
                    boolean global = data.get("global") != null;
                    int retry = _ret == null
                            ? Integer.parseUnsignedInt(result.getFirstHeader("Retry-After").getValue())
                            : _ret.toInt();
                    if (global)
                        Globals.lockGlobal(retry);
                    pool.schedule(() -> CLIENT.execute(base, this), retry, TimeUnit.MILLISECONDS);
                    EntityUtils.consumeQuietly(result.getEntity());
                    return;
                }
                EntityUtils.consumeQuietly(result.getEntity());
                if (code > 199 && code < 205) {
                    Any data = code == 204 ? null : JsonIterator.deserialize(text);
                    request.getSuccessCallback().accept(data);
                }
                else {
                    String format = String.format("HTTP Code %d: %s", code, result.getStatusLine().getReasonPhrase());
                    request.getFailureCallback().accept(new RequestException(format));
                }
            }
            @Override
            public void failed(Exception ex) {
                request.getFailureCallback().accept(new RequestException(ex));
            }
            @Override
            public void cancelled() {
                request.getFailureCallback().accept(new RequestException("HTTP Request cancelled!"));
            }
        };
        CLIENT.execute(base, callback);
    }
    static {
        CLIENT.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (CLIENT.isRunning()) {
                try {
                    CLIENT.close();
                } catch (IOException exc) {
                    LOGGER.error("Error when closing HTTP Client!", exc);
                }
            }
        }));
    }
}
