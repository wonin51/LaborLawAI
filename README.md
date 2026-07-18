# 劳动合同法律助手

本项目是一个基于大模型与 RAG 的劳动合同法律信息检索与咨询辅助系统。系统定位为学生实训项目和法律咨询原型，不替代律师、仲裁机构或法院意见。

当前已完成后端基础工程骨架和前端咨询工作台原型，后续可继续补充知识库入库、检索增强问答、依据引用、问答历史和管理后台真实接口。

## 项目结构

```text
proj1/
├─ backend/    # Java 17 + Spring Boot 后端
├─ frontend/   # Vue 3 + Vite + Element Plus 前端
├─ docs/       # 项目计划、UI 提示词、项目记忆和简历素材
└─ README.md
```

## 后端技术栈

- Java 17 目标版本
- Spring Boot 3.5.3
- Spring Web
- Validation
- MyBatis-Plus
- MySQL Driver
- Spring AI OpenAI Starter
- Elasticsearch Java Client
- Lombok

## 前端技术栈

- Vue 3
- Vite
- Element Plus
- Axios
- `@element-plus/icons-vue`

## 后端启动

```powershell
cd backend
mvn spring-boot:run
```

也可以先打包再运行：

```powershell
cd backend
mvn package -DskipTests
java -jar target/legal-contract-assistant-0.0.1-SNAPSHOT.jar
```

## 前端启动

```powershell
cd frontend
npm.cmd install
npm.cmd run dev
```

默认访问地址：

```text
http://localhost:5173
```

生产构建：

```powershell
cd frontend
npm.cmd run build
```

## 健康检查

后端接口：

```text
GET http://localhost:8080/api/health
```

预期返回：

```json
{"status":"ok"}
```

前端开发环境通过 Vite 代理访问：

```text
GET http://localhost:5173/api/health
```

## 前后端跨域处理

本项目同时做了两层处理，降低老师或队友本地运行时遇到 CORS 问题的概率。

### 1. 前端开发代理

`frontend/vite.config.js` 中配置：

```js
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true
    }
  }
}
```

前端代码统一请求 `/api/health`。开发环境下浏览器访问的是 `localhost:5173`，Vite 会把 `/api` 请求转发给 `localhost:8080`，从而避开浏览器跨域限制。

### 2. 后端 CORS

`backend/src/main/java/com/teddy/legal/config/WebMvcConfig.java` 中允许本地前端端口访问 `/api/**`：

```java
registry.addMapping("/api/**")
        .allowedOrigins(
                "http://localhost:5173",
                "http://127.0.0.1:5173",
                "http://localhost:4173",
                "http://127.0.0.1:4173"
        )
        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
        .allowedHeaders("*");
```

如果以后前端端口变化，例如 Vite 自动切到 `5174`，需要把对应 origin 加到后端 CORS 白名单，或固定前端端口。

## 环境变量

后端默认可以在无真实外部服务的情况下启动健康检查。后续接入真实 RAG 能力时，建议配置：

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/legal_contract_assistant?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your_password"
$env:OPENAI_API_KEY="your_api_key"
$env:OPENAI_BASE_URL="https://your-openai-compatible-endpoint"
$env:OPENAI_EMBEDDING_MODEL="Qwen/Qwen3-Embedding-4B"
$env:ELASTICSEARCH_URIS="http://localhost:9200"
```

不要把真实密码、Token 或 API Key 写入 Git。

## MySQL 初始化

后端默认连接：

```text
jdbc:mysql://localhost:3306/legal_contract_assistant
```

数据库初始化脚本：

```text
backend/src/main/resources/db/init.sql
```

MySQL 安装并启动后执行：

```powershell
mysql -u root -p < backend/src/main/resources/db/init.sql
```

更多说明见：

```text
docs/mysql-setup.md
```

## 已验证命令

后端：

```powershell
cd backend
mvn test
mvn package -DskipTests
```

前端：

```powershell
cd frontend
npm.cmd install
npm.cmd run build
```

接口：

```text
GET /api/health -> {"status":"ok"}
```

CORS 预检也已验证，后端会返回：

```text
Access-Control-Allow-Origin: http://localhost:5173
Access-Control-Allow-Methods: GET,POST,PUT,DELETE,OPTIONS
```

## 常见问题

### 8080 端口被占用

表现：后端启动失败，提示 `Port 8080 was already in use`。

处理方式一：关闭占用 8080 的旧后端进程。

处理方式二：临时换端口启动：

```powershell
cd backend
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"
```

如果后端改为 8081，前端代理也要同步改为：

```js
target: 'http://localhost:8081'
```

### 前端无法访问后端接口

优先检查：

- 后端是否已启动。
- `http://localhost:8080/api/health` 是否返回 `{"status":"ok"}`。
- 前端是否通过 `http://localhost:5173` 访问，而不是直接打开 `index.html`。
- `frontend/vite.config.js` 中代理目标端口是否和后端端口一致。
- 后端 CORS 白名单是否包含当前前端 origin。

### Node 版本不符合要求

当前已验证 Node `v22.21.0` 可用。如果安装依赖失败，先检查：

```powershell
node --version
npm.cmd --version
```

### Maven 依赖下载失败

检查网络连接或 Maven 镜像源。首次构建会下载 Spring Boot、Spring AI、MyBatis-Plus、Elasticsearch 等依赖，耗时可能较长。

### 未配置 OpenAI API Key

当前骨架默认使用 `demo-key` 占位，方便健康检查和基础启动。真正调用大模型或 Embedding 接口时，必须通过环境变量配置真实 `OPENAI_API_KEY`。

### 未配置 MySQL 或 Elasticsearch

当前阶段只搭建工程骨架、前端原型和健康检查接口，尚未实现数据库或向量检索业务逻辑。后续实现相关接口时再启动并配置 MySQL、Elasticsearch。
