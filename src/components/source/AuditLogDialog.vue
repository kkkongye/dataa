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
import { ref, computed } from 'vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  }
})
const emit = defineEmits(['close'])

const loading = ref(false)
const activeType = ref('all')
const currentPage = ref(1)
const pageSize = ref(10)

// 模拟数据
// 模拟数据（带重复 user）
const allData = ref([
  { user: '张管理员', time: '2025-05-30 14:30:00', type: '新建' },
  { user: '李管理员', time: '2025-05-31 09:05:45', type: '修改' },
  { user: '王审核员', time: '2025-05-31 10:20:33', type: '审核合格' },
  { user: '张管理员', time: '2025-06-01 08:15:27', type: '新建' },
  { user: '李管理员', time: '2025-06-01 11:45:19', type: '修改' },
  { user: '王审核员', time: '2025-06-02 13:30:55', type: '审核不合格' },
  { user: '张管理员', time: '2025-06-02 14:50:41', type: '新建' },
  { user: '李管理员', time: '2025-06-03 09:10:22', type: '修改' },
  { user: '王审核员', time: '2025-06-03 10:25:36', type: '审核合格' },
  { user: '张管理员', time: '2025-06-03 11:40:50', type: '新建' },
  { user: '李管理员', time: '2025-06-04 08:55:14', type: '修改' },
  { user: '王审核员', time: '2025-06-04 09:30:28', type: '审核不合格' },
  { user: '张管理员', time: '2025-06-04 10:45:42', type: '新建' },
])


// 目录筛选
const filteredData = computed(() => {
  if (activeType.value === 'all') return allData.value
  if (activeType.value === '审核') {
    return allData.value.filter(item => item.type === '审核合格' || item.type === '审核不合格')
  }
  return allData.value.filter(item => item.type === activeType.value)
})

// 分页数据
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