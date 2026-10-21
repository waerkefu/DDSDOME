<template>
  <nav class="tabbar">
    <div
      v-for="item in tabs"
      :key="item.path"
      class="tabbar__item"
      :class="{ 'is-active': activePath === item.path }"
      @click="go(item.path)"
    >
      <el-icon class="tabbar__icon"><component :is="item.icon" /></el-icon>
      <span class="tabbar__text">{{ item.text }}</span>
    </div>
  </nav>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const tabs = [
  { path: '/home', text: '下单', icon: 'Guide' },
  { path: '/orders', text: '我的订单', icon: 'List' },
  { path: '/mine', text: '我的', icon: 'User' }
]

// 订单详情页归属「我的订单」高亮
const activePath = computed(() => {
  if (route.path.startsWith('/orders')) return '/orders'
  return route.path
})

function go(path) {
  if (route.path !== path) {
    router.push(path)
  }
}
</script>

<style scoped>
.tabbar {
  position: fixed;
  left: 50%;
  transform: translateX(-50%);
  bottom: 0;
  width: 100%;
  max-width: var(--app-max-width);
  height: calc(var(--tabbar-height) + env(safe-area-inset-bottom));
  padding-bottom: env(safe-area-inset-bottom);
  background: #fff;
  border-top: 1px solid #eceef1;
  display: flex;
  z-index: 100;
}

.tabbar__item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  color: #8a9099;
  font-size: 11px;
  cursor: pointer;
  user-select: none;
}

.tabbar__item.is-active {
  color: var(--color-primary);
}

.tabbar__icon {
  font-size: 20px;
}
</style>
