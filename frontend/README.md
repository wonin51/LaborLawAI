# rag-kb-web

Vue 3 + Vite frontend for the EOS repair work order and enterprise maintenance document RAG knowledge base demo.

## Development

```bash
npm install
npm run dev
```

Default backend API base URL:

```text
VITE_API_BASE_URL=http://localhost:8080
```

The health check calls `GET /api/health`. In Vite development mode, `/api` is proxied to `VITE_API_BASE_URL` to avoid browser CORS issues.
