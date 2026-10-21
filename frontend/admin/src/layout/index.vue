<template>
  <el-container class="layout">
    <el-aside width="220px" class="layout-aside">
      <div class="layout-logo">
        <span class="layout-logo__text">代驾代办平台</span>
        <span class="layout-logo__sub">管理后台</span>
      </div>
      <el-scrollbar>
        <el-menu
          :default-active="activeMenu"
          router
          background-color="#1f2d3d"
          text-color="#c0c4cc"
          active-text-color="#ffd04b"
        >
          <el-menu-item v-for="item in visibleMenus" :key="item.path" :index="item.path">
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.title }}</span>
          </el-menu-item>
        </el-menu>
      </el-scrollbar>
    </el-aside>

    <el-container>
      <el-header class="layout-header">
        <div class="layout-header__title">{{ currentTitle }}</div>
        <el-dropdown @command="handleCommand">
          <span class="layout-user">
            <el-avatar :size="28" :src="userStore.avatar">
              {{ userStore.displayName.charAt(0) }}
            </el-avatar>
            <span class="layout-user__name">{{ userStore.displayName }}</span>
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item disabled>角色：{{ roleText }}</el-dropdown-item>
              <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>

      <el-main class="layout-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const menus = [
  { path: '/dashboard', title: '首页', icon: 'HomeFilled' },
  { path: '/system/user', title: '用户管理', icon: 'User', perm: 'system:user' },
  { path: '/system/provider', title: '服务商管理', icon: 'Avatar', perm: 'system:provider' },
  { path: '/system/role', title: '角色权限', icon: 'Lock', perm: 'system:role' },
  { path: '/order', title: '订单管理', icon: 'Tickets', perm: 'order:list' },
  { path: '/pricing', title: '计价规则', icon: 'Money', perm: 'pricing:rule' }
]

// 按当前用户的权限编码过滤菜单
const visibleMenus = computed(() =>
  menus.filter((item) => !item.perm || userStore.permissions.includes(item.perm))
)

const activeMenu = computed(() => route.path)
const currentTitle = computed(() => (route.meta && route.meta.title) || '')
const roleText = computed(() => (userStore.roles.length ? userStore.roles.join('、') : '无'))

async function handleCommand(command) {
  if (command !== 'logout') {
    return
  }
  try {
    await ElMessageBox.confirm('确认退出登录吗？', '提示', {
      type: 'warning',
      confirmButtonText: '确认退出',
      cancelButtonText: '取消'
    })
  } catch (error) {
    return
  }
  userStore.logout()
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>

<style scoped>
.layout {
  height: 100%;
}

.layout-aside {
  background-color: #1f2d3d;
  overflow: hidden;
}

.layout-logo {
  height: 60px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.layout-logo__text {
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 1px;
}

.layout-logo__sub {
  font-size: 12px;
  color: #909399;
}

.layout-aside :deep(.el-menu) {
  border-right: none;
}

.layout-header {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}

.layout-header__title {
  font-size: 16px;
  font-weight: 600;
}

.layout-user {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  outline: none;
}

.layout-user__name {
  font-size: 14px;
  color: #303133;
}

.layout-main {
  padding: 0;
  background-color: #f0f2f5;
}
</style>
