package rip.snake.antivpn.core.socket;

import lombok.Data;
import rip.snake.antivpn.core.data.DataResponse;
import rip.snake.antivpn.core.function.WatcherFunction;
import rip.snake.antivpn.core.utils.Console;
import rip.snake.antivpn.core.utils.GsonParser;

import java.net.http.WebSocket;
import java.util.concurrent.CompletionStage;

/**
 * This class is used to manage the socket client.
 */
@Data
public class SocketClient implements WebSocket.Listener {

    private final SocketManager socketManager;

    public SocketClient(SocketManager socketManager) {
        this.socketManager = socketManager;
    }

    @Override
    public void onOpen(WebSocket webSocket) {
        WebSocket.Listener.super.onOpen(webSocket);
    }

    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
        if (statusCode == 401 || statusCode == 402) {
            Console.error("Please use a valid API key in the config.yml file.");
            return null;
        }

        Console.log("Socket closed with status code %d: %s", statusCode, reason);
        return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        // Parse the data
        var response = GsonParser.fromJson(data.toString(), DataResponse.class);
        Console.debug("RECEIVED: %s", data);

        try {
            WatcherFunction<DataResponse> watcherFunction = WatcherFunction.getWatcherFunction(response.getUid());

            Console.debug("WatcherFunction: %s", watcherFunction);
            if (watcherFunction == null) return WebSocket.Listener.super.onText(webSocket, data, last);

            Console.debug("Calling Function");
            watcherFunction.call(response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return WebSocket.Listener.super.onText(webSocket, data, last);
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        Console.error("An error occurred in the socket: " + error.getMessage());
        WebSocket.Listener.super.onError(webSocket, error);
    }
}
