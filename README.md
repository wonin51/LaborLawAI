# 劳动合同法律助手

本项目是基于大模型与 RAG 的劳动合同法律信息检索与咨询辅助工具。本课完成前后端基础工程、三个功能入口和服务健康检查，不包含登录或实际业务功能。

> 本系统用于劳动合同法律信息检索与咨询辅助，不替代律师、仲裁机构或法院意见。

## 项目结构

```text
.
├─ backend/   Java 17 + Spring Boot 后端
├─ frontend/  Vue 3 + Vite 前端
└─ README.md  启动与联调说明
```

## 环境要求

- JDK 17
- Maven 3.9 或更高版本
- Node.js 20 或更高版本
- npm 10 或更高版本

可使用以下命令确认版本：

```powershell
java -version
mvn -version
node -v
npm -v
```

## 启动后端

在项目根目录执行：

```powershell
cd backend
mvn spring-boot:run
```

后端默认监听 `http://localhost:8080`。浏览器访问或使用命令检查健康状态：

```powershell
Invoke-RestMethod http://localhost:8080/api/health
```

原始响应为：

```json
{"status":"ok"}
```

执行测试和打包：

```powershell
cd backend
mvn test
mvn package
```

课程目录附带 Maven 3.9.9 和离线依赖仓库。无网络环境可从 `backend/` 执行：

```powershell
$env:JAVA_HOME = "C:\path\to\jdk-17"
& "..\course-materials\lesson-01-project-bootstrap\resources\toolchain\apache-maven-3.9.9\bin\mvn.cmd" -o "-Dmaven.repo.local=../course-materials/lesson-01-project-bootstrap/resources/maven-offline-repository" test
```

## 启动前端

新开一个终端，在项目根目录执行：

```powershell
cd frontend
npm install
npm run dev
```

浏览器访问 `http://localhost:5173`。页面顶部或侧栏会显示“后端服务已连接”；若后端未启动，则显示“后端服务未连接”。

前端默认请求 `http://localhost:8080`。如需更改后端地址，在启动前设置环境变量：

```powershell
$env:VITE_API_BASE_URL = "http://localhost:8080"
npm run dev
```

执行测试和生产构建：

```powershell
cd frontend
npm test
npm run build
```

## 联调验证

1. 启动后端，确认 `http://localhost:8080/api/health` 返回 `{"status":"ok"}`。
2. 启动前端并访问 `http://localhost:5173`。
3. 确认页面显示“后端服务已连接”。
4. 点击“法律咨询”“知识库管理”“问答记录”，确认内容区可切换。

## 常见问题

### 8080 端口被占用

查找占用进程：

```powershell
Get-NetTCPConnection -LocalPort 8080 | Select-Object OwningProcess
```

结束确认无误的进程，或在 `backend/src/main/resources/application.yml` 中修改 `server.port`。若端口改变，还需同步设置前端的 `VITE_API_BASE_URL`。

### Node 版本不符合要求

执行 `node -v`，确认主版本不低于 20。升级 Node.js 后删除旧的 `frontend/node_modules`，再执行 `npm install`。

### Maven 依赖下载失败

先确认 Maven 能访问中央仓库，并检查代理或镜像配置。无网络环境可使用上文课程离线仓库命令；若仍提示缺少依赖，需要联网执行一次相同命令并移除 `-o`，补齐仓库后再恢复离线构建。

### 前端无法访问后端接口

- 确认后端已经启动且健康接口可以直接访问。
- 确认 `VITE_API_BASE_URL` 与后端协议、主机和端口一致。
- 修改环境变量后重新启动 Vite。
- 检查浏览器开发者工具中的 Network 和 Console 信息。
- 本项目默认允许 `http://localhost:5173` 与 `http://127.0.0.1:5173` 跨域访问 `/api/**`。
