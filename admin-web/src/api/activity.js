import http from './http'

export function pageActivities(params) {
  return http.get('/activity/page', { params })
}

export function createActivity(data) {
  return http.post('/activity', data)
}

export function updateActivity(id, data) {
  return http.put('/activity/' + id, data)
}

export function updateActivityStatus(id, status) {
  return http.put('/activity/' + id + '/status', null, { params: { status } })
}

export function deleteActivity(id) {
  return http.delete('/activity/' + id)
}
