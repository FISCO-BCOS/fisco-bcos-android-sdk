# fisco-bcos-android-sdk
android sdk for FISCO BCOS

## 使用说明

1. 用户需提前部署 [bcos-node-proxy](https://github.com/FISCO-BCOS/bcos-node-proxy/tree/feature_mobile_http) 节点接入代理服务，android sdk 通过节点代理与 FISCO BCOS 节点进行通信。

2. 引入 android sdk 的项目要求`minSdkVersion`不少于**21**，也需获取**读写权限**及**网络权限**，用户可在`app/src/main/AndroidManifest.xml`中增加以下内容获取相应权限。

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

3. 修改`app/build.gradle`，添加以下源和依赖。

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

4. 初始化 sdk

初始化`fisco-bcos-android-sdk`时参考以下实现，需提供表格中的内容。

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

| 设置项             | 是否可选 | 说明                                                           | 
| ----------------- | ------- | --------------------------------------------------------------|
| chainId           | 必选    | 链标识，需与 FISCO BCOS 节点配置的一致                             |
| crytoType         | 必选    | 是否使用国密，需与 FISCO BCOS 节点配置的一致，目前支持 ECDSA_TYPE（0）|
| hexPrivateKey     | 必选    | 发交易进行签名使用的私钥                                           |
| networkHandlerImp | 可选    | http请求实现，如不存入，采用 sdk 内部实现                           |

5. 合约编译

- 确认JDK版本：要求JDK版本大于等于1.8，推荐使用JDK 14；
- 下载控制台：`curl -#LO https://github.com/FISCO-BCOS/console/releases/download/v2.7.1/download_console.sh && bash download_console.sh`；
- 将需编译的合约放于`console/contracts/solidity`目录；
- 在`console/`目录执行`./sol2java.sh org.fisco.bcos.sdk`，脚本参数唯一，指定生成的 Java 文件的包名；
- 编译生成的 Java 文件放置于`console/contracts/sdk/java`目录。

6. 合约调用

以`console/contracts/sdk/java`中的`Helloworld.java`为例说明如何部署、加载及调用合约。需先将`Helloworld.java`引入项目中。

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