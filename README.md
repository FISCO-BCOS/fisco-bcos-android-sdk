# fisco-bcos-android-sdk

## 概要介绍

开发者可在 Android 应用中通过`fisco-bcos-android-sdk`（以下简称 android-sdk）实现对 FISCO-BCOS 区块链的操作。目前，android-sdk 可实现的功能包括:

- 查询区块链数据
- 部署及调用合约
- 解析合约出参和交易回执

对于部署及调用合约，android-sdk 现有的接口能满足开发者的多种需求：

- 传入开发者指定的私钥 or 使用随机私钥
- 发送国密交易 or 非国密交易
- 基于合约 Java 类部署及调用合约 or 基于合约 abi 和 binary 部署及调用合约

| 注意事项              | 说明                                                                                                       |
| --------------------- | ---------------------------------------------------------------------------------------------------------- |
| 服务依赖              | [节点接入代理服务 bcos-node-proxy](https://github.com/FISCO-BCOS/bcos-node-proxy/tree/feature_mobile_http) |
| 架构支持              | `armeabi-v7a`、`arm64-v8a`                                                                                 |
| 最低 Android API 要求 | 21，对应 Android 5.0版本                                                                                   |
| android-sdk 所需权限  | 读权限、网络访问权限                                                                                       |
| android-sdk 包大小    | 1M                                                                                                         |
| android-sdk 编译依赖  | 详见`2. 添加依赖`内容                                                                                      |
| 网络请求实现          | 与区块链进行交互的接口均为**同步接口**，开发者需留意线程切换的影响                                         |

## 如何基于 android-sdk 开发区块链应用

关于如何安装 Android 集成开发环境及如何创建一个 Android 应用，请参考[Android 开发者手册](https://developer.android.google.cn/studio/intro)，此处不再赘述。本章节重点描述如何在一个应用中使用 android-sdk 与区块链节点进行交互。

### 1. 获取权限

对于引入 android-sdk 的项目，开发者可在项目的配置文件`app/src/main/AndroidManifest.xml`中增加以下内容获取读权限及网络访问权限。

```java
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="org.fisco.bcos.android.demo">

    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
    <uses-permission android:name="android.permission.INTERNET"/>

    <application
        android:requestLegacyExternalStorage="true"
        android:usesCleartextTraffic="true">
    </application>

</manifest>
```

### 2. 添加依赖

修改项目的`app/build.gradle`，添加以下源和依赖。

```java
allprojects {
    repositories {
        mavenCentral()
        maven{
            url "https://maven.aliyun.com/repository/google"
        }
        maven{
            url "https://maven.aliyun.com/repository/public"
        }
        maven{
            url "https://maven.aliyun.com/repository/gradle-plugin"
        }
        maven{
            url "https://oss.sonatype.org/content/repositories/snapshots"
        }
    }
}

## 注：对于部分原项目已有的依赖，可忽略引用
dependencies {
    implementation 'org.fisco-bcos.android-sdk:fisco-bcos-android-sdk:1.0.0-SNAPSHOT'
    implementation 'com.fasterxml.jackson.core:jackson-databind:2.12.1'
    implementation 'com.fasterxml.jackson.core:jackson-annotations:2.12.1'
    implementation 'com.fasterxml.jackson.core:jackson-core:2.12.1'
    implementation 'org.apache.commons:commons-lang3:3.1'
    implementation 'commons-io:commons-io:2.4'
    implementation 'com.squareup.okhttp3:okhttp:3.13.0'
}
```

### 3. 初始化 sdk

初始化 android-sdk 时需提供`ProxyConfig`配置信息，包括以下内容。

| 设置项         | 是否可选 | 说明                                                                                         |
| -------------- | -------- | -------------------------------------------------------------------------------------------- |
| chainId        | 必选     | 链标识，需与 FISCO-BCOS 节点配置的一致                                                       |
| crytoType      | 必选     | 是否使用国密交易及账号，需与 FISCO-BCOS 节点配置的一致，目前可选值包括 ECDSA_TYPE 和 SM_TYPE |
| hexPrivateKey  | 可选     | 发交易进行签名使用的私钥，开发者可从文件或数据库中读取进行设置，如不设置，sdk 内部随机生成   |
| networkHandler | 可选     | http/https 请求实现，开发者可自行实现并传入，如不传入，采用 sdk 内部实现                     |

对于`networkHandler`，android-sdk 提供了`http`和`https`两种传输协议的内置实现。其中，`NetworkHandlerImp`实现`http`请求，直接访问`Bcos-node-proxy`；`NetworkHandlerHttpsImp`实现`https`请求，通过 Nginx 访问`Bcos-node-proxy`，该方式须在`assets`目录放置 Nginx 的证书。

基于`NetworkHandlerImp`初始化 android-sdk 例子如下。

```java
    ProxyConfig proxyConfig = new ProxyConfig();
    proxyConfig.setChainId("1");
    proxyConfig.setCryptoType(CryptoType.ECDSA_TYPE);
    proxyConfig.setHexPrivateKey("65c70b77051903d7876c63256d9c165cd372ec7df813d0b45869c56fcf5fd564");
    NetworkHandlerImp networkHandlerImp = new NetworkHandlerImp();
    networkHandlerImp.setIpAndPort("http://127.0.0.1:8170/")
    proxyConfig.setNetworkHandler(networkHandlerImp);
    BcosSDKForProxy sdk = BcosSDKForProxy.build(proxyConfig);
```

基于`NetworkHandlerHttpsImp`初始化 android-sdk 例子如下。

```java
    ProxyConfig proxyConfig = new ProxyConfig();
    proxyConfig.setChainId("1");
    proxyConfig.setCryptoType(CryptoType.ECDSA_TYPE);
    proxyConfig.setHexPrivateKey("65c70b77051903d7876c63256d9c165cd372ec7df813d0b45869c56fcf5fd564");
    NetworkHandlerHttpsImp networkHandlerImp = new NetworkHandlerHttpsImp();
    networkHandlerImp.setIpAndPort("https://127.0.0.1:8180/");
    CertInfo certInfo = new CertInfo("nginx.crt");
    networkHandlerImp.setCertInfo(certInfo);
    networkHandlerImp.setContext(getApplicationContext());
    proxyConfig.setNetworkHandler(networkHandlerImp);
    BcosSDKForProxy sdk = BcosSDKForProxy.build(proxyConfig);
```

### 4. 编译合约

开发者基于智能合约开发应用前，需将 Solidity 合约文件编译生成 Java 文件和 abi 、binary 文件。编译过程如下。

- 使用`java -version`查询 JDK 版本，要求版本大于等于1.8，推荐使用 JDK 14；
- 在`tool/`目录执行`bash get_console.sh`下载控制台，下载完成后在当前目录下生成`console/`目录；
- 将需编译的合约放于`tool/console/contracts/solidity`目录（该目录已内置一些合约，如`Helloworld.sol`）；
- 在`tool/console/`目录执行`bash sol2java.sh org.fisco.bcos.sdk`，其中`org.fisco.bcos.sdk`指定生成的 Java 包名；
- 编译生成的 Java 文件（如`Helloworld.java`）位于`tool/console/contracts/sdk/java`目录。
- 编译生成的 abi 文件（如`Helloworld.abi`）位于`tool/console/contracts/sdk/abi`目录，binary 文件（如`Helloworld.bin`）位于`tool/console/contracts/sdk/bin`目录。

### 5. 部署及调用合约

#### 5.1 基于合约 Java 类部署及调用合约

以`Helloworld`合约为例说明如何部署、加载及调用合约。调用以下代码前需`import sHelloworld.java`到项目中。

```Java
try {
    Client client = sdk.getClient(1);

    HelloWorld sol = HelloWorld.deploy(client, client.getCryptoSuite().getCryptoKeyPair());
    logger.info("deploy contract , contract address: " + JsonUtils.toJson(sol.getContractAddress()));
    // HelloWorldProxy sol = HelloWorldProxy.load("0x2ffa020155c6c7e388c5e5c9ec7e6d403ec2c2d6", client, client.getCryptoSuite().getCryptoKeyPair());
    TransactionReceipt ret1 = sol.set("Hello, FISCO BCOS.");
    logger.info("send, receipt: " + JsonUtils.toJson(ret1));
    String ret2 = sol.get();
    logger.info("call to return string, result: " + ret2);
    BcosTransaction transaction = client.getTransactionByHash(ret1.getTransactionHash());
    logger.info("getTransactionByHash, result: " + JsonUtils.toJson(transaction.getResult()));
    BcosTransactionReceipt receipt = client.getTransactionReceipt(ret1.getTransactionHash());
    logger.info("getTransactionReceipt, result: " + JsonUtils.toJson(receipt.getResult()));

    client.stop();
} catch (NetworkHandlerException e) {
    logger.error("NetworkHandlerException error info: " + e.getMessage());
} catch (Exception e) {
    logger.error("error info: " + e.getMessage());
    e.printStackTrace();
}
```

#### 5.2 基于合约 abi 和 binary 部署及调用合约

以`Helloworld`合约为例说明如何部署及调用合约。调用以下代码前需先将`Helloworld.abi`和`Helloworld.bin`放置在项目的`assets`目录。

```Java
try {
    Client client = sdk.getClient(1);

    String contractName = "HelloWorld";
    String contractAbi = ""; // 读取 Helloworld.abi 内容
    String contractBin = ""; // 读取 Helloworld.bin 内容
    logger.info("Contract abi: " + contractAbi + ", bin: " + contractBin);
    TransactionProcessor manager = TransactionProcessorFactory.createTransactionProcessor(client, client.getCryptoSuite().createKeyPair(), contractName, contractAbi, contractBin);
    TransactionResponse response = manager.deployAndGetResponse(new ArrayList<>());
    String contractAddress = response.getContractAddress();    
    logger.info("deploy contract , contract address: " + contractAddress);
    List<Object> paramsSet = new ArrayList<>();
    paramsSet.add("Hello, FISCO BCOS.");
    TransactionResponse ret1 = manager.sendTransactionAndGetResponse(contractAddress, "set", paramsSet);
    logger.info("send, receipt: " + JsonUtils.toJson(ret1));
    List<Object> paramsGet = new ArrayList<>();
    CallResponse ret2 = manager.sendCall(client.getCryptoSuite().getCryptoKeyPair().getAddress(), contractAddress, "get", paramsGet);
    List<Object> ret3 = JsonUtils.fromJsonList(ret2.getValues(), Object.class);
    logger.info("call to return object list, result: " + ret3);

    client.stop();
} catch (Exception e) {
    logger.error("error info: " + e.getMessage());
    e.printStackTrace();
}
```