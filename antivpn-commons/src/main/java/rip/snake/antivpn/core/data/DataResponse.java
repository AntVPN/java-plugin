package rip.snake.antivpn.core.data;

import lombok.Data;

@Data
public class DataResponse {

    private String uid;
    // Returns if the IP is a player or not.
    private boolean valid;

    public String toString() {
        return String.format("DataResponse(uid=%s, valid=%s)", uid, valid);
    }

}
