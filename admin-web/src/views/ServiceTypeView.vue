<template>
  <div class="page-card">
    <div class="page-toolbar-title">
      <h3>服务项目</h3>
      <el-button type="primary" :icon="Plus" @click="openCreate">新增项目</el-button>
    </div>

    <div class="toolbar">
      <el-input v-model="query.name" placeholder="项目名称" clearable @keyup.enter="search" />
      <el-select v-model="query.category" placeholder="分类" clearable>
        <el-option v-for="item in categories" :key="item" :label="item" :value="item" />
      </el-select>
      <el-select v-model="query.status" placeholder="状态" clearable>
        <el-option label="上架中" :value="1" />
        <el-option label="已下架" :value="0" />
      </el-select>
      <el-button type="primary" @click="search">查询</el-button>
      <el-button @click="reset">重置</el-button>
    </div>

    <el-table v-loading="loading" :data="rows" border stripe>
      <el-table-column prop="id" label="ID" width="65" align="center" />
      <el-table-column prop="name" label="项目名称" min-width="150" show-overflow-tooltip />
      <el-table-column prop="category" label="分类" width="90" align="center">
        <template #default="{ row }">
          <el-tag effect="plain">{{ row.category || '其他' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="描述" min-width="220" show-overflow-tooltip />
      <el-table-column label="价格" width="95" align="right">
        <template #default="{ row }">
          <span class="money">¥{{ formatMoney(row.price) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="时长" width="95" align="center">
        <template #default="{ row }">{{ row.duration ? row.duration + ' 分钟' : '—' }}</template>
      </el-table-column>
      <el-table-column label="积分倍率" width="95" align="center">
        <template #default="{ row }">x{{ row.pointsRate == null ? 1 : row.pointsRate }}</template>
      </el-table-column>
      <el-table-column label="状态" width="95" align="center">
        <template #default="{ row }">
          <el-switch
            :model-value="row.status === 1"
            @change="value => toggleStatus(row, value)"
          />
        </template>
      </el-table-column>
      <el-table-column label="更新时间" width="150">
        <template #default="{ row }">{{ formatDateTime(row.updateTime || row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="135" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
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
      :title="editingId ? '编辑服务项目' : '新增服务项目'"
      width="560px"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="项目名称" prop="name">
          <el-input v-model="form.name" maxlength="50" placeholder="例如：标准精洗" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select
            v-model="form.category"
            allow-create
            filterable
            default-first-option
            placeholder="选择或输入分类"
            style="width: 100%"
          >
            <el-option v-for="item in categories" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" :step="10" style="width: 100%" />
        </el-form-item>
        <el-form-item label="预计时长">
          <el-input-number v-model="form.duration" :min="1" :step="15" style="width: 100%" />
          <span class="muted" style="margin-left: 8px">分钟</span>
        </el-form-item>
        <el-form-item label="积分倍率">
          <el-input-number v-model="form.pointsRate" :min="0" :precision="2" :step="0.1" style="width: 100%" />
          <span class="muted" style="margin-left: 8px">订单完成后 金额 × 倍率 发积分</span>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">上架</el-radio>
            <el-radio :value="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
            placeholder="服务内容说明，小程序详情页会拆成条目展示"
          />
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
  createServiceType,
  deleteServiceType,
  pageServiceTypes,
  updateServiceType,
  updateServiceTypeStatus
} from '../api/serviceType'
import { cleanParams, formatDateTime, formatMoney } from '../utils/format'

const categories = ['洗车', '内饰', '漆面', '保养']

const query = reactive({
  page: 1,
  size: 10,
  name: '',
  category: '',
  status: ''
})
const rows = ref([])
const total = ref(0)
const loading = ref(false)

const formVisible = ref(false)
const formRef = ref()
const saving = ref(false)
const editingId = ref(null)
const form = reactive(defaultForm())

const rules = {
  name: [{ required: true, message: '请输入项目名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }]
}

function defaultForm() {
  return {
    name: '',
    category: '洗车',
    description: '',
    price: 68,
    duration: 60,
    pointsRate: 1,
    status: 1
  }
}

async function load() {
  loading.value = true
  try {
    const data = await pageServiceTypes(cleanParams(query))
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
  query.name = ''
  query.category = ''
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
    name: row.name,
    category: row.category || '',
    description: row.description || '',
    price: Number(row.price || 0),
    duration: row.duration || 1,
    pointsRate: row.pointsRate == null ? 1 : Number(row.pointsRate),
    status: row.status == null ? 1 : row.status
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
      await updateServiceType(editingId.value, form)
      ElMessage.success('已保存')
    } else {
      await createServiceType(form)
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

async function toggleStatus(row, value) {
  try {
    await updateServiceTypeStatus(row.id, value ? 1 : 0)
    row.status = value ? 1 : 0
    ElMessage.success(value ? '已上架' : '已下架')
  } catch (e) {
    load()
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
      '确认删除「' + row.name + '」？删除后小程序不再展示该项目。',
      '删除确认',
      { type: 'warning' }
    )
    await deleteServiceType(row.id)
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
