package org.fisco.bcos.sdk;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Environment;

import com.google.common.util.concurrent.RateLimiter;

import org.fisco.bcos.sdk.abi.ABICodec;
import org.fisco.bcos.sdk.abi.ABICodecException;
import org.fisco.bcos.sdk.abi.tools.TopicTools;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.crypto.signature.ECDSASignatureResult;
import org.fisco.bcos.sdk.eventsub.EventCallback;
import org.fisco.bcos.sdk.eventsub.EventLogParams;
import org.fisco.bcos.sdk.eventsub.EventSubscribe;
import org.fisco.bcos.sdk.log.BcosSDKLogUtil;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;
import org.fisco.bcos.sdk.model.CryptoType;
import org.fisco.bcos.sdk.model.EventLog;
import org.fisco.bcos.sdk.model.NodeVersion;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;
import org.fisco.bcos.sdk.transaction.tools.JsonUtils;
import org.fisco.bcos.sdk.utils.ThreadPoolService;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends AppCompatActivity {

    private static AtomicInteger transactionCountSent = new AtomicInteger(0);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BcosSDKLogUtil.configLog(Environment.getExternalStorageDirectory().getPath(), Level.TRACE);
        Logger logger = Logger.getLogger(MainActivity.class);
        final String ConfigFilePath = Environment.getExternalStorageDirectory().getPath() + "/javasdk/config.toml";
        BcosSDK sdk = BcosSDK.build(ConfigFilePath);
        testDeployAndSendTx(sdk, logger);
        //testSubscribeEvent(sdk, logger);
        //testECRecover(sdk, logger);
        //testPerformanceOk(sdk, 1000, 10, 1);
        try {
            Thread.sleep(3000);
        } catch (Exception e) {
            logger.error("exception:", e);
        }
        sdk.stopAll();
    }

    private void testDeployAndSendTx(BcosSDK sdk, Logger logger) {
        try {
            Client client = sdk.getClient(1);
            NodeVersion nodeVersion = client.getClientNodeVersion();
            logger.info("node version: " + JsonUtils.toJson(nodeVersion));
            ComplexSol sol = ComplexSol.deploy(client, client.getCryptoSuite().getCryptoKeyPair(),new BigInteger("100"), "20");
            logger.info("deploy transaction response: " + JsonUtils.toJson(sol.getContractAddress()));
            TransactionReceipt receipt = sol.incrementUint256(new BigInteger("20"));
            logger.info("send transaction response: " + JsonUtils.toJson(receipt));
            BigInteger result = sol.getUint256();
            logger.info("getUint256 response: " + result);
        } catch (Exception e) {
            logger.error("deploy and send transaction exception:", e);
        }
    }

    private void testSubscribeEvent(BcosSDK sdk, Logger logger) {
        Client client = sdk.getClient(1);
        EventSubscribe eventSubscribe = sdk.getEventSubscribe(client.getGroupId());
        eventSubscribe.start();

        EventLogParams eventLogParams = new EventLogParams();
        eventLogParams.setFromBlock("latest");
        eventLogParams.setToBlock("latest");
        eventLogParams.setAddresses(new ArrayList<>());
        ArrayList<Object> topics = new ArrayList<>();
        CryptoSuite cryptoSuite =
                new CryptoSuite(client.getCryptoSuite().getCryptoTypeConfig());
        TopicTools topicTools = new TopicTools(cryptoSuite);
        topics.add(topicTools.stringToTopic("LogIncrement(address,uint256)"));
        eventLogParams.setTopics(topics);

        class SubscribeCallback implements EventCallback {
            public transient Semaphore semaphore = new Semaphore(1, true);

            SubscribeCallback() {
                try {
                    semaphore.acquire(1);
                } catch (InterruptedException e) {
                    logger.error("error :", e);
                    Thread.currentThread().interrupt();
                }
            }

            @Override
            public void onReceiveLog(int status, List<EventLog> logs) {
                String str = "status in onReceiveLog : " + status;
                logger.debug(str);
                semaphore.release();
                if (logs != null) {
                    for (EventLog log : logs) {
                        logger.debug(
                                " blockNumber:"
                                        + log.getBlockNumber()
                                        + ",txIndex:"
                                        + log.getTransactionIndex()
                                        + " data:"
                                        + log.getData());
                        ABICodec abiCodec = new ABICodec(client.getCryptoSuite());
                        try {
                            List<Object> list = abiCodec.decodeEvent(ComplexSol.ABI, "LogIncrement", log);
                            logger.debug("decode event log content, " + list);
                        } catch (ABICodecException e) {
                            logger.error("decode event log error, " + e.getMessage());
                        }
                    }
                }
            }
        }

        logger.info("start to subscribe event");
        SubscribeCallback subscribeEventCallback1 = new SubscribeCallback();
        String registerId =
                eventSubscribe.subscribeEvent(eventLogParams, subscribeEventCallback1);
        try {
            subscribeEventCallback1.semaphore.acquire(1);
            subscribeEventCallback1.semaphore.release();
            logger.info("subscribe successful, registerId is " + registerId);
        } catch (InterruptedException e) {
            logger.error("system error:", e);
            Thread.currentThread().interrupt();
        }

        try {
            Thread.sleep(3000);
        } catch (Exception e) {
            logger.error("exception:", e);
        }

        logger.info("start to unregister event");
        SubscribeCallback subscribeEventCallback2 = new SubscribeCallback();
        eventSubscribe.unsubscribeEvent(registerId, subscribeEventCallback2);
        try {
            subscribeEventCallback2.semaphore.acquire(1);
            subscribeEventCallback2.semaphore.release();
            logger.info("unregister event successful");
        } catch (InterruptedException e) {
            logger.error("system error:", e);
            Thread.currentThread().interrupt();
        }

        eventSubscribe.stop();
    }

    public void testECRecover(BcosSDK sdk, Logger logger) {
        Client client = sdk.getClient(1);
        try {
            // test EvidenceVerify(ecRecover)
            EvidenceVerify evidenceVerify =
                    EvidenceVerify.deploy(client, client.getCryptoSuite().getCryptoKeyPair());
            System.out.println("### address of evidenceVerify:" + evidenceVerify.getContractAddress());
            CryptoSuite ecdsaCryptoSuite = new CryptoSuite(CryptoType.ECDSA_TYPE);
            String evi = "test";
            String evInfo = "test_info";
            int random = new SecureRandom().nextInt(50000);
            String eviId = String.valueOf(random);
            // sign to evi
            byte[] message = ecdsaCryptoSuite.hash(evi.getBytes());
            CryptoKeyPair cryptoKeyPair = ecdsaCryptoSuite.createKeyPair();
            // sign with secp256k1
            ECDSASignatureResult signatureResult =
                    (ECDSASignatureResult) ecdsaCryptoSuite.sign(message, cryptoKeyPair);
            String signAddr = cryptoKeyPair.getAddress();
            TransactionReceipt insertReceipt =
                    evidenceVerify.insertEvidence(
                            evi,
                            evInfo,
                            eviId,
                            signAddr,
                            message,
                            BigInteger.valueOf(signatureResult.getV() + 27),
                            signatureResult.getR(),
                            signatureResult.getS());
            if (!insertReceipt.getStatus().equals("0x0")) {
                logger.error("sign with secp256k1 failed");
            } else {
                logger.info("sign with secp256k1 success");
            }
            // case wrong signature
            insertReceipt =
                    evidenceVerify.insertEvidence(
                            evi,
                            evInfo,
                            eviId,
                            signAddr,
                            message,
                            BigInteger.valueOf(signatureResult.getV()),
                            signatureResult.getR(),
                            signatureResult.getS());
            if (!insertReceipt.getStatus().equals("0x16")) {
                logger.error("case wrong signature failed");
            } else {
                logger.info("case wrong signature success");
            }
            // case wrong message
            byte[] fakedMessage = ecdsaCryptoSuite.hash(evInfo.getBytes());
            insertReceipt =
                    evidenceVerify.insertEvidence(
                            evi,
                            evInfo,
                            eviId,
                            signAddr,
                            fakedMessage,
                            BigInteger.valueOf(signatureResult.getV() + 27),
                            signatureResult.getR(),
                            signatureResult.getS());
            if (!insertReceipt.getStatus().equals("0x16")) {
                logger.error("case wrong message failed");
            } else {
                logger.info("case wrong message success");
            }
            // case wrong sender
            signAddr = ecdsaCryptoSuite.createKeyPair().getAddress();
            insertReceipt =
                    evidenceVerify.insertEvidence(
                            evi,
                            evInfo,
                            eviId,
                            signAddr,
                            message,
                            BigInteger.valueOf(signatureResult.getV() + 27),
                            signatureResult.getR(),
                            signatureResult.getS());
            if (!insertReceipt.getStatus().equals("0x16")) {
                logger.error("case wrong sender failed");
            } else {
                logger.info("case wrong sender success");
            }
        } catch (ContractException e) {
            logger.error("ContractException, error info: " + e.getMessage());
        }
    }

    private void testPerformanceOk(BcosSDK sdk, Integer count, Integer qps, Integer groupId) {
        try {
            System.out.println(
                    "====== PerformanceOk trans, count: "
                            + count
                            + ", qps:"
                            + qps
                            + ", groupId: "
                            + groupId);

            Client client = sdk.getClient(groupId);

            // deploy the HelloWorld
            System.out.println("====== Deploy Ok ====== ");
            Ok ok = Ok.deploy(client, client.getCryptoSuite().getCryptoKeyPair());
            System.out.println(
                    "====== Deploy Ok succ, address: " + ok.getContractAddress() + " ====== ");

            PerformanceCollector collector = new PerformanceCollector();
            collector.setTotal(count);
            RateLimiter limiter = RateLimiter.create(qps);
            Integer area = count / 10;
            final Integer total = count;

            System.out.println("====== PerformanceOk trans start ======");

            ThreadPoolService threadPoolService =
                    new ThreadPoolService(
                            "PerformanceOk",
                            sdk.getConfig().getThreadPoolConfig().getMaxBlockingQueueSize());

            for (Integer i = 0; i < count; ++i) {
                limiter.acquire();
                threadPoolService
                        .getThreadPool()
                        .execute(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        PerformanceCallback callback = new PerformanceCallback();
                                        callback.setTimeout(0);
                                        callback.setCollector(collector);
                                        try {
                                            ok.trans(new BigInteger("4"), callback);
                                        } catch (Exception e) {
                                            TransactionReceipt receipt = new TransactionReceipt();
                                            receipt.setStatus("-1");
                                            callback.onResponse(receipt);
                                            System.out.println(e.getMessage());
                                        }
                                        int current = transactionCountSent.incrementAndGet();
                                        if (current >= area && ((current % area) == 0)) {
                                            System.out.println(
                                                    "Already sended: "
                                                            + current
                                                            + "/"
                                                            + total
                                                            + " transactions");
                                        }
                                    }
                                });
            }
            // wait to collect all the receipts
            while (!collector.getReceived().equals(count)) {
                Thread.sleep(1000);
            }
            threadPoolService.stop();
        } catch (BcosSDKException | ContractException | InterruptedException e) {
            System.out.println(
                    "====== PerformanceOk test failed, error message: " + e.getMessage());
        }
    }
}