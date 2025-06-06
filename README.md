# Barrier Free Qunar

## 环境配置说明

本项目使用.env文件进行配置管理，可以方便地修改API密钥、模型名称等配置项，而无需修改代码。

### .env文件

项目根目录下的.env文件包含以下配置项：

```
# API密钥配置
API_KEY=your_api_key_here

# 模型配置
BASIC_MODEL=gpt-3.5-turbo
VL_MODEL=gpt-4-vision
REASONING_MODEL=gpt-4-turbo

# 其他配置
API_BASE_URL=https://api.example.com/v1
TIMEOUT_SECONDS=30
MAX_TOKENS=2048
```

### 配置项说明

- `API_KEY`: API密钥，用于访问模型服务
- `BASIC_MODEL`: 基础模型名称
- `VL_MODEL`: 视觉模型名称
- `REASONING_MODEL`: 推理模型名称
- `API_BASE_URL`: API基础URL
- `TIMEOUT_SECONDS`: 请求超时时间（秒）
- `MAX_TOKENS`: 最大令牌数

## 使用方法

### 初始化配置

在应用启动时，需要初始化环境配置：

```kotlin
// 在MainActivity或Application类中初始化
EnvConfig.init(applicationContext)
```

### 获取配置项

使用`EnvConfig`类获取配置项：

```kotlin
// 获取API密钥
val apiKey = EnvConfig.getApiKey()

// 获取基础模型名称
val basicModel = EnvConfig.getBasicModel()

// 获取视觉模型名称
val vlModel = EnvConfig.getVlModel()

// 获取推理模型名称
val reasoningModel = EnvConfig.getReasoningModel()

// 获取API基础URL
val apiBaseUrl = EnvConfig.getApiBaseUrl()

// 获取超时时间（秒）
val timeoutSeconds = EnvConfig.getTimeoutSeconds()

// 获取最大令牌数
val maxTokens = EnvConfig.getMaxTokens()
```

### 在Java代码中使用

```java
// 获取API密钥
String apiKey = EnvConfig.getApiKey();

// 获取基础模型名称
String basicModel = EnvConfig.getBasicModel();

// 获取视觉模型名称
String vlModel = EnvConfig.getVlModel();

// 获取推理模型名称
String reasoningModel = EnvConfig.getReasoningModel();

// 获取API基础URL
String apiBaseUrl = EnvConfig.getApiBaseUrl();

// 获取超时时间（秒）
int timeoutSeconds = EnvConfig.getTimeoutSeconds();

// 获取最大令牌数
int maxTokens = EnvConfig.getMaxTokens();
```

## 注意事项

1. 在使用`EnvConfig`类之前，必须先调用`EnvConfig.init(context)`方法进行初始化。
2. .env文件应该放在项目根目录下，并且在构建时复制到assets目录中。
3. 在实际部署时，应该将.env文件添加到.gitignore中，避免将敏感信息提交到代码仓库。