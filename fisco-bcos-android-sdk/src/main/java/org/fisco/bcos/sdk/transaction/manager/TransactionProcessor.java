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
package org.fisco.bcos.sdk.transaction.manager;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.fisco.bcos.sdk.abi.ABICodec;
import org.fisco.bcos.sdk.abi.ABICodecException;
import org.fisco.bcos.sdk.abi.wrapper.ABIObject;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.client.protocol.request.Transaction;
import org.fisco.bcos.sdk.client.protocol.response.Call;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.log.Logger;
import org.fisco.bcos.sdk.log.LoggerFactory;
import org.fisco.bcos.sdk.model.PrecompiledRetCode;
import org.fisco.bcos.sdk.model.RetCode;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.transaction.builder.TransactionBuilderInterface;
import org.fisco.bcos.sdk.transaction.builder.TransactionBuilderService;
import org.fisco.bcos.sdk.transaction.codec.decode.ReceiptParser;
import org.fisco.bcos.sdk.transaction.codec.decode.TransactionDecoderInterface;
import org.fisco.bcos.sdk.transaction.codec.decode.TransactionDecoderService;
import org.fisco.bcos.sdk.transaction.codec.encode.TransactionEncoderInterface;
import org.fisco.bcos.sdk.transaction.codec.encode.TransactionEncoderService;
import org.fisco.bcos.sdk.transaction.model.dto.CallRequest;
import org.fisco.bcos.sdk.transaction.model.dto.CallResponse;
import org.fisco.bcos.sdk.transaction.model.dto.ResultCodeEnum;
import org.fisco.bcos.sdk.transaction.model.dto.TransactionResponse;
import org.fisco.bcos.sdk.transaction.model.exception.TransactionBaseException;
import org.fisco.bcos.sdk.transaction.model.exception.TransactionException;
import org.fisco.bcos.sdk.transaction.model.gas.DefaultGasProvider;
import org.fisco.bcos.sdk.transaction.model.po.RawTransaction;
import org.fisco.bcos.sdk.transaction.tools.JsonUtils;
import org.fisco.bcos.sdk.utils.Numeric;

public class TransactionProcessor implements TransactionProcessorInterface {
    protected static Logger log = LoggerFactory.getLogger(TransactionProcessor.class);
    protected final CryptoSuite cryptoSuite;
    protected final CryptoKeyPair cryptoKeyPair;
    protected final Client client;
    protected final Integer groupId;
    protected final String chainId;
    protected final TransactionBuilderInterface transactionBuilder;
    protected final TransactionEncoderInterface transactionEncoder;

    protected TransactionDecoderInterface transactionDecoder;
    protected ABICodec abiCodec;
    protected String contractName;
    protected String contractAbi;
    protected String contractBin;

    public TransactionProcessor(
            Client client, CryptoKeyPair cryptoKeyPair, Integer groupId, String chainId) {
        this.cryptoSuite = client.getCryptoSuite();
        this.cryptoKeyPair = cryptoKeyPair;
        this.client = client;
        this.groupId = groupId;
        this.chainId = chainId;
        this.transactionBuilder = new TransactionBuilderService(client);
        this.transactionEncoder = new TransactionEncoderService(client.getCryptoSuite());
    }

    public TransactionProcessor(
            Client client,
            CryptoKeyPair cryptoKeyPair,
            Integer groupId,
            String chainId,
            String contractName,
            String abi,
            String bin) {
        this(client, cryptoKeyPair, groupId, chainId);
        this.transactionDecoder = new TransactionDecoderService(cryptoSuite);
        this.abiCodec = new ABICodec(cryptoSuite);
        this.contractName = contractName;
        this.contractAbi = abi;
        this.contractBin = bin;
    }

    @Override
    public TransactionReceipt sendTransactionAndGetReceipt(
            String to, String data, CryptoKeyPair cryptoKeyPair) {
        String signedData = createSignedTransaction(to, data, cryptoKeyPair);
        return this.client.sendRawTransactionAndGetReceipt(signedData);
    }

    @Override
    public Call executeCall(CallRequest callRequest) {
        return executeCall(
                callRequest.getFrom(), callRequest.getTo(), callRequest.getEncodedFunction());
    }

    @Override
    public Call executeCall(String from, String to, String encodedFunction) {
        return client.call(new Transaction(from, to, encodedFunction));
    }

    @Override
    public String createSignedTransaction(String to, String data, CryptoKeyPair cryptoKeyPair) {
        RawTransaction rawTransaction =
                transactionBuilder.createTransaction(
                        DefaultGasProvider.GAS_PRICE,
                        DefaultGasProvider.GAS_LIMIT,
                        to,
                        data,
                        BigInteger.ZERO,
                        new BigInteger(this.chainId),
                        BigInteger.valueOf(this.groupId),
                        "");
        return transactionEncoder.encodeAndSign(rawTransaction, cryptoKeyPair);
    }

    public TransactionResponse deployAndGetResponse(List<Object> params) throws ABICodecException {
        return deployAndGetResponse(
                contractAbi, createSignedConstructor(contractAbi, contractBin, params));
    }

    public String createSignedConstructor(String abi, String bin, List<Object> params)
            throws ABICodecException {
        return createSignedTransaction(
                null, abiCodec.encodeConstructor(abi, bin, params), this.cryptoKeyPair);
    }

    public TransactionResponse deployAndGetResponse(String abi, String signedData) {
        TransactionReceipt receipt = client.sendRawTransactionAndGetReceipt(signedData);
        try {
            return transactionDecoder.decodeReceiptWithoutValues(abi, receipt);
        } catch (TransactionException | IOException | ABICodecException e) {
            log.error("deploy exception: {}", e.getMessage());
            return new TransactionResponse(
                    receipt, ResultCodeEnum.EXCEPTION_OCCUR.getCode(), e.getMessage());
        }
    }

    public TransactionResponse sendTransactionAndGetResponse(
            String to, String functionName, List<Object> params) throws ABICodecException {
        String data = encodeFunction(contractAbi, functionName, params);
        return sendTransactionAndGetResponse(to, contractAbi, functionName, data);
    }

    public String encodeFunction(String abi, String functionName, List<Object> params)
            throws ABICodecException {
        return abiCodec.encodeMethod(abi, functionName, params);
    }

    public TransactionResponse sendTransactionAndGetResponse(
            String to, String abi, String functionName, String data) throws ABICodecException {
        String signedData = createSignedTransaction(to, data, this.cryptoKeyPair);
        TransactionReceipt receipt = client.sendRawTransactionAndGetReceipt(signedData);
        try {
            return transactionDecoder.decodeReceiptWithValues(abi, functionName, receipt);
        } catch (TransactionException | IOException e) {
            log.error("sendTransaction exception: {}", e.getMessage());
            return new TransactionResponse(
                    receipt, ResultCodeEnum.EXCEPTION_OCCUR.getCode(), e.getMessage());
        }
    }

    public CallResponse sendCall(
            String from, String to, String functionName, List<Object> paramsList)
            throws TransactionBaseException, ABICodecException {
        String data = abiCodec.encodeMethod(contractAbi, functionName, paramsList);
        return callAndGetResponse(from, to, contractAbi, functionName, data);
    }

    public CallResponse callAndGetResponse(
            String from, String to, String abi, String functionName, String data)
            throws ABICodecException, TransactionBaseException {
        Call call = executeCall(from, to, data);
        CallResponse callResponse = parseCallResponseStatus(call.getCallResult());
        Pair<List<Object>, List<ABIObject>> results =
                abiCodec.decodeMethodAndGetOutputObject(
                        abi, functionName, call.getCallResult().getOutput());
        callResponse.setValues(JsonUtils.toJson(results.getLeft()));
        callResponse.setReturnObject(results.getLeft());
        callResponse.setReturnABIObject(results.getRight());
        return callResponse;
    }

    private CallResponse parseCallResponseStatus(Call.CallOutput callOutput)
            throws TransactionBaseException {
        CallResponse callResponse = new CallResponse();
        RetCode retCode = ReceiptParser.parseCallOutput(callOutput, "");
        callResponse.setReturnCode(Numeric.decodeQuantity(callOutput.getStatus()).intValue());
        callResponse.setReturnMessage(retCode.getMessage());
        if (!retCode.getMessage().equals(PrecompiledRetCode.CODE_SUCCESS.getMessage())) {
            throw new TransactionBaseException(retCode);
        }
        return callResponse;
    }
}
