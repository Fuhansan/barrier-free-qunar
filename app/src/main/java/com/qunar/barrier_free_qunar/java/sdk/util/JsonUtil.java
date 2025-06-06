package com.qunar.barrier_free_qunar.java.sdk.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * JSON 序列化和反序列化工具类
 * 基于 Gson 实现，提供高性能的 JSON 转换功能
 */
public class JsonUtil {
    
    /**
     * 默认的 Gson 实例（线程安全）
     */
    private static final Gson DEFAULT_GSON = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create();
    
    /**
     * 美化输出的 Gson 实例
     */
    private static final Gson PRETTY_GSON = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .setPrettyPrinting()
            .create();
    
    /**
     * 私有构造函数，防止实例化
     */
    private JsonUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    /**
     * 将对象转换为 JSON 字符串
     * 
     * @param object 要转换的对象
     * @return JSON 字符串，如果对象为 null 则返回 "null"
     */
    public static String toJson(Object object) {
        if (object == null) {
            return "null";
        }
        try {
            return DEFAULT_GSON.toJson(object);
        } catch (Exception e) {
            throw new JsonConversionException("Failed to convert object to JSON", e);
        }
    }
    
    /**
     * 将对象转换为格式化的 JSON 字符串（美化输出）
     * 
     * @param object 要转换的对象
     * @return 格式化的 JSON 字符串
     */
    public static String toPrettyJson(Object object) {
        if (object == null) {
            return "null";
        }
        try {
            return PRETTY_GSON.toJson(object);
        } catch (Exception e) {
            throw new JsonConversionException("Failed to convert object to pretty JSON", e);
        }
    }
    
    /**
     * 将 JSON 字符串转换为指定类型的对象
     * 
     * @param json JSON 字符串
     * @param clazz 目标类型
     * @param <T> 泛型类型
     * @return 转换后的对象
     * @throws JsonConversionException 转换失败时抛出
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return DEFAULT_GSON.fromJson(json, clazz);
        } catch (JsonSyntaxException e) {
            throw new JsonConversionException("Invalid JSON syntax: " + json, e);
        } catch (Exception e) {
            throw new JsonConversionException("Failed to convert JSON to object", e);
        }
    }
    
    /**
     * 将 JSON 字符串转换为指定类型的对象（支持泛型）
     * 
     * @param json JSON 字符串
     * @param type 目标类型（支持泛型）
     * @param <T> 泛型类型
     * @return 转换后的对象
     * @throws JsonConversionException 转换失败时抛出
     */
    public static <T> T fromJson(String json, Type type) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return DEFAULT_GSON.fromJson(json, type);
        } catch (JsonSyntaxException e) {
            throw new JsonConversionException("Invalid JSON syntax: " + json, e);
        } catch (Exception e) {
            throw new JsonConversionException("Failed to convert JSON to object", e);
        }
    }
    
    /**
     * 将 JSON 字符串转换为 List
     * 
     * @param json JSON 字符串
     * @param elementClass List 元素的类型
     * @param <T> 元素类型
     * @return List 对象
     */
    public static <T> List<T> fromJsonToList(String json, Class<T> elementClass) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            Type listType = TypeToken.getParameterized(List.class, elementClass).getType();
            return DEFAULT_GSON.fromJson(json, listType);
        } catch (JsonSyntaxException e) {
            throw new JsonConversionException("Invalid JSON syntax: " + json, e);
        } catch (Exception e) {
            throw new JsonConversionException("Failed to convert JSON to List", e);
        }
    }
    
    /**
     * 将 JSON 字符串转换为 Map
     * 
     * @param json JSON 字符串
     * @return Map<String, Object> 对象
     */
    public static Map<String, Object> fromJsonToMap(String json) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            Type mapType = new TypeToken<Map<String, Object>>(){}.getType();
            return DEFAULT_GSON.fromJson(json, mapType);
        } catch (JsonSyntaxException e) {
            throw new JsonConversionException("Invalid JSON syntax: " + json, e);
        } catch (Exception e) {
            throw new JsonConversionException("Failed to convert JSON to Map", e);
        }
    }
    
    /**
     * 将 JSON 字符串转换为指定类型的 Map
     * 
     * @param json JSON 字符串
     * @param valueClass Map 值的类型
     * @param <T> 值类型
     * @return Map<String, T> 对象
     */
    public static <T> Map<String, T> fromJsonToMap(String json, Class<T> valueClass) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            Type mapType = TypeToken.getParameterized(Map.class, String.class, valueClass).getType();
            return DEFAULT_GSON.fromJson(json, mapType);
        } catch (JsonSyntaxException e) {
            throw new JsonConversionException("Invalid JSON syntax: " + json, e);
        } catch (Exception e) {
            throw new JsonConversionException("Failed to convert JSON to Map", e);
        }
    }
    
    /**
     * 检查字符串是否为有效的 JSON 格式
     * 
     * @param json 要检查的字符串
     * @return true 如果是有效的 JSON，否则返回 false
     */
    public static boolean isValidJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return false;
        }
        try {
            DEFAULT_GSON.fromJson(json, Object.class);
            return true;
        } catch (JsonSyntaxException e) {
            return false;
        }
    }
    
    /**
     * 深度克隆对象（通过 JSON 序列化和反序列化）
     * 注意：这种方式性能较低，仅适用于简单对象的克隆
     * 
     * @param object 要克隆的对象
     * @param clazz 对象类型
     * @param <T> 泛型类型
     * @return 克隆后的对象
     */
    public static <T> T deepClone(T object, Class<T> clazz) {
        if (object == null) {
            return null;
        }
        try {
            String json = DEFAULT_GSON.toJson(object);
            return DEFAULT_GSON.fromJson(json, clazz);
        } catch (Exception e) {
            throw new JsonConversionException("Failed to deep clone object", e);
        }
    }
    
    /**
     * 获取默认的 Gson 实例
     * 如果需要自定义配置，可以使用此方法获取实例
     * 
     * @return Gson 实例
     */
    public static Gson getGson() {
        return DEFAULT_GSON;
    }
    
    /**
     * JSON 转换异常类
     */
    public static class JsonConversionException extends RuntimeException {
        
        public JsonConversionException(String message) {
            super(message);
        }
        
        public JsonConversionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}