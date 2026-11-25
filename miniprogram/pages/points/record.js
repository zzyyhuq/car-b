const { get } = require('../../utils/request')

const TYPE_TEXT = {
  ORDER_GAIN: '订单获得',
  EXCHANGE_COST: '兑换消耗',
  ADMIN_ADJUST: '管理员调整'
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
    records: [],
    points: 0,
    loading: true
  },

  onShow() {
    this.loadRecords()
  },

  onPullDownRefresh() {
    this.loadRecords().then(() => wx.stopPullDownRefresh())
  },

  loadRecords() {
    return Promise.all([
      get('/api/points/record/my', { limit: 100 }, { loading: !this.data.records.length }),
      get('/api/user/profile', {}, { silent: true }).catch(() => null)
    ]).then(([list, profile]) => {
      this.setData({
        points: profile && profile.points != null ? profile.points : this.data.points,
        records: (list || []).map(item => ({
          id: item.id,
          amount: item.changeAmount,
          amountText: (item.changeAmount > 0 ? '+' : '') + item.changeAmount,
          positive: item.changeAmount > 0,
          typeText: TYPE_TEXT[item.type] || item.type,
          remark: item.remark || '',
          time: formatDateTime(item.createTime)
        })),
        loading: false
      })
    }).catch(() => {
      this.setData({ loading: false })
    })
  }
})
