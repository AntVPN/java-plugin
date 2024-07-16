package rip.snake.antivpn.velocity.utils;

import io.antivpn.api.logger.VPNLogger;
import org.slf4j.Logger;

public class SharkLoggerImpl implements VPNLogger {

    private final Logger logger;

    public SharkLoggerImpl(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void log(String message, Object... args) {
        logger.info(message, args);
    }

    @Override
    public void fine(String message, Object... args) {
        logger.info(message, args);
    }

    @Override
    public void error(String message, Object... args) {
        logger.error(message, args);
    }

    @Override
    public void debug(String message, Object... args) {
        logger.info(message, args);
    }
}
