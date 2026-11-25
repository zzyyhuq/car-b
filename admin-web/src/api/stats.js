import http from './http'

export function getOverview() {
  return http.get('/stats/overview')
}
