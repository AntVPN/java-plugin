package rip.snake.antivpn.core.utils;

import io.antivpn.api.logging.VPNLogger;
import lombok.experimental.UtilityClass;

import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

@UtilityClass
public class LoggerFactory {

    public static VPNLogger fromJUL(Logger logger) {
        return new VPNLogger() {
            @Override
            public void log(String message, Object... args) {
                logger.info(String.format(message, args));
            }

            @Override
            public void fine(String message, Object... args) {
                logger.info(String.format(message, args));
            }

            @Override
            public void error(String message, Object... args) {
                logger.severe(String.format(message, args));
            }

            @Override
            public void debug(String message, Object... args) {
                logger.info(String.format(message, args));
            }
        };
    }

    public static VPNLogger fromSlf4j(org.slf4j.Logger logger) {
        return new VPNLogger() {
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
        };
    }

    public static VPNLogger fromConsumer(Consumer<String> info, Consumer<String> error) {
        return new VPNLogger() {
            @Override
            public void log(String message, Object... args) {
                info.accept(String.format(message, args));
            }

            @Override
            public void fine(String message, Object... args) {
                info.accept(String.format(message, args));
            }

            @Override
            public void error(String message, Object... args) {
                error.accept(String.format(message, args));
            }

            @Override
            public void debug(String message, Object... args) {
                info.accept(String.format(message, args));
            }
        };
    }

}
