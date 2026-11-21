const { BASE_URL } = require('./config')
const { TOKEN_KEY, USER_KEY } = require('./utils/request')

/**
 * 阶段 3：小程序开始接后端。
 * 登录暂时用演示车主账号（owner001 / owner123），等配置好小程序 AppSecret 后，
 * 把 login() 换成 wx.login → POST /api/auth/wx-login 即可，页面不用改。
 */
App({
  globalData: {
    userInfo: null,
    token: ''
  },

  /** 登录中的 Promise，避免并发请求重复登录 */
  loginPromise: null,

  onLaunch() {
    if (!wx.getStorageSync('beauty_orders')) {
      wx.setStorageSync('beauty_orders', [])
    }
    this.globalData.token = wx.getStorageSync(TOKEN_KEY) || ''
    this.globalData.userInfo = wx.getStorageSync(USER_KEY) || null
    // 提前登录，页面第一次请求时不用等
    this.ensureLogin().catch(err => console.warn('自动登录失败：', err.message))
  },

  ensureLogin() {
    if (this.globalData.token) {
      return Promise.resolve(this.globalData.token)
    }
    return this.login()
  },

  login() {
    if (this.loginPromise) {
      return this.loginPromise
    }
    this.loginPromise = new Promise((resolve, reject) => {
      wx.request({
        url: BASE_URL + '/api/auth/login',
        method: 'POST',
        data: { username: 'owner001', password: 'owner123' },
        header: { 'Content-Type': 'application/json' },
        success: res => {
          const body = res.data || {}
          if (res.statusCode === 200 && body.code === 200 && body.data && body.data.token) {
            this.setSession(body.data)
            resolve(body.data.token)
            return
          }
          const message = body.message || '登录失败'
          wx.showToast({ title: message, icon: 'none' })
          reject(new Error(message))
        },
        fail: () => {
          wx.showToast({ title: '无法连接后端，请检查地址和防火墙', icon: 'none' })
          reject(new Error('无法连接后端'))
        },
        complete: () => {
          this.loginPromise = null
        }
      })
    })
    return this.loginPromise
  },

  setSession(data) {
    this.globalData.token = data.token
    this.globalData.userInfo = data
    wx.setStorageSync(TOKEN_KEY, data.token)
    wx.setStorageSync(USER_KEY, data)
  },

  clearSession() {
    this.globalData.token = ''
    this.globalData.userInfo = null
    wx.removeStorageSync(TOKEN_KEY)
    wx.removeStorageSync(USER_KEY)
  }
})
