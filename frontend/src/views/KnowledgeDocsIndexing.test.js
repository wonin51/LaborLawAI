import { flushPromises, mount } from '@vue/test-utils'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import ElementPlus, { ElMessage } from 'element-plus'
import KnowledgeDocsView from './KnowledgeDocsView.vue'
import * as knowledgeDocuments from '../api/knowledgeDocuments'

describe('KnowledgeDocsView knowledge indexing', () => {
  beforeEach(() => {
    vi.spyOn(knowledgeDocuments, 'getKnowledgeDocuments').mockResolvedValue({
      records: [{ id: 7, title: '劳动合同法', document_type: 'LAW', status: 'PUBLISHED' }],
      total: 1,
      current: 1,
      size: 10
    })
  })

  afterEach(() => {
    vi.restoreAllMocks()
    document.body.innerHTML = ''
  })

  it('shows the index result dialog after generating knowledge index', async () => {
    const generateIndex = vi.spyOn(knowledgeDocuments, 'generateLegalDocumentIndex').mockResolvedValue({
      document_id: 7,
      chunk_count: 3,
      indexed_count: 3,
      failed_count: 0,
      status: 'indexed'
    })
    const successMessage = vi.spyOn(ElMessage, 'success').mockImplementation(() => {})

    const wrapper = mount(KnowledgeDocsView, {
      global: {
        plugins: [ElementPlus]
      }
    })

    await flushPromises()
    await wrapper.findAll('button').find((button) => button.text().includes('生成知识索引')).trigger('click')
    await flushPromises()

    expect(generateIndex).toHaveBeenCalledWith(7)
    expect(successMessage).toHaveBeenCalledWith('知识索引生成成功')
    expect(wrapper.vm.indexResultDialogVisible).toBe(true)
    expect(wrapper.vm.indexResult.document_id).toBe(7)
    expect(wrapper.vm.indexResult.chunk_count).toBe(3)
    expect(wrapper.vm.indexResult.indexed_count).toBe(3)
    expect(wrapper.vm.indexResult.failed_count).toBe(0)
    expect(wrapper.vm.indexResult.status).toBe('indexed')
  })

  it('shows backend error when generating knowledge index fails', async () => {
    vi.spyOn(knowledgeDocuments, 'generateLegalDocumentIndex').mockRejectedValue(new Error('文档已禁用，禁止索引'))
    const errorMessage = vi.spyOn(ElMessage, 'error').mockImplementation(() => {})

    const wrapper = mount(KnowledgeDocsView, {
      global: {
        plugins: [ElementPlus]
      }
    })

    await flushPromises()
    await wrapper.findAll('button').find((button) => button.text().includes('生成知识索引')).trigger('click')
    await flushPromises()

    expect(errorMessage).toHaveBeenCalledWith('文档已禁用，禁止索引')
  })
})
