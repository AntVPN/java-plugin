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

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static rip.snake.antivpn.bungee.utils.Color.colorize;

public class BungeePlayerListener implements Listener {

    private final ServerAntiVPN plugin;
    private final Map<UUID, String> sessions = new HashMap<>();

    public BungeePlayerListener(ServerAntiVPN plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPreLogin(PreLoginEvent event) {
        if (event.isCancelled() || event.getConnection() == null) return;
        String address = ((InetSocketAddress) event.getConnection().getSocketAddress()).getAddress().getHostAddress();
        String username = event.getConnection().getName();
        String userId = event.getConnection().getUniqueId().toString();

        event.registerIntent(this.plugin);

        try {
            CompletableFuture<CheckResponse> response = this.plugin.getService().getAntiVPN()
                    .getSocketManager().getSocketDataHandler()
                    .verify(new CheckRequest(StringUtils.cleanAddress(address), userId, username));

            Objects.requireNonNull(response, "Server is offline :C").thenAccept(result -> {
                if (result == null) {
                    event.completeIntent(this.plugin);
                    return;
                }

                if (result.isAttack()) {
                    event.setCancelReason(colorize(this.plugin.getService().getAntiVPN().getSocketManager().getShieldKick()));
                    event.setCancelled(true);
                    event.completeIntent(this.plugin);
                    return;
                }

                if (result.isValid()) {
                    this.sessions.put(event.getConnection().getUniqueId(), result.getSessionId());
                    event.completeIntent(this.plugin);
                    return;
                }

                event.setCancelReason(colorize(this.plugin.getService().getAntiVPN().getSocketManager().getResponseKick()));
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

    @EventHandler
    public void onServerSwitch(ServerSwitchEvent event) {
        ProxiedPlayer player = event.getPlayer();
        ServerInfo from = event.getFrom();
        ServerInfo to = player.getServer().getInfo();

        if (from == null) {
            // First server after connecting to the proxy = logical join
            this.handlePlayer(player, Event.PLAYER_JOIN, to.getName());
        } else if (!from.getName().equals(to.getName())) {
            // Actual server switch
            this.handlePlayer(player, Event.PLAYER_SWITCH, to.getName());
        }
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerDisconnectEvent event) {
        String serverName = event.getPlayer().getServer() != null ? event.getPlayer().getServer().getInfo().getName() : null;
        this.handlePlayer(event.getPlayer(), Event.PLAYER_QUIT, serverName);
    }

    private void handlePlayer(ProxiedPlayer player, Event event, String serverName) {
        if (player == null) return;
        String username = player.getName();
        String userId = player.getUniqueId().toString();
        String address = ((InetSocketAddress) player.getSocketAddress()).getAddress().getHostAddress();
        String version = String.valueOf(player.getPendingConnection().getVersion());
        boolean isPremium = player.getPendingConnection().isOnlineMode();

        InetSocketAddress virtualHost = player.getPendingConnection().getVirtualHost();
        String hostname = virtualHost != null ? virtualHost.getHostString() : null;
        String checkId = event == Event.PLAYER_QUIT ? this.sessions.remove(player.getUniqueId()) : this.sessions.get(player.getUniqueId());

        // Send the data to the backend
        this.plugin.getService().getAntiVPN().getSocketManager().getSocketDataHandler()
                .sendUserData(checkId, username, userId, version, address, serverName, hostname, event, isPremium);
    }

}
