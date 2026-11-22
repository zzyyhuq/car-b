const { get } = require('../../utils/request')

function formatPrice(value) {
  const price = Number(value || 0)
  return Number.isInteger(price) ? price : price.toFixed(2)
}

function formatDateTime(value) {
  if (!value) {
    return { date: '', time: '' }
  }
  const parts = String(value).replace('T', ' ').split(' ')
  return {
    date: parts[0] || '',
    time: (parts[1] || '').slice(0, 5)
  }
}

function toOrderView(item) {
  const appointment = formatDateTime(item.appointmentTime)
  return {
    id: item.id,
    orderNo: item.orderNo,
    serviceName: item.serviceName || '服务项目',
    status: item.statusText || item.status || '',
    carPlate: item.plateNo || '',
    carName: [item.carBrand, item.carModel].filter(Boolean).join(' '),
    date: appointment.date,
    time: appointment.time,
    price: formatPrice(item.amount)
  }
}

Page({
  data: {
    nickname: '车主',
    subtitle: '车主',
    avatar: '',
    cars: [],
    orders: [],
    points: 0,
    goodsCount: 0
  },

  onShow() {
    this.loadMine()
  },

  onPullDownRefresh() {
    this.loadMine().then(() => wx.stopPullDownRefresh())
  },

  /** 资料、车辆、订单、可兑商品四个接口并行，单块失败不影响其它区块 */
  loadMine() {
    let failed = 0
    const safeGet = (url, data) => get(url, data, { silent: true }).catch(() => {
      failed += 1
      return null
    })

    return Promise.all([
      safeGet('/api/user/profile'),
      safeGet('/api/car/my'),
      safeGet('/api/order/my', { page: 1, size: 50 }),
      safeGet('/api/points/goods/list')
    ]).then(([profile, cars, orderPage, goods]) => {
      const phone = profile && profile.phone ? String(profile.phone) : ''
      const phoneTail = phone.length >= 4 ? phone.slice(-4) : ''
      this.setData({
        nickname: (profile && profile.nickname) || '车主',
        subtitle: phoneTail ? '车主 · 尾号 ' + phoneTail : '车主',
        avatar: (profile && profile.avatar) || '',
        points: profile && profile.points != null ? profile.points : 0,
        cars: cars || [],
        orders: ((orderPage && orderPage.records) || []).map(toOrderView),
        goodsCount: (goods || []).length
      })
      if (failed > 0) {
        wx.showToast({ title: '部分数据加载失败，请下拉重试', icon: 'none' })
      }
    })
  },

  goService() {
    wx.switchTab({ url: '/pages/service/list' })
  },

  goMall() {
    wx.switchTab({ url: '/pages/points/mall' })
  },

  goOrders() {
    if (this.data.orders.length === 0) {
      this.goService()
    } else {
      wx.showToast({ title: '订单在下方列表', icon: 'none' })
    }
  },

  goCars() {
    wx.showToast({ title: '车辆管理将在阶段 4 实现', icon: 'none' })
  },

  notReady() {
    wx.showToast({ title: '该功能将在阶段 4 实现', icon: 'none' })
  }
})
