import { flushPromises, mount } from '@vue/test-utils'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import ElementPlus from 'element-plus'
import App from './App.vue'
import * as http from './api/http'
import * as knowledgeDocuments from './api/knowledgeDocuments'
import * as knowledgeSources from './api/knowledgeSources'
import * as qaHistory from './api/qaHistory'

describe('App', () => {
  beforeEach(() => {
    vi.spyOn(http, 'getHealthStatus').mockResolvedValue({ status: 'ok' })
    vi.spyOn(knowledgeDocuments, 'getKnowledgeDocuments').mockResolvedValue({
      records: [],
      total: 0,
      current: 1,
      size: 10
    })
    vi.spyOn(knowledgeSources, 'getKnowledgeDocuments').mockResolvedValue({
      records: [{ id: 1, title: '劳动合同法', document_type: 'LAW', status: 'PUBLISHED' }],
      total: 1,
      current: 1,
      size: 10
    })
    vi.spyOn(knowledgeSources, 'getKnowledgeChunkRefs').mockResolvedValue({
      records: [{ id: 2, chunk_key: 'law-001', document_version_id: 1, chunk_no: 1, status: 'ACTIVE' }],
      total: 1,
      current: 1,
      size: 10
    })
    vi.spyOn(qaHistory, 'getQaRecords').mockResolvedValue({
      records: [{ id: 3, question: '未签劳动合同怎么办？', conclusion_summary: '可以申请仲裁', answer_status: 'COMPLETED' }],
      total: 1,
      current: 1,
      size: 10
    })
    vi.spyOn(qaHistory, 'getQaCitations').mockResolvedValue({
      records: [{ id: 4, claim_id: 1, source_title_snapshot: '劳动合同法', quote_snapshot: '应当订立书面劳动合同。' }],
      total: 1,
      current: 1,
      size: 10
    })
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('opens the real legal sources page from the main navigation', async () => {
    const wrapper = mount(App, {
      global: {
        plugins: [ElementPlus]
      }
    })

    await flushPromises()
    wrapper.vm.activeView = 'sources'
    await wrapper.vm.$nextTick()
    await flushPromises()

    expect(wrapper.text()).toContain('法律依据库')
    expect(wrapper.text()).toContain('知识文档')
    expect(wrapper.text()).toContain('知识分片')
    expect(wrapper.text()).toContain('劳动合同法')
    expect(wrapper.text()).not.toContain('待开发')
  })

  it('opens the real QA history page from the main navigation', async () => {
    const wrapper = mount(App, {
      global: {
        plugins: [ElementPlus]
      }
    })

    await flushPromises()
    wrapper.vm.activeView = 'history'
    await wrapper.vm.$nextTick()
    await flushPromises()

    expect(wrapper.text()).toContain('问答历史')
    expect(wrapper.text()).toContain('问答记录')
    expect(wrapper.text()).toContain('回答引用')
    expect(wrapper.text()).toContain('未签劳动合同怎么办？')
    expect(wrapper.text()).not.toContain('待开发')
  })

  it('opens the real knowledge document management page from the main navigation', async () => {
    const wrapper = mount(App, {
      global: {
        plugins: [ElementPlus]
      }
    })

    await flushPromises()
    await wrapper.findAll('button').find((button) => button.text().includes('知识库管理')).trigger('click')
    await flushPromises()

    expect(wrapper.text()).toContain('知识库文档管理')
    expect(wrapper.text()).toContain('新增文档')
    expect(wrapper.text()).not.toContain('Total')
    expect(wrapper.text()).not.toContain('/page')
    expect(wrapper.text()).not.toContain('Go to')
  })
})
