<template>
  <div class="page">
    <div class="orders__header">
      <h2 class="page-title">我的订单</h2>
      <el-button text :icon="Refresh" :loading="loading" @click="refresh">刷新</el-button>
    </div>

    <!-- 状态筛选 -->
    <div class="filter">
      <div
        v-for="item in statusFilters"
        :key="item.value"
        class="filter__chip"
        :class="{ 'is-active': status === item.value }"
        @click="onStatusChange(item.value)"
      >
        {{ item.label }}
      </div>
    </div>

    <div v-if="orders.length" class="orders__list">
      <div v-for="order in orders" :key="order.orderNo" class="card order" @click="goDetail(order)">
        <div class="order__head">
          <span class="order__no">单号：{{ order.orderNo }}</span>
          <el-tag :type="statusTagType(order.status)" size="small" effect="light">
            {{ order.statusDesc || orderStatusText(order.status) }}
          </el-tag>
        </div>

        <div class="order__route">
          <div class="order__route-line">
            <span class="order__dot order__dot--start"></span>
            <span class="order__addr">{{ order.startAddress || '-' }}</span>
          </div>
          <div class="order__route-line">
            <span class="order__dot order__dot--end"></span>
            <span class="order__addr">{{ order.endAddress || '-' }}</span>
          </div>
        </div>

        <div class="order__foot">
          <div class="order__meta">
            <el-tag size="small" type="info" effect="plain">
              {{ order.orderTypeDesc || orderTypeText(order.orderType) }}
            </el-tag>
            <span class="text-sub order__time">{{ formatDateTime(order.createTime) }}</span>
          </div>
          <span class="amount">{{ formatYuan(order.payAmount || order.actualAmount || order.estimateAmount) }}</span>
        </div>
      </div>
    </div>

    <div v-else-if="!loading" class="empty">暂无订单</div>

    <div class="orders__more">
      <el-button v-if="hasMore" :loading="loading" @click="loadMore">加载更多</el-button>
      <span v-else-if="orders.length" class="text-sub">没有更多了</span>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Refresh } from '@element-plus/icons-vue'
import { pageOrders } from '@/api/order'
import { formatDateTime, formatYuan, orderStatusText, orderTypeText, statusTagType } from '@/utils/format'

const router = useRouter()

const statusFilters = [
  { label: '全部', value: '' },
  { label: '待接单', value: 'PENDING_ACCEPT' },
  { label: '已接单', value: 'ACCEPTED' },
  { label: '服务中', value: 'IN_SERVICE' },
  { label: '待支付', value: 'PENDING_PAYMENT' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已取消', value: 'CANCELLED' }
]

const PAGE_SIZE = 10

const status = ref('')
const orders = ref([])
const pageNum = ref(1)
const total = ref(0)
const loading = ref(false)

const hasMore = ref(false)

async function fetchPage(reset) {
  if (loading.value) return
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: PAGE_SIZE }
    if (status.value) params.status = status.value
    const data = await pageOrders(params)
    const records = (data && data.records) || []
    total.value = (data && data.total) || 0
    orders.value = reset ? records : orders.value.concat(records)
    hasMore.value = orders.value.length < total.value
  } catch (e) {
    if (reset) {
      orders.value = []
      hasMore.value = false
    }
  } finally {
    loading.value = false
  }
}

function refresh() {
  pageNum.value = 1
  fetchPage(true)
}

function loadMore() {
  if (!hasMore.value || loading.value) return
  pageNum.value += 1
  fetchPage(false)
}

function onStatusChange(value) {
  if (status.value === value) return
  status.value = value
  refresh()
}

function goDetail(order) {
  router.push(`/orders/${order.orderNo}`)
}

onMounted(refresh)
</script>

<style scoped>
.orders__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.orders__header .page-title {
  margin-bottom: 0;
}

.filter {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  padding: 10px 0;
  margin-bottom: 4px;
  -webkit-overflow-scrolling: touch;
}

.filter__chip {
  flex: 0 0 auto;
  padding: 6px 14px;
  border-radius: 16px;
  background: #fff;
  color: var(--color-text-sub);
  font-size: 13px;
  cursor: pointer;
  border: 1px solid #eceef1;
}

.filter__chip.is-active {
  background: var(--color-primary);
  border-color: var(--color-primary);
  color: #fff;
}

.order {
  cursor: pointer;
}

.order__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.order__no {
  font-size: 12px;
  color: var(--color-text-sub);
  word-break: break-all;
}

.order__route {
  margin: 10px 0;
}

.order__route-line {
  display: flex;
  align-items: center;
  gap: 8px;
}

.order__route-line + .order__route-line {
  margin-top: 6px;
}

.order__dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex: 0 0 auto;
}

.order__dot--start {
  background: #52c41a;
}

.order__dot--end {
  background: #f5222d;
}

.order__addr {
  font-size: 14px;
  word-break: break-all;
}

.order__foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding-top: 10px;
  border-top: 1px solid #eef0f3;
}

.order__meta {
  display: flex;
  align-items: center;
  gap: 8px;
}

.order__time {
  font-size: 12px;
}

.orders__more {
  text-align: center;
  padding: 8px 0 16px;
}
</style>
