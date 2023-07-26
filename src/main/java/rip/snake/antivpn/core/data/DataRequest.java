package rip.snake.antivpn.core.data;

import lombok.Data;
import rip.snake.antivpn.core.utils.IDGenerator;

@Data
public class DataRequest {

    private String uid;
    private String address;

    public DataRequest(String address) {
        this.uid = IDGenerator.generateUniqueID();
        this.address = address;
    }

}
