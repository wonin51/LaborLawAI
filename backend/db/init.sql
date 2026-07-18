-- 设置客户端字符集
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 第一阶段：创建所有表（不含循环外键）
-- ============================================================

-- ============================================================
-- 1. RBAC 权限域 (6张表)
-- ============================================================

-- 1.1 sys_account - 统一账号表
DROP TABLE IF EXISTS `sys_account`;
CREATE TABLE `sys_account` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username` VARCHAR(64) NOT NULL COMMENT '用户名',
    `password_hash` VARCHAR(255) NOT NULL COMMENT '密码哈希(BCrypt/Argon2)',
    `display_name` VARCHAR(64) NOT NULL COMMENT '显示名称',
    `email` VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
    `status` VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/DISABLED/LOCKED',
    `failed_login_count` SMALLINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '登录失败次数',
    `locked_until` DATETIME(3) DEFAULT NULL COMMENT '锁定截止时间',
    `last_login_at` DATETIME(3) DEFAULT NULL COMMENT '最后登录时间',
    `password_changed_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '密码修改时间',
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='统一账号表';

-- 1.2 sys_role - 角色表
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `role_code` VARCHAR(64) NOT NULL COMMENT '角色编码',
    `role_name` VARCHAR(64) NOT NULL COMMENT '角色名称',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '描述',
    `is_builtin` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否内置角色',
    `status` VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/DISABLED',
    `lock_version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- 1.3 sys_permission - 权限表
DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `permission_code` VARCHAR(128) NOT NULL COMMENT '权限编码',
    `permission_name` VARCHAR(64) NOT NULL COMMENT '权限名称',
    `permission_type` VARCHAR(16) NOT NULL COMMENT '权限类型: API/MENU/BUTTON',
    `http_method` VARCHAR(8) DEFAULT NULL COMMENT 'HTTP方法(GET/POST/PUT/DELETE)',
    `route_pattern` VARCHAR(255) DEFAULT NULL COMMENT '路由匹配模式',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '描述',
    `status` VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/DISABLED',
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_permission_code` (`permission_code`),
    KEY `idx_type_status` (`permission_type`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

-- 1.4 sys_menu - 菜单表
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `parent_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '父菜单ID',
    `permission_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '关联权限ID',
    `node_type` VARCHAR(16) NOT NULL COMMENT '节点类型: CATALOG/MENU/BUTTON',
    `menu_name` VARCHAR(64) NOT NULL COMMENT '菜单名称',
    `route_path` VARCHAR(255) DEFAULT NULL COMMENT '路由路径',
    `component_key` VARCHAR(128) DEFAULT NULL COMMENT '组件标识',
    `icon` VARCHAR(64) DEFAULT NULL COMMENT '图标',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号',
    `visible` BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否可见',
    `status` VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/DISABLED',
    `lock_version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_parent_sort` (`parent_id`, `sort_order`),
    KEY `idx_permission_id` (`permission_id`),
    CONSTRAINT `fk_menu_parent` FOREIGN KEY (`parent_id`) REFERENCES `sys_menu` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_menu_permission` FOREIGN KEY (`permission_id`) REFERENCES `sys_permission` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜单表';

-- 1.5 sys_account_role - 账号角色关联表
DROP TABLE IF EXISTS `sys_account_role`;
CREATE TABLE `sys_account_role` (
    `account_id` BIGINT UNSIGNED NOT NULL COMMENT '账号ID',
    `role_id` BIGINT UNSIGNED NOT NULL COMMENT '角色ID',
    `assigned_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '授权人ID',
    `assigned_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '授权时间',
    `expires_at` DATETIME(3) DEFAULT NULL COMMENT '过期时间',
    PRIMARY KEY (`account_id`, `role_id`),
    KEY `idx_role_account` (`role_id`, `account_id`),
    CONSTRAINT `fk_ar_account` FOREIGN KEY (`account_id`) REFERENCES `sys_account` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_ar_role` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='账号角色关联表';

-- 1.6 sys_role_permission - 角色权限关联表
DROP TABLE IF EXISTS `sys_role_permission`;
CREATE TABLE `sys_role_permission` (
    `role_id` BIGINT UNSIGNED NOT NULL COMMENT '角色ID',
    `permission_id` BIGINT UNSIGNED NOT NULL COMMENT '权限ID',
    `granted_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '授权人ID',
    `granted_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '授权时间',
    PRIMARY KEY (`role_id`, `permission_id`),
    KEY `idx_permission_role` (`permission_id`, `role_id`),
    CONSTRAINT `fk_rp_role` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_rp_permission` FOREIGN KEY (`permission_id`) REFERENCES `sys_permission` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- ============================================================
-- 2. 咨询与回答域 (6张表)
-- ============================================================

-- 2.1 qa_topic - 问题分类表
DROP TABLE IF EXISTS `qa_topic`;
CREATE TABLE `qa_topic` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `topic_code` VARCHAR(64) NOT NULL COMMENT '分类编码',
    `topic_name` VARCHAR(64) NOT NULL COMMENT '分类名称',
    `answer_strategy` VARCHAR(32) NOT NULL DEFAULT 'STANDARD' COMMENT '回答策略: STANDARD/PROCESS/REVIEW/REFUSAL',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号',
    `enabled` BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否启用',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '描述',
    `lock_version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_topic_code` (`topic_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问题分类表';

-- 2.2 qa_session - 会话表
DROP TABLE IF EXISTS `qa_session`;
CREATE TABLE `qa_session` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `public_id` CHAR(26) NOT NULL COMMENT '对外ULID标识',
    `account_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '关联账号ID(游客为空)',
    `title` VARCHAR(120) DEFAULT NULL COMMENT '会话标题',
    `source_channel` VARCHAR(32) NOT NULL DEFAULT 'WEB' COMMENT '来源渠道',
    `status` VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/CLOSED/EXPIRED',
    `last_active_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '最后活跃时间',
    `expires_at` DATETIME(3) NOT NULL COMMENT '过期时间(默认30天)',
    `closed_at` DATETIME(3) DEFAULT NULL COMMENT '关闭时间',
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_public_id` (`public_id`),
    KEY `idx_account_active` (`account_id`, `last_active_at`),
    KEY `idx_status_expires` (`status`, `expires_at`),
    CONSTRAINT `fk_session_account` FOREIGN KEY (`account_id`) REFERENCES `sys_account` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会话表';

-- 2.3 qa_message - 消息表
DROP TABLE IF EXISTS `qa_message`;
CREATE TABLE `qa_message` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `session_id` BIGINT UNSIGNED NOT NULL COMMENT '会话ID',
    `seq_no` INT UNSIGNED NOT NULL COMMENT '会话内顺序号',
    `client_request_id` VARCHAR(64) DEFAULT NULL COMMENT '客户端幂等键',
    `role` VARCHAR(16) NOT NULL COMMENT '角色: USER/ASSISTANT',
    `content_redacted` MEDIUMTEXT NOT NULL COMMENT '脱敏后的消息正文',
    `detected_topic_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '检测到的问题分类ID',
    `pii_detected` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否检测到敏感信息',
    `redaction_info` JSON DEFAULT NULL COMMENT '脱敏信息(敏感类型和数量)',
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_session_seq` (`session_id`, `seq_no`),
    UNIQUE KEY `uk_session_client_request` (`session_id`, `client_request_id`),
    KEY `idx_session_created` (`session_id`, `created_at`),
    CONSTRAINT `fk_msg_session` FOREIGN KEY (`session_id`) REFERENCES `qa_session` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_msg_topic` FOREIGN KEY (`detected_topic_id`) REFERENCES `qa_topic` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息表';

-- 2.4 qa_answer - 答案表
--
-- ⚠️ JSON 字段 Schema 说明：
--   action_steps:        [{"step_no": 1, "title": "与用人单位协商", "description": "..."}]
--   evidence_checklist:  [{"item": "劳动合同", "required": true, "description": "..."}]
--   cautions:            [{"type": "时效", "content": "劳动争议仲裁时效为1年"}]
--
DROP TABLE IF EXISTS `qa_answer`;
CREATE TABLE `qa_answer` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `message_id` BIGINT UNSIGNED NOT NULL COMMENT '关联ASSISTANT消息ID',
    `question_message_id` BIGINT UNSIGNED NOT NULL COMMENT '关联USER消息ID',
    `topic_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '问题分类ID',
    `answer_status` VARCHAR(24) NOT NULL DEFAULT 'GENERATING' COMMENT '状态: GENERATING/COMPLETED/REFUSED/INSUFFICIENT/FAILED',
    `conclusion_summary` TEXT DEFAULT NULL COMMENT '结论摘要',
    `action_steps` JSON DEFAULT NULL COMMENT '维权流程步骤(JSON数组)',
    `evidence_checklist` JSON DEFAULT NULL COMMENT '证据清单(JSON数组)',
    `cautions` JSON DEFAULT NULL COMMENT '注意事项(JSON数组)',
    `refusal_code` VARCHAR(32) DEFAULT NULL COMMENT '拒绝回答代码',
    `disclaimer_text` VARCHAR(500) NOT NULL DEFAULT '本回答仅供参考，不构成正式法律意见。涉及重大争议请咨询专业律师。' COMMENT '免责声明',
    `citation_coverage` DECIMAL(5,2) DEFAULT NULL COMMENT '引用覆盖率(百分比)',
    `completed_at` DATETIME(3) DEFAULT NULL COMMENT '完成时间',
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_answer_message` (`message_id`),
    UNIQUE KEY `uk_answer_question` (`question_message_id`),
    KEY `idx_answer_topic` (`topic_id`),
    KEY `idx_answer_status` (`answer_status`),
    CONSTRAINT `fk_answer_message` FOREIGN KEY (`message_id`) REFERENCES `qa_message` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_answer_question` FOREIGN KEY (`question_message_id`) REFERENCES `qa_message` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_answer_topic` FOREIGN KEY (`topic_id`) REFERENCES `qa_topic` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='答案表';

-- 2.5 qa_answer_claim - 答案关键结论表
DROP TABLE IF EXISTS `qa_answer_claim`;
CREATE TABLE `qa_answer_claim` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `answer_id` BIGINT UNSIGNED NOT NULL COMMENT '答案ID',
    `claim_no` SMALLINT UNSIGNED NOT NULL COMMENT '结论序号',
    `claim_type` VARCHAR(24) NOT NULL COMMENT '类型: CONCLUSION/LEGAL_BASIS/PROCEDURE/RISK',
    `claim_text` TEXT NOT NULL COMMENT '结论文本',
    `support_status` VARCHAR(16) NOT NULL DEFAULT 'SUPPORTED' COMMENT '支撑状态: SUPPORTED/PARTIAL/UNSUPPORTED',
    `sort_order` SMALLINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '排序号',
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_answer_claim` (`answer_id`, `claim_no`),
    KEY `idx_answer_sort` (`answer_id`, `sort_order`),
    CONSTRAINT `fk_claim_answer` FOREIGN KEY (`answer_id`) REFERENCES `qa_answer` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='答案关键结论表';

-- 2.6 qa_answer_citation - 答案引用表
DROP TABLE IF EXISTS `qa_answer_citation`;
CREATE TABLE `qa_answer_citation` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `claim_id` BIGINT UNSIGNED NOT NULL COMMENT '关键结论ID',
    `document_version_id` BIGINT UNSIGNED NOT NULL COMMENT '文档版本ID',
    `chunk_ref_id` BIGINT UNSIGNED NOT NULL COMMENT '片段引用ID',
    `citation_no` SMALLINT UNSIGNED NOT NULL COMMENT '引用序号',
    `retrieval_rank` SMALLINT UNSIGNED DEFAULT NULL COMMENT '检索排名',
    `relevance_score` DECIMAL(7,6) DEFAULT NULL COMMENT '相关度分数',
    `source_title_snapshot` VARCHAR(255) NOT NULL COMMENT '来源标题快照',
    `source_label_snapshot` VARCHAR(64) NOT NULL COMMENT '来源标签快照',
    `section_snapshot` VARCHAR(255) DEFAULT NULL COMMENT '章节快照',
    `article_no_snapshot` VARCHAR(64) DEFAULT NULL COMMENT '条款号快照',
    `source_url_snapshot` VARCHAR(1024) DEFAULT NULL COMMENT '来源链接快照',
    `quote_snapshot` TEXT NOT NULL COMMENT '引用内容快照',
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_claim_chunk` (`claim_id`, `chunk_ref_id`),
    KEY `idx_citation_doc_version` (`document_version_id`),
    KEY `idx_citation_chunk_ref` (`chunk_ref_id`),
    CONSTRAINT `fk_citation_claim` FOREIGN KEY (`claim_id`) REFERENCES `qa_answer_claim` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='答案引用表';

-- ============================================================
-- 3. 知识库域 (5张表)
-- ============================================================

-- 3.1 file_object - 文件对象表
--
-- ⚠️ 注意事项：
--   sha256 唯一索引用于去重，相同内容的文件只存储一次。
--   入库前应先检查 SHA256 是否存在，存在则复用现有 file_object_id。
--
DROP TABLE IF EXISTS `file_object`;
CREATE TABLE `file_object` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `storage_provider` VARCHAR(16) NOT NULL DEFAULT 'LOCAL' COMMENT '存储类型: LOCAL/MINIO/S3',
    `bucket_name` VARCHAR(128) DEFAULT NULL COMMENT '存储桶名称',
    `object_key` VARCHAR(512) NOT NULL COMMENT '对象键(唯一)',
    `original_filename` VARCHAR(255) NOT NULL COMMENT '原始文件名',
    `content_type` VARCHAR(128) NOT NULL COMMENT '内容类型',
    `file_extension` VARCHAR(16) NOT NULL COMMENT '文件扩展名',
    `size_bytes` BIGINT UNSIGNED NOT NULL COMMENT '文件大小(字节)',
    `sha256` CHAR(64) NOT NULL COMMENT '文件SHA256哈希',
    `status` VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/DELETED',
    `uploaded_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '上传人ID',
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `deleted_at` DATETIME(3) DEFAULT NULL COMMENT '删除时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_object_key` (`object_key`),
    UNIQUE KEY `uk_sha256` (`sha256`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件对象表';

-- 3.2 kb_document - 知识文档表
DROP TABLE IF EXISTS `kb_document`;
CREATE TABLE `kb_document` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `public_id` CHAR(26) NOT NULL COMMENT '对外ULID标识',
    `title` VARCHAR(255) NOT NULL COMMENT '文档标题',
    `document_type` VARCHAR(32) NOT NULL COMMENT '文档类型: LAW/JUDICIAL_INTERPRETATION/POLICY/FAQ/SAMPLE_CONTRACT/USER_CONTRIBUTION',
    `issuing_authority` VARCHAR(255) DEFAULT NULL COMMENT '发布机构',
    `canonical_source_url` VARCHAR(1024) DEFAULT NULL COMMENT '权威来源链接',
    `jurisdiction_code` VARCHAR(32) NOT NULL DEFAULT 'CN' COMMENT '辖区代码',
    `scope_text` VARCHAR(500) DEFAULT NULL COMMENT '适用范围文本',
    `authority_level` SMALLINT UNSIGNED NOT NULL DEFAULT 100 COMMENT '权威等级(数值越小越权威)',
    `current_version_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '当前版本ID(循环外键，第二阶段追加)',
    `status` VARCHAR(16) NOT NULL DEFAULT 'DRAFT' COMMENT '状态: DRAFT/PUBLISHED/DISABLED',
    `created_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人ID',
    `lock_version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_doc_public_id` (`public_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识文档表';

-- 3.3 kb_document_version - 文档版本表
DROP TABLE IF EXISTS `kb_document_version`;
CREATE TABLE `kb_document_version` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `document_id` BIGINT UNSIGNED NOT NULL COMMENT '文档ID',
    `version_no` VARCHAR(32) NOT NULL COMMENT '版本号',
    `file_object_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '关联文件对象ID',
    `document_number` VARCHAR(128) DEFAULT NULL COMMENT '文号',
    `source_url` VARCHAR(1024) DEFAULT NULL COMMENT '来源链接',
    `published_date` DATE DEFAULT NULL COMMENT '发布日期',
    `effective_date` DATE DEFAULT NULL COMMENT '生效日期',
    `expiry_date` DATE DEFAULT NULL COMMENT '失效日期',
    `validity_status` VARCHAR(24) NOT NULL DEFAULT 'DRAFT' COMMENT '效力状态: DRAFT/PENDING/EFFECTIVE/EXPIRED/REPEALED/DISABLED',
    `review_status` VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT '审批状态: PENDING/APPROVED/REJECTED',
    `content_sha256` CHAR(64) NOT NULL COMMENT '内容SHA256哈希',
    `language_code` VARCHAR(16) NOT NULL DEFAULT 'zh-CN' COMMENT '语言代码',
    `created_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人ID',
    `reviewed_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '审批人ID',
    `reviewed_at` DATETIME(3) DEFAULT NULL COMMENT '审批时间',
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_doc_version` (`document_id`, `version_no`),
    UNIQUE KEY `uk_doc_content` (`document_id`, `content_sha256`),
    UNIQUE KEY `uk_version_id_doc` (`id`, `document_id`),
    KEY `idx_version_file` (`file_object_id`),
    KEY `idx_version_validity` (`validity_status`),
    KEY `idx_version_review` (`review_status`),
    CONSTRAINT `fk_version_document` FOREIGN KEY (`document_id`) REFERENCES `kb_document` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_version_file` FOREIGN KEY (`file_object_id`) REFERENCES `file_object` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文档版本表';

-- 3.4 kb_ingestion_job - 入库任务表
DROP TABLE IF EXISTS `kb_ingestion_job`;
CREATE TABLE `kb_ingestion_job` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `job_key` CHAR(26) NOT NULL COMMENT '任务唯一标识(ULID)',
    `document_version_id` BIGINT UNSIGNED NOT NULL COMMENT '文档版本ID',
    `job_type` VARCHAR(16) NOT NULL COMMENT '任务类型: INGEST/REINDEX/DELETE_INDEX',
    `status` VARCHAR(16) NOT NULL DEFAULT 'QUEUED' COMMENT '状态: QUEUED/RUNNING/SUCCEEDED/FAILED/CANCELED',
    `attempt_no` SMALLINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '尝试次数',
    `idempotency_key` VARCHAR(128) NOT NULL COMMENT '幂等键',
    `parser_name` VARCHAR(64) NOT NULL COMMENT '解析器名称',
    `chunk_strategy` VARCHAR(32) NOT NULL COMMENT '分片策略',
    `chunk_size` INT UNSIGNED DEFAULT NULL COMMENT '分片大小',
    `chunk_overlap` INT UNSIGNED DEFAULT NULL COMMENT '分片重叠量',
    `es_index_name` VARCHAR(128) NOT NULL COMMENT 'Elasticsearch索引名称',
    `chunks_total` INT NOT NULL DEFAULT 0 COMMENT '总分片数',
    `chunks_indexed` INT NOT NULL DEFAULT 0 COMMENT '已索引分片数',
    `started_at` DATETIME(3) DEFAULT NULL COMMENT '开始时间',
    `finished_at` DATETIME(3) DEFAULT NULL COMMENT '完成时间',
    `error_code` VARCHAR(64) DEFAULT NULL COMMENT '错误代码',
    `error_message` VARCHAR(1000) DEFAULT NULL COMMENT '错误信息',
    `created_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人ID',
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_job_key` (`job_key`),
    UNIQUE KEY `uk_idempotency_key` (`idempotency_key`),
    UNIQUE KEY `uk_job_id_version` (`id`, `document_version_id`),
    KEY `idx_job_version` (`document_version_id`),
    KEY `idx_job_status_created` (`status`, `created_at`),
    CONSTRAINT `fk_job_version` FOREIGN KEY (`document_version_id`) REFERENCES `kb_document_version` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='入库任务表';

-- 3.5 kb_chunk_ref - 片段引用表
DROP TABLE IF EXISTS `kb_chunk_ref`;
CREATE TABLE `kb_chunk_ref` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `document_version_id` BIGINT UNSIGNED NOT NULL COMMENT '文档版本ID',
    `ingestion_job_id` BIGINT UNSIGNED NOT NULL COMMENT '入库任务ID',
    `chunk_key` VARCHAR(128) NOT NULL COMMENT '片段唯一键',
    `chunk_no` INT UNSIGNED NOT NULL COMMENT '片段序号',
    `section_path` VARCHAR(512) DEFAULT NULL COMMENT '章节路径',
    `article_no` VARCHAR(64) DEFAULT NULL COMMENT '条款号',
    `paragraph_no` VARCHAR(64) DEFAULT NULL COMMENT '段落号',
    `content_sha256` CHAR(64) NOT NULL COMMENT '内容SHA256哈希',
    `token_count` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Token数量',
    `es_index_name` VARCHAR(128) NOT NULL COMMENT 'Elasticsearch索引名称',
    `es_document_id` VARCHAR(128) NOT NULL COMMENT 'Elasticsearch文档ID',
    `status` VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/SUPERSEDED/DELETED',
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_version_chunk` (`document_version_id`, `chunk_key`),
    UNIQUE KEY `uk_es_doc` (`es_index_name`, `es_document_id`),
    UNIQUE KEY `uk_chunk_id_version` (`id`, `document_version_id`),
    KEY `idx_chunk_job` (`ingestion_job_id`),
    CONSTRAINT `fk_chunk_version` FOREIGN KEY (`document_version_id`) REFERENCES `kb_document_version` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_chunk_job` FOREIGN KEY (`ingestion_job_id`, `document_version_id`) REFERENCES `kb_ingestion_job` (`id`, `document_version_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='片段引用表';

-- ============================================================
-- 4. 反馈与质量域 (3张表)
-- ============================================================

-- 4.1 qa_feedback - 问答反馈表
--
-- ⚠️ 注意事项：
--   uk_feedback_answer_session 唯一索引允许 answer_id 或 session_id 为 NULL。
--   MySQL 中唯一索引对 NULL 值不强制唯一性（多个 NULL 不冲突）。
--   应用层 UPSERT 逻辑需特殊处理：插入前先查询是否存在 NULL 记录，
--   或业务层保证 answer_id 和 session_id 都不为 NULL。
--
DROP TABLE IF EXISTS `qa_feedback`;
CREATE TABLE `qa_feedback` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `answer_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '答案ID',
    `session_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '会话ID',
    `submitted_by_account_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '提交人账号ID',
    `rating` SMALLINT NOT NULL COMMENT '评分: 1(点赞)/-1(点踩)',
    `issue_type` VARCHAR(32) DEFAULT NULL COMMENT '问题类型: INACCURATE/CITATION/UNCLEAR/SAFETY/OTHER',
    `comment_redacted` TEXT DEFAULT NULL COMMENT '脱敏后的反馈内容（TEXT 支持长文本）',
    `status` VARCHAR(16) NOT NULL DEFAULT 'OPEN' COMMENT '状态: OPEN/IN_PROGRESS/RESOLVED/IGNORED',
    `knowledge_disposition` VARCHAR(16) NOT NULL DEFAULT 'NONE' COMMENT '入库处置: NONE/PROPOSED/APPROVED/PUBLISHED/REJECTED',
    `retention_hold` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '保留标记(已发布为用户补充参考时设为true)',
    `promoted_at` DATETIME(3) DEFAULT NULL COMMENT '提升为知识的时间',
    `assigned_to` BIGINT UNSIGNED DEFAULT NULL COMMENT '指派处理人ID',
    `resolved_at` DATETIME(3) DEFAULT NULL COMMENT '解决时间',
    `lock_version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_feedback_answer_session` (`answer_id`, `session_id`),
    KEY `idx_feedback_status_created` (`status`, `created_at`),
    KEY `idx_feedback_assigned` (`assigned_to`),
    CONSTRAINT `fk_feedback_answer` FOREIGN KEY (`answer_id`) REFERENCES `qa_answer` (`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_feedback_session` FOREIGN KEY (`session_id`) REFERENCES `qa_session` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问答反馈表';

-- 4.2 qa_feedback_action - 反馈处理动作表
DROP TABLE IF EXISTS `qa_feedback_action`;
CREATE TABLE `qa_feedback_action` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `feedback_id` BIGINT UNSIGNED NOT NULL COMMENT '反馈ID',
    `operator_id` BIGINT UNSIGNED NOT NULL COMMENT '操作人ID',
    `action_type` VARCHAR(32) NOT NULL COMMENT '动作类型: COMMENT/STATUS_CHANGE/KB_FIX/PROMPT_FIX/KNOWLEDGE_APPROVED/KNOWLEDGE_PUBLISHED',
    `from_status` VARCHAR(16) DEFAULT NULL COMMENT '变更前状态',
    `to_status` VARCHAR(16) DEFAULT NULL COMMENT '变更后状态',
    `action_note` VARCHAR(1000) DEFAULT NULL COMMENT '操作备注',
    `document_version_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '关联文档版本ID',
    `prompt_version_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '关联提示词版本ID',
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_action_feedback` (`feedback_id`),
    CONSTRAINT `fk_action_feedback` FOREIGN KEY (`feedback_id`) REFERENCES `qa_feedback` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='反馈处理动作表';

-- 4.3 qa_feedback_knowledge_link - 反馈知识来源关联表
DROP TABLE IF EXISTS `qa_feedback_knowledge_link`;
CREATE TABLE `qa_feedback_knowledge_link` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `feedback_id` BIGINT UNSIGNED NOT NULL COMMENT '反馈ID',
    `feedback_action_id` BIGINT UNSIGNED NOT NULL COMMENT '反馈动作ID(必须为KNOWLEDGE_PUBLISHED)',
    `document_version_id` BIGINT UNSIGNED NOT NULL COMMENT '文档版本ID',
    `chunk_ref_id` BIGINT UNSIGNED NOT NULL COMMENT '片段引用ID',
    `created_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人ID',
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_link_feedback_chunk` (`feedback_id`, `chunk_ref_id`),
    KEY `idx_link_chunk_feedback` (`chunk_ref_id`, `feedback_id`),
    KEY `idx_link_doc_feedback` (`document_version_id`, `feedback_id`),
    CONSTRAINT `fk_link_feedback` FOREIGN KEY (`feedback_id`) REFERENCES `qa_feedback` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_link_action` FOREIGN KEY (`feedback_action_id`) REFERENCES `qa_feedback_action` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_link_chunk` FOREIGN KEY (`chunk_ref_id`, `document_version_id`) REFERENCES `kb_chunk_ref` (`id`, `document_version_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='反馈知识来源关联表';

-- ============================================================
-- 5. AI运行与审计域 (5张表)
-- ============================================================

-- 5.1 ai_prompt - 提示词表
DROP TABLE IF EXISTS `ai_prompt`;
CREATE TABLE `ai_prompt` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `prompt_code` VARCHAR(64) NOT NULL COMMENT '提示词编码',
    `prompt_name` VARCHAR(128) NOT NULL COMMENT '提示词名称',
    `purpose` VARCHAR(255) NOT NULL COMMENT '用途说明',
    `status` VARCHAR(16) NOT NULL DEFAULT 'DRAFT' COMMENT '状态: DRAFT/ACTIVE/RETIRED',
    `current_version_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '当前版本ID(循环外键，第二阶段追加)',
    `created_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人ID',
    `lock_version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_prompt_code` (`prompt_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='提示词表';

-- 5.2 ai_prompt_version - 提示词版本表
DROP TABLE IF EXISTS `ai_prompt_version`;
CREATE TABLE `ai_prompt_version` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `prompt_id` BIGINT UNSIGNED NOT NULL COMMENT '提示词ID',
    `version_no` VARCHAR(32) NOT NULL COMMENT '版本号',
    `template_text` MEDIUMTEXT NOT NULL COMMENT '模板文本',
    `variables_schema` JSON NOT NULL COMMENT '变量Schema(JSON)',
    `model_parameters` JSON DEFAULT NULL COMMENT '模型参数(JSON)',
    `content_sha256` CHAR(64) NOT NULL COMMENT '内容SHA256哈希',
    `status` VARCHAR(16) NOT NULL DEFAULT 'DRAFT' COMMENT '状态: DRAFT/ACTIVE/RETIRED',
    `created_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人ID',
    `approved_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '审批人ID',
    `approved_at` DATETIME(3) DEFAULT NULL COMMENT '审批时间',
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_prompt_version` (`prompt_id`, `version_no`),
    UNIQUE KEY `uk_prompt_content` (`prompt_id`, `content_sha256`),
    UNIQUE KEY `uk_pv_id_prompt` (`id`, `prompt_id`),
    CONSTRAINT `fk_pv_prompt` FOREIGN KEY (`prompt_id`) REFERENCES `ai_prompt` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='提示词版本表';

-- 5.3 ai_generation_run - AI生成运行表
DROP TABLE IF EXISTS `ai_generation_run`;
CREATE TABLE `ai_generation_run` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `run_key` CHAR(26) NOT NULL COMMENT '运行唯一标识(ULID)',
    `session_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '会话ID',
    `question_message_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '问题消息ID',
    `answer_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '答案ID',
    `prompt_version_id` BIGINT UNSIGNED NOT NULL COMMENT '提示词版本ID',
    `model_provider` VARCHAR(32) NOT NULL COMMENT '模型提供商',
    `model_name` VARCHAR(128) NOT NULL COMMENT '模型名称',
    `status` VARCHAR(16) NOT NULL DEFAULT 'RUNNING' COMMENT '状态: RUNNING/SUCCEEDED/FAILED/CANCELED',
    `retrieval_trace` JSON DEFAULT NULL COMMENT '检索追踪(片段ID、排序、分数)',
    `retrieved_chunk_count` INT NOT NULL DEFAULT 0 COMMENT '检索到的片段数量',
    `input_tokens` INT NOT NULL DEFAULT 0 COMMENT '输入Token数',
    `output_tokens` INT NOT NULL DEFAULT 0 COMMENT '输出Token数',
    `latency_ms` INT NOT NULL DEFAULT 0 COMMENT '延迟(毫秒)',
    `started_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '开始时间',
    `finished_at` DATETIME(3) DEFAULT NULL COMMENT '完成时间',
    `error_code` VARCHAR(64) DEFAULT NULL COMMENT '错误代码',
    `error_message` VARCHAR(1000) DEFAULT NULL COMMENT '错误信息',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_run_key` (`run_key`),
    KEY `idx_run_session` (`session_id`),
    KEY `idx_run_question` (`question_message_id`),
    KEY `idx_run_answer` (`answer_id`),
    KEY `idx_run_prompt` (`prompt_version_id`),
    KEY `idx_run_status` (`status`),
    CONSTRAINT `fk_run_session` FOREIGN KEY (`session_id`) REFERENCES `qa_session` (`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_run_question` FOREIGN KEY (`question_message_id`) REFERENCES `qa_message` (`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_run_answer` FOREIGN KEY (`answer_id`) REFERENCES `qa_answer` (`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_run_prompt` FOREIGN KEY (`prompt_version_id`) REFERENCES `ai_prompt_version` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI生成运行表';

-- 5.4 sys_audit_log - 审计日志表
DROP TABLE IF EXISTS `sys_audit_log`;
CREATE TABLE `sys_audit_log` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `trace_id` CHAR(26) NOT NULL COMMENT '追踪ID(ULID)',
    `actor_account_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '操作人账号ID',
    `action_code` VARCHAR(128) NOT NULL COMMENT '操作编码',
    `target_type` VARCHAR(64) NOT NULL COMMENT '目标类型',
    `target_id` VARCHAR(64) DEFAULT NULL COMMENT '目标ID',
    `request_method` VARCHAR(8) DEFAULT NULL COMMENT '请求方法',
    `request_path` VARCHAR(255) DEFAULT NULL COMMENT '请求路径',
    `result` VARCHAR(16) NOT NULL COMMENT '结果: SUCCESS/FAILURE',
    `ip_hash` CHAR(64) DEFAULT NULL COMMENT 'IP哈希',
    `before_data` JSON DEFAULT NULL COMMENT '变更前数据(已脱敏)',
    `after_data` JSON DEFAULT NULL COMMENT '变更后数据(已脱敏)',
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_audit_actor_created` (`actor_account_id`, `created_at`),
    KEY `idx_audit_action_created` (`action_code`, `created_at`),
    KEY `idx_audit_target` (`target_type`, `target_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审计日志表';

-- 5.5 sys_purge_log - 清理日志表
DROP TABLE IF EXISTS `sys_purge_log`;
CREATE TABLE `sys_purge_log` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `batch_key` CHAR(26) NOT NULL COMMENT '批次标识(ULID)',
    `retention_type` VARCHAR(32) NOT NULL COMMENT '保留类型',
    `cutoff_at` DATETIME(3) NOT NULL COMMENT '截止时间',
    `status` VARCHAR(16) NOT NULL DEFAULT 'RUNNING' COMMENT '状态: RUNNING/SUCCEEDED/FAILED',
    `deleted_counts` JSON NOT NULL COMMENT '各表删除行数',
    `started_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '开始时间',
    `finished_at` DATETIME(3) DEFAULT NULL COMMENT '完成时间',
    `error_message` VARCHAR(1000) DEFAULT NULL COMMENT '错误信息',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_batch_key` (`batch_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='清理日志表';


-- ============================================================
-- 第二阶段：追加循环外键和复合外键
-- ============================================================

-- 6.1 kb_document.current_version_id 引用 kb_document_version
ALTER TABLE `kb_document`
    ADD CONSTRAINT `fk_doc_current_version`
    FOREIGN KEY (`current_version_id`, `id`)
    REFERENCES `kb_document_version` (`id`, `document_id`)
    ON DELETE SET NULL;

-- 6.2 ai_prompt.current_version_id 引用 ai_prompt_version
ALTER TABLE `ai_prompt`
    ADD CONSTRAINT `fk_prompt_current_version`
    FOREIGN KEY (`current_version_id`, `id`)
    REFERENCES `ai_prompt_version` (`id`, `prompt_id`)
    ON DELETE SET NULL;

-- 6.3 qa_answer_citation 复合外键
ALTER TABLE `qa_answer_citation`
    ADD CONSTRAINT `fk_citation_chunk`
    FOREIGN KEY (`chunk_ref_id`, `document_version_id`)
    REFERENCES `kb_chunk_ref` (`id`, `document_version_id`)
    ON DELETE RESTRICT;


-- ============================================================
-- 第三阶段：初始化种子数据
-- ============================================================

-- 7.1 初始化内置角色
INSERT INTO `sys_role` (`role_code`, `role_name`, `description`, `is_builtin`, `status`) VALUES
    ('ADMIN', '管理员', '系统管理员，拥有所有权限', TRUE, 'ACTIVE'),
    ('REVIEWER', '评审员', '只读评审，可查看系统说明和测试问答', TRUE, 'ACTIVE');

-- 7.2 初始化问题分类
INSERT INTO `qa_topic` (`topic_code`, `topic_name`, `answer_strategy`, `sort_order`, `enabled`, `description`) VALUES
    ('CONTRACT_SIGN', '合同签订', 'STANDARD', 1, TRUE, '劳动合同签订相关咨询'),
    ('PROBATION', '试用期', 'STANDARD', 2, TRUE, '试用期相关规定咨询'),
    ('WAGE_PAYMENT', '工资支付', 'STANDARD', 3, TRUE, '工资支付相关咨询'),
    ('OVERTIME', '加班工资', 'STANDARD', 4, TRUE, '加班工资计算相关咨询'),
    ('REST_LEAVE', '休息休假', 'STANDARD', 5, TRUE, '休息休假相关咨询'),
    ('SOCIAL_SECURITY', '社保', 'STANDARD', 6, TRUE, '社会保险相关咨询'),
    ('TERMINATION', '解除/终止劳动合同', 'STANDARD', 7, TRUE, '劳动合同解除与终止咨询'),
    ('ECONOMIC_COMPENSATION', '经济补偿', 'STANDARD', 8, TRUE, '经济补偿金相关咨询'),
    ('LABOR_ARBITRATION', '劳动仲裁', 'PROCESS', 9, TRUE, '劳动仲裁流程咨询'),
    ('EVIDENCE_PREPARATION', '证据准备', 'PROCESS', 10, TRUE, '维权证据准备咨询');

-- 7.3 初始化默认提示词
INSERT INTO `ai_prompt` (`prompt_code`, `prompt_name`, `purpose`, `status`) VALUES
    ('LABOR_LAW_QA', '劳动合同法律问答', '劳动合同法律问题智能问答', 'DRAFT');

-- 7.4 初始化提示词版本
SET @prompt_id = (SELECT `id` FROM `ai_prompt` WHERE `prompt_code` = 'LABOR_LAW_QA');

INSERT INTO `ai_prompt_version` (
    `prompt_id`, `version_no`, `template_text`, `variables_schema`,
    `model_parameters`, `content_sha256`, `status`
) VALUES (
    @prompt_id,
    'v1.0',
    '你是一位专业的劳动合同法律助手。请根据以下检索到的法律依据，回答用户的问题。

## 检索到的法律依据：
{{context}}

## 用户问题：
{{question}}

## 回答要求：
请严格按照以下结构回答，每个部分都要有实质内容：

### 1. 结论摘要
用1-2句话给出简明判断。如果依据不足，请明确说明。

### 2. 法律依据
列出相关的法律法规、司法解释或政策文件，注明具体条款号。格式：[法规名称] 第X条。

### 3. 维权流程
按时间顺序列出具体操作步骤：
- 第一步：与用人单位协商沟通
- 第二步：保存相关证据
- 第三步：向劳动保障监察部门投诉举报
- 第四步：申请劳动仲裁
- 第五步：向人民法院提起诉讼（如需要）

### 4. 证据清单
列出需要准备的材料，包括但不限于：劳动合同、工资流水、考勤记录、聊天记录、加班通知、工作成果、社保缴纳记录等。

### 5. 注意事项
提示：仲裁时效（劳动争议一般为1年）、证据真实性要求、地方政策差异、复杂案件建议咨询专业律师。

## 重要限制：
1. 如果检索内容不足以支撑确定性结论，必须在结论摘要中明确告知用户"依据不足，建议咨询专业律师"
2. 不得编造法律条款或给出确定性胜诉承诺
3. 用户补充参考（如有）仅供辅助参考，不得作为法律结论的唯一依据

请开始回答：',
    '{"question": {"type": "string", "description": "用户提出的法律问题"}, "context": {"type": "string", "description": "从知识库检索到的相关法律依据片段，按相关性排序"}}',
    '{"temperature": 0.3, "max_tokens": 2000, "top_p": 0.9, "frequency_penalty": 0.1, "presence_penalty": 0.1}',
    SHA2('v1.0提示词模板', 256),
    'ACTIVE'
);

-- 更新 ai_prompt.current_version_id
UPDATE `ai_prompt`
SET `current_version_id` = (SELECT `id` FROM `ai_prompt_version` WHERE `prompt_id` = @prompt_id AND `status` = 'ACTIVE')
WHERE `id` = @prompt_id;

-- 7.5 创建默认管理员账号
-- 密码: admin123 (BCrypt 加密，真实可用)
-- $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM7lE9lB6z5y8Z8Z8Z8Z8
INSERT INTO `sys_account` (
    `username`, `password_hash`, `display_name`, `email`, `status`, `password_changed_at`
) VALUES (
    'admin',
    '$2a$10$Mtu9XHdc/wAOK3APYyiDuefM0TdEcPkzF0Fbxa5C3Zc5vV4NbRtmC',
    '系统管理员',
    'admin@laborlaw.local',
    'ACTIVE',
    CURRENT_TIMESTAMP(3)
);

-- 7.6 关联管理员角色
INSERT INTO `sys_account_role` (`account_id`, `role_id`)
SELECT
    (SELECT `id` FROM `sys_account` WHERE `username` = 'admin'),
    (SELECT `id` FROM `sys_role` WHERE `role_code` = 'ADMIN');

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 脚本执行完成
-- ============================================================
--
-- 表清单（25张）：
--   1. sys_account          2. sys_role           3. sys_permission
--   4. sys_menu             5. sys_account_role   6. sys_role_permission
--   7. qa_topic             8. qa_session         9. qa_message
--   10. qa_answer           11. qa_answer_claim   12. qa_answer_citation
--   13. file_object         14. kb_document       15. kb_document_version
--   16. kb_ingestion_job    17. kb_chunk_ref
--   18. qa_feedback         19. qa_feedback_action 20. qa_feedback_knowledge_link
--   21. ai_prompt           22. ai_prompt_version 23. ai_generation_run
--   24. sys_audit_log       25. sys_purge_log
-- ============================================================