import http from './http'

export function pageOrders(params) {
  return http.get('/order/page', { params })
}

export function getOrder(id) {
  return http.get('/order/' + id)
}

export function assignOrder(id, staffId) {
  return http.put('/order/' + id + '/assign', { staffId })
}

export function updateOrderStatus(id, status) {
  return http.put('/order/' + id + '/status', { status })
}

export function cancelOrder(id, reason) {
  return http.put('/order/' + id + '/cancel', { reason })
}
