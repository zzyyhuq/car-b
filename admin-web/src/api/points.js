import http from './http'

export function pageGoods(params) {
  return http.get('/points/goods/page', { params })
}

export function createGoods(data) {
  return http.post('/points/goods', data)
}

export function updateGoods(id, data) {
  return http.put('/points/goods/' + id, data)
}

export function updateGoodsStatus(id, status) {
  return http.put('/points/goods/' + id + '/status', null, { params: { status } })
}

export function deleteGoods(id) {
  return http.delete('/points/goods/' + id)
}

export function pageExchanges(params) {
  return http.get('/points/exchange/page', { params })
}

export function verifyExchange(id) {
  return http.put('/points/exchange/' + id + '/verify')
}
