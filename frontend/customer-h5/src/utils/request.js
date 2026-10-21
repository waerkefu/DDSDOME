import axios from 'axios'
import { ElMessage } from 'element-plus'

/** localStorage 持久化键名，store 与拦截器共用 */
export const TOKEN_KEY = 'daijia_customer_token'
export const USER_KEY = 'daijia_customer_user'

export function getToken() {
  return localStorage.getItem(TOKEN_KEY) || ''
}

export function clearAuthStorage() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 请求拦截：注入 JWT
request.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截：统一处理 { code, message, data }
request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res && typeof res === 'object' && 'code' in res) {
      if (res.code === 200) {
        return res.data
      }
      if (res.code === 401) {
        handleUnauthorized()
      }
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    // 非标准响应（如文件流）原样返回
    return res
  },
  (error) => {
    const status = error.response && error.response.status
    if (status === 401) {
      handleUnauthorized()
      ElMessage.error('登录已失效，请重新登录')
    } else if (status === 403) {
      ElMessage.error('暂无操作权限')
    } else {
      const msg =
        (error.response && error.response.data && error.response.data.message) ||
        error.message ||
        '网络异常，请稍后重试'
      ElMessage.error(msg)
    }
    return Promise.reject(error)
  }
)

/** 401：清除登录态并跳转登录页 */
function handleUnauthorized() {
  clearAuthStorage()
  if (window.location.pathname !== '/login') {
    window.location.replace('/login')
  }
}

export default request
