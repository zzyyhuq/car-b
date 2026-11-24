import http from './http'

export function pageMessages(params) {
  return http.get('/message/page', { params })
}

export function replyMessage(id, reply) {
  return http.put('/message/' + id + '/reply', { reply })
}

export function auditMessage(id, status) {
  return http.put('/message/' + id + '/audit', { status })
}
