const { post } = require('../../utils/request')

Page({
  data: {
    content: '',
    submitting: false
  },

  onInput(e) {
    this.setData({ content: e.detail.value })
  },

  submit() {
    const content = (this.data.content || '').trim()
    if (!content) {
      wx.showToast({ title: '请输入留言内容', icon: 'none' })
      return
    }
    if (this.data.submitting) {
      return
    }

    this.setData({ submitting: true })
    post('/api/message', { content }, { loading: true })
      .then(() => {
        wx.showModal({
          title: '提交成功',
          content: '留言已提交，管理员回复后可在「我的留言与评价」中查看。',
          showCancel: false,
          success: () => {
            const pages = getCurrentPages()
            const previous = pages.length > 1 ? pages[pages.length - 2] : null
            if (previous && previous.route === 'pages/message/list') {
              wx.navigateBack()
            } else {
              wx.redirectTo({ url: '/pages/message/list' })
            }
          }
        })
      })
      .catch(() => {})
      .then(() => this.setData({ submitting: false }))
  }
})
