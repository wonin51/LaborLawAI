import axios from 'axios'

const configuredBaseUrl = import.meta.env.VITE_API_BASE_URL || ''
const normalizedBaseUrl = configuredBaseUrl.replace(/\/$/, '')
const adminToken = import.meta.env.VITE_ADMIN_API_TOKEN || ''

const http = axios.create({
  baseURL: normalizedBaseUrl,
  timeout: 8000
})

// 请求拦截器：统一处理参数
http.interceptors.request.use(config => {
  const requestUrl = String(config.url || '')
  const isAdminRequest = requestUrl.startsWith('/api/admin/')

  // 1. 处理 Admin Token
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

  // 2. 统一处理分页参数：pageNo → page_no, pageSize → page_size
  if (config.method === 'get' && config.params) {
    const newParams = { ...config.params }
    
    // 如果存在 pageNo 且没有 page_no，则转换
    if ('pageNo' in newParams && !('page_no' in newParams)) {
      newParams.page_no = newParams.pageNo
      delete newParams.pageNo
    }
    
    if ('pageSize' in newParams && !('page_size' in newParams)) {
      newParams.page_size = newParams.pageSize
      delete newParams.pageSize
    }
    
    config.params = newParams
  }

  return config
})

// 响应拦截器
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