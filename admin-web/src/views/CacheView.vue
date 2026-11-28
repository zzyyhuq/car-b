<template>
  <div class="cache-page">
    <div class="page-card">
      <div class="page-toolbar-title">
        <h3>缓存管理</h3>
        <div class="actions">
          <el-button :icon="Refresh" :loading="loading" @click="loadAll">刷新</el-button>
          <el-button
            type="danger"
            plain
            :icon="Delete"
            :disabled="!overview.connected"
            :loading="clearing"
            @click="handleClear"
          >清空业务缓存</el-button>
        </div>
      </div>

      <el-alert
        v-if="!loading && !overview.connected"
        class="alert"
        type="warning"
        :closable="false"
        show-icon
        title="Redis 未连接"
        :description="overview.message || '请先启动 Redis；业务会自动降级为直接查库，不影响正常使用。'"
      />

      <div class="stat-grid">
        <div class="stat-card">
          <span class="stat-label">连接状态</span>
          <el-tag :type="overview.connected ? 'success' : 'danger'" effect="light">
            {{ overview.connected ? '已连接' : '未连接' }}
          </el-tag>
        </div>
        <div class="stat-card">
          <span class="stat-label">Redis 版本</span>
          <strong>{{ overview.version || '—' }}</strong>
        </div>
        <div class="stat-card">
          <span class="stat-label">已用内存</span>
          <strong>{{ overview.usedMemory || '—' }}</strong>
        </div>
        <div class="stat-card">
          <span class="stat-label">Key 总数</span>
          <strong>{{ overview.connected ? overview.keyCount : '—' }}</strong>
        </div>
        <div class="stat-card">
          <span class="stat-label">缓存命中率</span>
          <strong>{{ hitRateText }}</strong>
        </div>
        <div class="stat-card">
          <span class="stat-label">运行时长</span>
          <strong>{{ uptimeText }}</strong>
        </div>
      </div>
    </div>

    <div class="page-card">
      <div class="toolbar">
        <el-input
          v-model="prefix"
          placeholder="按前缀筛选，如 notice / beauty:"
          clearable
          style="width: 280px"
          @keyup.enter="loadKeys"
        />
        <el-button type="primary" @click="loadKeys">查询</el-button>
        <el-button @click="resetPrefix">重置</el-button>
        <span class="hint muted">只展示 beauty: 前缀下的业务缓存</span>
      </div>

      <el-table
        v-loading="keyLoading"
        :data="rows"
        border
        stripe
        :empty-text="overview.connected ? '暂无缓存，访问小程序首页/服务列表后会自动生成' : 'Redis 未连接'"
      >
        <el-table-column prop="key" label="缓存 Key" min-width="300" show-overflow-tooltip />
        <el-table-column label="类型" width="90" align="center">
          <template #default="{ row }">
            <el-tag size="small" effect="plain">{{ row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="剩余有效期" width="140" align="center">
          <template #default="{ row }">{{ ttlText(row.ttl) }}</template>
        </el-table-column>
        <el-table-column prop="description" label="说明" min-width="180" />
        <el-table-column label="操作" width="90" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Refresh } from '@element-plus/icons-vue'
import {
  clearBusinessCache,
  deleteCacheKey,
  getCacheOverview,
  listCacheKeys
} from '../api/cache'

const overview = ref({})
const rows = ref([])
const prefix = ref('')
const loading = ref(false)
const keyLoading = ref(false)
const clearing = ref(false)

const hitRateText = computed(() => {
  if (!overview.value.connected) return '—'
  return ((overview.value.hitRate || 0) * 100).toFixed(1) + '%'
})

const uptimeText = computed(() => {
  const seconds = overview.value.uptimeSeconds || 0
  if (!seconds) return '—'
  const days = Math.floor(seconds / 86400)
  const hours = Math.floor((seconds % 86400) / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  if (days > 0) return `${days} 天 ${hours} 小时`
  if (hours > 0) return `${hours} 小时 ${minutes} 分`
  return `${minutes} 分钟`
})

function ttlText(ttl) {
  if (ttl === -1) return '永不过期'
  if (ttl < 0) return '已失效'
  if (ttl >= 60) return `${Math.floor(ttl / 60)} 分 ${ttl % 60} 秒`
  return `${ttl} 秒`
}

async function loadOverview() {
  try {
    overview.value = await getCacheOverview()
  } catch (e) {
    overview.value = { connected: false, message: '获取缓存状态失败' }
  }
}

async function loadKeys() {
  if (!overview.value.connected) {
    rows.value = []
    return
  }
  keyLoading.value = true
  try {
    rows.value = (await listCacheKeys({
      prefix: prefix.value || undefined,
      limit: 200
    })) || []
  } catch (e) {
    rows.value = []
  } finally {
    keyLoading.value = false
  }
}

async function loadAll() {
  loading.value = true
  try {
    await loadOverview()
    await loadKeys()
  } finally {
    loading.value = false
  }
}

function resetPrefix() {
  prefix.value = ''
  loadKeys()
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
      `删除「${row.key}」后，下次访问会重新查库。`,
      '删除缓存',
      { type: 'warning' }
    )
    await deleteCacheKey(row.key)
    ElMessage.success('已删除')
    await loadKeys()
  } catch (e) {
    // 用户取消或接口已提示
  }
}

async function handleClear() {
  try {
    await ElMessageBox.confirm(
      '将删除所有 beauty: 前缀的业务缓存，下次访问会重新查库。',
      '清空业务缓存',
      { type: 'warning' }
    )
  } catch (e) {
    return
  }
  clearing.value = true
  try {
    const count = await clearBusinessCache()
    ElMessage.success(`已清理 ${count} 个缓存`)
    await loadAll()
  } catch (e) {
    // http.js 已提示
  } finally {
    clearing.value = false
  }
}

onMounted(loadAll)
</script>

<style scoped>
.cache-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.actions {
  display: flex;
  gap: 10px;
}

.alert {
  margin-bottom: 18px;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 14px;
}

.stat-card {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 16px 18px;
  border-radius: 10px;
  background: #f8fafc;
  border: 1px solid #eef1f6;
}

.stat-label {
  color: #8a94a6;
  font-size: 12px;
}

.stat-card strong {
  color: #10233f;
  font-size: 18px;
}

.hint {
  align-self: center;
  font-size: 12px;
}

@media (max-width: 1200px) {
  .stat-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}
</style>
