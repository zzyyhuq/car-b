const { BASE_URL, USE_WX_LOGIN, WX_LOGIN_FALLBACK } = require('./config')
const { TOKEN_KEY, USER_KEY } = require('./utils/request')

const LOGIN_MODE_KEY = 'beauty_login_mode'
const LOGIN_MODE = USE_WX_LOGIN ? 'wx' : 'demo'

/**
 * 登录策略：
 * - 正式：wx.login → POST /api/auth/wx-login，后端用 code 换 openid
 * - 开发：没有 AppSecret 时用演示车主 owner001
 * - USE_WX_LOGIN / WX_LOGIN_FALLBACK 在 config.js 中切换
 */
App({
  globalData: {
    userInfo: null,
    token: ''
  },

  /** 登录中的 Promise，避免并发请求重复登录 */
  loginPromise: null,

  onLaunch() {
    // 切换登录方式后清掉旧 token，避免继续用上一个账号
    const savedMode = wx.getStorageSync(LOGIN_MODE_KEY)
    if (savedMode && savedMode !== LOGIN_MODE) {
      this.clearSession()
    }
    wx.setStorageSync(LOGIN_MODE_KEY, LOGIN_MODE)

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

    const request = USE_WX_LOGIN
      ? this.wxLogin().catch(err => {
          if (!WX_LOGIN_FALLBACK) {
            wx.showToast({ title: err.message || '微信登录失败', icon: 'none' })
            throw err
          }
          console.warn('微信登录失败，回退演示账号：', err.message)
          wx.showToast({ title: '微信登录未配置，已使用演示账号', icon: 'none' })
          return this.demoLogin()
        })
      : this.demoLogin()

    this.loginPromise = request.then(data => {
      this.setSession(data)
      return data.token
    })

    // 不管成功失败都释放，下一次可以重新登录
    this.loginPromise.then(
      () => { this.loginPromise = null },
      () => { this.loginPromise = null }
    )
    return this.loginPromise
  },

  /** wx.login 拿 code，再交给后端换 openid */
  wxLogin() {
    return new Promise((resolve, reject) => {
      wx.login({
        success: res => {
          if (!res.code) {
            reject(new Error('微信登录失败：未获取到 code'))
            return
          }
          this.requestLogin('/api/auth/wx-login', { code: res.code })
            .then(resolve)
            .catch(reject)
        },
        fail: () => reject(new Error('微信登录失败，请重试'))
      })
    })
  },

  /** 开发期演示账号 */
  demoLogin() {
    return this.requestLogin('/api/auth/login', {
      username: 'owner001',
      password: 'owner123'
    }).catch(err => {
      wx.showToast({ title: err.message || '登录失败', icon: 'none' })
      throw err
    })
  },

  /** 统一登录请求，成功返回 LoginVO */
  requestLogin(url, data) {
    return new Promise((resolve, reject) => {
      wx.request({
        url: BASE_URL + url,
        method: 'POST',
        data,
        header: { 'Content-Type': 'application/json' },
        success: res => {
          const body = res.data || {}
          if (res.statusCode === 200 && body.code === 200 && body.data && body.data.token) {
            resolve(body.data)
            return
          }
          reject(new Error(body.message || '登录失败'))
        },
        fail: () => reject(new Error('无法连接后端，请检查地址和防火墙'))
      })
    })
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
