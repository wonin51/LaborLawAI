# 劳动合同法律助手

本项目是一个基于大模型与 RAG 的劳动合同法律信息检索与咨询辅助系统。系统定位为学生实训项目和法律咨询原型，不替代律师、仲裁机构或法院意见。

当前已完成后端知识库文档 CRUD 接口、配置型 RBAC、管理员审计日志，以及前端知识库文档的查询、新增、详情、编辑、删除和状态更新页面。后续可继续补充完整 RAG 入库链路、正式登录中心、依据引用和问答历史。

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

以下命令均假设当前项目路径为 `G:\LaborLawAI`，并在 PowerShell 中执行。

### 1. 准备 Java 和 Maven

后端需要：

- JDK 17
- Maven 3.8+
- MySQL 8.x（如只验证健康检查和部分 Mock 测试，可先不连接真实数据库）

检查版本：

```powershell
java -version
mvn -version
```

### 2. 初始化数据库（首次运行需要）

如果本机 MySQL 已启动，并且 root 用户可登录，可以执行：

```powershell
cd G:\LaborLawAI
mysql -u root -p < backend/db/init.sql
```

如果只想使用轻量旧版初始化脚本，也可以执行：

```powershell
cd G:\LaborLawAI
mysql -u root -p < backend/src/main/resources/db/init.sql
```

注意：`backend/db/init.sql` 是完整表结构脚本，包含 `DROP TABLE`，不要直接用于已有生产数据环境。

### 3. 配置后端环境变量

开发环境可以先使用默认值启动。若需要连接自己的 MySQL、模型服务或管理员接口，请在启动前设置：

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/legal_contract_assistant?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your_password"
$env:OPENAI_API_KEY="your_api_key"
$env:OPENAI_BASE_URL="https://your-openai-compatible-endpoint"
$env:OPENAI_EMBEDDING_MODEL="Qwen/Qwen3-Embedding-4B"
$env:ELASTICSEARCH_URIS="http://localhost:9200"
$env:ADMIN_ACCOUNTS="editor|replace-with-editor-token|KNOWLEDGE_READ,KNOWLEDGE_WRITE;reader|replace-with-reader-token|KNOWLEDGE_READ"
```

不要把真实密码、Token 或 API Key 写入 Git。

### 4. 启动后端开发服务

```powershell
cd G:\LaborLawAI\backend
mvn spring-boot:run
```

启动成功后，后端默认监听：

```text
http://localhost:8080
```

### 5. 验证后端是否启动成功

新开一个 PowerShell 窗口执行：

```powershell
curl.exe "http://localhost:8080/api/health"
```

预期返回统一响应结构，`code=0` 表示成功。

### 6. 后端测试和打包

运行测试：

```powershell
cd G:\LaborLawAI\backend
mvn test
```

打包：

```powershell
cd G:\LaborLawAI\backend
mvn package -DskipTests
```

打包后运行 JAR：

```powershell
cd G:\LaborLawAI\backend
java -jar target/legal-contract-assistant-0.0.1-SNAPSHOT.jar
```

## 前端启动

以下命令均假设当前项目路径为 `G:\LaborLawAI`，并在 PowerShell 中执行。

### 1. 准备 Node.js 和 npm

建议使用 Node.js 20+。检查版本：

```powershell
node -v
npm -v
```

### 2. 配置前端环境变量

可以复制示例环境文件：

```powershell
cd G:\LaborLawAI\frontend
Copy-Item .env.example .env -Force
```

常用配置项：

```text
VITE_API_BASE_URL=http://localhost:8080
VITE_ADMIN_API_TOKEN=replace-with-editor-token
```

说明：

- 普通只读接口，如 `/api/knowledge/documents`、`/api/qa/records`，当前不需要登录权限控制。
- 管理员接口 `/api/admin/**` 如需访问，需要与后端 `ADMIN_ACCOUNTS` 中某个账号的 token 保持一致。

### 3. 安装前端依赖

```powershell
cd G:\LaborLawAI\frontend
npm.cmd install
```

如果依赖已经安装过，也可以跳过本步骤。

### 4. 启动前端开发服务

```powershell
cd G:\LaborLawAI\frontend
npm.cmd run dev
```

启动成功后，浏览器访问：

```text
http://localhost:5173
```

前端开发服务会通过 Vite 代理把 `/api` 请求转发到后端 `http://localhost:8080`。

### 5. 前端测试和生产构建

运行测试：

```powershell
cd G:\LaborLawAI\frontend
npm.cmd test
```

生产构建：

```powershell
cd G:\LaborLawAI\frontend
npm.cmd run build
```

预览生产构建结果：

```powershell
cd G:\LaborLawAI\frontend
npm.cmd run preview
```

预览地址通常为：

```text
http://localhost:4173
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

`backend/src/main/java/com/laborlaw/ragkbdemo/config/WebMvcConfig.java` 中允许本地前端端口访问 `/api/**`：

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
管理员接口需要显式配置访问令牌。后端新配置使用 `ADMIN_ACCOUNTS`，前端使用其中一个账户的 token 配置 `VITE_ADMIN_API_TOKEN`。旧环境仍兼容 `ADMIN_API_TOKEN`：

```powershell
$env:ADMIN_ACCOUNTS="editor|replace-with-editor-token|KNOWLEDGE_READ,KNOWLEDGE_WRITE;reader|replace-with-reader-token|KNOWLEDGE_READ"
```

前端可以复制 `frontend/.env.example` 为本地环境文件，并填写 `editor` 或其他具有所需角色的账户 token。未配置后端令牌时，`/api/admin/**` 会返回 `503`；令牌缺失或不匹配时返回 `401`。

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
