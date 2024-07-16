package rip.snake.antivpn.commons.config;

import io.antivpn.api.config.AntiVPNConfig;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VPNConfig {

    private final boolean debug;

    private String secret;
    private String detectMessage;
    private int cacheTimeout;

    public VPNConfig() {
        this(
                false,
                "secret",
                "§cVPN Detected!\n§cPlease disable your VPN and rejoin.\n§cIf you believe this is a mistake, please contact an administrator.",
                120
        );
    }

    public void write(AntiVPNConfig antiVPNConfig) {
        antiVPNConfig.setDebug(this.debug);
        antiVPNConfig.withApiKey(this.secret);
    }
}
