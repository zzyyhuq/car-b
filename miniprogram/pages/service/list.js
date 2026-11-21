const { get } = require('../../utils/request')
const { toServiceView } = require('../../utils/service')

Page({
  data: {
    categories: ['全部'],
    activeCategory: '全部',
    keyword: '',
    services: [],
    filtered: []
  },

  onLoad() {
    this.loadServices()
  },

  onPullDownRefresh() {
    this.loadServices().then(() => wx.stopPullDownRefresh())
  },

  loadServices() {
    return get('/api/service-type/list', {}, { loading: true })
      .then(list => {
        const services = (list || []).map(item => toServiceView(item))
        const categories = ['全部'].concat(
          services.map(item => item.category).filter((value, index, arr) => arr.indexOf(value) === index)
        )
        this.setData({ services, categories }, () => this.applyFilter())
        return services
      })
      .catch(() => {
        // request.js 已经弹过错误提示，这里保持空列表
        return []
      })
  },

  switchCategory(e) {
    this.setData({ activeCategory: e.currentTarget.dataset.value }, () => this.applyFilter())
  },

  onSearch(e) {
    this.setData({ keyword: e.detail.value }, () => this.applyFilter())
  },

  applyFilter() {
    const { activeCategory, keyword, services } = this.data
    const text = keyword.trim()
    const filtered = services.filter(item => {
      const matchCategory = activeCategory === '全部' || item.category === activeCategory
      const matchKeyword = !text || item.name.indexOf(text) > -1 || item.brief.indexOf(text) > -1
      return matchCategory && matchKeyword
    })
    this.setData({ filtered })
  },

  goDetail(e) {
    wx.navigateTo({
      url: '/pages/service/detail?id=' + e.currentTarget.dataset.id
    })
  },

  goBook(e) {
    wx.navigateTo({
      url: '/pages/order/create?id=' + e.currentTarget.dataset.id
    })
  }
})
