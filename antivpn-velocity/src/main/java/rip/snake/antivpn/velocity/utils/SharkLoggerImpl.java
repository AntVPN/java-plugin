package rip.snake.antivpn.velocity.utils;

import io.antivpn.api.logging.VPNLogger;
import org.slf4j.Logger;
import rip.snake.antivpn.core.utils.LoggerFactory;

public class SharkLoggerImpl {

    public static VPNLogger create(Logger logger) {
        return LoggerFactory.fromSlf4j(logger);
    }

}
