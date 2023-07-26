package rip.snake.antivpn.core.utils;

import javax.annotation.Nullable;

/**
 * Callback interface for async functions
 *
 * @param <T> The type of the callback
 */
public interface Callback<T> {

    /**
     * Called when the function is called
     *
     * @param calling The calling object
     * @throws Exception If an error occurs
     */
    void call(@Nullable T calling) throws Exception;

}
