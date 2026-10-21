import request from '@/utils/request'

/** 全部角色 */
export function listRoles() {
  return request.get('/admin/roles')
}

/** 新增角色 */
export function createRole(data) {
  return request.post('/admin/roles', data)
}

/** 修改角色 */
export function updateRole(id, data) {
  return request.put(`/admin/roles/${id}`, data)
}

/** 角色已分配的权限 ID 列表 */
export function getRolePermissions(id) {
  return request.get(`/admin/roles/${id}/permissions`)
}

/** 分配角色权限 */
export function assignRolePermissions(id, permissionIds) {
  return request.put(`/admin/roles/${id}/permissions`, { permissionIds })
}

/** 全部权限 */
export function listPermissions() {
  return request.get('/admin/permissions')
}
