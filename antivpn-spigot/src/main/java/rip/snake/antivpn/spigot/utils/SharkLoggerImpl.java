package rip.snake.antivpn.spigot.utils;

import io.antivpn.api.logging.VPNLogger;
import org.bukkit.Bukkit;
import rip.snake.antivpn.core.utils.LoggerFactory;

public class SharkLoggerImpl {

    public static VPNLogger create() {
        return LoggerFactory.fromConsumer(
                msg -> Bukkit.getConsoleSender().sendMessage(msg),
                msg -> Bukkit.getConsoleSender().sendMessage(msg)
        );
    }

}
