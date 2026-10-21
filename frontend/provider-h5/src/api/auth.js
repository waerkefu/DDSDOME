import request from '@/utils/request'

/** 登录 */
export function login(data) {
  return request.post('/auth/login', data)
}

/** 当前登录用户信息 */
export function getMe() {
  return request.get('/auth/me')
}
