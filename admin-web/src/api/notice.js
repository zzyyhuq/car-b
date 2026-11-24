import http from './http'

export function pageNotices(params) {
  return http.get('/notice/page', { params })
}

export function createNotice(data) {
  return http.post('/notice', data)
}

export function updateNotice(id, data) {
  return http.put('/notice/' + id, data)
}

export function updateNoticeStatus(id, status) {
  return http.put('/notice/' + id + '/status', null, { params: { status } })
}

export function deleteNotice(id) {
  return http.delete('/notice/' + id)
}
