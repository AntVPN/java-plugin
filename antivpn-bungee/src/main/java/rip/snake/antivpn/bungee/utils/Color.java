package rip.snake.antivpn.bungee.utils;

import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import rip.snake.antivpn.core.utils.ColorParser;

public class Color {

    private static final BungeeComponentSerializer BUNGEE_SERIALIZER = BungeeComponentSerializer.get();

    public static BaseComponent[] colorize(String message) {
        if (message == null) return null;
        return BUNGEE_SERIALIZER.serialize(ColorParser.parseToComponent(message));
    }

}
