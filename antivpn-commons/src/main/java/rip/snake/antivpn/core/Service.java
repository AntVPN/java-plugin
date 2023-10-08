package rip.snake.antivpn.core;

import lombok.Data;
import rip.snake.antivpn.core.config.VPNConfig;
import rip.snake.antivpn.core.socket.SocketManager;
import rip.snake.antivpn.core.tasks.TimeoutTask;
import rip.snake.antivpn.core.utils.ConfigUtils;
import rip.snake.antivpn.core.utils.SharkLogger;

import java.nio.file.Path;
import java.util.Timer;

@Data
public class Service {

    public static Service INSTANCE;

    private final Path home;
    private final SharkLogger logger;
    private final String version;

    private final Timer timer;
    private final VPNConfig vpnConfig;
    private final SocketManager socketManager;

    public Service(SharkLogger logger, Path home, String version) {
        INSTANCE = this;

        this.timer = new Timer();

        this.logger = logger;
        this.home = Path.of(home.toString().replaceFirst("serverantivpn", "ServerAntiVPN"));
        this.version = version;

        this.vpnConfig = ConfigUtils.loadConfig(this.home.resolve("config.json"));
        this.socketManager = new SocketManager(this);
    }

    public void onLoad() {
        this.socketManager.connect();
        this.timer.scheduleAtFixedRate(new TimeoutTask(this), 0, 8000);
    }

    public void onDisable() {
        this.socketManager.close();
    }

    public boolean saveConfig() {
        return ConfigUtils.writeConfig(this.home.resolve("config.json"), this.vpnConfig);
    }
}
