import { http } from './http'

export function getHealthStatus() {
  return http.get('/health')
}

export function pingDatabase() {
  return http.get('/db/ping')
}
