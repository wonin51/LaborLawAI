<script setup>
import { computed, onMounted, ref } from 'vue'
import { getHealth } from './api/health'
import { configuredBaseUrl } from './api/http'
import ConsultView from './views/WorkOrdersView.vue'
import ContractReviewView from './views/KnowledgeDocsView.vue'
import AiQaView from './views/AiQaView.vue'
import HistoryView from './views/HistoryView.vue'
import FavoritesView from './views/FavoritesView.vue'
import HelpView from './views/HelpView.vue'

const activeEntry = ref('consult')
const healthStatus = ref('checking')
const healthText = ref('正在检查后端服务...')

const entries = [
  {
    key: 'consult',
    title: '发起咨询',
    subtitle: '劳动合同问题咨询',
    icon: 'ChatLineSquare',
    component: ConsultView
  },
  {
    key: 'contract-review',
    title: '合同审查',
    subtitle: '识别条款常见风险',
    icon: 'DocumentChecked',
    component: ContractReviewView
  },
  {
    key: 'history',
    title: '咨询历史',
    subtitle: '继续追问与导出',
    icon: 'RefreshLeft',
    component: HistoryView
  },
  {
    key: 'favorites',
    title: '收藏答案',
    subtitle: '常用结论与依据',
    icon: 'Bookmark',
    component: FavoritesView
  },
  {
    key: 'help',
    title: '帮助说明',
    subtitle: '边界与使用规则',
    icon: 'QuestionFilled',
    component: HelpView
  },
  {
    key: 'ai-qa',
    title: 'AI 问答',
    subtitle: '法规依据与维权流程',
    icon: 'ChatDotRound',
    component: AiQaView,
    hidden: true
  }
]

const visibleEntries = computed(() => entries.filter(item => !item.hidden))

const activeComponent = computed(() => {
  return entries.find(item => item.key === activeEntry.value)?.component || ConsultView
})

const activeTitle = computed(() => entries.find(item => item.key === activeEntry.value)?.title || '发起咨询')

const healthTagType = computed(() => {
  if (healthStatus.value === 'ok') return 'success'
  if (healthStatus.value === 'error') return 'danger'
  return 'warning'
})

async function refreshHealth() {
  healthStatus.value = 'checking'
  healthText.value = '正在检查后端服务...'

  try {
    const response = await getHealth()
    const status = String(response?.data?.status || response?.status || '').toLowerCase()
    if (status === 'ok' || status === 'up') {
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
  <div class="institutional-shell">
    <aside class="left-rail">
      <div class="brand-block with-seal">
        <div class="seal-mark"><el-icon><ScaleToOriginal /></el-icon></div>
        <div>
          <h1>劳动权益助手</h1>
          <p>Modern Institutional</p>
        </div>
      </div>

      <el-menu :default-active="activeEntry" class="rail-menu" @select="activeEntry = $event">
        <el-menu-item v-for="item in visibleEntries" :key="item.key" :index="item.key">
          <el-icon>
            <component :is="item.icon" />
          </el-icon>
          <div class="menu-copy">
            <strong>{{ item.title }}</strong>
            <span>{{ item.subtitle }}</span>
          </div>
        </el-menu-item>
      </el-menu>

      <div class="rail-footer">
        <el-button class="outline-action" plain>
          <el-icon><InfoFilled /></el-icon>
          全局免责声明
        </el-button>
        <div class="profile-link">
          <el-icon><User /></el-icon>
          <span>个人中心</span>
        </div>
      </div>
    </aside>

    <section class="workspace">
      <header class="topbar">
        <div class="breadcrumb">
          <span>劳动合同法律助手</span>
          <el-icon><ArrowRight /></el-icon>
          <strong>{{ activeTitle }}</strong>
        </div>
        <div class="topbar-meta">
          <span class="policy-pill"><el-icon><Shield /></el-icon> 全国性法规为主</span>
          <span class="mono-date">2026-07-18</span>
          <el-tag :type="healthTagType" effect="plain">{{ healthText }}</el-tag>
          <el-button size="small" @click="refreshHealth">重新检查</el-button>
        </div>
      </header>

      <main class="content-stage">
        <component :is="activeComponent" />
      </main>

      <footer class="notice-bar">
        <span>本工具提供劳动合同相关法律信息辅助，不构成正式法律意见。复杂争议、重大金额或诉讼策略问题，请咨询专业律师或当地劳动保障部门。</span>
        <span>API：{{ configuredBaseUrl }}/api/health</span>
      </footer>
    </section>
  </div>
</template>
