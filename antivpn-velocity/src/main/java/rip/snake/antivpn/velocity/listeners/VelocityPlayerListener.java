package rip.snake.antivpn.velocity.listeners;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import io.antivpn.api.data.socket.request.impl.CheckRequest;
import io.antivpn.api.data.socket.response.impl.CheckResponse;
import io.antivpn.api.utils.Event;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import rip.snake.antivpn.commons.Service;
import rip.snake.antivpn.commons.utils.StringUtils;

import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

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
            CompletableFuture<CheckResponse> response = service.getAntiVPN().getSocketManager().getSocketDataHandler()
                    .verify(new CheckRequest(StringUtils.cleanAddress(address), event.getUsername()));
            if (response == null) {
                service.getLogger().error(
                        "Failed to verify " + event.getUsername() + " (" + address + ")! Backend is not connected"
                );
                return;
            }
            CheckResponse result = response.get();
            if (result == null) {
                return;
            }

            if (result.isAttack()) {
                event.setResult(PreLoginEvent.PreLoginComponentResult.denied(
                        legacy.deserialize(service.getAntiVPN().getSocketManager().getShieldKick())
                ));
                return;
            }

            if (result.isValid()) {
                return;
            }

            event.setResult(PreLoginEvent.PreLoginComponentResult.denied(
                    legacy.deserialize(service.getAntiVPN().getSocketManager().getResponseKick())
            ));
        } catch (Exception e) {
            service.getLogger().error("Failed to verify address " + address + "! " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Subscribe
    public void onPlayerSwitch(ServerConnectedEvent event) {
        Player player = event.getPlayer();
        Optional<ServerConnection> currentServer = player.getCurrentServer();

        if (currentServer.isPresent()) {
            this.handlePlayer(player, Event.PLAYER_SWITCH);
        } else {
            this.handlePlayer(player, Event.PLAYER_JOIN);
        }
    }

    @Subscribe(order = PostOrder.LAST)
    public void onPlayerDisconnect(DisconnectEvent event) {
        this.handlePlayer(event.getPlayer(), Event.PLAYER_QUIT);
    }

    private void handlePlayer(Player player, Event event) {
        if (player == null) return;
        String username = player.getUsername();
        String userId = player.getUniqueId().toString();
        String address = player.getRemoteAddress().getAddress().getHostAddress();
        String version = String.valueOf(player.getProtocolVersion().getProtocol());
        boolean isPremium = player.isOnlineMode();

        String server = player.getCurrentServer().isPresent() ? player.getCurrentServer().get().getServerInfo().getName() : null;
        String hostname = player.getVirtualHost().map(InetSocketAddress::getHostString).orElse(null);

        // Send the data to the backend
        this.service.getAntiVPN().getSocketManager().getSocketDataHandler()
                .sendUserData(username, userId, version, address, server, hostname, event, isPremium);
    }
}