const mock = require('../../mock/data.js')

Page({
  data: {
    goods: mock.goods,
    points: 120
  },

  exchange(e) {
    const id = Number(e.currentTarget.dataset.id)
    const goods = mock.goods.find(item => item.id === id)
    wx.showModal({
      title: '积分兑换',
      content: '确认用 ' + goods.points + ' 积分兑换「' + goods.name + '」？',
      success: res => {
        if (res.confirm) {
          wx.showToast({ title: '兑换功能将在阶段 4 接入', icon: 'none' })
        }
      }
    })
  }
})
