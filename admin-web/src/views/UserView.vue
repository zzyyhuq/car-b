<template>
  <div class="page-card">
    <div class="page-toolbar-title">
      <h3>用户管理</h3>
      <span class="muted">当前仅提供查询，禁用/角色调整后续开放</span>
    </div>

    <div class="toolbar">
      <el-input
        v-model="query.keyword"
        placeholder="昵称 / 账号 / 手机号"
        clearable
        style="width: 240px"
        @keyup.enter="search"
      />
      <el-select v-model="query.role" placeholder="角色" clearable>
        <el-option label="管理员" value="ADMIN" />
        <el-option label="门店员工" value="STAFF" />
        <el-option label="车主" value="OWNER" />
      </el-select>
      <el-select v-model="query.status" placeholder="状态" clearable>
        <el-option label="正常" :value="1" />
        <el-option label="禁用" :value="0" />
      </el-select>
      <el-button type="primary" @click="search">查询</el-button>
      <el-button @click="reset">重置</el-button>
    </div>

    <el-table v-loading="loading" :data="rows" border stripe>
      <el-table-column prop="id" label="ID" width="65" align="center" />
      <el-table-column label="用户" min-width="170">
        <template #default="{ row }">
          <div>{{ row.nickname || '—' }}</div>
          <div class="muted">{{ row.username || row.openid || '微信用户' }}</div>
        </template>
      </el-table-column>
      <el-table-column label="角色" width="105" align="center">
        <template #default="{ row }">
          <el-tag :type="roleType(row.role)" effect="light">{{ ROLE_TEXT[row.role] || row.role }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="phone" label="手机号" width="135">
        <template #default="{ row }">{{ row.phone || '—' }}</template>
      </el-table-column>
      <el-table-column prop="points" label="积分" width="85" align="center" />
      <el-table-column label="状态" width="85" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" effect="light">
            {{ row.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="注册时间" width="160">
        <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
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
import { pageUsers } from '../api/user'
import { cleanParams, formatDateTime, ROLE_TEXT } from '../utils/format'

const query = reactive({
  page: 1,
  size: 10,
  keyword: '',
  role: '',
  status: ''
})
const rows = ref([])
const total = ref(0)
const loading = ref(false)

function roleType(role) {
  if (role === 'ADMIN') return 'danger'
  if (role === 'STAFF') return 'primary'
  return 'success'
}

async function load() {
  loading.value = true
  try {
    const data = await pageUsers(cleanParams(query))
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
  query.keyword = ''
  query.role = ''
  query.status = ''
  search()
}

onMounted(load)
</script>
