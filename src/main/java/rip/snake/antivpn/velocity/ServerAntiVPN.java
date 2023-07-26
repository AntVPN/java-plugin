package rip.snake.antivpn.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;
import rip.snake.antivpn.core.Service;
import rip.snake.antivpn.velocity.listeners.VelocityPlayerListener;

@Plugin(
        id = "AntiVPN",
        name = "AntiVPN"
)
public class ServerAntiVPN {

    private final Service service;
    private final Logger logger;
    private final ProxyServer server;

    @Inject
    public ServerAntiVPN(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;

        this.service = new Service(logger);
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        server.getEventManager().register(this, new VelocityPlayerListener(service));
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        server.getEventManager().unregisterListeners(this);
        this.service.onDisable();
    }

}
