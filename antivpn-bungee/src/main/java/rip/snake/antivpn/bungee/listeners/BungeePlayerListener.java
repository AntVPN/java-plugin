package rip.snake.antivpn.bungee.listeners;

import io.antivpn.api.model.response.CheckResponse;
import io.antivpn.api.util.Event;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import rip.snake.antivpn.bungee.ServerAntiVPN;
import rip.snake.antivpn.core.PlayerData;
import rip.snake.antivpn.core.VPNCheckHandler;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

import static rip.snake.antivpn.bungee.utils.Color.colorize;

public class BungeePlayerListener implements Listener {

    private final ServerAntiVPN plugin;
    private final VPNCheckHandler handler;

    public BungeePlayerListener(ServerAntiVPN plugin) {
        this.plugin = plugin;
        this.handler = new VPNCheckHandler(plugin.getService());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPreLogin(PreLoginEvent event) {
        if (event.isCancelled() || event.getConnection() == null) return;
        String address = ((InetSocketAddress) event.getConnection().getSocketAddress()).getAddress().getHostAddress();
        String username = event.getConnection().getName();
        UUID uniqueId = resolveUniqueId(event.getConnection().getUniqueId(), username);

        event.registerIntent(this.plugin);

        try {
            Objects.requireNonNull(handler.verify(address, uniqueId, username), "Server is offline :C")
                    .thenAccept(result -> handleCheckResult(event, uniqueId, result))
                    .exceptionally(e -> {
                        this.plugin.getLogger().severe("Failed to verify address " + address + "! " + e.getMessage());
                        event.completeIntent(this.plugin);
                        return null;
                    });
        } catch (Exception e) {
            this.plugin.getLogger().severe("Failed to verify address " + address + "! " + e.getMessage());
            event.completeIntent(this.plugin);
        }
    }

    private void handleCheckResult(PreLoginEvent event, UUID uniqueId, CheckResponse result) {
        if (result == null) {
            event.completeIntent(this.plugin);
            return;
        }
        if (result.isAttack()) {
            event.setCancelReason(colorize(handler.getShieldKick()));
            event.setCancelled(true);
        } else if (result.isValid()) {
            handler.storeSession(uniqueId, result.getSessionId());
        } else {
            event.setCancelReason(colorize(handler.getDetectKick()));
            event.setCancelled(true);
        }
        event.completeIntent(this.plugin);
    }

    private UUID resolveUniqueId(UUID uniqueId, String username) {
        if (uniqueId != null) return uniqueId;
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(StandardCharsets.UTF_8));
    }

    @EventHandler
    public void onServerSwitch(ServerSwitchEvent event) {
        ProxiedPlayer player = event.getPlayer();
        ServerInfo from = event.getFrom();
        ServerInfo to = player.getServer().getInfo();

        if (from == null) {
            handlePlayer(player, Event.PLAYER_JOIN, to.getName());
        } else if (!from.getName().equals(to.getName())) {
            handlePlayer(player, Event.PLAYER_SWITCH, to.getName());
        }
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerDisconnectEvent event) {
        String serverName = event.getPlayer().getServer() != null ? event.getPlayer().getServer().getInfo().getName() : null;
        handlePlayer(event.getPlayer(), Event.PLAYER_QUIT, serverName);
    }

    private void handlePlayer(ProxiedPlayer player, Event event, String serverName) {
        if (player == null) return;
        InetSocketAddress virtualHost = player.getPendingConnection().getVirtualHost();
        String hostname = virtualHost != null ? virtualHost.getHostString() : null;

        handler.sendUserData(new PlayerData(
                event == Event.PLAYER_QUIT ? handler.removeSession(player.getUniqueId()) : handler.getSession(player.getUniqueId()),
                player.getName(),
                player.getUniqueId().toString(),
                String.valueOf(player.getPendingConnection().getVersion()),
                ((InetSocketAddress) player.getSocketAddress()).getAddress().getHostAddress(),
                serverName,
                hostname,
                player.getPendingConnection().isOnlineMode(),
                event
        ));
    }

}
