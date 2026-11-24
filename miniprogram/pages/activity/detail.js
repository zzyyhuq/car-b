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
    activity: null,
    loading: true
  },

  onLoad(options) {
    const id = Number(options.id)
    if (!id) {
      this.setData({ loading: false })
      return
    }
    this.activityId = id
    this.loadActivity()
  },

  onPullDownRefresh() {
    this.loadActivity().then(() => wx.stopPullDownRefresh())
  },

  loadActivity() {
    if (!this.activityId) {
      return Promise.resolve()
    }
    return get('/api/activity/' + this.activityId, {}, { loading: !this.data.activity })
      .then(data => {
        this.setData({
          activity: Object.assign({}, data, {
            startTimeText: splitDateTime(data.startTime),
            endTimeText: splitDateTime(data.endTime)
          }),
          loading: false
        })
      })
      .catch(() => {
        this.setData({ loading: false })
      })
  }
})
