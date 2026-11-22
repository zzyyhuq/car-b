const { get } = require('../../utils/request')
const { toServiceView } = require('../../utils/service')

/** 后端活动表只存文字，首页卡片背景色按顺序分配 */
const ACTIVITY_BGS = [
  'linear-gradient(135deg, #1677ff, #41b3ff)',
  'linear-gradient(135deg, #7c4dff, #b388ff)',
  'linear-gradient(135deg, #ff7a45, #ffb36b)'
]

Page({
  data: {
    notices: [],
    activities: [],
    recommends: []
  },

  onLoad() {
    this.loadHome()
  },

  onPullDownRefresh() {
    this.loadHome().then(() => wx.stopPullDownRefresh())
  },

  /**
   * 三块数据分别来自服务项目、公告、活动接口。
   * 单块失败不影响其它区块，最后统一提示一次。
   */
  loadHome() {
    let failed = 0
    const safeGet = (url, data) => get(url, data, { silent: true }).catch(() => {
      failed += 1
      return []
    })

    return Promise.all([
      safeGet('/api/service-type/list'),
      safeGet('/api/notice/list', { limit: 5 }),
      safeGet('/api/activity/list', { limit: 5 })
    ]).then(([services, notices, activities]) => {
      this.setData({
        recommends: (services || []).slice(0, 3).map(item => toServiceView(item)),
        notices: (notices || [])
          .map(item => item.title || item.content || '')
          .filter(Boolean),
        activities: (activities || []).map((item, index) => ({
          id: item.id,
          title: item.title,
          desc: item.content || '',
          bg: ACTIVITY_BGS[index % ACTIVITY_BGS.length]
        }))
      })
      if (failed > 0) {
        wx.showToast({ title: '部分内容加载失败，请下拉重试', icon: 'none' })
      }
    })
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
