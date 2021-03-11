package org.fisco.bcos.sdk.demo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import org.apache.commons.lang3.tuple.Pair;
import org.fisco.bcos.sdk.BcosSDK;
import org.fisco.bcos.sdk.abi.ABICodecException;
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

public class DeployCallActivity extends AppCompatActivity {

    Logger logger = LoggerFactory.getLogger(DeployCallActivity.class);

    private ExecutorService singleThreadExecutor;
    private ProxyConfig proxyConfig = new ProxyConfig();
    private Intent intent;
    private BcosSDK sdk;
    private Button btnDeployContract;
    private Button btnLoadContract;
    private EditText etContractAddress;
    private Button btnSet;
    private EditText etSetString;
    private Button btnGet;
    private EditText etGetString;

    private Client client;
    private HelloWorld sol;
    private TransactionProcessor manager;
    private String contractAddress;
    private boolean isInitClient = false;
    private boolean isWrapper = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deploy_call);
        intent = getIntent();
        initView();
        initEvent();
        initParam();

        singleThreadExecutor = SingleLineUtil.getInstance().getSingle();
    }

    private void initView() {
        btnDeployContract = findViewById(R.id.btn_deploy);
        btnLoadContract = findViewById(R.id.btn_load);
        etContractAddress = findViewById(R.id.et_contract_address);
        btnSet = findViewById(R.id.btn_set);
        etSetString = findViewById(R.id.et_set_contract);
        btnGet = findViewById(R.id.btn_get);
        etGetString = findViewById(R.id.et_get_contract);
        etGetString.setEnabled(false);
    }

    private void initEvent() {
        btnDeployContract.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        singleThreadExecutor.execute(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        if (!isInitClient) {
                                            initClient();
                                        }
                                        if (client == null) {
                                            return;
                                        }
                                        if (isWrapper) {
                                            deployContractByWrapper();
                                        } else {
                                            deployContractByAbiBin();
                                        }
                                    }
                                });
                    }
                });

        btnLoadContract.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        singleThreadExecutor.execute(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        if (!isInitClient) {
                                            initClient();
                                        }
                                        if (client == null) {
                                            return;
                                        }
                                        if (isWrapper) {
                                            loadContractByWrapper();
                                        } else {
                                            loadContractByAbiBin();
                                        }
                                    }
                                });
                    }
                });

        btnSet.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        singleThreadExecutor.execute(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        if (!isInitClient) {
                                            initClient();
                                        }
                                        if (client == null) {
                                            return;
                                        }
                                        if (isWrapper) {
                                            if (sol != null) {
                                                setByWrapper();
                                            } else {
                                                showMessage("please first deploy or load contract");
                                            }
                                        } else {
                                            if (contractAddress != null) {
                                                setByAbiBin();
                                            } else {
                                                showMessage("please first deploy or load contract");
                                            }
                                        }
                                    }
                                });
                    }
                });

        btnGet.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        singleThreadExecutor.execute(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        if (!isInitClient) {
                                            initClient();
                                        }
                                        if (client == null) {
                                            return;
                                        }
                                        if (isWrapper) {
                                            if (sol != null) {
                                                getByWrapper();
                                            } else {
                                                showMessage("please first deploy or load contract");
                                            }
                                        } else {
                                            if (contractAddress != null) {
                                                getByAbiBin();
                                            } else {
                                                showMessage("please first deploy or load contract");
                                            }
                                        }
                                    }
                                });
                    }
                });
    }

    private void initParam() {
        // config param, if you do not pass in the implementation of network access, use the default
        // class
        proxyConfig.setChainId(intent.getStringExtra("chainId").trim());
        proxyConfig.setCryptoType(
                intent.getBooleanExtra("transactType", true)
                        ? CryptoType.ECDSA_TYPE
                        : CryptoType.SM_TYPE);
        if (!intent.getBooleanExtra("keyType", true)) {
            proxyConfig.setHexPrivateKey(intent.getStringExtra("keyContent").trim());
        }
        NetworkHandlerHttpsImp networkHandlerImp = new NetworkHandlerHttpsImp();
        networkHandlerImp.setIpAndPort(intent.getStringExtra("ipPort").trim());
        CertInfo certInfo = new CertInfo("nginx.crt");
        networkHandlerImp.setCertInfo(certInfo);
        networkHandlerImp.setContext(getApplicationContext());
        proxyConfig.setNetworkHandler(networkHandlerImp);
        // config param end
        isWrapper = intent.getBooleanExtra("transactInfo", true);
    }

    @Override
    protected void onDestroy() {
        if (client != null) {
            client.stop();
        }
        if (sdk != null) {
            sdk.stopAll();
        }
        super.onDestroy();
    }

    private void initClient() {
        try {
            sdk = BcosSDK.build(proxyConfig);
            client = sdk.getClient(new Integer(intent.getStringExtra("groupId").trim()));
            NodeVersion nodeVersion = client.getClientNodeVersion();
            logger.info("node version: " + JsonUtils.toJson(nodeVersion));
            isInitClient = true;
        } catch (NetworkHandlerException e) {
            String errorInfo = "init client NetworkHandlerException, error info: " + e.getMessage();
            logger.error(errorInfo);
            showMessage(errorInfo);
        } catch (Exception e) {
            String errorInfo = "init client Exception, error info: " + e.getMessage();
            logger.error(errorInfo);
            showMessage(errorInfo);
        }
    }

    private void showMessage(String message) {
        runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(DeployCallActivity.this, message, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void deployContractByWrapper() {
        try {
            sol = HelloWorld.deploy(client, client.getCryptoSuite().getCryptoKeyPair());
            logger.info(
                    "deploy contract , contract address: "
                            + JsonUtils.toJson(sol.getContractAddress()));
            runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            etContractAddress.setText(sol.getContractAddress());
                        }
                    });
        } catch (ContractException e) {
            // e.printStackTrace();
            String errorInfo = "ContractException error info: " + e.getMessage();
            logger.error(errorInfo);
            showMessage(errorInfo);
        }
    }

    private void deployContractByAbiBin() {
        String contractName = "HelloWorld";
        Pair<String, String> abiBinStr = getContractAbiBin(contractName);
        String contractAbi = abiBinStr.getLeft();
        String contractBin = abiBinStr.getRight();
        logger.info("Contract abi: " + contractAbi + ", bin: " + contractBin);

        try {
            manager =
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
            contractAddress = response.getContractAddress();
            logger.info("deploy contract , contract address: " + contractAddress);
            runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            etContractAddress.setText(contractAddress);
                        }
                    });
        } catch (NetworkHandlerException e) {
            String errorInfo = "NetworkHandlerException error info: " + e.getMessage();
            logger.error(errorInfo);
            showMessage(errorInfo);
        } catch (Exception e) {
            // e.printStackTrace();
            String errorInfo = "error info: " + e.getMessage();
            logger.error(errorInfo);
            showMessage(errorInfo);
        }
    }

    private void loadContractByWrapper() {
        if ("".equals(etContractAddress.getText().toString().trim())) {
            showMessage("please input contract address to load contract");
            return;
        }
        sol =
                HelloWorld.load(
                        etContractAddress.getText().toString().trim(),
                        client,
                        client.getCryptoSuite().getCryptoKeyPair());
        showMessage("load contract address: " + sol.getContractAddress());
    }

    private void loadContractByAbiBin() {
        if ("".equals(etContractAddress.getText().toString().trim())) {
            showMessage("please input contract address to load contract");
            return;
        }
        String contractName = "HelloWorld";
        Pair<String, String> abiBinStr = getContractAbiBin(contractName);
        String contractAbi = abiBinStr.getLeft();
        String contractBin = abiBinStr.getRight();
        logger.info("Contract abi: " + contractAbi + ", bin: " + contractBin);

        manager =
                TransactionProcessorFactory.createTransactionProcessor(
                        client,
                        client.getCryptoSuite().getCryptoKeyPair(),
                        contractName,
                        contractAbi,
                        contractBin);
        contractAddress = etContractAddress.getText().toString().trim();
        showMessage("load contract address: " + contractAddress);
    }

    private void setByWrapper() {
        String content = etSetString.getText().toString().trim();
        if ("".equals(content)) {
            showMessage("please input in set content");
            return;
        }
        TransactionReceipt ret1 = sol.set(content);
        logger.info("send, receipt: " + JsonUtils.toJson(ret1));
        BcosTransaction transaction = client.getTransactionByHash(ret1.getTransactionHash());
        logger.info("getTransactionByHash, result: " + JsonUtils.toJson(transaction.getResult()));
        BcosTransactionReceipt receipt = client.getTransactionReceipt(ret1.getTransactionHash());
        logger.info("getTransactionReceipt, result: " + JsonUtils.toJson(receipt.getResult()));
        showMessage("call contract successfully, transaction hash:" + ret1.getTransactionHash());
    }

    private void setByAbiBin() {
        String content = etSetString.getText().toString().trim();
        if ("".equals(content)) {
            showMessage("please input in set content");
            return;
        }
        try {
            List<Object> paramsSet = new ArrayList<>();
            paramsSet.add(content);
            TransactionResponse ret1 =
                    manager.sendTransactionAndGetResponse(contractAddress, "set", paramsSet);
            logger.info("send, receipt: " + JsonUtils.toJson(ret1));
            BcosTransaction transaction =
                    client.getTransactionByHash(ret1.getTransactionReceipt().getTransactionHash());
            logger.info(
                    "getTransactionByHash, result: " + JsonUtils.toJson(transaction.getResult()));
            BcosTransactionReceipt receipt =
                    client.getTransactionReceipt(ret1.getTransactionReceipt().getTransactionHash());
            logger.info("getTransactionReceipt, result: " + JsonUtils.toJson(receipt.getResult()));
            showMessage(
                    "call contract successfully, transaction hash:"
                            + ret1.getTransactionReceipt().getTransactionHash());
        } catch (ABICodecException e) {
            String errorInfo = "ABICodecException error info: " + e.getMessage();
            logger.error(errorInfo);
            showMessage(errorInfo);
        }
    }

    private void getByWrapper() {
        try {
            String ret1 = sol.get();
            logger.info("call to return string, result: " + ret1);
            String finalRet = ret1;
            runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            etGetString.setText(finalRet);
                        }
                    });
        } catch (ContractException e) {
            // e.printStackTrace();
            String errorInfo = "ContractException error info: " + e.getMessage();
            logger.error(errorInfo);
            showMessage(errorInfo);
        }
    }

    private void getByAbiBin() {
        try {
            List<Object> paramsGet = new ArrayList<>();
            CallResponse ret1 =
                    manager.sendCall(
                            client.getCryptoSuite().getCryptoKeyPair().getAddress(),
                            contractAddress,
                            "get",
                            paramsGet);
            List<Object> ret2 = JsonUtils.fromJsonList(ret1.getValues(), Object.class);
            logger.info("call to return object list, result: " + ret2);
            String finalRet = ret2.toString();
            runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            etGetString.setText(finalRet);
                        }
                    });
        } catch (Exception e) {
            String errorInfo = "Exception error info: " + e.getMessage();
            logger.error(errorInfo);
            showMessage(errorInfo);
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
            // e.printStackTrace();
            String errorInfo = "getFromAssets failed, error info: " + e.getMessage();
            logger.error(errorInfo);
            showMessage(errorInfo);
        }

        return null;
    }
}
