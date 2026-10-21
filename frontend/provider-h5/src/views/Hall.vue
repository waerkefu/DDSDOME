<template>
  <div class="page">
    <div class="panel-header">
      <div>
        <div class="panel-title">接单大厅</div>
        <div class="muted">你好，{{ userStore.displayName }}</div>
      </div>
      <el-button :icon="Refresh" circle :loading="loading" @click="loadOrders(true)" />
    </div>

    <div class="status-bar">
      <div class="status-item">
        <span class="dot" :class="isOnline ? 'on' : 'off'"></span>
        在线状态：{{ isOnline ? '在线' : '离线' }}
      </div>
      <div class="status-item">
        <span class="dot" :class="isAccept ? 'on' : 'off'"></span>
        接单开关：{{ isAccept ? '已开启' : '未开启' }}
      </div>
    </div>

    <div v-if="profile && !isAccept" class="notice">
      <span>你当前未开启接单，无法在大厅抢单。</span>
      <el-button type="primary" link :loading="enabling" @click="enableAccept">
        一键开启
      </el-button>
    </div>

    <div v-loading="loading && orders.length === 0" style="min-height: 120px">
      <el-empty v-if="!loading && orders.length === 0" description="暂无可接订单" />

      <div
        v-for="order in orders"
        :key="order.orderNo"
        class="order-card"
        @click="goDetail(order.orderNo)"
      >
        <div class="order-card-head">
          <el-tag size="small" effect="light">
            {{ orderTypeText(order.orderType, order.orderTypeDesc) }}
          </el-tag>
          <span class="order-no">{{ order.orderNo }}</span>
        </div>

        <div class="order-route">
          <div class="route-line">
            <span class="route-dot start"></span>
            <span>{{ order.startAddress || '-' }}</span>
          </div>
          <div class="route-line">
            <span class="route-dot end"></span>
            <span>{{ order.endAddress || '-' }}</span>
          </div>
        </div>

        <div class="order-meta">
          <span>车辆：{{ order.vehiclePlate || order.vehicleType || '-' }}</span>
          <span>预约：{{ formatDateTime(order.bookingTime) }}</span>
        </div>

        <div class="order-meta" style="margin-top: 4px">
          <span>下单时间：{{ formatDateTime(order.createTime) }}</span>
        </div>

        <div class="order-foot">
          <span class="amount order-amount">
            {{ displayYuan(order.estimateAmount, order.estimateAmountYuan) }}
          </span>
          <el-button
            type="primary"
            size="small"
            :loading="grabbingNo === order.orderNo"
            @click.stop="handleGrab(order)"
          >
            立即接单
          </el-button>
        </div>
      </div>
    </div>

    <div v-if="orders.length > 0" class="load-more">
      <el-button v-if="hasMore" plain style="width: 100%" :loading="loading" @click="loadMore">
        加载更多
      </el-button>
      <div v-else class="muted" style="text-align: center">没有更多了</div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { getHallOrders, grabOrder } from '@/api/order'
import { getProfile, updateAcceptEnabled } from '@/api/provider'
import { useUserStore } from '@/stores/user'
import { displayYuan, formatDateTime, orderTypeText } from '@/utils/format'

const POLL_INTERVAL = 15000

const router = useRouter()
const userStore = useUserStore()

const orders = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = 10
const loading = ref(false)
const grabbingNo = ref('')
const enabling = ref(false)
const profile = ref(null)
const extraLoaded = ref(false)
let timer = null

const isOnline = computed(() => profile.value?.onlineStatus === 1)
const isAccept = computed(() => profile.value?.acceptEnabled === 1)
const hasMore = computed(() => orders.value.length < total.value)

async function loadProfile() {
  try {
    profile.value = await getProfile()
  } catch (e) {
    profile.value = null
  }
}

async function loadOrders(reset = false) {
  if (loading.value) return
  loading.value = true
  const targetPage = reset ? 1 : pageNum.value
  try {
    const data = await getHallOrders({ pageNum: targetPage, pageSize })
    const records = data?.records || []
    total.value = Number(data?.total || 0)
    pageNum.value = targetPage + 1
    orders.value = reset ? records : orders.value.concat(records)
  } catch (e) {
    if (reset) {
      orders.value = []
      total.value = 0
    }
  } finally {
    loading.value = false
  }
}

function loadMore() {
  extraLoaded.value = true
  loadOrders(false)
}

async function handleGrab(order) {
  if (grabbingNo.value) return
  grabbingNo.value = order.orderNo
  try {
    await grabOrder(order.orderNo)
    ElMessage.success('接单成功')
    stopPolling()
    router.push({ name: 'OrderDetail', params: { orderNo: order.orderNo } })
  } catch (e) {
    // 失败（如已被接单）用后端 message 提示，并刷新列表
    await loadOrders(true)
  } finally {
    grabbingNo.value = ''
  }
}

async function enableAccept() {
  enabling.value = true
  try {
    await updateAcceptEnabled(1)
    ElMessage.success('已开启接单')
    await loadProfile()
    await loadOrders(true)
  } catch (e) {
    // 错误提示已统一处理
  } finally {
    enabling.value = false
  }
}

function goDetail(orderNo) {
  router.push({ name: 'OrderDetail', params: { orderNo } })
}

function startPolling() {
  stopPolling()
  timer = setInterval(() => {
    if (document.hidden) return
    if (extraLoaded.value) return
    if (grabbingNo.value) return
    loadOrders(true)
  }, POLL_INTERVAL)
}

function stopPolling() {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
}

onMounted(() => {
  loadProfile()
  loadOrders(true)
  startPolling()
})

onUnmounted(() => {
  stopPolling()
})
</script>
