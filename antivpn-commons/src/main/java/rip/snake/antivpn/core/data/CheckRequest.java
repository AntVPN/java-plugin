package rip.snake.antivpn.core.data;

import lombok.Data;
import rip.snake.antivpn.core.utils.IDGenerator;

@Data
public class CheckRequest {

    private String uid;
    private String address;
    private String username;

    public CheckRequest(String address, String username) {
        this.uid = IDGenerator.generateUniqueID();
        this.address = address;
        this.username = username;
    }

}