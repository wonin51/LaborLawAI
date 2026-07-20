# LaborLawAI frontend

Vue 3 + Vite 前端基础结构，用于劳动法律智能助手页面。

## Development

```bash
npm install
npm run dev
```

Default backend API base URL:

```text
VITE_API_BASE_URL=http://127.0.0.1:8080
```

## 任务 02 完成内容

- `src/api/http.js`：使用 Axios 封装统一请求实例。
- 请求基础地址读取 `VITE_API_BASE_URL`，开发环境通过 Vite `/api` 代理转发到后端。
- `src/App.vue`：增加基础布局，左侧菜单使用前端已有菜单配置。
- 主界面顶部展示 `GET /api/health` 后端健康检查结果。
- 后端不可用或返回非成功状态时，页面显示 `后端服务未连接`。
- 不包含登录和权限控制。

## 接口约定

后端接口统一返回：

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

`http.js` 会在 `code === 0` 时返回 `data`，其他业务状态会抛出错误交给页面处理。

健康检查接口：

```text
GET /api/health
```

期望成功数据：

```json
{
  "status": "ok"
}
```
