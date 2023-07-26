package rip.snake.antivpn.velocity.listeners;

import com.velocitypowered.api.event.Continuation;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import rip.snake.antivpn.core.Service;

public class VelocityPlayerListener {

    private Service service;

    public VelocityPlayerListener(Service service) {
        this.service = service;
    }

    @Subscribe(order = PostOrder.EARLY)
    public void onAsyncPreLogin(PreLoginEvent event, Continuation continuation) {
        if (!event.getResult().isAllowed()) return;
        String address = event.getConnection().getRemoteAddress().getAddress().getHostAddress();
        service.getSocketManager().verifyAddress(address).then(result -> {
            if (result.isValid()) {
                continuation.resume();
                return;
            }

            event.setResult(PreLoginEvent.PreLoginComponentResult.denied(Component.text("You are using a VPN or Proxy.").color(TextColor.color(0xFF5555))));
            continuation.resume();
        });
    }

}
