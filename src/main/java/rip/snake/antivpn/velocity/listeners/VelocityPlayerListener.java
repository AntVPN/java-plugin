package rip.snake.antivpn.velocity.listeners;

import com.velocitypowered.api.event.Continuation;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import net.kyori.adventure.text.Component;
import rip.snake.antivpn.core.Service;
import rip.snake.antivpn.core.utils.Console;

public class VelocityPlayerListener {

    private Service service;

    public VelocityPlayerListener(Service service) {
        this.service = service;
    }

    @Subscribe(order = PostOrder.EARLY)
    public void onAsyncPreLogin(PreLoginEvent event, Continuation continuation) {
        if (!event.getResult().isAllowed()) return;
        String address = event.getConnection().getRemoteAddress().getAddress().getHostAddress();
        Console.debug("PreLoginEvent: %s", address);

        service.getSocketManager().verifyAddress(address).then(result -> {
            Console.debug("PreLoginEvent: %s -> %s", address, result);

            if (result == null || result.isValid()) {
                continuation.resume();
                return;
            }

            event.setResult(PreLoginEvent.PreLoginComponentResult.denied(Component.text(service.getVpnConfig().getDetectMessage())));
            continuation.resume();
        });
    }

}
