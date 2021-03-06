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
package org.fisco.bcos.sdk;

import java.util.concurrent.ConcurrentHashMap;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.client.JsonRpcService;
import org.fisco.bcos.sdk.config.model.ProxyConfig;
import org.fisco.bcos.sdk.log.Logger;
import org.fisco.bcos.sdk.log.LoggerFactory;
import org.fisco.bcos.sdk.model.ConstantConfig;
import org.fisco.bcos.sdk.network.NetworkHandlerImp;
import org.fisco.bcos.sdk.network.NetworkHandlerInterface;

public class BcosSDK {
    private static Logger logger = LoggerFactory.getLogger(BcosSDK.class);
    private ConcurrentHashMap<Integer, Client> groupToClient = new ConcurrentHashMap<>();
    private ProxyConfig proxyConfig;
    private NetworkHandlerInterface networkHandler;

    public BcosSDK(ProxyConfig proxyConfig) throws BcosSDKException {
        try {
            this.proxyConfig = proxyConfig;
            Object networkHandler = proxyConfig.getNetworkHandler();
            if (networkHandler == null) {
                networkHandler = (new NetworkHandlerImp());
            }
            this.networkHandler = (NetworkHandlerInterface) networkHandler;
            logger.info("create BcosSDK successfully");
        } catch (Exception e) {
            stopAll();
            throw new BcosSDKException("create BcosSDK failed, error info: " + e.getMessage(), e);
        }
    }

    public static BcosSDK build(ProxyConfig proxyConfig) throws BcosSDKException {
        return new BcosSDK(proxyConfig);
    }

    public void checkGroupId(Integer groupId) {
        if (groupId < ConstantConfig.MIN_GROUPID || groupId > ConstantConfig.MAX_GROUPID) {
            throw new BcosSDKException(
                    "create client for group "
                            + groupId
                            + " failed for invalid groupId! The groupID must be no smaller than "
                            + ConstantConfig.MIN_GROUPID
                            + " and no more than "
                            + ConstantConfig.MAX_GROUPID);
        }
    }

    public Client getClient(Integer groupId) {
        checkGroupId(groupId);
        if (!groupToClient.containsKey(groupId)) {
            // create a new client for the specified group
            JsonRpcService jsonRpcService = new JsonRpcService(this.networkHandler);
            Client client = Client.build(groupId, jsonRpcService, this.proxyConfig);
            if (client == null) {
                throw new BcosSDKException(
                        "create client for group "
                                + groupId
                                + " failed! Please check the existence of group "
                                + groupId
                                + " of the connected node!");
            }
            client.start();
            groupToClient.put(groupId, client);
            logger.info("create client for group {} success", groupId);
        }
        return groupToClient.get(groupId);
    }

    public void stopAll() {
        // stop the client
        for (Integer groupId : groupToClient.keySet()) {
            groupToClient.get(groupId).stop();
        }
        logger.info("stop BcosSDK successfully");
    }
}
