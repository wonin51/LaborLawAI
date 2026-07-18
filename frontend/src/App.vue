<script setup lang="ts">
import { computed, markRaw, onMounted, ref } from 'vue'
import { ChatLineRound, Collection, DocumentChecked } from '@element-plus/icons-vue'
import { checkHealth } from './api/health'

const sections = [
  {
    key: 'consult',
    label: '法律咨询',
    eyebrow: '咨询工作台',
    description: '从劳动合同条款出发，整理问题与待核实的法律依据。',
    prompt: '输入劳动合同相关问题后，咨询内容将在这里展开。',
    icon: markRaw(ChatLineRound),
  },
  {
    key: 'knowledge',
    label: '知识库管理',
    eyebrow: '依据档案',
    description: '集中管理法律法规、司法解释与内部参考资料。',
    prompt: '知识资料的上传、分类与检索功能将在后续课程实现。',
    icon: markRaw(Collection),
  },
  {
    key: 'history',
    label: '问答记录',
    eyebrow: '咨询卷宗',
    description: '回看历史问题、回答和引用依据。',
    prompt: '完成咨询后，问答记录将在这里按时间归档。',
    icon: markRaw(DocumentChecked),
  },
] as const

const activeKey = ref<(typeof sections)[number]['key']>('consult')
const backendConnected = ref<boolean | null>(null)

const activeSection = computed(
  () => sections.find((section) => section.key === activeKey.value) ?? sections[0],
)

onMounted(async () => {
  backendConnected.value = await checkHealth()
})
</script>

<template>
  <main class="workspace">
    <aside class="case-sidebar" aria-label="主导航">
      <div class="brand-block">
        <span class="brand-seal" aria-hidden="true">法</span>
        <div>
          <p class="brand-kicker">LABOR CONTRACT DESK</p>
          <h1>劳动合同<br />法律助手</h1>
        </div>
      </div>

      <nav class="case-tabs">
        <button
          v-for="section in sections"
          :key="section.key"
          type="button"
          class="case-tab"
          :class="{ active: activeKey === section.key }"
          :data-section="section.key"
          :aria-current="activeKey === section.key ? 'page' : undefined"
          @click="activeKey = section.key"
        >
          <el-icon :size="19"><component :is="section.icon" /></el-icon>
          <span>{{ section.label }}</span>
        </button>
      </nav>

      <div class="service-card" aria-live="polite">
        <span
          class="service-dot"
          :class="{
            connected: backendConnected === true,
            disconnected: backendConnected === false,
          }"
          aria-hidden="true"
        />
        <div>
          <span class="service-label">API / 8080</span>
          <strong v-if="backendConnected === null">正在检查后端服务</strong>
          <strong v-else-if="backendConnected">后端服务已连接</strong>
          <strong v-else>后端服务未连接</strong>
        </div>
      </div>
    </aside>

    <section class="case-sheet">
      <header class="sheet-header">
        <div>
          <p class="sheet-eyebrow">{{ activeSection.eyebrow }}</p>
          <h2 data-testid="section-title">{{ activeSection.label }}</h2>
        </div>
        <el-tag class="stage-tag" effect="plain" round>基础框架</el-tag>
      </header>

      <div class="annotation-line" aria-hidden="true">
        <span>CONTRACT NOTE</span>
      </div>

      <article class="content-panel">
        <p class="section-intro">{{ activeSection.description }}</p>
        <div class="empty-docket">
          <el-icon :size="34"><component :is="activeSection.icon" /></el-icon>
          <p>{{ activeSection.prompt }}</p>
          <span>本课仅完成页面入口与前后端联通</span>
        </div>
      </article>

      <footer class="legal-note">
        本系统用于劳动合同法律信息检索与咨询辅助，不替代律师、仲裁机构或法院意见。
      </footer>
    </section>
  </main>
</template>
