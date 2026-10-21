import { defineStore } from 'pinia'
import { login as loginApi, register as registerApi, getMe } from '@/api/auth'
import { TOKEN_KEY, USER_KEY, clearAuthStorage } from '@/utils/request'

function readUserInfo() {
  try {
    return JSON.parse(localStorage.getItem(USER_KEY) || 'null')
  } catch (e) {
    return null
  }
}

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY) || '',
    userInfo: readUserInfo()
  }),

  getters: {
    isLogin: (state) => !!state.token,
    displayName: (state) =>
      (state.userInfo && (state.userInfo.nickname || state.userInfo.username)) || '未登录',
    roles: (state) => (state.userInfo && state.userInfo.roles) || []
  },

  actions: {
    /** 登录/注册成功后持久化登录态 */
    setAuth(data) {
      this.token = data.token || ''
      this.userInfo = data.user || null
      localStorage.setItem(TOKEN_KEY, this.token)
      localStorage.setItem(USER_KEY, JSON.stringify(this.userInfo))
    },

    async login(payload) {
      const data = await loginApi(payload)
      this.setAuth(data)
      return data
    },

    async register(payload) {
      const data = await registerApi(payload)
      this.setAuth(data)
      return data
    },

    async fetchMe() {
      const data = await getMe()
      this.userInfo = data
      localStorage.setItem(USER_KEY, JSON.stringify(data))
      return data
    },

    logout() {
      this.token = ''
      this.userInfo = null
      clearAuthStorage()
    }
  }
})
