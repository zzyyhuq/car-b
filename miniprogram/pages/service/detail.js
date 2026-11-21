const { get } = require('../../utils/request')
const { toServiceView, descriptionItems } = require('../../utils/service')

Page({
  data: {
    service: null,
    loading: true
  },

  onLoad(options) {
    const id = Number(options.id)
    if (!id) {
      this.setData({ loading: false })
      return
    }
    this.loadService(id)
  },

  loadService(id) {
    get('/api/service-type/' + id, {}, { loading: true })
      .then(data => {
        const service = toServiceView(data)
        // 后端没有“服务内容”字段，从描述里拆出条目展示
        service.items = descriptionItems(data.description)
        this.setData({ service, loading: false })
        wx.setNavigationBarTitle({ title: service.name })
      })
      .catch(() => {
        // request.js 已提示错误，页面显示空状态
        this.setData({ loading: false })
      })
  },

  book() {
    if (!this.data.service) {
      return
    }
    wx.navigateTo({
      url: '/pages/order/create?id=' + this.data.service.id
    })
  }
})
