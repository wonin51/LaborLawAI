# 知识库文档模块 curl 测试命令

## 前置说明

默认服务地址：

```text
http://127.0.0.1:8080
```

启动后端服务：

```powershell
cd G:\AI-law-creater\backend
mvn spring-boot:run "-Dspring-boot.run.profiles=local"
```

详情、更新、状态修改、HEAD、单资源 OPTIONS、删除接口都需要实际文档 ID。建议先执行“3. 新增知识库文档”，从返回结果中复制：

```json
{
  "data": {
    "id": 1
  }
}
```

然后将后续命令中的 `1` 替换为实际 `data.id`，例如替换为 `1`。

> Windows PowerShell 中如果 `curl` 被映射为 `Invoke-WebRequest`，建议使用 `curl.exe` 执行以下命令。

## 1. 健康检查

### 验证目的

验证后端服务是否已经启动，且 `/api/health` 能返回统一 `ApiResponse` 格式。

### curl 命令

```bash
curl "http://127.0.0.1:8080/api/health"
```

### 预期结果简述

返回 `code=0`，`message=success`，`data.status=ok`。

## 2. 数据库连通性检查

### 验证目的

验证后端能否连接 MySQL，并能返回数据库当前时间。

### curl 命令

```bash
curl "http://127.0.0.1:8080/api/db/ping"
```

### 预期结果简述

数据库连接正常时返回 `code=0`，`data.status=ok`，并包含 `data.timestamp`。

## 3. 新增知识库文档

### 验证目的

验证 `POST /api/admin/knowledge/documents` 可以新增 D01 示例文档元数据，并自动生成 `public_id`。

### curl 命令

```bash
curl -X POST "http://127.0.0.1:8080/api/admin/knowledge/documents" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "中华人民共和国劳动合同法",
    "document_type": "LAW",
    "issuing_authority": "全国人民代表大会常务委员会",
    "canonical_source_url": "https://www.gov.cn/flfg/2007-06/29/content_693995.htm",
    "jurisdiction_code": "CN",
    "scope_text": "劳动合同订立、履行、变更、解除和终止",
    "authority_level": 10,
    "status": "DRAFT"
  }'
```

### 预期结果简述

返回 `code=0`，`data.id` 为新增文档主键，`data.public_id` 为自动生成的 26 位大写字母数字 ID，`data.title=中华人民共和国劳动合同法`。

## 4. 分页查询知识库文档

### 验证目的

验证 `GET /api/admin/knowledge/documents` 支持分页查询和以下查询条件：

- `title` 模糊查询
- `document_type` 精确查询
- `status` 精确查询
- `issuing_authority` 模糊查询
- `jurisdiction_code` 精确查询
- `pageNo`
- `pageSize`

### curl 命令

```bash
curl "http://127.0.0.1:8080/api/admin/knowledge/documents?title=%E5%8A%B3%E5%8A%A8%E5%90%88%E5%90%8C&document_type=LAW&status=DRAFT&issuing_authority=%E5%85%A8%E5%9B%BD%E4%BA%BA%E6%B0%91%E4%BB%A3%E8%A1%A8%E5%A4%A7%E4%BC%9A%E5%B8%B8%E5%8A%A1%E5%A7%94%E5%91%98%E4%BC%9A&jurisdiction_code=CN&pageNo=1&pageSize=10"
```

### 预期结果简述

返回 `code=0`，`data.records` 为文档列表，`data.total` 为总数，`data.pageNo=1`，`data.pageSize=10`。

## 5. 查看知识库文档详情

### 验证目的

验证 `GET /api/admin/knowledge/documents/1` 可以根据主键查看文档详情。

### curl 命令

执行前请先将 `1` 替换为新增接口返回的实际 `data.id`。

```bash
curl "http://127.0.0.1:8080/api/admin/knowledge/documents/1"
```

### 预期结果简述

文档存在时返回 `code=0`，`data.id` 等于请求中的实际 ID，并返回标题、文档类型、发布机构、状态等元数据。

如果文档不存在，返回 `code=404`，`message=Knowledge document not found`。

## 6. 更新知识库文档

### 验证目的

验证 `PUT /api/admin/knowledge/documents/1` 可以更新文档元数据。

### curl 命令

执行前请先将 `1` 替换为新增接口返回的实际 `data.id`。

```bash
curl -X PUT "http://127.0.0.1:8080/api/admin/knowledge/documents/1" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "中华人民共和国劳动合同法",
    "document_type": "LAW",
    "issuing_authority": "全国人民代表大会常务委员会",
    "canonical_source_url": "https://www.gov.cn/flfg/2007-06/29/content_693995.htm",
    "jurisdiction_code": "CN",
    "scope_text": "劳动合同订立、履行、变更、解除和终止",
    "authority_level": 10,
    "status": "DRAFT"
  }'
```

### 预期结果简述

文档存在时返回 `code=0`，`data.id` 等于请求中的实际 ID，并返回更新后的元数据。

如果文档不存在，返回 `code=404`，`message=Knowledge document not found`。

## 7. 更新知识库文档状态

### 验证目的

验证 `PATCH /api/admin/knowledge/documents/1/status` 可以只更新文档 `status` 字段。

### curl 命令

执行前请先将 `1` 替换为新增接口返回的实际 `data.id`。

```bash
curl -X PATCH "http://127.0.0.1:8080/api/admin/knowledge/documents/1/status" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "PUBLISHED"
  }'
```

### 预期结果简述

文档存在时返回 `code=0`，且 `data.status=PUBLISHED`。

如果文档不存在，返回 `code=404`，`message=Knowledge document not found`。

## 8. HEAD 检查知识库文档是否存在

### 验证目的

验证 `HEAD /api/admin/knowledge/documents/1` 是否按 HTTP 语义只返回响应头，不返回响应体。

### curl 命令

执行前请先将 `1` 替换为新增接口返回的实际 `data.id`。

```bash
curl -I "http://127.0.0.1:8080/api/admin/knowledge/documents/1"
```

### 预期结果简述

文档存在时返回 HTTP `200`；文档不存在时返回 HTTP `404`。

## 9. OPTIONS 查询集合资源支持的方法

### 验证目的

验证 `OPTIONS /api/admin/knowledge/documents` 可以返回集合资源支持的请求方法，并设置 `Allow` 响应头。

### curl 命令

```bash
curl -i -X OPTIONS "http://127.0.0.1:8080/api/admin/knowledge/documents"
```

### 预期结果简述

响应头包含：

```text
Allow: POST,GET,OPTIONS
```

响应体返回 `code=0`，`data.methods` 包含 `POST`、`GET`、`OPTIONS`。

## 10. OPTIONS 查询单个资源支持的方法

### 验证目的

验证 `OPTIONS /api/admin/knowledge/documents/1` 可以返回单个文档资源支持的请求方法，并设置 `Allow` 响应头。

### curl 命令

执行前请先将 `1` 替换为新增接口返回的实际 `data.id`。

```bash
curl -i -X OPTIONS "http://127.0.0.1:8080/api/admin/knowledge/documents/1"
```

### 预期结果简述

响应头包含：

```text
Allow: GET,PUT,DELETE,HEAD,PATCH,OPTIONS
```

响应体返回 `code=0`，`data.methods` 包含 `GET`、`PUT`、`DELETE`、`HEAD`、`PATCH`、`OPTIONS`。

## 11. 删除知识库文档

### 验证目的

验证 `DELETE /api/admin/knowledge/documents/1` 可以删除文档元数据。

### curl 命令

执行前请先将 `1` 替换为新增接口返回的实际 `data.id`。

```bash
curl -X DELETE "http://127.0.0.1:8080/api/admin/knowledge/documents/1"
```

### 预期结果简述

删除成功时返回 `code=0`，`data=true`。

如果文档不存在，返回 `code=404`，`message=Knowledge document not found`。

如果存在外键约束等删除失败原因，返回 `code=500`，`data=false`。

## 12. 推荐测试顺序

建议按以下顺序执行，避免过早删除数据影响后续验证：

1. 健康检查：`GET /api/health`
2. 数据库连通性检查：`GET /api/db/ping`
3. 新增知识库文档：`POST /api/admin/knowledge/documents`
4. 从新增响应中复制 `data.id`
5. 分页查询：`GET /api/admin/knowledge/documents`
6. 查看详情：`GET /api/admin/knowledge/documents/1`
7. 更新文档：`PUT /api/admin/knowledge/documents/1`
8. 更新状态：`PATCH /api/admin/knowledge/documents/1/status`
9. HEAD 存在性检查：`HEAD /api/admin/knowledge/documents/1`
10. OPTIONS 集合资源：`OPTIONS /api/admin/knowledge/documents`
11. OPTIONS 单个资源：`OPTIONS /api/admin/knowledge/documents/1`
12. 删除文档：`DELETE /api/admin/knowledge/documents/1`

