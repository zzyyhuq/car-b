const { get, post } = require('../../utils/request')
const { toServiceView } = require('../../utils/service')

function pad(n) {
  return n < 10 ? '0' + n : '' + n
}

function formatDate(date) {
  return date.getFullYear() + '-' + pad(date.getMonth() + 1) + '-' + pad(date.getDate())
}

Page({
  data: {
    service: null,
    cars: [],
    carId: null,
    loading: true,
    minDate: formatDate(new Date()),
    date: formatDate(new Date(Date.now() + 24 * 60 * 60 * 1000)),
    timeSlots: ['09:00', '10:00', '11:00', '14:00', '15:00', '16:00', '17:00'],
    time: '',
    remark: '',
    submitting: false
  },

  onLoad(options) {
    const id = Number(options.id)
    if (!id) {
      this.setData({ loading: false })
      wx.showToast({ title: '缺少服务项目', icon: 'none' })
      return
    }

    Promise.all([
      get('/api/service-type/' + id, {}, { loading: true }),
      // 车辆加载失败不阻塞服务展示，提交时会再提示
      get('/api/car/my', {}, { silent: true }).catch(() => [])
    ]).then(([data, cars]) => {
      const service = toServiceView(data)
      this.setData({
        service,
        cars: cars || [],
        carId: cars && cars.length ? cars[0].id : null,
        loading: false
      })
      wx.setNavigationBarTitle({ title: '预约 ' + service.name })
    }).catch(() => {
      this.setData({ loading: false })
    })
  },

  onShow() {
    // 从车辆编辑页返回时刷新车辆列表
    if (this.data.service) {
      this.loadCars()
    }
  },

  loadCars() {
    return get('/api/car/my', {}, { silent: true }).catch(() => []).then(cars => {
      const list = cars || []
      const current = this.data.carId
      const exists = list.some(item => item.id === current)
      this.setData({
        cars: list,
        carId: exists ? current : (list.length ? list[0].id : null)
      })
      return list
    })
  },

  chooseCar(e) {
    this.setData({ carId: Number(e.currentTarget.dataset.id) })
  },

  addCar() {
    wx.navigateTo({ url: '/pages/car/edit' })
  },

  onDateChange(e) {
    this.setData({ date: e.detail.value })
  },

  chooseTime(e) {
    this.setData({ time: e.currentTarget.dataset.value })
  },

  onRemark(e) {
    this.setData({ remark: e.detail.value })
  },

  submit() {
    const { service, carId, date, time, remark, submitting } = this.data
    if (submitting) {
      return
    }
    if (!service) {
      wx.showToast({ title: '服务项目加载失败', icon: 'none' })
      return
    }
    if (!carId) {
      wx.showToast({ title: '请先选择车辆', icon: 'none' })
      return
    }
    if (!time) {
      wx.showToast({ title: '请选择预约时间', icon: 'none' })
      return
    }
    const appointmentTime = date + 'T' + time + ':00'
    if (new Date(appointmentTime).getTime() <= Date.now()) {
      wx.showToast({ title: '预约时间需晚于当前时间', icon: 'none' })
      return
    }

    this.setData({ submitting: true })
    post('/api/order', {
      carId,
      serviceTypeId: service.id,
      appointmentTime,
      remark: (remark || '').trim()
    }, { loading: true })
      // 再查一次详情拿订单号，失败也不影响预约结果
      .then(id => get('/api/order/' + id, {}, { silent: true }).catch(() => null))
      .then(order => {
        this.setData({ submitting: false })
        const lines = [service.name]
        if (order && order.orderNo) {
          lines.push('单号 ' + order.orderNo)
        }
        lines.push(date + ' ' + time)
        lines.push('请按时到店，服务完成后到店支付')
        wx.showModal({
          title: '预约成功',
          content: lines.join('\n'),
          showCancel: false,
          success: () => wx.switchTab({ url: '/pages/mine/index' })
        })
      })
      .catch(() => {
        this.setData({ submitting: false })
      })
  }
})
