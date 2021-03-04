package org.fisco.bcos.sdk.demo;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.fisco.bcos.sdk.BcosSDK;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.client.exceptions.NetworkHandlerException;
import org.fisco.bcos.sdk.client.protocol.response.BcosTransaction;
import org.fisco.bcos.sdk.client.protocol.response.BcosTransactionReceipt;
import org.fisco.bcos.sdk.config.model.ProxyConfig;
import org.fisco.bcos.sdk.demo.contract.HelloWorld;
import org.fisco.bcos.sdk.log.Logger;
import org.fisco.bcos.sdk.log.LoggerFactory;
import org.fisco.bcos.sdk.model.CryptoType;
import org.fisco.bcos.sdk.model.NodeVersion;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.network.NetworkHandlerHttpsImp;
import org.fisco.bcos.sdk.network.NetworkHandlerImp;
import org.fisco.bcos.sdk.network.model.CertInfo;
import org.fisco.bcos.sdk.transaction.manager.TransactionProcessor;
import org.fisco.bcos.sdk.transaction.manager.TransactionProcessorFactory;
import org.fisco.bcos.sdk.transaction.model.dto.CallResponse;
import org.fisco.bcos.sdk.transaction.model.dto.TransactionResponse;
import org.fisco.bcos.sdk.transaction.tools.JsonUtils;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Logger logger = LoggerFactory.getLogger(MainActivity.class);

        new Thread(
                        new Runnable() {
                            @Override
                            public void run() {
                                httpRequest(logger);
                                httpsRequest(logger);
                            }
                        })
                .start();
    }

    private void httpRequest(org.fisco.bcos.sdk.log.Logger logger) {
        // config param, if you do not pass in the implementation of network access, use the default
        // class
        ProxyConfig proxyConfig = new ProxyConfig();
        proxyConfig.setChainId("1");
        proxyConfig.setCryptoType(CryptoType.ECDSA_TYPE);
        proxyConfig.setHexPrivateKey(
                "65c70b77051903d7876c63256d9c165cd372ec7df813d0b45869c56fcf5fd564");
        NetworkHandlerImp networkHandlerImp = new NetworkHandlerImp();
        networkHandlerImp.setIpAndPort("http://116.63.184.110/");
        proxyConfig.setNetworkHandler(networkHandlerImp);
        // config param end

        BcosSDK sdk = BcosSDK.build(proxyConfig);
        deployAndSendContract(sdk, logger);
        try {
            Thread.sleep(3000);
        } catch (Exception e) {
            logger.error("exception:", e);
        }
        sdk.stopAll();
    }

    private void httpsRequest(org.fisco.bcos.sdk.log.Logger logger) {
        // config param, if you do not pass in the implementation of network access, use the default
        // class
        ProxyConfig proxyConfig = new ProxyConfig();
        proxyConfig.setChainId("1");
        proxyConfig.setCryptoType(CryptoType.ECDSA_TYPE);
        proxyConfig.setHexPrivateKey(
                "65c70b77051903d7876c63256d9c165cd372ec7df813d0b45869c56fcf5fd564");
        NetworkHandlerHttpsImp networkHandlerImp = new NetworkHandlerHttpsImp();
        networkHandlerImp.setIpAndPort("https://116.63.184.110:8180/");
        CertInfo certInfo = new CertInfo("nginx.crt");
        networkHandlerImp.setCertInfo(certInfo);
        networkHandlerImp.setContext(getApplicationContext());
        proxyConfig.setNetworkHandler(networkHandlerImp);
        // config param end

        BcosSDK sdk = BcosSDK.build(proxyConfig);
        deployAndSendContract(sdk, logger);
        callContract(sdk, logger);
        try {
            Thread.sleep(3000);
        } catch (Exception e) {
            logger.error("exception:", e);
        }
        sdk.stopAll();
    }

    private void deployAndSendContract(BcosSDK sdk, org.fisco.bcos.sdk.log.Logger logger) {
        try {
            Client client = sdk.getClient(1);
            NodeVersion nodeVersion = client.getClientNodeVersion();
            logger.info("node version: " + JsonUtils.toJson(nodeVersion));
            HelloWorld sol = HelloWorld.deploy(client, client.getCryptoSuite().getCryptoKeyPair());
            logger.info(
                    "deploy contract , contract address: "
                            + JsonUtils.toJson(sol.getContractAddress()));
            // HelloWorldProxy sol =
            // HelloWorldProxy.load("0x2ffa020155c6c7e388c5e5c9ec7e6d403ec2c2d6", client,
            // client.getCryptoSuite().getCryptoKeyPair());
            TransactionReceipt ret1 = sol.set("Hello, FISCO BCOS.");
            logger.info("send, receipt: " + JsonUtils.toJson(ret1));
            String ret2 = sol.get();
            logger.info("call to return string, result: " + ret2);
            BcosTransaction transaction = client.getTransactionByHash(ret1.getTransactionHash());
            logger.info(
                    "getTransactionByHash, result: " + JsonUtils.toJson(transaction.getResult()));
            BcosTransactionReceipt receipt =
                    client.getTransactionReceipt(ret1.getTransactionHash());
            logger.info("getTransactionReceipt, result: " + JsonUtils.toJson(receipt.getResult()));

            client.stop();
        } catch (NetworkHandlerException e) {
            logger.error("NetworkHandlerException error info: " + e.getMessage());
        } catch (Exception e) {
            logger.error("error info: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void callContract(BcosSDK sdk, org.fisco.bcos.sdk.log.Logger logger) {
        String contractName = "HelloWorld";
        Pair<String, String> abiBinStr = getContractAbiBin(contractName);
        String contractAbi = abiBinStr.getLeft();
        String contractBin = abiBinStr.getRight();
        logger.info("Contract abi: " + contractAbi + ", bin: " + contractBin);

        try {
            Client client = sdk.getClient(1);
            TransactionProcessor manager =
                    TransactionProcessorFactory.createTransactionProcessor(
                            client,
                            client.getCryptoSuite().getCryptoKeyPair(),
                            contractName,
                            contractAbi,
                            contractBin);
            TransactionResponse response = manager.deployAndGetResponse(new ArrayList<>());
            if (!response.getTransactionReceipt().getStatus().equals("0x0")) {
                return;
            }
            String contractAddress = response.getContractAddress();
            logger.info("deploy contract , contract address: " + contractAddress);
            List<Object> paramsSet = new ArrayList<>();
            paramsSet.add("Hello, FISCO BCOS.");
            TransactionResponse ret1 =
                    manager.sendTransactionAndGetResponse(contractAddress, "set", paramsSet);
            logger.info("send, receipt: " + JsonUtils.toJson(ret1));
            List<Object> paramsGet = new ArrayList<>();
            CallResponse ret2 =
                    manager.sendCall(
                            client.getCryptoSuite().getCryptoKeyPair().getAddress(),
                            contractAddress,
                            "get",
                            paramsGet);
            List<Object> ret3 = JsonUtils.fromJsonList(ret2.getValues(), Object.class);
            logger.info("call to return object list, result: " + ret3);
        } catch (NetworkHandlerException e) {
            logger.error("NetworkHandlerException error info: " + e.getMessage());
        } catch (Exception e) {
            logger.error("error info: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Pair<String, String> getContractAbiBin(String contractName) {
        String abi = getFromAssets(contractName + ".abi");
        String bin = getFromAssets(contractName + ".bin");
        return Pair.of(abi, bin);
    }

    private String getFromAssets(String fileName) {
        try {
            InputStreamReader inputReader =
                    new InputStreamReader(getResources().getAssets().open(fileName));
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line = "";
            String Result = "";
            while ((line = bufReader.readLine()) != null) Result += line;
            return Result;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
