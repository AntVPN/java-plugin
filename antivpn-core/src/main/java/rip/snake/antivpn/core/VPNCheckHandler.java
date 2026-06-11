package rip.snake.antivpn.core;

import io.antivpn.api.model.request.CheckRequest;
import io.antivpn.api.model.request.UserData;
import io.antivpn.api.model.response.CheckResponse;
import io.antivpn.api.util.Event;
import rip.snake.antivpn.core.utils.StringUtils;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class VPNCheckHandler {

    private final Service service;
    private final Map<UUID, String> sessions = new ConcurrentHashMap<>();

    public VPNCheckHandler(Service service) {
        this.service = service;
    }

    public CompletableFuture<CheckResponse> verify(String address, UUID uuid, String username) {
        return service.getAntiVPN().getSocketManager().getSocketDataHandler()
                .verify(new CheckRequest(StringUtils.cleanAddress(address), uuid.toString(), username));
    }

    public String getShieldKick() {
        return service.getAntiVPN().getSocketManager().getShieldKick();
    }

    public String getDetectKick() {
        return service.getAntiVPN().getSocketManager().getResponseKick();
    }

    public void storeSession(UUID uuid, String sessionId) {
        sessions.put(uuid, sessionId);
    }

    public String getSession(UUID uuid) {
        return sessions.get(uuid);
    }

    public String removeSession(UUID uuid) {
        return sessions.remove(uuid);
    }

    public void sendUserData(PlayerData data) {
        var builder = UserData.builder()
                .sessionId(data.sessionId())
                .username(data.username())
                .userId(data.userId())
                .version(data.version())
                .address(data.address())
                .event(data.event())
                .premium(data.premium());
        if (data.serverName() != null) builder.server(data.serverName());
        if (data.hostname() != null) builder.hostname(data.hostname());
        service.getAntiVPN().getSocketManager().getSocketDataHandler().sendUserData(builder.build());
    }

}
