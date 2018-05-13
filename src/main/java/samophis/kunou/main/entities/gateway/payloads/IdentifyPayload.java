package samophis.kunou.main.entities.gateway.payloads;

import com.jsoniter.annotation.JsonIgnore;
import com.jsoniter.annotation.JsonUnwrapper;
import com.jsoniter.output.JsonStream;
import samophis.kunou.main.entities.gateway.WebSocketShard;
import samophis.kunou.main.modules.IGatewayModule;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Objects;

@SuppressWarnings({"WeakerAccess", "unused"})
public class IdentifyPayload {
    public final int op;
    public final IdentifyPayloadData d;
    @JsonIgnore public final WebSocketShard shard;
    public IdentifyPayload(@Nonnull WebSocketShard shard) {
        this.shard = Objects.requireNonNull(shard);
        this.op = 2;
        this.d = new IdentifyPayloadData();
    }
    public class IdentifyPayloadData {
        public final String token;
        public final int[] shard;
        public final ConnectionProperties properties;
        public IdentifyPayloadData() {
            WebSocketShard _sh = IdentifyPayload.this.shard;
            IGatewayModule mod = _sh.getGatewayModule();
            this.token = mod.getApplicationModule().getToken();
            this.shard = new int[] {_sh.getShardNumber(), mod.getGatewayShards()};
            this.properties = new ConnectionProperties();
        }
    }
    public class ConnectionProperties {
        @JsonUnwrapper
        public void unwrapProperties(JsonStream stream) throws IOException {
            stream.writeObjectField("$os");
            stream.writeVal(System.getProperty("os.name"));
            stream.writeMore();
            stream.writeObjectField("$browser");
            stream.writeVal("Kunou");
            stream.writeMore();
            stream.writeObjectField("$device");
            stream.writeVal("Kunou");
        }
    }
}
