package rip.snake.antivpn.core.socket;

import com.google.gson.JsonObject;
import lombok.Getter;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import rip.snake.antivpn.core.Service;
import rip.snake.antivpn.core.data.CheckResponse;
import rip.snake.antivpn.core.data.ResponseType;
import rip.snake.antivpn.core.data.SettingsResponse;
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
    private Service service;

    public SocketClient(Service service, URI serverUri, Map<String, String> httpHeaders) {
        super(serverUri, httpHeaders);
        this.setTcpNoDelay(true);
        this.service = service;
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
            JsonObject object = GsonParser.parse(message);

            if (!object.has("type")) {
                Console.error("Received invalid message from the AntiVPN Server. (Message: %s)", message);
                return;
            }

            if (object.get("type").getAsString().equalsIgnoreCase(ResponseType.SETTINGS.name())) {
                JsonObject settingsObject = object.get("data").getAsJsonObject();
                SettingsResponse response = GsonParser.fromJson(settingsObject, SettingsResponse.class);

                // Update the settings
                this.service.getVpnConfig().setDetectMessage(response.getKickMessage());
                this.service.saveConfig();

                Console.fine("Received settings from the AntiVPN Server.");
            } else if (object.get("type").getAsString().equalsIgnoreCase(ResponseType.CHECK.name())) {
                // Parse the data
                var response = GsonParser.fromJson(message, CheckResponse.class);

                WatchableInvoker<CheckResponse> watcherFunction = WatchableInvoker.getWatchableInvoker(response.getUid());
                if (watcherFunction == null) return;

                watcherFunction.call(response);
            } else {
                Console.error("Received invalid message from the AntiVPN Server. (Message: %s)", message);
            }
        } catch (Exception e) {
            Console.error("An error occurred while parsing the message from the AntiVPN Server. (Message: %s)", message);
            Console.error("Error: %s", e.getMessage());
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
