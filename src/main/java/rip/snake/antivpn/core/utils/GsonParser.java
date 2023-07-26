package rip.snake.antivpn.core.utils;

import com.google.gson.Gson;
import lombok.experimental.UtilityClass;

/**
 * This class is used to parse json using Google's Gson library.
 */
@UtilityClass
public class GsonParser {

    /**
     * The Gson instance.
     */
    private final Gson gson = new Gson();

    /**
     * Parse a json string to a class.
     *
     * @param json  the json string.
     * @param clazz the class.
     * @param <T>   the class type.
     * @return the class instance.
     */
    public <T> T fromJson(String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }

    /**
     * Parse a json string to a class.
     *
     * @param object the object.
     * @return the json string.
     */
    public String toJson(Object object) {
        return gson.toJson(object);
    }

}
