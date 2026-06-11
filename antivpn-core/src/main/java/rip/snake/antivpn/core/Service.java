package rip.snake.antivpn.core;

import io.antivpn.api.AntiVPN;
import io.antivpn.api.config.AntiVPNConfig;
import io.antivpn.api.logging.VPNLogger;
import lombok.Data;
import rip.snake.antivpn.core.config.VPNConfig;
import rip.snake.antivpn.core.utils.ConfigUtils;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Timer;

@Data
public class Service {

    public static Service INSTANCE;

    private final Path home;
    private final VPNLogger logger;
    private final String version;

    private final Timer timer;
    private final VPNConfig vpnConfig;

    private final AntiVPN antiVPN;

    public Service(VPNLogger logger, Path home, String version) {
        INSTANCE = this;

        this.timer = new Timer();
        this.logger = logger;

        this.home = Path.of(home.toString().replaceFirst("serverantivpn", "ServerAntiVPN"));
        this.version = version;

        this.vpnConfig = ConfigUtils.loadConfig(this.home.resolve("config.json"), logger);
        AntiVPNConfig antiVPNConfig = this.vpnConfig.toAntiVPNConfig();
        this.saveConfig();

        String userAgent = this.vpnConfig.getUserAgent() != null
                ? this.vpnConfig.getUserAgent()
                : "ServerAntiVPN v" + version;

        this.antiVPN = AntiVPN.create(
                userAgent, this.logger,
                antiVPNConfig, Duration.ofSeconds(30)
        );

        this.antiVPN.getSocketManager().setResponseKick(this.vpnConfig.getDetectMessage());
        this.antiVPN.getSocketManager().setShieldKick(this.vpnConfig.getShieldMessage());
    }

    public void onLoad() {
        this.antiVPN.start();
    }

    public void onDisable() {
        this.antiVPN.getSocketManager().close();
    }

    public boolean saveConfig() {
        return ConfigUtils.writeConfig(this.home.resolve("config.json"), this.logger, this.vpnConfig);
    }
}
