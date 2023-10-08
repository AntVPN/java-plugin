package rip.snake.antivpn.core.utils;

import lombok.experimental.UtilityClass;
import rip.snake.antivpn.core.config.VPNConfig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@UtilityClass
public class ConfigUtils {

    public boolean writeConfig(Path config, VPNConfig vpnConfig) {
        String prettyJson = GsonParser.toPrettyJson(vpnConfig);

        try {
            // Verifying that the parent directory exists, if not, create it.
            if (!Files.exists(config.getParent())) {
                Files.createDirectories(config.getParent());
            }

            Files.writeString(config, prettyJson, StandardCharsets.UTF_8);
            return true;
        } catch (IOException e) {
            Console.error("Failed to write config to %s, message: %s", config.toString(), e.getMessage());
            return false;
        }
    }

    public VPNConfig loadConfig(Path config) {
        try {
            String content = Files.readString(config, StandardCharsets.UTF_8);
            return GsonParser.fromJson(content, VPNConfig.class);
        } catch (IOException e) {
            Console.error("Failed to read config from %s, writing one instead.", config.toString());

            VPNConfig vpnConfig = new VPNConfig();
            writeConfig(config, vpnConfig);

            return vpnConfig;
        }
    }

}
