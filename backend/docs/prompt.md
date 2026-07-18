# 第二课时：数据库基础与 MySQL 接入（法律助手版）

## 学习目标

本节围绕劳动合同法律助手场景，完成数据库基础建设：

- ~~设计 25 张 MySQL 业务表。~~（✅ 已完成，见 `backend/db/init.sql`）
- 准备 8~10 条法律文档示例数据。
- 将 SQL 脚本放到 `backend/db/`。
- 让 Spring Boot 后端接入 MySQL。
- 提供数据库连通性检查接口。

本节重点是数据库连接配置和连通性验证，不实现业务 CRUD。

## 前置说明

任务1（建表脚本）已由团队完成，输出文件为：

```
backend/db/init.sql
```

包含 25 张表，覆盖 RBAC、咨询问答、知识库、反馈、AI运行审计等完整 MVP 模块。后续任务基于该表结构生成示例数据。

## 使用方式

下面的内容可以按顺序复制给 Codex 或开发同学执行。每完成一个任务后，先确认文件位置和构建结果，再继续下一个任务。

## 任务 01：生成法律文档示例数据（已完成，直接跳过）

> 已由团队完成，输出 `backend/db/init.sql`，包含 25 张 MVP 核心表。示例数据将在任务2中补充。

## 任务 02：生成法律文档示例数据

```text
请帮我在数据库中插入法律文档示例数据。

SQL 文件输出位置：
backend/db/demo-data.sql

背景：
建表脚本已完成，现在需要为基础法律知识库准备示例数据。
请根据 init.sql 中的表结构，生成符合以下要求的示例数据：

1. 插入 8~10 条 legal_document（知识文档）记录：
   - 覆盖 document_type 类型：law（法律）、judicial（司法解释）、policy（人社政策）、faq（FAQ）、sample_contract（示例合同）
   - 文档标题要具体，如：《中华人民共和国劳动合同法》
   - 来源名称要规范，如：全国人民代表大会常务委员会
   - topic_tags 用逗号分隔，如：劳动合同,解除,经济补偿
   - 正文使用真实的法规条款摘要（至少包含 3~5 条关键条款）
   - status 统一为 draft（草稿状态，待后续索引）

2. 插入 2~3 条 qa_topic（问题分类）记录：
   - 至少覆盖：加班工资、试用期、未签合同
   - topic_code 使用英文缩写

3. 插入 1 条 ai_prompt 和 1 条 ai_prompt_version：
   - prompt_code: LABOR_LAW_QA
   - prompt_name: 劳动合同法律问答
   - template_text: 包含标准的回答模板（结论摘要、法律依据、维权流程、证据清单、注意事项）

4. 插入 1 条 sys_account：
   - username: admin
   - password_hash: 使用 BCrypt 加密 "admin123"
   - display_name: 系统管理员
   - email: admin@laborlaw.local

5. 插入 2 条 sys_role：
   - ADMIN / 管理员
   - REVIEWER / 评审员

6. 关联 admin 账号与 ADMIN 角色

要求：
1. SQL 可以直接插入到现有表中。
2. 所有 INSERT 语句要有明确的字段列表。
3. 文档正文内容要真实、具体，不要过于简单。
4. 建议直接使用 init.sql 中已有的种子数据部分作为基础，补充更多法律文档示例。
5. 如果使用 init.sql 中已存在的种子数据，请注明"数据已存在，无需重复插入"。
```

## 任务 03：后端接入 MySQL

```text
请帮我在后端项目中接入 MySQL。

当前项目结构：
- 后端根目录：backend/
- 配置文件：backend/src/main/resources/application.yml
- 包名：com.laborlaw.ragkbdemo

要求：
1. 在 backend/src/main/resources/application.yml 中增加 datasource 配置。
2. 数据库连接信息支持从环境变量读取，不要在配置中写死真实密码。
3. 增加 MyBatis-Plus 基础配置（mapper-locations、type-aliases-package）。
4. 新增数据库连通性检查接口：
   GET /api/db/ping
   返回当前数据库时间（SELECT NOW()）
5. 返回统一 ApiResponse 格式：
   {
     "code": 0,
     "message": "success",
     "data": {
       "timestamp": "2026-07-18 16:00:00",
       "status": "ok"
     }
   }
6. 如果连接失败，要返回清晰错误信息，便于定位问题。
7. 保持实现简单，只需完成连通性检查。

配置示例：
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:legal_assistant}?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:}

完成后请说明需要配置哪些环境变量。
```

## 任务 04：按本机环境填写数据库配置

```text
请根据当前项目可用的 MySQL 连接信息，更新 application.yml 中的 datasource 配置。

要求：
1. 数据库地址、端口、数据库名、用户名、密码按当前可用环境填写。
2. 数据库名为 legal_assistant。
3. JDBC URL 需要包含：
   - useUnicode=true
   - characterEncoding=utf8
   - serverTimezone=Asia/Shanghai
   - useSSL=false
   - allowPublicKeyRetrieval=true
4. 保留 MyBatis-Plus 基础配置。
5. 更新后运行数据库连通性测试。
6. 不修改与本任务无关的代码。

注意：
- 如果项目需要提交到公共仓库，请不要提交真实数据库密码。
- 可以改用环境变量或本地配置文件保存敏感信息。
- 可以使用 application-dev.yml 或 application-local.yml 存放本地配置。
```

## 完成检查

完成本节后，请确认以下内容：

- ✅ `backend/db/init.sql` 存在（已由团队完成）
- ✅ `backend/db/demo-data.sql` 存在，包含法律文档示例数据
- ✅ 后端 `application.yml` 包含 MySQL datasource 配置
- ✅ 后端 `application.yml` 包含 MyBatis-Plus 基础配置
- ✅ `GET /api/db/ping` 可以返回当前数据库时间
- ✅ 数据库连接失败时，接口返回清晰错误信息
- ✅ 后端测试通过：`mvn test`（至少 DbPingController 测试通过）
- ✅ 后端构建通过：`mvn package -DskipTests`

## 相关资源

- 建表脚本：`backend/db/init.sql`
- 示例数据：`backend/db/demo-data.sql`
- 后端配置：`backend/src/main/resources/application.yml`
- 数据库设计说明书：见之前的文档

