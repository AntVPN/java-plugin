package rip.snake.antivpn.core;

import lombok.Data;
import org.slf4j.Logger;
import rip.snake.antivpn.core.config.VPNConfig;
import rip.snake.antivpn.core.socket.SocketManager;
import rip.snake.antivpn.core.tasks.TimeoutTask;
import rip.snake.antivpn.core.utils.ConfigUtils;

import java.nio.file.Path;
import java.util.Timer;

@Data
public class Service {

    public static Service INSTANCE;

    private final Path home;
    private final Logger logger;
    private final String version;

    private final Timer timer;
    private final VPNConfig vpnConfig;
    private final SocketManager socketManager;

    public Service(Logger logger, Path home, String version) {
        INSTANCE = this;

        this.timer = new Timer();

        this.logger = logger;
        this.home = home;
        this.version = version;

        this.vpnConfig = ConfigUtils.loadConfig(home.resolve("config.json"));
        this.socketManager = new SocketManager(this);
    }

    public void onLoad() {
        this.socketManager.connect();
        this.timer.scheduleAtFixedRate(new TimeoutTask(this), 0, 8000);
    }

    public void onDisable() {
        this.socketManager.close();
    }

}
