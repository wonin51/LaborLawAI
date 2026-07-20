import axios from 'axios'

export const configuredBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

const apiBaseUrl = import.meta.env.DEV
  ? '/api'
  : `${configuredBaseUrl.replace(/\/$/, '')}/api`

export const http = axios.create({
  baseURL: apiBaseUrl,
  timeout: 8000
})

http.interceptors.response.use(
  (response) => response.data,
  (error) => Promise.reject(error)
)
