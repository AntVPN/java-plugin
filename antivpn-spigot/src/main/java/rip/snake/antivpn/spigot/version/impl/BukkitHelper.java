package rip.snake.antivpn.spigot.version.impl;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import rip.snake.antivpn.spigot.utils.ProtocolVersion;
import rip.snake.antivpn.spigot.version.VersionHelper;

public class BukkitHelper implements VersionHelper {
    @Override
    public int getProtocolVersion(Player player) {
        ProtocolVersion protocolVersion = ProtocolVersion.getProtocolVersion(Bukkit.getVersion().split("-")[1].split("\\.")[1]);

        if (protocolVersion == null || protocolVersion.isUnknown() || protocolVersion.isLegacy()) {
            return -1;
        }

        return protocolVersion.getProtocol();
    }
}
