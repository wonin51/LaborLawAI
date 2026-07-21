<script setup>
import { computed, onMounted, ref } from 'vue'
import {
  ChatDotRound,
  Collection,
  DocumentChecked,
  FolderOpened,
  Memo,
  Monitor,
  Search,
  Warning
} from '@element-plus/icons-vue'
import { getHealthStatus } from './api/http'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import KnowledgeDocsView from './views/KnowledgeDocsView.vue'
import KnowledgeSourcesView from './views/KnowledgeSourcesView.vue'
import QaHistoryView from './views/QaHistoryView.vue'

const activeView = ref('consult')
const health = ref({ status: 'checking', text: '正在检查后端服务' })
const question = ref('入职两个月没签劳动合同，我能要求赔偿吗？')
const reviewText = ref('合同约定：乙方试用期为六个月，试用期工资为正式工资的70%。甲方可根据经营需要随时解除劳动合同。')

const navItems = [
  { key: 'consult', label: '智能咨询', icon: ChatDotRound },
  { key: 'review', label: '合同审查', icon: DocumentChecked },
  { key: 'sources', label: '法律依据库', icon: Collection },
  { key: 'history', label: '问答历史', icon: Memo },
  { key: 'feedback', label: '反馈处理', icon: Warning },
  { key: 'admin', label: '知识库管理', icon: FolderOpened }
]

const quickQuestions = ['未签劳动合同', '试用期解除', '加班工资', '工资拖欠', '经济补偿', '社保公积金', '劳动仲裁', '合同条款风险']

const sourceCards = [
  {
    id: '依据 1',
    title: '中华人民共和国劳动合同法',
    article: '第十条、第八十二条',
    type: '法律法规',
    status: '现行有效',
    summary: '建立劳动关系，应当订立书面劳动合同；用人单位超过一个月不满一年未与劳动者订立书面劳动合同的，应向劳动者每月支付二倍工资。',
    date: '2012-12-28 修正'
  },
  {
    id: '依据 2',
    title: '劳动争议调解仲裁法',
    article: '第二十七条',
    type: '法律法规',
    status: '现行有效',
    summary: '劳动争议申请仲裁的时效期间为一年，仲裁时效期间从当事人知道或者应当知道其权利被侵害之日起计算。',
    date: '2007-12-29 发布'
  },
  {
    id: '依据 3',
    title: '人社部门劳动保障监察指引',
    article: '投诉举报、工资支付、用工资料',
    type: '人社政策',
    status: '需核对地方政策',
    summary: '发生工资拖欠、未签合同等争议时，可先保存劳动关系和工资支付证据，再选择投诉或仲裁路径。',
    date: '示例资料'
  }
]

const processSteps = [
  { title: '沟通确认', detail: '先与用人单位确认未签合同原因，保留沟通记录。' },
  { title: '保存证据', detail: '整理工资流水、考勤记录、录用通知、聊天记录和工作成果。' },
  { title: '投诉举报', detail: '可向当地劳动保障监察部门反映用工问题。' },
  { title: '劳动仲裁', detail: '准备仲裁申请书和证据材料，关注一年仲裁时效。' },
  { title: '专业咨询', detail: '金额较大或证据复杂时，建议咨询律师或法律援助机构。' }
]

const evidenceList = ['劳动合同或录用通知', '工资流水', '考勤记录', '加班通知', '聊天记录', '工作成果', '社保缴纳记录', '离职证明或解除通知']
const healthType = computed(() => {
  if (health.value.status === 'ok') return 'success'
  if (health.value.status === 'checking') return 'warning'
  return 'danger'
})

async function checkBackend() {
  health.value = { status: 'checking', text: '正在检查后端服务' }
  try {
    const data = await getHealthStatus()
    health.value = data?.status === 'ok'
      ? { status: 'ok', text: '后端服务已连接' }
      : { status: 'error', text: '后端状态异常' }
  } catch (error) {
    health.value = { status: 'error', text: '后端服务未连接' }
  }
}

function useQuickQuestion(item) {
  question.value = item === '未签劳动合同'
    ? '入职两个月没签劳动合同，我能要求赔偿吗？'
    : `请分析：${item}相关问题应该如何处理？`
}

onMounted(checkBackend)
</script>

<template>
  <el-config-provider :locale="zhCn">
  <div class="app-shell">
    <aside class="side-nav">
      <div class="brand">
        <div class="brand-mark">法</div>
        <div>
          <h1>劳动合同法律助手</h1>
          <p>RAG Legal Desk</p>
        </div>
      </div>

      <nav class="nav-list" aria-label="主导航">
        <button
          v-for="item in navItems"
          :key="item.key"
          class="nav-item"
          :class="{ active: activeView === item.key }"
          type="button"
          @click="activeView = item.key"
        >
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
        </button>
      </nav>

      <div class="side-note">
        <strong>咨询边界</strong>
        <p>本系统提供法律信息检索与咨询辅助，不构成正式法律意见。</p>
      </div>
    </aside>

    <main class="workspace">
      <header class="topbar">
        <div>
          <p class="eyebrow">法律依据可追溯 · 维权路径可执行</p>
          <h2>劳动合同智能咨询工作台</h2>
        </div>
        <div class="status-cluster">
          <el-tag :type="healthType" effect="light">{{ health.text }}</el-tag>
          <el-button :icon="Monitor" @click="checkBackend">重新检查</el-button>
        </div>
      </header>

      <section v-if="activeView === 'consult'" class="consult-grid">
        <div class="main-column">
          <section class="panel ask-panel">
            <div class="panel-heading">
              <div>
                <p class="eyebrow">智能咨询</p>
                <h3>先看结论，再核对依据</h3>
              </div>
              <el-tag type="warning">演示问题</el-tag>
            </div>
            <el-input
              v-model="question"
              type="textarea"
              :rows="4"
              resize="none"
              placeholder="例如：入职两个月没签劳动合同，我能要求赔偿吗？"
            />
            <div class="quick-tags">
              <el-button
                v-for="item in quickQuestions"
                :key="item"
                round
                size="small"
                @click="useQuickQuestion(item)"
              >
                {{ item }}
              </el-button>
            </div>
          </section>

          <section class="panel answer-panel">
            <div class="answer-head">
              <div>
                <p class="eyebrow">结构化回答</p>
                <h3>入职两个月未签劳动合同的处理建议</h3>
              </div>
              <div class="answer-tags">
                <el-tag>未签劳动合同</el-tag>
                <el-tag type="warning">风险等级：中</el-tag>
                <el-tag type="success">依据较充分</el-tag>
              </div>
            </div>

            <div class="answer-block">
              <h4>结论摘要</h4>
              <p>如果已经建立劳动关系但入职两个月仍未签书面劳动合同，通常可以主张用人单位补签合同，并结合实际入职时间、工资流水和考勤记录，评估二倍工资差额等请求。<a href="#source-1">[依据 1]</a></p>
            </div>

            <div class="answer-block">
              <h4>法律依据</h4>
              <p>当前示例主要关联《劳动合同法》关于书面劳动合同订立和未签合同责任的条款，同时需要结合仲裁时效判断是否仍可主张权利。<a href="#source-2">[依据 2]</a></p>
            </div>

            <div class="answer-block">
              <h4>注意事项</h4>
              <p>请避免在系统中输入完整身份证号、银行卡号、家庭住址等敏感信息。复杂争议或金额较大时，建议咨询当地劳动保障部门、法律援助机构或律师。</p>
            </div>

            <div class="answer-actions">
              <el-button type="primary">继续追问</el-button>
              <el-button>复制答案</el-button>
              <el-button>保存会话</el-button>
              <el-button>反馈有误</el-button>
            </div>
          </section>

          <section class="panel process-panel">
            <div class="panel-heading">
              <div>
                <p class="eyebrow">可执行下一步</p>
                <h3>维权流程与证据清单</h3>
              </div>
            </div>
            <el-steps :active="2" finish-status="success" align-center>
              <el-step v-for="step in processSteps" :key="step.title" :title="step.title" :description="step.detail" />
            </el-steps>
            <div class="evidence-grid">
              <label v-for="item in evidenceList" :key="item" class="evidence-item">
                <input type="checkbox" />
                <span>{{ item }}</span>
              </label>
            </div>
          </section>
        </div>

        <aside class="right-column">
          <section class="panel source-panel">
            <div class="panel-heading">
              <div>
                <p class="eyebrow">引用溯源</p>
                <h3>法律依据</h3>
              </div>
              <el-button :icon="Search" circle aria-label="检索依据" />
            </div>
            <article
              v-for="source in sourceCards"
              :id="source.id === '依据 1' ? 'source-1' : source.id === '依据 2' ? 'source-2' : undefined"
              :key="source.id"
              class="source-card"
            >
              <div class="source-meta">
                <el-tag size="small" type="success">{{ source.status }}</el-tag>
                <span>{{ source.type }}</span>
              </div>
              <h4>{{ source.id }} · {{ source.title }}</h4>
              <p class="article-code">{{ source.article }}</p>
              <p>{{ source.summary }}</p>
              <span class="source-date">{{ source.date }}</span>
            </article>
          </section>
        </aside>
      </section>

      <section v-else-if="activeView === 'review'" class="split-view">
        <section class="panel">
          <p class="eyebrow">合同条款审查</p>
          <h3>粘贴条款，辅助识别明显风险点</h3>
          <el-input v-model="reviewText" type="textarea" :rows="12" resize="none" />
        </section>
        <section class="panel review-result">
          <p class="eyebrow">审查结果</p>
          <h3>总体风险等级：中高</h3>
          <div class="risk-card">
            <el-tag type="danger">试用期风险</el-tag>
            <p>六个月试用期需要结合劳动合同期限判断是否合法，试用期工资比例也应核对最低工资和约定工资要求。</p>
          </div>
          <div class="risk-card">
            <el-tag type="warning">解除条款风险</el-tag>
            <p>“随时解除劳动合同”表述过宽，建议改为符合法定解除条件的具体场景。</p>
          </div>
        </section>
      </section>

      <KnowledgeSourcesView v-else-if="activeView === 'sources'" />

      <QaHistoryView v-else-if="activeView === 'history'" />

      <KnowledgeDocsView v-else-if="activeView === 'admin'" />

      <section v-else class="panel placeholder-panel">
        <p class="eyebrow">模块预览</p>
        <h3>{{ navItems.find((item) => item.key === activeView)?.label }}</h3>
        <p>该模块已预留入口，后续接入真实接口后可继续扩展，不影响当前前后端健康检查和演示链路。</p>
      </section>
    </main>
  </div>
  </el-config-provider>
</template>
