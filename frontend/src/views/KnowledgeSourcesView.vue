<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Search, View } from '@element-plus/icons-vue'
import { getKnowledgeDocuments, getKnowledgeChunkRefs, normalizeKnowledgeSourcePage } from '../api/knowledgeSources'

const activeTab = ref('documents')

// 知识文档
const docQueryForm = reactive(createDocQueryForm())
const docListLoading = ref(false)
const docDetailDialogVisible = ref(false)
const docDetailLoading = ref(false)
const docDetail = ref(null)
const docTableRows = ref([])
const docPagination = reactive({
  pageNo: 1,
  pageSize: 10,
  total: 0,
  pages: 0
})

// 知识分片
const chunkQueryForm = reactive(createChunkQueryForm())
const chunkListLoading = ref(false)
const chunkDetailDialogVisible = ref(false)
const chunkDetailLoading = ref(false)
const chunkDetail = ref(null)
const chunkTableRows = ref([])
const chunkPagination = reactive({
  pageNo: 1,
  pageSize: 10,
  total: 0,
  pages: 0
})

const documentTypeOptions = [
  { label: 'LAW', value: 'LAW' },
  { label: 'JUDICIAL_INTERPRETATION', value: 'JUDICIAL_INTERPRETATION' },
  { label: 'POLICY', value: 'POLICY' },
  { label: 'FAQ', value: 'FAQ' },
  { label: 'SAMPLE_CONTRACT', value: 'SAMPLE_CONTRACT' },
  { label: 'USER_CONTRIBUTION', value: 'USER_CONTRIBUTION' }
]

const statusOptions = [
  { label: 'DRAFT', value: 'DRAFT' },
  { label: 'PUBLISHED', value: 'PUBLISHED' },
  { label: 'DISABLED', value: 'DISABLED' }
]

const chunkStatusOptions = [
  { label: 'ACTIVE', value: 'ACTIVE' },
  { label: 'SUPERSEDED', value: 'SUPERSEDED' },
  { label: 'DELETED', value: 'DELETED' }
]

const statusTagTypeMap = {
  DRAFT: 'info',
  PUBLISHED: 'success',
  DISABLED: 'danger',
  ACTIVE: 'success',
  SUPERSEDED: 'warning',
  DELETED: 'info'
}

function createDocQueryForm() {
  return { title: '', document_type: '', status: '' }
}

function createChunkQueryForm() {
  return { document_version_id: '', status: '', chunk_key: '', section_path: '' }
}

function statusLabel(status) {
  return status || '-'
}

function statusTagType(status) {
  return statusTagTypeMap[status] || 'info'
}

function documentTypeLabel(type) {
  return type || '-'
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

async function loadDocuments(nextPageNo = docPagination.pageNo, nextPageSize = docPagination.pageSize) {
  docListLoading.value = true
  try {
    const page = normalizeKnowledgeSourcePage(await getKnowledgeDocuments({
      ...docQueryForm,
      pageNo: nextPageNo,
      pageSize: nextPageSize
    }))
    docTableRows.value = page.records
    docPagination.pageNo = page.current || nextPageNo
    docPagination.pageSize = page.size || nextPageSize
    docPagination.total = page.total || 0
    docPagination.pages = page.pages || 0
  } catch (error) {
    ElMessage.error(error?.message || '加载知识文档失败')
  } finally {
    docListLoading.value = false
  }
}

function handleDocSearch() {
  loadDocuments(1, docPagination.pageSize)
}

function handleDocReset() {
  Object.assign(docQueryForm, createDocQueryForm())
  loadDocuments(1, docPagination.pageSize)
}

function handleDocPageChange(pageNo) {
  loadDocuments(pageNo, docPagination.pageSize)
}

function handleDocSizeChange(pageSize) {
  loadDocuments(1, pageSize)
}

async function openDocDetail(row) {
  docDetailDialogVisible.value = true
  docDetailLoading.value = true
  docDetail.value = row
  docDetailLoading.value = false
}

async function loadChunks(nextPageNo = chunkPagination.pageNo, nextPageSize = chunkPagination.pageSize) {
  chunkListLoading.value = true
  try {
    const params = { ...chunkQueryForm, pageNo: nextPageNo, pageSize: nextPageSize }
    if (params.document_version_id) params.document_version_id = Number(params.document_version_id)
    const page = normalizeKnowledgeSourcePage(await getKnowledgeChunkRefs(params))
    chunkTableRows.value = page.records
    chunkPagination.pageNo = page.current || nextPageNo
    chunkPagination.pageSize = page.size || nextPageSize
    chunkPagination.total = page.total || 0
    chunkPagination.pages = page.pages || 0
  } catch (error) {
    ElMessage.error(error?.message || '加载知识分片失败')
  } finally {
    chunkListLoading.value = false
  }
}

function handleChunkSearch() {
  loadChunks(1, chunkPagination.pageSize)
}

function handleChunkReset() {
  Object.assign(chunkQueryForm, createChunkQueryForm())
  loadChunks(1, chunkPagination.pageSize)
}

function handleChunkPageChange(pageNo) {
  loadChunks(pageNo, chunkPagination.pageSize)
}

function handleChunkSizeChange(pageSize) {
  loadChunks(1, pageSize)
}

async function openChunkDetail(row) {
  chunkDetailDialogVisible.value = true
  chunkDetailLoading.value = true
  chunkDetail.value = row
  chunkDetailLoading.value = false
}

onMounted(() => {
  loadDocuments()
  loadChunks()
})
</script>

<template>
  <section class="page-template knowledge-sources-page">
    <div class="hero-copy compact">
      <p class="eyebrow">LEGAL SOURCES</p>
      <h2>法律依据库</h2>
      <p>查询法规、司法解释、政策文档及其知识分片，确保法律依据可追溯。</p>
    </div>

    <el-tabs v-model="activeTab" type="border-card">
      <!-- 知识文档 Tab -->
      <el-tab-pane label="知识文档" name="documents">
        <el-card class="paper-panel" shadow="never">
          <template #header>
            <div class="panel-title split-title">
              <span>查询条件</span>
              <el-button :icon="Refresh" @click="handleDocReset">重置</el-button>
            </div>
          </template>
          <el-form :model="docQueryForm" inline label-width="120px">
            <el-form-item label="标题">
              <el-input v-model="docQueryForm.title" clearable placeholder="输入文档标题" />
            </el-form-item>
            <el-form-item label="文档类型">
              <el-select v-model="docQueryForm.document_type" clearable placeholder="选择文档类型">
                <el-option v-for="item in documentTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="docQueryForm.status" clearable placeholder="选择状态">
                <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :icon="Search" @click="handleDocSearch">查询</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card class="paper-panel" shadow="never">
          <template #header>
            <div class="panel-title split-title">
              <span>文档列表</span>
              <el-tag effect="plain" type="info">共 {{ docPagination.total }} 条</el-tag>
            </div>
          </template>
          <div v-loading="docListLoading">
            <el-table :data="docTableRows" @row-click="openDocDetail" stripe empty-text="暂无文档">
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="title" label="文档标题" min-width="200" show-overflow-tooltip />
              <el-table-column prop="document_type" label="文档类型" width="140">
                <template #default="scope">{{ documentTypeLabel(scope.row.document_type) }}</template>
              </el-table-column>
              <el-table-column prop="issuing_authority" label="发布机构" min-width="160" show-overflow-tooltip />
              <el-table-column prop="status" label="状态" width="100">
                <template #default="scope">
                  <el-tag :type="statusTagType(scope.row.status)" effect="plain" size="small">{{ statusLabel(scope.row.status) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="created_at" label="创建时间" width="160">
                <template #default="scope">{{ formatDateTime(scope.row.created_at) }}</template>
              </el-table-column>
              <el-table-column label="操作" width="100" fixed="right">
                <template #default="scope">
                  <el-button link type="primary" :icon="View" @click="openDocDetail(scope.row)">详情</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
          <div class="knowledge-pagination">
            <el-pagination
              v-model:current-page="docPagination.pageNo"
              v-model:page-size="docPagination.pageSize"
              :page-sizes="[10, 20, 50]"
              :total="docPagination.total"
              layout="total, sizes, prev, pager, next, jumper"
              background
              @current-change="handleDocPageChange"
              @size-change="handleDocSizeChange"
            />
          </div>
        </el-card>
      </el-tab-pane>

      <!-- 知识分片 Tab -->
      <el-tab-pane label="知识分片" name="chunks">
        <el-card class="paper-panel" shadow="never">
          <template #header>
            <div class="panel-title split-title">
              <span>查询条件</span>
              <el-button :icon="Refresh" @click="handleChunkReset">重置</el-button>
            </div>
          </template>
          <el-form :model="chunkQueryForm" inline label-width="120px">
            <el-form-item label="文档版本ID">
              <el-input v-model="chunkQueryForm.document_version_id" clearable placeholder="输入文档版本ID" />
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="chunkQueryForm.status" clearable placeholder="选择状态">
                <el-option v-for="item in chunkStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="片段键">
              <el-input v-model="chunkQueryForm.chunk_key" clearable placeholder="输入片段键" />
            </el-form-item>
            <el-form-item label="章节路径">
              <el-input v-model="chunkQueryForm.section_path" clearable placeholder="输入章节路径" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :icon="Search" @click="handleChunkSearch">查询</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card class="paper-panel" shadow="never">
          <template #header>
            <div class="panel-title split-title">
              <span>分片列表</span>
              <el-tag effect="plain" type="info">共 {{ chunkPagination.total }} 条</el-tag>
            </div>
          </template>
          <div v-loading="chunkListLoading">
            <el-table :data="chunkTableRows" @row-click="openChunkDetail" stripe empty-text="暂无分片">
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="chunk_key" label="片段键" min-width="180" show-overflow-tooltip />
              <el-table-column prop="document_version_id" label="文档版本ID" width="120" />
              <el-table-column prop="chunk_no" label="片段序号" width="100" />
              <el-table-column prop="section_path" label="章节路径" min-width="160" show-overflow-tooltip />
              <el-table-column prop="article_no" label="条款号" width="120" />
              <el-table-column prop="token_count" label="Token数" width="100" />
              <el-table-column prop="status" label="状态" width="100">
                <template #default="scope">
                  <el-tag :type="statusTagType(scope.row.status)" effect="plain" size="small">{{ statusLabel(scope.row.status) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100" fixed="right">
                <template #default="scope">
                  <el-button link type="primary" :icon="View" @click="openChunkDetail(scope.row)">详情</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
          <div class="knowledge-pagination">
            <el-pagination
              v-model:current-page="chunkPagination.pageNo"
              v-model:page-size="chunkPagination.pageSize"
              :page-sizes="[10, 20, 50]"
              :total="chunkPagination.total"
              layout="total, sizes, prev, pager, next, jumper"
              background
              @current-change="handleChunkPageChange"
              @size-change="handleChunkSizeChange"
            />
          </div>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 文档详情弹窗 -->
    <el-dialog v-model="docDetailDialogVisible" title="文档详情" width="760px" destroy-on-close>
      <div v-loading="docDetailLoading">
        <el-descriptions v-if="docDetail" :column="2" border>
          <el-descriptions-item label="文档ID">{{ docDetail.id }}</el-descriptions-item>
          <el-descriptions-item label="对外编号">{{ docDetail.public_id }}</el-descriptions-item>
          <el-descriptions-item label="文档标题">{{ docDetail.title }}</el-descriptions-item>
          <el-descriptions-item label="文档类型">{{ documentTypeLabel(docDetail.document_type) }}</el-descriptions-item>
          <el-descriptions-item label="发布机构">{{ docDetail.issuing_authority || '-' }}</el-descriptions-item>
          <el-descriptions-item label="辖区">{{ docDetail.jurisdiction_code || '-' }}</el-descriptions-item>
          <el-descriptions-item label="适用范围" :span="2">{{ docDetail.scope_text || '-' }}</el-descriptions-item>
          <el-descriptions-item label="权威等级">{{ docDetail.authority_level ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTagType(docDetail.status)" effect="plain" size="small">{{ statusLabel(docDetail.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDateTime(docDetail.created_at) }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ formatDateTime(docDetail.updated_at) }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>

    <!-- 分片详情弹窗 -->
    <el-dialog v-model="chunkDetailDialogVisible" title="知识分片详情" width="760px" destroy-on-close>
      <div v-loading="chunkDetailLoading">
        <el-descriptions v-if="chunkDetail" :column="2" border>
          <el-descriptions-item label="分片ID">{{ chunkDetail.id }}</el-descriptions-item>
          <el-descriptions-item label="文档版本ID">{{ chunkDetail.document_version_id }}</el-descriptions-item>
          <el-descriptions-item label="片段键">{{ chunkDetail.chunk_key }}</el-descriptions-item>
          <el-descriptions-item label="片段序号">{{ chunkDetail.chunk_no }}</el-descriptions-item>
          <el-descriptions-item label="章节路径" :span="2">{{ chunkDetail.section_path || '-' }}</el-descriptions-item>
          <el-descriptions-item label="条款号">{{ chunkDetail.article_no || '-' }}</el-descriptions-item>
          <el-descriptions-item label="Token数">{{ chunkDetail.token_count ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTagType(chunkDetail.status)" effect="plain" size="small">{{ statusLabel(chunkDetail.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDateTime(chunkDetail.created_at) }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </section>
</template>

<style scoped>
.knowledge-sources-page .el-tabs {
  margin-top: 16px;
}
.knowledge-sources-page .paper-panel {
  margin-bottom: 16px;
}
.knowledge-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
