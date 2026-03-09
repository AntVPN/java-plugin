package rip.snake.antivpn.commons.config;

import io.antivpn.api.config.AntiVPNConfig;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.annotation.Nullable;
import java.util.logging.Level;

@Data
@AllArgsConstructor
public class VPNConfig {
    private final boolean debug;

    private String secret;
    private String detectMessage;
    private String shieldMessage;
    private @Nullable String endpoint;
    private int cacheTimeout;
    private String level;

    public VPNConfig() {
        this(
                false,
                "secret",
                "§cVPN Detected!\n§cPlease disable your VPN and rejoin.\n§cIf you believe this is a mistake, please contact an administrator.",
                "§cShield is enabled!\n§cPlease wait a couple seconds before joining.\n§cIf you believe this is a mistake, please contact an administrator.",
                null,
                120,
                Level.INFO.getName()
        );
    }

    public void write(AntiVPNConfig antiVPNConfig) {
        antiVPNConfig.setDebug(this.debug);
        antiVPNConfig.withApiKey(this.secret);
        antiVPNConfig.setLevel(Level.parse(this.level));
        if (this.endpoint != null) {
            antiVPNConfig.withEndpoint(this.endpoint);
        }
    }
}
