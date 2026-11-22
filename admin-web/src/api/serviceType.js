import http from './http'

export function pageServiceTypes(params) {
  return http.get('/service-type/page', { params })
}

export function createServiceType(data) {
  return http.post('/service-type', data)
}

export function updateServiceType(id, data) {
  return http.put('/service-type/' + id, data)
}

export function updateServiceTypeStatus(id, status) {
  return http.put('/service-type/' + id + '/status', null, { params: { status } })
}

export function deleteServiceType(id) {
  return http.delete('/service-type/' + id)
}
