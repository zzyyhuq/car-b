const { get, post } = require('../../utils/request')

/** 后端商品没有图标字段，按名称推导一个 emoji */
function goodsEmoji(name) {
  const text = name || ''
  if (text.indexOf('精洗') > -1) return '🧴'
  if (text.indexOf('玻璃水') > -1) return '💧'
  if (text.indexOf('喷雾') > -1 || text.indexOf('除味') > -1) return '🌿'
  if (text.indexOf('毛巾') > -1) return '🧽'
  return '🎁'
}

Page({
  data: {
    goods: [],
    points: 0
  },

  onShow() {
    this.loadMall()
  },

  onPullDownRefresh() {
    this.loadMall().then(() => wx.stopPullDownRefresh())
  },

  loadMall() {
    let failed = 0
    const safeGet = (url, data) => get(url, data, { silent: true }).catch(() => {
      failed += 1
      return null
    })

    return Promise.all([
      safeGet('/api/points/goods/list'),
      safeGet('/api/user/profile')
    ]).then(([goods, profile]) => {
      this.setData({
        goods: (goods || []).map(item => ({
          id: item.id,
          name: item.name,
          desc: item.description || '',
          points: item.pointsPrice,
          stock: item.stock || 0,
          emoji: goodsEmoji(item.name)
        })),
        points: profile && profile.points != null ? profile.points : this.data.points
      })
      if (failed > 0) {
        wx.showToast({ title: '部分数据加载失败，请下拉重试', icon: 'none' })
      }
    })
  },

  exchange(e) {
    const id = Number(e.currentTarget.dataset.id)
    const goods = this.data.goods.find(item => item.id === id)
    if (!goods) {
      return
    }
    if (goods.stock <= 0) {
      wx.showToast({ title: '该商品已兑完', icon: 'none' })
      return
    }
    if (this.data.points < goods.points) {
      wx.showToast({ title: '积分不足', icon: 'none' })
      return
    }

    wx.showModal({
      title: '积分兑换',
      content: '确认用 ' + goods.points + ' 积分兑换「' + goods.name + '」？兑换后请到店核销。',
      success: res => {
        if (!res.confirm) {
          return
        }
        post('/api/points/exchange', { goodsId: id }, { loading: true })
          .then(result => {
            this.setData({
              points: result.pointsBalance != null
                ? result.pointsBalance
                : this.data.points - goods.points
            })
            wx.showModal({
              title: '兑换成功',
              content: '兑换单号 ' + (result.exchangeNo || '') + '\n请到店出示单号核销',
              showCancel: false
            })
            // 重新拉商品，刷新库存
            this.loadMall()
          })
          .catch(() => {
            // request.js 已经提示过失败原因
          })
      }
    })
  }
})
