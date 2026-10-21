<template>
  <div class="page-container">
    <el-card shadow="never">
      <el-form :inline="true" :model="query" class="search-form">
        <el-form-item label="关键词">
          <el-input
            v-model="query.keyword"
            placeholder="账号 / 手机号 / 昵称"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="用户类型">
          <el-select v-model="query.userType" placeholder="全部" clearable style="width: 140px">
            <el-option v-for="item in userTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column prop="username" label="账号" min-width="120" />
        <el-table-column prop="phone" label="手机号" min-width="130" />
        <el-table-column prop="nickname" label="昵称" min-width="120" />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="userTypeTag(row.userType)">
              {{ row.userTypeDesc || userTypeLabel(row.userType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="(value) => handleStatusChange(row, value)"
            />
          </template>
        </el-table-column>
        <el-table-column label="角色" min-width="150">
          <template #default="{ row }">
            <template v-if="row.roles && row.roles.length">
              <el-tag v-for="role in row.roles" :key="role" size="small" type="info" class="tag-gap">
                {{ role }}
              </el-tag>
            </template>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="最近登录" min-width="170">
          <template #default="{ row }">{{ formatDateTime(row.lastLoginTime) }}</template>
        </el-table-column>
        <el-table-column label="创建时间" min-width="170">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="primary" @click="openRoleDialog(row)">分配角色</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        class="pagination"
        background
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        v-model:current-page="query.pageNum"
        v-model:page-size="query.pageSize"
        @current-change="fetchList"
        @size-change="handleSizeChange"
      />
    </el-card>

    <el-dialog v-model="editVisible" title="编辑用户" width="480px">
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="90px">
        <el-form-item label="账号">
          <el-input :model-value="editForm.username" disabled />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="editForm.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="editForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="editForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitEdit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="roleVisible" title="分配角色" width="480px">
      <el-form label-width="90px">
        <el-form-item label="账号">
          <el-input :model-value="roleForm.username" disabled />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="roleForm.roleIds" multiple placeholder="请选择角色" style="width: 100%">
            <el-option
              v-for="role in roleOptions"
              :key="role.id"
              :label="`${role.roleName}（${role.roleCode}）`"
              :value="role.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitRoles">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Search } from '@element-plus/icons-vue'
import { assignUserRoles, pageUsers, updateUser, updateUserStatus } from '@/api/user'
import { listRoles } from '@/api/role'
import { formatDateTime } from '@/utils/format'

const userTypeOptions = [
  { label: '客户', value: 1 },
  { label: '服务商', value: 2 },
  { label: '管理员', value: 3 }
]

const loading = ref(false)
const submitting = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  userType: null,
  status: null
})

const editVisible = ref(false)
const editFormRef = ref(null)
const editForm = reactive({
  id: null,
  username: '',
  nickname: '',
  phone: '',
  remark: ''
})

const editRules = {
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  phone: [{ pattern: /^1\d{10}$/, message: '请输入正确的手机号', trigger: 'blur' }]
}

const roleVisible = ref(false)
const roleOptions = ref([])
const roleForm = reactive({
  id: null,
  username: '',
  roleIds: []
})

function userTypeLabel(userType) {
  const item = userTypeOptions.find((option) => option.value === userType)
  return item ? item.label : '-'
}

function userTypeTag(userType) {
  if (userType === 3) {
    return 'danger'
  }
  if (userType === 2) {
    return 'success'
  }
  return 'primary'
}

async function fetchList() {
  loading.value = true
  try {
    const data = await pageUsers(query)
    list.value = (data && data.records) || []
    total.value = Number((data && data.total) || 0)
  } catch (error) {
    // 错误提示已由请求拦截器统一处理
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.pageNum = 1
  fetchList()
}

function handleReset() {
  query.keyword = ''
  query.userType = null
  query.status = null
  handleSearch()
}

function handleSizeChange() {
  query.pageNum = 1
  fetchList()
}

async function handleStatusChange(row, value) {
  try {
    await updateUserStatus(row.id, value)
    ElMessage.success(value === 1 ? '已启用' : '已禁用')
  } catch (error) {
    // 错误提示已由请求拦截器统一处理
  } finally {
    fetchList()
  }
}

function openEdit(row) {
  editForm.id = row.id
  editForm.username = row.username
  editForm.nickname = row.nickname || ''
  editForm.phone = row.phone || ''
  editForm.remark = row.remark || ''
  editVisible.value = true
}

async function submitEdit() {
  const valid = await editFormRef.value.validate().catch(() => false)
  if (!valid) {
    return
  }
  submitting.value = true
  try {
    await updateUser(editForm.id, {
      nickname: editForm.nickname,
      phone: editForm.phone,
      remark: editForm.remark
    })
    ElMessage.success('保存成功')
    editVisible.value = false
    fetchList()
  } catch (error) {
    // 错误提示已由请求拦截器统一处理
  } finally {
    submitting.value = false
  }
}

async function openRoleDialog(row) {
  roleForm.id = row.id
  roleForm.username = row.username
  roleForm.roleIds = []
  roleVisible.value = true
  try {
    if (roleOptions.value.length === 0) {
      roleOptions.value = (await listRoles()) || []
    }
    const codes = row.roles || []
    roleForm.roleIds = roleOptions.value
      .filter((role) => codes.includes(role.roleCode))
      .map((role) => role.id)
  } catch (error) {
    // 错误提示已由请求拦截器统一处理
  }
}

async function submitRoles() {
  submitting.value = true
  try {
    await assignUserRoles(roleForm.id, roleForm.roleIds)
    ElMessage.success('角色分配成功')
    roleVisible.value = false
    fetchList()
  } catch (error) {
    // 错误提示已由请求拦截器统一处理
  } finally {
    submitting.value = false
  }
}

onMounted(fetchList)
</script>
