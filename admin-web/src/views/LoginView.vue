<template>
  <div class="login-page">
    <div class="login-card">
      <div class="brand">
        <div class="brand-mark">星</div>
        <div>
          <h1>星辰汽车美容</h1>
          <p>门店管理后台</p>
        </div>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @keyup.enter="submit">
        <el-form-item label="账号" prop="username">
          <el-input v-model="form.username" placeholder="管理员 / 员工账号" size="large" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            placeholder="登录密码"
            size="large"
          />
        </el-form-item>
        <el-button class="login-btn" type="primary" size="large" :loading="loading" @click="submit">
          登录
        </el-button>
      </el-form>

      <div class="tips">
        <div>演示账号</div>
        <div>管理员：admin / admin123</div>
        <div>门店员工：staff001 / staff123</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '../api/auth'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

const formRef = ref()
const loading = ref(false)
const form = reactive({
  username: 'admin',
  password: ''
})
const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function submit() {
  if (loading.value) return
  try {
    await formRef.value.validate()
  } catch (e) {
    return
  }

  loading.value = true
  try {
    const data = await login(form)
    if (data.role !== 'ADMIN' && data.role !== 'STAFF') {
      ElMessage.error('请使用管理员或员工账号登录')
      return
    }
    auth.setSession(data)
    ElMessage.success('欢迎回来，' + (data.nickname || data.username))
    const fallback = data.role === 'ADMIN' ? '/dashboard' : '/orders'
    router.replace(route.query.redirect || fallback)
  } catch (e) {
    // http.js 已经提示错误
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background:
    radial-gradient(circle at 15% 20%, rgba(65, 179, 255, 0.35), transparent 45%),
    radial-gradient(circle at 85% 80%, rgba(124, 77, 255, 0.28), transparent 45%),
    linear-gradient(135deg, #081a33, #0f3d78 60%, #1677ff);
  padding: 24px;
}

.login-card {
  width: 400px;
  max-width: 100%;
  background: rgba(255, 255, 255, 0.97);
  border-radius: 20px;
  padding: 40px 38px 30px;
  box-shadow: 0 24px 60px rgba(3, 20, 50, 0.35);
}

.brand {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 30px;
}

.brand-mark {
  width: 54px;
  height: 54px;
  border-radius: 16px;
  background: linear-gradient(135deg, #1677ff, #41b3ff);
  color: #fff;
  font-size: 28px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}

.brand h1 {
  margin: 0;
  font-size: 22px;
  color: #10233f;
}

.brand p {
  margin: 4px 0 0;
  color: #8a94a6;
  font-size: 13px;
}

.login-btn {
  width: 100%;
  margin-top: 8px;
  border-radius: 10px;
  background: linear-gradient(135deg, #1677ff, #3aa0ff);
  border: none;
}

.tips {
  margin-top: 24px;
  padding-top: 18px;
  border-top: 1px solid #eef1f6;
  color: #8a94a6;
  font-size: 12px;
  line-height: 1.9;
}
</style>
