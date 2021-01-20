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

import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.fisco.bcos.sdk.NetworkHandler.model.NetworkResponseCode;
import org.fisco.bcos.sdk.client.protocol.request.JsonRpcMethods;
import org.fisco.bcos.sdk.client.protocol.request.JsonRpcRequest;
import org.fisco.bcos.sdk.client.protocol.request.Transaction;
import org.fisco.bcos.sdk.client.protocol.response.BlockNumber;
import org.fisco.bcos.sdk.client.protocol.response.Call;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.model.NetworkResponse;
import org.fisco.bcos.sdk.model.NodeVersion;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.utils.ThreadPoolService;

public class ClientImplForProxy extends ClientImpl {

    private JsonRpcServiceForProxy jsonRpcServiceForProxy;

    private BigInteger curBlockNum = BigInteger.ZERO;
    private ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1);
    private static long getBlockNumberInterval = 2000;
    private static BigInteger blockLimit = BigInteger.valueOf(500);

    public ClientImplForProxy(
            Integer groupId,
            CryptoSuite cryptoSuite,
            NodeVersion nodeVersion,
            JsonRpcServiceForProxy jsonRpcServiceForProxy) {
        this.groupId = groupId;
        this.cryptoSuite = cryptoSuite;
        this.nodeVersion = nodeVersion;
        this.jsonRpcServiceForProxy = jsonRpcServiceForProxy;
    }

    @Override
    public void start() {
        startPeriodTask();
    }

    private void startPeriodTask() {
        // periodically getBlockNumber, default period : 2s
        scheduledExecutorService.scheduleAtFixedRate(
                () -> updateBlockNumber(), 0, getBlockNumberInterval, TimeUnit.MILLISECONDS);
    }

    @Override
    public NodeVersion getNodeVersion() {
        NetworkResponse<NodeVersion> networkResponse = getNodeVersionByProxy();
        return networkResponse.getResult();
    }

    public NetworkResponse<NodeVersion> getNodeVersionByProxy() {
        JsonRpcRequest jsonRpcRequest = new JsonRpcRequest(
                JsonRpcMethods.GET_NODE_VERSION,
                Arrays.asList(groupId));

        String responseStr = this.jsonRpcServiceForProxy.sendRequestToGroup(jsonRpcRequest);
        int code = 0;
        String message = "success";
        NodeVersion nodeVersion = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map responseMap = objectMapper.readValue(responseStr, Map.class);
            code = (int)responseMap.get("code");
            message = (String) responseMap.get("message");
            if (code == NetworkResponseCode.SuccessCode) {
                Map dataMap = (Map)responseMap.get("data");
                nodeVersion = objectMapper.readValue(objectMapper.writeValueAsString(dataMap), NodeVersion.class);
            } else {
                logger.error("get node version failed, error info: " + message);
            }
        } catch (Exception e) {
            logger.error("get node version failed, error info: " + e.getMessage());
        }
        return new NetworkResponse(code, message, nodeVersion);
    }

    public NetworkResponse<BlockNumber> getBlockNumberByProxy() {
        JsonRpcRequest jsonRpcRequest = new JsonRpcRequest(
                JsonRpcMethods.GET_BLOCK_NUMBER,
                Arrays.asList(groupId));

        String responseStr = this.jsonRpcServiceForProxy.sendRequestToGroup(jsonRpcRequest);
        int code = 0;
        String message = "success";
        BlockNumber blockNumber = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map responseMap = objectMapper.readValue(responseStr, Map.class);
            code = (int)responseMap.get("code");
            message = (String) responseMap.get("message");
            if (code == NetworkResponseCode.SuccessCode) {
                Map dataMap = (Map)responseMap.get("data");
                blockNumber = objectMapper.readValue(objectMapper.writeValueAsString(dataMap), BlockNumber.class);
            } else {
                logger.error("get block number failed, error info: " + message);
            }
        } catch (Exception e) {
            logger.error("get block number failed, error info: " + e.getMessage());
        }
        return new NetworkResponse(code, message, blockNumber);
    }

    private void updateBlockNumber() {
        NetworkResponse<BlockNumber> response = getBlockNumberByProxy();
        if (response.getCode() == NetworkResponseCode.SuccessCode) {
            curBlockNum = response.getResult().getBlockNumber();
        } else {
            logger.error("updateBlockNumber failed, error info: " + response.getMessage());
        }
    }

    @Override
    public BigInteger getBlockLimit() {
        return curBlockNum.add(blockLimit);
    }

    @Override
    public TransactionReceipt sendRawTransactionAndGetReceipt(String signedTransactionData) {
        NetworkResponse<TransactionReceipt> networkResponse = sendRawTransactionAndGetReceiptByProxy(signedTransactionData);
        return networkResponse.getResult();
    }

    public NetworkResponse<TransactionReceipt> sendRawTransactionAndGetReceiptByProxy(String signedTransactionData) {
        JsonRpcRequest jsonRpcRequest = new JsonRpcRequest(
                JsonRpcMethods.SEND_RAWTRANSACTION,
                Arrays.asList(this.groupId, signedTransactionData));

        String responseStr = this.jsonRpcServiceForProxy.sendRequestToGroup(jsonRpcRequest);
        int code = 0;
        String message = "success";
        TransactionReceipt receipt = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map responseMap = objectMapper.readValue(responseStr, Map.class);
            code = (int)responseMap.get("code");
            message = (String) responseMap.get("message");
            if (code == NetworkResponseCode.SuccessCode) {
                Map dataMap = (Map)responseMap.get("data");
                receipt = objectMapper.readValue(objectMapper.writeValueAsString(dataMap), TransactionReceipt.class);
            } else {
                logger.error("sendRawTransactionAndGetReceiptByProxy failed, error info: " + message);
            }
        } catch (Exception e) {
            logger.error("sendRawTransactionAndGetReceiptByProxy failed, error info: " + e.getMessage());
        }
        return new NetworkResponse(code, message, receipt);
    }

    @Override
    public Call call(Transaction transaction) {
        NetworkResponse<Call> networkResponse = callByProxy(transaction);
        return networkResponse.getResult();
    }

    public NetworkResponse<Call> callByProxy(Transaction transaction) {
        JsonRpcRequest jsonRpcRequest = new JsonRpcRequest(
                JsonRpcMethods.CALL,
                Arrays.asList(this.groupId, transaction));

        String responseStr = this.jsonRpcServiceForProxy.sendRequestToGroup(jsonRpcRequest);
        int code = 0;
        String message = "success";
        Call callResult = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map responseMap = objectMapper.readValue(responseStr, Map.class);
            code = (int)responseMap.get("code");
            message = (String) responseMap.get("message");
            if (code == NetworkResponseCode.SuccessCode) {
                Map dataMap = (Map)responseMap.get("data");
                callResult = objectMapper.readValue(objectMapper.writeValueAsString(dataMap), Call.class);
            } else {
                logger.error("callByProxy failed, error info: " + message);
            }
        } catch (Exception e) {
            logger.error("callByProxy failed, error info: " + e.getMessage());
        }
        return new NetworkResponse(code, message, callResult);
    }

    @Override
    public void stop() {
        ThreadPoolService.stopThreadPool(scheduledExecutorService);
    }
}
