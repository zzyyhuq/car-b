<template>
  <div class="page-card">
    <div class="page-toolbar-title">
      <h3>留言管理</h3>
      <el-button :icon="Refresh" @click="load">刷新</el-button>
    </div>

    <div class="toolbar">
      <el-select v-model="query.type" placeholder="类型" clearable>
        <el-option label="评价" value="EVALUATION" />
        <el-option label="留言" value="FEEDBACK" />
      </el-select>
      <el-select v-model="query.status" placeholder="状态" clearable>
        <el-option label="待审核" :value="0" />
        <el-option label="已通过" :value="1" />
        <el-option label="已隐藏" :value="2" />
      </el-select>
      <el-input
        v-model="query.keyword"
        placeholder="内容关键词"
        clearable
        style="width: 220px"
        @keyup.enter="search"
      />
      <el-button type="primary" @click="search">查询</el-button>
      <el-button @click="reset">重置</el-button>
    </div>

    <el-table v-loading="loading" :data="rows" border stripe>
      <el-table-column label="类型" width="75" align="center">
        <template #default="{ row }">
          <el-tag :type="row.type === 'EVALUATION' ? 'warning' : 'primary'" effect="light">
            {{ row.typeText || (row.type === 'EVALUATION' ? '评价' : '留言') }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="用户" width="90">
        <template #default="{ row }">{{ row.userName || '—' }}</template>
      </el-table-column>
      <el-table-column label="关联订单" width="165" show-overflow-tooltip>
        <template #default="{ row }">{{ row.orderNo || '—' }}</template>
      </el-table-column>
      <el-table-column label="评分" width="100" align="center">
        <template #default="{ row }">
          <span v-if="row.rating" class="rating">{{ stars(row.rating) }}</span>
          <span v-else class="muted">—</span>
        </template>
      </el-table-column>
      <el-table-column prop="content" label="内容" min-width="160" show-overflow-tooltip />
      <el-table-column label="状态" width="85" align="center">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" effect="light">
            {{ row.statusText || statusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="回复" min-width="140" show-overflow-tooltip>
        <template #default="{ row }">{{ row.reply || '—' }}</template>
      </el-table-column>
      <el-table-column label="提交时间" width="140">
        <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="145" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openReply(row)">回复</el-button>
          <el-button
            v-if="row.status !== 1"
            link
            type="success"
            @click="handleAudit(row, 1)"
          >通过</el-button>
          <el-button
            v-if="row.status !== 2"
            link
            type="danger"
            @click="handleAudit(row, 2)"
          >隐藏</el-button>
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

    <el-dialog v-model="replyVisible" title="回复留言" width="560px">
      <div class="reply-target">
        <el-tag :type="replyRow && replyRow.type === 'EVALUATION' ? 'warning' : 'primary'" effect="light">
          {{ replyRow && (replyRow.typeText || replyRow.type) }}
        </el-tag>
        <span class="muted">{{ replyRow && replyRow.userName }}</span>
      </div>
      <div class="reply-content">{{ replyRow && replyRow.content }}</div>
      <el-input
        v-model="replyText"
        type="textarea"
        :rows="4"
        maxlength="1000"
        show-word-limit
        placeholder="输入回复内容，车主可在小程序中看到"
      />
      <template #footer>
        <el-button @click="replyVisible = false">取消</el-button>
        <el-button type="primary" :loading="replying" @click="submitReply">保存回复</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { auditMessage, pageMessages, replyMessage } from '../api/message'
import { cleanParams, formatDateTime } from '../utils/format'

const query = reactive({
  page: 1,
  size: 10,
  type: '',
  status: '',
  keyword: ''
})
const rows = ref([])
const total = ref(0)
const loading = ref(false)

const replyVisible = ref(false)
const replyRow = ref(null)
const replyText = ref('')
const replying = ref(false)

const STATUS_TEXT = {
  0: '待审核',
  1: '已通过',
  2: '已隐藏'
}

function statusText(status) {
  return STATUS_TEXT[status] || status
}

function statusType(status) {
  if (status === 1) return 'success'
  if (status === 2) return 'info'
  return 'warning'
}

function stars(rating) {
  const value = Number(rating || 0)
  return '★'.repeat(value) + '☆'.repeat(Math.max(0, 5 - value))
}

async function load() {
  loading.value = true
  try {
    const data = await pageMessages(cleanParams(query))
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
  query.type = ''
  query.status = ''
  query.keyword = ''
  search()
}

function openReply(row) {
  replyRow.value = row
  replyText.value = row.reply || ''
  replyVisible.value = true
}

async function submitReply() {
  if (!replyText.value.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  replying.value = true
  try {
    await replyMessage(replyRow.value.id, replyText.value.trim())
    ElMessage.success('回复已保存')
    replyVisible.value = false
    load()
  } finally {
    replying.value = false
  }
}

async function handleAudit(row, status) {
  const label = status === 1 ? '通过' : '隐藏'
  try {
    await ElMessageBox.confirm(
      '确认' + label + '这条' + (row.typeText || '留言') + '？',
      '审核确认',
      { type: 'warning' }
    )
    await auditMessage(row.id, status)
    ElMessage.success('已' + label)
    load()
  } catch (e) {
    // 用户取消或接口报错
  }
}

onMounted(load)
</script>

<style scoped>
.rating {
  color: #ff9f1c;
  font-size: 13px;
  letter-spacing: 1px;
}

.reply-target {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.reply-content {
  margin-bottom: 14px;
  padding: 12px 14px;
  border-radius: 8px;
  background: #f6f8fb;
  color: #4a5568;
  font-size: 13px;
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
