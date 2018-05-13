package samophis.kunou.main.entities.gateway.payloads;

import com.jsoniter.annotation.JsonIgnore;
import samophis.kunou.main.entities.gateway.WebSocketShard;

import javax.annotation.Nonnull;
import java.util.Objects;

@SuppressWarnings({"WeakerAccess", "unused"})
public class ResumePayload {
    public final int op;
    public final ResumePayloadData d;
    @JsonIgnore public final WebSocketShard shard;
    public ResumePayload(@Nonnull WebSocketShard shard) {
        this.shard = Objects.requireNonNull(shard);
        this.op = 6;
        this.d = new ResumePayloadData();
    }
    public class ResumePayloadData {
        public final String token, session_id;
        public final int seq;
        public ResumePayloadData() {
            this.token = shard.getGatewayModule().getApplicationModule().getToken();
            this.session_id = shard.getSessionId();
            this.seq = shard.getSeq();
        }
    }
}
