package samophis.kunou.main.modules;

import samophis.kunou.core.modules.Module;
import samophis.kunou.main.entities.http.Request;

/**
 * Module declaration which covers the Discord REST API so that users can create their own, custom REST API modules.
 * <br><p>It's strongly recommended to maximize performance here while making sure you monitor/manage state and active code.
 * Upon shutdown, your module implementation should gracefully kill all active code and reset to the initial starting conditions, for later usage.</p>
 *
 * @author SamOphis
 * @since 0.1
 */

public interface IRestModule extends Module {
    /**
     * Fetches the {@link IApplicationModule IApplicationModule} used to authorize requests to the Discord API.
     * @return The {@link IApplicationModule IApplicationModule} this REST Module depends on.
     */
    IApplicationModule getApplicationModule();

    /**
     *
     * @return
     */
    Request getGatewayRequest();
}