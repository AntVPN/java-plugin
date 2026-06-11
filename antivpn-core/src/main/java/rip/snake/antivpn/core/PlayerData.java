package rip.snake.antivpn.core;

import io.antivpn.api.util.Event;

public record PlayerData(
        String sessionId,
        String username,
        String userId,
        String version,
        String address,
        String serverName,
        String hostname,
        boolean premium,
        Event event
) {
}
