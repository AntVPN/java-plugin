package rip.snake.antivpn.velocity.listeners;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import rip.snake.antivpn.core.Service;
import rip.snake.antivpn.core.data.CheckResponse;
import rip.snake.antivpn.core.function.WatchableInvoker;

public class VelocityPlayerListener {

    public static final LegacyComponentSerializer legacy = LegacyComponentSerializer.legacy('&');
    private final Service service;

    public VelocityPlayerListener(Service service) {
        this.service = service;
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onAsyncPreLogin(PreLoginEvent event) {
        if (!event.getResult().isAllowed()) {
            return;
        }
        String address = event.getConnection().getRemoteAddress().getAddress().getHostAddress();

        try {
            WatchableInvoker<CheckResponse> response = service.getSocketManager().verifyAddress(
                    address, event.getUsername()
            );
            if (response == null) {
                service.getLogger().error(
                        "Failed to verify " + event.getUsername() + " (" + address + ")! Backend is not connected"
                );
                return;
            }
            CheckResponse result = response.await();
            if (result == null || result.isValid()) {
                return;
            }
            event.setResult(PreLoginEvent.PreLoginComponentResult.denied(
                    legacy.deserialize(service.getVpnConfig().getDetectMessage())
            ));
        } catch (Exception e) {
            service.getLogger().error("Failed to verify address " + address + "! " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onServerConnect(ServerConnectedEvent event) {
        this.handlePlayer(event.getPlayer(), true);
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onPlayerDisconnect(DisconnectEvent event) {
        this.handlePlayer(event.getPlayer(), false);
    }

    private void handlePlayer(Player player, boolean connected) {
        if (player == null) return;
        String username = player.getUsername();
        String userId = player.getUniqueId().toString();
        String address = player.getRemoteAddress().getAddress().getHostAddress();
        boolean isPremium = player.isOnlineMode();

        String server = player.getCurrentServer().isPresent() ? player.getCurrentServer().get().getServerInfo().getName() : null;

        // Send the data to the backend
        this.service.getSocketManager().sendUserData(username, userId, address, server, connected, isPremium);
    }

}