<template>
  <div class="page-container">
    <el-card shadow="never">
      <el-form :inline="true" :model="query" class="search-form">
        <el-form-item label="关键词">
          <el-input
            v-model="query.keyword"
            placeholder="姓名 / 手机号 / 编号"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="城市编码">
          <el-input v-model="query.cityCode" placeholder="如 310100" clearable style="width: 140px" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="审核状态">
          <el-select v-model="query.auditStatus" placeholder="全部" clearable style="width: 130px">
            <el-option v-for="item in auditOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="启用状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          <el-button type="primary" plain :icon="Plus" @click="openCreate">新增服务商</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column prop="providerNo" label="编号" min-width="120" />
        <el-table-column label="姓名" min-width="120">
          <template #default="{ row }">
            <div>{{ row.realName }}</div>
            <div class="text-muted">{{ row.username || '-' }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" min-width="130" />
        <el-table-column label="服务类型" min-width="200">
          <template #default="{ row }">
            <template v-if="serviceTypeCodes(row).length">
              <el-tag v-for="code in serviceTypeCodes(row)" :key="code" size="small" class="tag-gap">
                {{ serviceTypeLabel(code) }}
              </el-tag>
            </template>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="cityCode" label="城市" min-width="100" />
        <el-table-column prop="vehiclePlate" label="车牌" min-width="110" />
        <el-table-column label="接单状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="row.acceptEnabled === 1 ? 'success' : 'info'">
              {{ row.acceptEnabled === 1 ? '接单中' : '停止接单' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="在线状态" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="row.onlineStatus === 1 ? 'success' : 'info'">
              {{ row.onlineStatus === 1 ? '在线' : '离线' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="审核状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="auditTag(row.auditStatus)">{{ auditLabel(row.auditStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="启用状态" width="90">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="(value) => handleStatusChange(row, value)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="620px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="关联用户ID" prop="userId">
          <el-input-number v-model="form.userId" :min="1" :controls="false" :disabled="isEdit" style="width: 100%" />
          <div class="form-tip">服务商账号对应的用户 ID（新增必填，编辑不可修改）</div>
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="身份证号" prop="idCardNo">
          <el-input v-model="form.idCardNo" placeholder="请输入身份证号" />
        </el-form-item>
        <el-form-item label="服务类型" prop="serviceTypeList">
          <el-select v-model="form.serviceTypeList" multiple placeholder="请选择服务类型" style="width: 100%">
            <el-option v-for="item in serviceTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="城市编码" prop="cityCode">
          <el-input v-model="form.cityCode" placeholder="如 310100" />
        </el-form-item>
        <el-form-item label="车牌号" prop="vehiclePlate">
          <el-input v-model="form.vehiclePlate" placeholder="如 沪A88888" />
        </el-form-item>
        <el-form-item label="车辆型号" prop="vehicleModel">
          <el-input v-model="form.vehicleModel" placeholder="如 大众朗逸" />
        </el-form-item>
        <el-form-item label="驾龄（年）" prop="drivingYears">
          <el-input-number v-model="form.drivingYears" :min="0" :max="60" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Refresh, Search } from '@element-plus/icons-vue'
import { createProvider, pageProviders, updateProvider, updateProviderStatus } from '@/api/provider'

const serviceTypeOptions = [
  { label: '即时代驾', value: 'IMMEDIATE' },
  { label: '预约代驾', value: 'SCHEDULED' },
  { label: '年审代办', value: 'INSPECTION' },
  { label: '换证代办', value: 'RENEWAL' }
]

const auditOptions = [
  { label: '待审核', value: 0 },
  { label: '已通过', value: 1 },
  { label: '已驳回', value: 2 }
]

const loading = ref(false)
const submitting = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  status: null,
  auditStatus: null,
  cityCode: ''
})

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const form = reactive({
  id: null,
  userId: null,
  realName: '',
  phone: '',
  idCardNo: '',
  serviceTypeList: [],
  cityCode: '',
  vehiclePlate: '',
  vehicleModel: '',
  drivingYears: 0,
  remark: ''
})

const rules = {
  userId: [{ required: true, message: '请输入关联用户 ID', trigger: 'change' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  serviceTypeList: [{ required: true, type: 'array', message: '请选择服务类型', trigger: 'change' }]
}

const dialogTitle = computed(() => (isEdit.value ? '编辑服务商' : '新增服务商'))

function serviceTypeCodes(row) {
  if (row.serviceTypeList && row.serviceTypeList.length) {
    return row.serviceTypeList
  }
  if (!row.serviceTypes) {
    return []
  }
  return String(row.serviceTypes)
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean)
}

function serviceTypeLabel(code) {
  const item = serviceTypeOptions.find((option) => option.value === code)
  return item ? item.label : code
}

function auditLabel(auditStatus) {
  const item = auditOptions.find((option) => option.value === auditStatus)
  return item ? item.label : '-'
}

function auditTag(auditStatus) {
  if (auditStatus === 1) {
    return 'success'
  }
  if (auditStatus === 2) {
    return 'danger'
  }
  return 'warning'
}

async function fetchList() {
  loading.value = true
  try {
    const data = await pageProviders(query)
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
  query.status = null
  query.auditStatus = null
  query.cityCode = ''
  handleSearch()
}

function handleSizeChange() {
  query.pageNum = 1
  fetchList()
}

async function handleStatusChange(row, value) {
  try {
    await updateProviderStatus(row.id, value)
    ElMessage.success(value === 1 ? '已启用' : '已禁用')
  } catch (error) {
    // 错误提示已由请求拦截器统一处理
  } finally {
    fetchList()
  }
}

function resetForm() {
  form.id = null
  form.userId = null
  form.realName = ''
  form.phone = ''
  form.idCardNo = ''
  form.serviceTypeList = []
  form.cityCode = ''
  form.vehiclePlate = ''
  form.vehicleModel = ''
  form.drivingYears = 0
  form.remark = ''
}

function openCreate() {
  resetForm()
  isEdit.value = false
  dialogVisible.value = true
}

function openEdit(row) {
  resetForm()
  isEdit.value = true
  form.id = row.id
  form.userId = row.userId
  form.realName = row.realName || ''
  form.phone = row.phone || ''
  form.idCardNo = row.idCardNo || ''
  form.serviceTypeList = serviceTypeCodes(row)
  form.cityCode = row.cityCode || ''
  form.vehiclePlate = row.vehiclePlate || ''
  form.vehicleModel = row.vehicleModel || ''
  form.drivingYears = row.drivingYears || 0
  form.remark = row.remark || ''
  dialogVisible.value = true
}

async function submitForm() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) {
    return
  }
  const payload = {
    userId: form.userId,
    realName: form.realName,
    phone: form.phone,
    idCardNo: form.idCardNo,
    serviceTypes: form.serviceTypeList.join(','),
    cityCode: form.cityCode,
    vehiclePlate: form.vehiclePlate,
    vehicleModel: form.vehicleModel,
    drivingYears: form.drivingYears,
    remark: form.remark
  }
  submitting.value = true
  try {
    if (isEdit.value) {
      await updateProvider(form.id, payload)
      ElMessage.success('保存成功')
    } else {
      await createProvider(payload)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchList()
  } catch (error) {
    // 错误提示已由请求拦截器统一处理
  } finally {
    submitting.value = false
  }
}

onMounted(fetchList)
</script>
