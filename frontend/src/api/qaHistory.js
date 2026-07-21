import http from './http'

export function getQaRecords(params = {}) {
  return http.get('/api/qa/records', { params })
}

export function getQaCitations(params = {}) {
  return http.get('/api/qa/citations', { params })
}

export function normalizeQaHistoryPage(payload) {
  const data = payload || {}
  const records = Array.isArray(data.records)
    ? data.records
    : Array.isArray(data.list)
      ? data.list
      : []
  const total = Number(data.total ?? records.length ?? 0)
  const current = Number(data.current ?? data.pageNo ?? 1)
  const size = Number(data.size ?? data.pageSize ?? 10)
  const pages = Number(data.pages ?? Math.max(1, Math.ceil(total / Math.max(size, 1))))

  return {
    records,
    total,
    current,
    size,
    pages
  }
}
