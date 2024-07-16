package rip.snake.antivpn.spigot;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import rip.snake.antivpn.commons.Service;
import rip.snake.antivpn.spigot.commands.AntiVPNCommand;
import rip.snake.antivpn.spigot.listeners.PlayerListener;
import rip.snake.antivpn.spigot.utils.SharkLoggerImpl;
import rip.snake.antivpn.spigot.version.VersionHelper;

@Getter
public class ServerAntiVPN extends JavaPlugin {

    private Service service;
    private VersionHelper versionHelper;

    @Override
    public void onEnable() {
        this.service = new Service(new SharkLoggerImpl(), this.getDataFolder().toPath(), this.getDescription().getVersion());
        this.service.onLoad();

        this.getCommand("antivpn").setExecutor(new AntiVPNCommand(this.service));
        this.getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        if (Bukkit.getPluginManager().isPluginEnabled("ViaVersion")) {
            this.versionHelper = new rip.snake.antivpn.spigot.version.impl.ViaVersion();
        } else {
            this.versionHelper = new rip.snake.antivpn.spigot.version.impl.BukkitHelper();
        }
    }

    @Override
    public void onDisable() {
        this.service.onDisable();
    }
}

