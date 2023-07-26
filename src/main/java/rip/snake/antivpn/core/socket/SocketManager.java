package rip.snake.antivpn.core.socket;

import lombok.SneakyThrows;
import rip.snake.antivpn.core.data.DataRequest;
import rip.snake.antivpn.core.data.DataResponse;
import rip.snake.antivpn.core.function.WatcherFunction;
import rip.snake.antivpn.core.utils.GsonParser;

import javax.net.ssl.SSLContext;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

public class SocketManager {

    private final WebSocket socket;
    private final Map<String, WatcherFunction<DataResponse>> waitingResponses = new HashMap<>();

    @SneakyThrows
    public SocketManager() {
        var sslContext = SSLContext.getInstance("TLSv1.2");
        sslContext.init(null, null, new SecureRandom());

        this.socket = HttpClient
                .newBuilder()
                .sslContext(sslContext)
                .build()
                .newWebSocketBuilder()
                .header("User-Agent", "AntiVPN-Plugin")
                .header("Authorization", "Bearer " + System.getenv("SOCKET_TOKEN"))
                .buildAsync(URI.create("wss://anti.snake.rip/live_checker"), new SocketClient(this))
                .join();
    }

    public void open() {
        this.socket.request(1);
    }

    public void close() {
        this.socket.sendClose(WebSocket.NORMAL_CLOSURE, "Closing");
    }

    /**
     * Verifying if an address is a VPN or Proxy.
     *
     * @param address The address to verify
     * @return The response
     */
    public WatcherFunction<DataResponse> verifyAddress(String address) {
        if (this.socket.isInputClosed()) return null;

        DataRequest request = new DataRequest(address);
        this.socket.sendText(GsonParser.toJson(request), true);
        return this.createFunction(request.getUid());
    }

    /**
     * Get the function from the waiting responses.
     *
     * @param uid The uid of the request
     * @return The function
     */
    public WatcherFunction<DataResponse> getFunction(String uid) {
        // get and remove at the same time
        return this.waitingResponses.remove(uid);
    }

    /**
     * Create a function and put it in the waiting responses.
     *
     * @param uid The uid of the request
     * @return The function
     */
    private WatcherFunction<DataResponse> createFunction(String uid) {
        var function = new WatcherFunction<DataResponse>();
        this.waitingResponses.put(uid, function);
        return function;
    }


}
