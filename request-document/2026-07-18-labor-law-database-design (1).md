# 劳动合同法律助手数据库设计

- 状态：已批准
- 日期：2026-07-18
- 适用版本：MVP/V1.0，并为 V1.1 保留稳定扩展接口
- 技术基线：MySQL 8、Elasticsearch 8、对象存储、本地或第三方大模型 API

## 1. 背景与目标

本设计依据《劳动合同法律助手需求分析文档》和《业务场景》形成，服务于以下 MVP 能力：

1. 游客匿名咨询与连续追问。
2. 结构化回答，包括结论、法律依据、维权流程、证据清单和注意事项。
3. 关键结论级引用溯源。
4. 法规、司法解释、政策、FAQ、示例合同和用户补充参考的版本化入库。
5. 用户点踩补充内容经审核后形成“用户补充参考”，并可追溯到原始反馈和最终知识片段。
6. 用户反馈、管理员处理、提示词版本和操作审计。
7. 管理后台的 API、菜单和按钮级 RBAC。

设计成功标准：

- MySQL 中的业务事实可独立解释，不依赖 Elasticsearch 当前状态。
- 历史答案在索引重建或知识文档停用后仍可展示当时的引用。
- 用户补充内容必须标注为“用户补充参考”，不得作为法律结论的唯一依据。
- 未脱敏问题、身份证号、手机号和 API Key 不进入普通业务日志或运行表。
- 外部调用不持有数据库长事务，重试操作保持幂等。
- MVP 表结构可支持 V1.1 普通用户、收藏、导出、合同审查和 FAQ 维护。

## 2. 范围与非目标

### 2.1 本期范围

- 25 张 MVP 核心表。
- MySQL 与 Elasticsearch 的数据职责分工。
- 主外键、唯一约束、关键索引和删除语义。
- 文档发布、问答生成、反馈处理和数据清理状态流。
- 事务、幂等、异常恢复和验证策略。

### 2.2 本期非目标

- 不实现部门、岗位、租户和行级数据权限。
- 不提前创建 V1.1 的 5 张业务表。
- 不覆盖 V2.0 的多地区用户画像、仲裁材料生成和完整工作流引擎。
- 不把文件二进制、片段正文或向量保存在 MySQL。
- 不保存未脱敏的用户问题或完整第三方模型请求与响应。

## 3. 总体架构与数据职责

### 3.1 MySQL

MySQL 是事实库，保存：

- 账号、角色、权限和菜单。
- 匿名会话、脱敏消息、结构化答案、关键结论和引用快照。
- 逻辑文档、不可变版本、入库任务和片段注册信息。
- 反馈、处理轨迹、反馈与知识片段的来源关联、提示词版本、模型运行指标和审计记录。

### 3.2 Elasticsearch

Elasticsearch 是可重建的检索副本，保存：

- 片段正文 `content`。
- 向量 `content_vector`。
- 文档、版本、效力、辖区、权威级别和条款定位等检索过滤字段。

### 3.3 对象存储

对象存储保存原始 PDF、DOCX、TXT 等文件。MVP 可使用本地存储，后续切换 MinIO 或 S3。MySQL 只保存存储键、哈希、类型和大小，不保存文件二进制或带密钥的临时访问 URL。

### 3.4 数据域

| 数据域 | 表数 | 职责 |
|---|---:|---|
| RBAC | 6 | 账号、角色、权限、菜单及多对多授权 |
| 咨询与回答 | 6 | 分类、会话、消息、答案、关键结论和引用 |
| 知识库 | 5 | 文件、逻辑文档、版本、入库任务和片段注册 |
| 反馈与质量 | 3 | 回答反馈、管理员处理轨迹及反馈到知识片段的来源关联 |
| AI 运行与审计 | 5 | 提示词、生成运行、操作审计和清理记录 |

## 4. 物理设计约定

- 表名、字段名采用小写 `snake_case`。
- 内部主键使用 `BIGINT UNSIGNED`；对外标识使用 26 位 ULID `CHAR(26)`。
- 字符集使用 `utf8mb4`，排序规则统一使用项目选定的 MySQL 8 Unicode 排序规则。
- 时间使用 `DATETIME(3)`，应用与数据库统一按 UTC 写入，界面按用户时区展示。
- 金额若后续出现，使用 `DECIMAL`，禁止使用浮点类型。
- 状态字段使用 `VARCHAR` 加 CHECK 约束或应用枚举，不使用 MySQL ENUM，便于迁移。
- 可变主数据包含 `created_at`、`updated_at`；需要并发编辑的表包含 `lock_version INT NOT NULL DEFAULT 0`。
- 版本、处理轨迹和审计表采用只追加策略。
- 账号、RBAC、知识版本和提示词版本以停用或退役代替物理删除。
- 匿名咨询正文默认保留 30 天，期限可配置。

## 5. 核心表设计

### 5.1 RBAC 权限域

#### 5.1.1 `sys_account`

统一账号表。MVP 启用管理员和只读评审，V1.1 启用普通用户。

| 字段 | 类型 | 约束/说明 |
|---|---|---|
| id | BIGINT UNSIGNED | PK |
| username | VARCHAR(64) | NOT NULL，唯一 |
| password_hash | VARCHAR(255) | NOT NULL，仅保存 BCrypt 或 Argon2 哈希 |
| display_name | VARCHAR(64) | NOT NULL |
| email | VARCHAR(128) | NULL，唯一 |
| status | VARCHAR(16) | ACTIVE、DISABLED、LOCKED |
| failed_login_count | SMALLINT UNSIGNED | 默认 0 |
| locked_until | DATETIME(3) | NULL |
| last_login_at | DATETIME(3) | NULL |
| password_changed_at | DATETIME(3) | NOT NULL |
| created_at、updated_at | DATETIME(3) | NOT NULL |

索引：唯一 `username`、唯一 `email`、普通索引 `status`。

#### 5.1.2 `sys_role`

字段：`id`、唯一 `role_code VARCHAR(64)`、`role_name VARCHAR(64)`、`description VARCHAR(255) NULL`、`is_builtin BOOLEAN`、`status VARCHAR(16)`、`lock_version`、创建与更新时间。

种子角色：`ADMIN`、`REVIEWER`；V1.1 增加 `USER`。

#### 5.1.3 `sys_permission`

字段：`id`、唯一 `permission_code VARCHAR(128)`、`permission_name VARCHAR(64)`、`permission_type VARCHAR(16)`、`http_method VARCHAR(8) NULL`、`route_pattern VARCHAR(255) NULL`、`description VARCHAR(255) NULL`、`status VARCHAR(16)`、创建与更新时间。

权限类型：`API`、`MENU`、`BUTTON`。示例权限码：`kb:document:publish`。

索引：`(permission_type, status)`。

#### 5.1.4 `sys_menu`

字段：`id`、`parent_id NULL`、`permission_id NULL`、`node_type VARCHAR(16)`、`menu_name VARCHAR(64)`、`route_path VARCHAR(255) NULL`、`component_key VARCHAR(128) NULL`、`icon VARCHAR(64) NULL`、`sort_order INT`、`visible BOOLEAN`、`status VARCHAR(16)`、`lock_version`、创建与更新时间。

- `parent_id` 外键指向 `sys_menu.id`。
- `permission_id` 外键指向 `sys_permission.id`。
- 节点类型为 `CATALOG`、`MENU`、`BUTTON`。
- 索引为 `(parent_id, sort_order)` 和 `permission_id`。
- 菜单可见性由角色权限计算，不建立 `role_menu`。

#### 5.1.5 `sys_account_role`

字段：`account_id`、`role_id`、`assigned_by NULL`、`assigned_at`、`expires_at NULL`。

- 复合主键：`(account_id, role_id)`。
- 外键分别指向账号和角色，删除策略为 RESTRICT。
- 反向索引：`(role_id, account_id)`。

#### 5.1.6 `sys_role_permission`

字段：`role_id`、`permission_id`、`granted_by NULL`、`granted_at`。

- 复合主键：`(role_id, permission_id)`。
- 外键分别指向角色和权限，删除策略为 RESTRICT。
- 反向索引：`(permission_id, role_id)`。

### 5.2 咨询与回答域

#### 5.2.1 `qa_topic`

字段：`id`、唯一 `topic_code VARCHAR(64)`、`topic_name VARCHAR(64)`、`answer_strategy VARCHAR(32)`、`sort_order INT`、`enabled BOOLEAN`、`description VARCHAR(255) NULL`、`lock_version`、创建与更新时间。

回答策略：`STANDARD`、`PROCESS`、`REVIEW`、`REFUSAL`。分类名可调整，业务关联只保存 `topic_id`。

#### 5.2.2 `qa_session`

| 字段 | 类型 | 约束/说明 |
|---|---|---|
| id | BIGINT UNSIGNED | PK |
| public_id | CHAR(26) | 唯一 ULID，对外使用 |
| account_id | BIGINT UNSIGNED | NULL，FK 到 `sys_account`，MVP 游客为空 |
| title | VARCHAR(120) | NULL |
| source_channel | VARCHAR(32) | WEB 等 |
| status | VARCHAR(16) | ACTIVE、CLOSED、EXPIRED |
| last_active_at | DATETIME(3) | NOT NULL |
| expires_at | DATETIME(3) | NOT NULL |
| closed_at | DATETIME(3) | NULL |
| created_at、updated_at | DATETIME(3) | NOT NULL |

索引：唯一 `public_id`、`(account_id, last_active_at)`、`(status, expires_at)`。

#### 5.2.3 `qa_message`

| 字段 | 类型 | 约束/说明 |
|---|---|---|
| id | BIGINT UNSIGNED | PK |
| session_id | BIGINT UNSIGNED | FK 到 `qa_session` |
| seq_no | INT UNSIGNED | 会话内顺序号 |
| client_request_id | VARCHAR(64) | NULL，客户端幂等键 |
| role | VARCHAR(16) | USER、ASSISTANT |
| content_redacted | MEDIUMTEXT | 唯一持久化正文，禁止写未脱敏原文 |
| detected_topic_id | BIGINT UNSIGNED | NULL，FK 到 `qa_topic` |
| pii_detected | BOOLEAN | 是否检测到敏感信息 |
| redaction_info | JSON | NULL，只保存敏感类型和数量 |
| created_at | DATETIME(3) | NOT NULL |

约束与索引：

- 唯一 `(session_id, seq_no)`。
- 唯一 `(session_id, client_request_id)`；`client_request_id` 为空时允许普通系统消息。
- 索引 `(session_id, created_at)`。
- 助手消息是用户最终看到的回答正文事实源。

#### 5.2.4 `qa_answer`

字段：`id`、唯一 `message_id`、唯一 `question_message_id`、`topic_id NULL`、`answer_status VARCHAR(24)`、`conclusion_summary TEXT NULL`、`action_steps JSON NULL`、`evidence_checklist JSON NULL`、`cautions JSON NULL`、`refusal_code VARCHAR(32) NULL`、`disclaimer_text VARCHAR(500)`、`citation_coverage DECIMAL(5,2) NULL`、`completed_at DATETIME(3) NULL`、创建与更新时间。

- `message_id` 指向 ASSISTANT 消息。
- `question_message_id` 指向 USER 消息。
- 每个用户问题只对应一条答案；失败重试新增生成运行记录，不新增答案。
- 状态：`GENERATING`、`COMPLETED`、`REFUSED`、`INSUFFICIENT`、`FAILED`。
- JSON 只保存有序流程和清单；结论和引用单独建表。

#### 5.2.5 `qa_answer_claim`

字段：`id`、`answer_id`、`claim_no SMALLINT UNSIGNED`、`claim_type VARCHAR(24)`、`claim_text TEXT`、`support_status VARCHAR(16)`、`sort_order SMALLINT UNSIGNED`、`created_at`。

- 类型：`CONCLUSION`、`LEGAL_BASIS`、`PROCEDURE`、`RISK`。
- 支撑状态：`SUPPORTED`、`PARTIAL`、`UNSUPPORTED`。
- 唯一 `(answer_id, claim_no)`。
- 索引 `(answer_id, sort_order)`。

#### 5.2.6 `qa_answer_citation`

字段：`id`、`claim_id`、`document_version_id`、`chunk_ref_id`、`citation_no SMALLINT UNSIGNED`、`retrieval_rank SMALLINT UNSIGNED`、`relevance_score DECIMAL(7,6) NULL`、`source_title_snapshot VARCHAR(255)`、`source_label_snapshot VARCHAR(64)`、`section_snapshot VARCHAR(255) NULL`、`article_no_snapshot VARCHAR(64) NULL`、`source_url_snapshot VARCHAR(1024) NULL`、`quote_snapshot TEXT`、`created_at`。

- 唯一 `(claim_id, chunk_ref_id)`。
- 复合外键 `(chunk_ref_id, document_version_id)` 引用 `kb_chunk_ref(id, document_version_id)`，禁止片段与文档版本错配。
- 索引 `document_version_id`、`chunk_ref_id`。
- 快照字段保存当时实际展示的来源信息和片段，保证历史答案可解释。

### 5.3 知识库域

#### 5.3.1 `file_object`

字段：`id`、`storage_provider VARCHAR(16)`、`bucket_name VARCHAR(128) NULL`、唯一 `object_key VARCHAR(512)`、`original_filename VARCHAR(255)`、`content_type VARCHAR(128)`、`file_extension VARCHAR(16)`、`size_bytes BIGINT UNSIGNED`、唯一 `sha256 CHAR(64)`、`status VARCHAR(16)`、`uploaded_by`、`created_at`、`deleted_at NULL`。

存储类型：`LOCAL`、`MINIO`、`S3`。相同 SHA256 的文件复用同一对象。

#### 5.3.2 `kb_document`

字段：`id`、唯一 `public_id CHAR(26)`、`title VARCHAR(255)`、`document_type VARCHAR(32)`、`issuing_authority VARCHAR(255) NULL`、`canonical_source_url VARCHAR(1024) NULL`、`jurisdiction_code VARCHAR(32)`、`scope_text VARCHAR(500) NULL`、`authority_level SMALLINT UNSIGNED`、`current_version_id NULL`、`status VARCHAR(16)`、`created_by`、`lock_version`、创建与更新时间。

文档类型：`LAW`、`JUDICIAL_INTERPRETATION`、`POLICY`、`FAQ`、`SAMPLE_CONTRACT`、`USER_CONTRIBUTION`。

`USER_CONTRIBUTION` 的固定展示标签为“用户补充参考”，并遵循以下规则：

- 只有审核通过且已建立反馈来源关联的版本可以发布。
- 检索排序低于法律法规、司法解释和官方政策，不得作为法律结论的唯一依据。
- 管理员应将反馈内容脱敏、核实和编辑后形成版本正文，不直接把用户原话作为权威材料发布。

`current_version_id` 与本表 `id` 组成复合外键，引用 `kb_document_version(id, document_id)`，保证当前版本属于当前文档。该循环外键在两张表创建后通过迁移追加。

#### 5.3.3 `kb_document_version`

字段：`id`、`document_id`、`version_no VARCHAR(32)`、`file_object_id NULL`、`document_number VARCHAR(128) NULL`、`source_url VARCHAR(1024) NULL`、`published_date DATE NULL`、`effective_date DATE NULL`、`expiry_date DATE NULL`、`validity_status VARCHAR(24)`、`review_status VARCHAR(16)`、`content_sha256 CHAR(64)`、`language_code VARCHAR(16)`、`created_by`、`reviewed_by NULL`、`reviewed_at NULL`、`created_at`。

- 唯一 `(document_id, version_no)`。
- 唯一 `(document_id, content_sha256)`。
- 为复合当前版本外键增加唯一索引 `(id, document_id)`。
- 效力状态：`DRAFT`、`PENDING`、`EFFECTIVE`、`EXPIRED`、`REPEALED`、`DISABLED`。
- 审批状态：`PENDING`、`APPROVED`、`REJECTED`。
- 审批通过后禁止修改，只能创建新版本。

#### 5.3.4 `kb_ingestion_job`

字段：`id`、唯一 `job_key CHAR(26)`、`document_version_id`、`job_type VARCHAR(16)`、`status VARCHAR(16)`、`attempt_no SMALLINT UNSIGNED`、唯一 `idempotency_key VARCHAR(128)`、`parser_name VARCHAR(64)`、`chunk_strategy VARCHAR(32)`、`chunk_size INT UNSIGNED NULL`、`chunk_overlap INT UNSIGNED NULL`、`es_index_name VARCHAR(128)`、`chunks_total INT`、`chunks_indexed INT`、`started_at NULL`、`finished_at NULL`、`error_code VARCHAR(64) NULL`、`error_message VARCHAR(1000) NULL`、`created_by`、`created_at`。

- 任务类型：`INGEST`、`REINDEX`、`DELETE_INDEX`。
- 状态：`QUEUED`、`RUNNING`、`SUCCEEDED`、`FAILED`、`CANCELED`。
- 为片段复合外键增加唯一索引 `(id, document_version_id)`。
- 索引 `(status, created_at)` 和 `document_version_id`。
- 重试复用任务并增加 `attempt_no`；幂等键阻止重复并发任务。

#### 5.3.5 `kb_chunk_ref`

字段：`id`、`document_version_id`、`ingestion_job_id`、`chunk_key VARCHAR(128)`、`chunk_no INT UNSIGNED`、`section_path VARCHAR(512) NULL`、`article_no VARCHAR(64) NULL`、`paragraph_no VARCHAR(64) NULL`、`content_sha256 CHAR(64)`、`token_count INT UNSIGNED`、`es_index_name VARCHAR(128)`、`es_document_id VARCHAR(128)`、`status VARCHAR(16)`、`created_at`。

- 唯一 `(document_version_id, chunk_key)`。
- 唯一 `(es_index_name, es_document_id)`。
- 为引用复合外键增加唯一索引 `(id, document_version_id)`。
- 复合外键 `(ingestion_job_id, document_version_id)` 引用 `kb_ingestion_job(id, document_version_id)`，保证任务与片段属于同一版本。
- 状态：`ACTIVE`、`SUPERSEDED`、`DELETED`。
- 不保存片段正文和向量。

### 5.4 反馈与质量域

#### 5.4.1 `qa_feedback`

字段：`id`、`answer_id NULL`、`session_id NULL`、`submitted_by_account_id NULL`、`rating SMALLINT`、`issue_type VARCHAR(32) NULL`、`comment_redacted VARCHAR(1000) NULL`、`status VARCHAR(16)`、`knowledge_disposition VARCHAR(16)`、`retention_hold BOOLEAN`、`promoted_at NULL`、`assigned_to NULL`、`resolved_at NULL`、`lock_version`、创建与更新时间。

- `rating` 取 1 或 -1。
- 问题类型：`INACCURATE`、`CITATION`、`UNCLEAR`、`SAFETY`、`OTHER`。
- 状态：`OPEN`、`IN_PROGRESS`、`RESOLVED`、`IGNORED`。
- 入库处置：`NONE`、`PROPOSED`、`APPROVED`、`PUBLISHED`、`REJECTED`。
- `retention_hold` 只在内容已发布为用户补充参考时设为 true；会话到期后清除账号、会话和答案关联，但保留脱敏反馈及来源链。
- 唯一 `(answer_id, session_id)`，重复评价使用 UPSERT 更新当前反馈。
- 索引 `(status, created_at)` 和 `assigned_to`。

#### 5.4.2 `qa_feedback_action`

字段：`id`、`feedback_id`、`operator_id`、`action_type VARCHAR(32)`、`from_status VARCHAR(16) NULL`、`to_status VARCHAR(16) NULL`、`action_note VARCHAR(1000) NULL`、`document_version_id NULL`、`prompt_version_id NULL`、`created_at`。

处理记录只追加，不修改。动作类型：`COMMENT`、`STATUS_CHANGE`、`KB_FIX`、`PROMPT_FIX`、`KNOWLEDGE_APPROVED`、`KNOWLEDGE_PUBLISHED`。

#### 5.4.3 `qa_feedback_knowledge_link`

反馈与最终知识片段的多对多来源表。字段：`id`、`feedback_id`、`feedback_action_id`、`document_version_id`、`chunk_ref_id`、`created_by`、`created_at`。

- 唯一 `(feedback_id, chunk_ref_id)`，防止同一反馈重复关联同一片段。
- 复合外键 `(chunk_ref_id, document_version_id)` 引用 `kb_chunk_ref(id, document_version_id)`，保证片段属于记录的文档版本。
- `feedback_action_id` 必须指向 `KNOWLEDGE_PUBLISHED` 动作，表明该关联经过审核和发布。
- 索引 `(chunk_ref_id, feedback_id)` 和 `(document_version_id, feedback_id)`，用于统计用户补充片段的检索命中与反馈来源。
- 仅在入库任务成功并生成最终 chunk_ref 后创建；发布前由 `qa_feedback_action.document_version_id` 追踪候选版本。

### 5.5 AI 运行与审计域

#### 5.5.1 `ai_prompt`

字段：`id`、唯一 `prompt_code VARCHAR(64)`、`prompt_name VARCHAR(128)`、`purpose VARCHAR(255)`、`status VARCHAR(16)`、`current_version_id NULL`、`created_by`、`lock_version`、创建与更新时间。

`current_version_id` 与本表 `id` 组成复合外键，引用 `ai_prompt_version(id, prompt_id)`，保证当前版本属于当前提示词。循环外键在两张表创建后追加。

#### 5.5.2 `ai_prompt_version`

字段：`id`、`prompt_id`、`version_no VARCHAR(32)`、`template_text MEDIUMTEXT`、`variables_schema JSON`、`model_parameters JSON NULL`、`content_sha256 CHAR(64)`、`status VARCHAR(16)`、`created_by`、`approved_by NULL`、`approved_at NULL`、`created_at`。

- 唯一 `(prompt_id, version_no)` 和 `(prompt_id, content_sha256)`。
- 为复合当前版本外键增加唯一索引 `(id, prompt_id)`。
- 状态：`DRAFT`、`ACTIVE`、`RETIRED`。
- 激活后禁止修改；模板不包含 API Key。

#### 5.5.3 `ai_generation_run`

字段：`id`、唯一 `run_key CHAR(26)`、`session_id NULL`、`question_message_id NULL`、`answer_id NULL`、`prompt_version_id`、`model_provider VARCHAR(32)`、`model_name VARCHAR(128)`、`status VARCHAR(16)`、`retrieval_trace JSON NULL`、`retrieved_chunk_count INT`、`input_tokens INT`、`output_tokens INT`、`latency_ms INT`、`started_at`、`finished_at NULL`、`error_code VARCHAR(64) NULL`、`error_message VARCHAR(1000) NULL`。

- 会话、消息和答案外键在匿名数据删除时 `ON DELETE SET NULL`。
- `retrieval_trace` 只保存片段 ID、排序和分数，不保存正文。
- 状态：`RUNNING`、`SUCCEEDED`、`FAILED`、`CANCELED`。

#### 5.5.4 `sys_audit_log`

字段：`id`、`trace_id CHAR(26)`、`actor_account_id NULL`、`action_code VARCHAR(128)`、`target_type VARCHAR(64)`、`target_id VARCHAR(64) NULL`、`request_method VARCHAR(8) NULL`、`request_path VARCHAR(255) NULL`、`result VARCHAR(16)`、`ip_hash CHAR(64) NULL`、`before_data JSON NULL`、`after_data JSON NULL`、`created_at`。

- 只追加，不更新。
- 账号删除时外键置空。
- `before_data` 和 `after_data` 写入前过滤密码、令牌和个人敏感信息。
- 索引 `(actor_account_id, created_at)`、`(action_code, created_at)`、`(target_type, target_id, created_at)`。

#### 5.5.5 `sys_purge_log`

字段：`id`、唯一 `batch_key CHAR(26)`、`retention_type VARCHAR(32)`、`cutoff_at DATETIME(3)`、`status VARCHAR(16)`、`deleted_counts JSON`、`started_at`、`finished_at NULL`、`error_message VARCHAR(1000) NULL`。

清理日志只保存各表删除行数，不保存被删除正文或可逆标识。

## 6. Elasticsearch 映射

每个知识片段对应一个 Elasticsearch 文档：

| 类别 | 字段 |
|---|---|
| 标识与过滤 | chunk_key、document_id、document_version_id、document_type、source_label、validity_status、review_status、jurisdiction_code、authority_level |
| 来源定位 | title、section_path、article_no、paragraph_no、source_url、published_date、effective_date |
| 检索字段 | content、content_vector、content_sha256、indexed_at |

约束：

- `chunk_key` 和 `es_document_id` 由版本与片段位置确定，重跑入库任务覆盖相同文档。
- 新检索只允许当前文档版本、审批通过、当前有效且片段状态为 ACTIVE 的数据。
- `USER_CONTRIBUTION` 的 `source_label` 固定为“用户补充参考”，检索排序低于官方来源。
- 生成器不得用用户补充参考单独支撑 `LEGAL_BASIS` 或确定性法律结论；引用渲染必须展示来源标签。
- Elasticsearch 索引名称不写死在业务代码中，由配置与入库任务记录。

## 7. 核心业务流与事务边界

### 7.1 一次咨询

1. 应用在内存中脱敏问题。
2. 短事务写入 USER 消息，依靠 `(session_id, client_request_id)` 防重复。
3. 短事务创建 ASSISTANT 占位消息、`qa_answer(GENERATING)` 和 `ai_generation_run(RUNNING)`。
4. 事务外查询 Elasticsearch。
5. 事务外调用大模型；无合格依据时生成 `INSUFFICIENT`，基础设施故障生成 `FAILED`。
6. 单个短事务更新助手消息和结构化答案，插入 claims/citations，并完成 generation run。
7. 任一步数据库写入失败，最终事务整体回滚，不留下半套引用。

### 7.2 知识入库

1. 文件先写对象存储并校验类型、大小和 SHA256。
2. 短事务创建文件、文档版本和 QUEUED 入库任务。
3. worker 在事务外解析、切分并幂等写入 Elasticsearch。
4. 短事务登记 `kb_chunk_ref` 并完成任务。
5. 独立发布事务校验审批、效力和索引状态后切换 `current_version_id`。

### 7.3 用户反馈补充入库

1. 用户点踩并提交脱敏后的补充看法，反馈状态进入 `PROPOSED`。
2. 管理员审核、核实和编辑内容；不通过时标记 `REJECTED`。
3. 通过时创建 `USER_CONTRIBUTION` 类型的文档版本，并追加 `KNOWLEDGE_APPROVED` 动作。
4. 使用统一入库流水线生成片段；成功后创建 `qa_feedback_knowledge_link`，将原反馈关联到最终片段。
5. 发布事务验证来源关联完整，将反馈标记为 `PUBLISHED`、设置 `retention_hold = true`，并追加 `KNOWLEDGE_PUBLISHED` 动作。
6. 回答命中该片段时展示“用户补充参考”，且不得将其作为确定性法律结论的唯一依据。

### 7.4 文档发布规则

只有满足以下全部条件的版本可以成为当前版本：

- `review_status = APPROVED`。
- `validity_status = EFFECTIVE`。
- 最近有效入库任务状态为 `SUCCEEDED`。
- 片段数量大于 0，且登记数量与索引数量一致。

切换当前版本不删除旧版本、旧片段或历史引用。

## 8. 删除、保留与隐私

### 8.1 匿名咨询清理

匿名咨询默认保留 30 天。清理前先处理 `retention_hold = true` 的反馈：将其 `answer_id`、`session_id`、`submitted_by_account_id` 置空，保留脱敏反馈、审核动作和知识来源关联。其余数据按以下顺序分批物理删除：

1. 未设置保留标记的 `qa_feedback_action`
2. 未设置保留标记的 `qa_feedback`
3. `qa_answer_citation`
4. `qa_answer_claim`
5. `qa_answer`
6. `qa_message`
7. `qa_session`

跨多条关系的删除由应用服务显式排序，不依赖复杂的数据库多路径级联。批次结果写入 `sys_purge_log`。

### 8.2 保留数据

- `ai_generation_run` 的会话、消息和答案外键置空后保留性能指标。
- 已发布为用户补充参考的脱敏反馈、处理动作和 `qa_feedback_knowledge_link` 作为知识来源记录保留；所有用户身份和会话关联必须清除。
- RBAC 主数据使用 RESTRICT 和状态停用。
- 被引用或曾发布的知识版本、提示词版本不物理删除。
- 文件对象只有在没有文档版本引用时才可删除。

### 8.3 敏感信息红线

以下内容不得写入提示词、运行、反馈或审计表：

- 未脱敏用户问题。
- 身份证号、手机号等直接身份信息。
- API Key、访问令牌和对象存储签名 URL。
- 完整第三方模型请求与响应。

## 9. 异常恢复与幂等

| 异常 | 处理策略 |
|---|---|
| Elasticsearch 不可用 | 答案标记 FAILED/INFRASTRUCTURE，不得误报依据不足；恢复后幂等重试 |
| 模型超时或限流 | 生成运行标记 FAILED，有限指数退避；复用同一答案并新增运行记录 |
| 生成成功但最终落库失败 | 最终事务回滚；客户端重试命中幂等键并恢复现有占位记录 |
| ES 写入成功但 MySQL 登记失败 | 重跑任务，确定性 `es_document_id` 覆盖写，随后补登记 |
| 对象存储成功但数据库失败 | 定时扫描未登记对象并按存储键、哈希清理 |
| 并发后台编辑 | `lock_version` 配合 MyBatis-Plus 乐观锁返回冲突 |

## 10. 测试与验收

### 10.1 数据库约束

- 迁移可重复执行，开发环境可验证回滚。
- 唯一键、复合主键、外键和状态约束生效。
- `current_version_id` 必须属于相同文档或提示词。
- 已发布知识版本和已激活提示词版本不可修改。

### 10.2 集成与事务

- 使用 Testcontainers 验证 MySQL 与 Elasticsearch 集成。
- 答案、关键结论与引用原子提交。
- 入库失败重试不产生重复片段。
- RBAC 的 API、菜单和按钮权限结果一致。

### 10.3 隐私与保留

- 身份证号和手机号不进入持久化表。
- 审计与模型运行日志过滤敏感内容。
- 30 天到期清理顺序正确，运行指标完成去关联。
- 被清理正文无法通过日志恢复。

### 10.4 业务验收

- 每条关键结论至少一条引用，或明确标记依据不足。
- 历史答案在 Elasticsearch 重建后仍可展示引用快照。
- 停用或过期版本不参与新检索。
- 用户补充参考在检索、回答引用和来源展开中均显示固定标签，不与法律法规混同。
- 每个已发布的用户补充片段可通过 `qa_feedback_knowledge_link` 追溯到脱敏原始反馈和审核动作。
- 用户补充参考不能单独支撑确定性法律结论。
- 30 天清理后，已发布反馈仍可追溯，但不再保留用户账号、会话或答案关联。
- 重复请求、重复反馈、重复入库和并发发布保持幂等。

## 11. V1.1 延后创建的表

以下表只定义稳定接口，本期不创建：

| 表 | 稳定关系 |
|---|---|
| user_favorite | `account_id` + `answer_id` 唯一，复用账号和答案 |
| export_job | 关联 `account_id`、`session_id`、`file_object_id` |
| contract_review | 关联账号、会话和原始文件 |
| contract_review_issue | 关联 review；通过 `answer_claim_id` 复用现有引用链 |
| kb_faq | 发布时转换为 FAQ 类型的文档版本并进入统一索引流程 |

## 12. 迁移与实现顺序

建议迁移顺序：

1. RBAC 基础表与种子数据。
2. 文件、知识文档和版本表。
3. 入库任务和片段注册表。
4. 问题分类、会话、消息、答案、结论和引用表。
5. 提示词、提示词版本和生成运行表。
6. 反馈、处理轨迹、反馈知识来源关联、审计和清理表。
7. 追加文档与提示词的复合当前版本外键。
8. 创建 Elasticsearch 索引模板和别名。

本设计批准后，下一阶段应先编写实施计划，再创建 Flyway/Liquibase 迁移、MyBatis-Plus 实体和集成测试。
