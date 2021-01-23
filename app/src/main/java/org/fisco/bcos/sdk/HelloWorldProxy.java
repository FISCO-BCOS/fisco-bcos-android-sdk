package org.fisco.bcos.sdk;

import org.fisco.bcos.sdk.abi.FunctionEncoder;
import org.fisco.bcos.sdk.abi.FunctionReturnDecoder;
import org.fisco.bcos.sdk.abi.TypeReference;
import org.fisco.bcos.sdk.abi.datatypes.Function;
import org.fisco.bcos.sdk.abi.datatypes.Type;
import org.fisco.bcos.sdk.abi.datatypes.Utf8String;
import org.fisco.bcos.sdk.abi.datatypes.generated.StaticArray2;
import org.fisco.bcos.sdk.abi.datatypes.generated.Uint256;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple1;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple2;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.contract.Contract;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.model.CryptoType;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;
import org.fisco.bcos.sdk.utils.StringUtils;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("unchecked")
public class HelloWorldProxy extends Contract {
    public static final String[] BINARY_ARRAY = {"608060405234801561001057600080fd5b50604051610575380380610575833981018060405281019080805182019291905050508060009080519060200190610049929190610050565b50506100f5565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f1061009157805160ff19168380011785556100bf565b828001600101855582156100bf579182015b828111156100be5782518255916020019190600101906100a3565b5b5090506100cc91906100d0565b5090565b6100f291905b808211156100ee5760008160009055506001016100d6565b5090565b90565b610471806101046000396000f300608060405260043610610062576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff1680634ed3885e146100675780636d4ce63c146100d0578063942b765a14610160578063d51033db146101b3575b600080fd5b34801561007357600080fd5b506100ce600480360381019080803590602001908201803590602001908080601f016020809104026020016040519081016040528093929190818152602001838380828437820191505050505050919291929050505061024a565b005b3480156100dc57600080fd5b506100e5610264565b6040518080602001828103825283818151815260200191508051906020019080838360005b8381101561012557808201518184015260208101905061010a565b50505050905090810190601f1680156101525780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b34801561016c57600080fd5b50610175610306565b6040518082600260200280838360005b838110156101a0578082015181840152602081019050610185565b5050505090500191505060405180910390f35b3480156101bf57600080fd5b506101c8610336565b6040518080602001838152602001828103825284818151815260200191508051906020019080838360005b8381101561020e5780820151818401526020810190506101f3565b50505050905090810190601f16801561023b5780820380516001836020036101000a031916815260200191505b50935050505060405180910390f35b806000908051906020019061026092919061037e565b5050565b606060008054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156102fc5780601f106102d1576101008083540402835291602001916102fc565b820191906000526020600020905b8154815290600101906020018083116102df57829003601f168201915b5050505050905090565b61030e6103fe565b6103166103fe565b604080519081016040528060018152602001600281525090508091505090565b6060600060036040805190810160405280600381526020017f616263000000000000000000000000000000000000000000000000000000000081525090809050915091509091565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f106103bf57805160ff19168380011785556103ed565b828001600101855582156103ed579182015b828111156103ec5782518255916020019190600101906103d1565b5b5090506103fa9190610420565b5090565b6040805190810160405280600290602082028038833980820191505090505090565b61044291905b8082111561043e576000816000905550600101610426565b5090565b905600a165627a7a72305820298f3f0bfcf7e076a0165cdfb4dfb8bcade5f9b6964a3c31fc6e098ad38c4a270029"};

    public static final String BINARY = StringUtils.joinAll("", BINARY_ARRAY);

    public static final String[] SM_BINARY_ARRAY = {"608060405234801561001057600080fd5b50604051610575380380610575833981018060405281019080805182019291905050508060009080519060200190610049929190610050565b50506100f5565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f1061009157805160ff19168380011785556100bf565b828001600101855582156100bf579182015b828111156100be5782518255916020019190600101906100a3565b5b5090506100cc91906100d0565b5090565b6100f291905b808211156100ee5760008160009055506001016100d6565b5090565b90565b610471806101046000396000f300608060405260043610610062576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff168063299f7f9d146100675780633590b49f146100f7578063a23fa28914610160578063edecef4e146101b3575b600080fd5b34801561007357600080fd5b5061007c61024a565b6040518080602001828103825283818151815260200191508051906020019080838360005b838110156100bc5780820151818401526020810190506100a1565b50505050905090810190601f1680156100e95780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b34801561010357600080fd5b5061015e600480360381019080803590602001908201803590602001908080601f01602080910402602001604051908101604052809392919081815260200183838082843782019150505050505091929192905050506102ec565b005b34801561016c57600080fd5b50610175610306565b6040518082600260200280838360005b838110156101a0578082015181840152602081019050610185565b5050505090500191505060405180910390f35b3480156101bf57600080fd5b506101c8610336565b6040518080602001838152602001828103825284818151815260200191508051906020019080838360005b8381101561020e5780820151818401526020810190506101f3565b50505050905090810190601f16801561023b5780820380516001836020036101000a031916815260200191505b50935050505060405180910390f35b606060008054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156102e25780601f106102b7576101008083540402835291602001916102e2565b820191906000526020600020905b8154815290600101906020018083116102c557829003601f168201915b5050505050905090565b806000908051906020019061030292919061037e565b5050565b61030e6103fe565b6103166103fe565b604080519081016040528060018152602001600281525090508091505090565b6060600060036040805190810160405280600381526020017f616263000000000000000000000000000000000000000000000000000000000081525090809050915091509091565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f106103bf57805160ff19168380011785556103ed565b828001600101855582156103ed579182015b828111156103ec5782518255916020019190600101906103d1565b5b5090506103fa9190610420565b5090565b6040805190810160405280600290602082028038833980820191505090505090565b61044291905b8082111561043e576000816000905550600101610426565b5090565b905600a165627a7a723058205cb1d867a9508bdc2c019b6af94cae689c304b77b20b070ce0b931db65a2ad380029"};

    public static final String SM_BINARY = StringUtils.joinAll("", SM_BINARY_ARRAY);

    public static final String[] ABI_ARRAY = {"[{\"constant\":false,\"inputs\":[{\"name\":\"n\",\"type\":\"string\"}],\"name\":\"set\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"get\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"getList\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256[2]\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"getTuple\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"},{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[{\"name\":\"str\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"}]"};

    public static final String ABI = StringUtils.joinAll("", ABI_ARRAY);

    public static final String FUNC_SET = "set";

    public static final String FUNC_GET = "get";

    public static final String FUNC_GETLIST = "getList";

    public static final String FUNC_GETTUPLE = "getTuple";

    protected HelloWorldProxy(String contractAddress, Client client, CryptoKeyPair credential) {
        super(getBinary(client.getCryptoSuite()), contractAddress, client, credential);
    }

    public static String getBinary(CryptoSuite cryptoSuite) {
        return (cryptoSuite.getCryptoTypeConfig() == CryptoType.ECDSA_TYPE ? BINARY : SM_BINARY);
    }

    public static HelloWorldProxy load(String contractAddress, Client client, CryptoKeyPair credential) {
        return new HelloWorldProxy(contractAddress, client, credential);
    }

    public static HelloWorldProxy deploy(Client client, CryptoKeyPair credential, String str) throws ContractException {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.fisco.bcos.sdk.abi.datatypes.Utf8String(str)));
        return deploy(HelloWorldProxy.class, client, credential, getBinary(client.getCryptoSuite()), encodedConstructor);
    }

    public TransactionReceipt set(String n) {
        final Function function = new Function(
                FUNC_SET,
                Arrays.<Type>asList(new org.fisco.bcos.sdk.abi.datatypes.Utf8String(n)),
                Collections.<TypeReference<?>>emptyList());
        return executeTransaction(function);
    }

    public void set(String n, TransactionCallback callback) {
        final Function function = new Function(
                FUNC_SET,
                Arrays.<Type>asList(new org.fisco.bcos.sdk.abi.datatypes.Utf8String(n)),
                Collections.<TypeReference<?>>emptyList());
        asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForSet(String n) {
        final Function function = new Function(
                FUNC_SET,
                Arrays.<Type>asList(new org.fisco.bcos.sdk.abi.datatypes.Utf8String(n)),
                Collections.<TypeReference<?>>emptyList());
        return createSignedTransaction(function);
    }

    public Tuple1<String> getSetInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function = new Function(FUNC_SET,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {
                }));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<String>(

                (String) results.get(0).getValue()
        );
    }

    public String get() throws ContractException {
        final Function function = new Function(FUNC_GET,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {
                }));
        return executeCallWithSingleValueReturn(function, String.class);
    }

    public List getList() throws ContractException {
        final Function function = new Function(FUNC_GETLIST,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<StaticArray2<Uint256>>() {
                }));
        List<Type> result = (List<Type>) executeCallWithSingleValueReturn(function, List.class);
        return convertToNative(result);
    }

    public Tuple2<String, BigInteger> getTuple() throws ContractException {
        final Function function = new Function(FUNC_GETTUPLE,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {
                }, new TypeReference<Uint256>() {
                }));
        List<Type> results = executeCallWithMultipleValueReturn(function);
        return new Tuple2<String, BigInteger>(
                (String) results.get(0).getValue(),
                (BigInteger) results.get(1).getValue());
    }
}
