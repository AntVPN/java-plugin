package rip.snake.antivpn.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.bstats.velocity.Metrics;
import org.slf4j.Logger;
import rip.snake.antivpn.core.Service;
import rip.snake.antivpn.velocity.listeners.VelocityPlayerListener;

import java.nio.file.Path;

@Plugin(
        id = "serverantivpn",
        name = "AntiVPN"
)
public class ServerAntiVPN {

    private final Service service;
    private final Logger logger;
    private final ProxyServer server;
    private final Metrics.Factory metricsFactory;

    @Inject
    public ServerAntiVPN(ProxyServer server, Logger logger, @DataDirectory Path pluginData, Metrics.Factory metricsFactory) {
        this.server = server;
        this.logger = logger;
        this.metricsFactory = metricsFactory;

        this.service = new Service(logger, pluginData);
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        this.service.onLoad();
        this.initializeMetrics();

        server.getEventManager().register(this, new VelocityPlayerListener(service));
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        server.getEventManager().unregisterListeners(this);
        this.service.onDisable();
    }

    private void initializeMetrics() {
        try {
            metricsFactory.make(this, 19268);
        } catch (Exception ex) {
            logger.warn("Failed to connect with bStats", ex);
        }
    }

}
