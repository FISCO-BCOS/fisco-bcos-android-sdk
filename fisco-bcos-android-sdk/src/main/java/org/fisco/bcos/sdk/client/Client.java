/*
 * Copyright 2014-2020  [fisco-dev]
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package org.fisco.bcos.sdk.client;

import java.math.BigInteger;
import java.util.Arrays;
import org.fisco.bcos.sdk.client.protocol.request.JsonRpcMethods;
import org.fisco.bcos.sdk.client.protocol.request.JsonRpcRequest;
import org.fisco.bcos.sdk.client.protocol.request.Transaction;
import org.fisco.bcos.sdk.client.protocol.response.BcosTransaction;
import org.fisco.bcos.sdk.client.protocol.response.BcosTransactionReceipt;
import org.fisco.bcos.sdk.client.protocol.response.BlockNumber;
import org.fisco.bcos.sdk.client.protocol.response.Call;
import org.fisco.bcos.sdk.config.model.ProxyConfig;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.log.Logger;
import org.fisco.bcos.sdk.log.LoggerFactory;
import org.fisco.bcos.sdk.model.CryptoType;
import org.fisco.bcos.sdk.model.NetworkResponse;
import org.fisco.bcos.sdk.model.NodeVersion;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.network.model.NetworkResponseCode;

/**
 * This is the interface of client module.
 *
 * @author chaychen
 */
public interface Client {
    static Logger logger = LoggerFactory.getLogger(Client.class);

    static Client build(Integer groupId, JsonRpcService jsonRpcService, ProxyConfig proxyConfig) {
        int cryptoTypeProxyConfig = proxyConfig.getCryptoType();
        // get crypto type from nodeVersion
        Integer cryptoType = CryptoType.ECDSA_TYPE;
        JsonRpcRequest jsonRpcRequest =
                new JsonRpcRequest(JsonRpcMethods.GET_NODE_VERSION, Arrays.asList(groupId));
        NetworkResponse<NodeVersion> version =
                jsonRpcService.sendRequestToGroupByProxy(jsonRpcRequest, NodeVersion.class);
        try {
            if (version.getCode() == NetworkResponseCode.SuccessCode) {
                NodeVersion nodeVersion = version.getResult();
                if (nodeVersion.getNodeVersion().getVersion().contains("gm")) {
                    cryptoType = CryptoType.SM_TYPE;
                }
                if (cryptoTypeProxyConfig != cryptoType) {
                    logger.error(
                            "crypto in sdk and in node are different, sdk: "
                                    + cryptoTypeProxyConfig
                                    + ", node: "
                                    + cryptoType);
                    return null;
                }
                String chainIdProxyConfig = proxyConfig.getChainId();
                if (!chainIdProxyConfig.equals(nodeVersion.getNodeVersion().getChainId())) {
                    logger.error(
                            "chainId in sdk and in node are different, sdk: "
                                    + chainIdProxyConfig
                                    + ", node: "
                                    + nodeVersion.getNodeVersion().getChainId());
                    return null;
                }
                CryptoSuite cryptoSuite = new CryptoSuite(cryptoType);
                String privateKey = proxyConfig.getHexPrivateKey();
                if (privateKey != null && !privateKey.isEmpty()) {
                    cryptoSuite.derivePublicKey(privateKey);
                } else {
                    cryptoSuite.createKeyPair();
                }
                logger.info("created address: " + cryptoSuite.getCryptoKeyPair().getAddress());
                return new ClientImpl(groupId, cryptoSuite, nodeVersion, jsonRpcService);
            } else {
                logger.error("get node version failed, error info: " + version.getMessage());
            }
        } catch (Exception e) {
            logger.error("build client failed, error info: " + e.getMessage());
            // e.printStackTrace();
        }
        return null;
    }

    CryptoSuite getCryptoSuite();

    NodeVersion getClientNodeVersion();

    Integer getCryptoType();

    /**
     * get groupId of the client
     *
     * @return the groupId
     */
    Integer getGroupId();

    /**
     * Ledger operation: get block number
     *
     * @return block number
     */
    BlockNumber getBlockNumber();

    /**
     * Get cached block height
     *
     * @return block number
     */
    BigInteger getBlockLimit();

    /**
     * Ledger operation: get trnasaction by hash
     *
     * @param transactionHash the hashcode of transaction
     * @return transaction
     */
    BcosTransaction getTransactionByHash(String transactionHash);

    /**
     * send transaction and get the receipt as the response
     *
     * @param signedTransactionData the transaction data sent to the node
     * @return the transaction receipt
     */
    TransactionReceipt sendRawTransactionAndGetReceipt(String signedTransactionData);

    /**
     * Ledger operation: call contract functions without sending transaction
     *
     * @param transaction transaction instance
     * @return Call
     */
    Call call(Transaction transaction);

    /**
     * Ledger operation: get transaction receipt by transaction hash
     *
     * @param transactionHash the hashcode of transaction
     * @return transaction receipt
     */
    BcosTransactionReceipt getTransactionReceipt(String transactionHash);

    /**
     * Peer operation: get node version
     *
     * @return node version
     */
    NodeVersion getNodeVersion();

    void start();

    void stop();
}
