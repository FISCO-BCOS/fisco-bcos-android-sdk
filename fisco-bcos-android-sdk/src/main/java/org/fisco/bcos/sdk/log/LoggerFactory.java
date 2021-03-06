package org.fisco.bcos.sdk.log;

public class LoggerFactory {
    static Logger logger;

    public static void setLogger(Logger _logger) {
        logger = _logger;
    }

    public static Logger getLogger(Class<?> clazz) {
        if (logger != null) {
            return logger;
        } else {
            return new Logger(clazz.getName());
        }
    }
}
