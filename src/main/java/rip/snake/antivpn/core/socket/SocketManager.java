package rip.snake.antivpn.core.socket;

import rip.snake.antivpn.core.Service;
import rip.snake.antivpn.core.data.DataRequest;
import rip.snake.antivpn.core.data.DataResponse;
import rip.snake.antivpn.core.function.WatcherFunction;
import rip.snake.antivpn.core.utils.Console;
import rip.snake.antivpn.core.utils.GsonParser;

import javax.annotation.Nullable;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;

public class SocketManager {

    private final @Nullable WebSocket socket;

    public SocketManager(Service service) {
        this.socket = initialize(service);
    }

    public void open() {
        if (this.socket == null || this.socket.isInputClosed()) return;

        this.socket.request(1);
    }

    public void close() {
        if (this.socket == null || this.socket.isInputClosed()) return;

        this.socket.sendClose(WebSocket.NORMAL_CLOSURE, "Closing");
    }

    /**
     * Verifying if an address is a VPNor Proxy.
     *
     * @param address The address to verify
     * @return The response
     */
    public WatcherFunction<DataResponse> verifyAddress(String address) {
        Console.debug("Socket: %s", this.socket == null || this.socket.isInputClosed());
        if (this.socket == null || this.socket.isInputClosed()) return null;

        DataRequest request = new DataRequest(address);
        this.socket.sendText(GsonParser.toJson(request), true);
        return WatcherFunction.createFunction(request.getUid());
    }

    // Initialize WebSocket
    private WebSocket initialize(Service service) {
        try {
            return HttpClient
                    .newBuilder()
                    .build()
                    .newWebSocketBuilder()
                    .header("User-Agent", "AntiVPN-Plugin")
                    .header("Authorization", "Bearer " + service.getVpnConfig().getSecret())
                    .buildAsync(URI.create("wss://anti.snake.rip/live_checker"), new SocketClient(this))
                    .join();
        } catch (Exception e) {
            Console.debug("The secret is invalid, please check your config.yml");
            return null;
        }
    }


}
