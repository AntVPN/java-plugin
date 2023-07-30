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
import java.util.concurrent.CompletionException;

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
    public WatcherFunction<DataResponse> verifyAddress(String address, @Nullable String username) {
        if (this.socket == null || this.socket.isInputClosed()) return null;

        // Clean the address
        address = StringUtils.cleanAddress(address);

        DataRequest request = new DataRequest(address, username == null ? "N/A" : username);
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
        boolean isOffline = this.socket == null || this.socket.isInputClosed();
        return !isOffline;
    }

    public void sendPing() {
        if (this.socket == null || this.socket.isInputClosed()) return;
        this.socket.sendPing(ByteBuffer.wrap(PING.getBytes()));
    }

    public void reconnect() {
        if (isConnected()) return;
        Console.error("Trying to reconnect to the socket...");
        this.socket = initialize(this.service);
    }
}
