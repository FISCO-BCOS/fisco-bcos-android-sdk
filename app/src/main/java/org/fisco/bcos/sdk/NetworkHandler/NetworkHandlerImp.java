package org.fisco.bcos.sdk.NetworkHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ConnectException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NetworkHandlerImp implements NetworkHandlerInterface {

    private static Logger logger = LoggerFactory.getLogger(NetworkHandlerImp.class);

    @Override
    public String onRPCRequest(String requestBodyJsonStr) {
        logger.trace("onRPCRequest http request body: " + requestBodyJsonStr);

        OkHttpClient okHttpClient = new OkHttpClient();
        final String URL = "http://127.0.0.1:8170/Bcos-node-proxy/rpc/v1";
        MediaType JSON = MediaType.parse("application/json;charset=utf-8");
        RequestBody requestBody = RequestBody.create(JSON, requestBodyJsonStr);
        Request request = new Request.Builder()
                .url(URL)
                .post(requestBody)
                .build();
        try {
            Response response = okHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                String responseBodyJsonStr = response.body().string();
                logger.trace("onRPCRequest http response body: " + responseBodyJsonStr);
                return responseBodyJsonStr;
            }
        } catch (ConnectException e) {
            logger.error("onRPCRequest failed, error info: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}