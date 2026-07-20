# 任务05交接与验收说明

更新时间：2026-07-20

## 代码来源与范围

本次任务基于仓库 `zzh` 分支已有的劳动法律助手前后端框架完成。课程 zip、lesson-03 提示词和维修工单示例只用于参考 CRUD 接口形态、统一响应和联调验收方式，已改造成劳动法律知识库的法律文档场景，没有覆盖队友已有业务代码。

任务05已完成：

- 前端法律文档查询、筛选、分页列表、新建文档和详情查看。
- 前端 API 封装：`frontend/src/api/legalDocuments.js`、`frontend/src/api/health.js`。
- 后端统一响应 `ApiResponse<T>`。
- `GET /api/health`、`GET /api/db/ping`、`GET /api/legal-docs`、`GET /api/legal-docs/{id}`、`POST /api/legal-docs`。
- Vite `/api` 代理和 Spring Boot CORS 白名单，解决本地 `5173 -> 8080` 跨域问题。
- 页面与接口中的 Task 05 标注，方便队友定位改动。

## 计划书需求对照

| 计划书方向 | 当前落地 | 后续边界 |
|---|---|---|
| Java 17 + Spring Boot 后端 | 已有后端骨架、Controller、Service、VO、DTO | 接入真实业务数据后继续扩展 |
| Vue 3 + Vite 前端 | 已有法律咨询工作台和任务05文档管理页 | 按队友最终 UI 统一视觉细节 |
| MySQL | 已有 `db/init.sql` 和数据库探测接口 | 真实文档数据到位后替换内存 Service |
| RAG / Elasticsearch | 已预留依赖和项目结构 | 后续实现切分、检索、引用和答案生成 |
| Git / Feishu 协作 | 已有协作说明和上下文交接文档 | Feishu 需要用户自行完成授权，不记录密码 |

## 当前实现边界

任务05列表当前使用线程安全的内存演示数据，以保证前后端联调不依赖外部数据。`/api/db/ping` 只验证数据库连接能力，不代表文档列表已经从 MySQL 读取。后续接入 MySQL 时，应保持 `ApiResponse<PageResult<...>>` 等响应结构不变，这样前端无需重写。

## 本地验收

后端窗口：

```powershell
cd backend
mvn spring-boot:run
```

前端窗口：

```powershell
cd frontend
npm.cmd install
npm.cmd run dev
```

浏览器打开 `http://localhost:5173`，确认服务状态正常，并验证法律文档的筛选、详情和新建。也可以访问：

```text
GET  http://localhost:5173/api/health
GET  http://localhost:5173/api/db/ping
GET  http://localhost:5173/api/legal-docs?pageNo=1&pageSize=10
```

预期：接口返回统一成功结构，页面不出现 Axios 网络错误；直接访问后端时使用 `http://localhost:8080/api/...`。

## 队友接续顺序

1. 先按上面命令启动并验证三个接口。
2. 查看 `LegalDocumentController`、`LegalDocumentServiceImpl` 和 `LegalDocument*VO/DTO`，确认字段契约。
3. 收到真实数据后，在 Service 层替换内存查询为 Mapper 查询，保留前端接口路径和响应结构。
4. 运行 `mvn test`、`mvn package -DskipTests`、`npm.cmd run build`，再检查 CORS 和 Vite 代理。
5. 提交前不要把 `local-skills/`、数据库密码、API Key 或个人登录信息加入 Git。

## Git 状态

本地 `zzh` 分支已有任务05提交，当前相对 `origin/zzh` 领先两个提交。`local-skills/` 是本机辅助目录，保持未跟踪，不应上传。推送失败时先重试 `zzh`，只有确认远端网络可用且团队允许时才推送 `main`，避免误覆盖队友主分支。
