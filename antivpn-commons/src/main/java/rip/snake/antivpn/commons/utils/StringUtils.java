package rip.snake.antivpn.commons.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class StringUtils {

    public String cleanAddress(String address) {
        return address.replaceFirst(":[0-9]+", "").replaceFirst("^/", "");
    }

}
