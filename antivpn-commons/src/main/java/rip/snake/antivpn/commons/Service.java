package rip.snake.antivpn.commons;

import io.antivpn.api.AntiVPN;
import io.antivpn.api.config.AntiVPNConfig;
import io.antivpn.api.logger.Console;
import io.antivpn.api.logger.VPNLogger;
import lombok.Data;
import rip.snake.antivpn.commons.config.VPNConfig;
import rip.snake.antivpn.commons.utils.ConfigUtils;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Timer;

@Data
public class Service {

    public static Service INSTANCE;

    private final Path home;
    private final VPNLogger logger;
    private final Console console;
    private final String version;

    private final Timer timer;
    private final VPNConfig vpnConfig;

    private final AntiVPN antiVPN;

    public Service(VPNLogger logger, Path home, String version) {
        INSTANCE = this;

        AntiVPNConfig antiVPNConfig = AntiVPNConfig.create();

        this.timer = new Timer();
        this.logger = logger;

        this.console = new Console(antiVPNConfig, this.logger);
        this.home = Path.of(home.toString().replaceFirst("serverantivpn", "ServerAntiVPN"));
        this.version = version;

        this.vpnConfig = ConfigUtils.loadConfig(this.home.resolve("config.json"), console);
        this.vpnConfig.write(antiVPNConfig);

        this.antiVPN = AntiVPN.create(
                "ServerAntiVPN v" + version, this.logger, this.console,
                antiVPNConfig, Duration.ofSeconds(30)
        );
    }

    public void onLoad() {
        this.antiVPN.fireUp();
    }

    public void onDisable() {
        this.antiVPN.getSocketManager().close();
    }

    public boolean saveConfig() {
        return ConfigUtils.writeConfig(this.home.resolve("config.json"), this.console, this.vpnConfig);
    }
}
