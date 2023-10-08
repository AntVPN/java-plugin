package rip.snake.antivpn.core.data;

import lombok.Getter;

@Getter
public class UserDataRequest extends Request {

    private final String username;
    private final String uniqueId;
    private final String address;
    private final boolean premium;

    public UserDataRequest(String username, String uniqueId, String address, boolean premium) {
        super(RequestType.USER_DATA);
        this.username = username;
        this.uniqueId = uniqueId;
        this.address = address;
        this.premium = premium;
    }

}
