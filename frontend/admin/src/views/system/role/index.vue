<template>
  <div class="page-container">
    <el-card shadow="never">
      <div class="toolbar">
        <el-button type="primary" :icon="Plus" @click="openCreate">新增角色</el-button>
        <el-button :icon="Refresh" @click="fetchRoles">刷新</el-button>
      </div>

      <el-table v-loading="loading" :data="roles" border stripe>
        <el-table-column prop="roleCode" label="角色编码" min-width="140" />
        <el-table-column prop="roleName" label="角色名称" min-width="140" />
        <el-table-column prop="description" label="描述" min-width="220" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="primary" @click="openPermissionDrawer(row)">分配权限</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="480px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="form.roleCode" :disabled="isEdit" placeholder="如 ADMIN" />
        </el-form-item>
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入角色描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="drawerVisible" :title="`分配权限 - ${currentRole.roleName || ''}`" size="420px">
      <div v-loading="drawerLoading" class="permission-tree">
        <el-tree
          ref="treeRef"
          :data="treeData"
          :props="{ label: 'label', children: 'children' }"
          node-key="id"
          show-checkbox
          default-expand-all
        >
          <template #default="{ data }">
            <span>
              {{ data.label }}
              <span class="text-muted">（{{ data.permCode }}）</span>
            </span>
          </template>
        </el-tree>
      </div>
      <template #footer>
        <el-button @click="drawerVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitPermissions">保存</el-button>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import {
  assignRolePermissions,
  createRole,
  getRolePermissions,
  listPermissions,
  listRoles,
  updateRole
} from '@/api/role'

const loading = ref(false)
const submitting = ref(false)
const roles = ref([])

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const form = reactive({
  id: null,
  roleCode: '',
  roleName: '',
  description: ''
})

const rules = {
  roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }],
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }]
}

const dialogTitle = computed(() => (isEdit.value ? '编辑角色' : '新增角色'))

const drawerVisible = ref(false)
const drawerLoading = ref(false)
const treeRef = ref(null)
const currentRole = reactive({ id: null, roleName: '' })
const permissionList = ref([])
const treeData = ref([])

// 叶子节点 ID：回显时仅勾选叶子，避免父节点被动全选
const leafIdSet = computed(() => {
  const parentIds = new Set(permissionList.value.map((item) => item.parentId))
  return new Set(permissionList.value.filter((item) => !parentIds.has(item.id)).map((item) => item.id))
})

function buildTree(items) {
  const nodes = items.map((item) => ({
    id: item.id,
    label: item.permName,
    permCode: item.permCode,
    parentId: item.parentId,
    children: []
  }))
  const nodeMap = new Map(nodes.map((node) => [node.id, node]))
  const roots = []
  nodes.forEach((node) => {
    const parent = nodeMap.get(node.parentId)
    if (parent) {
      parent.children.push(node)
    } else {
      roots.push(node)
    }
  })
  return roots
}

async function fetchRoles() {
  loading.value = true
  try {
    roles.value = (await listRoles()) || []
  } catch (error) {
    // 错误提示已由请求拦截器统一处理
  } finally {
    loading.value = false
  }
}

function openCreate() {
  isEdit.value = false
  form.id = null
  form.roleCode = ''
  form.roleName = ''
  form.description = ''
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  form.id = row.id
  form.roleCode = row.roleCode || ''
  form.roleName = row.roleName || ''
  form.description = row.description || ''
  dialogVisible.value = true
}

async function submitForm() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) {
    return
  }
  submitting.value = true
  try {
    if (isEdit.value) {
      await updateRole(form.id, { roleName: form.roleName, description: form.description })
      ElMessage.success('保存成功')
    } else {
      await createRole({
        roleCode: form.roleCode,
        roleName: form.roleName,
        description: form.description
      })
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchRoles()
  } catch (error) {
    // 错误提示已由请求拦截器统一处理
  } finally {
    submitting.value = false
  }
}

async function openPermissionDrawer(row) {
  currentRole.id = row.id
  currentRole.roleName = row.roleName
  drawerVisible.value = true
  drawerLoading.value = true
  try {
    if (permissionList.value.length === 0) {
      permissionList.value = (await listPermissions()) || []
      treeData.value = buildTree(permissionList.value)
    }
    const assigned = (await getRolePermissions(row.id)) || []
    const leafIds = assigned.filter((id) => leafIdSet.value.has(id))
    await nextTick()
    if (treeRef.value) {
      treeRef.value.setCheckedKeys(leafIds)
    }
  } catch (error) {
    permissionList.value = []
    treeData.value = []
  } finally {
    drawerLoading.value = false
  }
}

async function submitPermissions() {
  if (!treeRef.value) {
    return
  }
  const checked = treeRef.value.getCheckedKeys()
  const halfChecked = treeRef.value.getHalfCheckedKeys()
  submitting.value = true
  try {
    await assignRolePermissions(currentRole.id, [...checked, ...halfChecked])
    ElMessage.success('权限分配成功')
    drawerVisible.value = false
  } catch (error) {
    // 错误提示已由请求拦截器统一处理
  } finally {
    submitting.value = false
  }
}

onMounted(fetchRoles)
</script>

<style scoped>
.toolbar {
  margin-bottom: 16px;
}

.permission-tree {
  min-height: 200px;
}
</style>
