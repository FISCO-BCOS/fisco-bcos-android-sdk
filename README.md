# fisco-bcos-android-sdk
android sdk for FISCO-BCOS

用户开发 Android 区块链应用时可通过引入`fisco-bcos-android-sdk`与 FISCO-BCOS 进行交互，交互内容包括**查询区块链状态**及**发送交易**。

| 注意事项      | 说明                        | 
| ------------ | ------------------------- | 
| 使用准备      | 部署 [节点接入代理服务 bcos-node-proxy](https://github.com/FISCO-BCOS/bcos-node-proxy/tree/feature_mobile_http) ，android sdk 通过节点代理与区块链节点进行通信 |
| 架构提供      | `armeabi-v7a`、`arm64-v8a` |
| Android API | 21+                         | 
| 权限申请      | 读权限、网络访问权限          | 
| sdk arr 大小  | 1M                         |
| 编译依赖      | 详见`2. 添加依赖`内容         |
| 网络请求      | 与区块链节点进行交互的`http/https`服务接口均为**同步接口**，用户使用 android sdk 过程中需留意线程切换的影响 |

## 如何基于 fisco-bcos-android-sdk 开发区块链应用

关于如何安装 Android 集成开发环境及如何创建一个 Android 应用，请参考[Android 开发者手册](https://developer.android.google.cn/studio/intro)，此处不再赘述。本章节重点描述如何在一个应用中使用`fisco-bcos-android-sdk`与区块链节点进行交互。

### 1. 获取权限

对于引入 android sdk 的项目，用户可在项目的配置文件`app/src/main/AndroidManifest.xml`中增加以下内容获取读权限及网络访问权限。

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

项目初始化`fisco-bcos-android-sdk`的示例如下。

初始化 android sdk 时需提供`ProxyConfig`配置信息，包括以下内容。

| 设置项             | 是否可选 | 说明                                                           | 
| ----------------- | ------- | --------------------------------------------------------------|
| chainId           | 必选    | 链标识，需与 FISCO-BCOS 节点配置的一致                             |
| crytoType         | 必选    | 是否使用国密交易及账号，需与 FISCO-BCOS 节点配置的一致，目前可选值包括 ECDSA_TYPE 和 SM_TYPE |
| hexPrivateKey     | 可选    | 发交易进行签名使用的私钥，用户可从文件或数据库中读取进行设置，如不设置，sdk 内部随机生成                   |
| networkHandler    | 可选    | http/https 请求实现，用户可自行实现并传入，如不传入，采用 sdk 内部实现  |

上述的`networkHandler`提供了`http`和`https`两种传输协议的内置实现。其中，`NetworkHandlerImp`实现`http`请求，直接访问`Bcos-node-proxy`；`NetworkHandlerHttpsImp`实现`https`请求，通过 Nginx 访问`Bcos-node-proxy`，需新建`assets`目录放置 Nginx 的证书。

基于`NetworkHandlerImp`初始化 android sdk 例子如下。

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

基于`NetworkHandlerHttpsImp`初始化 android sdk 例子如下。

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

用户基于智能合约开发具体应用前，需基于合约编译生成一个与合约同名的 Java 类。编译 Java 类的过程如下。

- 使用`java -version`查询 JDK 版本，要求版本大于等于1.8，推荐使用 JDK 14；
- 在`tool/`目录执行`bash get_console.sh`下载控制台，下载完成后在当前目录下生成`console/`目录；
- 将需编译的合约放于`tool/console/contracts/solidity`目录（该目录已内置一些合约，如`Helloworld.sol`）；
- 在`tool/console/`目录执行`bash sol2java.sh org.fisco.bcos.sdk`，脚本`org.fisco.bcos.sdk`指定生成的 Java 类的包名；
- 编译生成的 Java 文件（如`Helloworld.java`）放置于`tool/console/contracts/sdk/java`目录。

### 5. 部署及调用合约

以`tool/console/contracts/sdk/java`中的`Helloworld.java`为例说明如何部署、加载及调用合约。调用以下代码前需先将`Helloworld.java`引入项目中。

```Java
try {
    Client client = sdk.getClient(1);
    NodeVersion nodeVersion = client.getClientNodeVersion();
    logger.info("node version: " + JsonUtils.toJson(nodeVersion));
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