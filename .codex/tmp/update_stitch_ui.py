from pathlib import Path
root = Path(r'G:\AI-law-creater\frontend')

(root / 'src' / 'App.vue').write_text('''<script setup>
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
    subtitle: '维修文档知识库',
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

const activeTitle = computed(() => entries.find(item => item.key === activeEntry.value)?.title || '维修工单')

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
    const status = String(data?.status || '').toLowerCase()
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
      <div class="brand-block">
        <h1>维修知识助手</h1>
        <p>Modern Maintenance</p>
      </div>

      <el-menu :default-active="activeEntry" class="rail-menu" @select="activeEntry = $event">
        <el-menu-item v-for="item in entries" :key="item.key" :index="item.key">
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
        <el-button class="outline-action" plain>全局说明</el-button>
        <div class="profile-link">
          <el-icon><User /></el-icon>
          <span>实训工作台</span>
        </div>
      </div>
    </aside>

    <section class="workspace">
      <header class="topbar">
        <div class="breadcrumb">
          <span>EOS 维修知识库</span>
          <el-icon><ArrowRight /></el-icon>
          <strong>{{ activeTitle }}</strong>
        </div>
        <div class="topbar-meta">
          <span class="mono-date">2026-07-18</span>
          <span class="policy-mark">
            <el-icon><Shield /></el-icon>
            工单规范为主
          </span>
          <el-tag :type="healthTagType" effect="plain">{{ healthText }}</el-tag>
          <el-button size="small" @click="refreshHealth">重新检查</el-button>
        </div>
      </header>

      <main class="content-stage">
        <component :is="activeComponent" />
      </main>

      <footer class="notice-bar">
        <span>本工具用于 EOS 维修工单与企业维修文档知识检索实训，不替代正式维修决策、质量审核或安全审批。</span>
        <span>API：{{ configuredBaseUrl }}/api/health</span>
      </footer>
    </section>
  </div>
</template>
''', encoding='utf-8')

(root / 'src' / 'views' / 'WorkOrdersView.vue').write_text('''<template>
  <section class="page-template work-order-page">
    <div class="hero-copy">
      <p class="eyebrow">EOS WORK ORDER</p>
      <h2>把维修工单问题说清楚</h2>
      <p>
        按设备、故障现象、处理步骤和备件信息整理工单，为后续知识检索、相似案例匹配和 AI 问答提供结构化线索。
      </p>
    </div>

    <div class="two-column-grid">
      <el-card class="paper-panel input-panel" shadow="never">
        <template #header>
          <div class="panel-title">
            <el-icon><EditPen /></el-icon>
            <span>录入工单描述</span>
          </div>
        </template>
        <el-input
          type="textarea"
          :rows="13"
          placeholder="例如：EOS-2026-0718-001，空压机启动后 5 分钟高温报警，现场已检查冷却风扇和油位..."
          disabled
        />
        <div class="panel-actions">
          <span class="hint-line"><el-icon><InfoFilled /></el-icon> 当前阶段仅展示 UI，不提交业务数据</span>
          <el-button class="ink-button" disabled>生成工单摘要</el-button>
        </div>
      </el-card>

      <el-card class="paper-panel result-panel" shadow="never">
        <template #header>
          <div class="panel-title">
            <el-icon><DataAnalysis /></el-icon>
            <span>工单信息卡</span>
          </div>
        </template>
        <div class="case-card high">
          <div class="case-topline">
            <h3>高温报警排查未闭环</h3>
            <el-tag type="warning" effect="plain">待补充</el-tag>
          </div>
          <p class="case-label">建议补充字段</p>
          <div class="quote-box">设备型号、报警代码、现场照片、已更换备件、复测结果。</div>
          <div class="case-meta">
            <div><span>工单类型</span><strong>设备故障</strong></div>
            <div><span>知识匹配</span><strong>待接入</strong></div>
          </div>
        </div>

        <div class="case-card muted">
          <div class="case-topline">
            <h3>维修过程可追溯</h3>
            <el-tag type="success" effect="plain">规范项</el-tag>
          </div>
          <p>后续将把维修步骤、故障原因、处理结果沉淀为可检索知识片段。</p>
        </div>
      </el-card>
    </div>

    <section class="process-section">
      <h3>工单沉淀流程</h3>
      <div class="process-line">
        <div class="process-step"><span>01</span><strong>录入现象</strong></div>
        <div class="process-step"><span>02</span><strong>补全字段</strong></div>
        <div class="process-step"><span>03</span><strong>匹配案例</strong></div>
        <div class="process-step"><span>04</span><strong>形成知识</strong></div>
        <div class="process-step"><span>05</span><strong>复盘归档</strong></div>
      </div>
    </section>
  </section>
</template>
''', encoding='utf-8')

(root / 'src' / 'views' / 'KnowledgeDocsView.vue').write_text('''<template>
  <section class="page-template docs-page">
    <div class="hero-copy compact">
      <p class="eyebrow">MAINTENANCE DOCUMENTS</p>
      <h2>检查企业维修文档是否适合入库</h2>
      <p>先识别维修手册、SOP、故障案例和设备说明中的结构风险，为后续解析、切分、向量化预留入口。</p>
    </div>

    <div class="review-grid">
      <el-card class="paper-panel doc-input" shadow="never">
        <template #header>
          <div class="panel-title split-title">
            <span>输入文档摘要</span>
            <el-button link type="primary" disabled>
              <el-icon><Upload /></el-icon> 上传文本
            </el-button>
          </div>
        </template>
        <el-input
          type="textarea"
          :rows="18"
          placeholder="在此粘贴维修 SOP、设备手册、故障案例摘要。后续将接入文档上传、解析、切分和索引服务。"
          disabled
        />
        <div class="review-footer">
          <el-button text disabled>清空</el-button>
          <el-button class="ink-button" disabled>开始检查</el-button>
        </div>
      </el-card>

      <el-card class="paper-panel doc-result" shadow="never">
        <template #header>
          <div class="panel-title">
            <el-icon><DocumentChecked /></el-icon>
            <span>入库检查结果</span>
          </div>
        </template>

        <div class="risk-card danger-line">
          <div class="risk-title">
            <h3>步骤缺少安全前置条件</h3>
            <el-tag type="danger" effect="plain">高风险</el-tag>
          </div>
          <p class="case-label">原始片段</p>
          <div class="quote-box">“拆卸前确认设备停机。”</div>
          <div class="risk-columns">
            <div><span>风险类型</span><strong>安全条件不足</strong></div>
            <div><span>原因</span><strong>未明确断电、泄压、挂牌上锁。</strong></div>
          </div>
        </div>

        <div class="risk-card calm-line">
          <div class="risk-title">
            <h3>文档结构清晰</h3>
            <el-tag type="info" effect="plain">低风险</el-tag>
          </div>
          <p class="case-label">修改建议</p>
          <div class="quote-box">建议保留章节编号，作为切分和引用溯源依据。</div>
        </div>
      </el-card>
    </div>
  </section>
</template>
''', encoding='utf-8')

(root / 'src' / 'views' / 'AiQaView.vue').write_text('''<template>
  <section class="page-template ai-page">
    <div class="hero-copy centered">
      <p class="eyebrow">RAG ASSISTANT</p>
      <h2>向维修知识库提问</h2>
      <p>围绕 EOS 工单、维修案例和企业文档进行检索增强问答，后续会返回结论、引用片段、处理步骤和注意事项。</p>
    </div>

    <el-card class="ask-card" shadow="never">
      <el-input
        type="textarea"
        :rows="8"
        placeholder="例如：液压站压力波动，历史工单里通常是什么原因？需要先检查哪些项目？"
        disabled
      />
      <div class="ask-toolbar">
        <div class="toolbar-links">
          <span><el-icon><Files /></el-icon> 关联维修文档</span>
          <span><el-icon><Tickets /></el-icon> 引用相似工单</span>
        </div>
        <el-button class="ink-button" disabled>
          <el-icon><Promotion /></el-icon> 发送问题
        </el-button>
      </div>
      <p class="safe-hint"><el-icon><Warning /></el-icon> 请勿输入未脱敏的客户信息、账号密码或商业敏感数据。</p>
    </el-card>

    <section class="faq-section">
      <h3>高频问题</h3>
      <div class="faq-grid">
        <div class="faq-card">
          <span>报警排查</span>
          <strong>设备出现 E-07 高温报警，先检查哪些部件？</strong>
          <el-icon><Right /></el-icon>
        </div>
        <div class="faq-card">
          <span>备件更换</span>
          <strong>更换密封圈后仍泄漏，历史案例如何处理？</strong>
          <el-icon><Right /></el-icon>
        </div>
        <div class="faq-card">
          <span>维修 SOP</span>
          <strong>进入电柜检修前需要确认哪些安全步骤？</strong>
          <el-icon><Right /></el-icon>
        </div>
        <div class="faq-card">
          <span>工单复盘</span>
          <strong>如何把重复故障工单沉淀为知识文档？</strong>
          <el-icon><Right /></el-icon>
        </div>
      </div>
    </section>
  </section>
</template>
''', encoding='utf-8')

(root / 'src' / 'styles.css').write_text('''@import url('https://fonts.googleapis.com/css2?family=Noto+Sans+SC:wght@400;500;600;700&family=Noto+Serif+SC:wght@600;700&family=JetBrains+Mono:wght@500;600&display=swap');

:root {
  --paper: #f7f9ff;
  --paper-soft: #ecf4ff;
  --paper-line: #c3c6ce;
  --ink: #001d36;
  --ink-panel: #17324d;
  --ink-muted: #43474d;
  --teal: #146966;
  --teal-soft: #a5f0eb;
  --amber: #ce8a28;
  --danger: #ba1a1a;
  --danger-soft: #ffdad6;
  --white: #ffffff;
  --shadow: 0 12px 32px rgba(23, 50, 77, 0.07);
}

* { box-sizing: border-box; }

html, body, #app { min-height: 100%; }

body {
  margin: 0;
  min-width: 1120px;
  color: var(--ink);
  background: var(--paper);
  font-family: 'Noto Sans SC', 'Microsoft YaHei', Arial, sans-serif;
}

.institutional-shell {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 320px 1fr;
  background:
    linear-gradient(90deg, rgba(0, 29, 54, 0.035) 0 1px, transparent 1px) 320px 0 / 48px 48px,
    var(--paper);
}

.left-rail {
  position: sticky;
  top: 0;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: rgba(255, 255, 255, 0.96);
  border-right: 1px solid var(--paper-line);
}

.brand-block {
  padding: 34px 30px 28px;
  border-bottom: 1px solid rgba(195, 198, 206, 0.45);
}

.brand-block h1 {
  margin: 0;
  font-family: 'Noto Serif SC', serif;
  font-size: 32px;
  line-height: 1.2;
  letter-spacing: -0.04em;
  color: var(--ink);
}

.brand-block p {
  margin: 12px 0 0;
  color: #26323d;
  font-size: 16px;
}

.rail-menu {
  flex: 1;
  padding: 28px 15px;
  border-right: none !important;
  background: transparent;
}

.rail-menu .el-menu-item {
  height: 62px;
  margin: 0 0 10px;
  padding-left: 18px !important;
  border-radius: 5px;
  color: #26323d;
  border-left: 4px solid transparent;
}

.rail-menu .el-menu-item .el-icon { font-size: 24px; }

.rail-menu .el-menu-item.is-active {
  color: var(--ink);
  background: #d7e4f2;
  border-left-color: var(--ink-panel);
}

.menu-copy {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-left: 10px;
}

.menu-copy strong { font-size: 17px; font-weight: 700; }
.menu-copy span { font-size: 12px; color: #6b7280; }

.rail-footer {
  padding: 18px 20px 28px;
}

.outline-action {
  width: 100%;
  height: 42px;
  color: var(--ink) !important;
  border-color: var(--ink) !important;
  background: #fff !important;
}

.profile-link {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 28px;
  padding: 0 22px;
  color: #26323d;
  font-size: 16px;
}

.profile-link .el-icon { font-size: 24px; }

.workspace {
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.topbar {
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 30px 0 60px;
  border-bottom: 1px solid var(--paper-line);
  background: rgba(247, 249, 255, 0.92);
}

.breadcrumb,
.topbar-meta,
.policy-mark {
  display: flex;
  align-items: center;
  gap: 12px;
}

.breadcrumb span { color: var(--ink-muted); }
.breadcrumb strong { color: var(--ink); }

.mono-date {
  font-family: 'JetBrains Mono', monospace;
  letter-spacing: 0.08em;
  color: #111d27;
}

.policy-mark {
  color: #111d27;
  font-size: 14px;
}

.content-stage {
  flex: 1;
  padding: 64px 60px 96px;
}

.notice-bar {
  min-height: 58px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 10px 30px;
  color: #26323d;
  border-top: 1px solid #cbd5e1;
  background: #ecf4ff;
  font-size: 14px;
}

.page-template { max-width: 1180px; margin: 0 auto; }

.hero-copy { max-width: 960px; margin-bottom: 46px; }
.hero-copy.centered { text-align: center; margin-left: auto; margin-right: auto; }
.hero-copy.compact { margin-bottom: 36px; }

.eyebrow {
  margin: 0 0 12px;
  font-family: 'JetBrains Mono', monospace;
  font-size: 12px;
  letter-spacing: 0.18em;
  color: var(--teal);
  text-transform: uppercase;
}

.hero-copy h2 {
  margin: 0;
  font-family: 'Noto Serif SC', serif;
  font-size: 42px;
  line-height: 1.25;
  letter-spacing: -0.04em;
  color: var(--ink);
}

.hero-copy p:not(.eyebrow) {
  max-width: 820px;
  margin: 20px 0 0;
  font-size: 18px;
  line-height: 1.8;
  color: #26323d;
}

.hero-copy.centered p:not(.eyebrow) { margin-left: auto; margin-right: auto; }

.two-column-grid,
.review-grid {
  display: grid;
  grid-template-columns: minmax(480px, 1fr) minmax(420px, 0.9fr);
  gap: 40px;
  align-items: stretch;
}

.paper-panel,
.ask-card {
  border: 1px solid #dfe5eb !important;
  border-radius: 8px !important;
  background: #fff !important;
  box-shadow: var(--shadow);
}

.paper-panel .el-card__header,
.paper-panel .el-card__body {
  padding: 0;
}

.panel-title,
.split-title {
  min-height: 64px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 30px;
  font-weight: 800;
  font-size: 18px;
  color: var(--ink);
  border-bottom: 1px solid #e5e7eb;
  background: #f7f9ff;
}

.split-title { justify-content: space-between; }

.input-panel .el-textarea,
.doc-input .el-textarea { display: block; }

.input-panel .el-textarea__inner,
.doc-input .el-textarea__inner,
.ask-card .el-textarea__inner {
  border: 0;
  border-radius: 0;
  box-shadow: none;
  resize: none;
  padding: 28px 30px;
  font-size: 17px;
  line-height: 1.8;
  color: var(--ink);
}

.panel-actions,
.review-footer,
.ask-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 82px;
  padding: 18px 30px;
  border-top: 1px solid #e5e7eb;
  background: #f7f9ff;
}

.hint-line,
.safe-hint,
.toolbar-links {
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--amber);
  font-size: 14px;
}

.toolbar-links { color: var(--ink); gap: 24px; }
.toolbar-links span { display: flex; align-items: center; gap: 8px; }

.ink-button {
  min-width: 150px;
  height: 48px;
  color: #fff !important;
  background: var(--ink-panel) !important;
  border-color: #001d36 !important;
  font-weight: 700;
}

.result-panel .el-card__body,
.doc-result .el-card__body { padding: 30px; }

.case-card,
.risk-card {
  padding: 28px;
  border-radius: 8px;
  background: #fff;
  border: 1px solid #dfe5eb;
}

.case-card + .case-card,
.risk-card + .risk-card { margin-top: 26px; }

.case-card.high,
.risk-card.danger-line { border-left: 5px solid var(--danger); }
.case-card.muted,
.risk-card.calm-line { border-left: 5px solid #cbd5e1; }

.case-topline,
.risk-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
}

.case-topline h3,
.risk-title h3 {
  margin: 0;
  font-family: 'Noto Serif SC', serif;
  font-size: 24px;
  color: var(--ink);
}

.case-label {
  margin: 22px 0 8px;
  color: #26323d;
  font-size: 14px;
}

.quote-box {
  padding: 14px 16px;
  border-radius: 5px;
  border: 1px solid #c7d5e4;
  background: #ecf4ff;
  line-height: 1.8;
  color: #17324d;
}

.case-meta,
.risk-columns {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 26px;
  margin-top: 18px;
}

.case-meta span,
.risk-columns span { display: block; color: #6b7280; font-size: 13px; margin-bottom: 6px; }
.case-meta strong,
.risk-columns strong { color: #111d27; line-height: 1.6; }

.process-section,
.faq-section {
  margin-top: 70px;
}

.process-section h3,
.faq-section h3 {
  margin: 0 0 28px;
  font-family: 'Noto Serif SC', serif;
  font-size: 28px;
  color: var(--ink);
}

.process-line {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  border-top: 1px solid #9aa7b4;
  padding-top: 28px;
}

.process-step {
  position: relative;
  padding-left: 22px;
}

.process-step::before {
  content: '';
  position: absolute;
  top: -34px;
  left: 0;
  width: 9px;
  height: 9px;
  border-radius: 50%;
  background: #c3c6ce;
}

.process-step span {
  display: block;
  margin-bottom: 12px;
  font-family: 'JetBrains Mono', monospace;
  letter-spacing: 0.08em;
}

.process-step strong { font-size: 18px; color: #111d27; }

.review-grid { grid-template-columns: minmax(460px, 1fr) minmax(420px, 1fr); }

.ask-card {
  max-width: 1000px;
  margin: 0 auto;
  overflow: hidden;
}

.ask-card .el-card__body { padding: 0; }

.safe-hint {
  margin: 0;
  padding: 0 30px 22px;
  color: var(--amber);
  background: #f7f9ff;
}

.faq-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 18px;
}

.faq-card {
  position: relative;
  min-height: 96px;
  padding: 22px 56px 20px 22px;
  border: 1px solid #d7dde5;
  border-radius: 5px;
  background: #fff;
  box-shadow: 0 8px 24px rgba(23, 50, 77, 0.04);
}

.faq-card span {
  display: block;
  margin-bottom: 8px;
  color: var(--teal);
  font-size: 14px;
}

.faq-card strong {
  color: #111d27;
  font-size: 17px;
  line-height: 1.55;
}

.faq-card .el-icon {
  position: absolute;
  right: 22px;
  top: 50%;
  transform: translateY(-50%);
  color: #a8b0ba;
  font-size: 24px;
}

@media (max-width: 1200px) {
  body { min-width: 0; }
  .institutional-shell { grid-template-columns: 260px 1fr; }
  .content-stage { padding: 42px 30px 84px; }
  .two-column-grid,
  .review-grid,
  .faq-grid { grid-template-columns: 1fr; }
  .hero-copy h2 { font-size: 34px; }
}
''', encoding='utf-8')

print('UI_UPDATED')
for p in [root/'src/App.vue', root/'src/styles.css', root/'src/views/WorkOrdersView.vue', root/'src/views/KnowledgeDocsView.vue', root/'src/views/AiQaView.vue']:
    print(p, p.stat().st_size)
