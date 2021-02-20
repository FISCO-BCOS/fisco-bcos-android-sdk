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

import java.math.BigInteger;
import java.util.Arrays;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.fisco.bcos.sdk.client.protocol.request.JsonRpcMethods;
import org.fisco.bcos.sdk.client.protocol.request.JsonRpcRequest;
import org.fisco.bcos.sdk.client.protocol.request.Transaction;
import org.fisco.bcos.sdk.client.protocol.response.BcosTransaction;
import org.fisco.bcos.sdk.client.protocol.response.BcosTransactionReceipt;
import org.fisco.bcos.sdk.client.protocol.response.BlockNumber;
import org.fisco.bcos.sdk.client.protocol.response.Call;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.model.NetworkResponse;
import org.fisco.bcos.sdk.model.NodeVersion;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.network.model.NetworkResponseCode;
import org.fisco.bcos.sdk.utils.ThreadPoolService;

public class ClientImpl implements Client {
    private JsonRpcService jsonRpcService;
    private Integer groupId;
    private Integer DefaultGroupId = Integer.valueOf(1);
    private CryptoSuite cryptoSuite;
    private NodeVersion nodeVersion;

    private static long getBlockNumberInterval = 60000;
    private static BigInteger blockLimit = BigInteger.valueOf(500);
    private BigInteger curBlockNum = BigInteger.ZERO;
    private ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1);

    public ClientImpl(
            Integer groupId,
            CryptoSuite cryptoSuite,
            NodeVersion nodeVersion,
            JsonRpcService jsonRpcService) {
        this.groupId = groupId;
        this.cryptoSuite = cryptoSuite;
        this.nodeVersion = nodeVersion;
        this.jsonRpcService = jsonRpcService;
        getBlockNumber();
    }

    @Override
    public CryptoSuite getCryptoSuite() {
        return this.cryptoSuite;
    }

    @Override
    public NodeVersion getClientNodeVersion() {
        return this.nodeVersion;
    }

    @Override
    public Integer getCryptoType() {
        return this.cryptoSuite.getCryptoTypeConfig();
    }

    @Override
    public Integer getGroupId() {
        return this.groupId;
    }

    @Override
    public void start() {
        startPeriodTask();
    }

    private void startPeriodTask() {
        // periodically updateBlockNumber, default period : 60s
        scheduledExecutorService.scheduleAtFixedRate(
                () -> updateBlockNumber(), 0, getBlockNumberInterval, TimeUnit.MILLISECONDS);
    }

    @Override
    public NodeVersion getNodeVersion() {
        NetworkResponse<NodeVersion> networkResponse = getNodeVersionByProxy();
        return networkResponse.getResult();
    }

    public NetworkResponse<NodeVersion> getNodeVersionByProxy() {
        JsonRpcRequest jsonRpcRequest =
                new JsonRpcRequest(JsonRpcMethods.GET_NODE_VERSION, Arrays.asList(groupId));

        return this.jsonRpcService.sendRequestToGroupByProxy(jsonRpcRequest, NodeVersion.class);
    }

    @Override
    public BlockNumber getBlockNumber() {
        NetworkResponse<BlockNumber> networkResponse = getBlockNumberByProxy();
        curBlockNum = networkResponse.getResult().getBlockNumber();
        return networkResponse.getResult();
    }

    public NetworkResponse<BlockNumber> getBlockNumberByProxy() {
        JsonRpcRequest jsonRpcRequest =
                new JsonRpcRequest(JsonRpcMethods.GET_BLOCK_NUMBER, Arrays.asList(groupId));

        return this.jsonRpcService.sendRequestToGroupByProxy(jsonRpcRequest, BlockNumber.class);
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
        NetworkResponse<TransactionReceipt> networkResponse =
                sendRawTransactionAndGetReceiptByProxy(signedTransactionData);
        return networkResponse.getResult();
    }

    public NetworkResponse<TransactionReceipt> sendRawTransactionAndGetReceiptByProxy(
            String signedTransactionData) {
        JsonRpcRequest jsonRpcRequest =
                new JsonRpcRequest(
                        JsonRpcMethods.SEND_RAWTRANSACTION,
                        Arrays.asList(this.groupId, signedTransactionData));

        NetworkResponse<BcosTransactionReceipt> receipt =
                this.jsonRpcService.sendRequestToGroupByProxy(
                        jsonRpcRequest, BcosTransactionReceipt.class);
        return new NetworkResponse(
                receipt.getCode(),
                receipt.getMessage(),
                receipt.getResult().getTransactionReceipt());
    }

    @Override
    public Call call(Transaction transaction) {
        NetworkResponse<Call> networkResponse = callByProxy(transaction);
        return networkResponse.getResult();
    }

    public NetworkResponse<Call> callByProxy(Transaction transaction) {
        JsonRpcRequest jsonRpcRequest =
                new JsonRpcRequest(JsonRpcMethods.CALL, Arrays.asList(this.groupId, transaction));

        return this.jsonRpcService.sendRequestToGroupByProxy(jsonRpcRequest, Call.class);
    }

    @Override
    public BcosTransaction getTransactionByHash(String transactionHash) {
        NetworkResponse<BcosTransaction> networkResponse =
                getTransactionByHashByProxy(transactionHash);
        return networkResponse.getResult();
    }

    public NetworkResponse<BcosTransaction> getTransactionByHashByProxy(String transactionHash) {
        JsonRpcRequest jsonRpcRequest =
                new JsonRpcRequest(
                        JsonRpcMethods.GET_TRANSACTION_BY_HASH,
                        Arrays.asList(this.groupId, transactionHash));

        return this.jsonRpcService.sendRequestToGroupByProxy(jsonRpcRequest, BcosTransaction.class);
    }

    @Override
    public BcosTransactionReceipt getTransactionReceipt(String transactionHash) {
        NetworkResponse<BcosTransactionReceipt> networkResponse =
                getTransactionReceiptByProxy(transactionHash);
        return networkResponse.getResult();
    }

    public NetworkResponse<BcosTransactionReceipt> getTransactionReceiptByProxy(
            String transactionHash) {
        JsonRpcRequest jsonRpcRequest =
                new JsonRpcRequest(
                        JsonRpcMethods.GET_TRANSACTIONRECEIPT,
                        Arrays.asList(this.groupId, transactionHash));

        return this.jsonRpcService.sendRequestToGroupByProxy(
                jsonRpcRequest, BcosTransactionReceipt.class);
    }

    @Override
    public void stop() {
        ThreadPoolService.stopThreadPool(scheduledExecutorService);
    }
}
