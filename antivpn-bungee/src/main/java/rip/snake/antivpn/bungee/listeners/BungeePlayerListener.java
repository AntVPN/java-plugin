package rip.snake.antivpn.bungee.listeners;

import io.antivpn.api.data.socket.request.impl.CheckRequest;
import io.antivpn.api.data.socket.response.impl.CheckResponse;
import io.antivpn.api.utils.Event;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import rip.snake.antivpn.bungee.ServerAntiVPN;
import rip.snake.antivpn.commons.utils.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static rip.snake.antivpn.bungee.utils.Color.colorize;

public class BungeePlayerListener implements Listener {

    private final ServerAntiVPN plugin;
    private final Map<UUID, ServerInfo> players = new HashMap<>();

    public BungeePlayerListener(ServerAntiVPN plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPreLogin(PreLoginEvent event) {
        if (event.isCancelled() || event.getConnection() == null) return;
        String address = event.getConnection().getSocketAddress().toString();

        event.registerIntent(this.plugin);

        try {
            CompletableFuture<CheckResponse> response = this.plugin.getService().getAntiVPN()
                    .getSocketManager().getSocketDataHandler()
                    .verify(new CheckRequest(StringUtils.cleanAddress(address), event.getConnection().getName()));


            Objects.requireNonNull(response, "Server is offline :C").thenAccept(result -> {
                if (result == null || result.isValid()) {
                    event.completeIntent(this.plugin);
                    return;
                }

                event.setCancelReason(colorize(this.plugin.getConfig().getDetectMessage()));
                event.setCancelled(true);
                event.completeIntent(this.plugin);
            }).exceptionally(e -> {
                this.plugin.getLogger().severe("Failed to verify address " + address + "! " + e.getMessage());
                event.completeIntent(this.plugin);
                return null;
            });
        } catch (Exception e) {
            this.plugin.getLogger().severe("Failed to verify address " + address + "! " + e.getMessage());
            event.completeIntent(this.plugin);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PostLoginEvent event) {
        this.handlePlayer(event.getPlayer(), Event.PLAYER_JOIN);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onServerConnected(ServerConnectedEvent event) {
        this.handlePlayer(event.getPlayer(), Event.PLAYER_SWITCH);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(ServerSwitchEvent event) {
        ProxiedPlayer player = event.getPlayer();

        if (!this.players.containsKey(player.getUniqueId())) {
            handlePlayer(player, Event.PLAYER_JOIN);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerSwitch(ServerSwitchEvent event) {
        ProxiedPlayer player = event.getPlayer();

        ServerInfo next = player.getServer().getInfo();
        ServerInfo previous = this.players.put(player.getUniqueId(), next);

        if (previous != null) {
            handlePlayer(player, Event.PLAYER_SWITCH);
        }
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerDisconnectEvent event) {
        this.handlePlayer(event.getPlayer(), Event.PLAYER_QUIT);
    }

    private void handlePlayer(ProxiedPlayer player, Event event) {
        if (player == null) return;
        String username = player.getName();
        String userId = player.getUniqueId().toString();
        String address = player.getSocketAddress().toString();
        String version = String.valueOf(player.getPendingConnection().getVersion());
        boolean isPremium = player.getPendingConnection().isOnlineMode();

        String serverName = player.getServer() != null ? player.getServer().getInfo().getName() : null;
        String hostname = player.getPendingConnection().getVirtualHost().getHostString();

        // Send the data to the backend
        this.plugin.getService().getAntiVPN().getSocketManager().getSocketDataHandler()
                .sendUserData(username, userId, version, address, serverName, hostname, event, isPremium);
    }

}
