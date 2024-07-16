package rip.snake.antivpn.commons.utils;

import io.antivpn.api.utils.GsonParser;
import lombok.experimental.UtilityClass;
import rip.snake.antivpn.commons.config.VPNConfig;

import io.antivpn.api.logger.Console;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@UtilityClass
public class ConfigUtils {
    public boolean writeConfig(Path config, Console console, VPNConfig vpnConfig) {
        String prettyJson = GsonParser.toPrettyJson(vpnConfig);

        try {
            // Verifying that the parent directory exists, if not, create it.
            if (!Files.exists(config.getParent())) {
                Files.createDirectories(config.getParent());
            }

            Files.writeString(config, prettyJson, StandardCharsets.UTF_8);
            return true;
        } catch (IOException e) {
            console.error("Failed to write config to %s, message: %s", config.toString(), e.getMessage());
            return false;
        }
    }

    public VPNConfig loadConfig(Path config, Console console) {
        try {
            String content = Files.readString(config, StandardCharsets.UTF_8);
            return GsonParser.fromJson(content, VPNConfig.class);
        } catch (IOException e) {
            console.error("Failed to read config from %s, writing one instead.", config.toString());

            VPNConfig vpnConfig = new VPNConfig();
            writeConfig(config, console, vpnConfig);

            return vpnConfig;
        }
    }
}
