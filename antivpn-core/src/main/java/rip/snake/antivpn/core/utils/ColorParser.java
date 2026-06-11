package rip.snake.antivpn.core.utils;

import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

@UtilityClass
public class ColorParser {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    public static Component parseToComponent(String text) {
        if (text == null) return null;

        text = text.replaceAll("(?i)[&§]#([0-9a-f]{6})", "<#$1>");
        text = text.replaceAll("(?i)[&§]x[&§]([0-9a-f])[&§]([0-9a-f])[&§]([0-9a-f])[&§]([0-9a-f])[&§]([0-9a-f])[&§]([0-9a-f])", "<#$1$2$3$4$5$6>");

        StringBuilder sb = new StringBuilder();
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if ((chars[i] == '&' || chars[i] == '§') && i + 1 < chars.length) {
                char c = Character.toLowerCase(chars[i + 1]);
                String replacement = null;
                switch (c) {
                    case '0': replacement = "<black>"; break;
                    case '1': replacement = "<dark_blue>"; break;
                    case '2': replacement = "<dark_green>"; break;
                    case '3': replacement = "<dark_aqua>"; break;
                    case '4': replacement = "<dark_red>"; break;
                    case '5': replacement = "<dark_purple>"; break;
                    case '6': replacement = "<gold>"; break;
                    case '7': replacement = "<gray>"; break;
                    case '8': replacement = "<dark_gray>"; break;
                    case '9': replacement = "<blue>"; break;
                    case 'a': replacement = "<green>"; break;
                    case 'b': replacement = "<aqua>"; break;
                    case 'c': replacement = "<red>"; break;
                    case 'd': replacement = "<light_purple>"; break;
                    case 'e': replacement = "<yellow>"; break;
                    case 'f': replacement = "<white>"; break;
                    case 'k': replacement = "<obfuscated>"; break;
                    case 'l': replacement = "<bold>"; break;
                    case 'm': replacement = "<strikethrough>"; break;
                    case 'n': replacement = "<underlined>"; break;
                    case 'o': replacement = "<italic>"; break;
                    case 'r': replacement = "<reset>"; break;
                }
                if (replacement != null) {
                    sb.append(replacement);
                    i++;
                    continue;
                }
            }
            sb.append(chars[i]);
        }
        return MINI_MESSAGE.deserialize(sb.toString());
    }

}
