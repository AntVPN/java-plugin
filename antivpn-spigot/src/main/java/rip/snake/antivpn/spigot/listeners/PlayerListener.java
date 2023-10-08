package rip.snake.antivpn.spigot.listeners;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerPreLoginEvent;
import rip.snake.antivpn.core.Service;
import rip.snake.antivpn.core.data.CheckResponse;
import rip.snake.antivpn.core.function.WatchableInvoker;

public class PlayerListener implements Listener {

    private final Service service;

    public PlayerListener(Service service) {
        this.service = service;
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(AsyncPlayerPreLoginEvent event) {
        if (event.getResult() != PlayerPreLoginEvent.Result.ALLOWED) {
            return;
        }
        String address = event.getAddress().getHostAddress();

        try {
            WatchableInvoker<CheckResponse> response = service.getSocketManager().verifyAddress(
                    address, event.getName()
            );
            if (response == null) {
                service.getLogger().error(
                        "Failed to verify " + event.getName() + " (" + address + ")! Backend is not connected"
                );
                return;
            }
            CheckResponse result = response.await();
            if (result == null || result.isValid()) {
                return;
            }

            event.setKickMessage(ChatColor.translateAlternateColorCodes('&', service.getVpnConfig().getDetectMessage()));
            event.setResult(PlayerPreLoginEvent.Result.KICK_OTHER);
        } catch (Exception e) {
            service.getLogger().error("Failed to verify address " + address + "! " + e.getMessage());
            e.printStackTrace();
        }
    }

}