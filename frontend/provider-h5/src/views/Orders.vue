<template>
  <div class="page">
    <div class="panel-header">
      <div class="panel-title">我的订单</div>
      <el-button :icon="Refresh" circle :loading="loading" @click="loadOrders(true)" />
    </div>

    <el-tabs v-model="activeStatus" class="status-tabs" @tab-change="handleTabChange">
      <el-tab-pane
        v-for="item in ORDER_STATUS_FILTERS"
        :key="item.value || 'ALL'"
        :label="item.label"
        :name="item.value"
      />
    </el-tabs>

    <div v-loading="loading && orders.length === 0" style="min-height: 120px">
      <el-empty v-if="!loading && orders.length === 0" description="暂无订单" />

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
          <el-tag size="small" :type="statusTagType(order.status)" effect="dark">
            {{ orderStatusText(order.status, order.statusDesc) }}
          </el-tag>
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
          <span class="order-no">{{ order.orderNo }}</span>
          <span>{{ order.vehiclePlate || order.vehicleType || '-' }}</span>
          <span>预约：{{ formatDateTime(order.bookingTime) }}</span>
        </div>

        <div class="order-foot">
          <span class="amount order-amount">{{ orderAmount(order) }}</span>
          <el-button type="primary" size="small" plain @click.stop="goDetail(order.orderNo)">
            查看详情
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
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Refresh } from '@element-plus/icons-vue'
import { getMyOrders } from '@/api/order'
import {
  ORDER_STATUS_FILTERS,
  displayYuan,
  formatDateTime,
  orderStatusText,
  orderTypeText,
  statusTagType
} from '@/utils/format'

const router = useRouter()

const activeStatus = ref('')
const orders = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = 10
const loading = ref(false)

const hasMore = computed(() => orders.value.length < total.value)

/** 有实际金额则展示实际金额，否则展示预估金额 */
function orderAmount(order) {
  if (order.actualAmount !== null && order.actualAmount !== undefined) {
    return displayYuan(order.actualAmount, order.actualAmountYuan)
  }
  return displayYuan(order.estimateAmount, order.estimateAmountYuan)
}

async function loadOrders(reset = false) {
  if (loading.value) return
  loading.value = true
  const targetPage = reset ? 1 : pageNum.value
  try {
    const data = await getMyOrders({
      pageNum: targetPage,
      pageSize,
      status: activeStatus.value || undefined
    })
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
  loadOrders(false)
}

function handleTabChange() {
  loadOrders(true)
}

function goDetail(orderNo) {
  router.push({ name: 'OrderDetail', params: { orderNo } })
}

onMounted(() => {
  loadOrders(true)
})
</script>

<style scoped>
.status-tabs :deep(.el-tabs__header) {
  margin-bottom: 12px;
}
.status-tabs :deep(.el-tabs__nav-wrap::after) {
  height: 1px;
}
</style>
