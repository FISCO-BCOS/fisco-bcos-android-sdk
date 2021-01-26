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

import org.fisco.bcos.sdk.NetworkHandler.NetworkHandlerImp;
import org.fisco.bcos.sdk.NetworkHandler.NetworkHandlerInterface;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.client.JsonRpcServiceForProxy;
import org.fisco.bcos.sdk.config.model.ProxyConfig;
import org.fisco.bcos.sdk.model.ConstantConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

public class BcosSDKForProxy {

    private static Logger logger = LoggerFactory.getLogger(BcosSDKForProxy.class);

    private ConcurrentHashMap<Integer, Client> groupToClient = new ConcurrentHashMap<>();
    private ProxyConfig proxyConfig;
    private NetworkHandlerInterface networkHandler;

    public BcosSDKForProxy(ProxyConfig proxyConfig) throws BcosSDKException {
        try {
            this.proxyConfig = proxyConfig;
            Object networkHandler = proxyConfig.getNetworkHandler();
            if (networkHandler == null) {
                networkHandler = (new NetworkHandlerImp());
            }
            this.networkHandler = (NetworkHandlerImp) networkHandler;
            logger.info("create BcosSDKForProxy successfully");
        } catch (Exception e) {
            stopAll();
            throw new BcosSDKException("create BcosSDK failed, error info: " + e.getMessage(), e);
        }
    }

    public static BcosSDKForProxy build(ProxyConfig proxyConfig) throws BcosSDKException {
        return new BcosSDKForProxy(proxyConfig);
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
            JsonRpcServiceForProxy jsonRpcServiceForProxy = new JsonRpcServiceForProxy(this.networkHandler);
            Client client =
                    Client.build(groupId,
                            jsonRpcServiceForProxy,
                            this.proxyConfig);
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
        logger.info("stop BcosSDKForProxy successfully");
    }
}
