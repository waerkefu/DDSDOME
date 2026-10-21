<template>
  <div class="app-shell" :class="{ 'app-shell--with-tab': showTabBar }">
    <router-view v-slot="{ Component }">
      <component :is="Component" />
    </router-view>
    <TabBar v-if="showTabBar" />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import TabBar from '@/components/TabBar.vue'

const route = useRoute()

// 仅主页面（首页 / 订单 / 我的）展示底部导航
const showTabBar = computed(() => route.meta.tab === true)
</script>
