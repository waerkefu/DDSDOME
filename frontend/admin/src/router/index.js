import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: () => import('@/layout/index.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'system/user',
        name: 'SystemUser',
        component: () => import('@/views/system/user/index.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'system/provider',
        name: 'SystemProvider',
        component: () => import('@/views/system/provider/index.vue'),
        meta: { title: '服务商管理' }
      },
      {
        path: 'system/role',
        name: 'SystemRole',
        component: () => import('@/views/system/role/index.vue'),
        meta: { title: '角色权限' }
      },
      {
        path: 'order',
        name: 'OrderList',
        component: () => import('@/views/order/index.vue'),
        meta: { title: '订单管理' }
      },
      {
        path: 'pricing',
        name: 'PricingRule',
        component: () => import('@/views/pricing/index.vue'),
        meta: { title: '计价规则' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to) => {
  const userStore = useUserStore()

  // 未登录：仅放行登录页
  if (!userStore.token) {
    if (to.path === '/login') {
      return true
    }
    return { path: '/login', query: { redirect: to.fullPath } }
  }

  // 已登录访问登录页：跳转首页
  if (to.path === '/login') {
    return { path: '/' }
  }

  // 进入页面前拉取权限（用于动态菜单）
  if (userStore.permissions.length === 0) {
    try {
      await userStore.fetchMe()
    } catch (error) {
      userStore.logout()
      return { path: '/login', query: { redirect: to.fullPath } }
    }
  }

  return true
})

router.afterEach((to) => {
  document.title = to.meta && to.meta.title
    ? `${to.meta.title} - 代驾代办平台管理后台`
    : '代驾代办平台管理后台'
})

export default router
