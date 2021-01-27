package org.fisco.bcos.sdk.abi.datatypes;

import org.fisco.bcos.sdk.abi.TypeReference;
import org.fisco.bcos.sdk.abi.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Event wrapper type.
 */
public class Event {
    private String name;
    private List<TypeReference<Type>> parameters;

    public Event(String name, List<TypeReference<?>> parameters) {
        this.name = name;
        this.parameters = Utils.convert(parameters);
    }

    public String getName() {
        return name;
    }

    public List<TypeReference<Type>> getParameters() {
        return parameters;
    }

    public List<TypeReference<Type>> getIndexedParameters() {
        //return parameters.stream().filter(TypeReference::isIndexed).collect(Collectors.toList());
        List<TypeReference<Type>> ret = new ArrayList<>();
        for (int i = 0; i < parameters.size(); i++) {
            if (parameters.get(i).isIndexed()) {
                ret.add(parameters.get(i));
            }
        }
        return ret;
    }

    public List<TypeReference<Type>> getNonIndexedParameters() {
        //return parameters.stream().filter(p -> !p.isIndexed()).collect(Collectors.toList());
        List<TypeReference<Type>> ret = new ArrayList<>();
        for (int i = 0; i < parameters.size(); i++) {
            if (!parameters.get(i).isIndexed()) {
                ret.add(parameters.get(i));
            }
        }
        return ret;
    }
}
