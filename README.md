# 大学生就业法律助手

## 1. 项目简介

本项目是一个面向大学生就业签约场景的法律助手基础工程，主要用于后续扩展：

- 劳务合同审查
- 就业法律知识查询
- AI 法律问答

当前阶段已经完成前后端基础框架搭建，不包含登录权限和复杂业务功能。

项目结构：

```text
.
├─ backend
│  └─ student-employment-law-assistant
│     ├─ pom.xml
│     └─ src
├─ frontend
│  └─ student-employment-law-web
│     ├─ package.json
│     └─ src
└─ README.md
```

## 2. 环境版本要求

| 工具 | 要求版本 | 当前项目验证版本 |
|---|---:|---:|
| JDK | 17 或以上 | 17.0.19 |
| Maven | 3.8 或以上 | 3.9.16 |
| Node.js | 20 或以上 | 22.23.1 |
| npm | 10 或以上 | 10.9.8 |

## 3. 后端启动方式

后端项目目录：

```text
backend/student-employment-law-assistant
```

进入后端目录：

```powershell
cd backend/student-employment-law-assistant
```

安装依赖并运行测试：

```powershell
mvn test
```

启动后端：

```powershell
mvn spring-boot:run
```

后端默认端口：

```text
http://localhost:8080
```

也可以使用 IntelliJ IDEA 打开并运行：

```text
backend/student-employment-law-assistant
```

运行入口类：

```text
src/main/java/com/example/studentemploymentlawassistant/StudentEmploymentLawAssistantApplication.java
```

## 4. 前端启动方式

前端项目目录：

```text
frontend/student-employment-law-web
```

进入前端目录：

```powershell
cd frontend/student-employment-law-web
```

安装依赖：

```powershell
npm install
```

启动前端开发服务：

```powershell
npm run dev
```

前端默认访问地址：

```text
http://localhost:5173
```

前端接口基础地址配置在：

```text
frontend/student-employment-law-web/.env
```

默认内容：

```env
VITE_API_BASE_URL=http://localhost:8080
```

## 5. 健康检查接口

后端健康检查接口：

```text
GET /api/health
```

完整访问地址：

```text
http://localhost:8080/api/health
```

正常返回：

```json
{"status":"ok"}
```

前端页面右上角会显示后端连接状态：

- 后端正常：`后端服务已连接`
- 后端不可用：`后端服务未连接`

## 6. 常见启动问题和处理方式

### 6.1 8080 端口被占用

现象：

```text
Web server failed to start. Port 8080 was already in use.
```

原因：

已有后端服务或其他程序占用了 8080 端口。

处理方式一：关闭占用 8080 的进程。

```powershell
Get-NetTCPConnection -LocalPort 8080
```

查看到 `OwningProcess` 后，关闭对应进程：

```powershell
Stop-Process -Id 进程ID -Force
```

处理方式二：临时修改后端端口。

```powershell
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"
```

如果改成 8081，需要同步修改前端 `.env`：

```env
VITE_API_BASE_URL=http://localhost:8081
```

### 6.2 Node 版本不符合要求

现象：

```text
Unsupported engine
```

或前端依赖安装、启动失败。

处理方式：

查看 Node 版本：

```powershell
node -v
```

建议使用 Node.js 20 或以上版本。

### 6.3 Maven 依赖下载失败

现象：

```text
Could not resolve dependencies
```

处理方式一：重新执行依赖下载。

```powershell
mvn clean test
```

处理方式二：检查网络或 Maven 镜像源。

如果依赖下载非常慢，可以配置国内 Maven 镜像源后重试。

### 6.4 前端无法访问后端接口

现象：

前端页面显示：

```text
后端服务未连接
```

处理步骤：

1. 确认后端已经启动。

```text
http://localhost:8080/api/health
```

2. 确认前端 `.env` 配置正确。

```env
VITE_API_BASE_URL=http://localhost:8080
```

3. 修改 `.env` 后，需要重启前端服务。

```powershell
npm run dev
```

4. 如果浏览器缓存旧页面，可以强制刷新。

```text
Ctrl + F5
```

### 6.5 npm 在 PowerShell 中无法运行

现象：

```text
npm.ps1 cannot be loaded because running scripts is disabled on this system
```

处理方式：

使用 Windows 可执行版 npm：

```powershell
npm.cmd install
npm.cmd run dev
```

## 7. 当前已验证命令

后端验证：

```powershell
cd backend/student-employment-law-assistant
mvn test
mvn package -DskipTests
```

前端验证：

```powershell
cd frontend/student-employment-law-web
npm install
npm run build
npm run dev
```
