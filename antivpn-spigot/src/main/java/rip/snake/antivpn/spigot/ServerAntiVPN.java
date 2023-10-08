package rip.snake.antivpn.spigot;

import org.bukkit.plugin.java.JavaPlugin;
import rip.snake.antivpn.core.Service;
import rip.snake.antivpn.spigot.commands.AntiVPNCommand;
import rip.snake.antivpn.spigot.listeners.PlayerListener;
import rip.snake.antivpn.spigot.utils.SharkLoggerImpl;

public class ServerAntiVPN extends JavaPlugin {

    private Service service;

    @Override
    public void onEnable() {
        this.service = new Service(new SharkLoggerImpl(), this.getDataFolder().toPath(), this.getDescription().getVersion());
        this.service.onLoad();

        this.getCommand("antivpn").setExecutor(new AntiVPNCommand(this.service));
        this.getServer().getPluginManager().registerEvents(new PlayerListener(this.service), this);
    }

    @Override
    public void onDisable() {
        this.service.onDisable();
    }
}
