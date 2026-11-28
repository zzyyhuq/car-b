import http from './http'

export function getCacheOverview() {
  return http.get('/cache/overview')
}

export function listCacheKeys(params) {
  return http.get('/cache/keys', { params })
}

export function deleteCacheKey(key) {
  return http.delete('/cache/keys', { params: { key } })
}

export function clearBusinessCache() {
  return http.delete('/cache/business')
}
