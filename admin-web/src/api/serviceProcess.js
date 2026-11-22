import http from './http'

export function listProcess(orderId) {
  return http.get('/service-process/order/' + orderId)
}

export function addProcess(data) {
  return http.post('/service-process', data)
}
