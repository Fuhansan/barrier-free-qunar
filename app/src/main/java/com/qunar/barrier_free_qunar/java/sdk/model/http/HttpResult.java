package com.qunar.barrier_free_qunar.java.sdk.model.http;

import java.lang.reflect.Type;

import com.google.gson.Gson;


public class HttpResult {
    private boolean success;
    private int code;
    private String msg;
    private Object data;

    private static Gson gson = new Gson();

    public HttpResult(boolean success, int code, String msg, Object data) {
        this.success = success;
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    // Getters
    public boolean isSuccess() {
        return success;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data){
        this.data = data;
    }




    @Override
    public String toString() {
        return "HttpResult{" +
                "success=" + success +
                ", code=" + code +
                ", message='" + msg + '\'' +
                ", data='" + data + '\'' +
                '}';
    }
}