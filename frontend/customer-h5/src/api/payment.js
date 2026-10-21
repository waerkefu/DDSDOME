import request from '@/utils/request'

/** 发起支付（创建支付单） */
export function createPayment(orderNo, idempotencyKey) {
  return request.post(`/payments/orders/${orderNo}/pay`, null, {
    headers: idempotencyKey ? { 'X-Idempotency-Key': idempotencyKey } : {}
  })
}

/** 查询订单对应支付单 */
export function getPayment(orderNo) {
  return request.get(`/payments/orders/${orderNo}`)
}

/** 模拟第三方支付网关回调（无需登录） */
export function mockPayCallback(data) {
  return request.post('/public/payment/mock-callback', data)
}
