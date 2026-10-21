<template>
  <div class="login page page--no-tab">
    <div class="login__brand">
      <div class="login__logo">代</div>
      <h1 class="login__title">代驾代办</h1>
      <p class="login__slogan">一键下单，代驾与车辆代办更省心</p>
    </div>

    <div class="card">
      <el-radio-group v-model="mode" class="login__mode">
        <el-radio-button value="login">登录</el-radio-button>
        <el-radio-button value="register">注册</el-radio-button>
      </el-radio-group>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        size="large"
        @submit.prevent
      >
        <el-form-item label="账号" prop="username">
          <el-input v-model="form.username" placeholder="请输入账号" clearable :prefix-icon="User" />
        </el-form-item>

        <el-form-item v-if="isRegister" label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" clearable :prefix-icon="Iphone" />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            placeholder="请输入密码"
            :prefix-icon="Lock"
          />
        </el-form-item>

        <el-form-item v-if="isRegister" label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="请输入昵称（选填）" clearable />
        </el-form-item>

        <el-button
          type="primary"
          class="login__submit"
          size="large"
          :loading="loading"
          @click="handleSubmit"
        >
          {{ isRegister ? '注册并登录' : '登录' }}
        </el-button>
      </el-form>
    </div>

    <div class="login__tip">演示账号：customer1 / admin123</div>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Iphone } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const mode = ref('login')
const isRegister = computed(() => mode.value === 'register')

const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  phone: '',
  password: '',
  nickname: ''
})

const rules = computed(() => ({
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ]
}))

async function handleSubmit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch (e) {
    return
  }

  loading.value = true
  try {
    if (isRegister.value) {
      const payload = {
        username: form.username,
        phone: form.phone,
        password: form.password
      }
      if (form.nickname) payload.nickname = form.nickname
      await userStore.register(payload)
      ElMessage.success('注册成功')
    } else {
      await userStore.login({ username: form.username, password: form.password })
      ElMessage.success('登录成功')
    }
    const redirect = route.query.redirect
    router.replace(typeof redirect === 'string' && redirect ? redirect : '/home')
  } catch (e) {
    // 错误提示已在响应拦截器中统一处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login {
  padding-top: 48px;
}

.login__brand {
  text-align: center;
  margin-bottom: 24px;
}

.login__logo {
  width: 64px;
  height: 64px;
  margin: 0 auto 12px;
  border-radius: 18px;
  background: linear-gradient(135deg, #3b6ef0, #6f9bff);
  color: #fff;
  font-size: 30px;
  font-weight: 700;
  line-height: 64px;
}

.login__title {
  margin: 0;
  font-size: 22px;
}

.login__slogan {
  margin: 6px 0 0;
  font-size: 13px;
  color: var(--color-text-sub);
}

.login__mode {
  display: flex;
  margin-bottom: 16px;
}

.login__mode :deep(.el-radio-button) {
  flex: 1;
}

.login__mode :deep(.el-radio-button__inner) {
  width: 100%;
}

.login__submit {
  width: 100%;
  margin-top: 4px;
}

.login__tip {
  text-align: center;
  font-size: 12px;
  color: var(--color-text-sub);
}
</style>
