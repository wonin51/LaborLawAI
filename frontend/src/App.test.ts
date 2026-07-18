import { flushPromises, mount } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import { beforeEach, describe, expect, it, vi } from 'vitest'

const { checkHealthMock } = vi.hoisted(() => ({ checkHealthMock: vi.fn() }))

vi.mock('./api/health', () => ({
  checkHealth: checkHealthMock,
}))

import App from './App.vue'

function mountApp() {
  return mount(App, {
    global: {
      plugins: [ElementPlus],
    },
  })
}

describe('App', () => {
  beforeEach(() => {
    checkHealthMock.mockReset()
  })

  it('shows and switches all three sections', async () => {
    checkHealthMock.mockResolvedValue(true)
    const wrapper = mountApp()

    expect(wrapper.text()).toContain('法律咨询')
    expect(wrapper.text()).toContain('知识库管理')
    expect(wrapper.text()).toContain('问答记录')

    await wrapper.get('[data-section="knowledge"]').trigger('click')
    expect(wrapper.get('[data-testid="section-title"]').text()).toBe('知识库管理')

    await wrapper.get('[data-section="history"]').trigger('click')
    expect(wrapper.get('[data-testid="section-title"]').text()).toBe('问答记录')
  })

  it('shows the connected state after a successful health check', async () => {
    checkHealthMock.mockResolvedValue(true)
    const wrapper = mountApp()

    await flushPromises()

    expect(wrapper.text()).toContain('后端服务已连接')
  })

  it('shows the required message when the backend is unavailable', async () => {
    checkHealthMock.mockResolvedValue(false)
    const wrapper = mountApp()

    await flushPromises()

    expect(wrapper.text()).toContain('后端服务未连接')
  })
})
