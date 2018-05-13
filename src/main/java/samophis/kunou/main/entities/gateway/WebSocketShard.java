package samophis.kunou.main.entities.gateway;

import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;
import com.jsoniter.output.JsonStream;
import com.neovisionaries.ws.client.*;
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import samophis.kunou.main.entities.gateway.events.Event;
import samophis.kunou.main.entities.gateway.events.EventBuilder;
import samophis.kunou.main.entities.gateway.payloads.HeartbeatPayload;
import samophis.kunou.main.entities.gateway.payloads.IdentifyPayload;
import samophis.kunou.main.entities.gateway.payloads.ResumePayload;
import samophis.kunou.main.exceptions.SocketException;
import samophis.kunou.main.modules.IGatewayModule;
import samophis.kunou.main.util.Asserter;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.net.ssl.SSLContext;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.Inflater;
import java.util.zip.InflaterOutputStream;

public class WebSocketShard extends WebSocketAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketShard.class);
    private final IGatewayModule module;
    private final AtomicInteger beats, acks;
    private final WebSocket socket;
    private final EventLimiter limiter;
    private final Object readLock;
    private final int shardNumber;
    private int seq;
    private String session_id;
    private volatile Inflater inflater;
    private volatile ByteArrayOutputStream buffer, decompress;
    private volatile ScheduledExecutorService keepAlive;
    private volatile boolean shouldResume, isActive;
    public WebSocketShard(@Nonnull IGatewayModule module, @Nonnull EventLimiter limiter, @Nonnegative int shardNumber) {
        LOGGER.info("created!");
        this.module = Objects.requireNonNull(module);
        this.limiter = Objects.requireNonNull(limiter);
        this.shardNumber = Asserter.requireNonNegative(shardNumber);
        this.readLock = new Object();
        this.beats = new AtomicInteger(0);
        this.acks = new AtomicInteger(0);
        this.inflater = new Inflater();
        this.keepAlive = Executors.newSingleThreadScheduledExecutor();
        this.isActive = true;
        this.decompress = new ByteArrayOutputStream(1024);
        try {
            this.socket = new WebSocketFactory()
                    .setSSLContext(SSLContext.getDefault())
                    .setConnectionTimeout(5000)
                    .createSocket(module.getGatewayEndpoint() + "/?v=6&encoding=json&compress=zlib-stream")
                    .addListener(this)
                    .connectAsynchronously();
        } catch (NoSuchAlgorithmException exc) {
            LOGGER.error("No suitable SSL Context found!", exc);
            throw new SocketException(exc);
        } catch (IOException exc) {
            LOGGER.error("Error when creating a WebSocketShard!", exc);
            throw new SocketException(exc);
        }
    }
    public IGatewayModule getGatewayModule() {
        return module;
    }
    public int getBeats() {
        return beats.get();
    }
    public int getAcks() {
        return acks.get();
    }
    public int getSeq() {
        return seq;
    }
    public int getShardNumber() {
        return shardNumber;
    }
    public String getSessionId() {
        return session_id;
    }
    public WebSocket getSocket() {
        return socket;
    }
    public EventLimiter getEventLimiter() {
        return limiter;
    }

    @Override
    public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) {
        int code = closedByServer ? serverCloseFrame.getCloseCode() : clientCloseFrame.getCloseCode();
        String reason = closedByServer ? serverCloseFrame.getCloseReason() : clientCloseFrame.getCloseReason();
        String name = closedByServer ? "SERVER" : "CLIENT";
        if (code != 1000)
            LOGGER.warn("{} - Close Code: {} - Reason: {}", name, code, reason);
    }

    @Override
    public void onConnectError(WebSocket websocket, WebSocketException exception) {
        LOGGER.error("Error when connecting a WebSocketShard!", exception);
        throw new SocketException(exception);
    }
    @Override
    public void onError(WebSocket websocket, WebSocketException cause) {
        LOGGER.error("Error during the normal operation of a WebSocketShard!", cause);
        throw new SocketException(cause);
    }
    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) {
        try {
            LOGGER.info("Connected Shard #{}!", shardNumber);
            resetKeepAliveScheduler();
            inflater = new Inflater();
            decompress = new ByteArrayOutputStream(1024);
            if (shouldResume)
                sendResume();
            else
                sendIdentify();
        } catch (Throwable thr) {
            thr.printStackTrace();
        }
    }
    @Override
    public void onTextMessage(WebSocket websocket, String text) throws IOException {
        Any data = JsonIterator.deserialize(text);
        Any d = data.get("d");
        int op = data.get("op").toInt();
        switch (op) {
            case 0:
                seq = data.get("s").toInt();
                String name = data.get("t").toString();
                if (name.equals("READY"))
                    session_id = d.get("session_id").toString();
                Event event = new EventBuilder(module)
                        .setEventOrder(seq)
                        .setShardNumber(shardNumber)
                        .setData(d)
                        .setName(name)
                        .build();
                module.getEventListeners().forEach(listener -> listener.onEvent(event));
                break;
            case 1:
                sendHeartbeat();
                break;
            case 7:
                shouldResume = true;
                socket.disconnect(1000, "Gateway issued a Reconnect Payload!").recreate().connectAsynchronously();
                break;
            case 9:
                shouldResume = d.toBoolean();
                if (!shouldResume) {
                    beats.set(0);
                    acks.set(0);
                    seq = 0;
                    session_id = null;
                }
                socket.disconnect(1006, "Session Invalidated!").recreate().connectAsynchronously();
                break;
            case 10:
                System.out.println(text);
                initHeartbeat(d.get("heartbeat_interval").toLong());
                break;
            case 11:
                acks.incrementAndGet();
                break;
            default:
                LOGGER.warn("Unhandled OP Code: {} with Data: {}", op, text);
        }
    }
    @Override
    public void onBinaryMessage(WebSocket websocket, byte[] binary) {
        /* -- logic comes partly from JDA, adapted to work simpler, faster and in context -- */
        try {
            synchronized (readLock) {
                int length = binary.length;
                int result = binary[length - 1] & 0xFF
                        | ((binary[length - 2] & 0xFF) << 8)
                        | ((binary[length - 3] & 0xFF) << 16)
                        | ((binary[length - 4] & 0xFF) << 24);
                if (buffer == null)
                    buffer = new ByteArrayOutputStream(length * 2);
                buffer.write(binary);
                if (length >= 4 && result == 0x0000FFFF) {
                    try (InflaterOutputStream stream = new InflaterOutputStream(decompress, inflater)) {
                        if (buffer != null)
                            buffer.writeTo(stream);
                        else
                            stream.write(binary);
                    } catch (IOException exc) {
                        LOGGER.error("Malformed Data!", exc);
                        decompress.reset();
                        throw new SocketException("Malformed Data Buffer!");
                    } finally {
                        buffer = null;
                    }
                    String text = decompress.toString("UTF-8");
                    decompress.reset();
                    onTextMessage(websocket, text);
                }
            }
        } catch (Throwable thr) {
            thr.printStackTrace();
            //LOGGER.error("Shard #{}: Error during compressed binary message handling! {}", shardNumber, thr.getMessage());
            throw new SocketException(thr);
        }
    }
    @Override
    public void onUnexpectedError(WebSocket websocket, WebSocketException cause) {
        onError(websocket, cause);
    }
    private void resetKeepAliveScheduler() {
        if (keepAlive != null) {
            if (!keepAlive.isShutdown())
                keepAlive.shutdown();
        }
        keepAlive = Executors.newSingleThreadScheduledExecutor();
    }
    private void initHeartbeat(long interval) {
        System.out.print(interval);
        resetKeepAliveScheduler();
        keepAlive.scheduleAtFixedRate(() -> {
            if (isActive) {
                keepAlive.shutdown();
                throw new IllegalStateException("WebSocketShard no longer active!");
            }
            if (beats.get() - acks.get() > 1) {
                try {
                    socket.disconnect(1006, "Lack of Heartbeat ACKs (failed connection!)").recreate().connectAsynchronously();
                } catch (IOException exc) {
                    LOGGER.error("Error when attempting to recreate Shard #{}! {}", shardNumber, exc.getMessage());
                }
                keepAlive.shutdown();
                throw new SocketException("Lack of Heartbeat ACKs received from Discord!");
            }
            sendHeartbeat();
        }, 0, interval, TimeUnit.MILLISECONDS);
    }
    private void sendHeartbeat() {
        limiter.submitEvent(JsonStream.serialize(new HeartbeatPayload(this)), data -> {
           beats.incrementAndGet();
           socket.sendText(data);
        });
    }
    public void sendIdentify() {
        socket.sendText(JsonStream.serialize(new IdentifyPayload(this)));
    }
    private void sendResume() {
        limiter.submitEvent(JsonStream.serialize(new ResumePayload(this)), socket::sendText);
    }
    public void shutdown() {
        if (!isActive)
            throw new IllegalStateException("WebSocketShard already shut-down!");
        isActive = false;
        socket.disconnect(1000, "Disconnected normally and successfully!");
        if (keepAlive != null)
            keepAlive.shutdown();
        limiter.shutdown();
        if (buffer != null)
            buffer.reset();
        decompress.reset();
        beats.set(0);
        acks.set(0);
        seq = 0;
        session_id = null;
    }
}

