<template>
  <nav class="tabbar">
    <div
      v-for="item in tabs"
      :key="item.path"
      class="tabbar-item"
      :class="{ active: route.path === item.path }"
      @click="go(item.path)"
    >
      <el-icon class="tabbar-icon"><component :is="item.icon" /></el-icon>
      <span>{{ item.label }}</span>
    </div>
  </nav>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'
import { List, Tickets, User } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

const tabs = [
  { path: '/hall', label: '接单大厅', icon: List },
  { path: '/orders', label: '我的订单', icon: Tickets },
  { path: '/mine', label: '我的', icon: User }
]

function go(path) {
  if (route.path !== path) {
    router.push(path)
  }
}
</script>
