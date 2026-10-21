import request from '@/utils/request'

/** 当前服务商档案 */
export function getProfile() {
  return request.get('/provider/profile')
}

/** 修改在线状态 0-离线 1-在线 */
export function updateOnlineStatus(onlineStatus) {
  return request.put('/provider/profile/online', { onlineStatus })
}

/** 修改接单开关 0-关闭 1-开启 */
export function updateAcceptEnabled(acceptEnabled) {
  return request.put('/provider/profile/accept', { acceptEnabled })
}
