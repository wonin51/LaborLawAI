import axios from 'axios'

const configuredBaseUrl = import.meta.env.VITE_API_BASE_URL || ''
const normalizedBaseUrl = configuredBaseUrl.replace(/\/$/, '')
const adminToken = import.meta.env.VITE_ADMIN_API_TOKEN || ''

const http = axios.create({
  baseURL: normalizedBaseUrl,
  timeout: 8000
})

http.interceptors.request.use(config => {
  const requestUrl = String(config.url || '')
  const isAdminRequest = requestUrl.startsWith('/api/admin/')

  if (isAdminRequest && adminToken) {
    if (config.headers && typeof config.headers.set === 'function') {
      config.headers.set('X-Admin-Token', adminToken)
    } else {
      config.headers = {
        ...(config.headers || {}),
        'X-Admin-Token': adminToken
      }
    }
  }

  return config
})

http.interceptors.response.use(
  response => {
    const body = response.data

    if (body && typeof body === 'object' && Object.prototype.hasOwnProperty.call(body, 'code')) {
      if (body.code === 0) {
        return body.data
      }

      const error = new Error(body.message || '请求失败')
      error.responseBody = body
      error.responseStatus = response.status
      return Promise.reject(error)
    }

    return body
  },
  error => Promise.reject(error)
)

export async function getHealthStatus() {
  const response = await http.get('/api/health')
  return response
}

export default http
export { configuredBaseUrl, adminToken }
