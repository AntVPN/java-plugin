package rip.snake.antivpn.spigot.utils;

import org.bukkit.Bukkit;
import rip.snake.antivpn.core.utils.SharkLogger;

public class SharkLoggerImpl implements SharkLogger {

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
