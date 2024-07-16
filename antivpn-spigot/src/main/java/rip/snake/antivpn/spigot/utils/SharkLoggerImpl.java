package rip.snake.antivpn.spigot.utils;

import io.antivpn.api.logger.VPNLogger;
import org.bukkit.Bukkit;

public class SharkLoggerImpl implements VPNLogger {

    @Override
    public void log(String message, Object... args) {
        Bukkit.getConsoleSender().sendMessage(String.format(message, args));
    }

    @Override
    public void fine(String message, Object... args) {
        Bukkit.getConsoleSender().sendMessage(String.format(message, args));
    }

    @Override
    public void error(String message, Object... args) {
        Bukkit.getConsoleSender().sendMessage(String.format(message, args));
    }

    @Override
    public void debug(String message, Object... args) {
        Bukkit.getConsoleSender().sendMessage(String.format(message, args));
    }

}
