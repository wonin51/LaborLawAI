from pathlib import Path
root = Path(r'G:\AI-law-creater\frontend')

(root / 'src' / 'App.vue').write_text('''<script setup>
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
''', encoding='utf-8')

(root / 'src' / 'views' / 'WorkOrdersView.vue').write_text('''<template>
  <section class="page-template consult-page">
    <div class="hero-copy centered consult-hero">
      <p class="eyebrow">LEGAL CONSULTATION</p>
      <h2>把劳动合同问题说清楚</h2>
      <p>根据现行法律法规、司法解释和人社政策，为你整理判断依据、处理步骤和证据清单。</p>
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

(root / 'src' / 'views' / 'HistoryView.vue').write_text('''<template>
  <section class="page-template history-page">
    <div class="history-heading-row">
      <div class="hero-copy compact">
        <p class="eyebrow">CONSULTATION HISTORY</p>
        <h2>咨询历史</h2>
        <p>查看、继续提问或导出您过去的劳动法咨询记录。</p>
      </div>
      <div class="history-tools">
        <el-input placeholder="搜索问题或关键词..." disabled>
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button disabled>全部类型</el-button>
        <el-button disabled>近30天</el-button>
      </div>
    </div>

    <div class="history-list">
      <article class="history-card">
        <el-tag effect="plain" type="success">薪酬福利</el-tag>
        <h3>关于试用期工资低于当地最低工资标准的维权</h3>
        <p><strong>结论摘要：</strong>试用期工资不得低于本单位相同岗位最低档工资的 80% 或劳动合同约定工资的 80%，且不得低于用人单位所在地最低工资标准。</p>
        <div class="history-meta">
          <span><el-icon><Clock /></el-icon> 2026-07-18 14:30 更新</span>
          <span><el-icon><ScaleToOriginal /></el-icon> 引用法规：3项</span>
          <el-icon class="bookmark"><Bookmark /></el-icon>
        </div>
      </article>

      <article class="history-card">
        <el-tag effect="plain" type="info">解除辞退</el-tag>
        <h3>公司未提前通知单方面解除劳动合同的赔偿计算</h3>
        <p><strong>结论摘要：</strong>若认定为违法解除，可主张经济赔偿金；若合法但未提前 30 天通知，可主张代通知金加经济补偿金。</p>
        <div class="history-meta">
          <span><el-icon><Clock /></el-icon> 2026-07-16 09:15 更新</span>
          <span><el-icon><ScaleToOriginal /></el-icon> 引用法规：5项</span>
          <el-icon class="bookmark"><Bookmark /></el-icon>
        </div>
      </article>
    </div>
  </section>
</template>
''', encoding='utf-8')

(root / 'src' / 'views' / 'FavoritesView.vue').write_text('''<template>
  <section class="page-template history-page">
    <div class="hero-copy compact">
      <p class="eyebrow">SAVED ANSWERS</p>
      <h2>收藏答案</h2>
      <p>集中保存常用结论、法律依据和证据清单，便于后续复查。</p>
    </div>

    <div class="saved-grid">
      <div class="saved-card">
        <span>引用 [1]</span>
        <h3>《中华人民共和国劳动合同法》第八十二条</h3>
        <p>用人单位自用工之日起超过一个月不满一年未与劳动者订立书面劳动合同的，应当向劳动者每月支付二倍的工资。</p>
      </div>
      <div class="saved-card">
        <span>证据清单</span>
        <h3>未签劳动合同咨询常用材料</h3>
        <p>工资流水、考勤记录、入职沟通记录、工作群记录、社保缴纳记录、工牌或办公系统账号。</p>
      </div>
      <div class="saved-card">
        <span>流程提醒</span>
        <h3>协商、投诉与仲裁路径</h3>
        <p>先固定证据并与公司协商，协商不成可向劳动监察部门投诉或申请劳动仲裁。</p>
      </div>
    </div>
  </section>
</template>
''', encoding='utf-8')

(root / 'src' / 'views' / 'HelpView.vue').write_text('''<template>
  <section class="page-template help-page">
    <div class="hero-copy compact">
      <p class="eyebrow">HELP & BOUNDARY</p>
      <h2>帮助说明</h2>
      <p>了解本工具能做什么、不能做什么，以及如何获得更可靠的咨询结果。</p>
    </div>

    <div class="help-grid">
      <el-card class="paper-panel" shadow="never">
        <template #header><div class="panel-title">适用范围</div></template>
        <ul class="brief-list">
          <li>劳动合同签订、变更、解除、终止相关问题。</li>
          <li>工资、加班费、试用期、竞业限制等常见条款咨询。</li>
          <li>维权流程、证据准备和法规依据检索辅助。</li>
        </ul>
      </el-card>
      <el-card class="paper-panel" shadow="never">
        <template #header><div class="panel-title">使用边界</div></template>
        <ul class="brief-list">
          <li>不替代律师、仲裁机构或法院意见。</li>
          <li>不处理未脱敏的个人敏感信息。</li>
          <li>复杂争议和重大金额问题应咨询专业人士。</li>
        </ul>
      </el-card>
    </div>
  </section>
</template>
''', encoding='utf-8')

print('STITCH_STYLE_LABOR_UI_UPDATED')
