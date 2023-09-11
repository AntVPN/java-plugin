package rip.snake.antivpn.velocity.listeners;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import rip.snake.antivpn.core.Service;
import rip.snake.antivpn.core.data.DataResponse;
import rip.snake.antivpn.core.function.WatcherFunction;

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
            WatcherFunction<DataResponse> response = service.getSocketManager().verifyAddress(
                    address, event.getUsername()
            );

            response.then(result -> {
                if (result == null || result.isValid()) {
                    return;
                }
                event.setResult(PreLoginEvent.PreLoginComponentResult.denied(
                        legacy.deserialize(service.getVpnConfig().getDetectMessage())
                ));
            }).await();
        } catch (Exception e) {
            service.getLogger().error("Failed to verify address " + address + "! " + e.getMessage());
            e.printStackTrace();
        }
    }
}