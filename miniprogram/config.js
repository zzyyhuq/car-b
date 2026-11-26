/**
 * 后端地址配置。
 * - 微信开发者工具（模拟器）：用 localhost
 * - 真机预览/调试：改成电脑的局域网 IP，手机和电脑要在同一个 WiFi 下
 *   查看方式：Windows 命令行执行 ipconfig，找 WLAN 的“IPv4 地址”
 */
const BASE_URL = 'http://localhost:8081'

/**
 * 登录方式：
 * - true：wx.login 拿 code → POST /api/auth/wx-login
 *   需要后端配置 wx.appid / wx.secret，否则会走下面的回退
 * - false：直接用演示车主 owner001 登录，方便没有 AppSecret 时开发
 */
const USE_WX_LOGIN = true

/**
 * 微信登录失败时是否回退到演示账号。
 * 开发阶段建议 true；正式演示/上线建议 false，避免配置错误被掩盖。
 */
const WX_LOGIN_FALLBACK = true

module.exports = {
  BASE_URL,
  USE_WX_LOGIN,
  WX_LOGIN_FALLBACK
}
