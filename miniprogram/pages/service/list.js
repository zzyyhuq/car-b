const mock = require('../../mock/data.js')

Page({
  data: {
    categories: mock.categories,
    activeCategory: '全部',
    keyword: '',
    filtered: mock.services
  },

  onShow() {
    this.applyFilter()
  },

  switchCategory(e) {
    this.setData({ activeCategory: e.currentTarget.dataset.value }, () => this.applyFilter())
  },

  onSearch(e) {
    this.setData({ keyword: e.detail.value }, () => this.applyFilter())
  },

  applyFilter() {
    const { activeCategory, keyword } = this.data
    const text = keyword.trim()
    const filtered = mock.services.filter(item => {
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
