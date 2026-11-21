/**
 * 统一请求封装。
 * 负责：拼 BASE_URL、自动带 token、解析 { code, message, data }、401 清理登录态、失败提示。
 * 页面里只需要：const { get } = require('../../utils/request'); get('/api/service-type/list')
 */
const { BASE_URL } = require('../config')

const TOKEN_KEY = 'beauty_token'
const USER_KEY = 'beauty_user'

function getToken() {
  return wx.getStorageSync(TOKEN_KEY) || ''
}

function clearSession() {
  wx.removeStorageSync(TOKEN_KEY)
  wx.removeStorageSync(USER_KEY)
  const app = getApp()
  if (app && app.clearSession) {
    app.clearSession()
  }
}

/** 没有 token 时先等 app 登录完成，再发请求 */
function ensureToken(auth) {
  if (!auth) {
    return Promise.resolve('')
  }
  const token = getToken()
  if (token) {
    return Promise.resolve(token)
  }
  const app = getApp()
  if (!app || !app.login) {
    return Promise.reject(new Error('未登录'))
  }
  return app.login()
}

function request(options) {
  const {
    url,
    method = 'GET',
    data = {},
    auth = true,
    loading = false,
    silent = false
  } = options

  return ensureToken(auth).then(token => new Promise((resolve, reject) => {
    if (loading) {
      wx.showLoading({ title: '加载中', mask: true })
    }
    wx.request({
      url: BASE_URL + url,
      method,
      data,
      header: Object.assign(
        { 'Content-Type': 'application/json' },
        token ? { Authorization: 'Bearer ' + token } : {}
      ),
      success(res) {
        // 先关 loading 再弹 toast，否则 hideLoading 会把 toast 一起关掉
        if (loading) {
          wx.hideLoading()
        }
        const body = res.data || {}
        if (res.statusCode >= 200 && res.statusCode < 300 && body.code === 200) {
          resolve(body.data)
          return
        }
        if (res.statusCode === 401 || body.code === 401) {
          clearSession()
          if (!silent) {
            wx.showToast({ title: '登录已过期，请重试', icon: 'none' })
          }
          reject(new Error('登录已过期'))
          return
        }
        const message = body.message || '请求失败（' + res.statusCode + '）'
        if (!silent) {
          wx.showToast({ title: message, icon: 'none' })
        }
        reject(new Error(message))
      },
      fail() {
        if (loading) {
          wx.hideLoading()
        }
        if (!silent) {
          wx.showToast({ title: '网络异常，请检查后端是否启动', icon: 'none' })
        }
        reject(new Error('网络异常'))
      }
    })
  }))
}

function get(url, data, options) {
  return request(Object.assign({ url, method: 'GET', data }, options))
}

function post(url, data, options) {
  return request(Object.assign({ url, method: 'POST', data }, options))
}

function put(url, data, options) {
  return request(Object.assign({ url, method: 'PUT', data }, options))
}

function del(url, data, options) {
  return request(Object.assign({ url, method: 'DELETE', data }, options))
}

module.exports = {
  request,
  get,
  post,
  put,
  del,
  TOKEN_KEY,
  USER_KEY,
  clearSession
}
