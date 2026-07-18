import axios from 'axios'

export const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 5000
})

export async function getHealthStatus() {
  const response = await http.get('/api/health')
  return response.data
}
