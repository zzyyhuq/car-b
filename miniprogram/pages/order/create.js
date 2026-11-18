const mock = require('../../mock/data.js')

function pad(n) {
  return n < 10 ? '0' + n : '' + n
}

function formatDate(date) {
  return date.getFullYear() + '-' + pad(date.getMonth() + 1) + '-' + pad(date.getDate())
}

function formatDateTime(date) {
  return formatDate(date) + ' ' + pad(date.getHours()) + ':' + pad(date.getMinutes())
}

Page({
  data: {
    service: null,
    cars: mock.cars,
    carId: null,
    minDate: formatDate(new Date()),
    date: formatDate(new Date(Date.now() + 24 * 60 * 60 * 1000)),
    timeSlots: ['09:00', '10:00', '11:00', '14:00', '15:00', '16:00', '17:00'],
    time: '',
    remark: '',
    submitting: false
  },

  onLoad(options) {
    const service = mock.services.find(item => item.id === Number(options.id)) || mock.services[0]
    this.setData({
      service,
      carId: mock.cars[0].id
    })
    wx.setNavigationBarTitle({ title: '预约 ' + service.name })
  },

  chooseCar(e) {
    this.setData({ carId: Number(e.currentTarget.dataset.id) })
  },

  addCar() {
    wx.showToast({ title: '车辆管理将在阶段 4 实现', icon: 'none' })
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
    const { service, cars, carId, date, time, remark, submitting } = this.data
    if (submitting) {
      return
    }
    if (!time) {
      wx.showToast({ title: '请选择预约时间', icon: 'none' })
      return
    }
    const car = cars.find(item => item.id === carId)
    const orders = wx.getStorageSync('beauty_orders') || []
    const order = {
      id: Date.now(),
      orderNo: 'BM' + String(Date.now()).slice(-10),
      serviceId: service.id,
      serviceName: service.name,
      price: service.price,
      carPlate: car.plateNo,
      carName: car.brand + ' ' + car.model,
      date,
      time,
      remark,
      status: '待接单',
      createTime: formatDateTime(new Date())
    }
    orders.unshift(order)
    wx.setStorageSync('beauty_orders', orders)

    this.setData({ submitting: true })
    wx.showModal({
      title: '预约成功',
      content: service.name + '\n' + date + ' ' + time + '\n请按时到店，订单可在「我的」中查看',
      showCancel: false,
      success: () => {
        this.setData({ submitting: false })
        wx.switchTab({ url: '/pages/mine/index' })
      }
    })
  }
})
