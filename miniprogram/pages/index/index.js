const mock = require('../../mock/data.js')

Page({
  data: {
    notices: mock.notices,
    activities: mock.activities,
    recommends: mock.services.slice(0, 3)
  },

  goDetail(e) {
    wx.navigateTo({
      url: '/pages/service/detail?id=' + e.currentTarget.dataset.id
    })
  },

  goServiceList() {
    wx.switchTab({ url: '/pages/service/list' })
  },

  goMine() {
    wx.switchTab({ url: '/pages/mine/index' })
  }
})
