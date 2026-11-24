<template>
  <div class="page-card">
    <div class="page-toolbar-title">
      <h3>公告管理</h3>
      <el-button type="primary" :icon="Plus" @click="openCreate">新增公告</el-button>
    </div>

    <div class="toolbar">
      <el-input v-model="query.title" placeholder="公告标题" clearable @keyup.enter="search" />
      <el-select v-model="query.status" placeholder="状态" clearable>
        <el-option label="已发布" :value="1" />
        <el-option label="草稿" :value="0" />
      </el-select>
      <el-button type="primary" @click="search">查询</el-button>
      <el-button @click="reset">重置</el-button>
    </div>

    <el-table v-loading="loading" :data="rows" border stripe>
      <el-table-column prop="id" label="ID" width="65" align="center" />
      <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
      <el-table-column prop="content" label="内容" min-width="240" show-overflow-tooltip />
      <el-table-column label="状态" width="95" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="light">
            {{ row.status === 1 ? '已发布' : '草稿' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="发布时间" width="150">
        <template #default="{ row }">{{ formatDateTime(row.publishTime) }}</template>
      </el-table-column>
      <el-table-column label="更新时间" width="150">
        <template #default="{ row }">{{ formatDateTime(row.updateTime || row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button
            link
            :type="row.status === 1 ? 'warning' : 'success'"
            @click="toggleStatus(row)"
          >{{ row.status === 1 ? '下线' : '发布' }}</el-button>
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
      :title="editingId ? '编辑公告' : '新增公告'"
      width="580px"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" maxlength="100" placeholder="例如：国庆营业时间调整" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="6"
            maxlength="2000"
            show-word-limit
            placeholder="公告正文，小程序详情页展示"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">立即发布</el-radio>
            <el-radio :value="0">保存草稿</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  createNotice,
  deleteNotice,
  pageNotices,
  updateNotice,
  updateNoticeStatus
} from '../api/notice'
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

const rules = {
  title: [{ required: true, message: '请输入公告标题', trigger: 'blur' }]
}

function defaultForm() {
  return { title: '', content: '', status: 1 }
}

async function load() {
  loading.value = true
  try {
    const data = await pageNotices(cleanParams(query))
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
    status: row.status == null ? 0 : row.status
  })
  formVisible.value = true
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
  saving.value = true
  try {
    if (editingId.value) {
      await updateNotice(editingId.value, form)
      ElMessage.success('已保存')
    } else {
      await createNotice(form)
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
    await updateNoticeStatus(row.id, next)
    ElMessage.success(next === 1 ? '已发布' : '已下线')
    load()
  } catch (e) {
    // http.js 已提示
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确认删除公告「' + row.title + '」？', '删除确认', {
      type: 'warning'
    })
    await deleteNotice(row.id)
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
