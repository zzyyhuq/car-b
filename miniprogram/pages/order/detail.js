const { get, post, put } = require('../../utils/request')

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

function starsText(rating) {
  const value = Number(rating || 0)
  return '★'.repeat(value) + '☆'.repeat(Math.max(0, 5 - value))
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
    evaluation: null,
    stars: [1, 2, 3, 4, 5],
    rating: 5,
    evaluateContent: '',
    evaluating: false,
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
      get('/api/service-process/order/' + id, {}, { silent: true }).catch(() => []),
      // 没有评价时返回 null
      get('/api/message/order/' + id, {}, { silent: true }).catch(() => null)
    ]).then(([order, list, evaluationData]) => {
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
        evaluation: evaluationData ? {
          rating: evaluationData.rating || 0,
          starsText: starsText(evaluationData.rating),
          content: evaluationData.content || '',
          reply: evaluationData.reply || '',
          replyTimeText: splitDateTime(evaluationData.replyTime).full,
          createTimeText: splitDateTime(evaluationData.createTime).full
        } : null,
        loading: false
      })
      wx.setNavigationBarTitle({ title: order.serviceName || '订单详情' })
    }).catch(() => {
      this.setData({ loading: false })
    })
  },

  chooseRating(e) {
    this.setData({ rating: Number(e.currentTarget.dataset.value) })
  },

  onEvaluateInput(e) {
    this.setData({ evaluateContent: e.detail.value })
  },

  submitEvaluate() {
    if (this.data.evaluating) {
      return
    }
    if (!this.data.rating) {
      wx.showToast({ title: '请选择星级', icon: 'none' })
      return
    }
    this.setData({ evaluating: true })
    post('/api/message/evaluate', {
      orderId: this.orderId,
      rating: this.data.rating,
      content: (this.data.evaluateContent || '').trim()
    }, { loading: true })
      .then(() => {
        wx.showToast({ title: '评价成功', icon: 'success' })
        this.loadDetail()
      })
      .catch(() => {})
      .then(() => this.setData({ evaluating: false }))
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
