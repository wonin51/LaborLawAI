<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  ChatDotRound,
  Collection,
  DataAnalysis,
  Document,
  DocumentChecked,
  Files,
  InfoFilled,
  Link,
  Monitor,
  Plus,
  Refresh,
  Search,
  View
} from '@element-plus/icons-vue'
import { configuredBaseUrl } from './api/http'
import { getHealthStatus, pingDatabase } from './api/health'
import {
  createLegalDocument,
  getLegalDocumentDetail,
  getLegalDocuments
} from './api/legalDocuments'

const activeView = ref('documents')
const health = ref({ status: 'checking', text: '正在检查后端服务' })
const dbHealth = ref({ status: 'checking', text: '正在检查数据库连接' })

const navItems = [
  { key: 'documents', label: '法律文档', hint: '任务05页面', icon: Files },
  { key: 'consult', label: '智能问答', hint: '依据溯源', icon: ChatDotRound },
  { key: 'review', label: '合同审查', hint: '条款风险', icon: DocumentChecked },
  { key: 'sources', label: '依据收藏', hint: '引用清单', icon: Collection },
  { key: 'help', label: '使用边界', hint: '免责声明', icon: InfoFilled }
]

// Task 05: 查询区状态，字段按法律知识库语义改造。
const queryForm = reactive({
  title: '',
  sourceType: '',
  jurisdiction: '',
  effectiveStatus: '',
  pageNo: 1,
  pageSize: 10
})

const sourceTypeOptions = ['法律法规', '司法解释', '人社政策', '项目知识']
const statusOptions = ['现行有效', '已启用', '草稿', '待复核', '已失效']
const jurisdictionOptions = ['全国', '项目资料', '北京', '上海', '广东', '浙江']

const tableLoading = ref(false)
const documentRows = ref([])
const total = ref(0)

const createDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const createFormRef = ref()
const detailLoading = ref(false)
const selectedDocument = ref(null)

// Task 05: 新增文档弹窗表单。
const createForm = reactive({
  title: '',
  sourceType: '法律法规',
  jurisdiction: '全国',
  publishDate: '',
  effectiveStatus: '草稿',
  sourceUrl: '',
  summary: '',
  content: ''
})

const createRules = {
  title: [{ required: true, message: '请输入文档标题', trigger: 'blur' }],
  sourceType: [{ required: true, message: '请选择来源类型', trigger: 'change' }],
  jurisdiction: [{ required: true, message: '请选择适用地区', trigger: 'change' }],
  summary: [{ required: true, message: '请输入文档摘要', trigger: 'blur' }],
  content: [{ required: true, message: '请输入正文内容', trigger: 'blur' }]
}

const healthType = computed(() => {
  if (health.value.status === 'ok') return 'success'
  if (health.value.status === 'checking') return 'warning'
  return 'danger'
})

const dbHealthType = computed(() => {
  if (dbHealth.value.status === 'ok') return 'success'
  if (dbHealth.value.status === 'checking') return 'warning'
  return 'danger'
})

function unwrapApiResponse(response) {
  if (!response || response.code !== 0) {
    throw new Error(response?.message || '接口请求失败')
  }
  return response.data
}

async function refreshHealth() {
  health.value = { status: 'checking', text: '正在检查后端服务' }
  try {
    const data = unwrapApiResponse(await getHealthStatus())
    health.value = data?.status === 'ok'
      ? { status: 'ok', text: '后端服务已连接' }
      : { status: 'error', text: '后端状态异常' }
  } catch (error) {
    health.value = { status: 'error', text: '后端服务未连接' }
  }
}

async function refreshDbHealth() {
  dbHealth.value = { status: 'checking', text: '正在检查数据库连接' }
  try {
    const data = unwrapApiResponse(await pingDatabase())
    dbHealth.value = data?.status === 'ok'
      ? { status: 'ok', text: `数据库已连接 ${data.timestamp}` }
      : { status: 'error', text: '数据库状态异常' }
  } catch (error) {
    dbHealth.value = { status: 'error', text: '数据库未连接' }
  }
}

async function loadDocuments() {
  tableLoading.value = true
  try {
    const page = unwrapApiResponse(await getLegalDocuments({ ...queryForm }))
    documentRows.value = page?.list || []
    total.value = page?.total || 0
    queryForm.pageNo = page?.pageNo || queryForm.pageNo
    queryForm.pageSize = page?.pageSize || queryForm.pageSize
  } catch (error) {
    ElMessage.error(error.message || '法律文档列表加载失败')
  } finally {
    tableLoading.value = false
  }
}

function searchDocuments() {
  queryForm.pageNo = 1
  loadDocuments()
}

function resetQuery() {
  queryForm.title = ''
  queryForm.sourceType = ''
  queryForm.jurisdiction = ''
  queryForm.effectiveStatus = ''
  queryForm.pageNo = 1
  loadDocuments()
}

function openCreateDialog() {
  createDialogVisible.value = true
}

function resetCreateForm() {
  createForm.title = ''
  createForm.sourceType = '法律法规'
  createForm.jurisdiction = '全国'
  createForm.publishDate = ''
  createForm.effectiveStatus = '草稿'
  createForm.sourceUrl = ''
  createForm.summary = ''
  createForm.content = ''
}

async function submitCreate() {
  if (!createFormRef.value) return
  await createFormRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      unwrapApiResponse(await createLegalDocument({ ...createForm }))
      ElMessage.success('法律文档新增成功')
      createDialogVisible.value = false
      resetCreateForm()
      searchDocuments()
    } catch (error) {
      ElMessage.error(error.message || '新增法律文档失败')
    }
  })
}

async function openDetail(row) {
  detailDialogVisible.value = true
  detailLoading.value = true
  selectedDocument.value = null
  try {
    selectedDocument.value = unwrapApiResponse(await getLegalDocumentDetail(row.id))
  } catch (error) {
    ElMessage.error(error.message || '文档详情加载失败')
    detailDialogVisible.value = false
  } finally {
    detailLoading.value = false
  }
}

function handlePageChange(pageNo) {
  queryForm.pageNo = pageNo
  loadDocuments()
}

function handleSizeChange(pageSize) {
  queryForm.pageSize = pageSize
  queryForm.pageNo = 1
  loadDocuments()
}

function statusTagType(status) {
  if (status === '现行有效' || status === '已启用') return 'success'
  if (status === '待复核' || status === '草稿') return 'warning'
  if (status === '已失效') return 'danger'
  return 'info'
}

onMounted(() => {
  refreshHealth()
  refreshDbHealth()
  loadDocuments()
})
</script>

<template>
  <div class="app-shell">
    <aside class="side-nav">
      <div class="brand">
        <div class="brand-mark">法</div>
        <div>
          <h1>劳动合同法律助手</h1>
          <p>Legal Knowledge Desk</p>
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
          <span>
            <strong>{{ item.label }}</strong>
            <small>{{ item.hint }}</small>
          </span>
        </button>
      </nav>

      <div class="side-note">
        <strong>接口联调提示</strong>
        <p>本页通过 Vite 代理调用 `/api/legal-docs`、`/api/health` 与 `/api/db/ping`，避免本地开发 CORS 问题。</p>
      </div>
    </aside>

    <main class="workspace">
      <header class="topbar">
        <div>
          <p class="eyebrow">TASK 05 · 法律文档前端页面</p>
          <h2>{{ activeView === 'documents' ? '法律文档管理与接口联调' : '劳动合同法律助手' }}</h2>
        </div>
        <div class="status-cluster">
          <el-tag :type="healthType" effect="light">{{ health.text }}</el-tag>
          <el-tag :type="dbHealthType" effect="light">{{ dbHealth.text }}</el-tag>
          <el-button :icon="Monitor" @click="refreshHealth">检查后端</el-button>
          <el-button :icon="Refresh" @click="refreshDbHealth">检查数据库</el-button>
        </div>
      </header>

      <!-- Task 05: 法律文档页面，迁移自课程“维修工单前端页面”任务要求。 -->
      <section v-if="activeView === 'documents'" class="document-page">
        <section class="panel intro-panel">
          <div>
            <el-tag type="success" effect="plain">Task 05 新增</el-tag>
            <h3>查询、入库与查看法律文档</h3>
            <p>面向劳动合同法律知识库，支持按标题、来源类型、适用地区和时效状态筛选；新增后会刷新列表，详情弹窗展示来源、摘要和正文。</p>
          </div>
          <div class="api-card">
            <span>API</span>
            <strong>{{ configuredBaseUrl }}/api/legal-docs</strong>
          </div>
        </section>

        <section class="panel query-panel">
          <div class="panel-heading">
            <div>
              <p class="eyebrow">QUERY AREA</p>
              <h3>查询区</h3>
            </div>
            <el-button type="primary" :icon="Plus" @click="openCreateDialog">新增文档</el-button>
          </div>

          <el-form :model="queryForm" label-width="84px" class="query-form">
            <el-form-item label="文档标题">
              <el-input v-model="queryForm.title" clearable placeholder="如：劳动合同法、未签合同" />
            </el-form-item>
            <el-form-item label="来源类型">
              <el-select v-model="queryForm.sourceType" clearable placeholder="全部类型">
                <el-option v-for="item in sourceTypeOptions" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
            <el-form-item label="适用地区">
              <el-select v-model="queryForm.jurisdiction" clearable placeholder="全部地区">
                <el-option v-for="item in jurisdictionOptions" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
            <el-form-item label="时效状态">
              <el-select v-model="queryForm.effectiveStatus" clearable placeholder="全部状态">
                <el-option v-for="item in statusOptions" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
            <div class="query-actions">
              <el-button type="primary" :icon="Search" @click="searchDocuments">查询</el-button>
              <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
            </div>
          </el-form>
        </section>

        <section class="panel table-panel">
          <div class="panel-heading">
            <div>
              <p class="eyebrow">LIST AREA</p>
              <h3>法律文档列表</h3>
            </div>
            <span class="list-count">共 {{ total }} 条</span>
          </div>

          <el-table v-loading="tableLoading" :data="documentRows" stripe class="doc-table">
            <el-table-column prop="id" label="编号" width="80" />
            <el-table-column prop="title" label="文档标题" min-width="260" show-overflow-tooltip />
            <el-table-column prop="sourceType" label="来源类型" width="110" />
            <el-table-column prop="jurisdiction" label="适用地区" width="110" />
            <el-table-column prop="publishDate" label="发布日期" width="130" />
            <el-table-column label="时效状态" width="120">
              <template #default="{ row }">
                <el-tag :type="statusTagType(row.effectiveStatus)" effect="plain">{{ row.effectiveStatus }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="summary" label="摘要" min-width="280" show-overflow-tooltip />
            <el-table-column label="操作" width="110" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" :icon="View" @click="openDetail(row)">查看详情</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-row">
            <el-pagination
              v-model:current-page="queryForm.pageNo"
              v-model:page-size="queryForm.pageSize"
              :page-sizes="[5, 10, 20]"
              :total="total"
              layout="total, sizes, prev, pager, next"
              @current-change="handlePageChange"
              @size-change="handleSizeChange"
            />
          </div>
        </section>
      </section>

      <section v-else class="panel placeholder-panel">
        <el-icon><DataAnalysis /></el-icon>
        <h3>该模块已预留</h3>
        <p>当前任务重点是“法律文档前端页面”。问答、合同审查、收藏和帮助模块后续接入真实 RAG 接口后继续扩展。</p>
      </section>
    </main>

    <el-dialog v-model="createDialogVisible" title="新增法律文档" width="720px" @closed="resetCreateForm">
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="96px">
        <el-form-item label="文档标题" prop="title">
          <el-input v-model="createForm.title" placeholder="请输入法规、政策或项目知识标题" />
        </el-form-item>
        <div class="dialog-grid">
          <el-form-item label="来源类型" prop="sourceType">
            <el-select v-model="createForm.sourceType">
              <el-option v-for="item in sourceTypeOptions" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
          <el-form-item label="适用地区" prop="jurisdiction">
            <el-select v-model="createForm.jurisdiction">
              <el-option v-for="item in jurisdictionOptions" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
          <el-form-item label="发布日期">
            <el-date-picker
              v-model="createForm.publishDate"
              type="date"
              value-format="YYYY-MM-DD"
              placeholder="选择日期"
            />
          </el-form-item>
          <el-form-item label="时效状态">
            <el-select v-model="createForm.effectiveStatus">
              <el-option v-for="item in statusOptions" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
        </div>
        <el-form-item label="来源链接">
          <el-input v-model="createForm.sourceUrl" placeholder="可填写官方网站、法规库或项目资料链接" />
        </el-form-item>
        <el-form-item label="摘要" prop="summary">
          <el-input v-model="createForm.summary" type="textarea" :rows="3" placeholder="概括该文档适用场景和核心内容" />
        </el-form-item>
        <el-form-item label="正文" prop="content">
          <el-input v-model="createForm.content" type="textarea" :rows="7" placeholder="粘贴条文、政策说明或知识库正文片段" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCreate">保存并刷新列表</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialogVisible" title="法律文档详情" width="760px">
      <div v-loading="detailLoading" class="detail-dialog" v-if="selectedDocument">
        <div class="detail-title-row">
          <div>
            <h3>{{ selectedDocument.title }}</h3>
            <p>{{ selectedDocument.summary }}</p>
          </div>
          <el-tag :type="statusTagType(selectedDocument.effectiveStatus)" effect="plain">{{ selectedDocument.effectiveStatus }}</el-tag>
        </div>

        <div class="detail-meta-grid">
          <div><span>来源类型</span><strong>{{ selectedDocument.sourceType }}</strong></div>
          <div><span>适用地区</span><strong>{{ selectedDocument.jurisdiction }}</strong></div>
          <div><span>发布日期</span><strong>{{ selectedDocument.publishDate || '未填写' }}</strong></div>
          <div><span>最近更新</span><strong>{{ selectedDocument.updatedAt }}</strong></div>
        </div>

        <div class="source-link" v-if="selectedDocument.sourceUrl">
          <el-icon><Link /></el-icon>
          <span>{{ selectedDocument.sourceUrl }}</span>
        </div>

        <section class="content-reader">
          <div class="reader-heading">
            <el-icon><Document /></el-icon>
            <strong>正文内容</strong>
          </div>
          <p>{{ selectedDocument.content }}</p>
        </section>
      </div>
    </el-dialog>
  </div>
</template>
