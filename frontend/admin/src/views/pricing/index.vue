<template>
  <div class="page-container">
    <el-card shadow="never">
      <el-form :inline="true" :model="query" class="search-form">
        <el-form-item label="城市编码">
          <el-input v-model="query.cityCode" placeholder="如 310100" clearable style="width: 160px" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="车型">
          <el-select v-model="query.vehicleType" placeholder="全部" clearable style="width: 150px">
            <el-option v-for="item in vehicleTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          <el-button type="primary" plain :icon="Plus" @click="openCreate">新增规则</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column prop="ruleName" label="规则名称" min-width="180" show-overflow-tooltip />
        <el-table-column label="城市" min-width="130">
          <template #default="{ row }">{{ row.cityName || '-' }}（{{ row.cityCode }}）</template>
        </el-table-column>
        <el-table-column label="车型" width="100">
          <template #default="{ row }">{{ vehicleTypeLabel(row.vehicleType) }}</template>
        </el-table-column>
        <el-table-column label="起步价(元)" width="110">
          <template #default="{ row }">{{ formatYuan(row.basePrice) }}</template>
        </el-table-column>
        <el-table-column label="起步里程(米)" width="130">
          <template #default="{ row }">{{ row.baseDistanceM ?? '-' }}</template>
        </el-table-column>
        <el-table-column label="起步时长(秒)" width="130">
          <template #default="{ row }">{{ row.baseDurationS ?? '-' }}</template>
        </el-table-column>
        <el-table-column label="每公里(元)" width="120">
          <template #default="{ row }">{{ formatYuan(row.pricePerKm) }}</template>
        </el-table-column>
        <el-table-column label="每分钟(元)" width="120">
          <template #default="{ row }">{{ formatYuan(row.pricePerMin) }}</template>
        </el-table-column>
        <el-table-column label="最低消费(元)" width="120">
          <template #default="{ row }">{{ formatYuan(row.minPrice) }}</template>
        </el-table-column>
        <el-table-column label="夜间时段" min-width="150">
          <template #default="{ row }">{{ formatTime(row.nightStart) }} ~ {{ formatTime(row.nightEnd) }}</template>
        </el-table-column>
        <el-table-column label="夜间系数" width="100">
          <template #default="{ row }">{{ row.nightRatio ?? '-' }}</template>
        </el-table-column>
        <el-table-column label="预约系数" width="100">
          <template #default="{ row }">{{ row.bookingRatio ?? '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="680px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="规则名称" prop="ruleName">
          <el-input v-model="form.ruleName" placeholder="如 上海-舒适型-标准计价" />
        </el-form-item>
        <el-form-item label="城市编码" prop="cityCode">
          <el-input v-model="form.cityCode" placeholder="如 310100" />
        </el-form-item>
        <el-form-item label="城市名称" prop="cityName">
          <el-input v-model="form.cityName" placeholder="如 上海市" />
        </el-form-item>
        <el-form-item label="车型" prop="vehicleType">
          <el-select
            v-model="form.vehicleType"
            placeholder="请选择车型"
            filterable
            allow-create
            default-first-option
            style="width: 100%"
          >
            <el-option v-for="item in vehicleTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="起步价(元)" prop="basePrice">
          <el-input-number v-model="form.basePrice" :min="0" :precision="2" :step="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="起步里程(米)" prop="baseDistanceM">
          <el-input-number v-model="form.baseDistanceM" :min="0" :step="100" style="width: 100%" />
        </el-form-item>
        <el-form-item label="起步时长(秒)" prop="baseDurationS">
          <el-input-number v-model="form.baseDurationS" :min="0" :step="60" style="width: 100%" />
        </el-form-item>
        <el-form-item label="每公里(元)" prop="pricePerKm">
          <el-input-number v-model="form.pricePerKm" :min="0" :precision="2" :step="0.5" style="width: 100%" />
        </el-form-item>
        <el-form-item label="每分钟(元)" prop="pricePerMin">
          <el-input-number v-model="form.pricePerMin" :min="0" :precision="2" :step="0.5" style="width: 100%" />
        </el-form-item>
        <el-form-item label="最低消费(元)" prop="minPrice">
          <el-input-number v-model="form.minPrice" :min="0" :precision="2" :step="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="夜间开始" prop="nightStart">
          <el-time-picker v-model="form.nightStart" value-format="HH:mm:ss" placeholder="如 22:00:00" style="width: 100%" />
        </el-form-item>
        <el-form-item label="夜间结束" prop="nightEnd">
          <el-time-picker v-model="form.nightEnd" value-format="HH:mm:ss" placeholder="如 06:00:00" style="width: 100%" />
        </el-form-item>
        <el-form-item label="夜间系数" prop="nightRatio">
          <el-input-number v-model="form.nightRatio" :min="0" :precision="2" :step="0.1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="预约系数" prop="bookingRatio">
          <el-input-number v-model="form.bookingRatio" :min="0" :precision="2" :step="0.1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="禁用" />
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Search } from '@element-plus/icons-vue'
import { createPricingRule, deletePricingRule, pagePricingRules, updatePricingRule } from '@/api/pricing'
import { fenToYuan, formatYuan, yuanToFen } from '@/utils/format'

const vehicleTypeOptions = [
  { label: '经济型', value: 'ECONOMY' },
  { label: '舒适型', value: 'COMFORT' },
  { label: '商务型', value: 'BUSINESS' },
  { label: '豪华型', value: 'LUXURY' }
]

const loading = ref(false)
const submitting = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({
  pageNum: 1,
  pageSize: 10,
  cityCode: '',
  vehicleType: null
})

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const form = reactive({
  id: null,
  ruleName: '',
  cityCode: '',
  cityName: '',
  vehicleType: '',
  basePrice: 0,
  baseDistanceM: 0,
  baseDurationS: 0,
  pricePerKm: 0,
  pricePerMin: 0,
  minPrice: 0,
  nightStart: '',
  nightEnd: '',
  nightRatio: 1,
  bookingRatio: 1,
  status: 1
})

const rules = {
  ruleName: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
  cityCode: [{ required: true, message: '请输入城市编码', trigger: 'blur' }],
  vehicleType: [{ required: true, message: '请选择车型', trigger: 'change' }],
  basePrice: [{ required: true, message: '请输入起步价', trigger: 'change' }]
}

const dialogTitle = computed(() => (isEdit.value ? '编辑计价规则' : '新增计价规则'))

function vehicleTypeLabel(vehicleType) {
  const item = vehicleTypeOptions.find((option) => option.value === vehicleType)
  return item ? item.label : vehicleType || '-'
}

function formatTime(value) {
  if (!value) {
    return '-'
  }
  return String(value).slice(0, 5)
}

async function fetchList() {
  loading.value = true
  try {
    const data = await pagePricingRules(query)
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
  query.cityCode = ''
  query.vehicleType = null
  handleSearch()
}

function handleSizeChange() {
  query.pageNum = 1
  fetchList()
}

function resetForm() {
  form.id = null
  form.ruleName = ''
  form.cityCode = ''
  form.cityName = ''
  form.vehicleType = ''
  form.basePrice = 0
  form.baseDistanceM = 0
  form.baseDurationS = 0
  form.pricePerKm = 0
  form.pricePerMin = 0
  form.minPrice = 0
  form.nightStart = ''
  form.nightEnd = ''
  form.nightRatio = 1
  form.bookingRatio = 1
  form.status = 1
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
  form.ruleName = row.ruleName || ''
  form.cityCode = row.cityCode || ''
  form.cityName = row.cityName || ''
  form.vehicleType = row.vehicleType || ''
  // 金额字段：分 → 元
  form.basePrice = fenToYuan(row.basePrice)
  form.baseDistanceM = row.baseDistanceM ?? 0
  form.baseDurationS = row.baseDurationS ?? 0
  form.pricePerKm = fenToYuan(row.pricePerKm)
  form.pricePerMin = fenToYuan(row.pricePerMin)
  form.minPrice = fenToYuan(row.minPrice)
  form.nightStart = row.nightStart || ''
  form.nightEnd = row.nightEnd || ''
  form.nightRatio = row.nightRatio ?? 1
  form.bookingRatio = row.bookingRatio ?? 1
  form.status = row.status ?? 1
  dialogVisible.value = true
}

async function submitForm() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) {
    return
  }
  const payload = {
    ruleName: form.ruleName,
    cityCode: form.cityCode,
    cityName: form.cityName,
    vehicleType: form.vehicleType,
    // 金额字段：元 → 分
    basePrice: yuanToFen(form.basePrice),
    baseDistanceM: form.baseDistanceM,
    baseDurationS: form.baseDurationS,
    pricePerKm: yuanToFen(form.pricePerKm),
    pricePerMin: yuanToFen(form.pricePerMin),
    minPrice: yuanToFen(form.minPrice),
    nightStart: form.nightStart || null,
    nightEnd: form.nightEnd || null,
    nightRatio: form.nightRatio,
    bookingRatio: form.bookingRatio,
    status: form.status
  }
  submitting.value = true
  try {
    if (isEdit.value) {
      await updatePricingRule(form.id, payload)
      ElMessage.success('保存成功')
    } else {
      await createPricingRule(payload)
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

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除计价规则「${row.ruleName}」吗？`, '提示', {
      type: 'warning',
      confirmButtonText: '确认删除',
      cancelButtonText: '取消'
    })
  } catch (error) {
    return
  }
  try {
    await deletePricingRule(row.id)
    ElMessage.success('删除成功')
    fetchList()
  } catch (error) {
    // 错误提示已由请求拦截器统一处理
  }
}

onMounted(fetchList)
</script>
