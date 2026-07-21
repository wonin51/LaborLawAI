<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Search, View } from '@element-plus/icons-vue'
import { getQaRecords, getQaCitations, normalizeQaHistoryPage } from '../api/qaHistory'

const activeTab = ref('records')

// 问答记录
const recordQueryForm = reactive(createRecordQueryForm())
const recordListLoading = ref(false)
const recordDetailDialogVisible = ref(false)
const recordDetailLoading = ref(false)
const recordDetail = ref(null)
const recordTableRows = ref([])
const recordPagination = reactive({
  pageNo: 1,
  pageSize: 10,
  total: 0,
  pages: 0
})

// 回答引用
const citationQueryForm = reactive(createCitationQueryForm())
const citationListLoading = ref(false)
const citationDetailDialogVisible = ref(false)
const citationDetailLoading = ref(false)
const citationDetail = ref(null)
const citationTableRows = ref([])
const citationPagination = reactive({
  pageNo: 1,
  pageSize: 10,
  total: 0,
  pages: 0
})

const answerStatusOptions = [
  { label: 'GENERATING', value: 'GENERATING' },
  { label: 'COMPLETED', value: 'COMPLETED' },
  { label: 'REFUSED', value: 'REFUSED' },
  { label: 'INSUFFICIENT', value: 'INSUFFICIENT' },
  { label: 'FAILED', value: 'FAILED' }
]

const statusTagTypeMap = {
  GENERATING: 'warning',
  COMPLETED: 'success',
  REFUSED: 'danger',
  INSUFFICIENT: 'info',
  FAILED: 'danger'
}

function createRecordQueryForm() {
  return { answer_status: '', topic_id: '' }
}

function createCitationQueryForm() {
  return { claim_id: '', document_version_id: '', chunk_ref_id: '' }
}

function statusLabel(status) {
  return status || '-'
}

function statusTagType(status) {
  return statusTagTypeMap[status] || 'info'
}

function formatDateTime(value) {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value).replace('T', ' ')
  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).format(date)
}

async function loadRecords(nextPageNo = recordPagination.pageNo, nextPageSize = recordPagination.pageSize) {
  recordListLoading.value = true
  try {
    const params = { ...recordQueryForm, pageNo: nextPageNo, pageSize: nextPageSize }
    if (params.topic_id) params.topic_id = Number(params.topic_id)
    const page = normalizeQaHistoryPage(await getQaRecords(params))
    recordTableRows.value = page.records
    recordPagination.pageNo = page.current || nextPageNo
    recordPagination.pageSize = page.size || nextPageSize
    recordPagination.total = page.total || 0
    recordPagination.pages = page.pages || 0
  } catch (error) {
    ElMessage.error(error?.message || '加载问答记录失败')
  } finally {
    recordListLoading.value = false
  }
}

function handleRecordSearch() {
  loadRecords(1, recordPagination.pageSize)
}

function handleRecordReset() {
  Object.assign(recordQueryForm, createRecordQueryForm())
  loadRecords(1, recordPagination.pageSize)
}

function handleRecordPageChange(pageNo) {
  loadRecords(pageNo, recordPagination.pageSize)
}

function handleRecordSizeChange(pageSize) {
  loadRecords(1, pageSize)
}

async function openRecordDetail(row) {
  recordDetailDialogVisible.value = true
  recordDetailLoading.value = true
  recordDetail.value = row
  recordDetailLoading.value = false
}

async function loadCitations(nextPageNo = citationPagination.pageNo, nextPageSize = citationPagination.pageSize) {
  citationListLoading.value = true
  try {
    const params = { ...citationQueryForm, pageNo: nextPageNo, pageSize: nextPageSize }
    if (params.claim_id) params.claim_id = Number(params.claim_id)
    if (params.document_version_id) params.document_version_id = Number(params.document_version_id)
    if (params.chunk_ref_id) params.chunk_ref_id = Number(params.chunk_ref_id)
    const page = normalizeQaHistoryPage(await getQaCitations(params))
    citationTableRows.value = page.records
    citationPagination.pageNo = page.current || nextPageNo
    citationPagination.pageSize = page.size || nextPageSize
    citationPagination.total = page.total || 0
    citationPagination.pages = page.pages || 0
  } catch (error) {
    ElMessage.error(error?.message || '加载回答引用失败')
  } finally {
    citationListLoading.value = false
  }
}

function handleCitationSearch() {
  loadCitations(1, citationPagination.pageSize)
}

function handleCitationReset() {
  Object.assign(citationQueryForm, createCitationQueryForm())
  loadCitations(1, citationPagination.pageSize)
}

function handleCitationPageChange(pageNo) {
  loadCitations(pageNo, citationPagination.pageSize)
}

function handleCitationSizeChange(pageSize) {
  loadCitations(1, pageSize)
}

async function openCitationDetail(row) {
  citationDetailDialogVisible.value = true
  citationDetailLoading.value = true
  citationDetail.value = row
  citationDetailLoading.value = false
}

onMounted(() => {
  loadRecords()
  loadCitations()
})
</script>

<template>
  <section class="page-template qa-history-page">
    <div class="hero-copy compact">
      <p class="eyebrow">QA HISTORY</p>
      <h2>问答历史</h2>
      <p>查看问答记录和回答引用的法律依据，追溯每一条回答的知识来源。</p>
    </div>

    <el-tabs v-model="activeTab" type="border-card">
      <!-- 问答记录 Tab -->
      <el-tab-pane label="问答记录" name="records">
        <el-card class="paper-panel" shadow="never">
          <template #header>
            <div class="panel-title split-title">
              <span>查询条件</span>
              <el-button :icon="Refresh" @click="handleRecordReset">重置</el-button>
            </div>
          </template>
          <el-form :model="recordQueryForm" inline label-width="120px">
            <el-form-item label="回答状态">
              <el-select v-model="recordQueryForm.answer_status" clearable placeholder="选择回答状态">
                <el-option v-for="item in answerStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="问题分类ID">
              <el-input v-model="recordQueryForm.topic_id" clearable placeholder="输入问题分类ID" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :icon="Search" @click="handleRecordSearch">查询</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card class="paper-panel" shadow="never">
          <template #header>
            <div class="panel-title split-title">
              <span>问答记录列表</span>
              <el-tag effect="plain" type="info">共 {{ recordPagination.total }} 条</el-tag>
            </div>
          </template>
          <div v-loading="recordListLoading">
            <el-table :data="recordTableRows" @row-click="openRecordDetail" stripe empty-text="暂无问答记录">
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="question" label="问题内容" min-width="260" show-overflow-tooltip />
              <el-table-column prop="conclusion_summary" label="结论摘要" min-width="200" show-overflow-tooltip />
              <el-table-column prop="answer_status" label="状态" width="120">
                <template #default="scope">
                  <el-tag :type="statusTagType(scope.row.answer_status)" effect="plain" size="small">{{ statusLabel(scope.row.answer_status) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="completed_at" label="完成时间" width="160">
                <template #default="scope">{{ formatDateTime(scope.row.completed_at) }}</template>
              </el-table-column>
              <el-table-column prop="created_at" label="创建时间" width="160">
                <template #default="scope">{{ formatDateTime(scope.row.created_at) }}</template>
              </el-table-column>
              <el-table-column label="操作" width="100" fixed="right">
                <template #default="scope">
                  <el-button link type="primary" :icon="View" @click="openRecordDetail(scope.row)">详情</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
          <div class="knowledge-pagination">
            <el-pagination
              v-model:current-page="recordPagination.pageNo"
              v-model:page-size="recordPagination.pageSize"
              :page-sizes="[10, 20, 50]"
              :total="recordPagination.total"
              layout="total, sizes, prev, pager, next, jumper"
              background
              @current-change="handleRecordPageChange"
              @size-change="handleRecordSizeChange"
            />
          </div>
        </el-card>
      </el-tab-pane>

      <!-- 回答引用 Tab -->
      <el-tab-pane label="回答引用" name="citations">
        <el-card class="paper-panel" shadow="never">
          <template #header>
            <div class="panel-title split-title">
              <span>查询条件</span>
              <el-button :icon="Refresh" @click="handleCitationReset">重置</el-button>
            </div>
          </template>
          <el-form :model="citationQueryForm" inline label-width="120px">
            <el-form-item label="关键结论ID">
              <el-input v-model="citationQueryForm.claim_id" clearable placeholder="输入关键结论ID" />
            </el-form-item>
            <el-form-item label="文档版本ID">
              <el-input v-model="citationQueryForm.document_version_id" clearable placeholder="输入文档版本ID" />
            </el-form-item>
            <el-form-item label="片段引用ID">
              <el-input v-model="citationQueryForm.chunk_ref_id" clearable placeholder="输入片段引用ID" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :icon="Search" @click="handleCitationSearch">查询</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card class="paper-panel" shadow="never">
          <template #header>
            <div class="panel-title split-title">
              <span>引用依据列表</span>
              <el-tag effect="plain" type="info">共 {{ citationPagination.total }} 条</el-tag>
            </div>
          </template>
          <div v-loading="citationListLoading">
            <el-table :data="citationTableRows" @row-click="openCitationDetail" stripe empty-text="暂无引用依据">
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="claim_id" label="关键结论ID" width="100" />
              <el-table-column prop="source_title_snapshot" label="来源标题" min-width="180" show-overflow-tooltip />
              <el-table-column prop="article_no_snapshot" label="条款号" width="120" />
              <el-table-column prop="relevance_score" label="相关度" width="100">
                <template #default="scope">{{ scope.row.relevance_score ?? '-' }}</template>
              </el-table-column>
              <el-table-column prop="quote_snapshot" label="引用内容" min-width="260" show-overflow-tooltip />
              <el-table-column prop="created_at" label="创建时间" width="160">
                <template #default="scope">{{ formatDateTime(scope.row.created_at) }}</template>
              </el-table-column>
              <el-table-column label="操作" width="100" fixed="right">
                <template #default="scope">
                  <el-button link type="primary" :icon="View" @click="openCitationDetail(scope.row)">详情</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
          <div class="knowledge-pagination">
            <el-pagination
              v-model:current-page="citationPagination.pageNo"
              v-model:page-size="citationPagination.pageSize"
              :page-sizes="[10, 20, 50]"
              :total="citationPagination.total"
              layout="total, sizes, prev, pager, next, jumper"
              background
              @current-change="handleCitationPageChange"
              @size-change="handleCitationSizeChange"
            />
          </div>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 问答记录详情弹窗 -->
    <el-dialog v-model="recordDetailDialogVisible" title="问答记录详情" width="760px" destroy-on-close>
      <div v-loading="recordDetailLoading">
        <el-descriptions v-if="recordDetail" :column="1" border>
          <el-descriptions-item label="记录ID">{{ recordDetail.id }}</el-descriptions-item>
          <el-descriptions-item label="问题内容">{{ recordDetail.question || '-' }}</el-descriptions-item>
          <el-descriptions-item label="结论摘要">{{ recordDetail.conclusion_summary || '-' }}</el-descriptions-item>
          <el-descriptions-item label="回答状态">
            <el-tag :type="statusTagType(recordDetail.answer_status)" effect="plain" size="small">{{ statusLabel(recordDetail.answer_status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="维权流程">{{ recordDetail.action_steps || '-' }}</el-descriptions-item>
          <el-descriptions-item label="证据清单">{{ recordDetail.evidence_checklist || '-' }}</el-descriptions-item>
          <el-descriptions-item label="注意事项">{{ recordDetail.cautions || '-' }}</el-descriptions-item>
          <el-descriptions-item label="引用覆盖率">{{ recordDetail.citation_coverage ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="完成时间">{{ formatDateTime(recordDetail.completed_at) }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDateTime(recordDetail.created_at) }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>

    <!-- 引用依据详情弹窗 -->
    <el-dialog v-model="citationDetailDialogVisible" title="引用依据详情" width="760px" destroy-on-close>
      <div v-loading="citationDetailLoading">
        <el-descriptions v-if="citationDetail" :column="1" border>
          <el-descriptions-item label="引用ID">{{ citationDetail.id }}</el-descriptions-item>
          <el-descriptions-item label="关键结论ID">{{ citationDetail.claim_id }}</el-descriptions-item>
          <el-descriptions-item label="文档版本ID">{{ citationDetail.document_version_id }}</el-descriptions-item>
          <el-descriptions-item label="片段引用ID">{{ citationDetail.chunk_ref_id }}</el-descriptions-item>
          <el-descriptions-item label="来源标题">{{ citationDetail.source_title_snapshot || '-' }}</el-descriptions-item>
          <el-descriptions-item label="来源标签">{{ citationDetail.source_label_snapshot || '-' }}</el-descriptions-item>
          <el-descriptions-item label="章节快照">{{ citationDetail.section_snapshot || '-' }}</el-descriptions-item>
          <el-descriptions-item label="条款号">{{ citationDetail.article_no_snapshot || '-' }}</el-descriptions-item>
          <el-descriptions-item label="来源链接">{{ citationDetail.source_url_snapshot || '-' }}</el-descriptions-item>
          <el-descriptions-item label="引用内容">{{ citationDetail.quote_snapshot || '-' }}</el-descriptions-item>
          <el-descriptions-item label="相关度分数">{{ citationDetail.relevance_score ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="检索排名">{{ citationDetail.retrieval_rank ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDateTime(citationDetail.created_at) }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </section>
</template>

<style scoped>
.qa-history-page .el-tabs {
  margin-top: 16px;
}
.qa-history-page .paper-panel {
  margin-bottom: 16px;
}
.knowledge-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
