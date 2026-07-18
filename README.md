# EOS 维修 RAG 知识库实训项目

## 项目简介

本项目是一个企业 AI RAG 知识库实训工程，当前包含：

- `backend`：Java 17 + Spring Boot 后端项目，项目名 `rag-kb-demo`
- `frontend`：Vue 3 + Vite 前端项目，项目名 `rag-kb-web`

当前阶段只搭建基础前后端工程、健康检查接口和前端基础入口，不包含复杂业务功能。

## 目录结构

```text
G:\AI-law-creater
├─ backend      # Spring Boot 后端
├─ frontend     # Vue 3 + Vite 前端
├─ 需求文档      # 本地需求材料
└─ README.md
```

## 环境版本要求

建议使用以下版本或兼容版本：

| 工具 | 要求 | 当前验证版本 |
| --- | --- | --- |
| JDK | 17 | 17.0.8 |
| Maven | 3.8+ | 3.9.15 |
| Node.js | 20+，建议 22+ 或 24+ | 24.18.0 |
| npm | 10+ | 11.16.0 |

检查命令：

```powershell
java -version
mvn -version
node -v
npm -v
```

## 后端启动方式

进入后端目录：

```powershell
cd /d G:\AI-law-creater\backend
```

安装依赖并运行测试：

```powershell
mvn test
```

启动后端服务：

```powershell
mvn spring-boot:run
```

也可以先打包再启动：

```powershell
mvn package -DskipTests
java -jar target\rag-kb-demo-0.0.1-SNAPSHOT.jar
```

后端默认端口：

```text
http://localhost:8080
```

## 前端启动方式

进入前端目录：

```powershell
cd /d G:\AI-law-creater\frontend
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
http://127.0.0.1:5173/
```

前端默认后端地址配置在：

```text
G:\AI-law-creater\frontend\.env
```

默认内容：

```env
VITE_API_BASE_URL=http://localhost:8080
```

## 健康检查接口

后端健康检查接口：

```http
GET /api/health
```

直接访问：

```text
http://localhost:8080/api/health
```

PowerShell 验证命令：

```powershell
Invoke-RestMethod http://localhost:8080/api/health
```

预期返回类似：

```json
{"status":"ok"}
```

如果当前环境中后端返回 `{"status":"UP"}`，前端也已兼容显示为后端已连接。

## 前端如何访问后端健康检查

前端开发环境通过 Vite 代理访问后端：

```text
http://127.0.0.1:5173/api/health
```

验证命令：

```powershell
Invoke-RestMethod http://127.0.0.1:5173/api/health
```

页面右上角会显示后端连接状态：

- 后端可访问：`后端服务已连接`
- 后端不可访问：`后端服务未连接`

## 常见启动问题

### 1. 8080 端口被占用

现象：后端启动失败，提示端口 `8080` 已被占用。

查看占用进程：

```powershell
netstat -ano | findstr :8080
```

结束对应进程，将 `<PID>` 替换为实际进程号：

```powershell
taskkill /PID <PID> /F
```

或者修改后端端口：

```text
G:\AI-law-creater\backend\src\main\resources\application.yml
```

示例：

```yaml
server:
  port: 8081
```

如果修改了后端端口，也要同步修改前端配置：

```text
G:\AI-law-creater\frontend\.env
```

示例：

```env
VITE_API_BASE_URL=http://localhost:8081
```

修改 `.env` 后需要重启前端：

```powershell
cd /d G:\AI-law-creater\frontend
npm run dev
```

### 2. Node 版本不符合要求

现象：`npm install` 或 `npm run dev` 报 Node 版本过低。

检查版本：

```powershell
node -v
npm -v
```

建议安装 Node.js 20+，推荐 22+ 或 24+。升级后重新执行：

```powershell
cd /d G:\AI-law-creater\frontend
npm install
npm run dev
```

### 3. Maven 依赖下载失败

现象：`mvn test`、`mvn package` 或 `mvn spring-boot:run` 下载依赖失败。

先确认网络可用，然后重试：

```powershell
cd /d G:\AI-law-creater\backend
mvn -U clean test
```

如果是本地 Maven 缓存损坏，可以删除对应依赖目录后重试。例如删除某个失败依赖缓存：

```powershell
Remove-Item -Recurse -Force "$env:USERPROFILE\.m2\repository\失败依赖路径"
mvn -U clean test
```

如果公司网络需要代理或私服，请检查 Maven 配置文件：

```text
%USERPROFILE%\.m2\settings.xml
```

### 4. 前端无法访问后端接口

现象：页面显示 `后端服务未连接`。

按顺序检查：

1. 后端是否启动：

```powershell
Invoke-RestMethod http://localhost:8080/api/health
```

2. 前端环境变量是否正确：

```powershell
Get-Content G:\AI-law-creater\frontend\.env
```

应包含：

```env
VITE_API_BASE_URL=http://localhost:8080
```

3. 前端是否重启过：

修改 `.env` 后必须重启前端：

```powershell
cd /d G:\AI-law-creater\frontend
npm run dev
```

4. Vite 代理是否可访问：

```powershell
Invoke-RestMethod http://127.0.0.1:5173/api/health
```

如果第 1 步成功但第 4 步失败，通常是前端未重启或 Vite 代理配置未生效。

## 快速启动顺序

先启动后端：

```powershell
cd /d G:\AI-law-creater\backend
mvn spring-boot:run
```

再启动前端：

```powershell
cd /d G:\AI-law-creater\frontend
npm install
npm run dev
```

访问前端：

```text
http://127.0.0.1:5173/
```