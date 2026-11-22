export function formatMoney(value) {
  const amount = Number(value || 0)
  return amount.toFixed(2)
}

export function formatDateTime(value) {
  if (!value) return '—'
  return String(value).replace('T', ' ').slice(0, 16)
}

/** 去掉空字符串 / null，避免把 status= 传给 Integer 参数 */
export function cleanParams(source) {
  const params = {}
  Object.keys(source).forEach(key => {
    const value = source[key]
    if (value !== '' && value !== null && value !== undefined) {
      params[key] = value
    }
  })
  return params
}

export const ROLE_TEXT = {
  ADMIN: '管理员',
  STAFF: '门店员工',
  OWNER: '车主'
}

export const ORDER_STATUS_TEXT = {
  PENDING: '待接单',
  ACCEPTED: '已接单',
  IN_PROGRESS: '服务中',
  COMPLETED: '已完成',
  EVALUATED: '已评价',
  CANCELLED: '已取消'
}

export const ORDER_STATUS_TYPE = {
  PENDING: 'warning',
  ACCEPTED: 'primary',
  IN_PROGRESS: 'primary',
  COMPLETED: 'success',
  EVALUATED: 'info',
  CANCELLED: 'danger'
}
