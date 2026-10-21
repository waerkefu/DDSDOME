<template>
  <div class="page">
    <h2 class="page-title">我的</h2>

    <div class="card mine__profile">
      <el-avatar :size="56" :src="userInfo && userInfo.avatar">
        {{ avatarText }}
      </el-avatar>
      <div class="mine__info">
        <div class="mine__name">{{ userStore.displayName }}</div>
        <div class="mine__account">账号：{{ (userInfo && userInfo.username) || '-' }}</div>
        <div class="mine__account">角色：{{ roleText }}</div>
      </div>
    </div>

    <div class="card">
      <div
        class="mine__item"
        v-for="item in menus"
        :key="item.path"
        @click="router.push(item.path)"
      >
        <el-icon><component :is="item.icon" /></el-icon>
        <span class="mine__item-text">{{ item.text }}</span>
        <el-icon class="mine__arrow"><ArrowRight /></el-icon>
      </div>
    </div>

    <div class="card mine__logout">
      <el-button type="danger" plain size="large" class="mine__logout-btn" @click="handleLogout">
        退出登录
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowRight } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const userInfo = computed(() => userStore.userInfo)

const avatarText = computed(() => {
  const name = userStore.displayName || ''
  return name ? name.slice(0, 1) : '客'
})

const roleText = computed(() => {
  const roles = userStore.roles
  if (!roles.length) return '普通用户'
  const map = { CUSTOMER: '客户', PROVIDER: '服务商', ADMIN: '管理员' }
  return roles.map((r) => map[r] || r).join('、')
})

const menus = [
  { text: '我的订单', path: '/orders', icon: 'List' },
  { text: '去下单', path: '/home', icon: 'Guide' }
]

onMounted(() => {
  // 静默刷新最新用户信息，失败不打断页面
  userStore.fetchMe().catch(() => {})
})

function handleLogout() {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '退出',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(() => {
      userStore.logout()
      ElMessage.success('已退出登录')
      router.replace('/login')
    })
    .catch(() => {})
}
</script>

<style scoped>
.mine__profile {
  display: flex;
  align-items: center;
  gap: 14px;
}

.mine__name {
  font-size: 17px;
  font-weight: 600;
}

.mine__account {
  font-size: 12px;
  color: var(--color-text-sub);
  margin-top: 4px;
}

.mine__item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 0;
  cursor: pointer;
  color: var(--color-text);
}

.mine__item + .mine__item {
  border-top: 1px solid #eef0f3;
}

.mine__item-text {
  flex: 1;
}

.mine__arrow {
  color: var(--color-text-sub);
}

.mine__logout {
  padding: 0;
}

.mine__logout-btn {
  width: 100%;
}

.mine__logout-btn.el-button {
  border-radius: 12px;
}

.mine__logout .el-button {
  margin-left: 0;
}
</style>
