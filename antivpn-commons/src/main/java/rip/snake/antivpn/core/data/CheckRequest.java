package rip.snake.antivpn.core.data;

import lombok.Getter;
import rip.snake.antivpn.core.utils.IDGenerator;

@Getter
public class CheckRequest extends Request {

    private final String uid;
    private final String address;
    private final String username;

    public CheckRequest(String address, String username) {
        super(RequestType.VERIFY);
        this.uid = IDGenerator.generateUniqueID();
        this.address = address;
        this.username = username;
    }

}
