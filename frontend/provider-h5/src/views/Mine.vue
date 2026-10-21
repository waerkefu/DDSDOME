<template>
  <div class="page">
    <div class="mine-header">
      <div class="mine-avatar">{{ avatarText }}</div>
      <div>
        <div class="mine-name">{{ profile?.realName || userStore.displayName }}</div>
        <div class="mine-sub">
          {{ userStore.userInfo?.username || '-' }} · 服务商
        </div>
      </div>
    </div>

    <el-card class="card" shadow="never" header="接单设置">
      <div v-loading="loading" style="min-height: 80px">
        <div class="switch-row">
          <div>
            <div class="switch-label">在线状态</div>
            <div class="switch-desc">下线后不会收到新的订单推送</div>
          </div>
          <el-switch
            v-model="onlineValue"
            :loading="onlineLoading"
            active-text="在线"
            inactive-text="离线"
            @change="handleOnlineChange"
          />
        </div>
        <div class="switch-row">
          <div>
            <div class="switch-label">接单开关</div>
            <div class="switch-desc">开启后可在接单大厅抢单</div>
          </div>
          <el-switch
            v-model="acceptValue"
            :loading="acceptLoading"
            active-text="开启"
            inactive-text="关闭"
            @change="handleAcceptChange"
          />
        </div>
      </div>
    </el-card>

    <el-card class="card" shadow="never" header="服务商档案">
      <div v-loading="loading" style="min-height: 120px">
        <template v-if="profile">
          <div class="detail-row">
            <span class="label">服务商编号</span>
            <span class="value">{{ profile.providerNo || '-' }}</span>
          </div>
          <div class="detail-row">
            <span class="label">姓名</span>
            <span class="value">{{ profile.realName || '-' }}</span>
          </div>
          <div class="detail-row">
            <span class="label">手机号</span>
            <span class="value">{{ profile.phone || '-' }}</span>
          </div>
          <div class="detail-row">
            <span class="label">城市</span>
            <span class="value">{{ profile.cityCode || '-' }}</span>
          </div>
          <div class="detail-row">
            <span class="label">车牌号</span>
            <span class="value">{{ profile.vehiclePlate || '-' }}</span>
          </div>
          <div class="detail-row">
            <span class="label">车型</span>
            <span class="value">{{ profile.vehicleModel || '-' }}</span>
          </div>
          <div class="detail-row">
            <span class="label">服务类型</span>
            <span class="value">{{ serviceTypeText }}</span>
          </div>
          <div class="detail-row">
            <span class="label">评分</span>
            <span class="value">{{ ratingText }}</span>
          </div>
          <div class="detail-row">
            <span class="label">累计订单数</span>
            <span class="value">{{ profile.orderCount ?? 0 }}</span>
          </div>
        </template>
        <el-empty v-else-if="!loading" description="未获取到服务商档案" :image-size="60" />
      </div>
    </el-card>

    <el-button type="danger" plain style="width: 100%; margin-bottom: 12px" @click="handleLogout">
      退出登录
    </el-button>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getProfile, updateAcceptEnabled, updateOnlineStatus } from '@/api/provider'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const profile = ref(null)
const loading = ref(false)
const onlineValue = ref(false)
const acceptValue = ref(false)
const onlineLoading = ref(false)
const acceptLoading = ref(false)

const avatarText = computed(() => {
  const name = profile.value?.realName || userStore.displayName || '服'
  return String(name).slice(0, 1)
})

const serviceTypeText = computed(() => {
  const list = profile.value?.serviceTypeList
  if (Array.isArray(list) && list.length) return list.join('、')
  return profile.value?.serviceTypes || '-'
})

const ratingText = computed(() => {
  const rating = profile.value?.rating
  if (rating === null || rating === undefined || rating === '') return '-'
  return `${Number(rating).toFixed(1)} 分`
})

function syncSwitch() {
  onlineValue.value = profile.value?.onlineStatus === 1
  acceptValue.value = profile.value?.acceptEnabled === 1
}

async function loadProfile() {
  loading.value = true
  try {
    profile.value = await getProfile()
    syncSwitch()
  } catch (e) {
    // 错误提示已在请求拦截器中统一处理
  } finally {
    loading.value = false
  }
}

async function handleOnlineChange(val) {
  onlineLoading.value = true
  try {
    await updateOnlineStatus(val ? 1 : 0)
    ElMessage.success(val ? '已上线' : '已下线')
    await loadProfile()
  } catch (e) {
    // 失败回滚开关状态
    onlineValue.value = !val
  } finally {
    onlineLoading.value = false
  }
}

async function handleAcceptChange(val) {
  acceptLoading.value = true
  try {
    await updateAcceptEnabled(val ? 1 : 0)
    ElMessage.success(val ? '已开启接单' : '已关闭接单')
    await loadProfile()
  } catch (e) {
    // 失败回滚开关状态
    acceptValue.value = !val
  } finally {
    acceptLoading.value = false
  }
}

async function handleLogout() {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      type: 'warning',
      confirmButtonText: '退出',
      cancelButtonText: '取消'
    })
  } catch (e) {
    return
  }
  userStore.logout()
  ElMessage.success('已退出登录')
  router.replace('/login')
}

onMounted(() => {
  loadProfile()
})
</script>
