package rip.snake.antivpn.spigot.listeners;

import io.antivpn.api.model.response.CheckResponse;
import io.antivpn.api.util.Event;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import rip.snake.antivpn.core.PlayerData;
import rip.snake.antivpn.core.Service;
import rip.snake.antivpn.core.VPNCheckHandler;
import rip.snake.antivpn.spigot.ServerAntiVPN;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static rip.snake.antivpn.spigot.utils.Color.colorize;

public class PlayerListener implements Listener {

    private final Service service;
    private final ServerAntiVPN plugin;
    private final VPNCheckHandler handler;

    public PlayerListener(ServerAntiVPN plugin) {
        this.plugin = plugin;
        this.service = plugin.getService();
        this.handler = new VPNCheckHandler(service);
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(AsyncPlayerPreLoginEvent event) {
        if (event.getResult() != PlayerPreLoginEvent.Result.ALLOWED) return;
        String address = event.getAddress().getHostAddress();
        String userId = event.getUniqueId().toString();
        String username = event.getName();

        try {
            CompletableFuture<CheckResponse> response = handler.verify(address, event.getUniqueId(), username);
            if (response == null) {
                this.service.getLogger().error("Failed to verify " + username + " (" + address + ")! Backend is not connected");
                return;
            }
            CheckResponse result = response.get();
            if (result == null) return;

            if (result.isAttack()) {
                event.setKickMessage(colorize(handler.getShieldKick()));
                event.setResult(PlayerPreLoginEvent.Result.KICK_OTHER);
            } else if (result.isValid()) {
                handler.storeSession(event.getUniqueId(), result.getSessionId());
            } else {
                event.setKickMessage(colorize(handler.getDetectKick()));
                event.setResult(PlayerPreLoginEvent.Result.KICK_OTHER);
            }
        } catch (Exception e) {
            this.service.getLogger().error("Failed to verify address " + address + "! " + e.getMessage());
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        event.getPlayer().setMetadata("avpn-hostname", new FixedMetadataValue(this.plugin, event.getHostname()));
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void playerJoin(PlayerJoinEvent event) {
        handlePlayer(event.getPlayer(), Event.PLAYER_JOIN);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void playerQuit(PlayerQuitEvent event) {
        handlePlayer(event.getPlayer(), Event.PLAYER_QUIT);
    }

    private void handlePlayer(Player player, Event event) {
        List<MetadataValue> metadatas = player.getMetadata("avpn-hostname");
        String hostname = metadatas == null || metadatas.isEmpty() ? null : metadatas.get(0).asString();

        handler.sendUserData(new PlayerData(
                event == Event.PLAYER_QUIT ? handler.removeSession(player.getUniqueId()) : handler.getSession(player.getUniqueId()),
                player.getName(),
                player.getUniqueId().toString(),
                String.valueOf(plugin.getVersionHelper().getProtocolVersion(player)),
                player.getAddress().getAddress().getHostAddress(),
                null,
                hostname,
                Bukkit.getOnlineMode(),
                event
        ));
    }

}
