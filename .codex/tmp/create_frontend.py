from pathlib import Path
import shutil

root = Path(r'G:\AI-law-creater').resolve()
frontend = (root / 'frontend').resolve()
if root not in frontend.parents:
    raise SystemExit(f'Refuse to operate outside root: {frontend}')
if frontend.exists():
    shutil.rmtree(frontend)

for p in [
    frontend / 'src' / 'api',
    frontend / 'src' / 'components',
    frontend / 'src' / 'views',
    frontend / 'public',
]:
    p.mkdir(parents=True, exist_ok=True)

(frontend / 'package.json').write_text('''{
  "name": "rag-kb-web",
  "version": "0.0.1",
  "private": true,
  "type": "module",
  "scripts": {
    "dev": "vite --host 127.0.0.1",
    "build": "vite build",
    "preview": "vite preview --host 127.0.0.1"
  },
  "dependencies": {
    "@element-plus/icons-vue": "^2.3.1",
    "@vitejs/plugin-vue": "^6.0.1",
    "axios": "^1.13.2",
    "element-plus": "^2.11.8",
    "vite": "^7.2.7",
    "vue": "^3.5.24"
  },
  "devDependencies": {}
}
''', encoding='utf-8')

(frontend / '.env').write_text('''VITE_API_BASE_URL=http://localhost:8080
''', encoding='utf-8')

(frontend / '.env.development').write_text('''VITE_API_BASE_URL=http://localhost:8080
''', encoding='utf-8')

(frontend / 'index.html').write_text('''<!doctype html>
<html lang="zh-CN">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>rag-kb-web</title>
  </head>
  <body>
    <div id="app"></div>
    <script type="module" src="/src/main.js"></script>
  </body>
</html>
''', encoding='utf-8')

(frontend / 'vite.config.js').write_text('''import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const apiTarget = env.VITE_API_BASE_URL || 'http://localhost:8080'

  return {
    plugins: [vue()],
    server: {
      host: '127.0.0.1',
      port: 5173,
      proxy: {
        '/api': {
          target: apiTarget,
          changeOrigin: true,
          secure: false
        }
      }
    }
  }
})
''', encoding='utf-8')

(frontend / 'src' / 'main.js').write_text('''import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import './styles.css'

const app = createApp(App)

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(ElementPlus)
app.mount('#app')
''', encoding='utf-8')

(frontend / 'src' / 'api' / 'http.js').write_text('''import axios from 'axios'

const configuredBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

// 开发环境优先走 Vite /api 代理，避免浏览器跨域；配置值仍保留为 http://localhost:8080。
const isDev = import.meta.env.DEV
const baseURL = isDev ? '/api' : `${configuredBaseUrl.replace(/\/$/, '')}/api`

const http = axios.create({
  baseURL,
  timeout: 5000
})

http.interceptors.response.use(
  response => response.data,
  error => Promise.reject(error)
)

export default http
export { configuredBaseUrl }
''', encoding='utf-8')

(frontend / 'src' / 'api' / 'health.js').write_text('''import http from './http'

export function getHealth() {
  return http.get('/health')
}
''', encoding='utf-8')

(frontend / 'src' / 'views' / 'WorkOrdersView.vue').write_text('''<template>
  <section class="view-card">
    <div class="view-header">
      <el-icon><Tickets /></el-icon>
      <div>
        <h2>维修工单</h2>
        <p>面向 EOS 维修工单的录入、检索和处理过程展示入口。</p>
      </div>
    </div>

    <el-empty description="维修工单业务功能待后续实训阶段实现">
      <el-button type="primary" plain>预留工单列表</el-button>
    </el-empty>
  </section>
</template>
''', encoding='utf-8')

(frontend / 'src' / 'views' / 'KnowledgeDocsView.vue').write_text('''<template>
  <section class="view-card">
    <div class="view-header">
      <el-icon><Document /></el-icon>
      <div>
        <h2>知识文档</h2>
        <p>面向企业维修手册、故障案例、SOP 和设备文档的知识库入口。</p>
      </div>
    </div>

    <el-empty description="知识文档上传、解析、切分和向量化功能待后续实现">
      <el-button type="success" plain>预留文档管理</el-button>
    </el-empty>
  </section>
</template>
''', encoding='utf-8')

(frontend / 'src' / 'views' / 'AiQaView.vue').write_text('''<template>
  <section class="view-card">
    <div class="view-header">
      <el-icon><ChatDotRound /></el-icon>
      <div>
        <h2>AI 问答</h2>
        <p>面向维修知识库的 RAG 检索增强问答入口。</p>
      </div>
    </div>

    <el-card shadow="never" class="qa-placeholder">
      <el-input
        type="textarea"
        :rows="4"
        placeholder="例如：某设备报警后如何排查？后续将接入 RAG 问答接口。"
        disabled
      />
      <div class="qa-actions">
        <el-button type="primary" disabled>发送问题</el-button>
      </div>
    </el-card>
  </section>
</template>
''', encoding='utf-8')

(frontend / 'src' / 'App.vue').write_text('''<script setup>
import { computed, onMounted, ref } from 'vue'
import { getHealth } from './api/health'
import { configuredBaseUrl } from './api/http'
import WorkOrdersView from './views/WorkOrdersView.vue'
import KnowledgeDocsView from './views/KnowledgeDocsView.vue'
import AiQaView from './views/AiQaView.vue'

const activeEntry = ref('work-orders')
const healthStatus = ref('checking')
const healthText = ref('正在检查后端服务...')

const entries = [
  {
    key: 'work-orders',
    title: '维修工单',
    subtitle: 'EOS 工单处理入口',
    icon: 'Tickets',
    component: WorkOrdersView
  },
  {
    key: 'knowledge-docs',
    title: '知识文档',
    subtitle: '企业维修文档知识库',
    icon: 'Document',
    component: KnowledgeDocsView
  },
  {
    key: 'ai-qa',
    title: 'AI 问答',
    subtitle: 'RAG 检索增强问答',
    icon: 'ChatDotRound',
    component: AiQaView
  }
]

const activeComponent = computed(() => {
  return entries.find(item => item.key === activeEntry.value)?.component || WorkOrdersView
})

const healthTagType = computed(() => {
  if (healthStatus.value === 'ok') return 'success'
  if (healthStatus.value === 'error') return 'danger'
  return 'warning'
})

async function refreshHealth() {
  healthStatus.value = 'checking'
  healthText.value = '正在检查后端服务...'

  try {
    const data = await getHealth()
    if (data?.status === 'ok') {
      healthStatus.value = 'ok'
      healthText.value = '后端服务已连接'
      return
    }
    throw new Error('Unexpected health response')
  } catch (error) {
    healthStatus.value = 'error'
    healthText.value = '后端服务未连接'
  }
}

onMounted(refreshHealth)
</script>

<template>
  <el-container class="app-shell">
    <el-header class="app-header">
      <div>
        <h1>EOS 维修 RAG 知识库实训系统</h1>
        <p>维修工单 · 企业维修文档 · AI 问答</p>
      </div>

      <div class="health-panel">
        <el-tag :type="healthTagType" size="large">{{ healthText }}</el-tag>
        <el-button size="small" @click="refreshHealth">重新检查</el-button>
      </div>
    </el-header>

    <el-container>
      <el-aside width="280px" class="app-aside">
        <div class="api-base">
          <span>后端地址</span>
          <strong>{{ configuredBaseUrl }}</strong>
        </div>

        <el-menu :default-active="activeEntry" class="entry-menu" @select="activeEntry = $event">
          <el-menu-item v-for="item in entries" :key="item.key" :index="item.key">
            <el-icon>
              <component :is="item.icon" />
            </el-icon>
            <div class="menu-text">
              <span>{{ item.title }}</span>
              <small>{{ item.subtitle }}</small>
            </div>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-main class="app-main">
        <component :is="activeComponent" />
      </el-main>
    </el-container>
  </el-container>
</template>
''', encoding='utf-8')

(frontend / 'src' / 'styles.css').write_text('''* {
  box-sizing: border-box;
}

body {
  margin: 0;
  min-width: 1024px;
  min-height: 100vh;
  font-family: Inter, "PingFang SC", "Microsoft YaHei", Arial, sans-serif;
  color: #1f2937;
  background: #eef3f8;
}

.app-shell {
  min-height: 100vh;
}

.app-header {
  height: 92px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32px;
  color: #fff;
  background: linear-gradient(135deg, #1f4e78 0%, #2563eb 55%, #0f766e 100%);
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.18);
}

.app-header h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 800;
}

.app-header p {
  margin: 8px 0 0;
  opacity: 0.86;
}

.health-panel {
  display: flex;
  align-items: center;
  gap: 12px;
}

.app-aside {
  padding: 24px 18px;
  background: #fff;
  border-right: 1px solid #dde7f0;
}

.api-base {
  padding: 14px 16px;
  margin-bottom: 18px;
  border-radius: 12px;
  background: #f4f8fb;
  border: 1px solid #d8e7f1;
}

.api-base span {
  display: block;
  margin-bottom: 6px;
  font-size: 12px;
  color: #64748b;
}

.api-base strong {
  display: block;
  font-size: 13px;
  word-break: break-all;
  color: #0f172a;
}

.entry-menu {
  border-right: 0;
}

.entry-menu .el-menu-item {
  height: 68px;
  margin-bottom: 10px;
  border-radius: 14px;
}

.menu-text {
  display: flex;
  flex-direction: column;
  line-height: 1.3;
}

.menu-text small {
  margin-top: 4px;
  color: #64748b;
}

.app-main {
  padding: 28px;
}

.view-card {
  min-height: calc(100vh - 150px);
  padding: 28px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid #dce8f2;
  box-shadow: 0 18px 45px rgba(15, 23, 42, 0.08);
}

.view-header {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding-bottom: 20px;
  margin-bottom: 24px;
  border-bottom: 1px solid #e5edf5;
}

.view-header .el-icon {
  width: 48px;
  height: 48px;
  padding: 12px;
  border-radius: 14px;
  font-size: 24px;
  color: #2563eb;
  background: #eaf2ff;
}

.view-header h2 {
  margin: 0;
  font-size: 24px;
}

.view-header p {
  margin: 8px 0 0;
  color: #64748b;
}

.qa-placeholder {
  max-width: 720px;
}

.qa-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 14px;
}
''', encoding='utf-8')

(frontend / 'README.md').write_text('''# rag-kb-web

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
''', encoding='utf-8')

print('CREATED_FRONTEND', frontend)
for path in sorted(frontend.rglob('*')):
    if path.is_file():
        print(path)
