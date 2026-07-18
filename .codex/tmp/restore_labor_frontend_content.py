from pathlib import Path
root = Path(r'G:\AI-law-creater\frontend')

(root / 'src' / 'App.vue').write_text('''<script setup>
import { computed, onMounted, ref } from 'vue'
import { getHealth } from './api/health'
import { configuredBaseUrl } from './api/http'
import WorkOrdersView from './views/WorkOrdersView.vue'
import KnowledgeDocsView from './views/KnowledgeDocsView.vue'
import AiQaView from './views/AiQaView.vue'

const activeEntry = ref('consult')
const healthStatus = ref('checking')
const healthText = ref('正在检查后端服务...')

const entries = [
  {
    key: 'consult',
    title: '发起咨询',
    subtitle: '劳动合同问题咨询',
    icon: 'ChatLineSquare',
    component: WorkOrdersView
  },
  {
    key: 'contract-review',
    title: '合同审查',
    subtitle: '识别条款常见风险',
    icon: 'DocumentChecked',
    component: KnowledgeDocsView
  },
  {
    key: 'ai-qa',
    title: 'AI 问答',
    subtitle: '法规依据与维权流程',
    icon: 'ChatDotRound',
    component: AiQaView
  }
]

const activeComponent = computed(() => {
  return entries.find(item => item.key === activeEntry.value)?.component || WorkOrdersView
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
        <h1>劳动权益助手</h1>
        <p>Modern Institutional</p>
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
        <el-button class="outline-action" plain>全局免责声明</el-button>
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
          <span class="mono-date">2026-07-18</span>
          <span class="policy-mark">
            <el-icon><Shield /></el-icon>
            全国性法规为主
          </span>
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
''', encoding='utf-8')

(root / 'src' / 'views' / 'WorkOrdersView.vue').write_text('''<template>
  <section class="page-template consult-page">
    <div class="hero-copy centered">
      <p class="eyebrow">LEGAL CONSULTATION</p>
      <h2>把劳动合同问题说清楚</h2>
      <p>
        根据现行法律法规、司法解释和人社政策，为你整理判断依据、处理步骤和证据清单。
      </p>
    </div>

    <el-card class="ask-card" shadow="never">
      <el-input
        type="textarea"
        :rows="8"
        placeholder="例如：入职两个月还没有签劳动合同，我可以要求公司赔偿吗？"
        disabled
      />
      <div class="ask-toolbar">
        <div class="toolbar-links">
          <span><el-icon><Clipboard /></el-icon> 粘贴合同条款</span>
          <span><el-icon><Upload /></el-icon> 上传示例文本</span>
        </div>
        <el-button class="ink-button" disabled>
          <el-icon><Promotion /></el-icon> 发送咨询
        </el-button>
      </div>
      <p class="safe-hint"><el-icon><Warning /></el-icon> 请勿输入身份证号、银行卡号等敏感信息。</p>
    </el-card>

    <section class="faq-section">
      <h3>高频问题</h3>
      <div class="faq-grid">
        <div class="faq-card">
          <span>加班费</span>
          <strong>周末总是被叫去加班，没给加班费怎么办？</strong>
          <el-icon><Right /></el-icon>
        </div>
        <div class="faq-card">
          <span>未签合同</span>
          <strong>工作半年了公司一直不跟我签合同合法吗？</strong>
          <el-icon><Right /></el-icon>
        </div>
        <div class="faq-card">
          <span>试用期补偿</span>
          <strong>试用期最后一天被辞退，有 N+1 补偿吗？</strong>
          <el-icon><Right /></el-icon>
        </div>
        <div class="faq-card">
          <span>拖欠工资</span>
          <strong>公司以效益不好为由拖欠工资两个月，如何维权？</strong>
          <el-icon><Right /></el-icon>
        </div>
      </div>
    </section>

    <section class="process-section">
      <h3>你将获得什么</h3>
      <div class="process-line">
        <div class="process-step"><span>01</span><strong>结论摘要</strong></div>
        <div class="process-step"><span>02</span><strong>法律依据</strong></div>
        <div class="process-step"><span>03</span><strong>维权流程</strong></div>
        <div class="process-step"><span>04</span><strong>证据清单</strong></div>
        <div class="process-step"><span>05</span><strong>注意事项</strong></div>
      </div>
    </section>
  </section>
</template>
''', encoding='utf-8')

(root / 'src' / 'views' / 'KnowledgeDocsView.vue').write_text('''<template>
  <section class="page-template review-page">
    <div class="hero-copy compact">
      <p class="eyebrow">CONTRACT REVIEW</p>
      <h2>检查劳动合同条款中的常见风险</h2>
      <p>识别常见明显风险，不替代完整的律师合同审查。</p>
    </div>

    <div class="review-grid">
      <el-card class="paper-panel doc-input" shadow="never">
        <template #header>
          <div class="panel-title split-title">
            <span>输入条款文本</span>
            <el-button link type="primary" disabled>
              <el-icon><Upload /></el-icon> 上传文本
            </el-button>
          </div>
        </template>
        <el-input
          type="textarea"
          :rows="18"
          placeholder="在此粘贴您的劳动合同条款..."
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
            <span>审查结果</span>
          </div>
        </template>

        <div class="risk-card danger-line">
          <div class="risk-title">
            <h3>竞业限制补偿约定不明确</h3>
            <el-tag type="danger" effect="plain">高风险</el-tag>
          </div>
          <p class="case-label">原始片段</p>
          <div class="quote-box">“乙方离职后两年内不得从事同类业务，甲方视情况给予一定补偿。”</div>
          <div class="risk-columns">
            <div><span>风险类型</span><strong>约定不明 / 显失公平</strong></div>
            <div><span>原因</span><strong>未明确具体补偿金额和发放方式。</strong></div>
          </div>
          <p class="case-label">修改建议</p>
          <div class="quote-box strong-box">建议补充明确的月度补偿标准，通常不低于离职前十二个月平均工资的 30%。</div>
        </div>

        <div class="risk-card calm-line">
          <div class="risk-title">
            <h3>工作地点表述宽泛</h3>
            <el-tag type="info" effect="plain">低风险</el-tag>
          </div>
          <p class="case-label">原始片段</p>
          <div class="quote-box">“工作地点：全国范围内根据公司业务需要调配。”</div>
          <p class="case-label">修改建议</p>
          <div class="quote-box">虽属常见，但建议尽可能细化核心工作常驻地，避免未来跨省调岗引发争议。</div>
        </div>
      </el-card>
    </div>
  </section>
</template>
''', encoding='utf-8')

(root / 'src' / 'views' / 'AiQaView.vue').write_text('''<template>
  <section class="page-template ai-page">
    <div class="hero-copy">
      <p class="eyebrow">RAG ASSISTANT</p>
      <h2>围绕劳动合同法规与案例提问</h2>
      <p>后续将接入法规政策知识库、合同条款片段和咨询历史，返回可追溯的依据和处理建议。</p>
    </div>

    <div class="two-column-grid">
      <el-card class="paper-panel input-panel" shadow="never">
        <template #header>
          <div class="panel-title">
            <el-icon><ChatDotRound /></el-icon>
            <span>输入咨询问题</span>
          </div>
        </template>
        <el-input
          type="textarea"
          :rows="13"
          placeholder="例如：公司没有提前三十天通知就解除劳动合同，我能要求哪些补偿？"
          disabled
        />
        <div class="panel-actions">
          <span class="hint-line"><el-icon><InfoFilled /></el-icon> 当前阶段仅展示 UI，不提交业务数据</span>
          <el-button class="ink-button" disabled>生成答复</el-button>
        </div>
      </el-card>

      <el-card class="paper-panel result-panel" shadow="never">
        <template #header>
          <div class="panel-title">
            <el-icon><DataAnalysis /></el-icon>
            <span>答复结构预览</span>
          </div>
        </template>
        <div class="case-card high">
          <div class="case-topline">
            <h3>解除劳动合同补偿</h3>
            <el-tag type="warning" effect="plain">需结合事实</el-tag>
          </div>
          <p class="case-label">结论摘要</p>
          <div class="quote-box">可能涉及经济补偿、违法解除赔偿或代通知金，需结合解除理由、通知方式和工作年限判断。</div>
          <div class="case-meta">
            <div><span>依据类型</span><strong>法律法规</strong></div>
            <div><span>引用状态</span><strong>待接入知识库</strong></div>
          </div>
        </div>

        <div class="case-card muted">
          <div class="case-topline">
            <h3>建议准备证据</h3>
            <el-tag type="success" effect="plain">清单项</el-tag>
          </div>
          <p>劳动合同、解除通知、工资流水、社保记录、沟通记录、考勤记录等。</p>
        </div>
      </el-card>
    </div>
  </section>
</template>
''', encoding='utf-8')

print('LABOR_CONTENT_RESTORED')
for p in [root/'src/App.vue', root/'src/views/WorkOrdersView.vue', root/'src/views/KnowledgeDocsView.vue', root/'src/views/AiQaView.vue']:
    print(p, p.stat().st_size)
