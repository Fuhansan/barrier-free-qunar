package com.qunar.barrier_free_qunar.java.sdk.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JsonUtil 使用示例
 * 展示各种 JSON 转换场景的用法
 */
public class JsonUtilExample {
    
    /**
     * 示例用户类
     */
    public static class User {
        private String name;
        private int age;
        private String email;
        private List<String> hobbies;
        
        public User() {}
        
        public User(String name, int age, String email) {
            this.name = name;
            this.age = age;
            this.email = email;
            this.hobbies = new ArrayList<>();
        }
        
        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public List<String> getHobbies() { return hobbies; }
        public void setHobbies(List<String> hobbies) { this.hobbies = hobbies; }
        
        @Override
        public String toString() {
            return "User{name='" + name + "', age=" + age + ", email='" + email + "', hobbies=" + hobbies + "}";
        }
    }
    
    /**
     * 演示基本的对象转 JSON 和 JSON 转对象
     */
    public static void basicExample() {
        System.out.println("=== 基本转换示例 ===");
        
        // 创建用户对象
        User user = new User("张三", 25, "zhangsan@example.com");
        user.getHobbies().add("阅读");
        user.getHobbies().add("游泳");
        
        // 对象转 JSON
        String json = JsonUtil.toJson(user);
        System.out.println("对象转JSON: " + json);
        
        // 美化输出
        String prettyJson = JsonUtil.toPrettyJson(user);
        System.out.println("美化JSON:\n" + prettyJson);
        
        // JSON 转对象
        User parsedUser = JsonUtil.fromJson(json, User.class);
        System.out.println("JSON转对象: " + parsedUser);
        
        System.out.println();
    }
    
    /**
     * 演示 List 和 Map 的转换
     */
    public static void collectionExample() {
        System.out.println("=== 集合转换示例 ===");
        
        // List 转换
        List<User> userList = new ArrayList<>();
        userList.add(new User("李四", 30, "lisi@example.com"));
        userList.add(new User("王五", 28, "wangwu@example.com"));
        
        String listJson = JsonUtil.toJson(userList);
        System.out.println("List转JSON: " + listJson);
        
        List<User> parsedList = JsonUtil.fromJsonToList(listJson, User.class);
        System.out.println("JSON转List: " + parsedList);
        
        // Map 转换
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("status", "success");
        dataMap.put("code", 200);
        dataMap.put("message", "操作成功");
        dataMap.put("data", userList);
        
        String mapJson = JsonUtil.toJson(dataMap);
        System.out.println("Map转JSON: " + mapJson);
        
        Map<String, Object> parsedMap = JsonUtil.fromJsonToMap(mapJson);
        System.out.println("JSON转Map: " + parsedMap);
        
        System.out.println();
    }
    
    /**
     * 演示错误处理和验证
     */
    public static void errorHandlingExample() {
        System.out.println("=== 错误处理示例 ===");
        
        // 测试无效 JSON
        String invalidJson = "{name: 'test', age: }";
        System.out.println("无效JSON检查: " + JsonUtil.isValidJson(invalidJson));
        
        String validJson = "{\"name\": \"test\", \"age\": 25}";
        System.out.println("有效JSON检查: " + JsonUtil.isValidJson(validJson));
        
        // 测试 null 和空字符串处理
        System.out.println("null转JSON: " + JsonUtil.toJson(null));
        System.out.println("空字符串转对象: " + JsonUtil.fromJson("", User.class));
        System.out.println("null字符串转对象: " + JsonUtil.fromJson(null, User.class));
        
        // 测试异常处理
        try {
            JsonUtil.fromJson(invalidJson, User.class);
        } catch (JsonUtil.JsonConversionException e) {
            System.out.println("捕获到转换异常: " + e.getMessage());
        }
        
        System.out.println();
    }
    
    /**
     * 演示深度克隆
     */
    public static void deepCloneExample() {
        System.out.println("=== 深度克隆示例 ===");
        
        User original = new User("原始用户", 25, "original@example.com");
        original.getHobbies().add("编程");
        original.getHobbies().add("音乐");
        
        User cloned = JsonUtil.deepClone(original, User.class);
        
        System.out.println("原始对象: " + original);
        System.out.println("克隆对象: " + cloned);
        System.out.println("是否为同一对象: " + (original == cloned));
        System.out.println("内容是否相等: " + original.toString().equals(cloned.toString()));
        
        // 修改克隆对象，验证深度克隆
        cloned.setName("克隆用户");
        cloned.getHobbies().add("绘画");
        
        System.out.println("修改后原始对象: " + original);
        System.out.println("修改后克隆对象: " + cloned);
        
        System.out.println();
    }
    
    /**
     * 演示复杂嵌套对象的转换
     */
    public static void complexObjectExample() {
        System.out.println("=== 复杂对象转换示例 ===");
        
        // 创建复杂的嵌套结构
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", System.currentTimeMillis());
        response.put("success", true);
        
        Map<String, Object> result = new HashMap<>();
        result.put("total", 2);
        result.put("page", 1);
        result.put("size", 10);
        
        List<User> users = new ArrayList<>();
        User user1 = new User("管理员", 35, "admin@example.com");
        user1.getHobbies().add("管理");
        User user2 = new User("普通用户", 22, "user@example.com");
        user2.getHobbies().add("浏览");
        users.add(user1);
        users.add(user2);
        
        result.put("users", users);
        response.put("result", result);
        
        // 转换为 JSON
        String complexJson = JsonUtil.toPrettyJson(response);
        System.out.println("复杂对象JSON:\n" + complexJson);
        
        // 转换回对象
        Map<String, Object> parsedResponse = JsonUtil.fromJsonToMap(complexJson);
        System.out.println("解析后的对象: " + parsedResponse);
        
        System.out.println();
    }
    
    /**
     * 主方法，运行所有示例
     */
    public static void main(String[] args) {
        System.out.println("JsonUtil 使用示例\n");
        
        basicExample();
        collectionExample();
        errorHandlingExample();
        deepCloneExample();
        complexObjectExample();
        
        System.out.println("所有示例运行完成！");
    }
}