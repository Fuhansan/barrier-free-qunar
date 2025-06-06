package com.qunar.barrier_free_qunar.java.sdk.model;

/**
 * HTTP 接口返回前端参数格式
 * @param <T>
 */
public class ApiResponse<T> {

    private int code;
    private String msg;
    private T data;


    public static <T> ApiResponse<T> success(){
        ApiResponse<T> tApiResponse = new ApiResponse<>();
        tApiResponse.setCode(200);
        tApiResponse.setMsg("success");
        return tApiResponse;
    }


    public static <T> ApiResponse<T> success(T data){
        ApiResponse<T> tApiResponse = new ApiResponse<>();
        tApiResponse.setCode(200);
        tApiResponse.setMsg("success");
        tApiResponse.setData(data);
        return tApiResponse;
    }


    public static <T> ApiResponse<T> success(String msg){
        ApiResponse<T> tApiResponse = new ApiResponse<>();
        tApiResponse.setCode(200);
        tApiResponse.setMsg(msg);
        return tApiResponse;
    }


    public static <T> ApiResponse<T> success(T data, String msg){
        ApiResponse<T> tApiResponse = new ApiResponse<>();
        tApiResponse.setCode(200);
        tApiResponse.setMsg(msg);
        tApiResponse.setData(data);
        return tApiResponse;
    }


    public static <T> ApiResponse<T> fail(T data, String err){
        ApiResponse<T> tApiResponse = new ApiResponse<>();
        tApiResponse.setCode(200);
        tApiResponse.setMsg(err);
        tApiResponse.setData(data);
        return tApiResponse;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
