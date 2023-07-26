package rip.snake.antivpn.core;

import lombok.Data;
import org.slf4j.Logger;
import rip.snake.antivpn.core.config.VPNConfig;
import rip.snake.antivpn.core.socket.SocketManager;
import rip.snake.antivpn.core.utils.ConfigUtils;

import java.nio.file.Path;

@Data
public class Service {

    public static Service INSTANCE;

    private final Path home;
    private final Logger logger;

    private final VPNConfig vpnConfig;
    private final SocketManager socketManager;

    public Service(Logger logger, Path home) {
        INSTANCE = this;

        this.logger = logger;
        this.home = home;

        this.vpnConfig = ConfigUtils.loadConfig(home.resolve("config.json"));
        this.socketManager = new SocketManager(this);
    }

    public void onLoad() {
        this.socketManager.open();
    }

    public void onDisable() {
        this.socketManager.close();
    }

}
