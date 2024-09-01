package rip.snake.antivpn.spigot.utils;

import net.md_5.bungee.api.ChatColor;

public class Color {

    public static String colorize(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

}
