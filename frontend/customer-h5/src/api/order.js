import request from '@/utils/request'

/** 价格预估 */
export function estimateOrder(data) {
  return request.post('/customer/orders/estimate', data)
}

/** 创建订单（带幂等键） */
export function createOrder(data, idempotencyKey) {
  return request.post('/customer/orders', data, {
    headers: idempotencyKey ? { 'X-Idempotency-Key': idempotencyKey } : {}
  })
}

/** 订单分页列表 */
export function pageOrders(params) {
  return request.get('/customer/orders', { params })
}

/** 订单详情（含状态流转日志） */
export function getOrderDetail(orderNo) {
  return request.get(`/customer/orders/${orderNo}`)
}

/** 取消订单 */
export function cancelOrder(orderNo, reason) {
  return request.put(`/customer/orders/${orderNo}/cancel`, { reason })
}
