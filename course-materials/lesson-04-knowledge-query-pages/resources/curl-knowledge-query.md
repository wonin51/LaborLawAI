# 知识库四张表分页查询接口 curl 测试命令

假设后端服务已启动在：`http://localhost:8080`

> 说明：以下接口均为只读 `GET` 查询接口，统一返回 `ApiResponse`，其中 `code=0` 表示成功。分页参数统一为 `page_no` 和 `page_size`。

## 1. GET /api/knowledge/documents —— 知识文档分页查询

### 基础分页查询

```bash
curl -X GET "http://localhost:8080/api/knowledge/documents?page_no=1&page_size=10"
```

### 带条件筛选查询

支持筛选条件：

- `title`：标题模糊匹配
- `document_type`：文档类型精确匹配
- `status`：状态精确匹配

```bash
curl -X GET "http://localhost:8080/api/knowledge/documents?title=%E5%8A%B3%E5%8A%A8%E5%90%88%E5%90%8C&document_type=LAW&status=PUBLISHED&page_no=1&page_size=10"
```

等价的可读参数含义：

```text
title=劳动合同
document_type=LAW
status=PUBLISHED
page_no=1
page_size=10
```

## 2. GET /api/knowledge/chunk-refs —— 知识分片分页查询

### 基础分页查询

```bash
curl -X GET "http://localhost:8080/api/knowledge/chunk-refs?page_no=1&page_size=10"
```

### 带条件筛选查询

支持筛选条件：

- `document_version_id`：文档版本 ID 精确匹配
- `status`：状态精确匹配
- `chunk_key`：分片键模糊匹配
- `section_path`：章节路径模糊匹配

```bash
curl -X GET "http://localhost:8080/api/knowledge/chunk-refs?document_version_id=1&status=ACTIVE&chunk_key=law&section_path=%E6%80%BB%E5%88%99&page_no=1&page_size=10"
```

等价的可读参数含义：

```text
document_version_id=1
status=ACTIVE
chunk_key=law
section_path=总则
page_no=1
page_size=10
```

## 3. GET /api/qa/records —— 问答记录分页查询

### 基础分页查询

```bash
curl -X GET "http://localhost:8080/api/qa/records?page_no=1&page_size=10"
```

### 带条件筛选查询

支持筛选条件：

- `answer_status`：回答状态精确匹配
- `topic_id`：主题 ID 精确匹配

返回 VO 中包含 `question` 字段，该字段由后端通过 `qa_answer.question_message_id` 关联 `qa_message.content_redacted` 组装得到。

```bash
curl -X GET "http://localhost:8080/api/qa/records?answer_status=COMPLETED&topic_id=1&page_no=1&page_size=10"
```

## 4. GET /api/qa/citations —— 回答引用依据分页查询

### 基础分页查询

```bash
curl -X GET "http://localhost:8080/api/qa/citations?page_no=1&page_size=10"
```

### 带条件筛选查询

支持筛选条件：

- `claim_id`：回答主张 ID 精确匹配
- `document_version_id`：文档版本 ID 精确匹配
- `chunk_ref_id`：知识分片 ID 精确匹配

```bash
curl -X GET "http://localhost:8080/api/qa/citations?claim_id=1&document_version_id=1&chunk_ref_id=1&page_no=1&page_size=10"
```

## 常用分页变体

查询第 2 页，每页 20 条：

```bash
curl -X GET "http://localhost:8080/api/knowledge/documents?page_no=2&page_size=20"
```

注意：后端会将 `page_size` 上限限制为 100。
