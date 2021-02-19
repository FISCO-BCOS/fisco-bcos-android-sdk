/**
 * Copyright 2014-2020 [fisco-dev]
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fisco.bcos.sdk.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.fisco.bcos.sdk.NetworkHandler.NetworkHandlerInterface;
import org.fisco.bcos.sdk.NetworkHandler.model.NetworkResponseCode;
import org.fisco.bcos.sdk.client.exceptions.ClientException;
import org.fisco.bcos.sdk.client.exceptions.NetworkHandlerException;
import org.fisco.bcos.sdk.client.protocol.request.JsonRpcRequest;
import org.fisco.bcos.sdk.log.Logger;
import org.fisco.bcos.sdk.log.LoggerFactory;
import org.fisco.bcos.sdk.model.JsonRpcResponse;
import org.fisco.bcos.sdk.model.NetworkResponse;
import org.fisco.bcos.sdk.utils.ObjectMapperFactory;

public class JsonRpcServiceForProxy extends JsonRpcService {

    private static Logger logger = LoggerFactory.getLogger(JsonRpcServiceForProxy.class);
    private NetworkHandlerInterface networkHandle;

    public JsonRpcServiceForProxy(NetworkHandlerInterface networkHandle) {
        this.networkHandle = networkHandle;
    }

    public <T extends JsonRpcResponse> NetworkResponse<T> sendRequestToGroupByProxy(
            JsonRpcRequest request, Class<T> responseType) {
        try {
            String requestBodyJsonStr =
                    ObjectMapperFactory.getObjectMapper().writeValueAsString(request);
            logger.info("sendRequestToGroupByProxy, request body: " + requestBodyJsonStr);
            String responseBodyJsonStr = networkHandle.onRPCRequest(requestBodyJsonStr);
            logger.info("sendRequestToGroupByProxy, response body: " + responseBodyJsonStr);
            if (responseBodyJsonStr == null) {
                NetworkResponseCode errorInfo =
                        new NetworkResponseCode(
                                NetworkResponseCode.RespNullCode,
                                NetworkResponseCode.RespNullMessage);
                String errorStr =
                        ObjectMapperFactory.getObjectMapper().writeValueAsString(errorInfo);
                throw new NetworkHandlerException(errorStr);
            }
            return parseNetworkResponse(request, responseBodyJsonStr, responseType);
        } catch (JsonProcessingException e) {
            logger.error("serialize request failed, error info: " + e.getMessage());
        }
        return null;
    }

    private <T extends JsonRpcResponse> NetworkResponse<T> parseNetworkResponse(
            JsonRpcRequest request, String responseStr, Class<T> responseType) {
        int code = NetworkResponseCode.SuccessCode;
        String message = NetworkResponseCode.SuccessMessage;
        T entity = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map responseMap = objectMapper.readValue(responseStr, Map.class);
            code = (int) responseMap.get(NetworkResponseCode.CODE);
            message = (String) responseMap.get(NetworkResponseCode.MESSAGE);
            if (code == NetworkResponseCode.SuccessCode) {
                Map dataMap = (Map) responseMap.get(NetworkResponseCode.DATA);
                entity =
                        objectMapper.readValue(
                                objectMapper.writeValueAsString(dataMap), responseType);
            } else {
                logger.error(
                        "parseNetworkResponse failed for request "
                                + request.getMethod()
                                + ", error info: "
                                + message);
                NetworkResponseCode errorInfo = new NetworkResponseCode(code, message);
                String errorStr =
                        ObjectMapperFactory.getObjectMapper().writeValueAsString(errorInfo);
                throw new NetworkHandlerException(errorStr);
            }
        } catch (Exception e) {
            logger.error(
                    "parseNetworkResponse failed for request "
                            + request.getMethod()
                            + ", exception info: "
                            + e.getMessage());
            throw new ClientException(
                    "parseNetworkResponse failed for request "
                            + request.getMethod()
                            + ", exception info: "
                            + e.getMessage());
        }

        return new NetworkResponse(code, message, entity);
    }
}
