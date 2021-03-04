package org.fisco.bcos.sdk.demo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
import org.fisco.bcos.sdk.network.model.CertInfo;
import org.fisco.bcos.sdk.transaction.manager.TransactionProcessor;
import org.fisco.bcos.sdk.transaction.manager.TransactionProcessorFactory;
import org.fisco.bcos.sdk.transaction.model.dto.CallResponse;
import org.fisco.bcos.sdk.transaction.model.dto.TransactionResponse;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;
import org.fisco.bcos.sdk.transaction.tools.JsonUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class DeployLoadActivity extends AppCompatActivity {
    private ExecutorService singleThreadExecutor;
    ProxyConfig proxyConfig = new ProxyConfig();
    Intent intent;
    BcosSDK sdk;
    Button btnDeployContract;
    Button btnLoadContract;
    EditText etContractAddress;
    Button btnSet;
    EditText etSetString;
    Button btnGet;
    EditText etGetString;
    TextView tvMsg;

    Client client;
    HelloWorld sol = null;
    boolean isInitClient = false;
    Logger logger;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deploy_load);
        intent = getIntent();
        initView();
        initEvent();
        initParam();
//        intent.putExtra("chainId", etChainId.getText().toString().trim());
//        intent.putExtra("groupId", etGroupId.getText().toString().trim());
//        intent.putExtra("transactType", rbECDSA.isChecked());
//        intent.putExtra("keyType", rbRadonKey.isChecked());
//        intent.putExtra("ipPort", etIPPort.getText().toString().trim());
        logger = LoggerFactory.getLogger(MainActivityA.class);
        singleThreadExecutor = SingleLineUtil.getInstance().getSingle();

//        new Thread(
//                new Runnable() {
//                    @Override
//                    public void run() {
////                        httpRequest(logger);
//                        httpsRequest(logger);
//                    }
//                })
//                .start();
    }


    private void initView() {
        btnDeployContract = findViewById(R.id.btn_deploy);
        btnLoadContract = findViewById(R.id.btn_load);
        etContractAddress = findViewById(R.id.et_contract_address);
        btnSet = findViewById(R.id.btn_set);
        etSetString = findViewById(R.id.et_set_contract);
        btnGet = findViewById(R.id.btn_get);
        etGetString = findViewById(R.id.et_get_contract);
        tvMsg = findViewById(R.id.tv_msg);
    }

    private void initEvent() {
        btnDeployContract.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                singleThreadExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (!isInitClient) {
                            initClient();
                        }
                        deployContract();
                    }
                });


            }
        });
        btnLoadContract.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                singleThreadExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (!isInitClient) {
                            initClient();
                        }
                        loadContract();
                    }
                });


            }
        });

        btnSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                singleThreadExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (!isInitClient) {
                            initClient();
                        }
                        setContract();
                    }
                });
            }
        });

        btnGet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                singleThreadExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (!isInitClient) {
                            initClient();
                        }
                        getContract();
                    }
                });
            }
        });
    }

    private void initParam() {
        // config param, if you do not pass in the implementation of network access, use the default
        // class
        proxyConfig.setChainId(intent.getStringExtra("chainId"));
        proxyConfig.setCryptoType(intent.getBooleanExtra("keyType", true) ? CryptoType.ECDSA_TYPE : CryptoType.SM_TYPE);
        proxyConfig.setHexPrivateKey(
                "65c70b77051903d7876c63256d9c165cd372ec7df813d0b45869c56fcf5fd564");
        NetworkHandlerHttpsImp networkHandlerImp = new NetworkHandlerHttpsImp();
        networkHandlerImp.setIpAndPort(intent.getStringExtra("ipPort"));
        CertInfo certInfo = new CertInfo("nginx.crt");
        networkHandlerImp.setCertInfo(certInfo);
        networkHandlerImp.setContext(getApplicationContext());
        proxyConfig.setNetworkHandler(networkHandlerImp);
        // config param end
    }


    @Override
    protected void onDestroy() {
        sdk.stopAll();
        client.stop();
        super.onDestroy();
    }

    private void httpsRequest(Logger logger) {
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

    private void initClient() {
        try {
            sdk = BcosSDK.build(proxyConfig);
            client = sdk.getClient(1);
            NodeVersion nodeVersion = client.getClientNodeVersion();
            logger.info("node version: " + JsonUtils.toJson(nodeVersion));
            isInitClient = true;
        } catch (NetworkHandlerException e) {
            logger.error("exception", e);
        }

    }

    private void deployContract() {
        try {
            sol = HelloWorld.deploy(client, client.getCryptoSuite().getCryptoKeyPair());
            logger.info(
                    "deploy contract , contract address: "
                            + JsonUtils.toJson(sol.getContractAddress()));
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    etContractAddress.setText(sol.getContractAddress());
                }
            });
        } catch (ContractException e) {
            e.printStackTrace();
            logger.error("NetworkHandlerException error info: " + e.getMessage());
        }

    }

    private void loadContract() {
        sol = HelloWorld.load("".equals(etContractAddress.getText().toString().trim()) ?
                        "0x2ffa020155c6c7e388c5e5c9ec7e6d403ec2c2d6" : etContractAddress.getText().toString().trim(),
                client,
                client.getCryptoSuite().getCryptoKeyPair());
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(DeployLoadActivity.this, "contract address: " + sol.getContractAddress(), Toast.LENGTH_LONG).show();

            }
        });
    }

    private void setContract() {
        if("".equals(etSetString.getText().toString().trim())){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(DeployLoadActivity.this,"please type in set content!",Toast.LENGTH_LONG).show();
                }
            });
        }
        TransactionReceipt ret1 = sol.set(etSetString.getText().toString().trim());
        logger.info("send, receipt: " + JsonUtils.toJson(ret1));
        BcosTransaction transaction = client.getTransactionByHash(ret1.getTransactionHash());
        logger.info(
                "getTransactionByHash, result: " + JsonUtils.toJson(transaction.getResult()));
        BcosTransactionReceipt receipt =
                client.getTransactionReceipt(ret1.getTransactionHash());
        logger.info("getTransactionReceipt, result: " + JsonUtils.toJson(receipt.getResult()));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvMsg.setText("send, receipt: " + JsonUtils.toJson(ret1) + "/n" +
                        "getTransactionByHash, result: " + JsonUtils.toJson(transaction.getResult()) +
                        "/n" + "getTransactionReceipt, result: " + JsonUtils.toJson(receipt.getResult()));
            }
        });
    }

    private String getContract() {
        String ret2 = null;
        try {
            ret2 = sol.get();
        } catch (ContractException e) {
            e.printStackTrace();
        }
        logger.info("call to return string, result: " + ret2);
        String finalRet = ret2;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                etGetString.setText(finalRet);
            }
        });
        return ret2;
    }

    private void deployAndSendContract(BcosSDK sdk, Logger logger) {
        try {
//            initClient(sdk, logger);
//            HelloWorld sol = HelloWorld.deploy(client, client.getCryptoSuite().getCryptoKeyPair());
//            logger.info(
//            "deploy contract , contract address: "
//                    + JsonUtils.toJson(sol.getContractAddress()));
            // HelloWorldProxy sol =
//            HelloWorld.load("0x2ffa020155c6c7e388c5e5c9ec7e6d403ec2c2d6", client,
            // client.getCryptoSuite().getCryptoKeyPair());
//            TransactionReceipt ret1 = sol.set("Hello, FISCO BCOS.");
//            logger.info("send, receipt: " + JsonUtils.toJson(ret1));
//            String ret2 = sol.get();
//            logger.info("call to return string, result: " + ret2);
//            BcosTransaction transaction = client.getTransactionByHash(ret1.getTransactionHash());
//            logger.info(
//                    "getTransactionByHash, result: " + JsonUtils.toJson(transaction.getResult()));
//            BcosTransactionReceipt receipt =
//                    client.getTransactionReceipt(ret1.getTransactionHash());
//            logger.info("getTransactionReceipt, result: " + JsonUtils.toJson(receipt.getResult()));

            client.stop();
        } catch (NetworkHandlerException e) {
            logger.error("NetworkHandlerException error info: " + e.getMessage());
        } catch (Exception e) {
            logger.error("error info: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void callContract(BcosSDK sdk, Logger logger) {
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
    //    private void httpRequest(Logger logger) {
//
//
//        sdk = BcosSDK.build(proxyConfig);
//        deployAndSendContract(sdk, logger);
//        try {
//            Thread.sleep(3000);
//        } catch (Exception e) {
//            logger.error("exception:", e);
//        }
//    }
}
