package rip.snake.antivpn.core.socket;

import lombok.Data;
import rip.snake.antivpn.core.data.DataResponse;
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
        return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        // Parse the data
        var response = GsonParser.fromJson(data.toString(), DataResponse.class);

        try {
            // Call the function from the waiting responses
            this.socketManager.getFunction(response.getUid()).call(response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return WebSocket.Listener.super.onText(webSocket, data, last);
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Closing");
        WebSocket.Listener.super.onError(webSocket, error);
    }
}
