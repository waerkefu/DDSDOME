import request from '@/utils/request'

/** 用户分页查询 */
export function pageUsers(params) {
  return request.get('/admin/users', { params })
}

/** 修改用户基本信息 */
export function updateUser(id, data) {
  return request.put(`/admin/users/${id}`, data)
}

/** 修改用户状态：1=启用 0=禁用 */
export function updateUserStatus(id, status) {
  return request.put(`/admin/users/${id}/status`, null, { params: { status } })
}

/** 分配用户角色 */
export function assignUserRoles(id, roleIds) {
  return request.put(`/admin/users/${id}/roles`, { roleIds })
}
