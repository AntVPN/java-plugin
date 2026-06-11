package rip.snake.antivpn.spigot.utils;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import rip.snake.antivpn.core.utils.ColorParser;

public class Color {

    private static final LegacyComponentSerializer SECTION_SERIALIZER = LegacyComponentSerializer.builder()
            .character('§')
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    public static String colorize(String text) {
        if (text == null) return null;
        return SECTION_SERIALIZER.serialize(ColorParser.parseToComponent(text));
    }

}
