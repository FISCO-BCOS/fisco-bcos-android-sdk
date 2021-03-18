# fisco-bcos-android-sdk

开发者可在 Android 应用中通过 fisco-bcos-android-sdk（以下简称 android-sdk）实现对 FISCO-BCOS 区块链的操作。目前，android-sdk 支持 FISCO BCOS 2.0+，实现的功能包括:

- 查询区块链数据
- 部署及调用合约
- 解析合约出参和交易回执

对于**部署及调用合约**，android-sdk 现有的接口能满足开发者的多种需求：

- 使用开发者传入的私钥/随机私钥发送交易
- 发送国密/非国密交易
- 基于合约 Java 类部署及调用合约
- 基于合约 abi 和 binary 部署及调用合约

| 重要事项      | 说明                                                                                                                                                                     |
| ------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| 服务依赖      | [节点接入代理服务 bcos-node-proxy](https://github.com/FISCO-BCOS/bcos-node-proxy)                                                                                        |
| 包含架构      | armeabi-v7a 和 arm64-v8a                                                                                                                                                 |
| 支持 API 下限 | 21（平台版本 Android 5.0）                                                                                                                                               |
| 所需权限      | 读权限（用于获取服务端证书）、网络访问权限（用于访问区块链节点接入代理服务）                                                                                             |
| 依赖大小      | 整体 4M，其中 android-sdk 大小约 1M，sdk 使用的[依赖](https://fisco-bcos-documentation.readthedocs.io/zh_CN/latest/docs/sdk/android_sdk/quick_start.html#sdk)总大小约 3M |

关于如何基于 android-sdk 开发区块链应用，请参考[详细文档](https://fisco-bcos-documentation.readthedocs.io/zh_CN/latest/docs/sdk/android_sdk/index.html)。