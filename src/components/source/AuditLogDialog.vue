<template>
  <el-dialog
    :model-value="visible"
    title="审计日志"
    width="900px"
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
        >
          <el-table-column prop="user" label="操作用户" align="center" />
          <el-table-column prop="time" label="操作时间" align="center" />
          <el-table-column prop="type" label="操作类型" align="center" />
        </el-table>
        <div class="pagination-area">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[5, 10, 15, 20]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="filteredData.length"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </div>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="emit('close')">关闭</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  objectId: {
    type: String,
    required: true
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
      allData.value = result.data.map((record) => {
        const mappedData = {
          user: record.username,
          time: record.formattedTimestamp,
          type: getStatusType(record.statusCode)
        }
        console.log('映射数据:', record, ' -> ', mappedData)
        return mappedData
      })
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
</script>

<style scoped>
.audit-log-content {
  display: flex;
  flex-direction: row;
  min-height: 400px;
}
.log-directory {
  width: 150px;
  margin-right: 24px;
}
.log-menu {
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  min-height: 320px;
}
.log-table-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: stretch;
  justify-content: center;
  padding: 0;
}
.el-table {
  width: 100% !important;
  margin: 0;
}
.pagination-area {
  margin-top: 16px;
  text-align: right;
}
</style> 