<template>
  <!-- 登录页面相关界面的样式 -->
  <div class="login-page">
    <el-card class="login-card" shadow="always">
      <div class="login-card__header">
        <h2 class="login-card__title">代驾代办平台管理后台</h2>
        <p class="login-card__desc">请使用管理员账号登录</p>
      </div>

      <!-- formRef：获取表单 DOM 实例，用来手动触发表单校验 -->
      <!-- `:model="form"` 绑定表单数据源对象(":" 传form对象本身而非字符串) 
            冒号`:` 是`v-bind:` 的缩写，表示后面是 JS 表达式（传的是`form` 对象本身），而不是字符串`"form"` 。
       -->
      <!-- `:rules="rules"` 绑定表单校验规则(script部分的代码) -->
      <!-- `label-position="top"` label 文字显示在输入框上方 
                            left：label 文字显示在输入框左侧
                            top：label 文字显示在输入框上方
                            right：label 文字显示在输入框右侧
                            bottom：label 文字显示在输入框下方
        -->
      <!-- `size="large"` 全局表单组件大尺寸 -->
      <!-- `@keyup.enter="handleSubmit"` 按下 Enter 键提交表单 
            `@` 是`v-on:` 的缩写，监听 DOM 事件
            在这个表单范围内按下 Enter 键松开 时，执行`handleSubmit` 函数
            和点「登 录」按钮效果完全一样。
        -->
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        size="large"
        @keyup.enter="handleSubmit"
      >

        <el-form-item label="用户名" prop="username">
          <!-- v-model="form.username" 双向绑定，打字实时写进 form 对象 -->
          <!-- placeholder="请输入用户名" 空值时的灰色提示文字 -->
          <!-- :prefix-icon="User" 输入框左侧显示小图标 -->
          <!-- clearable 有内容时右侧出现一键清空的图标 -->
          <el-input 
            v-model="form.username" 
            placeholder="请输入用户名" 
            :prefix-icon="User" 
            clearable 
          />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <!-- type="password" 密文显示（打字变 •••）-->
          <!-- show-password 右侧出现小眼睛，点击可切换明文/密文 -->
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码" 
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item>
          <!-- type="primary" 主题色按钮（蓝色实心） -->
          <!-- class="login-card__submit" 登录按钮样式 -->
          <!-- :loading="loading" 登录按钮点击后显示加载状态,防连点的关键 -->
          <!-- @click="handleSubmit" 点击登录按钮触发提交表单 -->
          <el-button 
              type="primary" 
              class="login-card__submit" 
              :loading="loading" 
              @click="handleSubmit">
            登 录
          </el-button>
        </el-form-item>

      </el-form>

      <!-- 登录提示信息 
       `<el-alert>` —— Element Plus 的 警告/提示条 组件
        -->
        <!-- `type="info"` 提示风格，决定颜色和图标 -->
        <!-- `:closable="false"` 不显示关闭按钮 -->
        <!-- `show-icon` 显示图标 -->
        <!-- `title="演示账号：admin / admin123"` 提示标题 -->
      <el-alert
        type="info"
        :closable="false"
        show-icon
        title="演示账号：admin / admin123"
      />
    </el-card>
  </div>
</template>

<script setup>
// reactive:把对象变成 深度响应式对象 ref:创建响应式变量
import { reactive, ref } from 'vue'
// useRoute:获取当前激活路由信息 useRouter:获取路由实例对象
import { useRoute, useRouter } from 'vue-router'
// ElMessage:消息提示组件
import { ElMessage } from 'element-plus'
// Lock:锁图标 User:用户图标
import { Lock, User } from '@element-plus/icons-vue'
// useUserStore:引入用户状态管理模块
import { useUserStore } from '@/stores/user'

//返回当前激活路由的 描述对象 ，是只读的。
const route = useRoute()
//返回 全局路由器实例 ，可以主动发起导航。
const router = useRouter()

const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: 'admin',
  password: 'admin123'
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleSubmit() {
  if (loading.value) {
    return
  }
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) {
    return
  }
  loading.value = true
  try {
    await userStore.login({ username: form.username, password: form.password })
    ElMessage.success('登录成功')
    //.query URL 查询参数对象
    const redirect = route.query.redirect
    router.replace(typeof redirect === 'string' && redirect ? redirect : '/')
  } catch (error) {
    // 错误提示已由请求拦截器统一处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1f2d3d 0%, #3a5169 100%);
}

.login-card {
  width: 420px;
  border-radius: 8px;
}

.login-card__header {
  text-align: center;
  margin-bottom: 16px;
}

.login-card__title {
  margin: 0 0 8px;
  font-size: 20px;
  color: #303133;
}

.login-card__desc {
  margin: 0;
  font-size: 13px;
  color: #909399;
}

.login-card__submit {
  width: 100%;
}
</style>
