import http from './http'

export function getKnowledgeDocuments(params = {}) {
  return http.get('/api/admin/knowledge/documents', { params })
}

export function getKnowledgeDocumentDetail(id) {
  return http.get(`/api/admin/knowledge/documents/${encodeURIComponent(id)}`)
}

export function createKnowledgeDocument(payload) {
  return http.post('/api/admin/knowledge/documents', payload)
}

export function updateKnowledgeDocument(id, payload) {
  return http.put(`/api/admin/knowledge/documents/${encodeURIComponent(id)}`, payload)
}

export function deleteKnowledgeDocument(id) {
  return http.delete(`/api/admin/knowledge/documents/${encodeURIComponent(id)}`)
}

export function updateKnowledgeDocumentStatus(id, status) {
  return http.patch(`/api/admin/knowledge/documents/${encodeURIComponent(id)}/status`, { status })
}

export function normalizeKnowledgeDocumentPage(payload) {
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
