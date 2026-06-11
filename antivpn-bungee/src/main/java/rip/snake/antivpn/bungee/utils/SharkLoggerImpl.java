package rip.snake.antivpn.bungee.utils;

import io.antivpn.api.logging.VPNLogger;
import net.md_5.bungee.api.ProxyServer;
import rip.snake.antivpn.core.utils.LoggerFactory;

public class SharkLoggerImpl {

    public static VPNLogger create() {
        return LoggerFactory.fromJUL(ProxyServer.getInstance().getLogger());
    }

}
