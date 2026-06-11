package rip.snake.antivpn.core.utils;

import lombok.experimental.UtilityClass;
import rip.snake.antivpn.core.Service;

@UtilityClass
public class TokenCommand {

    public static boolean processToken(String tokenId, Service service) {
        service.getVpnConfig().setSecret(tokenId);
        service.getAntiVPN().reload(service.getVpnConfig().toAntiVPNConfig());
        return service.saveConfig();
    }

}
