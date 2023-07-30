package rip.snake.antivpn.core.tasks;

import rip.snake.antivpn.core.Service;

import java.util.TimerTask;

public class TimeoutTask extends TimerTask {

    private final Service service;

    public TimeoutTask(Service service) {
        this.service = service;
    }

    @Override
    public void run() {
        // Already connected to the socket, so ping the server.
        if (service.getSocketManager().isConnected()) {
            service.getSocketManager().sendPing();
            return;
        }

        // Not connected to the socket, so try to reconnect.
        service.getSocketManager().reconnect();
    }


}