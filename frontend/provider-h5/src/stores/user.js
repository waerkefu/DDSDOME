import { defineStore } from 'pinia'
import { login as loginApi, getMe } from '@/api/auth'
import { TOKEN_KEY, USER_KEY } from '@/utils/request'

function readCachedUser() {
  try {
    const raw = localStorage.getItem(USER_KEY)
    return raw ? JSON.parse(raw) : null
  } catch (e) {
    return null
  }
}

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY) || '',
    userInfo: readCachedUser()
  }),

  getters: {
    isLogin: (state) => !!state.token,
    displayName: (state) =>
      state.userInfo?.nickname || state.userInfo?.username || '服务商',
    roles: (state) => state.userInfo?.roles || [],
    isProvider: (state) => (state.userInfo?.roles || []).includes('PROVIDER')
  },

  actions: {
    /** 账号密码登录 */
    async login(payload) {
      const data = await loginApi(payload)
      this.token = data?.token || ''
      this.userInfo = data?.user || null
      localStorage.setItem(TOKEN_KEY, this.token)
      localStorage.setItem(USER_KEY, JSON.stringify(this.userInfo))
      return data
    },

    /** 拉取当前用户信息 */
    async fetchMe() {
      const data = await getMe()
      this.userInfo = data || null
      localStorage.setItem(USER_KEY, JSON.stringify(this.userInfo))
      return data
    },

    /** 退出登录，清空本地登录态 */
    logout() {
      this.token = ''
      this.userInfo = null
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(USER_KEY)
    }
  }
})
