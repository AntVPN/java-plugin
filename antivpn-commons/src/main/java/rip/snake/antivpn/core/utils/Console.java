package rip.snake.antivpn.core.utils;

import rip.snake.antivpn.core.Service;

public class Console {

    public static void log(String message, Object... args) {
        Service.INSTANCE.getLogger().log(
                placeholder(message, args)
        );
    }

    public static void fine(String message, Object... args) {
        Service.INSTANCE.getLogger().fine(
                placeholder(message, "§a", args)
        );
    }

    public static void error(String message, Object... args) {
        Service.INSTANCE.getLogger().error(
                placeholder(message, "§c", args)
        );
    }

    public static void debug(String message, Object... args) {
        if (!Service.INSTANCE.getVpnConfig().isDebug()) return;
        Service.INSTANCE.getLogger().debug(
                placeholder(message, args)
        );
    }

    private static String placeholder(String message, Object... args) {
        return placeholder(message, "", args);
    }

    private static String placeholder(String message, String color, Object... args) {
        // Add [ServerAntiVPN] prefix
        message = "[ServerAntiVPN] " + color + message;

        // Replace %s with args
        return String.format(message, args);
    }

}
