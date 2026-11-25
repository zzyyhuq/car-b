const { get, post, put } = require('../../utils/request')

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
    signup: null,
    signing: false,
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
        if (data.needSignup === 1 && data.status === 1) {
          this.loadSignup()
        }
      })
      .catch(() => {
        this.setData({ loading: false })
      })
  },

  loadSignup() {
    return get('/api/activity/' + this.activityId + '/signup/my', {}, { silent: true })
      .then(signup => {
        this.setData({ signup: signup || null })
      })
      .catch(() => {
        this.setData({ signup: null })
      })
  },

  signupActivity() {
    if (this.data.signing) {
      return
    }
    this.setData({ signing: true })
    post('/api/activity/' + this.activityId + '/signup', {}, { loading: true })
      .then(() => {
        wx.showToast({ title: '报名成功', icon: 'success' })
        this.loadSignup()
      })
      .catch(() => {})
      .then(() => this.setData({ signing: false }))
  },

  cancelSignup() {
    if (this.data.signing) {
      return
    }
    wx.showModal({
      title: '取消报名',
      content: '确认取消这个活动的报名吗？',
      success: res => {
        if (!res.confirm) {
          return
        }
        this.setData({ signing: true })
        put('/api/activity/' + this.activityId + '/signup/cancel', {}, { loading: true })
          .then(() => {
            wx.showToast({ title: '已取消', icon: 'success' })
            this.loadSignup()
          })
          .catch(() => {})
          .then(() => this.setData({ signing: false }))
      }
    })
  }
})
