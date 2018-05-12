package samophis.kunou.main.modules;

import samophis.kunou.core.modules.Module;

/**
 * Represents an application/bot module with information about the User Token, possible Configuration Options, etc.
 * <br><p>This doesn't actually have to do much work on its own. The {@link IGatewayModule IGatewayModule} and {@link IRestModule IRestModule} depend on it
 * to retrieve starting information for authorization, etc.
 *
 * <br>However, this is traditionally the "base" module. It should start other modules that need to run and generally represent a typical "application", but modular.</p>
 *
 * @author SamOphis
 * @since 0.1
 */

public interface IApplicationModule extends Module {
    /**
     * Fetches the token of the Discord Bot used for all communication with the Discord API.
     * @return The token of the Discord Bot/Application (not to be confused with OAuth2 Applications).
     */
    String getToken();
}