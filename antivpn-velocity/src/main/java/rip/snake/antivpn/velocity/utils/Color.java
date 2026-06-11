package rip.snake.antivpn.velocity.utils;

import net.kyori.adventure.text.Component;
import rip.snake.antivpn.core.utils.ColorParser;

public class Color {

    public static Component colorize(String text) {
        if (text == null) return null;
        return ColorParser.parseToComponent(text);
    }

}
