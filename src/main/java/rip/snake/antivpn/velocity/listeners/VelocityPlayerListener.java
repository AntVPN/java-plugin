package rip.snake.antivpn.velocity.listeners;

import com.velocitypowered.api.event.Continuation;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import net.kyori.adventure.text.Component;
import rip.snake.antivpn.core.Service;
import rip.snake.antivpn.core.data.DataResponse;
import rip.snake.antivpn.core.function.WatcherFunction;

import java.util.Objects;

public class VelocityPlayerListener {

    private final Service service;

    public VelocityPlayerListener(Service service) {
        this.service = service;
    }

    @Subscribe(order = PostOrder.EARLY)
    public void onAsyncPreLogin(PreLoginEvent event, Continuation continuation) {
        if (!event.getResult().isAllowed()) return;
        String address = event.getConnection().getRemoteAddress().getAddress().getHostAddress();

        try {
            WatcherFunction<DataResponse> response = service.getSocketManager().verifyAddress(address, event.getUsername());

            Objects.requireNonNull(response, "Server is offline :C").then(result -> {
                if (result == null || result.isValid()) {
                    continuation.resume();
                    return;
                }

                event.setResult(PreLoginEvent.PreLoginComponentResult.denied(Component.text(service.getVpnConfig().getDetectMessage())));
                continuation.resume();
            });
        } catch (Exception e) {
            service.getLogger().error("Failed to verify address " + address + "! " + e.getMessage());
            continuation.resume();
        }
    }

}
