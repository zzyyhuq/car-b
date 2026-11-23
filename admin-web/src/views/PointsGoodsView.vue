<template>
  <div class="page-card">
    <div class="page-toolbar-title">
      <h3>积分商品</h3>
      <el-button type="primary" :icon="Plus" @click="openCreate">新增商品</el-button>
    </div>

    <div class="toolbar">
      <el-input v-model="query.name" placeholder="商品名称" clearable @keyup.enter="search" />
      <el-select v-model="query.status" placeholder="状态" clearable>
        <el-option label="上架中" :value="1" />
        <el-option label="已下架" :value="0" />
      </el-select>
      <el-button type="primary" @click="search">查询</el-button>
      <el-button @click="reset">重置</el-button>
    </div>

    <el-table v-loading="loading" :data="rows" border stripe>
      <el-table-column prop="id" label="ID" width="65" align="center" />
      <el-table-column prop="name" label="商品名称" min-width="150" show-overflow-tooltip />
      <el-table-column prop="description" label="描述" min-width="220" show-overflow-tooltip />
      <el-table-column label="所需积分" width="100" align="right">
        <template #default="{ row }">
          <span class="money">{{ row.pointsPrice }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" width="90" align="center" />
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
      :title="editingId ? '编辑积分商品' : '新增积分商品'"
      width="520px"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="form.name" maxlength="50" placeholder="例如：免费精洗券" />
        </el-form-item>
        <el-form-item label="所需积分" prop="pointsPrice">
          <el-input-number v-model="form.pointsPrice" :min="1" :step="10" style="width: 100%" />
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input-number v-model="form.stock" :min="0" :step="1" style="width: 100%" />
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
            placeholder="兑换说明，小程序商城会展示"
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
  createGoods,
  deleteGoods,
  pageGoods,
  updateGoods,
  updateGoodsStatus
} from '../api/points'
import { cleanParams, formatDateTime } from '../utils/format'

const query = reactive({
  page: 1,
  size: 10,
  name: '',
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
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  pointsPrice: [{ required: true, message: '请输入所需积分', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }]
}

function defaultForm() {
  return {
    name: '',
    description: '',
    pointsPrice: 100,
    stock: 10,
    status: 1
  }
}

async function load() {
  loading.value = true
  try {
    const data = await pageGoods(cleanParams(query))
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
    description: row.description || '',
    pointsPrice: row.pointsPrice,
    stock: row.stock,
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
      await updateGoods(editingId.value, form)
      ElMessage.success('已保存')
    } else {
      await createGoods(form)
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
    await updateGoodsStatus(row.id, value ? 1 : 0)
    row.status = value ? 1 : 0
    ElMessage.success(value ? '已上架' : '已下架')
  } catch (e) {
    load()
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
      '确认删除「' + row.name + '」？删除后小程序商城不再展示。',
      '删除确认',
      { type: 'warning' }
    )
    await deleteGoods(row.id)
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
