package org.fisco.bcos.sdk.abi;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.fisco.bcos.sdk.abi.datatypes.Function;
import org.fisco.bcos.sdk.abi.datatypes.Type;
import org.fisco.bcos.sdk.abi.datatypes.Uint;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.utils.Numeric;
import org.fisco.bcos.sdk.utils.StringUtils;

/**
 * Ethereum Contract Application Binary Interface (ABI) encoding for functions. Further details are
 * available <a href="https://github.com/ethereum/wiki/wiki/Ethereum-Contract-ABI">here</a>.
 */
public class FunctionEncoder {

    private CryptoSuite cryptoSuite;

    public FunctionEncoder(CryptoSuite cryptoSuite) {
        this.cryptoSuite = cryptoSuite;
    }

    public String encode(Function function) {
        List<Type> parameters = function.getInputParameters();

        String methodSignature = buildMethodSignature(function.getName(), parameters);
        String methodId = buildMethodId(methodSignature);

        StringBuilder result = new StringBuilder();
        result.append(methodId);

        return encodeParameters(parameters, result);
    }

    public static String encodeConstructor(List<Type> parameters) {
        return encodeParameters(parameters, new StringBuilder());
    }

    public static String encodeParameters(List<Type> parameters, StringBuilder result) {
        int dynamicDataOffset = Utils.getLength(parameters) * Type.MAX_BYTE_LENGTH;
        StringBuilder dynamicData = new StringBuilder();

        for (Type parameter : parameters) {
            String encodedValue = TypeEncoder.encode(parameter);

            if (parameter.dynamicType()) {
                String encodedDataOffset =
                        TypeEncoder.encodeNumeric(new Uint(BigInteger.valueOf(dynamicDataOffset)));
                result.append(encodedDataOffset);
                dynamicData.append(encodedValue);
                dynamicDataOffset += (encodedValue.length() >> 1);
            } else {
                result.append(encodedValue);
            }
        }
        result.append(dynamicData);

        return result.toString();
    }

    public static String buildMethodSignature(String methodName, List<Type> parameters) {
        StringBuilder result = new StringBuilder();
        result.append(methodName);
        result.append("(");
        /*String params =
                parameters.stream().map(Type::getTypeAsString).collect(Collectors.joining(","));
        result.append(params);*/
        List<String> strList = new ArrayList<>();
        for (int i = 0; i < parameters.size(); i++) {
            strList.add(parameters.get(i).getTypeAsString());
        }
        result.append(StringUtils.joinAll(",", strList));
        result.append(")");
        return result.toString();
    }

    public String buildMethodId(String methodSignature) {
        byte[] input = methodSignature.getBytes();
        byte[] hash = cryptoSuite.hash(input);
        return Numeric.toHexString(hash).substring(0, 10);
    }

    /** @return the cryptoSuite */
    public CryptoSuite getCryptoSuite() {
        return cryptoSuite;
    }

    /** @param cryptoSuite the cryptoSuite to set */
    public void setCryptoSuite(CryptoSuite cryptoSuite) {
        this.cryptoSuite = cryptoSuite;
    }
}
