import { flushPromises, mount } from '@vue/test-utils'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import ElementPlus from 'element-plus'
import QaHistoryView from './QaHistoryView.vue'
import * as qaHistory from '../api/qaHistory'

const recordRow = {
  id: 31,
  question: '入职两个月没签劳动合同怎么办？',
  conclusion_summary: '可以评估二倍工资差额请求',
  answer_status: 'COMPLETED',
  action_steps: '准备证据后申请仲裁',
  evidence_checklist: '工资流水、考勤记录',
  cautions: '注意仲裁时效',
  citation_coverage: 0.9,
  completed_at: '2026-07-21T10:30:00',
  created_at: '2026-07-21T10:00:00'
}

const citationRow = {
  id: 41,
  claim_id: 5,
  document_version_id: 4,
  chunk_ref_id: 3,
  source_title_snapshot: '中华人民共和国劳动合同法',
  article_no_snapshot: '第十条',
  relevance_score: 0.88,
  quote_snapshot: '建立劳动关系，应当订立书面劳动合同。',
  created_at: '2026-07-21T10:00:00'
}

describe('QaHistoryView', () => {
  beforeEach(() => {
    vi.spyOn(qaHistory, 'getQaRecords').mockResolvedValue({
      records: [recordRow],
      total: 1,
      current: 1,
      size: 10,
      pages: 1
    })
    vi.spyOn(qaHistory, 'getQaCitations').mockResolvedValue({
      records: [citationRow],
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
    const wrapper = mount(QaHistoryView, {
      global: { plugins: [ElementPlus] }
    })

    await flushPromises()

    expect(wrapper.text()).toContain('问答历史')
    expect(wrapper.text()).toContain('问答记录')
    expect(wrapper.text()).toContain('回答引用')
    expect(wrapper.text()).toContain('查询条件')
    expect(wrapper.text()).toContain('问题内容')
    expect(wrapper.text()).toContain('结论摘要')
    expect(wrapper.text()).toContain('回答状态')
    expect(wrapper.text()).toContain('完成时间')
    expect(wrapper.text()).toContain('关键结论ID')
    expect(wrapper.text()).toContain('来源标题')
    expect(wrapper.text()).toContain('条款号')
    expect(wrapper.text()).toContain('相关度')
    expect(wrapper.text()).toContain('引用内容')
    expect(wrapper.text()).toContain('入职两个月没签劳动合同怎么办？')
    expect(wrapper.text()).toContain('中华人民共和国劳动合同法')
    expect(wrapper.text()).not.toContain('标题(title)')
  })

  it('passes record filters and pagination params to the API', async () => {
    const wrapper = mount(QaHistoryView, {
      global: { plugins: [ElementPlus] }
    })
    await flushPromises()

    wrapper.vm.recordQueryForm.answer_status = 'COMPLETED'
    wrapper.vm.recordQueryForm.topic_id = '6'
    await wrapper.vm.loadRecords(2, 20)

    expect(qaHistory.getQaRecords).toHaveBeenLastCalledWith({
      answer_status: 'COMPLETED',
      topic_id: 6,
      pageNo: 2,
      pageSize: 20
    })
  })

  it('passes citation filters and pagination params to the API', async () => {
    const wrapper = mount(QaHistoryView, {
      global: { plugins: [ElementPlus] }
    })
    await flushPromises()

    wrapper.vm.citationQueryForm.claim_id = '5'
    wrapper.vm.citationQueryForm.document_version_id = '4'
    wrapper.vm.citationQueryForm.chunk_ref_id = '3'
    await wrapper.vm.loadCitations(2, 20)

    expect(qaHistory.getQaCitations).toHaveBeenLastCalledWith({
      claim_id: 5,
      document_version_id: 4,
      chunk_ref_id: 3,
      pageNo: 2,
      pageSize: 20
    })
  })

  it('opens record and citation detail dialogs when table rows are clicked', async () => {
    const wrapper = mount(QaHistoryView, {
      global: { plugins: [ElementPlus] }
    })
    await flushPromises()

    const tables = wrapper.findAllComponents({ name: 'ElTable' })
    await tables[0].vm.$emit('row-click', recordRow)
    await tables[1].vm.$emit('row-click', citationRow)
    await flushPromises()

    expect(wrapper.text()).toContain('问答记录详情')
    expect(wrapper.text()).toContain('引用依据详情')
    expect(wrapper.text()).toContain('准备证据后申请仲裁')
    expect(wrapper.text()).toContain('建立劳动关系，应当订立书面劳动合同。')
  })
})
