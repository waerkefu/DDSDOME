<template>
  <div class="page">
    <div class="detail-header">
      <el-icon class="back" @click="goBack"><ArrowLeft /></el-icon>
      <span class="detail-title">订单详情</span>
      <div style="flex: 1"></div>
      <el-button :icon="Refresh" circle size="small" :loading="loading" @click="loadDetail" />
    </div>

    <el-skeleton v-if="loading && !order" :rows="8" animated />

    <template v-else-if="order">
      <el-card class="card" shadow="never">
        <div class="order-card-head">
          <el-tag :type="statusTagType(order.status)" effect="dark">
            {{ orderStatusText(order.status, order.statusDesc) }}
          </el-tag>
          <el-tag size="small" effect="light">
            {{ orderTypeText(order.orderType, order.orderTypeDesc) }}
          </el-tag>
        </div>
        <div class="muted" style="margin-top: 8px">订单号：{{ order.orderNo }}</div>

        <div class="action-row">
          <el-button
            v-if="order.status === 'PENDING_ACCEPT'"
            type="primary"
            :loading="acting"
            @click="handleGrab"
          >
            接单
          </el-button>
          <el-button
            v-else-if="order.status === 'ACCEPTED'"
            type="primary"
            :loading="acting"
            @click="handleStart"
          >
            开始服务
          </el-button>
          <el-button
            v-else-if="order.status === 'IN_SERVICE'"
            type="primary"
            :loading="acting"
            @click="finishVisible = true"
          >
            结束服务
          </el-button>
          <span v-else class="muted">当前状态无需操作</span>
        </div>
      </el-card>

      <el-card class="card" shadow="never" header="行程信息">
        <div class="detail-row">
          <span class="label">起点</span>
          <span class="value">{{ order.startAddress || '-' }}</span>
        </div>
        <div class="detail-row">
          <span class="label">终点</span>
          <span class="value">{{ order.endAddress || '-' }}</span>
        </div>
        <div class="detail-row">
          <span class="label">预约时间</span>
          <span class="value">{{ formatDateTime(order.bookingTime) }}</span>
        </div>
        <div class="detail-row">
          <span class="label">车辆信息</span>
          <span class="value">{{ vehicleText }}</span>
        </div>
        <div class="detail-row">
          <span class="label">城市编码</span>
          <span class="value">{{ order.cityCode || '-' }}</span>
        </div>
        <div class="detail-row">
          <span class="label">备注</span>
          <span class="value">{{ order.remark || '-' }}</span>
        </div>
      </el-card>

      <el-card class="card" shadow="never" header="客户信息">
        <div class="detail-row">
          <span class="label">客户</span>
          <span class="value">{{ order.contactName || order.customerName || '-' }}</span>
        </div>
        <div class="detail-row">
          <span class="label">联系电话</span>
          <span class="value">
            <a v-if="order.contactPhone" :href="`tel:${order.contactPhone}`">
              {{ order.contactPhone }}
            </a>
            <span v-else>-</span>
          </span>
        </div>
        <div class="detail-row">
          <span class="label">服务商</span>
          <span class="value">{{ order.providerName || '-' }}</span>
        </div>
      </el-card>

      <el-card class="card" shadow="never" header="费用与里程">
        <div class="detail-row">
          <span class="label">预估金额</span>
          <span class="value amount">
            {{ displayYuan(order.estimateAmount, order.estimateAmountYuan) }}
          </span>
        </div>
        <div class="detail-row">
          <span class="label">实际金额</span>
          <span class="value amount">
            {{ displayYuan(order.actualAmount, order.actualAmountYuan) }}
          </span>
        </div>
        <div class="detail-row">
          <span class="label">实付金额</span>
          <span class="value">{{ displayYuan(order.payAmount, order.payAmountYuan) }}</span>
        </div>
        <div class="detail-row">
          <span class="label">退款金额</span>
          <span class="value">{{ displayYuan(order.refundAmount, order.refundAmountYuan) }}</span>
        </div>
        <div class="detail-row">
          <span class="label">支付状态</span>
          <span class="value">{{ order.payStatusDesc || '-' }}</span>
        </div>
        <div class="detail-row">
          <span class="label">服务里程</span>
          <span class="value">{{ formatDistance(order.distanceM) }}</span>
        </div>
        <div class="detail-row">
          <span class="label">服务时长</span>
          <span class="value">{{ formatDuration(order.durationS) }}</span>
        </div>
      </el-card>

      <el-card class="card" shadow="never" header="时间信息">
        <div class="detail-row">
          <span class="label">下单时间</span>
          <span class="value">{{ formatDateTime(order.createTime) }}</span>
        </div>
        <div class="detail-row">
          <span class="label">接单时间</span>
          <span class="value">{{ formatDateTime(order.acceptTime) }}</span>
        </div>
        <div class="detail-row">
          <span class="label">开始服务</span>
          <span class="value">{{ formatDateTime(order.serviceStartTime) }}</span>
        </div>
        <div class="detail-row">
          <span class="label">结束服务</span>
          <span class="value">{{ formatDateTime(order.serviceEndTime) }}</span>
        </div>
        <div class="detail-row">
          <span class="label">完成时间</span>
          <span class="value">{{ formatDateTime(order.finishTime) }}</span>
        </div>
        <div v-if="order.cancelTime" class="detail-row">
          <span class="label">取消时间</span>
          <span class="value">{{ formatDateTime(order.cancelTime) }}</span>
        </div>
        <div v-if="order.cancelReason" class="detail-row">
          <span class="label">取消原因</span>
          <span class="value">{{ order.cancelReason }}</span>
        </div>
      </el-card>

      <el-card class="card" shadow="never" header="状态时间线">
        <el-timeline v-if="statusLogs.length > 0">
          <el-timeline-item
            v-for="(log, index) in statusLogs"
            :key="index"
            :timestamp="formatDateTime(log.createTime)"
            placement="top"
            :type="index === statusLogs.length - 1 ? 'primary' : undefined"
            :hollow="index !== statusLogs.length - 1"
          >
            <div class="strong">
              {{ log.toStatusDesc || orderStatusText(log.toStatus) }}
            </div>
            <div class="muted">
              {{ log.fromStatusDesc || orderStatusText(log.fromStatus) }} →
              {{ log.toStatusDesc || orderStatusText(log.toStatus) }}
            </div>
            <div class="muted">
              操作人：{{ log.operatorName || log.operatorType || '-' }}
              <template v-if="log.remark"> · {{ log.remark }}</template>
            </div>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-else description="暂无流转记录" :image-size="60" />
      </el-card>
    </template>

    <el-empty v-else description="订单不存在或已失效" />

    <el-dialog
      v-model="finishVisible"
      title="结束服务"
      width="86%"
      align-center
      :close-on-click-modal="false"
    >
      <el-form :model="finishForm" label-position="top">
        <el-form-item label="实际里程（公里）">
          <el-input-number
            v-model="finishForm.km"
            :min="0"
            :max="9999"
            :step="0.1"
            :precision="1"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="实际时长（分钟）">
          <el-input-number
            v-model="finishForm.minutes"
            :min="0"
            :max="9999"
            :step="1"
            :precision="0"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="finishVisible = false">取消</el-button>
        <el-button type="primary" :loading="acting" @click="submitFinish">确认结束</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Refresh } from '@element-plus/icons-vue'
import { finishService, getOrderDetail, grabOrder, startService } from '@/api/order'
import {
  displayYuan,
  formatDateTime,
  formatDistance,
  formatDuration,
  orderStatusText,
  orderTypeText,
  statusTagType
} from '@/utils/format'

const route = useRoute()
const router = useRouter()

const orderNo = route.params.orderNo
const order = ref(null)
const statusLogs = ref([])
const loading = ref(false)
const acting = ref(false)
const finishVisible = ref(false)
const finishForm = reactive({ km: 0, minutes: 0 })

const vehicleText = computed(() => {
  const o = order.value
  if (!o) return '-'
  const parts = [o.vehiclePlate, o.vehicleType].filter(Boolean)
  return parts.length ? parts.join(' · ') : '-'
})

async function loadDetail() {
  if (!orderNo) return
  loading.value = true
  try {
    const data = await getOrderDetail(orderNo)
    order.value = data?.order || null
    statusLogs.value = data?.statusLogs || []
  } catch (e) {
    order.value = null
    statusLogs.value = []
  } finally {
    loading.value = false
  }
}

async function handleGrab() {
  await doAction(() => grabOrder(orderNo), '接单成功')
}

async function handleStart() {
  await doAction(() => startService(orderNo), '已开始服务')
}

async function submitFinish() {
  const distanceM = Math.round(Number(finishForm.km || 0) * 1000)
  const durationS = Math.round(Number(finishForm.minutes || 0) * 60)
  const ok = await doAction(
    () => finishService(orderNo, { distanceM, durationS }),
    '服务已结束'
  )
  if (ok) {
    finishVisible.value = false
  }
}

async function doAction(action, successMsg) {
  if (acting.value) return false
  acting.value = true
  try {
    await action()
    ElMessage.success(successMsg)
    await loadDetail()
    return true
  } catch (e) {
    // 失败用后端 message 提示（已在拦截器统一处理）
    return false
  } finally {
    acting.value = false
  }
}

function goBack() {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/orders')
  }
}

onMounted(() => {
  loadDetail()
})
</script>
