package rip.snake.antivpn.bungee.utils;

import net.md_5.bungee.api.ProxyServer;
import rip.snake.antivpn.core.utils.SharkLogger;

public class SharkLoggerImpl implements SharkLogger {
    @Override
    public void log(String message, Object... args) {
        ProxyServer.getInstance().getLogger().info(String.format(message, args));
    }

    @Override
    public void fine(String message, Object... args) {
        ProxyServer.getInstance().getLogger().info(String.format(message, args));
    }

    @Override
    public void error(String message, Object... args) {
        ProxyServer.getInstance().getLogger().severe(String.format(message, args));
    }

    @Override
    public void debug(String message, Object... args) {
        ProxyServer.getInstance().getLogger().info(String.format(message, args));
    }
}
