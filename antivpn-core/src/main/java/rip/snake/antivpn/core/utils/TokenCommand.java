package rip.snake.antivpn.core.utils;

import lombok.experimental.UtilityClass;
import rip.snake.antivpn.core.Service;

import java.util.Set;

@UtilityClass
public class TokenCommand {

    private static final Set<String> RESERVED = Set.of("reload", "help");

    public static String validateToken(String tokenId) {
        if (RESERVED.contains(tokenId.toLowerCase())) {
            return "Invalid token. Usage: /antivpn <token>";
        }
        return null;
    }

    public static boolean processToken(String tokenId, Service service) {
        service.getVpnConfig().setSecret(tokenId);
        service.getAntiVPN().reload(service.getVpnConfig().toAntiVPNConfig());
        return service.saveConfig();
    }

}
