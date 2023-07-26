package rip.snake.antivpn.bungee;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.md_5.bungee.api.plugin.Plugin;
import rip.snake.antivpn.core.Service;

@Slf4j
@Getter
public class ServerAntiVPN extends Plugin {

    private final Service service;

    public ServerAntiVPN() {
        this.service = new Service(log);
    }

    @Override
    public void onLoad() {
        this.service.onLoad();
        super.onLoad();
    }

    @Override
    public void onDisable() {
        this.service.onDisable();
        super.onDisable();
    }

}
