package rip.snake.antivpn.velocity.listeners;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import io.antivpn.api.data.socket.request.impl.CheckRequest;
import io.antivpn.api.data.socket.response.impl.CheckResponse;
import io.antivpn.api.utils.Event;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import rip.snake.antivpn.commons.Service;
import rip.snake.antivpn.commons.utils.StringUtils;
import rip.snake.antivpn.velocity.utils.Color;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class VelocityPlayerListener {

    public static final LegacyComponentSerializer legacy = LegacyComponentSerializer.legacy('&');
    private final Service service;

    private final Map<UUID, String> sessions = new HashMap<>();

    public VelocityPlayerListener(Service service) {
        this.service = service;
    }

    @Subscribe(order = PostOrder.LAST)
    public void onAsyncPreLogin(LoginEvent event) {
        if (!event.getResult().isAllowed()) {
            return;
        }

        String address = event.getPlayer().getRemoteAddress().getAddress().getHostAddress();
        String userId = event.getPlayer().getUniqueId().toString();
        String username = event.getPlayer().getUsername();

        try {
            CompletableFuture<CheckResponse> response = service.getAntiVPN().getSocketManager().getSocketDataHandler()
                    .verify(new CheckRequest(StringUtils.cleanAddress(address), userId, username));
            if (response == null) {
                service.getLogger().error(
                        "Failed to verify " + username + " (" + address + ")! Backend is not connected"
                );
                return;
            }
            CheckResponse result = response.get();
            if (result == null) {
                return;
            }

            if (result.isAttack()) {
                event.setResult(ResultedEvent.ComponentResult.denied(
                        Color.colorize(service.getAntiVPN().getSocketManager().getShieldKick())
                ));
                return;
            }

            if (result.isValid()) {
                this.sessions.put(event.getPlayer().getUniqueId(), result.getSessionId());
                return;
            }

            event.setResult(ResultedEvent.ComponentResult.denied(
                    Color.colorize(service.getAntiVPN().getSocketManager().getResponseKick())
            ));
        } catch (Exception e) {
            service.getLogger().error("Failed to verify address " + address + "! " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Subscribe
    public void onPlayerSwitch(ServerConnectedEvent event) {
        Player player = event.getPlayer();
        Optional<RegisteredServer> previous = event.getPreviousServer();
        RegisteredServer current = event.getServer();

        if (previous.isEmpty()) {
            // First server after connecting to the proxy = logical join
            this.handlePlayer(player, Event.PLAYER_JOIN, current.getServerInfo().getName());
        } else if (!previous.get().getServerInfo().getName().equals(current.getServerInfo().getName())) {
            // Actual server switch
            this.handlePlayer(player, Event.PLAYER_SWITCH, current.getServerInfo().getName());
        }
    }


    @Subscribe(order = PostOrder.LAST)
    public void onPlayerDisconnect(DisconnectEvent event) {
        String serverName = event.getPlayer().getCurrentServer().map(ServerConnection::getServerInfo).map(ServerInfo::getName).orElse(null);
        this.handlePlayer(event.getPlayer(), Event.PLAYER_QUIT, serverName);
    }

    private void handlePlayer(Player player, Event event, String serverName) {
        if (player == null) return;
        String username = player.getUsername();
        String userId = player.getUniqueId().toString();
        String address = player.getRemoteAddress().getAddress().getHostAddress();
        String version = String.valueOf(player.getProtocolVersion().getProtocol());
        boolean isPremium = player.isOnlineMode();

        String hostname = player.getVirtualHost().map(InetSocketAddress::getHostString).orElse(null);
        String checkId = event == Event.PLAYER_QUIT ? this.sessions.remove(player.getUniqueId()) : this.sessions.get(player.getUniqueId());

        // Send the data to the backend
        this.service.getAntiVPN().getSocketManager().getSocketDataHandler()
                .sendUserData(checkId, username, userId, version, address, serverName, hostname, event, isPremium);
    }
}