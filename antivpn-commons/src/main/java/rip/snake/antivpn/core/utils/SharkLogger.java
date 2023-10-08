package rip.snake.antivpn.core.utils;

/**
 * This is a logger interface that is used to log messages to the console.
 */
public interface SharkLogger {

    /**
     * Log a message to the console
     *
     * @param message The message to log
     * @param args    The arguments to replace in the message
     */
    void log(String message, Object... args);

    /**
     * Log a message to the console with the fine level
     *
     * @param message The message to log
     * @param args    The arguments to replace in the message
     */
    void fine(String message, Object... args);

    /**
     * Log a message to the console with the error level
     *
     * @param message The message to log
     * @param args    The arguments to replace in the message
     */
    void error(String message, Object... args);

    /**
     * Log a message to the console with the debug level
     *
     * @param message The message to log
     * @param args    The arguments to replace in the message
     */
    void debug(String message, Object... args);

}
