import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'

const http = axios.create({
  baseURL: '/api',
  timeout: 15000
})

http.interceptors.request.use(config => {
  const auth = useAuthStore()
  if (auth.token) {
    config.headers.Authorization = 'Bearer ' + auth.token
  }
  return config
})

http.interceptors.response.use(
  response => {
    const body = response.data || {}
    if (body.code === 200) {
      return body.data
    }
    ElMessage.error(body.message || '请求失败')
    return Promise.reject(new Error(body.message || '请求失败'))
  },
  error => {
    const status = error.response && error.response.status
    const body = (error.response && error.response.data) || {}
    if (status === 401) {
      const auth = useAuthStore()
      auth.clear()
      ElMessage.error('登录已过期，请重新登录')
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    } else {
      ElMessage.error(body.message || error.message || '网络异常')
    }
    return Promise.reject(error)
  }
)

export default http
