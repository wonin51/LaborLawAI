import { flushPromises, mount } from '@vue/test-utils'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import ElementPlus, { ElMessage, ElMessageBox } from 'element-plus'
import KnowledgeDocsView from './KnowledgeDocsView.vue'
import * as knowledgeDocuments from '../api/knowledgeDocuments'

describe('KnowledgeDocsView', () => {
  beforeEach(() => {
    vi.spyOn(knowledgeDocuments, 'getKnowledgeDocuments').mockResolvedValue({
      records: [],
      total: 0,
      current: 1,
      size: 10
    })
    vi.spyOn(knowledgeDocuments, 'getKnowledgeDocumentDetail').mockResolvedValue({})
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('renders the query area, list area, and create button', async () => {
    const wrapper = mount(KnowledgeDocsView, {
      global: {
        plugins: [ElementPlus]
      }
    })

    await flushPromises()

    expect(wrapper.text()).toContain('知识库文档管理')
    expect(wrapper.text()).toContain('查询条件')
    expect(wrapper.text()).toContain('新增文档')
    expect(wrapper.find('table').exists()).toBe(true)

    for (const label of ['\u6807\u9898', '\u7c7b\u578b', '\u72b6\u6001', '\u53d1\u5e03\u673a\u6784', '\u8f96\u533a']) {
      expect(wrapper.text()).toContain(label)
    }
    for (const fieldLabel of ['\uff08title\uff09', '\uff08document_type\uff09', '\uff08status\uff09', '\uff08issuing_authority\uff09', '\uff08jurisdiction_code\uff09']) {
      expect(wrapper.text()).not.toContain(fieldLabel)
    }
  })

  it('opens the create dialog and exposes all required fields', async () => {
    const wrapper = mount(KnowledgeDocsView, {
      global: {
        plugins: [ElementPlus]
      }
    })

    await wrapper.findAll('button').find((button) => button.text().includes('新增文档')).trigger('click')

    expect(wrapper.text()).toContain('新增知识库文档')
    for (const label of [
      '\u6587\u6863\u6807\u9898',
      '\u6587\u6863\u7c7b\u578b',
      '\u53d1\u5e03\u673a\u6784',
      '\u6765\u6e90\u94fe\u63a5',
      '\u8f96\u533a',
      '\u9002\u7528\u8303\u56f4',
      '\u6743\u5a01\u7b49\u7ea7',
      '\u72b6\u6001'
    ]) {
      expect(wrapper.text()).toContain(label)
    }
    for (const fieldLabel of [
      '\uff08title\uff09',
      '\uff08document_type\uff09',
      '\uff08issuing_authority\uff09',
      '\uff08canonical_source_url\uff09',
      '\uff08jurisdiction_code\uff09',
      '\uff08scope_text\uff09',
      '\uff08authority_level\uff09',
      '\uff08status\uff09'
    ]) {
      expect(wrapper.text()).not.toContain(fieldLabel)
    }
  })

  it('renders a detail entry when the list contains a document', async () => {
    vi.spyOn(knowledgeDocuments, 'getKnowledgeDocuments').mockResolvedValue({
      records: [{ id: 7, title: '劳动合同法', document_type: 'LAW', status: 'DRAFT' }],
      total: 1,
      current: 1,
      size: 10
    })

    const wrapper = mount(KnowledgeDocsView, {
      global: {
        plugins: [ElementPlus]
      }
    })

    await flushPromises()

    expect(wrapper.text()).toContain('劳动合同法')
    expect(wrapper.text()).toContain('查看详情')
  })

  it('calls the detail API when the detail action is clicked', async () => {
    const getDetail = vi.spyOn(knowledgeDocuments, 'getKnowledgeDocumentDetail')
      .mockResolvedValue({ id: 7, title: '劳动合同法', document_type: 'LAW', status: 'PUBLISHED' })
    vi.spyOn(knowledgeDocuments, 'getKnowledgeDocuments').mockResolvedValue({
      records: [{ id: 7, title: '劳动合同法', document_type: 'LAW', status: 'DRAFT' }],
      total: 1,
      current: 1,
      size: 10
    })

    const wrapper = mount(KnowledgeDocsView, {
      global: {
        plugins: [ElementPlus]
      }
    })

    await flushPromises()
    await wrapper.findAll('button').find((button) => button.text().includes('查看详情')).trigger('click')
    await flushPromises()

    expect(getDetail).toHaveBeenCalledWith(7)
    expect(wrapper.text()).toContain('PUBLISHED')
  })

  it('calls create API and refreshes the list after a successful submission', async () => {
    const getList = vi.spyOn(knowledgeDocuments, 'getKnowledgeDocuments')
      .mockResolvedValueOnce({ records: [], total: 0, current: 1, size: 10 })
      .mockResolvedValueOnce({
        records: [{ id: 8, title: '新增文档', document_type: 'LAW', status: 'DRAFT' }],
        total: 1,
        current: 1,
        size: 10
      })
    const createDocument = vi.spyOn(knowledgeDocuments, 'createKnowledgeDocument').mockResolvedValue({})

    const wrapper = mount(KnowledgeDocsView, {
      global: {
        plugins: [ElementPlus]
      }
    })

    await flushPromises()
    await wrapper.findAll('button').find((button) => button.text().includes('新增文档')).trigger('click')
    wrapper.vm.createForm.title = '新增文档'
    await wrapper.findAll('button').find((button) => button.text().includes('创建文档')).trigger('click')
    await flushPromises()

    expect(createDocument).toHaveBeenCalledWith(expect.objectContaining({
      title: '新增文档',
      document_type: 'LAW',
      jurisdiction_code: 'CN',
      authority_level: 100,
      status: 'DRAFT'
    }))
    expect(getList).toHaveBeenCalledTimes(2)
    expect(wrapper.text()).toContain('新增文档')
  })
  it('opens edit dialog and updates an existing document', async () => {
    const getDetail = vi.spyOn(knowledgeDocuments, 'getKnowledgeDocumentDetail').mockResolvedValue({
      id: 7,
      title: '劳动合同法',
      document_type: 'LAW',
      issuing_authority: '全国人大常委会',
      jurisdiction_code: 'CN',
      status: 'DRAFT'
    })
    const updateDocument = vi.spyOn(knowledgeDocuments, 'updateKnowledgeDocument').mockResolvedValue({})
    vi.spyOn(knowledgeDocuments, 'getKnowledgeDocuments').mockResolvedValue({
      records: [{ id: 7, title: '劳动合同法', document_type: 'LAW', status: 'DRAFT' }],
      total: 1,
      current: 1,
      size: 10
    })

    const wrapper = mount(KnowledgeDocsView, {
      global: {
        plugins: [ElementPlus]
      }
    })

    await flushPromises()
    await wrapper.findAll('button').find((button) => button.text().includes('编辑')).trigger('click')
    await flushPromises()

    expect(getDetail).toHaveBeenCalledWith(7)
    expect(wrapper.text()).toContain('编辑知识库文档')
    wrapper.vm.editForm.title = '劳动合同法（修订）'
    await wrapper.findAll('button').find((button) => button.text().includes('保存修改')).trigger('click')
    await flushPromises()

    expect(updateDocument).toHaveBeenCalledWith(7, expect.objectContaining({
      title: '劳动合同法（修订）',
      document_type: 'LAW'
    }))
  })

  it('deletes a document after confirmation and refreshes the list', async () => {
    vi.spyOn(knowledgeDocuments, 'getKnowledgeDocuments').mockResolvedValue({
      records: [{ id: 7, title: '待删除文档', document_type: 'LAW', status: 'DRAFT' }],
      total: 1,
      current: 1,
      size: 10
    })
    const deleteDocument = vi.spyOn(knowledgeDocuments, 'deleteKnowledgeDocument').mockResolvedValue(true)
    vi.spyOn(ElMessageBox, 'confirm').mockResolvedValue('confirm')

    const wrapper = mount(KnowledgeDocsView, {
      global: {
        plugins: [ElementPlus]
      }
    })

    await flushPromises()
    await wrapper.findAll('button').find((button) => button.text().includes('删除')).trigger('click')
    await flushPromises()

    expect(deleteDocument).toHaveBeenCalledWith(7)
  })

  it('updates status through the status API', async () => {
    const updateStatus = vi.spyOn(knowledgeDocuments, 'updateKnowledgeDocumentStatus').mockResolvedValue({})
    const wrapper = mount(KnowledgeDocsView, {
      global: {
        plugins: [ElementPlus]
      }
    })

    await wrapper.vm.handleStatusChange({ id: 7, status: 'DRAFT' }, 'PUBLISHED')

    expect(updateStatus).toHaveBeenCalledWith(7, 'PUBLISHED')
  })
  it('shows an error message when loading the list fails', async () => {
    vi.spyOn(knowledgeDocuments, 'getKnowledgeDocuments').mockRejectedValue(new Error('接口不可用'))
    const errorMessage = vi.spyOn(ElMessage, 'error').mockImplementation(() => {})

    mount(KnowledgeDocsView, {
      global: {
        plugins: [ElementPlus]
      }
    })

    await flushPromises()

    expect(errorMessage).toHaveBeenCalledWith('接口不可用')
  })
})
