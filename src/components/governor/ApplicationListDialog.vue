<template>
  <el-dialog v-model="dialogVisible" title="申请列表" width="1000px" :close-on-click-modal="false">
    <el-table :data="tableData" border stripe style="width: 100%">
      <el-table-column prop="id" label="ID" min-width="240" />
      <el-table-column prop="username" label="用户名" min-width="120" />
      <el-table-column prop="entity" label="实体名" min-width="120" />
      <el-table-column label="操作" width="160">
        <template #default="scope">
          <el-button type="success" size="small" @click="handleApprove(scope.row)" :disabled="scope.row.applyStatus === '治理方同意'">同意</el-button>
          <el-button type="danger" size="small" @click="handleReject(scope.row)" :disabled="scope.row.applyStatus === '已拒绝'" style="margin-left: 8px;">拒绝</el-button>
        </template>
      </el-table-column>
      <el-table-column prop="applyStatus" label="申请状态" min-width="220">
        <template #default="scope">
          <el-tag :type="getStatusTagType(scope.row.applyStatus)">{{ scope.row.applyStatus }}</el-tag>
        </template>
      </el-table-column>
    </el-table>
  </el-dialog>
</template>

<script setup>
import { ref, defineProps, watch } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  visible: Boolean
})
const emit = defineEmits(['update:visible', 'close'])

const dialogVisible = ref(props.visible)
watch(() => props.visible, v => dialogVisible.value = v)
watch(dialogVisible, v => {
  emit('update:visible', v)
  if (!v) emit('close')
})

const tableData = ref([
  { id: '123e4567-e89b-12d3-a456-426614174000', username: 'user1', entity: '库存管理', applyStatus: '数源方同意，等待治理方处理' },
  { id: '550e8400-e29b-41d4-a716-446655440001', username: 'user2', entity: '订单管理', applyStatus: '数源方同意，等待治理方处理' },
  { id: 'A003', username: 'user3', entity: '客户信息', applyStatus: '治理方同意' },
  { id: 'A004', username: 'user4', entity: '财务报表', applyStatus: '已拒绝' }
])

function handleApprove(row) {
  row.applyStatus = '治理方同意'
  ElMessage.success(`已同意ID为${row.id}的申请`)
}
function handleReject(row) {
  row.applyStatus = '已拒绝'
  ElMessage.warning(`已拒绝ID为${row.id}的申请`)
}
function getStatusTagType(status) {
  if (status.includes('治理方同意')) return 'success'
  if (status.includes('拒绝')) return 'danger'
  return 'info'
}
</script> 