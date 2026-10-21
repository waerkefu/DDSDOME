<template>
  <div class="page page--no-tab">
    <div class="detail__header">
      <el-button text :icon="ArrowLeft" @click="goBack">返回</el-button>
      <el-button text :icon="Refresh" :loading="loading" @click="loadDetail">刷新</el-button>
    </div>

    <template v-if="order">
      <!-- 状态概览 -->
      <div class="card detail__summary">
        <div class="detail__status">
          <span class="detail__status-text">{{ order.statusDesc || orderStatusText(order.status) }}</span>
          <el-tag :type="statusTagType(order.status)" effect="light">
            {{ order.orderTypeDesc || orderTypeText(order.orderType) }}
          </el-tag>
        </div>
        <div class="detail__no">订单号：{{ order.orderNo }}</div>
      </div>

      <!-- 行程信息 -->
      <div class="card">
        <div class="card__title">行程信息</div>
        <div class="row">
          <span class="row__label">起点</span>
          <span class="row__value">{{ order.startAddress || '-' }}</span>
        </div>
        <div class="row">
          <span class="row__label">终点</span>
          <span class="row__value">{{ order.endAddress || '-' }}</span>
        </div>
        <div class="row">
          <span class="row__label">预约时间</span>
          <span class="row__value">{{ formatDateTime(order.bookingTime) }}</span>
        </div>
        <div class="row">
          <span class="row__label">里程</span>
          <span class="row__value">{{ formatKm(order.distanceM) }}</span>
        </div>
        <div class="row">
          <span class="row__label">时长</span>
          <span class="row__value">{{ formatMinutes(order.durationS) }}</span>
        </div>
        <div class="row">
          <span class="row__label">车型</span>
          <span class="row__value">{{ vehicleTypeText(order.vehicleType) }}</span>
        </div>
        <div class="row">
          <span class="row__label">车牌号</span>
          <span class="row__value">{{ order.vehiclePlate || '-' }}</span>
        </div>
      </div>

      <!-- 联系人 -->
      <div class="card">
        <div class="card__title">联系人</div>
        <div class="row">
          <span class="row__label">姓名</span>
          <span class="row__value">{{ order.contactName || '-' }}</span>
        </div>
        <div class="row">
          <span class="row__label">电话</span>
          <span class="row__value">{{ order.contactPhone || '-' }}</span>
        </div>
        <div class="row">
          <span class="row__label">服务商</span>
          <span class="row__value">{{ order.providerName || '待分配' }}</span>
        </div>
        <div class="row">
          <span class="row__label">备注</span>
          <span class="row__value">{{ order.remark || '-' }}</span>
        </div>
      </div>

      <!-- 金额与支付 -->
      <div class="card">
        <div class="card__title">费用与支付</div>
        <div class="row">
          <span class="row__label">预估金额</span>
          <span class="row__value">{{ formatYuan(order.estimateAmount) }}</span>
        </div>
        <div class="row">
          <span class="row__label">实际金额</span>
          <span class="row__value">{{ formatYuan(order.actualAmount) }}</span>
        </div>
        <div class="row">
          <span class="row__label">已支付</span>
          <span class="row__value">{{ formatYuan(order.payAmount) }}</span>
        </div>
        <div class="row">
          <span class="row__label">已退款</span>
          <span class="row__value">{{ formatYuan(order.refundAmount) }}</span>
        </div>
        <div class="divider"></div>
        <div class="row">
          <span class="row__label">支付状态</span>
          <span class="row__value">
            {{ order.payStatusDesc || payStatusText(order.payStatus) }}
          </span>
        </div>
        <template v-if="payment">
          <div class="row">
            <span class="row__label">支付单号</span>
            <span class="row__value">{{ payment.payNo }}</span>
          </div>
          <div class="row">
            <span class="row__label">支付渠道</span>
            <span class="row__value">{{ payment.channel }}</span>
          </div>
          <div class="row">
            <span class="row__label">支付时间</span>
            <span class="row__value">{{ formatDateTime(payment.payTime) }}</span>
          </div>
        </template>
      </div>

      <!-- 时间信息 -->
      <div class="card">
        <div class="card__title">时间信息</div>
        <div class="row">
          <span class="row__label">下单时间</span>
          <span class="row__value">{{ formatDateTime(order.createTime) }}</span>
        </div>
        <div class="row">
          <span class="row__label">接单时间</span>
          <span class="row__value">{{ formatDateTime(order.acceptTime) }}</span>
        </div>
        <div class="row">
          <span class="row__label">开始服务</span>
          <span class="row__value">{{ formatDateTime(order.serviceStartTime) }}</span>
        </div>
        <div class="row">
          <span class="row__label">结束服务</span>
          <span class="row__value">{{ formatDateTime(order.serviceEndTime) }}</span>
        </div>
        <div class="row">
          <span class="row__label">完成时间</span>
          <span class="row__value">{{ formatDateTime(order.finishTime) }}</span>
        </div>
        <div v-if="order.cancelTime" class="row">
          <span class="row__label">取消时间</span>
          <span class="row__value">{{ formatDateTime(order.cancelTime) }}</span>
        </div>
        <div v-if="order.cancelReason" class="row">
          <span class="row__label">取消原因</span>
          <span class="row__value">{{ order.cancelReason }}</span>
        </div>
      </div>

      <!-- 状态时间线 -->
      <div class="card">
        <div class="card__title">状态流转</div>
        <Timeline :status-logs="statusLogs" />
      </div>

      <div class="detail__placeholder"></div>
    </template>

    <div v-else-if="!loading" class="empty">订单不存在或已被删除</div>

    <!-- 底部操作 -->
    <div v-if="order && showActions" class="fixed-bottom-bar">
      <el-button
        v-if="canCancelStatus"
        size="large"
        class="detail__action"
        :disabled="paying"
        @click="openCancel"
      >
        取消订单
      </el-button>
      <el-button
        v-if="order.status === 'PENDING_PAYMENT'"
        type="primary"
        size="large"
        class="detail__action"
        :loading="paying"
        @click="openCashier"
      >
        去支付
      </el-button>
    </div>

    <!-- 模拟收银台 -->
    <el-dialog v-model="cashierVisible" title="模拟收银台" width="86%" align-center>
      <div class="cashier">
        <div class="cashier__amount">{{ formatYuan(paymentCreate && paymentCreate.amount) }}</div>
        <div class="cashier__row">
          <span class="text-sub">订单号</span>
          <span>{{ orderNo }}</span>
        </div>
        <div class="cashier__row">
          <span class="text-sub">支付单号</span>
          <span>{{ paymentCreate && paymentCreate.payNo }}</span>
        </div>
        <div class="cashier__row">
          <span class="text-sub">支付渠道</span>
          <span>{{ (paymentCreate && paymentCreate.channel) || 'MOCK' }}</span>
        </div>
        <div class="cashier__row" v-if="paymentCreate && paymentCreate.expireSeconds">
          <span class="text-sub">有效时间</span>
          <span>{{ paymentCreate.expireSeconds }} 秒</span>
        </div>
        <el-alert
          title="此处为模拟支付网关，确认后将直接回调支付成功"
          type="warning"
          :closable="false"
          show-icon
        />
      </div>
      <template #footer>
        <el-button @click="cashierVisible = false">取消</el-button>
        <el-button type="primary" :loading="confirming" @click="confirmPay">确认支付</el-button>
      </template>
    </el-dialog>

    <!-- 取消订单 -->
    <el-dialog v-model="cancelVisible" title="取消订单" width="86%" align-center>
      <el-form label-position="top">
        <el-form-item label="取消原因（必填）">
          <el-input
            v-model="cancelReason"
            type="textarea"
            :rows="3"
            maxlength="100"
            show-word-limit
            placeholder="请填写取消原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cancelVisible = false">再想想</el-button>
        <el-button type="primary" :loading="cancelling" @click="confirmCancel">确认取消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Refresh } from '@element-plus/icons-vue'
import Timeline from '@/components/Timeline.vue'
import { cancelOrder, getOrderDetail } from '@/api/order'
import { createPayment, getPayment, mockPayCallback } from '@/api/payment'
import {
  canCancel,
  formatDateTime,
  formatKm,
  formatMinutes,
  formatYuan,
  orderStatusText,
  orderTypeText,
  payStatusText,
  randomKey,
  statusTagType
} from '@/utils/format'

const route = useRoute()
const router = useRouter()

const orderNo = computed(() => String(route.params.orderNo || ''))

const loading = ref(false)
const order = ref(null)
const statusLogs = ref([])
const payment = ref(null)

const paying = ref(false)
const confirming = ref(false)
const cancelling = ref(false)

const cashierVisible = ref(false)
const paymentCreate = ref(null)

const cancelVisible = ref(false)
const cancelReason = ref('')

const canCancelStatus = computed(() => !!order.value && canCancel(order.value.status))
const showActions = computed(
  () => canCancelStatus.value || (order.value && order.value.status === 'PENDING_PAYMENT')
)

const VEHICLE_TYPE_MAP = { COMFORT: '舒适型', BUSINESS: '商务型' }

function vehicleTypeText(type) {
  return VEHICLE_TYPE_MAP[type] || type || '-'
}

async function loadDetail() {
  if (!orderNo.value) return
  loading.value = true
  try {
    const data = await getOrderDetail(orderNo.value)
    order.value = data.order
    statusLogs.value = data.statusLogs || []
    await loadPayment()
  } catch (e) {
    order.value = null
    statusLogs.value = []
  } finally {
    loading.value = false
  }
}

/** 已支付/已退款时补充支付单信息，失败不影响主流程 */
async function loadPayment() {
  payment.value = null
  const payStatus = order.value && order.value.payStatus
  if (!payStatus || payStatus === 'UNPAID') return
  try {
    payment.value = await getPayment(orderNo.value)
  } catch (e) {
    payment.value = null
  }
}

/** 发起支付：创建支付单后弹出模拟收银台 */
async function openCashier() {
  paying.value = true
  try {
    paymentCreate.value = await createPayment(orderNo.value, randomKey('pay'))
    cashierVisible.value = true
  } catch (e) {
    // 错误提示已统一处理
  } finally {
    paying.value = false
  }
}

/** 确认支付：调用模拟网关回调 */
async function confirmPay() {
  if (!paymentCreate.value) return
  confirming.value = true
  try {
    await mockPayCallback({ payNo: paymentCreate.value.payNo, success: true })
    cashierVisible.value = false
    ElMessage.success('支付成功')
    await loadDetail()
    router.replace({
      path: '/pay-result',
      query: {
        orderNo: orderNo.value,
        status: 'success',
        amount: String(paymentCreate.value.amount || '')
      }
    })
  } catch (e) {
    // 错误提示已统一处理
  } finally {
    confirming.value = false
  }
}

function openCancel() {
  cancelReason.value = ''
  cancelVisible.value = true
}

async function confirmCancel() {
  const reason = cancelReason.value.trim()
  if (!reason) {
    ElMessage.warning('请填写取消原因')
    return
  }
  cancelling.value = true
  try {
    await cancelOrder(orderNo.value, reason)
    cancelVisible.value = false
    ElMessage.success('订单已取消')
    await loadDetail()
  } catch (e) {
    // 错误提示已统一处理
  } finally {
    cancelling.value = false
  }
}

function goBack() {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.replace('/orders')
  }
}

onMounted(loadDetail)

watch(orderNo, (value) => {
  if (value) loadDetail()
})
</script>

<style scoped>
.detail__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;
}

.detail__status {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.detail__status-text {
  font-size: 18px;
  font-weight: 600;
}

.detail__no {
  margin-top: 6px;
  font-size: 12px;
  color: var(--color-text-sub);
  word-break: break-all;
}

.detail__placeholder {
  height: 56px;
}

.detail__action {
  flex: 1;
}

.cashier__amount {
  text-align: center;
  font-size: 30px;
  font-weight: 700;
  color: #f5222d;
  margin-bottom: 16px;
}

.cashier__row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  font-size: 13px;
  margin-bottom: 8px;
  word-break: break-all;
}

.cashier .el-alert {
  margin-top: 12px;
}
</style>
