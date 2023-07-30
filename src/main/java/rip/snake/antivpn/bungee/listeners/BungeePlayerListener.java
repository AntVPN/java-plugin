package rip.snake.antivpn.bungee.listeners;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import rip.snake.antivpn.bungee.ServerAntiVPN;
import rip.snake.antivpn.core.data.DataResponse;
import rip.snake.antivpn.core.function.WatcherFunction;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

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
            WatcherFunction<DataResponse> response = this.plugin.getService().getSocketManager().verifyAddress(address, event.getConnection().getName());

            Objects.requireNonNull(response, "Server is offline :C").then(result -> {
                if (result == null || result.isValid()) return;

                event.setCancelReason(TextComponent.fromLegacyText(this.plugin.getConfig().getDetectMessage()));
                event.setCancelled(true);
                event.completeIntent(this.plugin);
            });
        } catch (Exception e) {
            this.plugin.getLogger().severe("Failed to verify address " + address + "! " + e.getMessage());
            event.completeIntent(this.plugin);
        }
    }

}
