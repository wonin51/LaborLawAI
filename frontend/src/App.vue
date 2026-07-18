<script setup>
import { computed, onMounted, ref } from 'vue'
import { ChatDotRound, Collection, Connection, Document, RefreshRight } from '@element-plus/icons-vue'
import { getHealth } from './api/health'

const activeEntry = ref('qa')
const healthStatus = ref('checking')
const healthText = ref('正在检查后端服务')

const entries = [
  {
    key: 'qa',
    label: 'AI 问答',
    icon: ChatDotRound,
    caption: '围绕劳动合同问题生成结构化咨询结果',
    highlights: ['结论摘要', '法律依据', '维权流程', '证据清单']
  },
  {
    key: 'documents',
    label: '知识文档',
    icon: Document,
    caption: '后续用于维护法规、司法解释、人社政策和FAQ',
    highlights: ['文档入库', '来源管理', '向量索引', '版本启停']
  }
]

const currentEntry = computed(() => entries.find((item) => item.key === activeEntry.value))
const isBackendOk = computed(() => healthStatus.value === 'ok')

async function checkHealth() {
  healthStatus.value = 'checking'
  healthText.value = '正在检查后端服务'

  try {
    const response = await getHealth()
    if (response.data?.status === 'ok') {
      healthStatus.value = 'ok'
      healthText.value = '后端服务已连接'
      return
    }
    healthStatus.value = 'error'
    healthText.value = '后端服务未连接'
  } catch {
    healthStatus.value = 'error'
    healthText.value = '后端服务未连接'
  }
}

onMounted(() => {
  checkHealth()
})
</script>

<template>
  <el-container class="app-shell">
    <el-aside class="side-panel" width="280px">
      <div class="brand-block">
        <p class="eyebrow">RAG KB DEMO</p>
        <h1>劳动合同法律助手</h1>
        <p>Modern Institutional</p>
      </div>

      <el-menu class="entry-menu" :default-active="activeEntry" @select="activeEntry = $event">
        <el-menu-item v-for="entry in entries" :key="entry.key" :index="entry.key">
          <el-icon><component :is="entry.icon" /></el-icon>
          <span>{{ entry.label }}</span>
        </el-menu-item>
      </el-menu>

      <div class="side-footer">
        <el-alert
          title="系统定位为劳动合同法律信息检索与咨询辅助工具，不替代正式法律意见。"
          type="info"
          :closable="false"
          show-icon
        />
      </div>
    </el-aside>

    <el-container>
      <el-header class="top-bar">
        <div class="top-meta">
          <span>全国性法规为主</span>
          <span>AI + RAG 实训项目</span>
        </div>
        <div class="health-pill" :class="healthStatus">
          <el-icon><Connection /></el-icon>
          <span>{{ healthText }}</span>
          <el-button :icon="RefreshRight" text circle aria-label="重新检查后端健康状态" @click="checkHealth" />
        </div>
      </el-header>

      <el-main class="main-canvas">
        <section class="hero-section">
          <div>
            <p class="eyebrow">业务入口</p>
            <h2>把劳动合同问题说清楚</h2>
            <p>
              先搭建可演示的前端入口，后续逐步接入智能问答、知识库管理、引用溯源和反馈评估。
            </p>
          </div>
          <el-card class="health-card" shadow="never">
            <template #header>
              <div class="card-header">
                <el-icon><Connection /></el-icon>
                <span>后端健康检查</span>
              </div>
            </template>
            <div class="health-result">
              <el-tag :type="isBackendOk ? 'success' : 'danger'" effect="plain">
                {{ isBackendOk ? '{"status":"ok"}' : '后端服务未连接' }}
              </el-tag>
              <p>接口：GET /api/health</p>
            </div>
          </el-card>
        </section>

        <section class="workspace-grid">
          <el-card class="workspace-card" shadow="never">
            <template #header>
              <div class="card-header">
                <el-icon><component :is="currentEntry.icon" /></el-icon>
                <span>{{ currentEntry.label }}</span>
              </div>
            </template>

            <div v-if="activeEntry === 'qa'" class="entry-content">
              <h3>智能咨询入口</h3>
              <p>{{ currentEntry.caption }}</p>
              <el-input
                type="textarea"
                :rows="5"
                placeholder="例如：入职两个月还没有签劳动合同，我可以要求公司赔偿吗？"
              />
              <div class="entry-actions">
                <el-button type="primary">发送咨询</el-button>
                <el-button>粘贴合同条款</el-button>
              </div>
            </div>

            <div v-else class="entry-content">
              <h3>知识文档入口</h3>
              <p>{{ currentEntry.caption }}</p>
              <el-upload drag action="#" :auto-upload="false">
                <el-icon class="upload-icon"><Collection /></el-icon>
                <div class="el-upload__text">拖拽法规政策文件到这里，或点击选择</div>
                <template #tip>
                  <div class="el-upload__tip">后续接入上传、解析、切分和向量化流程。</div>
                </template>
              </el-upload>
            </div>
          </el-card>

          <el-card class="outline-card" shadow="never">
            <template #header>
              <div class="card-header">
                <span>后续回答结构</span>
              </div>
            </template>
            <div class="answer-outline">
              <div v-for="(item, index) in currentEntry.highlights" :key="item" class="outline-item">
                <span>{{ String(index + 1).padStart(2, '0') }}</span>
                <strong>{{ item }}</strong>
              </div>
            </div>
          </el-card>
        </section>
      </el-main>
    </el-container>
  </el-container>
</template>
