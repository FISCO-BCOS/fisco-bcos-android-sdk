package org.fisco.bcos.sdk.NetworkHandler;

public interface NetworkHandlerInterface {

    void setIpAndPort(String ipPort);

    String onRPCRequest(String requestBodyJsonStr);
}
