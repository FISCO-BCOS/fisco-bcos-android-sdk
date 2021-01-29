# fisco-bcos-android-sdk
android sdk for FISCO BCOS

- 用户使用`fisco-bcos-android-sdk`前，需部署 [节点接入代理服务 bcos-node-proxy](https://github.com/FISCO-BCOS/bcos-node-proxy/tree/feature_mobile_http) ，android sdk 通过节点代理与 FISCO-BCOS 节点进行通信。

- `fisco-bcos-android-sdk`支持`armeabi-v7a`和`arm64-v8a`两种架构，兼容的最低 Android 版本`minSdkVersion`为**21**，需获取**读写权限**及**网络访问权限**。

- `fisco-bcos-android-sdk`提供的接口均为**同步接口**，与区块链节点进行交互的接口涉及 http/https 请求，用户使用 android sdk 过程中需留意线程切换的影响。

## 使用说明

### 1. 获取权限

对于引入 android sdk 的项目，用户可在项目的配置文件`app/src/main/AndroidManifest.xml`中增加以下内容获取读写权限及网络访问权限。

```java
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="org.fisco.bcos.android.demo">

    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
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
    implementation 'org.slf4j:slf4j-api:1.7.21'
    implementation 'org.slf4j:slf4j-log4j12:1.7.30'
    implementation 'com.moandjiezana.toml:toml4j:0.7.2'
    implementation 'com.fasterxml.jackson.core:jackson-databind:2.9.0.pr3'
    implementation 'com.fasterxml.jackson.core:jackson-annotations:2.9.0.pr3'
    implementation 'com.fasterxml.jackson.core:jackson-core:2.9.0.pr3'
    implementation 'org.apache.commons:commons-lang3:3.1'
    implementation 'io.netty:netty-all:4.1.50.Final'
    implementation 'commons-io:commons-io:2.4'
    implementation 'com.squareup:javapoet:1.7.0'
    implementation 'info.picocli:picocli:3.6.0'
    implementation 'com.madgag.spongycastle:core:1.54.0.0'
    implementation 'com.madgag.spongycastle:prov:1.54.0.0'
    implementation 'com.madgag.spongycastle:pkix:1.54.0.0'
    implementation 'com.madgag.spongycastle:pg:1.54.0.0'
    implementation 'com.squareup.okhttp3:okhttp:3.13.0'
    implementation 'com.google.guava:guava:29.0-jre'
}
```

### 3. 初始化 sdk

项目初始化`fisco-bcos-android-sdk`的示例如下。

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

上述的`ProxyConfig`作为初始化 android sdk 的配置项，包括以下内容。

| 设置项             | 是否可选 | 说明                                                           | 
| ----------------- | ------- | --------------------------------------------------------------|
| chainId           | 必选    | 链标识，需与 FISCO BCOS 节点配置的一致                             |
| crytoType         | 必选    | 是否使用国密，需与 FISCO BCOS 节点配置的一致，目前支持 ECDSA_TYPE（0）|
| hexPrivateKey     | 必选    | 发交易进行签名使用的私钥                                           |
| networkHandlerImp | 可选    | http请求实现，如不存入，采用 sdk 内部实现                           |

### 4. 编译合约

用户基于智能合约开发具体应用前，需基于合约编译生成一个与合约同名的 Java 类。编译 Java 类的过程如下。

- 使用`java -version`查询 JDK 版本，要求版本大于等于1.8，推荐使用 JDK 14；
- 在`tool/`目录执行`bash get_console.sh`下载控制台，下载完成后在当前目录下生成`console/`目录；
- 将需编译的合约放于`tool/console/contracts/solidity`目录；
- 在`tool/console/`目录执行`bash sol2java.sh org.fisco.bcos.sdk`，脚本`org.fisco.bcos.sdk`指定生成的 Java 类的包名；
- 编译生成的 Java 文件放置于`tool/console/contracts/sdk/java`目录。

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
} catch (Exception e) {
    logger.error("error info: " + e.getMessage());
}
```