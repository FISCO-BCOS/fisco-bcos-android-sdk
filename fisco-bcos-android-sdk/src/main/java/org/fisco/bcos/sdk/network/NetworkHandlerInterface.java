package org.fisco.bcos.sdk.network;

public interface NetworkHandlerInterface {

    void setIpAndPort(String ipPort);

    String onRPCRequest(String requestBodyJsonStr);
}
