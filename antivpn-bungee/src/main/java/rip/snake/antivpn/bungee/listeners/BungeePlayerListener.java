package rip.snake.antivpn.bungee.listeners;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import rip.snake.antivpn.bungee.ServerAntiVPN;
import rip.snake.antivpn.core.data.CheckResponse;
import rip.snake.antivpn.core.function.WatchableInvoker;

import java.util.Objects;

public class BungeePlayerListener implements Listener {

    private final ServerAntiVPN plugin;

    public BungeePlayerListener(ServerAntiVPN plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPreLogin(PreLoginEvent event) {
        if (event.isCancelled() || event.getConnection() == null) return;
        String address = event.getConnection().getSocketAddress().toString();

        event.registerIntent(this.plugin);

        try {
            WatchableInvoker<CheckResponse> response = this.plugin.getService().getSocketManager().verifyAddress(address, event.getConnection().getName());

            Objects.requireNonNull(response, "Server is offline :C").then(result -> {
                if (result == null || result.isValid()) {
                    event.completeIntent(this.plugin);
                    return;
                }

                event.setCancelReason(TextComponent.fromLegacyText(this.plugin.getConfig().getDetectMessage()));
                event.setCancelled(true);
                event.completeIntent(this.plugin);
            });
        } catch (Exception e) {
            this.plugin.getLogger().severe("Failed to verify address " + address + "! " + e.getMessage());
            event.completeIntent(this.plugin);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onServerConnected(ServerConnectedEvent event) {
        this.handlePlayer(event.getPlayer(), true);
    }

    @EventHandler
    public void onPlayerDisconnect(PostLoginEvent event) {
        this.handlePlayer(event.getPlayer(), false);
    }

    private void handlePlayer(ProxiedPlayer player, boolean connected) {
        if (player == null) return;
        String username = player.getName();
        String userId = player.getUniqueId().toString();
        String address = player.getSocketAddress().toString();
        boolean isPremium = player.getPendingConnection().isOnlineMode();

        String serverName = player.getServer() != null ? player.getServer().getInfo().getName() : null;

        // Send the data to the backend
        this.plugin.getService().getSocketManager().sendUserData(username, userId, address, serverName, connected, isPremium);
    }

}
