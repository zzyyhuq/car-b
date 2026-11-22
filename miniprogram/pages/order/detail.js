const { get, put } = require('../../utils/request')

function splitDateTime(value) {
  if (!value) {
    return { date: '', time: '', full: '' }
  }
  const parts = String(value).replace('T', ' ').split(' ')
  const date = parts[0] || ''
  const time = (parts[1] || '').slice(0, 5)
  return { date, time, full: date + (time ? ' ' + time : '') }
}

function formatPrice(value) {
  const price = Number(value || 0)
  return Number.isInteger(price) ? price : price.toFixed(2)
}

const STATUS_CLASS = {
  PENDING: 'pending',
  ACCEPTED: 'accepted',
  IN_PROGRESS: 'progress',
  COMPLETED: 'done',
  EVALUATED: 'done',
  CANCELLED: 'cancelled'
}

Page({
  data: {
    order: null,
    timeline: [],
    statusClass: '',
    loading: true,
    cancelling: false
  },

  onLoad(options) {
    const id = Number(options.id)
    if (!id) {
      this.setData({ loading: false })
      wx.showToast({ title: '订单不存在', icon: 'none' })
      return
    }
    this.orderId = id
    this.loadDetail()
  },

  onPullDownRefresh() {
    this.loadDetail().then(() => wx.stopPullDownRefresh())
  },

  loadDetail() {
    const id = this.orderId
    if (!id) {
      return Promise.resolve()
    }
    return Promise.all([
      get('/api/order/' + id, {}, { loading: !this.data.order }),
      // 进度加载失败不阻塞订单信息
      get('/api/service-process/order/' + id, {}, { silent: true }).catch(() => [])
    ]).then(([order, list]) => {
      const appointment = splitDateTime(order.appointmentTime)
      this.setData({
        order: Object.assign({}, order, {
          price: formatPrice(order.amount),
          appointmentDate: appointment.date,
          appointmentTime: appointment.time,
          finishTimeText: splitDateTime(order.finishTime).full
        }),
        statusClass: STATUS_CLASS[order.status] || '',
        timeline: (list || []).map(item => ({
          id: item.id,
          nodeText: item.nodeText || item.node,
          content: item.content || '',
          staffName: item.staffName || '',
          time: splitDateTime(item.createTime).full,
          images: item.images || []
        })),
        loading: false
      })
      wx.setNavigationBarTitle({ title: order.serviceName || '订单详情' })
    }).catch(() => {
      this.setData({ loading: false })
    })
  },

  previewImage(e) {
    const { index, src } = e.currentTarget.dataset
    const item = this.data.timeline[index]
    if (item && item.images && item.images.length) {
      wx.previewImage({ current: src, urls: item.images })
    }
  },

  cancelOrder() {
    if (this.data.cancelling) {
      return
    }
    wx.showModal({
      title: '取消订单',
      content: '确认取消这个预约吗？',
      confirmText: '确认取消',
      confirmColor: '#e34d59',
      success: res => {
        if (!res.confirm) {
          return
        }
        this.setData({ cancelling: true })
        put('/api/order/' + this.orderId + '/cancel', { reason: '车主取消' }, { loading: true })
          .then(() => {
            wx.showToast({ title: '已取消', icon: 'success' })
            this.loadDetail()
          })
          .catch(() => {})
          .then(() => this.setData({ cancelling: false }))
      }
    })
  }
})
