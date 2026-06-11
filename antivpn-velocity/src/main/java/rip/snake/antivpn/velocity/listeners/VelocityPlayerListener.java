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
import io.antivpn.api.model.response.CheckResponse;
import io.antivpn.api.util.Event;
import rip.snake.antivpn.core.PlayerData;
import rip.snake.antivpn.core.Service;
import rip.snake.antivpn.core.VPNCheckHandler;
import rip.snake.antivpn.velocity.utils.Color;

import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class VelocityPlayerListener {

    private final VPNCheckHandler handler;

    public VelocityPlayerListener(Service service) {
        this.handler = new VPNCheckHandler(service);
    }

    @Subscribe(order = PostOrder.LAST)
    public void onAsyncPreLogin(LoginEvent event) {
        if (!event.getResult().isAllowed()) return;

        String address = event.getPlayer().getRemoteAddress().getAddress().getHostAddress();
        String username = event.getPlayer().getUsername();
        UUID uuid = event.getPlayer().getUniqueId();

        try {
            CompletableFuture<CheckResponse> response = handler.verify(address, uuid, username);
            if (response == null) {
                return;
            }
            CheckResponse result = response.get();
            if (result == null) return;

            if (result.isAttack()) {
                event.setResult(ResultedEvent.ComponentResult.denied(Color.colorize(handler.getShieldKick())));
            } else if (result.isValid()) {
                handler.storeSession(uuid, result.getSessionId());
            } else {
                event.setResult(ResultedEvent.ComponentResult.denied(Color.colorize(handler.getDetectKick())));
            }
        } catch (Exception e) {
            // silently ignore on timeout
        }
    }

    @Subscribe
    public void onPlayerSwitch(ServerConnectedEvent event) {
        Player player = event.getPlayer();
        Optional<RegisteredServer> previous = event.getPreviousServer();
        RegisteredServer current = event.getServer();

        if (previous.isEmpty()) {
            handlePlayer(player, Event.PLAYER_JOIN, current.getServerInfo().getName());
        } else if (!previous.get().getServerInfo().getName().equals(current.getServerInfo().getName())) {
            handlePlayer(player, Event.PLAYER_SWITCH, current.getServerInfo().getName());
        }
    }

    @Subscribe(order = PostOrder.LAST)
    public void onPlayerDisconnect(DisconnectEvent event) {
        String serverName = event.getPlayer().getCurrentServer().map(ServerConnection::getServerInfo).map(ServerInfo::getName).orElse(null);
        handlePlayer(event.getPlayer(), Event.PLAYER_QUIT, serverName);
    }

    private void handlePlayer(Player player, Event event, String serverName) {
        if (player == null) return;
        handler.sendUserData(new PlayerData(
                event == Event.PLAYER_QUIT ? handler.removeSession(player.getUniqueId()) : handler.getSession(player.getUniqueId()),
                player.getUsername(),
                player.getUniqueId().toString(),
                String.valueOf(player.getProtocolVersion().getProtocol()),
                player.getRemoteAddress().getAddress().getHostAddress(),
                serverName,
                player.getVirtualHost().map(InetSocketAddress::getHostString).orElse(null),
                player.isOnlineMode(),
                event
        ));
    }

}