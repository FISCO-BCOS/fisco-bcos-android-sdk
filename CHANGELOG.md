### v1.0.0

(2021-03-18)

**新增**

- 基于智能合约编译生成的 Java Wrapper 类实现合约的部署及调用
- 基于智能合约编译生成的 abi 和 binary 文件实现合约的部署及调用
- 支持发送国密/非国密交易
- 提供 FISCO BCOS 6 个基础 [JSON-RPC](https://fisco-bcos-documentation.readthedocs.io/zh_CN/latest/docs/api.html) 的调用，包括 sendRawTransaction、call、getClientVersion、getBlockNumber、getTransactionByHash 和 getTransactionReceipt
- 使用 https 协议与 FISCO BCOS 节点接入代理服务 bcos-node-proxy 进行通信，并对 proxy 进行验证
- 提供 android-sdk 的使用 demo

关于 android-sdk 的具体使用，请参考[文档](https://fisco-bcos-documentation.readthedocs.io/zh_CN/latest/docs/sdk/android_sdk/index.html)。