const { get } = require('../../utils/request')

const STATUS_CLASS = {
  PENDING: 'pending',
  VERIFIED: 'verified',
  CANCELLED: 'cancelled'
}

function formatDateTime(value) {
  if (!value) {
    return ''
  }
  const parts = String(value).replace('T', ' ').split(' ')
  return (parts[0] || '') + ((parts[1] || '') ? ' ' + parts[1].slice(0, 5) : '')
}

Page({
  data: {
    exchanges: [],
    loading: true
  },

  onShow() {
    this.loadExchanges()
  },

  onPullDownRefresh() {
    this.loadExchanges().then(() => wx.stopPullDownRefresh())
  },

  loadExchanges() {
    return get('/api/points/exchange/my', { limit: 50 }, {
      loading: !this.data.exchanges.length
    }).then(list => {
      this.setData({
        exchanges: (list || []).map(item => ({
          id: item.id,
          exchangeNo: item.exchangeNo,
          goodsName: item.goodsName || '商品已删除',
          pointsCost: item.pointsCost,
          status: item.status,
          statusText: item.statusText || '',
          statusClass: STATUS_CLASS[item.status] || '',
          exchangeTime: formatDateTime(item.exchangeTime),
          verifyTime: formatDateTime(item.verifyTime)
        })),
        loading: false
      })
    }).catch(() => {
      this.setData({ loading: false })
    })
  }
})
