import request from '@/utils/request'

/** 计价规则分页查询 */
export function pagePricingRules(params) {
  return request.get('/admin/pricing-rules', { params })
}

/** 新增计价规则 */
export function createPricingRule(data) {
  return request.post('/admin/pricing-rules', data)
}

/** 修改计价规则 */
export function updatePricingRule(id, data) {
  return request.put(`/admin/pricing-rules/${id}`, data)
}

/** 删除计价规则 */
export function deletePricingRule(id) {
  return request.delete(`/admin/pricing-rules/${id}`)
}
