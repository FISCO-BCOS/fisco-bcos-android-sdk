package org.fisco.bcos.sdk;

import android.os.Bundle;
import android.os.Environment;

import androidx.appcompat.app.AppCompatActivity;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.fisco.bcos.sdk.NetworkHandler.NetworkHandlerImp;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple2;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.client.protocol.response.BcosTransaction;
import org.fisco.bcos.sdk.client.protocol.response.BcosTransactionReceipt;
import org.fisco.bcos.sdk.log.BcosSDKLogUtil;
import org.fisco.bcos.sdk.model.NodeVersion;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.transaction.tools.JsonUtils;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends AppCompatActivity {

    private static AtomicInteger transactionCountSent = new AtomicInteger(0);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new Thread(new Runnable() {
            @Override
            public void run() {
                BcosSDKLogUtil.configLog(Environment.getExternalStorageDirectory().getPath(), Level.TRACE);
                Logger logger = Logger.getLogger(MainActivity.class);
                final String ConfigFilePath = Environment.getExternalStorageDirectory().getPath() + "/javasdk/config.toml";

                BcosSDKForProxy sdk = BcosSDKForProxy.build(ConfigFilePath, new NetworkHandlerImp());
                //BcosSDK sdk = BcosSDK.build(ConfigFilePath);
                // HelloWorldProxy
                try {
                    Client client = sdk.getClient(1);
                    NodeVersion nodeVersion = client.getClientNodeVersion();
                    logger.info("node version: " + JsonUtils.toJson(nodeVersion));
                    HelloWorldProxy sol = HelloWorldProxy.deploy(client, client.getCryptoSuite().getCryptoKeyPair(), "Hello world.");
                    logger.info("deploy contract , contract address: " + JsonUtils.toJson(sol.getContractAddress()));
                    //HelloWorldProxy sol = HelloWorldProxy.load("0x2ffa020155c6c7e388c5e5c9ec7e6d403ec2c2d6", client, client.getCryptoSuite().getCryptoKeyPair());
                    TransactionReceipt ret1 = sol.set("Hello, FISCO BCOS.");
                    logger.info("send, receipt: " + JsonUtils.toJson(ret1));
                    String ret2 = sol.get();
                    logger.info("call to return string, result: " + ret2);
                    List<String> ret3 = sol.getList();
                    logger.info("call to return list, result: " + JsonUtils.toJson(ret3));
                    Tuple2<String, BigInteger> ret4 = sol.getTuple();
                    logger.info("call to return tuple, result: " + JsonUtils.toJson(ret4));
                    BcosTransaction ret5 = client.getTransactionByHash(ret1.getTransactionHash());
                    logger.info("getTransactionByHash, result: " + JsonUtils.toJson(ret5));
                    BcosTransactionReceipt ret6 = client.getTransactionReceipt(ret1.getTransactionHash());
                    logger.info("getTransactionReceipt, result: " + JsonUtils.toJson(ret6));
                } catch (Exception e) {
                    logger.error("error info: " + e.getMessage());
                    e.printStackTrace();
                }

                try {
                    Thread.sleep(3000);
                } catch (Exception e) {
                    logger.error("exception:", e);
                }
                sdk.stopAll();
            }
        }).start();
    }
}