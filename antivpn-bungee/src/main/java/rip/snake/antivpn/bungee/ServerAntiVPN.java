package rip.snake.antivpn.bungee;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.md_5.bungee.api.plugin.Plugin;
import rip.snake.antivpn.bungee.commands.AntiVPNCommand;
import rip.snake.antivpn.bungee.listeners.BungeePlayerListener;
import rip.snake.antivpn.bungee.metrics.Metrics;
import rip.snake.antivpn.core.Service;
import rip.snake.antivpn.core.config.VPNConfig;

@Slf4j
@Getter
public class ServerAntiVPN extends Plugin {

    private final Service service;

    public ServerAntiVPN() {
        this.service = new Service(log, getDataFolder().toPath(), this.getDescription().getVersion());
    }

    @Override
    public void onLoad() {
        this.service.onLoad();
        this.initializeMetrics();

        this.getProxy().getPluginManager().registerListener(this, new BungeePlayerListener(this));
        this.getProxy().getPluginManager().registerCommand(this, new AntiVPNCommand(this.service));
    }

    @Override
    public void onDisable() {
        this.service.onDisable();
    }

    public VPNConfig getConfig() {
        return this.service.getVpnConfig();
    }

    private void initializeMetrics() {
        new Metrics(this, 19267);
    }
}
