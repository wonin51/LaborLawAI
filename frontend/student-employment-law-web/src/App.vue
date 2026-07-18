<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getHealth } from './api/health'

const route = useRoute()
const router = useRouter()
const activeMenu = computed(() => route.path)
const healthStatus = ref('checking')
const healthText = computed(() => {
  if (healthStatus.value === 'ok') return '后端服务已连接'
  if (healthStatus.value === 'error') return '后端服务未连接'
  return '正在检测后端服务'
})
const healthTagType = computed(() => healthStatus.value === 'ok' ? 'success' : healthStatus.value === 'error' ? 'danger' : 'warning')

async function checkBackendHealth() {
  healthStatus.value = 'checking'
  try {
    const data = await getHealth()
    healthStatus.value = data?.status === 'ok' ? 'ok' : 'error'
  } catch (error) {
    healthStatus.value = 'error'
  }
}

function go(path) {
  router.push(path)
}

onMounted(checkBackendHealth)
</script>

<template>
  <div class="app-shell">
    <aside class="side-panel">
      <div class="brand-card">
        <div class="brand-mark">法</div>
        <div>
          <p class="eyebrow">Student Employment</p>
          <h1>大学生就业法律助手</h1>
        </div>
      </div>

      <el-menu :default-active="activeMenu" class="main-menu" @select="go">
        <el-menu-item index="/contract-review">
          <el-icon><DocumentChecked /></el-icon>
          <span>劳务合同审查</span>
        </el-menu-item>
        <el-menu-item index="/legal-knowledge">
          <el-icon><Reading /></el-icon>
          <span>就业法律知识</span>
        </el-menu-item>
        <el-menu-item index="/ai-consultation">
          <el-icon><ChatDotRound /></el-icon>
          <span>AI 法律问答</span>
        </el-menu-item>
      </el-menu>

      <div class="side-note">
        <span>面向实习、兼职、试用期、三方协议与劳动/劳务合同场景。</span>
      </div>
    </aside>

    <main class="content-panel">
      <header class="topbar">
        <div>
          <p class="eyebrow">就业风险预警台</p>
          <h2>{{ route.meta.title }}</h2>
        </div>
        <div class="health-box">
          <el-tag :type="healthTagType" effect="dark" round>{{ healthText }}</el-tag>
          <el-button size="small" plain @click="checkBackendHealth">重新检测</el-button>
        </div>
      </header>

      <router-view />
    </main>
  </div>
</template>
