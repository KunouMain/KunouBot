package samophis.kunou.main.entities.gateway.payloads;

import samophis.kunou.main.entities.gateway.WebSocketShard;

import javax.annotation.Nonnull;
import java.util.Objects;

@SuppressWarnings({"WeakerAccess", "unused"})
public class HeartbeatPayload {
    public final int op;
    public final Integer d;
    public HeartbeatPayload(@Nonnull WebSocketShard shard) {
        int result = Objects.requireNonNull(shard).getSeq();
        this.d = result == 0 ? null : result;
        this.op = 1;
    }
}
