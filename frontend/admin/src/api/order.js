import request from '@/utils/request'

/** 订单分页查询 */
export function pageOrders(params) {
  return request.get('/admin/orders', { params })
}

/** 订单详情（含状态流转记录） */
export function getOrderDetail(orderNo) {
  return request.get(`/admin/orders/${orderNo}`)
}

/** 取消订单 */
export function cancelOrder(orderNo, reason) {
  return request.put(`/admin/orders/${orderNo}/cancel`, { reason })
}

/** 退款（amount 单位为分） */
export function refundOrder(orderNo, data) {
  return request.post(`/admin/orders/${orderNo}/refund`, data)
}
