# 动作执行框架使用说明

## 概述

动作执行框架是一个基于策略模式和命令模式设计的可扩展架构，用于解析和执行文本中的动作指令。框架支持多种动作类型，包括点击、滑动、返回、Home键、截屏等操作。

## 架构设计

### 核心组件

1. **ActionParser** - 动作解析器接口
   - 负责从文本内容中解析出动作指令
   - 默认实现：`DefaultActionParser`

2. **ActionExecutor** - 动作执行器接口
   - 负责执行具体的动作指令
   - 默认实现：`GestureActionExecutor`

3. **ActionManager** - 动作管理器
   - 统一管理动作的解析和执行
   - 支持多个执行器的注册和管理

4. **ActionCommand** - 动作指令模型
   - 封装解析出的动作指令信息

5. **ActionResult** - 动作执行结果模型
   - 封装动作执行的结果信息

### 设计模式

- **策略模式**：不同的执行器实现不同的执行策略
- **命令模式**：将动作请求封装为对象
- **工厂模式**：ActionType枚举提供类型创建

## 支持的动作类型

| 动作类型 | 指令格式 | 参数说明 |
|---------|---------|----------|
| 点击 | `click(start_box='[x1,y1,x2,y2]')` | start_box: 点击区域坐标 |
| 滑动 | `performSwipeGesture(from='[x1,y1]', to='[x2,y2]')` | from: 起始坐标, to: 结束坐标 |
| 返回 | `back()` | 无参数 |
| Home键 | `home()` | 无参数 |
| 截屏 | `screenShot()` | 无参数 |
| 打开应用 | `openApp(packageName='com.example.app')` | packageName: 应用包名 |
| 完成 | `complete` | 无参数 |

## 使用示例

### 基本使用

```java
// 创建动作管理器
GestureApi gestureApi = new GestureApi(accessibilityService);
ActionManager actionManager = new ActionManager(gestureApi);

// 检查内容是否包含动作指令
String content = "请点击登录按钮 click(start_box='[700,2300,886,2450]')";
if (actionManager.containsActions(content)) {
    // 解析并执行动作指令
    List<ActionResult> results = actionManager.parseAndExecuteActions(content);
    
    // 处理执行结果
    for (ActionResult result : results) {
        if (result.isSuccess()) {
            Log.i(TAG, "动作执行成功: " + result.getMessage());
        } else {
            Log.e(TAG, "动作执行失败: " + result.getMessage());
        }
    }
}
```

### 自定义执行器

```java
public class CustomActionExecutor implements ActionExecutor {
    @Override
    public ActionResult execute(ActionCommand command) {
        // 实现自定义动作执行逻辑
        return ActionResult.success("自定义动作执行成功");
    }
    
    @Override
    public boolean canExecute(ActionCommand command) {
        // 判断是否支持执行该动作
        return command.getActionType() == ActionType.CUSTOM;
    }
}

// 注册自定义执行器
actionManager.addExecutor(new CustomActionExecutor());
```

## 扩展指南

### 添加新的动作类型

1. 在 `ActionType` 枚举中添加新的动作类型
2. 在解析器中添加对应的解析逻辑
3. 创建或扩展执行器以支持新动作
4. 在 `ActionManager` 中注册新的执行器

### 自定义解析器

```java
public class CustomActionParser implements ActionParser {
    @Override
    public List<ActionCommand> parseActions(String content) {
        // 实现自定义解析逻辑
        return new ArrayList<>();
    }
    
    @Override
    public boolean containsActions(String content) {
        // 实现自定义检查逻辑
        return false;
    }
}
```

## 注意事项

1. **线程安全**：当前实现不是线程安全的，如需在多线程环境使用，请添加同步机制
2. **错误处理**：执行器应该妥善处理异常，避免影响后续动作的执行
3. **性能考虑**：对于大量动作指令，建议分批执行或异步处理
4. **权限要求**：某些动作（如截屏）需要特定的系统权限

## 日志标签

- `DefaultActionParser` - 动作解析相关日志
- `GestureActionExecutor` - 手势执行相关日志
- `ActionManager` - 动作管理相关日志
- `【ActionProcessor】` - UserTaskService中的动作处理日志