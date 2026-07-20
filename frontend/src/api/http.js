import axios from 'axios'

const configuredBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://127.0.0.1:8080'
const normalizedBaseUrl = configuredBaseUrl.replace(/\/$/, '')

const http = axios.create({
  baseURL: import.meta.env.DEV ? '/api' : `${normalizedBaseUrl}/api`,
  timeout: 5000
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
      return Promise.reject(error)
    }

    return body
  },
  error => Promise.reject(error)
)

export default http
export { configuredBaseUrl }
