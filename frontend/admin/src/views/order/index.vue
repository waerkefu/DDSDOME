<template>
  <div class="page-container">
    <el-card shadow="never">
      <el-form :inline="true" :model="query" class="search-form">
        <el-form-item label="订单号">
          <el-input v-model="query.orderNo" placeholder="请输入订单号" clearable style="width: 200px" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 150px">
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="订单类型">
          <el-select v-model="query.orderType" placeholder="全部" clearable style="width: 150px">
            <el-option v-for="item in orderTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="创建时间">
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 380px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column prop="orderNo" label="订单号" min-width="180" />
        <el-table-column prop="orderTypeDesc" label="类型" width="110" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag size="small" :type="statusTag(row.status)">{{ row.statusDesc || statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="客户" min-width="120">
          <template #default="{ row }">{{ row.customerName || '-' }}</template>
        </el-table-column>
        <el-table-column label="服务商" min-width="120">
          <template #default="{ row }">{{ row.providerName || '-' }}</template>
        </el-table-column>
        <el-table-column label="行程" min-width="240" show-overflow-tooltip>
          <template #default="{ row }">{{ row.startAddress || '-' }} → {{ row.endAddress || '-' }}</template>
        </el-table-column>
        <el-table-column label="预估金额" width="110">
          <template #default="{ row }">¥{{ formatYuan(row.estimateAmount) }}</template>
        </el-table-column>
        <el-table-column label="支付金额" width="110">
          <template #default="{ row }">¥{{ formatYuan(row.payAmount) }}</template>
        </el-table-column>
        <el-table-column label="支付状态" width="110">
          <template #default="{ row }">{{ row.payStatusDesc || row.payStatus || '-' }}</template>
        </el-table-column>
        <el-table-column label="创建时间" min-width="170">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="190" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
            <el-button v-if="canCancel(row)" link type="warning" @click="openCancel(row)">取消</el-button>
            <el-button v-if="canRefund(row)" link type="danger" @click="openRefund(row)">退款</el-button>
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

    <el-drawer v-model="detailVisible" title="订单详情" size="640px">
      <div v-loading="detailLoading">
        <template v-if="detail.order">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="订单号">{{ detail.order.orderNo }}</el-descriptions-item>
            <el-descriptions-item label="订单类型">{{ detail.order.orderTypeDesc || '-' }}</el-descriptions-item>
            <el-descriptions-item label="订单状态">
              {{ detail.order.statusDesc || statusLabel(detail.order.status) }}
            </el-descriptions-item>
            <el-descriptions-item label="支付状态">
              {{ detail.order.payStatusDesc || detail.order.payStatus || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="客户">{{ detail.order.customerName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="服务商">{{ detail.order.providerName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="联系人">
              {{ detail.order.contactName || '-' }} / {{ detail.order.contactPhone || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="城市编码">{{ detail.order.cityCode || '-' }}</el-descriptions-item>
            <el-descriptions-item label="车型 / 车牌">
              {{ detail.order.vehicleType || '-' }} / {{ detail.order.vehiclePlate || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="预约时间">
              {{ formatDateTime(detail.order.bookingTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="起始地址" :span="2">
              {{ detail.order.startAddress || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="终点地址" :span="2">
              {{ detail.order.endAddress || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="接单时间">
              {{ formatDateTime(detail.order.acceptTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="完成时间">
              {{ formatDateTime(detail.order.finishTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="服务开始">
              {{ formatDateTime(detail.order.serviceStartTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="服务结束">
              {{ formatDateTime(detail.order.serviceEndTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="距离">{{ formatDistance(detail.order.distanceM) }}</el-descriptions-item>
            <el-descriptions-item label="时长">{{ formatDuration(detail.order.durationS) }}</el-descriptions-item>
            <el-descriptions-item label="预估金额">
              ¥{{ formatYuan(detail.order.estimateAmount) }}
            </el-descriptions-item>
            <el-descriptions-item label="实际金额">
              ¥{{ formatYuan(detail.order.actualAmount) }}
            </el-descriptions-item>
            <el-descriptions-item label="支付金额">
              ¥{{ formatYuan(detail.order.payAmount) }}
            </el-descriptions-item>
            <el-descriptions-item label="退款金额">
              ¥{{ formatYuan(detail.order.refundAmount) }}
            </el-descriptions-item>
            <el-descriptions-item label="取消时间">
              {{ formatDateTime(detail.order.cancelTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="取消原因">{{ detail.order.cancelReason || '-' }}</el-descriptions-item>
            <el-descriptions-item label="备注" :span="2">{{ detail.order.remark || '-' }}</el-descriptions-item>
            <el-descriptions-item label="创建时间" :span="2">
              {{ formatDateTime(detail.order.createTime) }}
            </el-descriptions-item>
          </el-descriptions>

          <el-divider content-position="left">状态流转记录</el-divider>
          <el-timeline v-if="detail.statusLogs && detail.statusLogs.length">
            <el-timeline-item
              v-for="(log, index) in detail.statusLogs"
              :key="index"
              :timestamp="formatDateTime(log.createTime)"
              placement="top"
            >
              <div>
                {{ log.fromStatusDesc || log.fromStatus || '-' }} →
                <strong>{{ log.toStatusDesc || log.toStatus }}</strong>
              </div>
              <div class="text-muted">
                操作人：{{ operatorLabel(log.operatorType) }} {{ log.operatorName || '' }}
              </div>
              <div v-if="log.remark" class="text-muted">备注：{{ log.remark }}</div>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暂无状态流转记录" />
        </template>
      </div>
    </el-drawer>

    <el-dialog v-model="cancelVisible" title="取消订单" width="460px">
      <el-form ref="cancelFormRef" :model="cancelForm" :rules="cancelRules" label-width="80px">
        <el-form-item label="订单号">
          <el-input :model-value="cancelForm.orderNo" disabled />
        </el-form-item>
        <el-form-item label="取消原因" prop="reason">
          <el-input v-model="cancelForm.reason" type="textarea" :rows="3" placeholder="请输入取消原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cancelVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitCancel">确认取消</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="refundVisible" title="订单退款" width="460px">
      <el-form ref="refundFormRef" :model="refundForm" :rules="refundRules" label-width="90px">
        <el-form-item label="订单号">
          <el-input :model-value="refundForm.orderNo" disabled />
        </el-form-item>
        <el-form-item label="可退金额">
          <el-input :model-value="`¥${refundForm.refundable}`" disabled />
        </el-form-item>
        <el-form-item label="退款金额" prop="amount">
          <el-input-number v-model="refundForm.amount" :min="0.01" :precision="2" :step="1" style="width: 100%" />
          <div class="form-tip">单位：元，提交时自动换算为分</div>
        </el-form-item>
        <el-form-item label="退款原因" prop="reason">
          <el-input v-model="refundForm.reason" type="textarea" :rows="3" placeholder="请输入退款原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="refundVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitRefund">确认退款</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Search } from '@element-plus/icons-vue'
import { cancelOrder, getOrderDetail, pageOrders, refundOrder } from '@/api/order'
import { formatDateTime, formatYuan, yuanToFen } from '@/utils/format'

const statusOptions = [
  { label: '待接单', value: 'PENDING_ACCEPT' },
  { label: '已接单', value: 'ACCEPTED' },
  { label: '服务中', value: 'IN_SERVICE' },
  { label: '待支付', value: 'PENDING_PAYMENT' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已取消', value: 'CANCELLED' },
  { label: '超时关闭', value: 'CLOSED' }
]

const orderTypeOptions = [
  { label: '即时代驾', value: 'IMMEDIATE' },
  { label: '预约代驾', value: 'SCHEDULED' },
  { label: '年审代办', value: 'INSPECTION' },
  { label: '换证代办', value: 'RENEWAL' }
]

const operatorOptions = [
  { label: '客户', value: 1 },
  { label: '服务商', value: 2 },
  { label: '管理员', value: 3 },
  { label: '系统', value: 4 }
]

// 后端允许管理员取消的状态
const cancellableStatus = ['PENDING_ACCEPT', 'ACCEPTED', 'PENDING_PAYMENT']
// 允许退款的支付状态
const refundablePayStatus = ['PAID', 'PARTIAL_REFUNDED']

const loading = ref(false)
const submitting = ref(false)
const list = ref([])
const total = ref(0)
const dateRange = ref([])
const query = reactive({
  pageNum: 1,
  pageSize: 10,
  orderNo: '',
  status: null,
  orderType: null
})

const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = reactive({ order: null, statusLogs: [] })

const cancelVisible = ref(false)
const cancelFormRef = ref(null)
const cancelForm = reactive({ orderNo: '', reason: '' })
const cancelRules = {
  reason: [{ required: true, message: '请输入取消原因', trigger: 'blur' }]
}

const refundVisible = ref(false)
const refundFormRef = ref(null)
const refundForm = reactive({ orderNo: '', amount: 0.01, reason: '', refundable: '0.00' })
const refundRules = {
  amount: [{ required: true, message: '请输入退款金额', trigger: 'blur' }],
  reason: [{ required: true, message: '请输入退款原因', trigger: 'blur' }]
}

function statusLabel(status) {
  const item = statusOptions.find((option) => option.value === status)
  return item ? item.label : status || '-'
}

function statusTag(status) {
  if (status === 'COMPLETED') {
    return 'success'
  }
  if (status === 'CANCELLED' || status === 'CLOSED') {
    return 'info'
  }
  if (status === 'PENDING_ACCEPT' || status === 'PENDING_PAYMENT') {
    return 'warning'
  }
  return 'primary'
}

function operatorLabel(operatorType) {
  const item = operatorOptions.find((option) => option.value === operatorType)
  return item ? item.label : '未知'
}

function formatDistance(distanceM) {
  if (distanceM === null || distanceM === undefined) {
    return '-'
  }
  return `${(Number(distanceM) / 1000).toFixed(2)} 公里`
}

function formatDuration(durationS) {
  if (durationS === null || durationS === undefined) {
    return '-'
  }
  return `${Math.ceil(Number(durationS) / 60)} 分钟`
}

function canCancel(row) {
  return cancellableStatus.includes(row.status)
}

function canRefund(row) {
  return refundablePayStatus.includes(row.payStatus)
}

async function fetchList() {
  loading.value = true
  try {
    const params = { ...query }
    if (dateRange.value && dateRange.value.length === 2) {
      params.startTime = dateRange.value[0]
      params.endTime = dateRange.value[1]
    }
    const data = await pageOrders(params)
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
  query.orderNo = ''
  query.status = null
  query.orderType = null
  dateRange.value = []
  handleSearch()
}

function handleSizeChange() {
  query.pageNum = 1
  fetchList()
}

async function openDetail(row) {
  detailVisible.value = true
  detailLoading.value = true
  detail.order = null
  detail.statusLogs = []
  try {
    const data = await getOrderDetail(row.orderNo)
    detail.order = (data && data.order) || null
    detail.statusLogs = (data && data.statusLogs) || []
  } catch (error) {
    // 错误提示已由请求拦截器统一处理
  } finally {
    detailLoading.value = false
  }
}

function openCancel(row) {
  cancelForm.orderNo = row.orderNo
  cancelForm.reason = ''
  cancelVisible.value = true
}

async function submitCancel() {
  const valid = await cancelFormRef.value.validate().catch(() => false)
  if (!valid) {
    return
  }
  submitting.value = true
  try {
    await cancelOrder(cancelForm.orderNo, cancelForm.reason)
    ElMessage.success('订单已取消')
    cancelVisible.value = false
    fetchList()
  } catch (error) {
    // 错误提示已由请求拦截器统一处理
  } finally {
    submitting.value = false
  }
}

function openRefund(row) {
  refundForm.orderNo = row.orderNo
  refundForm.reason = ''
  const refundableFen = Number(row.payAmount || 0) - Number(row.refundAmount || 0)
  refundForm.refundable = formatYuan(refundableFen)
  refundForm.amount = Number(refundForm.refundable)
  refundVisible.value = true
}

async function submitRefund() {
  const valid = await refundFormRef.value.validate().catch(() => false)
  if (!valid) {
    return
  }
  submitting.value = true
  try {
    await refundOrder(refundForm.orderNo, {
      amount: yuanToFen(refundForm.amount),
      reason: refundForm.reason
    })
    ElMessage.success('退款申请已提交')
    refundVisible.value = false
    fetchList()
  } catch (error) {
    // 错误提示已由请求拦截器统一处理
  } finally {
    submitting.value = false
  }
}

onMounted(fetchList)
</script>
