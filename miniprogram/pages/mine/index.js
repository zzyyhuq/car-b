const mock = require('../../mock/data.js')

Page({
  data: {
    cars: mock.cars,
    orders: [],
    points: 120,
    goodsCount: mock.goods.length
  },

  onShow() {
    this.refreshOrders()
  },

  refreshOrders() {
    const orders = wx.getStorageSync('beauty_orders') || []
    this.setData({ orders })
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
