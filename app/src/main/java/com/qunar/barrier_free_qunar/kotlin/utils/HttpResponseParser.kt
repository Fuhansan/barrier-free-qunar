package com.qunar.barrier_free_qunar.kotlin.utils

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject

/**
 * HTTP响应数据解析工具类
 * 提供通用的JSON数据解析方法
 */
object HttpResponseParser {
    
    private const val TAG = "HttpResponseParser"
    
    /**
     * 通用的HTTP接口返回数据解析方法
     * @param responseData 接口返回的原始数据
     * @param fieldMappings 字段映射关系，key为目标字段名，value为JSON中的字段路径
     * @param defaultValues 默认值映射，当解析失败时使用
     * @return 解析后的数据Map
     */
    fun parseResponseData(
        responseData: Any?,
        fieldMappings: Map<String, String>,
        defaultValues: Map<String, Any> = emptyMap()
    ): Map<String, Any> {
        return try {
            val gson = Gson()
            val jsonResponse = gson.fromJson(responseData.toString(), JsonObject::class.java)
            val dataObject = jsonResponse.getAsJsonObject("data")
            
            val result = mutableMapOf<String, Any>()
            
            // 根据字段映射解析数据
            fieldMappings.forEach { (targetField, jsonPath) ->
                val value = when {
                    jsonPath.startsWith("data.") -> {
                        val fieldName = jsonPath.removePrefix("data.")
                        dataObject.get(fieldName)?.asString ?: ""
                    }
                    jsonPath == "current_time" -> System.currentTimeMillis()
                    jsonPath.startsWith("root.") -> {
                        val fieldName = jsonPath.removePrefix("root.")
                        jsonResponse.get(fieldName)?.asString ?: ""
                    }
                    else -> defaultValues[targetField] ?: ""
                }
                result[targetField] = value
            }
            
            // 添加默认值中未在映射中的字段
            defaultValues.forEach { (key, value) ->
                if (!result.containsKey(key)) {
                    result[key] = value
                }
            }
            
            Log.d(TAG, "解析HTTP响应数据成功: $result")
            result
        } catch (e: Exception) {
            Log.e(TAG, "解析HTTP响应数据失败: ${e.message}")
            // 返回默认值
            defaultValues.toMutableMap()
        }
    }
    
    /**
     * 解析用户信息的便捷方法
     * @param responseData 接口返回的原始数据
     * @param phone 用户手机号（用于补充信息）
     * @return 解析后的用户信息Map
     */
    fun parseUserInfo(responseData: Any?, phone: String): Map<String, Any> {
        val fieldMappings = mapOf(
            "userId" to "data.userId",
            "userName" to "data.userName",
            "userCode" to "data.userCode",
            "idCard" to "data.idCard",
            "regPhone" to "data.regPhone",
            "email" to "data.email",
            "loginTime" to "current_time"
        )
        
        val defaultValues = mapOf(
            "userId" to "",
            "userName" to "",
            "userCode" to "",
            "idCard" to "",
            "regPhone" to "",
            "email" to "",
            "phone" to phone,
            "loginTime" to System.currentTimeMillis()
        )
        
        return parseResponseData(responseData, fieldMappings, defaultValues)
    }
    
    /**
     * 解析注册信息的便捷方法
     * @param responseData 接口返回的原始数据
     * @param phone 用户手机号
     * @return 解析后的注册信息Map
     */
    fun parseRegisterInfo(responseData: Any?, phone: String): Map<String, Any> {
        val fieldMappings = mapOf(
            "userId" to "data.userId",
            "userName" to "data.userName",
            "userCode" to "data.userCode",
            "message" to "root.msg",
            "registerTime" to "current_time"
        )
        
        val defaultValues = mapOf(
            "userId" to "",
            "userName" to "",
            "userCode" to "",
            "phone" to phone,
            "message" to "注册成功",
            "registerTime" to System.currentTimeMillis()
        )
        
        return parseResponseData(responseData, fieldMappings, defaultValues)
    }
}