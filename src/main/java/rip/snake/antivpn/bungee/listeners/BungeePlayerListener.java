package rip.snake.antivpn.bungee.listeners;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import rip.snake.antivpn.bungee.ServerAntiVPN;
import rip.snake.antivpn.core.utils.Console;

public class BungeePlayerListener implements Listener {

    private final ServerAntiVPN plugin;

    public BungeePlayerListener(ServerAntiVPN plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPreLogin(PreLoginEvent event) {
        if (event.isCancelled() || event.getConnection() == null) return;
        String address = event.getConnection().getSocketAddress().toString();
        Console.debug("PreLoginEvent: %s", address);

        try {
            plugin.getService().getSocketManager().verifyAddress(address).then(result -> {
                Console.debug("PreLoginEvent: %s -> %s", address, result);
                if (result == null || result.isValid()) return;

                event.setCancelReason(TextComponent.fromLegacyText(plugin.getConfig().getDetectMessage()));
                event.setCancelled(true);
            }).await();
        } catch (InterruptedException e) {
            plugin.getLogger().severe("Failed to verify address " + address + "! " + e.getMessage());
        }
    }

}
