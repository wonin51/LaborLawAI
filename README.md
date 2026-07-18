# LaborLawAI

劳动合同法律助手 AI + RAG 实训项目。当前项目包含一个 Spring Boot 后端和一个 Vue 3 + Vite 前端，先完成基础启动、页面入口和后端健康检查联通。

## 项目结构

```text
LaborLawAI/
├─ backend/    # Java 17 + Spring Boot 后端项目，项目名 rag-kb-demo
├─ frontend/   # Vue 3 + Vite 前端项目，项目名 rag-kb-web
├─ stitch_ui/  # UI 设计参考
└─ pom.xml     # Maven 聚合入口，包含 backend 模块
```

## 环境要求

```bash
java -version
mvn -version
node -v
npm -v
```

建议版本：

| 工具 | 要求 |
| --- | --- |
| JDK | 17 或更高，推荐 JDK 17 |
| Maven | 3.9.x |
| Node.js | 20 或更高，推荐 22.x |
| npm | 10.x |

注意：后端使用 Java 17 编译，不能用 Java 8 启动。

## 启动后端

方式一：从项目根目录启动。

```bash
cd D:\2026-shixi\LaborLawAI
mvn -pl backend spring-boot:run
```

方式二：进入后端目录启动。

```bash
cd D:\2026-shixi\LaborLawAI\backend
mvn spring-boot:run
```

后端默认端口：

```text
http://localhost:8080
```

## 启动前端

首次启动前安装依赖：

```bash
cd D:\2026-shixi\LaborLawAI\frontend
npm install
```

启动开发服务器：

```bash
npm run dev
```

前端默认访问地址：

```text
http://localhost:5173
```

前端默认后端地址配置在：

```text
frontend/.env
```

默认值：

```env
VITE_API_BASE_URL=http://localhost:8080
```

## 健康检查接口

后端健康检查：

```bash
curl http://localhost:8080/api/health
```

正常返回：

```json
{"status":"ok"}
```

浏览器也可以直接打开：

```text
http://localhost:8080/api/health
```

前端页面会自动请求该接口。后端未启动或连接失败时，页面会显示：

```text
后端服务未连接
```

## 构建验证

后端测试：

```bash
cd D:\2026-shixi\LaborLawAI
mvn test
```

后端打包：

```bash
cd D:\2026-shixi\LaborLawAI
mvn package -DskipTests
```

前端构建：

```bash
cd D:\2026-shixi\LaborLawAI\frontend
npm run build
```

## 常见问题

### 1. 8080 端口被占用

现象：

```text
Web server failed to start. Port 8080 was already in use.
```

查看占用进程：

```powershell
Get-NetTCPConnection -LocalPort 8080
```

结束占用进程，将 `<PID>` 替换为 `OwningProcess` 对应的进程号：

```powershell
Stop-Process -Id <PID> -Force
```

也可以临时换端口启动：

```bash
cd D:\2026-shixi\LaborLawAI\backend
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

如果后端改为 `8081`，前端也要修改：

```env
VITE_API_BASE_URL=http://localhost:8081
```

### 2. Node 版本不符合要求

现象：`npm install` 或 `npm run dev` 报 Node 版本不支持。

检查版本：

```bash
node -v
```

处理方式：安装 Node.js 20 或更高版本，然后重新安装依赖。

```bash
cd D:\2026-shixi\LaborLawAI\frontend
npm install
npm run dev
```

### 3. Maven 依赖下载失败

现象：插件或依赖 unresolved，或者下载中断。

先确认 Maven 能正常联网并重新下载：

```bash
cd D:\2026-shixi\LaborLawAI
mvn -U test
```

如果提示锁或下载残留，关闭 IDEA 的 Maven 导入任务后重试：

```bash
mvn -U package -DskipTests
```

如果本机默认 Java 是 8，需要先切换到 JDK 17：

```powershell
$env:JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-17.0.19\OpenJDK17U-jdk_x64_windows_hotspot_17.0.19_10\jdk-17.0.19+10"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
mvn -version
```

### 4. 前端无法访问后端接口

检查后端是否启动：

```bash
curl http://localhost:8080/api/health
```

检查前端环境变量：

```text
D:\2026-shixi\LaborLawAI\frontend\.env
```

应包含：

```env
VITE_API_BASE_URL=http://localhost:8080
```

修改 `.env` 后需要重启前端：

```bash
cd D:\2026-shixi\LaborLawAI\frontend
npm run dev
```

如果浏览器控制台出现跨域错误，确认后端已包含 CORS 配置，并重新启动后端。
