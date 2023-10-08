package rip.snake.antivpn.core.config;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VPNConfig {

    private final boolean debug;

    private String secret;
    private String detectMessage;

    public VPNConfig() {
        this(
                "secret",
                "§cVPN Detected!\n§cPlease disable your VPN and rejoin.\n§cIf you believe this is a mistake, please contact an administrator.",
                false
        );
    }

    public VPNConfig(String secret, String detectMessage, boolean debug) {
        this.secret = secret;
        this.detectMessage = detectMessage;
        this.debug = debug;
    }
}
