package rip.snake.antivpn.core;

import lombok.Data;
import org.slf4j.Logger;
import rip.snake.antivpn.core.function.WatcherFunction;
import rip.snake.antivpn.core.socket.SocketManager;

import java.util.HashMap;
import java.util.Map;

@Data
public class Service {

    private final Logger logger;
    private final SocketManager socketManager;


    public Service(Logger logger) {
        this.logger = logger;
        this.socketManager = new SocketManager();
    }

    public void onLoad() {
        this.socketManager.open();
    }

    public void onDisable() {
        this.socketManager.close();
    }

}
