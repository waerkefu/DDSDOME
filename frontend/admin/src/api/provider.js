import request from '@/utils/request'

/** 服务商分页查询 */
export function pageProviders(params) {
  return request.get('/admin/providers', { params })
}

/** 新增服务商档案 */
export function createProvider(data) {
  return request.post('/admin/providers', data)
}

/** 修改服务商档案 */
export function updateProvider(id, data) {
  return request.put(`/admin/providers/${id}`, data)
}

/** 修改服务商状态：1=启用 0=禁用 */
export function updateProviderStatus(id, status) {
  return request.put(`/admin/providers/${id}/status`, null, { params: { status } })
}
