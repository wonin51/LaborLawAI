import axios from 'axios'

const configuredBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

// 开发环境优先走 Vite /api 代理，避免浏览器跨域；配置值仍保留为 http://localhost:8080。
const isDev = import.meta.env.DEV
const baseURL = isDev ? '/api' : `${configuredBaseUrl.replace(/\/$/, '')}/api`

const http = axios.create({
  baseURL,
  timeout: 5000
})

http.interceptors.response.use(
  response => response.data,
  error => Promise.reject(error)
)

export default http
export { configuredBaseUrl }
