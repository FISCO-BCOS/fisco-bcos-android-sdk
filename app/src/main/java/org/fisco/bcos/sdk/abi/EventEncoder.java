package org.fisco.bcos.sdk.abi;

import org.fisco.bcos.sdk.abi.datatypes.Event;
import org.fisco.bcos.sdk.abi.datatypes.Type;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.utils.Numeric;
import org.fisco.bcos.sdk.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Ethereum filter encoding. Further limited details are available <a
 * href="https://github.com/ethereum/wiki/wiki/Ethereum-Contract-ABI#events">here</a>.
 */
public class EventEncoder {

    private CryptoSuite cryptoSuite;

    public EventEncoder(CryptoSuite cryptoSuite) {
        this.cryptoSuite = cryptoSuite;
    }

    public String encode(Event event) {

        String methodSignature = buildMethodSignature(event.getName(), event.getParameters());

        return buildEventSignature(methodSignature);
    }

    private <T extends Type> String buildMethodSignature(
            String methodName, List<TypeReference<T>> parameters) {

        StringBuilder result = new StringBuilder();
        result.append(methodName);
        result.append("(");
        /*String params =
                parameters.stream().map(p -> Utils.getTypeName(p)).collect(Collectors.joining(","));
        result.append(params);*/
        List<String> strList = new ArrayList<>();
        for (int i = 0; i < parameters.size(); i++) {
            strList.add(Utils.getTypeName(parameters.get(i)));
        }
        result.append(StringUtils.joinAll(",", strList));
        result.append(")");
        return result.toString();
    }

    public String buildEventSignature(String methodSignature) {
        byte[] input = methodSignature.getBytes();
        byte[] hash = cryptoSuite.hash(input);
        return Numeric.toHexString(hash);
    }

    /**
     * @return the cryptoSuite
     */
    public CryptoSuite getCryptoSuite() {
        return cryptoSuite;
    }

    /**
     * @param cryptoSuite the cryptoSuite to set
     */
    public void setCryptoSuite(CryptoSuite cryptoSuite) {
        this.cryptoSuite = cryptoSuite;
    }
}
