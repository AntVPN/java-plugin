package rip.snake.antivpn.spigot.version.impl;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import rip.snake.antivpn.spigot.utils.ProtocolVersion;
import rip.snake.antivpn.spigot.version.VersionHelper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BukkitHelper implements VersionHelper {

    // (MC: 1.20.5)
    private static final String pattern = "\\(\\w+: (\\d+(\\.\\d+)*)\\)";
    private static final Pattern VERSION_RGX = Pattern.compile(pattern);


    @Override
    public int getProtocolVersion(Player player) {
        Matcher matcher = VERSION_RGX.matcher(Bukkit.getVersion());

        if (!matcher.find()) {
            return -1;
        }

        String version = matcher.group(1);
        ProtocolVersion protocolVersion = ProtocolVersion.getProtocolVersion(version);

        if (protocolVersion == null || protocolVersion.isUnknown() || protocolVersion.isLegacy()) {
            return -1;
        }

        return protocolVersion.getProtocol();
    }
}
