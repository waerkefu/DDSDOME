import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { useUserStore } from '@/stores/user'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 请求拦截：注入 token
request.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 401 统一处理：清理登录态并跳转登录页
function handleUnauthorized(message) {
  const userStore = useUserStore()
  userStore.logout()
  ElMessage.error(message || '登录已过期，请重新登录')
  const current = router.currentRoute.value
  if (current.path !== '/login') {
    router.push({ path: '/login', query: { redirect: current.fullPath } })
  }
}

// 响应拦截：统一处理 { code, message, data }
request.interceptors.response.use(
  (response) => {
    const body = response.data
    // 非统一结构（如文件流）直接返回
    if (!body || typeof body.code === 'undefined') {
      return body
    }
    if (body.code === 200) {
      return body.data
    }
    if (body.code === 401) {
      handleUnauthorized(body.message)
      return Promise.reject(new Error(body.message || '未登录或登录已过期'))
    }
    const message = body.message || '请求失败'
    ElMessage.error(message)
    return Promise.reject(new Error(message))
  },
  (error) => {
    const status = error.response && error.response.status
    if (status === 401) {
      handleUnauthorized()
      return Promise.reject(error)
    }
    const message =
      (error.response && error.response.data && error.response.data.message) ||
      error.message ||
      '网络异常，请稍后重试'
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default request
