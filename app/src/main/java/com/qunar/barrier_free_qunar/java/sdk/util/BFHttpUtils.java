package com.qunar.barrier_free_qunar.java.sdk.util;

import android.util.Log;
import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.qunar.barrier_free_qunar.java.sdk.model.http.HttpResult;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.lang.reflect.Type;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;
import java.io.InputStream;

/**
 * BFHttpUtils - 基于OkHttp的HTTP工具类
 * 支持普通请求和流式请求
 */
public class BFHttpUtils {
    
    private static final int DEFAULT_TIMEOUT = 30; // 30秒
    private static final int STREAM_TIMEOUT = 120; // 120秒
    private static final int MAX_RETRY_COUNT = 3; // 最大重试次数
    private static final int CONNECTION_POOL_MAX_IDLE = 5; // 连接池最大空闲连接数
    private static final int CONNECTION_POOL_KEEP_ALIVE = 5; // 连接保持时间（分钟）
    
    private static OkHttpClient normalClient;
    private static OkHttpClient streamClient;

    private static Gson gson = new Gson();

    
    static {
        initClients();
    }
    
    /**
     * 初始化HTTP客户端
     */
    private static void initClients() {
        // 日志拦截器
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
        
        // 连接池配置
        ConnectionPool connectionPool = new ConnectionPool(
                CONNECTION_POOL_MAX_IDLE, 
                CONNECTION_POOL_KEEP_ALIVE, 
                TimeUnit.MINUTES
        );
        
        // 普通请求客户端
        normalClient = new OkHttpClient.Builder()
                .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .connectionPool(connectionPool)
                .addInterceptor(loggingInterceptor)
                .addInterceptor(new UserAgentInterceptor())
                .retryOnConnectionFailure(true) // 启用连接失败重试
                .followRedirects(true) // 允许重定向
                .followSslRedirects(true) // 允许SSL重定向
                .build();
        
        // 流式请求客户端（更长的超时时间和连接池配置）
        streamClient = new OkHttpClient.Builder()
                .connectTimeout(STREAM_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(STREAM_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(STREAM_TIMEOUT, TimeUnit.SECONDS)
                .connectionPool(connectionPool)
                .addInterceptor(loggingInterceptor)
                .addInterceptor(new UserAgentInterceptor())
                .retryOnConnectionFailure(true) // 启用连接失败重试
                .followRedirects(true) // 允许重定向
                .followSslRedirects(true) // 允许SSL重定向
                .build();
    }
    
    /**
     * HTTP响应结果封装类
     */

    
    /**
     * 流式响应回调接口
     */
    public interface StreamCallback {
        /**
         * 接收到数据块
         * @param chunk 数据块
         */
        void onChunk(String chunk);
        
        /**
         * 流式响应完成
         */
        void onComplete();
        
        /**
         * 发生错误
         * @param error 错误信息
         */
        void onError(String error);
    }
    
    /**
     * GET请求
     * @param url 请求URL
     * @param headers 请求头（可为null）
     * @return HttpResult<Object> 返回原始字符串数据
     */
    public static HttpResult get(String url, Map<String, String> headers) {
        Request.Builder builder = new Request.Builder().url(url);
        
        // 添加请求头
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                builder.addHeader(entry.getKey(), entry.getValue());
            }
        }
        
        Request request = builder.build();
        return executeRequest(normalClient, request);
    }



    /**
     * POST请求（JSON格式）
     * @param url 请求URL
     * @param jsonBody JSON请求体
     * @param headers 请求头（可为null）
     * @return HttpResult<Object> 返回原始字符串数据
     */
    public static HttpResult postJson(String url, String jsonBody, Map<String, String> headers) {
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(jsonBody, JSON);

        Request.Builder builder = new Request.Builder()
                .url(url)
                .post(body);

        // 添加请求头
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                builder.addHeader(entry.getKey(), entry.getValue());
            }
        }

        Request request = builder.build();
        return executeRequest(normalClient, request);
    }

    /**
     * POST请求（JSON格式，泛型版本）
     * @param url 请求URL
     * @param jsonBody JSON请求体
     * @param headers 请求头（可为null）
     * @param clazz 目标数据类型
     * @return HttpResult<T> 返回指定类型的数据
     */


    /**
     * POST请求（对象自动转JSON）
     * @param url 请求URL
     * @param object 请求对象
     * @param headers 请求头（可为null）
     * @return HttpResult<String> 返回原始字符串数据
     */
    public static HttpResult postObject(String url, Object object, Map<String, String> headers) {
        String jsonBody = gson.toJson(object);
        return postJson(url, jsonBody, headers);
    }


    /**
     * PUT请求（JSON格式）
     * @param url 请求URL
     * @param jsonBody JSON请求体
     * @param headers 请求头（可为null）
     * @return HttpResult<String> 返回原始字符串数据
     */
    public static HttpResult putJson(String url, String jsonBody, Map<String, String> headers) {
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(jsonBody, JSON);

        Request.Builder builder = new Request.Builder()
                .url(url)
                .put(body);

        // 添加请求头
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                builder.addHeader(entry.getKey(), entry.getValue());
            }
        }

        Request request = builder.build();
        return executeRequest(normalClient, request);
    }


    /**
     * DELETE请求
     * @param url 请求URL
     * @param headers 请求头（可为null）
     * @return HttpResult<String> 返回原始字符串数据
     */
    public static HttpResult delete(String url, Map<String, String> headers) {
        Request.Builder builder = new Request.Builder()
                .url(url)
                .delete();

        // 添加请求头
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                builder.addHeader(entry.getKey(), entry.getValue());
            }
        }

        Request request = builder.build();
        return executeRequest(normalClient, request);
    }

    /**
     * 流式GET请求
     * @param url 请求URL
     * @param headers 请求头（可为null）
     * @param callback 流式回调
     */
    public static void getStream(String url, Map<String, String> headers, StreamCallback callback) {
        Request.Builder builder = new Request.Builder().url(url);
        
        // 添加请求头
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                builder.addHeader(entry.getKey(), entry.getValue());
            }
        }
        
        Request request = builder.build();
        executeStreamRequest(request, callback);
    }
    
    /**
     * 流式POST请求
     * @param url 请求URL
     * @param jsonBody JSON请求体
     * @param headers 请求头（可为null）
     * @param callback 流式回调
     */
    public static void postJsonStream(String url, String jsonBody, Map<String, String> headers, StreamCallback callback) {
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(jsonBody, JSON);
        
        Request.Builder builder = new Request.Builder()
                .url(url)
                .post(body);
        
        // 添加请求头
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                builder.addHeader(entry.getKey(), entry.getValue());
            }
        }
        
        Request request = builder.build();
        executeStreamRequest(request, callback);
    }
    
    /**
     * 流式POST请求（对象自动转JSON）
     * @param url 请求URL
     * @param object 请求对象
     * @param headers 请求头（可为null）
     * @param callback 流式回调
     */
    public static void postObjectStream(String url, Object object, Map<String, String> headers, StreamCallback callback) {
        String jsonBody = gson.toJson(object);
        postJsonStream(url, jsonBody, headers, callback);
    }
    
    /**
     * 执行普通HTTP请求（带重试机制）
     */
    private static HttpResult executeRequest(OkHttpClient client, Request request) {
        IOException lastException = null;
        int maxAttempts = Math.max(1, MAX_RETRY_COUNT); // 确保至少尝试一次
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try (Response response = client.newCall(request).execute()) {
                int code = response.code();
                String message = response.message();
                String data = null;
                
                if (response.body() != null) {
                    data = getResponseBodyString(response);
                }
                
                boolean success = response.isSuccessful();
                return new HttpResult(success, code, message, data);
                
            } catch (IOException e) {
                lastException = e;
                String errorType = getErrorType(e);
                Log.e("BFHttpUtils", "请求失败 [" + errorType + "]，第 " + attempt + "/" + maxAttempts + " 次尝试: " + e.getMessage());
                
                // 如果不是最后一次尝试，等待一段时间再重试
                if (attempt < maxAttempts) {
                    try {
                        Thread.sleep(1000 * attempt); // 递增等待时间
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        
        // 所有重试都失败了
        if (lastException != null) {
            lastException.printStackTrace();
        }
        return new HttpResult(false, -1, "Network error after " + maxAttempts + " attempts: " +
                (lastException != null ? lastException.getMessage() : "Unknown error"), null);
    }
    
    /**
     * 执行流式HTTP请求（带重试机制）
     */
    private static void executeStreamRequest(Request request, StreamCallback callback) {
        executeStreamRequestWithRetry(request, callback, 1);
    }
    
    /**
     * 执行流式HTTP请求的重试逻辑
     */
    private static void executeStreamRequestWithRetry(Request request, StreamCallback callback, int attempt) {
        streamClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                String errorType = getErrorType(e);
                Log.e("BFHttpUtils", "流式请求失败 [" + errorType + "]，第 " + attempt + "/" + MAX_RETRY_COUNT + " 次尝试: " + e.getMessage());
                
                if (attempt < MAX_RETRY_COUNT) {
                    // 延迟后重试
                    new Thread(() -> {
                        try {
                            // 对于EOFException等连接问题，使用更长的等待时间
                            int waitTime = isConnectionError(e) ? 2000 * attempt : 1000 * attempt;
                            Thread.sleep(waitTime);
                            executeStreamRequestWithRetry(request, callback, attempt + 1);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            callback.onError("Request interrupted after " + attempt + " attempts");
                        }
                    }).start();
                } else {
                    callback.onError("Network error [" + errorType + "] after " + MAX_RETRY_COUNT + " attempts: " + e.getMessage());
                }
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e("BFHttpUtils", "流式请求HTTP错误，第 " + attempt + "/" + MAX_RETRY_COUNT + " 次尝试: " + response.code() + " " + response.message());
                    
                    if (attempt < MAX_RETRY_COUNT && (response.code() >= 500 || response.code() == 408)) {
                        // 对于服务器错误或超时，进行重试
                        new Thread(() -> {
                            try {
                                Thread.sleep(1000 * attempt);
                                executeStreamRequestWithRetry(request, callback, attempt + 1);
                            } catch (InterruptedException ie) {
                                Thread.currentThread().interrupt();
                                callback.onError("Request interrupted after " + attempt + " attempts");
                            }
                        }).start();
                    } else {
                        callback.onError("HTTP error: " + response.code() + " " + response.message());
                    }
                    return;
                }
                
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(response.body().byteStream(), StandardCharsets.UTF_8))) {
                    
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (!line.trim().isEmpty()) {
                            callback.onChunk(line);
                        }
                    }
                    
                    callback.onComplete();
                    
                } catch (IOException e) {
                    String errorType = getErrorType(e);
                    Log.e("BFHttpUtils", "流式读取错误 [" + errorType + "]，第 " + attempt + "/" + MAX_RETRY_COUNT + " 次尝试: " + e.getMessage());
                    
                    if (attempt < MAX_RETRY_COUNT) {
                        // 对于读取错误，也进行重试
                        new Thread(() -> {
                            try {
                                // 对于EOFException等连接问题，使用更长的等待时间
                                int waitTime = isConnectionError(e) ? 2000 * attempt : 1000 * attempt;
                                Thread.sleep(waitTime);
                                executeStreamRequestWithRetry(request, callback, attempt + 1);
                            } catch (InterruptedException ie) {
                                Thread.currentThread().interrupt();
                                callback.onError("Request interrupted after " + attempt + " attempts");
                            }
                        }).start();
                    } else {
                        callback.onError("Stream reading error [" + errorType + "] after " + MAX_RETRY_COUNT + " attempts: " + e.getMessage());
                    }
                }
            }
        });
    }
    
    /**
     * 设置全局请求超时时间
     * @param timeoutSeconds 超时时间（秒）
     */
    public static void setGlobalTimeout(int timeoutSeconds) {
        normalClient = normalClient.newBuilder()
                .connectTimeout(timeoutSeconds, TimeUnit.SECONDS)
                .readTimeout(timeoutSeconds, TimeUnit.SECONDS)
                .writeTimeout(timeoutSeconds, TimeUnit.SECONDS)
                .build();
    }
    
    /**
     * 设置流式请求超时时间
     * @param timeoutSeconds 超时时间（秒）
     */
    public static void setStreamTimeout(int timeoutSeconds) {
        // 重新创建streamClient
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        
        ConnectionPool connectionPool = new ConnectionPool(
                CONNECTION_POOL_MAX_IDLE, 
                CONNECTION_POOL_KEEP_ALIVE, 
                TimeUnit.MINUTES
        );
        
        streamClient = new OkHttpClient.Builder()
                .connectTimeout(timeoutSeconds, TimeUnit.SECONDS)
                .readTimeout(timeoutSeconds, TimeUnit.SECONDS)
                .writeTimeout(timeoutSeconds, TimeUnit.SECONDS)
                .connectionPool(connectionPool)
                .addInterceptor(loggingInterceptor)
                .retryOnConnectionFailure(true)
                .build();
    }
    
    /**
     * 判断是否为连接相关错误
     */
    private static boolean isConnectionError(IOException e) {
        String message = e.getMessage();
        String className = e.getClass().getSimpleName();
        
        return className.equals("EOFException") ||
               className.equals("SocketTimeoutException") ||
               className.equals("ConnectException") ||
               className.equals("UnknownHostException") ||
               (message != null && (
                   message.contains("unexpected end of stream") ||
                   message.contains("Connection reset") ||
                   message.contains("Software caused connection abort") ||
                   message.contains("\\n not found")
               ));
    }
    
    /**
     * 获取响应体字符串，自动处理压缩格式
     */
    private static String getResponseBodyString(Response response) throws IOException {
        if (response.body() == null) {
            return null;
        }
        
        String contentEncoding = response.header("Content-Encoding");
        InputStream inputStream = response.body().byteStream();
        
        // 根据Content-Encoding头处理压缩格式
        if ("gzip".equalsIgnoreCase(contentEncoding)) {
            inputStream = new GZIPInputStream(inputStream);
        } else if ("deflate".equalsIgnoreCase(contentEncoding)) {
            inputStream = new InflaterInputStream(inputStream);
        }
        
        // 读取解压后的数据
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }
            // 移除最后一个换行符
            if (result.length() > 0) {
                result.setLength(result.length() - 1);
            }
            return result.toString();
        }
    }
    
    /**
     * 获取错误类型描述
     */
    private static String getErrorType(IOException e) {
        String className = e.getClass().getSimpleName();
        String message = e.getMessage();
        
        switch (className) {
            case "EOFException":
                return "连接意外中断";
            case "SocketTimeoutException":
                return "连接超时";
            case "ConnectException":
                return "连接失败";
            case "UnknownHostException":
                return "域名解析失败";
            case "SSLException":
                return "SSL证书错误";
            default:
                if (message != null) {
                    if (message.contains("unexpected end of stream")) {
                        return "服务器提前关闭连接";
                    } else if (message.contains("Connection reset")) {
                        return "连接被重置";
                    } else if (message.contains("\\n not found")) {
                        return "响应格式错误";
                    }
                }
                return "网络异常";
         }
     }
     
     /**
      * 文件上传方法（multipart/form-data格式）
      * @param url 请求URL
      * @param file 要上传的文件
      * @param headers 请求头（可为null）
      * @return HttpResult<String> 返回原始字符串数据
      */
     public static HttpResult postFile(String url, File file, Map<String, String> headers) {
         if (file == null || !file.exists()) {
             Log.e("BFHttpUtils", "文件不存在或为空");
             return  new HttpResult(false, 400, "文件不存在或为空", null);
         }

         try {
             // 创建multipart请求体
             RequestBody fileBody = RequestBody.create(file, MediaType.parse("application/octet-stream"));
             MultipartBody.Builder multipartBuilder = new MultipartBody.Builder()
                     .setType(MultipartBody.FORM)
                     .addFormDataPart("file", file.getName(), fileBody);

             RequestBody requestBody = multipartBuilder.build();

             Request.Builder builder = new Request.Builder()
                     .url(url)
                     .post(requestBody);

             // 添加请求头
             if (headers != null) {
                 for (Map.Entry<String, String> entry : headers.entrySet()) {
                     builder.addHeader(entry.getKey(), entry.getValue());
                 }
             }

             Request request = builder.build();
             return executeRequest(normalClient, request);
         } catch (Exception e) {
             Log.e("BFHttpUtils", "文件上传失败", e);
             return new HttpResult(false, 500, "文件上传失败: " + e.getMessage(), null);
         }
     }
     



     /**
      * 用户代理拦截器，为Android设备添加合适的User-Agent
      */
     private static class UserAgentInterceptor implements Interceptor {
         @Override
         public Response intercept(Chain chain) throws IOException {
             Request originalRequest = chain.request();
             Request requestWithUserAgent = originalRequest.newBuilder()
                     .header("User-Agent", "BarrierFreeQunar/1.0 (Android; Mobile)")
                     .header("Accept", "application/json, text/plain, */*")
                     .header("Accept-Encoding", "gzip, deflate")
                     .header("Connection", "keep-alive")
                     .build();
             return chain.proceed(requestWithUserAgent);
         }
     }
}