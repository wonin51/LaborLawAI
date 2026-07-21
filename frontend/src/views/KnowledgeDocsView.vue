<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { CirclePlus, Delete, Edit, Refresh, Search, View } from '@element-plus/icons-vue'
import {
  createKnowledgeDocument,
  deleteKnowledgeDocument,
  getKnowledgeDocumentDetail,
  getKnowledgeDocuments,
  normalizeKnowledgeDocumentPage,
  updateKnowledgeDocument,
  updateKnowledgeDocumentStatus
} from '../api/knowledgeDocuments'

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

const statusTagTypeMap = {
  DRAFT: 'info',
  PUBLISHED: 'success',
  DISABLED: 'danger'
}

const queryForm = reactive(createQueryForm())
const createForm = reactive(createDocumentForm())
const editForm = reactive(createDocumentForm())
const createFormRef = ref()
const editFormRef = ref()
const listLoading = ref(false)
const createLoading = ref(false)
const editLoading = ref(false)
const detailLoading = ref(false)
const deletingDocumentId = ref(null)
const statusUpdatingId = ref(null)
const createDialogVisible = ref(false)
const editDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const editingDocumentId = ref(null)
const detailDocument = ref(null)
const tableRows = ref([])
const pagination = reactive({
  pageNo: 1,
  pageSize: 10,
  total: 0,
  pages: 0
})

const documentFormRules = {
  title: [{ required: true, message: '请输入文档标题', trigger: 'blur' }],
  document_type: [{ required: true, message: '请选择文档类型', trigger: 'change' }]
}

function createQueryForm() {
  return {
    title: '',
    document_type: '',
    status: '',
    issuing_authority: '',
    jurisdiction_code: ''
  }
}

function createDocumentForm() {
  return {
    title: '',
    document_type: 'LAW',
    issuing_authority: '',
    canonical_source_url: '',
    jurisdiction_code: 'CN',
    scope_text: '',
    authority_level: 100,
    status: 'DRAFT'
  }
}

function assignDocumentForm(target, document = {}) {
  Object.assign(target, createDocumentForm(), {
    title: document.title || '',
    document_type: document.document_type || 'LAW',
    issuing_authority: document.issuing_authority || '',
    canonical_source_url: document.canonical_source_url || '',
    jurisdiction_code: document.jurisdiction_code || 'CN',
    scope_text: document.scope_text || '',
    authority_level: document.authority_level ?? 100,
    status: document.status || 'DRAFT'
  })
}

function resetCreateForm() {
  assignDocumentForm(createForm)
  createFormRef.value?.clearValidate?.()
}

function statusLabel(status) {
  return status || '-'
}

function documentTypeLabel(type) {
  return type || '-'
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

async function loadDocuments(nextPageNo = pagination.pageNo, nextPageSize = pagination.pageSize) {
  listLoading.value = true
  try {
    const page = normalizeKnowledgeDocumentPage(await getKnowledgeDocuments({
      ...queryForm,
      pageNo: nextPageNo,
      pageSize: nextPageSize
    }))

    tableRows.value = page.records
    pagination.pageNo = page.current || nextPageNo
    pagination.pageSize = page.size || nextPageSize
    pagination.total = page.total || 0
    pagination.pages = page.pages || 0
  } catch (error) {
    ElMessage.error(error?.message || '加载知识库文档失败')
  } finally {
    listLoading.value = false
  }
}

function handleSearch() {
  loadDocuments(1, pagination.pageSize)
}

function handleReset() {
  Object.assign(queryForm, createQueryForm())
  loadDocuments(1, pagination.pageSize)
}

function openCreateDialog() {
  resetCreateForm()
  createDialogVisible.value = true
}

async function submitCreate() {
  if (!createFormRef.value) return

  try {
    await createFormRef.value.validate()
    createLoading.value = true
    await createKnowledgeDocument({ ...createForm })
    ElMessage.success('文档创建成功')
    createDialogVisible.value = false
    await loadDocuments(1, pagination.pageSize)
  } catch (error) {
    if (error?.message) ElMessage.error(error.message)
  } finally {
    createLoading.value = false
  }
}

async function openEditDialog(row) {
  editDialogVisible.value = true
  editLoading.value = true
  editingDocumentId.value = row.id

  try {
    const detail = await getKnowledgeDocumentDetail(row.id)
    assignDocumentForm(editForm, detail || row)
    editFormRef.value?.clearValidate?.()
  } catch (error) {
    editDialogVisible.value = false
    editingDocumentId.value = null
    ElMessage.error(error?.message || '加载待编辑文档失败')
  } finally {
    editLoading.value = false
  }
}

async function submitEdit() {
  if (!editFormRef.value || !editingDocumentId.value) return

  try {
    await editFormRef.value.validate()
    editLoading.value = true
    await updateKnowledgeDocument(editingDocumentId.value, { ...editForm })
    ElMessage.success('文档更新成功')
    editDialogVisible.value = false
    await loadDocuments(pagination.pageNo, pagination.pageSize)
  } catch (error) {
    if (error?.message) ElMessage.error(error.message)
  } finally {
    editLoading.value = false
  }
}

function closeEditDialog() {
  editDialogVisible.value = false
  editingDocumentId.value = null
  assignDocumentForm(editForm)
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
      `确认删除“${row.title || row.id}”吗？删除后无法恢复。`,
      '删除知识库文档',
      {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    deletingDocumentId.value = row.id
    await deleteKnowledgeDocument(row.id)
    ElMessage.success('文档删除成功')
    const nextPageNo = tableRows.value.length === 1 && pagination.pageNo > 1
      ? pagination.pageNo - 1
      : pagination.pageNo
    await loadDocuments(nextPageNo, pagination.pageSize)
  } catch (error) {
    if (error === 'cancel' || error === 'close') return
    ElMessage.error(error?.message || '删除文档失败')
  } finally {
    deletingDocumentId.value = null
  }
}

async function handleStatusChange(row, nextStatus) {
  if (!nextStatus || nextStatus === row.status) return

  statusUpdatingId.value = row.id
  try {
    await updateKnowledgeDocumentStatus(row.id, nextStatus)
    ElMessage.success('文档状态更新成功')
    await loadDocuments(pagination.pageNo, pagination.pageSize)
  } catch (error) {
    ElMessage.error(error?.message || '更新文档状态失败')
  } finally {
    statusUpdatingId.value = null
  }
}

async function openDetail(row) {
  detailDialogVisible.value = true
  detailLoading.value = true
  detailDocument.value = null

  try {
    detailDocument.value = await getKnowledgeDocumentDetail(row.id)
  } catch (error) {
    ElMessage.error(error?.message || '加载文档详情失败')
  } finally {
    detailLoading.value = false
  }
}

function closeDetailDialog() {
  detailDialogVisible.value = false
  detailDocument.value = null
}

function handlePageChange(pageNo) {
  loadDocuments(pageNo, pagination.pageSize)
}

function handleSizeChange(pageSize) {
  loadDocuments(1, pageSize)
}

onMounted(() => {
  loadDocuments()
})
</script>

<template>
  <section class="page-template knowledge-docs-page">
    <div class="hero-copy compact">
      <p class="eyebrow">KNOWLEDGE BASE</p>
      <h2>知识库文档管理</h2>
      <p>维护法规、司法解释、政策与问答文档的元数据，确保检索依据清晰可追溯。</p>
    </div>

    <el-card class="paper-panel knowledge-panel" shadow="never">
      <template #header>
        <div class="panel-title split-title">
          <span>查询条件</span>
          <div class="knowledge-panel-actions">
            <el-button :icon="Refresh" @click="handleReset">重置</el-button>
            <el-button type="primary" :icon="CirclePlus" @click="openCreateDialog">新增文档</el-button>
          </div>
        </div>
      </template>

      <el-form :model="queryForm" inline class="knowledge-filter-form" label-width="120px">
        <el-form-item label="标题">
          <el-input v-model="queryForm.title" clearable placeholder="输入文档标题" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="queryForm.document_type" clearable placeholder="选择文档类型">
            <el-option v-for="item in documentTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" clearable placeholder="选择状态">
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="发布机构">
          <el-input v-model="queryForm.issuing_authority" clearable placeholder="输入发布机构" />
        </el-form-item>
        <el-form-item label="辖区">
          <el-input v-model="queryForm.jurisdiction_code" clearable placeholder="例如 CN" />
        </el-form-item>
        <el-form-item class="knowledge-filter-actions">
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="paper-panel knowledge-panel" shadow="never">
      <template #header>
        <div class="panel-title split-title">
          <span>文档列表</span>
          <el-tag effect="plain" type="info">共 {{ pagination.total }} 条</el-tag>
        </div>
      </template>

      <div class="knowledge-table-wrap" v-loading="listLoading">
        <el-table :data="tableRows" stripe class="knowledge-table" empty-text="暂无文档">
          <el-table-column prop="id" label="文档ID" width="100" />
          <el-table-column prop="public_id" label="对外编号" min-width="180" show-overflow-tooltip />
          <el-table-column prop="title" label="文档标题" min-width="220" show-overflow-tooltip />
          <el-table-column prop="document_type" label="文档类型" width="190">
            <template #default="scope">{{ documentTypeLabel(scope.row.document_type) }}</template>
          </el-table-column>
          <el-table-column prop="issuing_authority" label="发布机构" min-width="200" show-overflow-tooltip />
          <el-table-column prop="jurisdiction_code" label="辖区" width="180" />
          <el-table-column prop="status" label="状态" width="140">
            <template #default="scope">
              <el-tag :type="statusTagType(scope.row.status)" effect="plain">{{ statusLabel(scope.row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="created_at" label="创建时间" min-width="180">
            <template #default="scope">{{ formatDateTime(scope.row.created_at) }}</template>
          </el-table-column>
          <el-table-column prop="updated_at" label="更新时间" min-width="180">
            <template #default="scope">{{ formatDateTime(scope.row.updated_at) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="360" fixed="right">
            <template #default="scope">
              <div class="knowledge-row-actions">
                <el-button link type="primary" :icon="View" @click="openDetail(scope.row)">查看详情</el-button>
                <el-button link type="primary" :icon="Edit" @click="openEditDialog(scope.row)">编辑</el-button>
                <el-select
                  class="knowledge-status-select"
                  size="small"
                  :model-value="scope.row.status"
                  :loading="statusUpdatingId === scope.row.id"
                  @change="handleStatusChange(scope.row, $event)"
                >
                  <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
                <el-button
                  link
                  type="danger"
                  :icon="Delete"
                  :loading="deletingDocumentId === scope.row.id"
                  @click="handleDelete(scope.row)"
                >删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="knowledge-pagination">
        <el-pagination
          v-model:current-page="pagination.pageNo"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="createDialogVisible" title="新增知识库文档" width="760px" destroy-on-close>
      <el-form ref="createFormRef" :model="createForm" :rules="documentFormRules" label-width="170px" class="knowledge-create-form">
        <div class="knowledge-form-grid">
          <el-form-item label="文档标题" prop="title">
            <el-input v-model="createForm.title" placeholder="请输入文档标题" />
          </el-form-item>
          <el-form-item label="文档类型" prop="document_type">
            <el-select v-model="createForm.document_type" placeholder="请选择文档类型">
              <el-option v-for="item in documentTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="发布机构">
            <el-input v-model="createForm.issuing_authority" placeholder="请输入发布机构" />
          </el-form-item>
          <el-form-item label="来源链接">
            <el-input v-model="createForm.canonical_source_url" placeholder="请输入权威来源链接" />
          </el-form-item>
          <el-form-item label="辖区">
            <el-input v-model="createForm.jurisdiction_code" placeholder="例如 CN" />
          </el-form-item>
          <el-form-item label="适用范围">
            <el-input v-model="createForm.scope_text" placeholder="请输入适用范围说明" />
          </el-form-item>
          <el-form-item label="权威等级">
            <el-input-number v-model="createForm.authority_level" :min="0" :max="1000" controls-position="right" />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="createForm.status" placeholder="请选择状态">
              <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
        </div>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="createDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="createLoading" @click="submitCreate">创建文档</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog v-model="editDialogVisible" title="编辑知识库文档" width="760px" destroy-on-close @closed="closeEditDialog">
      <div v-loading="editLoading">
        <el-form ref="editFormRef" :model="editForm" :rules="documentFormRules" label-width="170px" class="knowledge-create-form">
          <div class="knowledge-form-grid">
            <el-form-item label="文档标题" prop="title">
              <el-input v-model="editForm.title" placeholder="请输入文档标题" />
            </el-form-item>
            <el-form-item label="文档类型" prop="document_type">
              <el-select v-model="editForm.document_type" placeholder="请选择文档类型">
                <el-option v-for="item in documentTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="发布机构">
              <el-input v-model="editForm.issuing_authority" placeholder="请输入发布机构" />
            </el-form-item>
            <el-form-item label="来源链接">
              <el-input v-model="editForm.canonical_source_url" placeholder="请输入权威来源链接" />
            </el-form-item>
            <el-form-item label="辖区">
              <el-input v-model="editForm.jurisdiction_code" placeholder="例如 CN" />
            </el-form-item>
            <el-form-item label="适用范围">
              <el-input v-model="editForm.scope_text" placeholder="请输入适用范围说明" />
            </el-form-item>
            <el-form-item label="权威等级">
              <el-input-number v-model="editForm.authority_level" :min="0" :max="1000" controls-position="right" />
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="editForm.status" placeholder="请选择状态">
                <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </div>
        </el-form>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="editLoading" @click="submitEdit">保存修改</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialogVisible" title="文档详情" width="760px" destroy-on-close @closed="closeDetailDialog">
      <div class="knowledge-detail-wrap" v-loading="detailLoading">
        <template v-if="detailDocument">
          <el-descriptions :column="2" border class="knowledge-detail-descriptions">
            <el-descriptions-item label="文档ID">{{ detailDocument.id }}</el-descriptions-item>
            <el-descriptions-item label="对外编号">{{ detailDocument.public_id }}</el-descriptions-item>
            <el-descriptions-item label="文档标题">{{ detailDocument.title }}</el-descriptions-item>
            <el-descriptions-item label="文档类型">{{ documentTypeLabel(detailDocument.document_type) }}</el-descriptions-item>
            <el-descriptions-item label="发布机构">{{ detailDocument.issuing_authority || '-' }}</el-descriptions-item>
            <el-descriptions-item label="辖区">{{ detailDocument.jurisdiction_code || '-' }}</el-descriptions-item>
            <el-descriptions-item label="适用范围" :span="2">{{ detailDocument.scope_text || '-' }}</el-descriptions-item>
            <el-descriptions-item label="来源链接" :span="2">{{ detailDocument.canonical_source_url || '-' }}</el-descriptions-item>
            <el-descriptions-item label="权威等级">{{ detailDocument.authority_level ?? '-' }}</el-descriptions-item>
            <el-descriptions-item label="状态">{{ statusLabel(detailDocument.status) }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ formatDateTime(detailDocument.created_at) }}</el-descriptions-item>
            <el-descriptions-item label="更新时间">{{ formatDateTime(detailDocument.updated_at) }}</el-descriptions-item>
          </el-descriptions>
        </template>
      </div>
    </el-dialog>
  </section>
</template>