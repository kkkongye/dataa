<template>
  <el-dialog
    :model-value="visible"
    :title="`审计日志-${entityName || objectId || '未知实体'}`"
    width="1200px"
    :close-on-click-modal="false"
    :show-close="true"
    @close="emit('close')"
    class="audit-log-dialog"
    destroy-on-close
  >
    <div class="audit-log-content">
      <!-- 左侧目录 -->
      <div class="log-directory">
        <el-menu
          :default-active="activeType"
          class="log-menu"
          @select="handleMenuSelect"
        >
          <el-menu-item index="all">全部日志</el-menu-item>
          <el-menu-item index="新建">新建日志</el-menu-item>
          <el-menu-item index="修改">修改日志</el-menu-item>
          <el-menu-item index="查询">查询日志</el-menu-item>
          <el-menu-item index="审核">审核日志</el-menu-item>
        </el-menu>
      </div>
      <!-- 右侧表格 -->
      <div class="log-table-area">
        <el-table
          :data="pagedData"
          border
          stripe
          style="width: 100%"
          v-loading="loading"
          empty-text="暂无数据"
        >
          <el-table-column prop="user" label="操作用户" align="center" />
          <el-table-column prop="time" label="操作时间" align="center" />
          <el-table-column prop="type" label="操作类型" align="center" />
          <el-table-column prop="blockNumber" label="区块号" align="center" />
          <el-table-column prop="transactionHash" label="交易哈希" align="center" width="300" :show-overflow-tooltip="false" />
        </el-table>
      </div>
    </div>
    <template #footer>
      <div class="dialog-footer">
        <!-- 左侧分页栏 -->
        <div class="footer-left">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[5, 10, 15, 20]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="filteredData.length"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            small
          />
        </div>
        <!-- 右侧按钮 -->
        <div class="footer-right">
          <el-button @click="emit('close')">关闭</el-button>
          <!-- <el-button type="primary" plain @click="handleViewAuditLog">查看区块链详细日志</el-button> -->
        </div>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  objectId: {
    type: String,
    required: true
  },
  entityName: {
    type: String,
    default: ''
  }
})
const emit = defineEmits(['close'])

const loading = ref(false)
const activeType = ref('all')
const currentPage = ref(1)
const pageSize = ref(10)

const allData = ref([])

const getStatusType = (status) => {
  switch (status) {
    case 1:
      return '注册'
    case 2:
      return '新建' 
    case 3:
      return '修改'
    case 4:
      return '查询'
    case 5:
      return '审核合格'
    case 6:
      return '审核不合格'
    default:
      return '未知'
  }
}

const fetchTransactionHash = async (blockNumber) => {
  try {
    const response = await fetch(`http://localhost:8081/api/transaction-hash/${blockNumber}`)
    if (!response.ok) {
      throw new Error(`请求失败: ${response.status} ${response.statusText}`)
    }
    const result = await response.json()
    if (result.code === 1 && result.data) {
      return result.data
    }
    return '获取失败'
  } catch (error) {
    console.error('获取交易哈希失败:', error)
    return '获取失败'
  }
}

const fetchAuditLogs = async () => {
  console.log('开始获取审计日志，objectId:', props.objectId)
  if (!props.objectId) {
    console.warn('未提供objectId，取消请求')
    return
  }
  loading.value = true
  const url = `http://localhost:8081/api/objects/${props.objectId}/activityRecords`
  console.log('请求URL:', url)
  
  try {
    console.log('发起请求...')
    const response = await fetch(url)
    console.log('收到响应:', response.status, response.statusText)
    
    if (!response.ok) {
      throw new Error(`请求失败: ${response.status} ${response.statusText}`)
    }
    
    const result = await response.json()
    console.log('解析响应数据:', result)
    
    if (result.code === 1 && result.data) {
      console.log('数据处理前:', result.data)
      // 并行获取所有交易哈希
      const recordsWithHash = await Promise.all(
        result.data.map(async (record) => {
          const transactionHash = await fetchTransactionHash(record.blocknumber)
          const mappedData = {
            user: record.username,
            time: record.readableTimestamp,
            type: getStatusType(record.statusCode),
            blockNumber: record.blocknumber,
            transactionHash: transactionHash
          }
          console.log('映射数据:', record, ' -> ', mappedData)
          return mappedData
        })
      )
      allData.value = recordsWithHash
      console.log('最终数据:', allData.value)
    } else {
      console.error('API返回错误:', result.msg)
      allData.value = []
    }
  } catch (error) {
    console.error('获取审计日志失败:', error)
    allData.value = []
  } finally {
    loading.value = false
  }
}

// 确保在组件挂载时也调用一次
onMounted(() => {
  if (props.visible && props.objectId) {
    fetchAuditLogs()
  }
})

// 监听visible变化
watch(
  () => props.visible,
  (isVisible) => {
    console.log('dialog visible changed:', isVisible, 'objectId:', props.objectId)
    if (isVisible) {
      fetchAuditLogs()
    }
  },
  { immediate: true }
)

// 目录筛选
const filteredData = computed(() => {
  if (activeType.value === 'all') return allData.value
  if (activeType.value === '审核') {
    return allData.value.filter(item => item.type === '审核合格' || item.type === '审核不合格')
  }
  return allData.value.filter(item => item.type === activeType.value)
})

const pagedData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredData.value.slice(start, start + pageSize.value)
})

const handleMenuSelect = (key) => {
  activeType.value = key
  currentPage.value = 1
}
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
}
const handleCurrentChange = (val) => {
  currentPage.value = val
}

function handleViewAuditLog() {
  ElMessageBox.confirm(
    '即将跳转到区块链详细日志页面，是否继续？',
    '确认跳转',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    }
  ).then(() => {
    window.open('http://121.36.228.85/home', '_blank')
  }).catch(() => {
    ElMessage.info('已取消跳转')
  })
}
</script>

<style scoped>
.audit-log-content {
  display: flex;
  flex-direction: row;
  height: 600px;
  align-items: stretch;
}
.log-directory {
  width: 150px;
  margin-right: 24px;
}
.log-menu {
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  height: 100%;
}
.log-table-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: stretch;
  justify-content: flex-start;
  padding: 0;
  height: 100%;
}
.el-table {
  width: 100% !important;
  margin: 0;
  flex: 1;
}

.el-table .el-table__cell {
  word-break: break-all;
  white-space: normal;
}

.el-table .el-table__empty-block {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 100%;
  text-align: center;
}

.el-table .el-table__empty-text {
  color: #909399;
  font-size: 14px;
  text-align: center;
}

.log-table-area .el-table {
  position: relative;
}

/* 表头样式 - 浅灰色背景 */
:deep(.el-table__header th.el-table__cell) {
  background-color: #f5f7fa !important;
  color: #606266 !important;
  font-weight: bold !important;
  font-size: 14px !important;
  text-align: center !important;
  padding: 12px 8px !important;
}

.dialog-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}
.footer-left {
  flex: 1;
}
.footer-right {
  display: flex;
  gap: 10px;
}
</style>