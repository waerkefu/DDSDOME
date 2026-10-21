<template>
  <div class="page-container">
    <el-card shadow="never" class="welcome-card">
      <div class="welcome-card__body">
        <el-avatar :size="56" :src="userStore.avatar">
          {{ userStore.displayName.charAt(0) }}
        </el-avatar>
        <div>
          <div class="welcome-card__title">你好，{{ userStore.displayName }}</div>
          <div class="text-muted">
            账号：{{ (userStore.userInfo && userStore.userInfo.username) || '-' }}；
            角色：{{ roleText }}
          </div>
          <div class="text-muted">
            当前已授权 {{ userStore.permissions.length }} 项功能权限，可从下方入口进入各模块。
          </div>
        </div>
      </div>
    </el-card>

    <el-row :gutter="16" class="entry-row">
      <el-col v-for="item in visibleEntries" :key="item.path" :xs="24" :sm="12" :md="8" :lg="6">
        <el-card shadow="hover" class="entry-card" @click="router.push(item.path)">
          <div class="entry-card__body">
            <el-icon :size="30" class="entry-card__icon"><component :is="item.icon" /></el-icon>
            <div>
              <div class="entry-card__title">{{ item.title }}</div>
              <div class="text-muted">{{ item.desc }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-empty v-if="visibleEntries.length === 0" description="当前账号暂无可用功能权限，请联系管理员分配" />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const entries = [
  { path: '/system/user', title: '用户管理', icon: 'User', perm: 'system:user', desc: '账号、状态与角色分配' },
  { path: '/system/provider', title: '服务商管理', icon: 'Avatar', perm: 'system:provider', desc: '服务商档案与接单资质' },
  { path: '/system/role', title: '角色权限', icon: 'Lock', perm: 'system:role', desc: '角色维护与权限授权' },
  { path: '/order', title: '订单管理', icon: 'Tickets', perm: 'order:list', desc: '订单检索、详情、取消与退款' },
  { path: '/pricing', title: '计价规则', icon: 'Money', perm: 'pricing:rule', desc: '城市车型计价规则维护' }
]

const visibleEntries = computed(() =>
  entries.filter((item) => userStore.permissions.includes(item.perm))
)

const roleText = computed(() => (userStore.roles.length ? userStore.roles.join('、') : '无'))
</script>

<style scoped>
.welcome-card__body {
  display: flex;
  align-items: center;
  gap: 16px;
}

.welcome-card__title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 6px;
}

.entry-row {
  margin-top: 16px;
}

.entry-card {
  cursor: pointer;
  margin-bottom: 16px;
}

.entry-card__body {
  display: flex;
  align-items: center;
  gap: 12px;
}

.entry-card__icon {
  color: #409eff;
}

.entry-card__title {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 4px;
}
</style>
