import { flushPromises, mount } from '@vue/test-utils'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import ElementPlus from 'element-plus'
import KnowledgeSourcesView from './KnowledgeSourcesView.vue'
import * as knowledgeSources from '../api/knowledgeSources'

const documentRow = {
  id: 11,
  title: '中华人民共和国劳动合同法',
  document_type: 'LAW',
  issuing_authority: '全国人民代表大会常务委员会',
  status: 'PUBLISHED',
  created_at: '2026-07-21T10:00:00'
}

const chunkRow = {
  id: 21,
  chunk_key: 'law-001',
  document_version_id: 3,
  chunk_no: 1,
  section_path: '第一章 总则',
  article_no: '第一条',
  token_count: 128,
  status: 'ACTIVE',
  created_at: '2026-07-21T10:00:00'
}

describe('KnowledgeSourcesView', () => {
  beforeEach(() => {
    vi.spyOn(knowledgeSources, 'getKnowledgeDocuments').mockResolvedValue({
      records: [documentRow],
      total: 1,
      current: 1,
      size: 10,
      pages: 1
    })
    vi.spyOn(knowledgeSources, 'getKnowledgeChunkRefs').mockResolvedValue({
      records: [chunkRow],
      total: 1,
      current: 1,
      size: 10,
      pages: 1
    })
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('renders Chinese tabs, query forms, tables and paged data', async () => {
    const wrapper = mount(KnowledgeSourcesView, {
      global: { plugins: [ElementPlus] }
    })

    await flushPromises()

    expect(wrapper.text()).toContain('法律依据库')
    expect(wrapper.text()).toContain('知识文档')
    expect(wrapper.text()).toContain('知识分片')
    expect(wrapper.text()).toContain('查询条件')
    expect(wrapper.text()).toContain('文档标题')
    expect(wrapper.text()).toContain('文档类型')
    expect(wrapper.text()).toContain('发布机构')
    expect(wrapper.text()).toContain('创建时间')
    expect(wrapper.text()).toContain('片段键')
    expect(wrapper.text()).toContain('文档版本ID')
    expect(wrapper.text()).toContain('片段序号')
    expect(wrapper.text()).toContain('Token数')
    expect(wrapper.text()).toContain('中华人民共和国劳动合同法')
    expect(wrapper.text()).toContain('law-001')
    expect(wrapper.text()).not.toContain('标题(title)')
  })

  it('passes document filters and pagination params to the API', async () => {
    const wrapper = mount(KnowledgeSourcesView, {
      global: { plugins: [ElementPlus] }
    })
    await flushPromises()

    wrapper.vm.docQueryForm.title = '劳动合同'
    wrapper.vm.docQueryForm.document_type = 'LAW'
    wrapper.vm.docQueryForm.status = 'PUBLISHED'
    await wrapper.vm.loadDocuments(2, 20)

    expect(knowledgeSources.getKnowledgeDocuments).toHaveBeenLastCalledWith({
      title: '劳动合同',
      document_type: 'LAW',
      status: 'PUBLISHED',
      pageNo: 2,
      pageSize: 20
    })
  })

  it('passes chunk filters and pagination params to the API', async () => {
    const wrapper = mount(KnowledgeSourcesView, {
      global: { plugins: [ElementPlus] }
    })
    await flushPromises()

    wrapper.vm.chunkQueryForm.document_version_id = '3'
    wrapper.vm.chunkQueryForm.status = 'ACTIVE'
    wrapper.vm.chunkQueryForm.chunk_key = 'law'
    wrapper.vm.chunkQueryForm.section_path = '总则'
    await wrapper.vm.loadChunks(2, 20)

    expect(knowledgeSources.getKnowledgeChunkRefs).toHaveBeenLastCalledWith({
      document_version_id: 3,
      status: 'ACTIVE',
      chunk_key: 'law',
      section_path: '总则',
      pageNo: 2,
      pageSize: 20
    })
  })

  it('opens document and chunk detail dialogs when table rows are clicked', async () => {
    const wrapper = mount(KnowledgeSourcesView, {
      global: { plugins: [ElementPlus] }
    })
    await flushPromises()

    const tables = wrapper.findAllComponents({ name: 'ElTable' })
    await tables[0].vm.$emit('row-click', documentRow)
    await tables[1].vm.$emit('row-click', chunkRow)
    await flushPromises()

    expect(wrapper.text()).toContain('文档详情')
    expect(wrapper.text()).toContain('知识分片详情')
    expect(wrapper.text()).toContain('中华人民共和国劳动合同法')
    expect(wrapper.text()).toContain('第一章 总则')
  })
})
