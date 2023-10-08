package rip.snake.antivpn.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;
import rip.snake.antivpn.core.Service;
import rip.snake.antivpn.velocity.commands.AntiVPNCommand;
import rip.snake.antivpn.velocity.listeners.VelocityPlayerListener;
import rip.snake.antivpn.velocity.metrics.Metrics;
import rip.snake.antivpn.velocity.utils.SharkLoggerImpl;

import java.nio.file.Path;

@Plugin(
        id = "serverantivpn",
        version = "${VERSION}",
        authors = {"iSnakeBuzz_"},
        url = "https://anti.snake.rip",
        name = "AntiVPN"
)
public class ServerAntiVPN {

    private final Service service;
    private final Logger logger;
    private final ProxyServer server;
    private final Metrics.Factory metricsFactory;

    private String version = "Unknown";

    @Inject
    public ServerAntiVPN(ProxyServer server, Logger logger, @DataDirectory Path pluginData, Metrics.Factory metricsFactory) {
        this.server = server;
        this.logger = logger;
        this.metricsFactory = metricsFactory;

        this.service = new Service(new SharkLoggerImpl(logger), pluginData, this.version);
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        this.initializeMetrics();
        PluginContainer container = this.server
                .getPluginManager()
                .fromInstance(this)
                .orElseThrow(() -> new IllegalArgumentException("The provided instance is not a plugin"));
        this.version = container.getDescription().getVersion().orElse("Unknown");

        this.service.onLoad();
        this.server.getEventManager().register(this, new VelocityPlayerListener(this.service));
       this.server.getCommandManager().register(AntiVPNCommand.createAntiVPNCommand(this.service));
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        this.server.getEventManager().unregisterListeners(this);
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
