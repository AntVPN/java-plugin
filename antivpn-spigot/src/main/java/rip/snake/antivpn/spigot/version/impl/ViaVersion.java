package rip.snake.antivpn.spigot.version.impl;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.ViaAPI;
import org.bukkit.entity.Player;
import rip.snake.antivpn.spigot.version.VersionHelper;

public class ViaVersion implements VersionHelper {

    private final ViaAPI<?> api;

    public ViaVersion() {
        this.api = Via.getAPI();
    }

    @Override
    public int getProtocolVersion(Player player) {
        return api.getPlayerVersion(player.getUniqueId());
    }

}
