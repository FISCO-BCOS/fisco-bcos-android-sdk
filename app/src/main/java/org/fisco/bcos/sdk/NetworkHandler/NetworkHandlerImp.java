package org.fisco.bcos.sdk.NetworkHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkHandlerImp implements NetworkHandlerInterface {

    private static Logger logger = LoggerFactory.getLogger(NetworkHandlerImp.class);

    @Override
    public String onRPCRequestCallback(String input) {
        logger.trace("onRPCRequestCallback http request body:" + input);
        return null;
    }
}