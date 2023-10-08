package rip.snake.antivpn.core.data;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SettingsResponse {

    private final boolean enabled;
    private final String kickMessage;

    public SettingsResponse() {
        this(false, "§cVPN Detected!\n§cPlease disable your VPN and rejoin.\n§cIf you believe this is a mistake, please contact an administrator.");
    }

}
