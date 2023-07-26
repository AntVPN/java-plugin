package rip.snake.antivpn.core.data;

import lombok.Data;

@Data
public class DataResponse {

    private String uid;
    private boolean valid;

    public String toString() {
        return String.format("DataResponse(uid=%s, valid=%s)", uid, valid);
    }

}
