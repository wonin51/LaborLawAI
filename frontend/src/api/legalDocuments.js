import { http } from './http'

export function getLegalDocuments(params) {
  return http.get('/legal-docs', { params })
}

export function getLegalDocumentDetail(id) {
  return http.get(`/legal-docs/${id}`)
}

export function createLegalDocument(payload) {
  return http.post('/legal-docs', payload)
}
