const mock = require('../../mock/data.js')

Page({
  data: {
    service: null
  },

  onLoad(options) {
    const id = Number(options.id)
    const service = mock.services.find(item => item.id === id) || mock.services[0]
    this.setData({ service })
    wx.setNavigationBarTitle({ title: service.name })
  },

  book() {
    wx.navigateTo({
      url: '/pages/order/create?id=' + this.data.service.id
    })
  }
})
