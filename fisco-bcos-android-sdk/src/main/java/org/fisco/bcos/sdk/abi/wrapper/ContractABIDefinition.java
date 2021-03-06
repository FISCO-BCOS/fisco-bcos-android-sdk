package org.fisco.bcos.sdk.abi.wrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.log.Logger;
import org.fisco.bcos.sdk.log.LoggerFactory;
import org.fisco.bcos.sdk.utils.Numeric;

public class ContractABIDefinition {

    private static final Logger logger = LoggerFactory.getLogger(ContractABIDefinition.class);

    private ABIDefinition constructor = null;
    private Map<String, List<ABIDefinition>> functions = new HashMap<>();
    private Map<String, List<ABIDefinition>> events = new HashMap<>();
    // method id => function
    private Map<String, ABIDefinition> methodIDToFunctions = new HashMap<>();
    // event topic => topic
    private Map<String, ABIDefinition> eventTopicToEvents = new HashMap<>();
    private CryptoSuite cryptoSuite;

    public ContractABIDefinition(CryptoSuite cryptoSuite) {
        this.cryptoSuite = cryptoSuite;
    }

    public ABIDefinition getConstructor() {
        return constructor;
    }

    public void setConstructor(ABIDefinition constructor) {
        this.constructor = constructor;
    }

    public Map<String, List<ABIDefinition>> getFunctions() {
        return functions;
    }

    public void setFunctions(Map<String, List<ABIDefinition>> functions) {
        this.functions = functions;
    }

    public Map<String, List<ABIDefinition>> getEvents() {
        return events;
    }

    public void setEvents(Map<String, List<ABIDefinition>> events) {
        this.events = events;
    }

    public Map<String, ABIDefinition> getMethodIDToFunctions() {
        return methodIDToFunctions;
    }

    public void setMethodIDToFunctions(Map<String, ABIDefinition> methodIDToFunctions) {
        this.methodIDToFunctions = methodIDToFunctions;
    }

    public Map<String, ABIDefinition> getEventTopicToEvents() {
        return eventTopicToEvents;
    }

    public void setEventTopicToEvents(Map<String, ABIDefinition> eventTopicToEvents) {
        this.eventTopicToEvents = eventTopicToEvents;
    }

    public void addFunction(String name, ABIDefinition abiDefinition) {

        List<ABIDefinition> abiDefinitions = functions.get(name);
        if (abiDefinitions == null) {
            functions.put(name, new ArrayList<>());
            abiDefinitions = functions.get(name);
        } else {
            logger.info(" overload method ??? name: {}, abiDefinition: {}", name, abiDefinition);
        }
        abiDefinitions.add(abiDefinition);

        // calculate method id and add abiDefinition to methodIdToFunctions
        String methodId = abiDefinition.getMethodId(cryptoSuite);
        methodIDToFunctions.put(methodId, abiDefinition);

        logger.info(
                " name: {}, methodId: {}, methodSignature: {}, abi: {}",
                name,
                methodId,
                abiDefinition.getMethodSignatureAsString(),
                abiDefinition);
    }

    public void addEvent(String name, ABIDefinition abiDefinition) {
        // events.putIfAbsent(name, new ArrayList<>());
        if (!events.containsKey(name) || events.get(name) == null) {
            events.put(name, new ArrayList<>());
        }
        List<ABIDefinition> abiDefinitions = events.get(name);
        abiDefinitions.add(abiDefinition);
        logger.info(" name: {}, abi: {}", name, abiDefinition);

        // calculate method id and add abiDefinition to eventTopicToEvents
        String methodId = abiDefinition.getMethodId(cryptoSuite);
        eventTopicToEvents.put(methodId, abiDefinition);
    }

    public ABIDefinition getABIDefinitionByMethodId(String methodId) {
        return methodIDToFunctions.get(Numeric.prependHexPrefix(methodId));
    }

    public ABIDefinition getABIDefinitionByEventTopic(String topic) {
        return eventTopicToEvents.get(Numeric.prependHexPrefix(topic));
    }
}
