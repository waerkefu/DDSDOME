import { defineStore } from 'pinia'
import { login as loginApi, getMe } from '@/api/auth'

const TOKEN_KEY = 'daijia_admin_token'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY) || '',
    userInfo: null,
    permissions: [],
    roles: []
  }),

  getters: {
    isLogin: (state) => !!state.token,
    displayName: (state) =>
      (state.userInfo && (state.userInfo.nickname || state.userInfo.username)) || '未登录',
    avatar: (state) => (state.userInfo && state.userInfo.avatar) || '',
    /** 判断是否拥有某个权限编码 */
    hasPermission: (state) => (permCode) => state.permissions.includes(permCode)
  },

  actions: {
    /** 登录：写入 token 与用户信息 */
    async login(payload) {
      const data = await loginApi(payload)
      this.token = (data && data.token) || ''
      if (this.token) {
        localStorage.setItem(TOKEN_KEY, this.token)
      }
      if (data && data.user) {
        this.userInfo = data.user
        this.roles = data.user.roles || []
      }
      return data
    },

    /** 拉取当前登录用户信息（含权限编码） */
    async fetchMe() {
      const data = await getMe()
      this.userInfo = data
      this.roles = (data && data.roles) || []
      this.permissions = (data && data.permissions) || []
      return data
    },

    /** 退出登录：清理本地登录态 */
    logout() {
      this.token = ''
      this.userInfo = null
      this.permissions = []
      this.roles = []
      localStorage.removeItem(TOKEN_KEY)
    }
  }
})
