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

import org.fisco.bcos.sdk.NetworkHandler.NetworkHandlerInterface;
import org.fisco.bcos.sdk.client.protocol.request.JsonRpcRequest;
import org.fisco.bcos.sdk.utils.ObjectMapperFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonRpcServiceForProxy extends JsonRpcService {

    private static Logger logger = LoggerFactory.getLogger(JsonRpcServiceForProxy.class);
    private NetworkHandlerInterface networkHandle;

    public JsonRpcServiceForProxy(NetworkHandlerInterface networkHandle) {
        this.networkHandle = networkHandle;
    }

    public String sendRequestToGroup(JsonRpcRequest request) {
        try {
            String input = ObjectMapperFactory.getObjectMapper().writeValueAsString(request);
            return networkHandle.onRPCRequestCallback(input);
        } catch (JsonProcessingException e) {
            logger.error("serialize request failed, error info: " + e.getMessage());
        }
        return null;
    }
}
