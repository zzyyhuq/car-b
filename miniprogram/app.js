App({
  globalData: {
    userInfo: null
  },

  onLaunch() {
    // 阶段 1 没有后端，全部用本地假数据。
    // 阶段 3 接接口时，这里换成 wx.login 换 token，页面里的 mock 数据换成请求结果。
    if (!wx.getStorageSync('beauty_orders')) {
      wx.setStorageSync('beauty_orders', [])
    }
  }
})
