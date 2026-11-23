<template>
  <div class="page-card">
    <div class="page-toolbar-title">
      <h3>兑换核销</h3>
      <el-button :icon="Refresh" @click="load">刷新</el-button>
    </div>

    <div class="toolbar">
      <el-input
        v-model="query.exchangeNo"
        placeholder="兑换单号"
        clearable
        style="width: 220px"
        @keyup.enter="search"
      />
      <el-select v-model="query.status" placeholder="状态" clearable>
        <el-option
          v-for="item in statusOptions"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>
      <el-button type="primary" @click="search">查询</el-button>
      <el-button @click="reset">重置</el-button>
    </div>

    <el-table v-loading="loading" :data="rows" border stripe>
      <el-table-column prop="exchangeNo" label="兑换单号" width="210" show-overflow-tooltip />
      <el-table-column label="车主" width="120">
        <template #default="{ row }">{{ row.userName || '—' }}</template>
      </el-table-column>
      <el-table-column prop="goodsName" label="兑换商品" min-width="160" show-overflow-tooltip />
      <el-table-column label="消耗积分" width="100" align="right">
        <template #default="{ row }">
          <span class="money">{{ row.pointsCost }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" effect="light">
            {{ row.statusText || statusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="兑换时间" width="160">
        <template #default="{ row }">{{ formatDateTime(row.exchangeTime) }}</template>
      </el-table-column>
      <el-table-column label="核销时间" width="160">
        <template #default="{ row }">{{ formatDateTime(row.verifyTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="110" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="row.status === 'PENDING'"
            link
            type="primary"
            @click="handleVerify(row)"
          >核销</el-button>
          <span v-else class="muted">—</span>
        </template>
      </el-table-column>
    </el-table>

    <div class="table-footer">
      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @current-change="load"
        @size-change="search"
      />
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { pageExchanges, verifyExchange } from '../api/points'
import { cleanParams, formatDateTime } from '../utils/format'

const statusOptions = [
  { value: 'PENDING', label: '待核销' },
  { value: 'VERIFIED', label: '已核销' },
  { value: 'CANCELLED', label: '已取消' }
]

const query = reactive({
  page: 1,
  size: 10,
  exchangeNo: '',
  status: ''
})
const rows = ref([])
const total = ref(0)
const loading = ref(false)

function statusText(status) {
  const item = statusOptions.find(option => option.value === status)
  return item ? item.label : status
}

function statusType(status) {
  if (status === 'VERIFIED') return 'success'
  if (status === 'CANCELLED') return 'info'
  return 'warning'
}

async function load() {
  loading.value = true
  try {
    const data = await pageExchanges(cleanParams(query))
    rows.value = (data && data.records) || []
    total.value = (data && data.total) || 0
  } catch (e) {
    rows.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function search() {
  query.page = 1
  load()
}

function reset() {
  query.exchangeNo = ''
  query.status = ''
  search()
}

async function handleVerify(row) {
  try {
    await ElMessageBox.confirm(
      '确认核销「' + row.goodsName + '」？核销后不可撤销。',
      '核销确认',
      { type: 'warning' }
    )
    await verifyExchange(row.id)
    ElMessage.success('核销成功')
    load()
  } catch (e) {
    // 用户取消或接口报错
  }
}

onMounted(load)
</script>
