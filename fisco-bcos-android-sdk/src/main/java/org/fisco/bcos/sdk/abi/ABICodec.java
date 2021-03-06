/*
 * Copyright 2014-2020  [fisco-dev]
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package org.fisco.bcos.sdk.abi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.fisco.bcos.sdk.abi.wrapper.ABICodecJsonWrapper;
import org.fisco.bcos.sdk.abi.wrapper.ABICodecObject;
import org.fisco.bcos.sdk.abi.wrapper.ABIDefinition;
import org.fisco.bcos.sdk.abi.wrapper.ABIDefinitionFactory;
import org.fisco.bcos.sdk.abi.wrapper.ABIObject;
import org.fisco.bcos.sdk.abi.wrapper.ABIObjectFactory;
import org.fisco.bcos.sdk.abi.wrapper.ContractABIDefinition;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.log.Logger;
import org.fisco.bcos.sdk.log.LoggerFactory;
import org.fisco.bcos.sdk.model.EventLog;

public class ABICodec {

    private static final Logger logger = LoggerFactory.getLogger(ABICodec.class);

    private final CryptoSuite cryptoSuite;
    public static final String TYPE_CONSTRUCTOR = "constructor";
    private ABIDefinitionFactory abiDefinitionFactory;
    private final ABIObjectFactory abiObjectFactory = new ABIObjectFactory();
    private final ABICodecJsonWrapper abiCodecJsonWrapper = new ABICodecJsonWrapper();

    public ABICodec(CryptoSuite cryptoSuite) {
        super();
        this.cryptoSuite = cryptoSuite;
        abiDefinitionFactory = new ABIDefinitionFactory(cryptoSuite);
    }

    public CryptoSuite getCryptoSuite() {
        return cryptoSuite;
    }

    public String encodeConstructor(String ABI, String BIN, List<Object> params)
            throws ABICodecException {

        ContractABIDefinition contractABIDefinition = abiDefinitionFactory.loadABI(ABI);
        ABIDefinition abiDefinition = contractABIDefinition.getConstructor();
        @SuppressWarnings("static-access")
        ABIObject inputABIObject = abiObjectFactory.createInputObject(abiDefinition);
        ABICodecObject abiCodecObject = new ABICodecObject();
        try {
            return BIN + abiCodecObject.encodeValue(inputABIObject, params).encode();
        } catch (Exception e) {
            logger.error(" exception in encodeMethodFromObject : {}", e.getMessage());
        }
        String errorMsg = " cannot encode in encodeMethodFromObject with appropriate interface ABI";
        logger.error(errorMsg);
        throw new ABICodecException(errorMsg);
    }

    public String encodeConstructorFromString(String ABI, String BIN, List<String> params)
            throws ABICodecException {
        ContractABIDefinition contractABIDefinition = abiDefinitionFactory.loadABI(ABI);
        ABIDefinition abiDefinition = contractABIDefinition.getConstructor();
        @SuppressWarnings("static-access")
        ABIObject inputABIObject = abiObjectFactory.createInputObject(abiDefinition);

        try {
            return BIN + abiCodecJsonWrapper.encode(inputABIObject, params).encode();
        } catch (Exception e) {
            logger.error(" exception in encodeMethodFromObject : {}", e.getMessage());
        }
        String errorMsg = " cannot encode in encodeMethodFromObject with appropriate interface ABI";
        logger.error(errorMsg);
        throw new ABICodecException(errorMsg);
    }

    public String encodeMethod(String ABI, String methodName, List<Object> params)
            throws ABICodecException {
        ContractABIDefinition contractABIDefinition = abiDefinitionFactory.loadABI(ABI);
        List<ABIDefinition> methods = contractABIDefinition.getFunctions().get(methodName);
        for (ABIDefinition abiDefinition : methods) {
            if (abiDefinition.getInputs().size() == params.size()) {
                @SuppressWarnings("static-access")
                ABIObject inputABIObject = abiObjectFactory.createInputObject(abiDefinition);
                ABICodecObject abiCodecObject = new ABICodecObject();
                try {
                    String methodId = abiDefinition.getMethodId(cryptoSuite);
                    return methodId + abiCodecObject.encodeValue(inputABIObject, params).encode();
                } catch (Exception e) {
                    logger.error(" exception in encodeMethodFromObject : {}", e.getMessage());
                }
            }
        }

        String errorMsg = " cannot encode in encodeMethodFromObject with appropriate interface ABI";
        logger.error(errorMsg);
        throw new ABICodecException(errorMsg);
    }

    public String encodeMethodById(String ABI, String methodId, List<Object> params)
            throws ABICodecException {
        ContractABIDefinition contractABIDefinition = abiDefinitionFactory.loadABI(ABI);
        ABIDefinition abiDefinition = contractABIDefinition.getABIDefinitionByMethodId(methodId);
        if (abiDefinition == null) {
            String errorMsg = " methodId " + methodId + " is invalid";
            logger.error(errorMsg);
            throw new ABICodecException(errorMsg);
        }
        ABIObject inputABIObject = abiObjectFactory.createInputObject(abiDefinition);
        ABICodecObject abiCodecObject = new ABICodecObject();
        try {
            return methodId + abiCodecObject.encodeValue(inputABIObject, params).encode();
        } catch (Exception e) {
            logger.error(" exception in encodeMethodByIdFromObject : {}", e.getMessage());
        }

        String errorMsg =
                " cannot encode in encodeMethodByIdFromObject with appropriate interface ABI";
        logger.error(errorMsg);
        throw new ABICodecException(errorMsg);
    }

    private ABIDefinition getABIDefinition(String methodInterface) throws ABICodecException {
        int start = methodInterface.indexOf("(");
        int end = methodInterface.lastIndexOf(")");
        if (start == -1 || end == -1 || start >= end) {
            String errorMsg = " error format";
            logger.error(errorMsg);
            throw new ABICodecException(errorMsg);
        }
        String name = methodInterface.substring(0, start);
        String type = methodInterface.substring(start + 1, end);
        if (type.indexOf("tuple") != -1) {
            String errorMsg = " cannot support tuple type";
            logger.error(errorMsg);
            throw new ABICodecException(errorMsg);
        }
        String[] types = type.split(",");
        List<ABIDefinition.NamedType> inputs = new ArrayList<ABIDefinition.NamedType>();
        for (String s : types) {
            ABIDefinition.NamedType input = new ABIDefinition.NamedType("name", s);
            inputs.add(input);
        }

        return new ABIDefinition(false, inputs, name, null, "function", false, "nonpayable");
    }

    public String encodeMethodByInterface(String methodInterface, List<Object> params)
            throws ABICodecException {
        ABIDefinition abiDefinition = getABIDefinition(methodInterface);
        if (abiDefinition.getInputs().size() == params.size()) {
            @SuppressWarnings("static-access")
            ABIObject inputABIObject = abiObjectFactory.createInputObject(abiDefinition);
            ABICodecObject abiCodecObject = new ABICodecObject();
            try {
                String methodId = abiDefinition.getMethodId(cryptoSuite);
                return methodId + abiCodecObject.encodeValue(inputABIObject, params).encode();
            } catch (Exception e) {
                logger.error(
                        " exception in encodeMethodByInterfaceFromObject : {}", e.getMessage());
            }
        }

        String errorMsg = " cannot encode in encodeMethodByInterfaceFromObject";
        logger.error(errorMsg);
        throw new ABICodecException(errorMsg);
    }

    public String encodeMethodFromString(String ABI, String methodName, List<String> params)
            throws ABICodecException {
        ContractABIDefinition contractABIDefinition = abiDefinitionFactory.loadABI(ABI);
        List<ABIDefinition> methods = contractABIDefinition.getFunctions().get(methodName);
        if (methods == null) {
            logger.debug(
                    "Invalid methodName: {}, all the functions are: {}",
                    methodName,
                    contractABIDefinition.getFunctions());
            throw new ABICodecException(
                    "Invalid method "
                            + methodName
                            + " , supported functions are: "
                            + contractABIDefinition.getFunctions().keySet());
        }
        for (ABIDefinition abiDefinition : methods) {
            if (abiDefinition.getInputs().size() == params.size()) {
                ABIObject inputABIObject = abiObjectFactory.createInputObject(abiDefinition);
                ABICodecJsonWrapper abiCodecJsonWrapper = new ABICodecJsonWrapper();
                try {
                    String methodId = abiDefinition.getMethodId(cryptoSuite);
                    return methodId + abiCodecJsonWrapper.encode(inputABIObject, params).encode();
                } catch (IOException e) {
                    logger.error(" exception in encodeMethodFromString : {}", e.getMessage());
                }
            }
        }

        String errorMsg = " cannot encode in encodeMethodFromString with appropriate interface ABI";
        logger.error(errorMsg);
        throw new ABICodecException(errorMsg);
    }

    public String encodeMethodByIdFromString(String ABI, String methodId, List<String> params)
            throws ABICodecException {
        ContractABIDefinition contractABIDefinition = abiDefinitionFactory.loadABI(ABI);
        ABIDefinition abiDefinition = contractABIDefinition.getABIDefinitionByMethodId(methodId);
        if (abiDefinition == null) {
            String errorMsg = " methodId " + methodId + " is invalid";
            logger.error(errorMsg);
            throw new ABICodecException(errorMsg);
        }
        ABIObject inputABIObject = abiObjectFactory.createInputObject(abiDefinition);
        ABICodecJsonWrapper abiCodecJsonWrapper = new ABICodecJsonWrapper();
        try {
            return methodId + abiCodecJsonWrapper.encode(inputABIObject, params).encode();
        } catch (IOException e) {
            logger.error(" exception in encodeMethodByIdFromString : {}", e.getMessage());
        }

        String errorMsg =
                " cannot encode in encodeMethodByIdFromString with appropriate interface ABI";
        logger.error(errorMsg);
        throw new ABICodecException(errorMsg);
    }

    public String encodeMethodByInterfaceFromString(String methodInterface, List<String> params)
            throws ABICodecException {
        ABIDefinition abiDefinition = getABIDefinition(methodInterface);
        if (abiDefinition.getInputs().size() == params.size()) {
            @SuppressWarnings("static-access")
            ABIObject inputABIObject = abiObjectFactory.createInputObject(abiDefinition);
            ABICodecJsonWrapper abiCodecJsonWrapper = new ABICodecJsonWrapper();
            try {
                String methodId = abiDefinition.getMethodId(cryptoSuite);
                return methodId + abiCodecJsonWrapper.encode(inputABIObject, params).encode();
            } catch (IOException e) {
                logger.error(
                        " exception in encodeMethodByInterfaceFromString : {}", e.getMessage());
            }
        }

        String errorMsg = " cannot encode in encodeMethodByInterfaceFromString";
        logger.error(errorMsg);
        throw new ABICodecException(errorMsg);
    }

    public Pair<List<Object>, List<ABIObject>> decodeMethodAndGetOutputObject(
            ABIDefinition abiDefinition, String output) throws ABICodecException {
        ABIObject outputABIObject = abiObjectFactory.createOutputObject(abiDefinition);
        ABICodecObject abiCodecObject = new ABICodecObject();
        try {
            return abiCodecObject.decodeJavaObjectAndOutputObject(outputABIObject, output);
        } catch (Exception e) {
            logger.error(" exception in decodeMethodToObject : {}", e.getMessage());
        }
        String errorMsg = " cannot decode in decodeMethodToObject with appropriate interface ABI";
        logger.error(errorMsg);
        throw new ABICodecException(errorMsg);
    }

    public Pair<List<Object>, List<ABIObject>> decodeMethodAndGetOutputObject(
            String ABI, String methodName, String output) throws ABICodecException {
        ContractABIDefinition contractABIDefinition = abiDefinitionFactory.loadABI(ABI);
        List<ABIDefinition> methods = contractABIDefinition.getFunctions().get(methodName);
        for (ABIDefinition abiDefinition : methods) {
            ABIObject outputABIObject = abiObjectFactory.createOutputObject(abiDefinition);
            ABICodecObject abiCodecObject = new ABICodecObject();
            try {
                return abiCodecObject.decodeJavaObjectAndOutputObject(outputABIObject, output);
            } catch (Exception e) {
                logger.error(" exception in decodeMethodToObject : {}", e.getMessage());
            }
        }

        String errorMsg = " cannot decode in decodeMethodToObject with appropriate interface ABI";
        logger.error(errorMsg);
        throw new ABICodecException(errorMsg);
    }

    public List<Object> decodeMethod(ABIDefinition abiDefinition, String output)
            throws ABICodecException {
        return decodeMethodAndGetOutputObject(abiDefinition, output).getLeft();
    }

    public List<Object> decodeMethod(String ABI, String methodName, String output)
            throws ABICodecException {
        return decodeMethodAndGetOutputObject(ABI, methodName, output).getLeft();
    }

    public List<Object> decodeMethodById(String ABI, String methodId, String output)
            throws ABICodecException {
        ContractABIDefinition contractABIDefinition = abiDefinitionFactory.loadABI(ABI);
        ABIDefinition abiDefinition = contractABIDefinition.getABIDefinitionByMethodId(methodId);
        if (abiDefinition == null) {
            String errorMsg = " methodId " + methodId + " is invalid";
            logger.error(errorMsg);
            throw new ABICodecException(errorMsg);
        }
        ABIObject outputABIObject = abiObjectFactory.createOutputObject(abiDefinition);
        ABICodecObject abiCodecObject = new ABICodecObject();
        try {
            return abiCodecObject.decodeJavaObject(outputABIObject, output);
        } catch (Exception e) {
            logger.error(" exception in decodeMethodByIdToObject : {}", e.getMessage());
        }

        String errorMsg = " cannot decode in decodeMethodToObject with appropriate interface ABI";
        logger.error(errorMsg);
        throw new ABICodecException(errorMsg);
    }

    public List<Object> decodeMethodByInterface(String ABI, String methodInterface, String output)
            throws ABICodecException {
        FunctionEncoder functionEncoder = new FunctionEncoder(cryptoSuite);
        String methodId = functionEncoder.buildMethodId(methodInterface);
        return decodeMethodById(ABI, methodId, output);
    }

    public List<String> decodeMethodToString(String ABI, String methodName, String output)
            throws ABICodecException {
        ContractABIDefinition contractABIDefinition = abiDefinitionFactory.loadABI(ABI);
        List<ABIDefinition> methods = contractABIDefinition.getFunctions().get(methodName);
        if (methods == null) {
            throw new ABICodecException(
                    "Invalid method "
                            + methodName
                            + ", supported methods are: "
                            + contractABIDefinition.getFunctions().keySet());
        }
        for (ABIDefinition abiDefinition : methods) {
            ABIObject outputABIObject = abiObjectFactory.createOutputObject(abiDefinition);
            ABICodecJsonWrapper abiCodecJsonWrapper = new ABICodecJsonWrapper();
            try {
                return abiCodecJsonWrapper.decode(outputABIObject, output);
            } catch (Exception e) {
                logger.error(" exception in decodeMethodToString : {}", e.getMessage());
            }
        }

        String errorMsg = " cannot decode in decodeMethodToString with appropriate interface ABI";
        logger.error(errorMsg);
        throw new ABICodecException(errorMsg);
    }

    public List<String> decodeMethodByIdToString(String ABI, String methodId, String output)
            throws ABICodecException {
        ContractABIDefinition contractABIDefinition = abiDefinitionFactory.loadABI(ABI);
        ABIDefinition abiDefinition = contractABIDefinition.getABIDefinitionByMethodId(methodId);
        if (abiDefinition == null) {
            String errorMsg = " methodId " + methodId + " is invalid";
            logger.error(errorMsg);
            throw new ABICodecException(errorMsg);
        }
        ABIObject outputABIObject = abiObjectFactory.createOutputObject(abiDefinition);
        ABICodecJsonWrapper abiCodecJsonWrapper = new ABICodecJsonWrapper();
        try {
            return abiCodecJsonWrapper.decode(outputABIObject, output);
        } catch (UnsupportedOperationException e) {
            logger.error(" exception in decodeMethodByIdToString : {}", e.getMessage());
        }

        String errorMsg =
                " cannot decode in decodeMethodByIdToString with appropriate interface ABI";
        logger.error(errorMsg);
        throw new ABICodecException(errorMsg);
    }

    public List<String> decodeMethodByInterfaceToString(
            String ABI, String methodInterface, String output) throws ABICodecException {
        FunctionEncoder functionEncoder = new FunctionEncoder(cryptoSuite);
        String methodId = functionEncoder.buildMethodId(methodInterface);
        return decodeMethodByIdToString(ABI, methodId, output);
    }

    public List<Object> decodeEvent(String ABI, String eventName, EventLog log)
            throws ABICodecException {
        ContractABIDefinition contractABIDefinition = abiDefinitionFactory.loadABI(ABI);
        List<ABIDefinition> events = contractABIDefinition.getEvents().get(eventName);
        if (events == null) {
            throw new ABICodecException(
                    "Invalid event "
                            + eventName
                            + ", supported events are: "
                            + contractABIDefinition.getEvents().keySet());
        }
        for (ABIDefinition abiDefinition : events) {
            ABIObject inputObject = abiObjectFactory.createEventInputObject(abiDefinition);
            ABICodecObject abiCodecObject = new ABICodecObject();
            try {
                List<Object> params = new ArrayList<>();
                if (!log.getData().equals("0x")) {
                    params = abiCodecObject.decodeJavaObject(inputObject, log.getData());
                }
                List<String> topics = log.getTopics();
                return mergeEventParamsAndTopics(abiDefinition, params, topics);
            } catch (Exception e) {
                logger.error(" exception in decodeEventToObject : {}", e.getMessage());
            }
        }

        String errorMsg = " cannot decode in decodeEventToObject with appropriate interface ABI";
        logger.error(errorMsg);
        throw new ABICodecException(errorMsg);
    }

    public List<Object> decodeEventByTopic(String ABI, String eventTopic, EventLog log)
            throws ABICodecException {
        ContractABIDefinition contractABIDefinition = abiDefinitionFactory.loadABI(ABI);
        ABIDefinition abiDefinition =
                contractABIDefinition.getABIDefinitionByEventTopic(eventTopic);
        ABIObject inputObject = abiObjectFactory.createEventInputObject(abiDefinition);
        ABICodecObject abiCodecObject = new ABICodecObject();
        try {
            List<Object> params = new ArrayList<>();
            if (!log.getData().equals("0x")) {
                params = abiCodecObject.decodeJavaObject(inputObject, log.getData());
            }
            List<String> topics = log.getTopics();
            return mergeEventParamsAndTopics(abiDefinition, params, topics);
        } catch (Exception e) {
            logger.error(" exception in decodeEventByTopicToObject : {}", e.getMessage());
        }

        String errorMsg =
                " cannot decode in decodeEventByTopicToObject with appropriate interface ABI";
        logger.error(errorMsg);
        throw new ABICodecException(errorMsg);
    }

    public List<Object> decodeEventByInterface(String ABI, String eventSignature, EventLog log)
            throws ABICodecException {
        FunctionEncoder functionEncoder = new FunctionEncoder(cryptoSuite);
        String methodId = functionEncoder.buildMethodId(eventSignature);
        return decodeEventByTopic(ABI, methodId, log);
    }

    public List<String> decodeEventToString(String ABI, String eventName, EventLog log)
            throws ABICodecException {
        ContractABIDefinition contractABIDefinition = abiDefinitionFactory.loadABI(ABI);
        List<ABIDefinition> events = contractABIDefinition.getEvents().get(eventName);
        if (events == null) {
            throw new ABICodecException(
                    "Invalid event "
                            + eventName
                            + ", current supported events are: "
                            + contractABIDefinition.getEvents().keySet());
        }
        for (ABIDefinition abiDefinition : events) {
            ABIObject inputObject = abiObjectFactory.createEventInputObject(abiDefinition);
            ABICodecJsonWrapper abiCodecJsonWrapper = new ABICodecJsonWrapper();
            try {
                List<String> params = new ArrayList<>();
                if (!log.getData().equals("0x")) {
                    params = abiCodecJsonWrapper.decode(inputObject, log.getData());
                }
                List<String> topics = log.getTopics();
                return mergeEventParamsAndTopicsToString(abiDefinition, params, topics);
            } catch (Exception e) {
                logger.error(" exception in decodeEventToString : {}", e.getMessage());
            }
        }

        String errorMsg = " cannot decode in decodeEventToString with appropriate interface ABI";
        logger.error(errorMsg);
        throw new ABICodecException(errorMsg);
    }

    public List<String> decodeEventByTopicToString(String ABI, String eventTopic, EventLog log)
            throws ABICodecException {
        ContractABIDefinition contractABIDefinition = abiDefinitionFactory.loadABI(ABI);
        ABIDefinition abiDefinition =
                contractABIDefinition.getABIDefinitionByEventTopic(eventTopic);
        ABIObject inputObject = abiObjectFactory.createEventInputObject(abiDefinition);
        ABICodecJsonWrapper abiCodecJsonWrapper = new ABICodecJsonWrapper();
        try {
            List<String> params = new ArrayList<>();
            if (!log.getData().equals("0x")) {
                params = abiCodecJsonWrapper.decode(inputObject, log.getData());
            }
            List<String> topics = log.getTopics();
            return mergeEventParamsAndTopicsToString(abiDefinition, params, topics);
        } catch (Exception e) {
            logger.error(" exception in decodeEventByTopicToString : {}", e.getMessage());
        }

        String errorMsg =
                " cannot decode in decodeEventByTopicToString with appropriate interface ABI";
        logger.error(errorMsg);
        throw new ABICodecException(errorMsg);
    }

    public List<String> decodeEventByInterfaceToString(
            String ABI, String eventSignature, EventLog log) throws ABICodecException {
        FunctionEncoder functionEncoder = new FunctionEncoder(cryptoSuite);
        String methodId = functionEncoder.buildMethodId(eventSignature);
        return decodeEventByTopicToString(ABI, methodId, log);
    }

    private List<Object> mergeEventParamsAndTopics(
            ABIDefinition abiDefinition, List<Object> params, List<String> topics) {
        List<Object> ret = new ArrayList<>();
        int paramIdx = 0;
        int topicIdx = 1;
        for (ABIDefinition.NamedType namedType : abiDefinition.getInputs()) {
            if (namedType.isIndexed()) {
                ret.add(topics.get(topicIdx));
                topicIdx++;
            } else {
                ret.add(params.get(paramIdx));
                paramIdx++;
            }
        }
        return ret;
    }

    private List<String> mergeEventParamsAndTopicsToString(
            ABIDefinition abiDefinition, List<String> params, List<String> topics) {
        List<String> ret = new ArrayList<>();
        int paramIdx = 0;
        int topicIdx = 1;
        for (ABIDefinition.NamedType namedType : abiDefinition.getInputs()) {
            if (namedType.isIndexed()) {
                ret.add(topics.get(topicIdx));
                topicIdx++;
            } else {
                ret.add(params.get(paramIdx));
                paramIdx++;
            }
        }
        return ret;
    }
}
