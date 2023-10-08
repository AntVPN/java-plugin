package rip.snake.antivpn.core.socket;

import lombok.Getter;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import rip.snake.antivpn.core.data.DataResponse;
import rip.snake.antivpn.core.function.WatchableInvoker;
import rip.snake.antivpn.core.utils.Console;
import rip.snake.antivpn.core.utils.GsonParser;

import java.net.URI;
import java.util.Map;

/**
 * This class is used to manage the socket client.
 */
@Getter
public class SocketClient extends WebSocketClient {

    private boolean connecting = true;

    public SocketClient(URI serverUri, Map<String, String> httpHeaders) {
        super(serverUri, httpHeaders);
        this.setTcpNoDelay(true);
    }

    @Override
    public void connect() {
        this.connecting = true;
        super.connect();
    }

    @Override
    public void reconnect() {
        this.connecting = true;
        super.reconnect();
    }

    @Override
    public void onMessage(String message) {
        try {
            // Parse the data
            var response = GsonParser.fromJson(message, DataResponse.class);

            WatchableInvoker<DataResponse> watcherFunction = WatchableInvoker.getWatchableInvoker(response.getUid());
            if (watcherFunction == null) return;

            watcherFunction.call(response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        this.connecting = false;
        Console.fine("Connected to the AntiVPN Server.");
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        this.connecting = false;
        if (reason == null || reason.isEmpty()) reason = "Unknown";

        Console.error("Disconnected from the AntiVPN Server. (Code: %s, Reason: %s)", code, reason);
        this.close();
    }

    @Override
    public void onError(Exception e) {
        this.connecting = false;
        Console.error("An error occurred, please report this to the developer. (Error: %s)", e.getMessage());
        e.printStackTrace();
    }

    public boolean isConnected() {
        if (this.isClosed()) return false;
        return this.isOpen() && !this.isClosing();
    }

}
