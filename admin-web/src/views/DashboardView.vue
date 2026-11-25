<template>
  <div class="dashboard">
    <div class="hero">
      <div>
        <h2>数据概览</h2>
        <p>{{ today }} · 欢迎回来，{{ auth.displayName }}</p>
      </div>
      <el-button :icon="Refresh" :loading="loading" @click="load">刷新数据</el-button>
    </div>

    <div class="stat-grid">
      <div v-for="card in cards" :key="card.label" class="stat-card">
        <div class="stat-icon" :style="{ background: card.bg, color: card.color }">
          <el-icon :size="22"><component :is="card.icon" /></el-icon>
        </div>
        <div class="stat-body">
          <div class="stat-label">{{ card.label }}</div>
          <div class="stat-value">{{ card.value }}</div>
        </div>
      </div>
    </div>

    <div class="panel-grid">
      <div class="page-card">
        <div class="panel-head">
          <h3>最近订单</h3>
          <el-button link type="primary" @click="goOrders">查看全部 ›</el-button>
        </div>
        <el-table :data="recentOrders" size="small" stripe>
          <el-table-column prop="orderNo" label="订单号" width="175" show-overflow-tooltip />
          <el-table-column prop="serviceName" label="服务" width="95" show-overflow-tooltip />
          <el-table-column prop="plateNo" label="车牌" width="95" />
          <el-table-column label="状态" width="85" align="center">
            <template #default="{ row }">
              <el-tag :type="ORDER_STATUS_TYPE[row.status] || 'info'" size="small" effect="light">
                {{ row.statusText || ORDER_STATUS_TEXT[row.status] || row.status }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="金额" width="85" align="right">
            <template #default="{ row }">
              <span class="money">¥{{ formatMoney(row.amount) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="下单时间" width="135">
            <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
          </el-table-column>
        </el-table>
      </div>

      <div class="page-card">
        <div class="panel-head">
          <h3>运营数据</h3>
        </div>
        <div class="mini-row" v-for="item in operations" :key="item.label">
          <span>{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
        </div>
        <div class="quick-actions">
          <el-button type="primary" plain @click="goOrders">订单管理</el-button>
          <el-button type="warning" plain @click="goExchanges">兑换核销</el-button>
          <el-button type="danger" plain @click="goMessages">留言管理</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  Calendar,
  ChatDotRound,
  DataLine,
  Money,
  Odometer,
  Refresh,
  ShoppingCart,
  Tickets,
  User
} from '@element-plus/icons-vue'
import { getOverview } from '../api/stats'
import { pageOrders } from '../api/order'
import { useAuthStore } from '../stores/auth'
import {
  formatDateTime,
  formatMoney,
  ORDER_STATUS_TEXT,
  ORDER_STATUS_TYPE
} from '../utils/format'

const router = useRouter()
const auth = useAuthStore()

const loading = ref(false)
const stats = ref({})
const recentOrders = ref([])

const today = new Date().toLocaleDateString('zh-CN', {
  year: 'numeric',
  month: 'long',
  day: 'numeric',
  weekday: 'long'
})

const cards = computed(() => [
  {
    label: '今日订单',
    value: stats.value.todayOrderCount || 0,
    icon: ShoppingCart,
    bg: '#eaf2ff',
    color: '#1677ff'
  },
  {
    label: '待接单',
    value: stats.value.pendingOrderCount || 0,
    icon: Odometer,
    bg: '#fff4e5',
    color: '#ff8f1f'
  },
  {
    label: '累计营收',
    value: '¥' + formatMoney(stats.value.totalRevenue),
    icon: Money,
    bg: '#eaf8ef',
    color: '#00a870'
  },
  {
    label: '已完成订单',
    value: stats.value.completedOrderCount || 0,
    icon: DataLine,
    bg: '#f1ecff',
    color: '#7c4dff'
  },
  {
    label: '车主数',
    value: stats.value.ownerCount || 0,
    icon: User,
    bg: '#eaf2ff',
    color: '#1677ff'
  },
  {
    label: '待核销兑换',
    value: stats.value.pendingExchangeCount || 0,
    icon: Tickets,
    bg: '#fff4e5',
    color: '#ff8f1f'
  },
  {
    label: '待审核留言',
    value: stats.value.pendingMessageCount || 0,
    icon: ChatDotRound,
    bg: '#fff0f0',
    color: '#e34d59'
  },
  {
    label: '活动报名',
    value: stats.value.signupCount || 0,
    icon: Calendar,
    bg: '#f1ecff',
    color: '#7c4dff'
  }
])

const operations = computed(() => [
  { label: '用户总数', value: stats.value.userCount || 0 },
  { label: '门店员工', value: stats.value.staffCount || 0 },
  { label: '订单总数', value: stats.value.orderCount || 0 },
  { label: '已发布公告', value: stats.value.noticeCount || 0 },
  { label: '进行中活动', value: stats.value.activityCount || 0 },
  { label: '活动报名人次', value: stats.value.signupCount || 0 }
])

async function load() {
  loading.value = true
  try {
    const [overview, orderPage] = await Promise.all([
      getOverview().catch(() => null),
      pageOrders({ page: 1, size: 5 }).catch(() => null)
    ])
    if (overview) {
      stats.value = overview
    }
    recentOrders.value = (orderPage && orderPage.records) || []
  } finally {
    loading.value = false
  }
}

function goOrders() {
  router.push('/orders')
}

function goExchanges() {
  router.push('/points/exchanges')
}

function goMessages() {
  router.push('/messages')
}

onMounted(load)
</script>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 26px 28px;
  border-radius: 12px;
  background:
    radial-gradient(circle at 88% 20%, rgba(65, 179, 255, 0.35), transparent 42%),
    linear-gradient(135deg, #0f3d78, #1677ff 65%, #3aa0ff);
  color: #ffffff;
  box-shadow: 0 12px 30px rgba(22, 119, 255, 0.22);
}

.hero h2 {
  margin: 0;
  font-size: 24px;
}

.hero p {
  margin: 8px 0 0;
  font-size: 13px;
  opacity: 0.85;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 20px;
  border-radius: 10px;
  background: #ffffff;
  box-shadow: 0 2px 10px rgba(23, 43, 77, 0.05);
}

.stat-icon {
  width: 46px;
  height: 46px;
  flex-shrink: 0;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-label {
  color: #8a94a6;
  font-size: 12px;
}

.stat-value {
  margin-top: 4px;
  font-size: 24px;
  font-weight: 700;
  color: #10233f;
}

.panel-grid {
  display: grid;
  grid-template-columns: 1.65fr 1fr;
  gap: 20px;
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

.panel-head h3 {
  margin: 0;
  font-size: 16px;
}

.mini-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 11px 0;
  border-bottom: 1px solid #f3f5f8;
  color: #6b7688;
  font-size: 13px;
}

.mini-row strong {
  color: #10233f;
  font-size: 15px;
}

.quick-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 18px;
}

@media (max-width: 1100px) {
  .stat-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .panel-grid {
    grid-template-columns: 1fr;
  }
}
</style>
