package org.fisco.bcos.sdk.log;

public class LoggerFactory {
    static Logger logger;

    public static void setLogger(Logger _logger) {
        logger = _logger;
    }

    public static Logger getLogger(Class<?> clazz) {
        String className = clazz.getName();
        if (logger == null) {
            logger = new Logger(className);
        }
        return logger;
    }
}
