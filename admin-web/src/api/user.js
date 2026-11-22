import http from './http'

export function pageUsers(params) {
  return http.get('/user/page', { params })
}

export function listStaff() {
  return pageUsers({ page: 1, size: 200, role: 'STAFF', status: 1 })
}
