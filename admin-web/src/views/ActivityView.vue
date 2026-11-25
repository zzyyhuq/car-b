<template>
  <div class="page-card">
    <div class="page-toolbar-title">
      <h3>活动管理</h3>
      <el-button type="primary" :icon="Plus" @click="openCreate">新增活动</el-button>
    </div>

    <div class="toolbar">
      <el-input v-model="query.title" placeholder="活动标题" clearable @keyup.enter="search" />
      <el-select v-model="query.status" placeholder="状态" clearable>
        <el-option label="进行中" :value="1" />
        <el-option label="已下线" :value="0" />
      </el-select>
      <el-button type="primary" @click="search">查询</el-button>
      <el-button @click="reset">重置</el-button>
    </div>

    <el-table v-loading="loading" :data="rows" border stripe>
      <el-table-column prop="id" label="ID" width="65" align="center" />
      <el-table-column prop="title" label="活动标题" min-width="160" show-overflow-tooltip />
      <el-table-column prop="content" label="活动说明" min-width="200" show-overflow-tooltip />
      <el-table-column label="活动时间" min-width="220">
        <template #default="{ row }">
          <div>{{ formatDateTime(row.startTime) }}</div>
          <div class="muted">至 {{ formatDateTime(row.endTime) }}</div>
        </template>
      </el-table-column>
      <el-table-column label="报名" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="row.needSignup === 1 ? 'warning' : 'info'" effect="plain">
            {{ row.needSignup === 1 ? '需报名' : '仅公告' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="95" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="light">
            {{ row.status === 1 ? '进行中' : '已下线' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button
            v-if="row.needSignup === 1"
            link
            type="primary"
            @click="openSignups(row)"
          >名单</el-button>
          <el-button
            link
            :type="row.status === 1 ? 'warning' : 'success'"
            @click="toggleStatus(row)"
          >{{ row.status === 1 ? '下线' : '上线' }}</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
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

    <el-dialog
      v-model="formVisible"
      :title="editingId ? '编辑活动' : '新增活动'"
      width="620px"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="活动标题" prop="title">
          <el-input v-model="form.title" maxlength="100" placeholder="例如：秋季漆面养护节" />
        </el-form-item>
        <el-form-item label="活动说明">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="3"
            maxlength="1000"
            show-word-limit
            placeholder="小程序首页卡片和详情页展示"
          />
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker
            v-model="form.startTime"
            type="datetime"
            value-format="YYYY-MM-DDTHH:mm:ss"
            placeholder="选择开始时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker
            v-model="form.endTime"
            type="datetime"
            value-format="YYYY-MM-DDTHH:mm:ss"
            placeholder="选择结束时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="封面地址">
          <el-input v-model="form.cover" maxlength="255" placeholder="图片地址，可选" />
        </el-form-item>
        <el-form-item label="报名">
          <el-radio-group v-model="form.needSignup">
            <el-radio :value="0">仅公告</el-radio>
            <el-radio :value="1">需要报名</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">上线</el-radio>
            <el-radio :value="0">下线</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="signupVisible" title="报名名单" width="620px">
      <div class="muted" style="margin-bottom: 12px">
        活动 {{ signupRow && signupRow.title }}
      </div>
      <el-table
        v-loading="signupLoading"
        :data="signups"
        border
        stripe
        size="small"
        empty-text="还没有车主报名"
      >
        <el-table-column label="车主" min-width="120">
          <template #default="{ row }">{{ row.userName || '—' }}</template>
        </el-table-column>
        <el-table-column label="手机号" width="130">
          <template #default="{ row }">{{ row.phone || '—' }}</template>
        </el-table-column>
        <el-table-column label="报名时间" width="160">
          <template #default="{ row }">{{ formatDateTime(row.signupTime) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="light">
              {{ row.statusText }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="signupVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  createActivity,
  deleteActivity,
  listSignups,
  pageActivities,
  updateActivity,
  updateActivityStatus
} from '../api/activity'
import { cleanParams, formatDateTime } from '../utils/format'

const query = reactive({ page: 1, size: 10, title: '', status: '' })
const rows = ref([])
const total = ref(0)
const loading = ref(false)

const formVisible = ref(false)
const formRef = ref()
const saving = ref(false)
const editingId = ref(null)
const form = reactive(defaultForm())

const signupVisible = ref(false)
const signupRow = ref(null)
const signups = ref([])
const signupLoading = ref(false)

const rules = {
  title: [{ required: true, message: '请输入活动标题', trigger: 'blur' }]
}

function pad(n) {
  return n < 10 ? '0' + n : '' + n
}

function toIso(date) {
  return date.getFullYear() + '-' + pad(date.getMonth() + 1) + '-' + pad(date.getDate())
    + 'T' + pad(date.getHours()) + ':' + pad(date.getMinutes()) + ':00'
}

function defaultForm() {
  return {
    title: '',
    content: '',
    cover: '',
    startTime: toIso(new Date()),
    endTime: toIso(new Date(Date.now() + 30 * 24 * 60 * 60 * 1000)),
    needSignup: 0,
    status: 1
  }
}

async function load() {
  loading.value = true
  try {
    const data = await pageActivities(cleanParams(query))
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
  query.title = ''
  query.status = ''
  search()
}

function openCreate() {
  editingId.value = null
  Object.assign(form, defaultForm())
  formVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  Object.assign(form, {
    title: row.title,
    content: row.content || '',
    cover: row.cover || '',
    startTime: row.startTime || '',
    endTime: row.endTime || '',
    needSignup: row.needSignup == null ? 0 : row.needSignup,
    status: row.status == null ? 1 : row.status
  })
  formVisible.value = true
}

async function openSignups(row) {
  signupRow.value = row
  signups.value = []
  signupVisible.value = true
  signupLoading.value = true
  try {
    signups.value = (await listSignups(row.id)) || []
  } catch (e) {
    signups.value = []
  } finally {
    signupLoading.value = false
  }
}

function resetForm() {
  formRef.value && formRef.value.clearValidate()
  Object.assign(form, defaultForm())
  editingId.value = null
}

async function submitForm() {
  try {
    await formRef.value.validate()
  } catch (e) {
    return
  }
  if (form.startTime && form.endTime
    && new Date(form.endTime).getTime() < new Date(form.startTime).getTime()) {
    ElMessage.warning('结束时间不能早于开始时间')
    return
  }

  saving.value = true
  try {
    if (editingId.value) {
      await updateActivity(editingId.value, form)
      ElMessage.success('已保存')
    } else {
      await createActivity(form)
      ElMessage.success('已新增')
    }
    formVisible.value = false
    load()
  } catch (e) {
    // http.js 已提示
  } finally {
    saving.value = false
  }
}

async function toggleStatus(row) {
  const next = row.status === 1 ? 0 : 1
  try {
    await updateActivityStatus(row.id, next)
    ElMessage.success(next === 1 ? '已上线' : '已下线')
    load()
  } catch (e) {
    // http.js 已提示
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确认删除活动「' + row.title + '」？', '删除确认', {
      type: 'warning'
    })
    await deleteActivity(row.id)
    ElMessage.success('已删除')
    if (rows.value.length === 1 && query.page > 1) {
      query.page -= 1
    }
    load()
  } catch (e) {
    // 用户取消或接口报错
  }
}

onMounted(load)
</script>
