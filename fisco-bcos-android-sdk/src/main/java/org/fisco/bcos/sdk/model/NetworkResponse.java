package org.fisco.bcos.sdk.model;

public class NetworkResponse<T> {

    // the value of `code` and `message` have been described in class NetworkResponseCode
    int code;
    String message;
    T result;

    public NetworkResponse(int code, String message, T result) {
        this.code = code;
        this.message = message;
        this.result = result;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }
}
