import { flushPromises, mount } from '@vue/test-utils'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import ElementPlus from 'element-plus'
import App from './App.vue'
import * as http from './api/http'
import * as knowledgeDocuments from './api/knowledgeDocuments'

describe('App', () => {
  beforeEach(() => {
    vi.spyOn(http, 'getHealthStatus').mockResolvedValue({ status: 'ok' })
    vi.spyOn(knowledgeDocuments, 'getKnowledgeDocuments').mockResolvedValue({
      records: [],
      total: 0,
      current: 1,
      size: 10
    })
  })

  afterEach(() => {
    vi.restoreAllMocks()
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
  })
})