const { get } = require('../../utils/request')

function splitDateTime(value) {
  if (!value) {
    return ''
  }
  const parts = String(value).replace('T', ' ').split(' ')
  const date = parts[0] || ''
  const time = (parts[1] || '').slice(0, 5)
  return date + (time ? ' ' + time : '')
}

function starsText(rating) {
  const value = Number(rating || 0)
  if (!value) {
    return ''
  }
  return '★'.repeat(value) + '☆'.repeat(Math.max(0, 5 - value))
}

Page({
  data: {
    messages: [],
    loading: true
  },

  onShow() {
    this.loadMessages()
  },

  onPullDownRefresh() {
    this.loadMessages().then(() => wx.stopPullDownRefresh())
  },

  loadMessages() {
    return get('/api/message/my', { limit: 50 }, { loading: !this.data.messages.length })
      .then(list => {
        this.setData({
          messages: (list || []).map(item => ({
            id: item.id,
            type: item.type,
            typeText: item.typeText || (item.type === 'EVALUATION' ? '评价' : '留言'),
            rating: item.rating || 0,
            stars: starsText(item.rating),
            content: item.content || '',
            reply: item.reply || '',
            replyTime: splitDateTime(item.replyTime),
            status: item.status,
            statusText: item.statusText || '',
            orderNo: item.orderNo || '',
            time: splitDateTime(item.createTime)
          })),
          loading: false
        })
      })
      .catch(() => {
        this.setData({ loading: false })
      })
  },

  goFeedback() {
    wx.navigateTo({ url: '/pages/message/feedback' })
  }
})
