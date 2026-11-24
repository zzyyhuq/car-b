<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="logo">
        <div class="logo-mark">星</div>
        <div class="logo-text">
          <strong>星辰汽车美容</strong>
          <span>门店管理后台</span>
        </div>
      </div>

      <el-menu
        :default-active="route.path"
        router
        class="menu"
        background-color="#0f1f33"
        text-color="#a9b7c9"
        active-text-color="#ffffff"
      >
        <el-menu-item index="/orders">
          <el-icon><Document /></el-icon>
          <span>订单管理</span>
        </el-menu-item>
        <el-menu-item index="/services">
          <el-icon><PriceTag /></el-icon>
          <span>服务项目</span>
        </el-menu-item>
        <el-menu-item v-if="auth.isAdmin" index="/points/goods">
          <el-icon><Goods /></el-icon>
          <span>积分商品</span>
        </el-menu-item>
        <el-menu-item index="/points/exchanges">
          <el-icon><Tickets /></el-icon>
          <span>兑换核销</span>
        </el-menu-item>
        <el-menu-item v-if="auth.isAdmin" index="/messages">
          <el-icon><ChatDotRound /></el-icon>
          <span>留言管理</span>
        </el-menu-item>
        <el-menu-item v-if="auth.isAdmin" index="/users">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
      </el-menu>

      <div class="aside-foot">v0.1 · 毕业设计演示</div>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="page-title">{{ route.meta.title || '管理后台' }}</div>
        <el-dropdown trigger="click" @command="handleCommand">
          <span class="user">
            <el-avatar :size="32" class="avatar">{{ avatarText }}</el-avatar>
            <span class="user-name">{{ auth.displayName }}</span>
            <el-tag size="small" :type="auth.isAdmin ? 'danger' : 'primary'" effect="light">
              {{ roleText }}
            </el-tag>
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">
                <el-icon><SwitchButton /></el-icon>
                退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>

      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowDown, ChatDotRound, Document, Goods, PriceTag, SwitchButton, Tickets, User } from '@element-plus/icons-vue'
import { useAuthStore } from '../stores/auth'
import { ROLE_TEXT } from '../utils/format'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const roleText = computed(() => ROLE_TEXT[auth.role] || auth.role || '—')
const avatarText = computed(() => (auth.displayName || '用').slice(0, 1))

function handleCommand(command) {
  if (command === 'logout') {
    auth.clear()
    ElMessage.success('已退出登录')
    router.replace('/login')
  }
}
</script>

<style scoped>
.layout {
  height: 100%;
}

.aside {
  background: #0f1f33;
  display: flex;
  flex-direction: column;
}

.logo {
  height: 78px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 22px;
  color: #ffffff;
}

.logo-mark {
  width: 38px;
  height: 38px;
  border-radius: 12px;
  background: linear-gradient(135deg, #1677ff, #41b3ff);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: 700;
}

.logo-text {
  display: flex;
  flex-direction: column;
  line-height: 1.4;
}

.logo-text strong {
  font-size: 15px;
}

.logo-text span {
  color: #7e90a8;
  font-size: 11px;
}

.menu {
  border-right: none;
  flex: 1;
  padding-top: 8px;
}

.menu :deep(.el-menu-item) {
  margin: 4px 12px;
  border-radius: 8px;
}

.menu :deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, #1677ff, #3aa0ff);
}

.aside-foot {
  padding: 18px 22px;
  color: #52647c;
  font-size: 11px;
}

.header {
  height: 64px;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 26px;
  box-shadow: 0 1px 0 #eef1f6;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
}

.user {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  color: #1b2537;
}

.avatar {
  background: linear-gradient(135deg, #1677ff, #41b3ff);
  color: #fff;
  font-weight: 600;
}

.user-name {
  font-size: 14px;
}

.main {
  padding: 22px;
  background: #f4f6fa;
  overflow-y: auto;
}
</style>
