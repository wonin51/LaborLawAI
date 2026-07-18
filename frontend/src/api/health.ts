import http from './http'

interface HealthResponse {
  status: string
}

export async function checkHealth(): Promise<boolean> {
  try {
    const response = await http.get<HealthResponse>('/api/health')
    return response.data.status === 'ok'
  } catch {
    return false
  }
}
