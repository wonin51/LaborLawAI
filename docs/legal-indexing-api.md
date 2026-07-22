# 法律文档知识索引接口文档

本文档总结这几次改动的接口、配置和最终功能。

## 1. 改动总览

1. Elasticsearch 索引初始化
2. 法律文档切分为知识分片
3. 单条法律文档生成知识索引
4. 法律文档前端增加“生成知识索引”
5. AI / ES 连通性检查接口

## 2. 接口文档

### 2.1 初始化 ES 索引

`POST /api/es/init-index`

参数:
- `index_name`，可选，默认 `legal_chunk_index_g5`

说明:
- 索引已存在时，直接返回“索引已存在”
- 不存在时创建索引并写入 dense_vector mapping
- `content_vector` 维度来自 `rag.ai.embedding.dimension`，上限 2048
- 向量相似度使用 `cosine`

返回示例:
```json
{
  "code": 0,
  "message": "success",
  "data": "索引创建成功"
}
```

### 2.2 单条法律文档生成知识索引

`POST /api/legal-documents/{id}/index`

路径参数:
- `id`：法律文档 ID

业务流程:
1. 查询 `legal_document`
2. 如果 `status=disabled`，拒绝索引
3. 将 `raw_text` 切分为知识分片
4. 写入 `legal_chunk`
5. 调用 embedding
6. 写入 Elasticsearch 的 `legal_chunk_index_g5`
7. 更新 chunk 和 document 状态

返回字段:
- `document_id`
- `chunk_count`
- `indexed_count`
- `failed_count`
- `status`

返回示例:
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "document_id": 7,
    "chunk_count": 3,
    "indexed_count": 3,
    "failed_count": 0,
    "status": "indexed"
  }
}
```

### 2.3 AI 连通性检查

`GET /api/ai/ping`

返回三项:
- `elasticsearch`
- `embedding`
- `chat`

用途:
- 确认 ES、向量模型、对话模型是否可用

## 3. 配置说明

### 3.1 后端必需环境变量

- `RAG_AI_ELASTICSEARCH_URL`
  - 需要带上实际 ES 路径
  - 当前默认示例: `http://server1.shanci.tech:60080/es/`
- `RAG_AI_ELASTICSEARCH_USERNAME`
- `RAG_AI_ELASTICSEARCH_PASSWORD`
- `RAG_AI_EMBEDDING_BASE_URL`
  - 默认示例: `http://localhost:11434/v1`
- `RAG_AI_EMBEDDING_API_KEY`
- `RAG_AI_EMBEDDING_MODEL`
  - 默认示例: `qwen3-embedding:4b`
- `RAG_AI_EMBEDDING_DIMENSION`
  - 默认示例: `2560`
  - 运行时写 ES 前会截断到 2048
- `RAG_AI_CHAT_BASE_URL`
- `RAG_AI_CHAT_API_KEY`
- `RAG_AI_CHAT_MODEL`
  - 默认示例: `qwen2.5:7b`
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`

### 3.2 本地开发入口

- ES 索引默认名: `legal_chunk_index_g5`
- 管理员账号配置: `ADMIN_ACCOUNTS`
- 浏览器直连管理接口时使用: `VITE_ADMIN_API_TOKEN`

### 3.3 数据库

需要保证存在这两张表:
- `legal_document`
- `legal_chunk`

## 4. 四次改动后的功能结果

1. 能初始化法律知识分片 ES 索引
2. 能按条款号、章节标题、自然段切分法律正文
3. 能对单条法律文档生成知识索引并写入 ES
4. 法律文档列表页可直接点击“生成知识索引”
5. 成功后弹窗展示索引结果
6. 前端不展示向量内容
7. 后端和前端均有对应测试

## 5. 拉取后怎么跑

1. 配好后端环境变量
2. 确认 MySQL 表结构已导入
3. 确认 ES、embedding、chat 可访问
4. 启动后端，默认端口 `8080`
5. 启动前端，调用法律文档列表页进行验证

