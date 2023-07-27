package rip.snake.antivpn.core.socket;

import rip.snake.antivpn.core.Service;
import rip.snake.antivpn.core.data.DataRequest;
import rip.snake.antivpn.core.data.DataResponse;
import rip.snake.antivpn.core.function.WatcherFunction;
import rip.snake.antivpn.core.utils.Console;
import rip.snake.antivpn.core.utils.GsonParser;
import rip.snake.antivpn.core.utils.StringUtils;

import javax.annotation.Nullable;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.net.http.WebSocketHandshakeException;
import java.nio.ByteBuffer;

/**
 * The socket manager.
 */
public class SocketManager {

    private static final String PING = "PING";

    private final Service service;
    private @Nullable WebSocket socket;

    /**
     * Initializing the socket.
     *
     * @param service The service to initialize the socket with
     */
    public SocketManager(Service service) {
        this.service = service;
        this.socket = initialize(service);
    }

    /**
     * Closing the socket.
     */
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
        if (this.socket == null || this.socket.isInputClosed()) return null;

        // Clean the address
        address = StringUtils.cleanAddress(address);

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
            e.printStackTrace();
            Console.error("Your secret is invalid or ");
            return null;
        }
    }


    public boolean isConnected() {
        boolean isOffline = this.socket == null || this.socket.isInputClosed();
        return !isOffline;
    }

    public void sendPing() {
        if (this.socket == null || this.socket.isInputClosed()) return;
        this.socket.sendPing(ByteBuffer.wrap(PING.getBytes()));
    }

    public void reconnect() {
        if (isConnected()) return;
        Console.debug("Reconnecting socket");
        this.socket = initialize(this.service);
    }
}
