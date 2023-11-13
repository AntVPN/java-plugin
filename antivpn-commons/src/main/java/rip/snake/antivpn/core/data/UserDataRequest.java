package rip.snake.antivpn.core.data;

import lombok.Getter;

@Getter
public class UserDataRequest extends Request {

    private final String username;
    private final String uniqueId;
    private final String address;
    private final String server;
    private final String version;
    private final boolean connected;
    private final boolean premium;

    public UserDataRequest(String username, String uniqueId, String version, String address, String server, boolean connected, boolean premium) {
        super(RequestType.USER_DATA);
        this.username = username;
        this.uniqueId = uniqueId;
        this.version = version;
        this.address = address;
        this.server = server;
        this.connected = connected;
        this.premium = premium;
    }

}
