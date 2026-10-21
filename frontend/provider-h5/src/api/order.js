import request from '@/utils/request'

/** 接单大厅分页列表（仅待接单且在服务范围内的订单） */
export function getHallOrders(params) {
  return request.get('/provider/orders/hall', { params })
}

/** 抢单/接单 */
export function grabOrder(orderNo) {
  return request.post(`/provider/orders/${orderNo}/grab`)
}

/** 我的订单分页列表 */
export function getMyOrders(params) {
  return request.get('/provider/orders', { params })
}

/** 订单详情（含状态流转日志） */
export function getOrderDetail(orderNo) {
  return request.get(`/provider/orders/${orderNo}`)
}

/** 开始服务 */
export function startService(orderNo) {
  return request.put(`/provider/orders/${orderNo}/start`)
}

/** 结束服务，data: { distanceM, durationS } */
export function finishService(orderNo, data) {
  return request.put(`/provider/orders/${orderNo}/finish`, data)
}
