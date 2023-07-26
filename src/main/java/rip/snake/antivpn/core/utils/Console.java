package rip.snake.antivpn.core.utils;

import rip.snake.antivpn.core.Service;

public class Console {

    public static void log(String message, Object... args) {
        Service.INSTANCE.getLogger().info(
                String.format(message, args)
        );
    }

    public static void error(String message, Object... args) {
        Service.INSTANCE.getLogger().warn(
                String.format(message, args)
        );
    }

    public static void debug(String message, Object... args) {
        if (!Service.INSTANCE.getVpnConfig().isDebug()) return;
        Service.INSTANCE.getLogger().warn(
                String.format(message, args)
        );
    }

}
