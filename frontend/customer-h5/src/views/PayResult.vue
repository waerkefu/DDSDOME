<template>
  <div class="page page--no-tab">
    <div class="result">
      <el-icon class="result__icon" :class="isSuccess ? 'is-success' : 'is-fail'">
        <component :is="isSuccess ? 'CircleCheckFilled' : 'CircleCloseFilled'" />
      </el-icon>
      <div class="result__title">{{ isSuccess ? '支付成功' : '支付失败' }}</div>
      <div class="result__amount">{{ formatYuan(amount) }}</div>
      <div class="result__no">订单号：{{ orderNo || '-' }}</div>
    </div>

    <div class="result__actions">
      <el-button type="primary" size="large" class="result__btn" @click="goOrder">
        查看订单
      </el-button>
      <el-button size="large" class="result__btn" @click="goHome">继续下单</el-button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { formatYuan } from '@/utils/format'

const route = useRoute()
const router = useRouter()

const isSuccess = computed(() => route.query.status !== 'fail')
const orderNo = computed(() => String(route.query.orderNo || ''))
const amount = computed(() => {
  const value = route.query.amount
  return value === undefined || value === '' ? 0 : Number(value)
})

function goOrder() {
  if (orderNo.value) {
    router.replace(`/orders/${orderNo.value}`)
  } else {
    router.replace('/orders')
  }
}

function goHome() {
  router.replace('/home')
}
</script>

<style scoped>
.result {
  text-align: center;
  padding: 56px 0 24px;
}

.result__icon {
  font-size: 72px;
}

.result__icon.is-success {
  color: #52c41a;
}

.result__icon.is-fail {
  color: #f5222d;
}

.result__title {
  font-size: 20px;
  font-weight: 600;
  margin-top: 12px;
}

.result__amount {
  font-size: 26px;
  font-weight: 700;
  color: #f5222d;
  margin-top: 10px;
}

.result__no {
  font-size: 12px;
  color: var(--color-text-sub);
  margin-top: 8px;
  word-break: break-all;
}

.result__actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 16px;
}

.result__btn {
  width: 100%;
  margin-left: 0;
}
</style>
