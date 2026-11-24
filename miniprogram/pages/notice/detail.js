const { get } = require('../../utils/request')

function splitDateTime(value) {
  if (!value) {
    return ''
  }
  const parts = String(value).replace('T', ' ').split(' ')
  return (parts[0] || '') + ((parts[1] || '') ? ' ' + parts[1].slice(0, 5) : '')
}

Page({
  data: {
    notice: null,
    loading: true
  },

  onLoad(options) {
    const id = Number(options.id)
    if (!id) {
      this.setData({ loading: false })
      return
    }
    this.noticeId = id
    this.loadNotice()
  },

  onPullDownRefresh() {
    this.loadNotice().then(() => wx.stopPullDownRefresh())
  },

  loadNotice() {
    if (!this.noticeId) {
      return Promise.resolve()
    }
    return get('/api/notice/' + this.noticeId, {}, { loading: !this.data.notice })
      .then(data => {
        this.setData({
          notice: Object.assign({}, data, {
            publishTimeText: splitDateTime(data.publishTime)
          }),
          loading: false
        })
      })
      .catch(() => {
        this.setData({ loading: false })
      })
  }
})
