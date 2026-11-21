/**
 * 后端地址配置。
 * - 微信开发者工具（模拟器）：用 localhost
 * - 真机预览/调试：改成电脑的局域网 IP，例如 http://192.168.1.5:8081
 *   查看方式：Windows 命令行执行 ipconfig，找“IPv4 地址”
 *   手机和电脑必须在同一个 WiFi 下
 */
const BASE_URL = 'http://localhost:8081'

module.exports = {
  BASE_URL
}
