<template>
  <div class="page">
    <h2 class="page-title">代驾下单</h2>

    <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent>
      <!-- 服务类型 -->
      <div class="card">
        <div class="card__title">服务类型</div>
        <div class="type-grid">
          <div
            v-for="item in orderTypes"
            :key="item.value"
            class="type-item"
            :class="{ 'is-active': form.orderType === item.value }"
            @click="onOrderTypeChange(item.value)"
          >
            <el-icon class="type-item__icon"><component :is="item.icon" /></el-icon>
            <div class="type-item__name">{{ item.label }}</div>
            <div class="type-item__desc">{{ item.desc }}</div>
          </div>
        </div>
      </div>

      <!-- 行程信息 -->
      <div class="card">
        <div class="card__title">行程信息</div>
        <el-form-item label="起点地址" prop="startAddress">
          <el-input
            v-model="form.startAddress"
            placeholder="请输入起点地址（如：人民广场）"
            clearable
            @input="resetEstimate"
          />
        </el-form-item>

        <el-form-item label="终点地址" prop="endAddress">
          <el-input
            v-model="form.endAddress"
            placeholder="请输入终点地址（如：虹桥火车站）"
            clearable
            @input="resetEstimate"
          />
        </el-form-item>

        <el-form-item v-if="isBookingType" label="预约时间" prop="bookingTime">
          <el-date-picker
            v-model="form.bookingTime"
            type="datetime"
            placeholder="请选择预约时间"
            value-format="YYYY-MM-DDTHH:mm:ss"
            format="YYYY-MM-DD HH:mm"
            :disabled-date="disablePastDate"
            style="width: 100%"
            @change="resetEstimate"
          />
        </el-form-item>

        <el-alert
          v-if="tripPlan"
          :title="`预估里程 ${(tripPlan.distanceM / 1000).toFixed(1)} 公里 · 约 ${Math.round(
            tripPlan.durationS / 60
          )} 分钟（模拟值，非真实导航结果）`"
          type="info"
          :closable="false"
          show-icon
        />
      </div>

      <!-- 车辆与联系人 -->
      <div class="card">
        <div class="card__title">车辆与联系人</div>
        <el-form-item label="车牌号" prop="vehiclePlate">
          <el-input v-model="form.vehiclePlate" placeholder="如：沪A12345" clearable />
        </el-form-item>

        <el-form-item label="车型" prop="vehicleType">
          <el-select
            v-model="form.vehicleType"
            placeholder="请选择车型"
            style="width: 100%"
            @change="resetEstimate"
          >
            <el-option
              v-for="item in vehicleTypes"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="联系人" prop="contactName">
          <el-input v-model="form.contactName" placeholder="请输入联系人姓名" clearable />
        </el-form-item>

        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="form.contactPhone" placeholder="请输入联系人手机号" clearable />
        </el-form-item>

        <el-form-item label="备注">
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="2"
            maxlength="100"
            show-word-limit
            placeholder="选填，如车辆位置说明等"
          />
        </el-form-item>
      </div>
    </el-form>

    <!-- 预估结果 -->
    <div v-if="estimate" class="card estimate">
      <div class="card__title">
        <span>预估费用</span>
        <span class="amount estimate__total">{{ formatYuan(estimate.totalAmount) }}</span>
      </div>
      <div class="row">
        <span class="row__label">起步价</span>
        <span class="row__value">{{ formatYuan(estimate.basePrice) }}</span>
      </div>
      <div class="row">
        <span class="row__label">里程费</span>
        <span class="row__value">{{ formatYuan(estimate.distanceFee) }}</span>
      </div>
      <div class="row">
        <span class="row__label">时长费</span>
        <span class="row__value">{{ formatYuan(estimate.durationFee) }}</span>
      </div>
      <div class="row">
        <span class="row__label">最低消费</span>
        <span class="row__value">{{ formatYuan(estimate.minPrice) }}</span>
      </div>
      <div class="row">
        <span class="row__label">夜间系数</span>
        <span class="row__value">{{ estimate.nightRatio }}</span>
      </div>
      <div class="row">
        <span class="row__label">预约系数</span>
        <span class="row__value">{{ estimate.bookingRatio }}</span>
      </div>
    </div>

    <div class="home__placeholder"></div>

    <!-- 底部操作栏 -->
    <div class="fixed-bottom-bar">
      <el-button size="large" :loading="estimating" @click="handleEstimate">预估费用</el-button>
      <el-button
        type="primary"
        size="large"
        class="fixed-bottom-bar__main"
        :loading="submitting"
        @click="handleCreate"
      >
        立即下单
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { estimateOrder, createOrder } from '@/api/order'
import { formatYuan, needBookingTime, randomKey } from '@/utils/format'

const router = useRouter()

/** 城市编码：与后端初始化的计价规则保持一致（上海市） */
const CITY_CODE = '310100'

const orderTypes = [
  { value: 'IMMEDIATE', label: '即时代驾', desc: '马上出发', icon: 'Promotion' },
  { value: 'SCHEDULED', label: '预约代驾', desc: '约好时间', icon: 'Calendar' },
  { value: 'INSPECTION', label: '年审代办', desc: '代跑年审', icon: 'DocumentChecked' },
  { value: 'RENEWAL', label: '换证代办', desc: '代跑换证', icon: 'Postcard' }
]

const vehicleTypes = [
  { value: 'COMFORT', label: '舒适型' },
  { value: 'BUSINESS', label: '商务型' }
]

const form = reactive({
  orderType: 'IMMEDIATE',
  cityCode: CITY_CODE,
  vehicleType: 'COMFORT',
  vehiclePlate: '',
  contactName: '',
  contactPhone: '',
  startAddress: '',
  endAddress: '',
  bookingTime: '',
  remark: ''
})

const isBookingType = computed(() => needBookingTime(form.orderType))

const rules = computed(() => ({
  startAddress: [{ required: true, message: '请输入起点地址', trigger: 'blur' }],
  endAddress: [
    { required: true, message: '请输入终点地址', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value && form.startAddress && value.trim() === form.startAddress.trim()) {
          callback(new Error('起点与终点不能相同'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  bookingTime: isBookingType.value
    ? [
        {
          validator: (rule, value, callback) => {
            if (!value) {
              callback(new Error('请选择预约时间'))
            } else if (new Date(value).getTime() <= Date.now()) {
              callback(new Error('预约时间必须晚于当前时间'))
            } else {
              callback()
            }
          },
          trigger: 'change'
        }
      ]
    : [],
  vehiclePlate: [{ required: true, message: '请输入车牌号', trigger: 'blur' }],
  vehicleType: [{ required: true, message: '请选择车型', trigger: 'change' }],
  contactName: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  contactPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ]
}))

const formRef = ref(null)
const estimating = ref(false)
const submitting = ref(false)
const estimate = ref(null)
const tripPlan = ref(null)

/**
 * 模拟行程计算（无地图能力）。
 * 说明：当前客户端未接入地图与路径规划服务，此处仅依据起终点文本长度生成
 *       1000~20000 米的「模拟里程」，并按 8 米/秒（约 28.8 km/h）估算「模拟时长」。
 *       结果仅用于演示，不代表真实距离与耗时。
 */
function mockTripPlan(startAddress, endAddress) {
  const startLen = (startAddress || '').trim().length
  const endLen = (endAddress || '').trim().length
  // 用文本长度生成稳定的伪随机种子，范围 0~99
  const seed = (startLen * 137 + endLen * 911) % 100
  const distanceM = 1000 + seed * 190 // 1000 ~ 19810 米
  const durationS = Math.max(300, Math.round(distanceM / 8)) // 按 8 米/秒换算
  return { distanceM, durationS }
}

function disablePastDate(date) {
  return date.getTime() < Date.now() - 24 * 60 * 60 * 1000
}

function onOrderTypeChange(value) {
  form.orderType = value
  if (!needBookingTime(value)) {
    form.bookingTime = ''
  }
  resetEstimate()
}

function resetEstimate() {
  estimate.value = null
  tripPlan.value = null
}

/** 校验表单并生成模拟行程数据 */
async function validateAndPlan() {
  if (!formRef.value) return null
  try {
    await formRef.value.validate()
  } catch (e) {
    return null
  }
  if (form.startAddress.trim() === form.endAddress.trim()) {
    ElMessage.warning('起点与终点不能相同')
    return null
  }
  const plan = mockTripPlan(form.startAddress, form.endAddress)
  tripPlan.value = plan
  return plan
}

/** 组装计价/下单请求体 */
function buildPayload(plan) {
  const payload = {
    orderType: form.orderType,
    cityCode: form.cityCode,
    vehicleType: form.vehicleType,
    distanceM: plan.distanceM,
    durationS: plan.durationS
  }
  if (isBookingType.value && form.bookingTime) {
    payload.bookingTime = form.bookingTime
  }
  return payload
}

async function handleEstimate() {
  const plan = await validateAndPlan()
  if (!plan) return
  estimating.value = true
  try {
    estimate.value = await estimateOrder(buildPayload(plan))
    ElMessage.success('预估成功')
  } catch (e) {
    estimate.value = null
  } finally {
    estimating.value = false
  }
}

async function handleCreate() {
  const plan = await validateAndPlan()
  if (!plan) return
  if (!estimate.value) {
    ElMessage.warning('请先点击「预估费用」')
    return
  }
  submitting.value = true
  try {
    const payload = {
      ...buildPayload(plan),
      vehiclePlate: form.vehiclePlate.trim(),
      contactName: form.contactName.trim(),
      contactPhone: form.contactPhone.trim(),
      startAddress: form.startAddress.trim(),
      startLongitude: null,
      startLatitude: null,
      endAddress: form.endAddress.trim(),
      endLongitude: null,
      endLatitude: null
    }
    if (form.remark) payload.remark = form.remark
    // 幂等键：防止重复提交生成多笔订单
    const order = await createOrder(payload, randomKey('order'))
    ElMessage.success('下单成功，等待接单')
    router.replace(`/orders/${order.orderNo}`)
  } catch (e) {
    // 错误提示已统一处理
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.type-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.type-item {
  border: 1px solid #e6e8eb;
  border-radius: 10px;
  padding: 10px;
  cursor: pointer;
  transition: all 0.2s;
}

.type-item.is-active {
  border-color: var(--color-primary);
  background: #f2f6ff;
}

.type-item__icon {
  font-size: 18px;
  color: var(--color-primary);
}

.type-item__name {
  font-weight: 600;
  margin-top: 4px;
}

.type-item__desc {
  font-size: 12px;
  color: var(--color-text-sub);
}

.estimate__total {
  font-size: 20px;
}

.home__placeholder {
  /* 底部操作栏（按钮 + padding + 安全区）的实际高度，避免内容被按钮栏遮挡 */
  height: calc(64px + env(safe-area-inset-bottom));
}

.fixed-bottom-bar__main {
  flex: 1;
}
</style>
