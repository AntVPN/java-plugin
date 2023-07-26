package rip.snake.antivpn.core.config;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VPNConfig {

    private final String secret;
    private final String detectMessage;
    private final boolean debug;

    public VPNConfig() {
        this(
                "secret",
                "§cVPN Detected!\n§cPlease disable your VPN and rejoin.\n§cIf you believe this is a mistake, please contact an administrator.",
                false
        );
    }

}
