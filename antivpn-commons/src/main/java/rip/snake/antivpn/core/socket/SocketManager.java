package rip.snake.antivpn.core.socket;

import org.java_websocket.framing.CloseFrame;
import rip.snake.antivpn.core.Service;
import rip.snake.antivpn.core.data.DataRequest;
import rip.snake.antivpn.core.data.DataResponse;
import rip.snake.antivpn.core.function.WatcherFunction;
import rip.snake.antivpn.core.utils.Console;
import rip.snake.antivpn.core.utils.GsonParser;
import rip.snake.antivpn.core.utils.StringUtils;

import java.net.URI;
import java.net.http.WebSocketHandshakeException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionException;

/**
 * The socket manager.
 */
public class SocketManager {

    private final Service service;
    private final SocketClient socket;

    /**
     * Initializing the socket.
     *
     * @param service The service to initialize the socket with
     */
    public SocketManager(Service service) {
        this.service = service;
        this.socket = initialize(this.service);
    }

    public void connect() {
        if (this.isConnected()) return;
        this.socket.connect();
    }

    /**
     * Closing the socket.
     */
    public void close() {
        if (this.isConnected()) return;
        this.socket.close(CloseFrame.NORMAL, "Closing");
    }

    /**
     * Verifying if an address is a VPN or Proxy.
     *
     * @param address The address to verify
     * @return The response
     */
    public WatcherFunction<DataResponse> verifyAddress(String address, String username) {
        if (!this.isConnected()) return null;

        // Clean the address
        address = StringUtils.cleanAddress(address);

        DataRequest request = new DataRequest(address, username == null ? "N/A" : username);
        this.socket.send(GsonParser.toJson(request));

        return WatcherFunction.createFunction(request.getUid());
    }

    // Initialize WebSocket
    private SocketClient initialize(Service service) {
        try {
            var connection_url = URI.create("wss://connection.antivpn.io/live_checker");

            Map<String, String> httpHeaders = new HashMap<>();

            // User-Agent and Authorization headers
            httpHeaders.put("User-Agent", "AntiVPN-Server/" + service.getVersion());
            httpHeaders.put("Authorization", "Bearer " + service.getVpnConfig().getSecret());

            return new SocketClient(connection_url, httpHeaders);
        } catch (CompletionException ex) {
            if (!(ex.getCause() instanceof WebSocketHandshakeException)) return null;

            WebSocketHandshakeException throwable = (WebSocketHandshakeException) ex.getCause();
            int statusCode = throwable.getResponse().statusCode();

            if (statusCode == 401) {
                Console.error("Failed to authenticate with the server, please check your secret in the config.json file.");
            } else if (statusCode >= 500 && statusCode <= 505) {
                Console.error("Our server is restarting or something related... If this still happening after 10 minutes please report it on discord.snake.rip. Useful data: (HttpStatus: %s)", statusCode);
            } else {
                Console.error("Report this to the developer: %s", throwable.getClass().getSimpleName());
                throwable.printStackTrace();
            }

            return null;
        }
    }


    public boolean isConnected() {
        if (this.socket == null) return false;
        return this.socket.isConnected();
    }

    public void sendPing() {
        if (!this.isConnected()) return;
        this.socket.sendPing();
    }

    public void reconnect() {
        if (this.socket.isConnecting() || this.isConnected()) return;
        Console.error("Trying to reconnect to the AntiVPN Server...");
        this.socket.reconnect();
    }
}
