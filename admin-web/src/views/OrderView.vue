<template>
  <div class="page-card">
    <div class="page-toolbar-title">
      <h3>订单管理</h3>
      <el-button :icon="Refresh" @click="load">刷新</el-button>
    </div>

    <div class="toolbar">
      <el-input v-model="query.orderNo" placeholder="订单号" clearable @keyup.enter="search" />
      <el-input v-model="query.plateNo" placeholder="车牌号" clearable @keyup.enter="search" />
      <el-select v-model="query.status" placeholder="订单状态" clearable>
        <el-option
          v-for="item in statusOptions"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        value-format="YYYY-MM-DD"
        start-placeholder="下单开始"
        end-placeholder="下单结束"
        style="width: 260px"
      />
      <el-button type="primary" @click="search">查询</el-button>
      <el-button @click="reset">重置</el-button>
    </div>

    <el-table v-loading="loading" :data="rows" border stripe>
      <el-table-column prop="orderNo" label="订单号" width="180" />
      <el-table-column prop="serviceName" label="服务项目" width="120" show-overflow-tooltip />
      <el-table-column label="车主 / 车辆" min-width="160">
        <template #default="{ row }">
          <div>{{ row.userName || '—' }}</div>
          <div class="muted">
            {{ row.plateNo || '—' }}
            <span v-if="row.carBrand || row.carModel">· {{ row.carBrand }} {{ row.carModel }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="预约时间" width="145">
        <template #default="{ row }">{{ formatDateTime(row.appointmentTime) }}</template>
      </el-table-column>
      <el-table-column label="金额" width="90" align="right">
        <template #default="{ row }">
          <span class="money">¥{{ formatMoney(row.amount) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="ORDER_STATUS_TYPE[row.status] || 'info'" effect="light">
            {{ row.statusText || ORDER_STATUS_TEXT[row.status] || row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="负责员工" width="110">
        <template #default="{ row }">{{ row.staffName || '—' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row)">详情</el-button>
          <el-button
            v-if="row.status === 'PENDING'"
            link
            type="primary"
            @click="changeStatus(row, 'ACCEPTED')"
          >接单</el-button>
          <el-button
            v-if="row.status === 'ACCEPTED'"
            link
            type="primary"
            @click="changeStatus(row, 'IN_PROGRESS')"
          >开始服务</el-button>
          <el-button
            v-if="row.status === 'IN_PROGRESS'"
            link
            type="success"
            @click="changeStatus(row, 'COMPLETED')"
          >完成</el-button>
          <el-button
            v-if="showProgress(row)"
            link
            type="primary"
            @click="openProcess(row)"
          >进度</el-button>
          <el-dropdown
            v-if="hasMoreActions(row)"
            trigger="click"
            @command="command => handleMore(command, row)"
          >
            <el-button link type="primary" class="more-btn">
              更多<el-icon><ArrowDown /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item v-if="auth.isAdmin && canAssign(row)" command="assign">
                  派单
                </el-dropdown-item>
                <el-dropdown-item v-if="cancellable(row)" command="cancel">
                  取消订单
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
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

    <el-dialog v-model="detailVisible" title="订单详情" width="720px">
      <el-descriptions v-if="detail" :column="2" border>
        <el-descriptions-item label="订单号">{{ detail.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="ORDER_STATUS_TYPE[detail.status] || 'info'" effect="light">
            {{ detail.statusText || ORDER_STATUS_TEXT[detail.status] || detail.status }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="服务项目">{{ detail.serviceName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="金额">
          <span class="money">¥{{ formatMoney(detail.amount) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="车主">{{ detail.userName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="负责员工">{{ detail.staffName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="车牌">
          {{ detail.plateNo || '—' }}
          <span v-if="detail.carBrand || detail.carModel">
            （{{ detail.carBrand }} {{ detail.carModel }}）
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="预约时间">{{ formatDateTime(detail.appointmentTime) }}</el-descriptions-item>
        <el-descriptions-item label="下单时间">{{ formatDateTime(detail.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="完成时间">{{ formatDateTime(detail.finishTime) }}</el-descriptions-item>
        <el-descriptions-item label="取消原因" :span="2">
          {{ detail.cancelReason || '—' }}
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detail.remark || '—' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="assignVisible" title="派单给员工" width="420px">
      <div class="muted" style="margin-bottom: 12px">
        订单 {{ assignRow && assignRow.orderNo }}
      </div>
      <el-select v-model="assignStaffId" placeholder="选择员工" style="width: 100%">
        <el-option
          v-for="staff in staffOptions"
          :key="staff.id"
          :label="staff.nickname || staff.username"
          :value="staff.id"
        />
      </el-select>
      <template #footer>
        <el-button @click="assignVisible = false">取消</el-button>
        <el-button type="primary" :loading="assigning" @click="submitAssign">确认派单</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="processVisible" title="服务进度" width="640px">
      <div class="muted" style="margin-bottom: 14px">
        订单 {{ processRow && processRow.orderNo }} · {{ processRow && processRow.serviceName }}
      </div>

      <div v-loading="processLoading">
        <el-timeline v-if="processList.length">
          <el-timeline-item
            v-for="item in processList"
            :key="item.id"
            :timestamp="formatDateTime(item.createTime)"
            placement="top"
            :type="item.node === 'DELIVER' ? 'success' : 'primary'"
          >
            <div class="process-head">
              <strong>{{ item.nodeText }}</strong>
              <span class="muted">{{ item.staffName || '—' }} 登记</span>
            </div>
            <div v-if="item.content" class="process-content">{{ item.content }}</div>
            <div v-if="item.images && item.images.length" class="process-images">
              <el-image
                v-for="(img, index) in item.images"
                :key="index"
                :src="img"
                :preview-src-list="item.images"
                :initial-index="index"
                fit="cover"
                class="process-image"
              />
            </div>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-else description="还没有进度记录" :image-size="70" />
      </div>

      <template v-if="canAddProgress">
        <el-divider content-position="left">登记进度</el-divider>
        <el-form label-width="70px">
          <el-form-item label="节点">
            <el-select v-model="processForm.node" style="width: 100%">
              <el-option
                v-for="node in processNodes"
                :key="node.value"
                :label="node.label"
                :value="node.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="说明">
            <el-input
              v-model="processForm.content"
              type="textarea"
              :rows="2"
              maxlength="500"
              show-word-limit
              placeholder="例如：已完成内饰吸尘，正在质检"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="processSaving" @click="submitProcess">
              登记进度
            </el-button>
          </el-form-item>
        </el-form>
      </template>
      <div v-else class="muted process-tip">
        当前订单状态不能登记进度，只有「已接单 / 服务中」可以登记。
      </div>

      <template #footer>
        <el-button @click="processVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown, Refresh } from '@element-plus/icons-vue'
import {
  assignOrder,
  cancelOrder,
  getOrder,
  pageOrders,
  updateOrderStatus
} from '../api/order'
import { addProcess, listProcess } from '../api/serviceProcess'
import { listStaff } from '../api/user'
import { useAuthStore } from '../stores/auth'
import {
  cleanParams,
  formatDateTime,
  formatMoney,
  ORDER_STATUS_TEXT,
  ORDER_STATUS_TYPE
} from '../utils/format'

const auth = useAuthStore()
const statusOptions = Object.entries(ORDER_STATUS_TEXT).map(([value, label]) => ({ value, label }))

const query = reactive({
  page: 1,
  size: 10,
  orderNo: '',
  plateNo: '',
  status: ''
})
const dateRange = ref([])
const rows = ref([])
const total = ref(0)
const loading = ref(false)

const detailVisible = ref(false)
const detail = ref(null)

const assignVisible = ref(false)
const assignRow = ref(null)
const assignStaffId = ref(null)
const staffOptions = ref([])
const assigning = ref(false)

const processVisible = ref(false)
const processRow = ref(null)
const processList = ref([])
const processLoading = ref(false)
const processSaving = ref(false)
const processForm = reactive({ node: 'RECEIVE', content: '' })
const processNodes = [
  { value: 'RECEIVE', label: '接车' },
  { value: 'CLEAN', label: '清洗' },
  { value: 'CHECK', label: '质检' },
  { value: 'DELIVER', label: '交车' }
]
const canAddProgress = computed(() => {
  const status = processRow.value && processRow.value.status
  return status === 'ACCEPTED' || status === 'IN_PROGRESS'
})

function canAssign(row) {
  return row.status === 'PENDING' || row.status === 'ACCEPTED'
}

function cancellable(row) {
  return row.status === 'PENDING' || row.status === 'ACCEPTED'
}

function showProgress(row) {
  return ['ACCEPTED', 'IN_PROGRESS', 'COMPLETED', 'EVALUATED'].includes(row.status)
}

function hasMoreActions(row) {
  return (auth.isAdmin && canAssign(row)) || cancellable(row)
}

function handleMore(command, row) {
  if (command === 'assign') {
    openAssign(row)
  } else if (command === 'cancel') {
    handleCancel(row)
  }
}

async function load() {
  loading.value = true
  try {
    const params = cleanParams(query)
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    const data = await pageOrders(params)
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
  query.orderNo = ''
  query.plateNo = ''
  query.status = ''
  dateRange.value = []
  search()
}

async function openDetail(row) {
  detail.value = row
  detailVisible.value = true
  try {
    detail.value = await getOrder(row.id)
  } catch (e) {
    // 保留列表里的数据
  }
}

async function changeStatus(row, status) {
  const labels = {
    ACCEPTED: '接单',
    IN_PROGRESS: '开始服务',
    COMPLETED: '完成订单'
  }
  try {
    await ElMessageBox.confirm(
      '确认对订单 ' + row.orderNo + ' 执行「' + labels[status] + '」？',
      '操作确认',
      { type: 'warning' }
    )
    await updateOrderStatus(row.id, status)
    ElMessage.success('操作成功')
    load()
  } catch (e) {
    // 用户取消或接口报错，接口错误已由 http.js 提示
  }
}

async function handleCancel(row) {
  try {
    const { value } = await ElMessageBox.prompt('取消原因可留空', '取消订单 ' + row.orderNo, {
      confirmButtonText: '确认取消',
      cancelButtonText: '返回',
      inputPlaceholder: '例如：车主临时有事'
    })
    await cancelOrder(row.id, value || '')
    ElMessage.success('订单已取消')
    load()
  } catch (e) {
    // 用户取消或接口报错
  }
}

async function openAssign(row) {
  assignRow.value = row
  assignStaffId.value = row.staffId || null
  assignVisible.value = true
  if (!staffOptions.value.length) {
    try {
      const data = await listStaff()
      staffOptions.value = (data && data.records) || []
    } catch (e) {
      assignVisible.value = false
    }
  }
}

async function submitAssign() {
  if (!assignStaffId.value) {
    ElMessage.warning('请选择员工')
    return
  }
  assigning.value = true
  try {
    await assignOrder(assignRow.value.id, assignStaffId.value)
    ElMessage.success('派单成功')
    assignVisible.value = false
    load()
  } finally {
    assigning.value = false
  }
}

async function openProcess(row) {
  processRow.value = row
  processForm.node = 'RECEIVE'
  processForm.content = ''
  processVisible.value = true
  await loadProcess(row.id)
}

async function loadProcess(orderId) {
  processLoading.value = true
  try {
    processList.value = (await listProcess(orderId)) || []
  } catch (e) {
    processList.value = []
  } finally {
    processLoading.value = false
  }
}

async function submitProcess() {
  if (!processForm.node) {
    ElMessage.warning('请选择进度节点')
    return
  }
  processSaving.value = true
  try {
    await addProcess({
      orderId: processRow.value.id,
      node: processForm.node,
      content: processForm.content
    })
    ElMessage.success('进度已登记')
    processForm.content = ''
    await loadProcess(processRow.value.id)
  } finally {
    processSaving.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.more-btn {
  margin-left: 8px;
}

.more-btn .el-icon {
  margin-left: 2px;
}

.process-head {
  display: flex;
  align-items: baseline;
  gap: 10px;
}

.process-head .muted {
  font-size: 12px;
}

.process-content {
  margin-top: 6px;
  color: #4a5568;
  font-size: 13px;
  line-height: 1.7;
}

.process-images {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
}

.process-image {
  width: 78px;
  height: 78px;
  border-radius: 6px;
}

.process-tip {
  margin-top: 8px;
}
</style>
