package rip.snake.antivpn.spigot.listeners;

import io.antivpn.api.data.socket.request.impl.CheckRequest;
import io.antivpn.api.data.socket.response.impl.CheckResponse;
import io.antivpn.api.utils.Event;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import rip.snake.antivpn.commons.Service;
import rip.snake.antivpn.commons.utils.StringUtils;
import rip.snake.antivpn.spigot.ServerAntiVPN;

import java.util.concurrent.CompletableFuture;

import static rip.snake.antivpn.spigot.utils.Color.colorize;

public class PlayerListener implements Listener {

    private final Service service;
    private final ServerAntiVPN plugin;

    public PlayerListener(ServerAntiVPN plugin) {
        this.plugin = plugin;
        this.service = plugin.getService();
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(AsyncPlayerPreLoginEvent event) {
        if (event.getResult() != PlayerPreLoginEvent.Result.ALLOWED) {
            return;
        }
        String address = event.getAddress().getHostAddress();

        try {
            CompletableFuture<CheckResponse> response = service.getAntiVPN().getSocketManager().getSocketDataHandler()
                    .verify(new CheckRequest(StringUtils.cleanAddress(address), event.getName()));
            if (response == null) {
                service.getLogger().error(
                        "Failed to verify " + event.getName() + " (" + address + ")! Backend is not connected"
                );
                return;
            }
            CheckResponse result = response.get();
            if (result == null || result.isValid()) {
                return;
            }

            event.setKickMessage(colorize(service.getVpnConfig().getDetectMessage()));
            event.setResult(PlayerPreLoginEvent.Result.KICK_OTHER);
        } catch (Exception e) {
            service.getLogger().error("Failed to verify address " + address + "! " + e.getMessage());
            e.printStackTrace();
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void playerJoin(PlayerJoinEvent event) {
        this.handlePlayer(event.getPlayer(), Event.PLAYER_JOIN);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void playerQuit(PlayerQuitEvent event) {
        this.handlePlayer(event.getPlayer(), Event.PLAYER_QUIT);
    }

    private void handlePlayer(Player player, Event event) {
        boolean isOnlineMode = Bukkit.getOnlineMode();
        String playerName = player.getName();
        String userId = player.getUniqueId().toString();
        String address = player.getAddress().getAddress().getHostAddress();

        // get protocol version
        String version = String.valueOf(plugin.getVersionHelper().getProtocolVersion(player));

        // Send the data to the backend server
        service.getAntiVPN().getSocketManager().getSocketDataHandler()
                .sendUserData(playerName, userId, version, address, null, event, isOnlineMode);
    }

}
