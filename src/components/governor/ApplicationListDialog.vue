<template>
  <el-dialog v-model="dialogVisible" title="申请列表" width="1000px" :close-on-click-modal="false">
    <el-table :data="tableData" border stripe style="width: 100%">
      <el-table-column prop="objectId" label="ID" min-width="240" />
      <el-table-column prop="applicant" label="用户名" min-width="120" />
      <el-table-column prop="entity" label="实体名" min-width="120" />
      <el-table-column label="操作" width="160">
        <template #default="scope">
          <el-button type="success" size="small" @click="handleApprove(scope.row)" :disabled="scope.row.governanceAgreed === true">同意</el-button>
          <el-button type="danger" size="small" @click="handleReject(scope.row)" :disabled="scope.row.governanceAgreed === false" style="margin-left: 8px;">拒绝</el-button>
        </template>
      </el-table-column>
      <el-table-column label="申请状态" min-width="220">
        <template #default="scope">
          <el-tag :type="getStatusTagType(getStatusText(scope.row))">
            {{ getStatusText(scope.row) }}
          </el-tag>
        </template>
      </el-table-column>
    </el-table>
  </el-dialog>
</template>

<script setup>
import { ref, defineProps, watch } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const props = defineProps({
  visible: Boolean
})
const emit = defineEmits(['update:visible', 'close'])

const dialogVisible = ref(props.visible)
watch(() => props.visible, v => dialogVisible.value = v)

const tableData = ref([])

function fetchRecords() {
  axios.get('http://localhost:8080/api/applications/records', { withCredentials: true })
    .then(res => {
      if (res.data && res.data.code === 1) {
        tableData.value = (res.data.data || []).filter(item => item.sourceAgreed === true)
      } else {
        ElMessage.error(res.data.msg || '获取申请记录失败')
      }
    })
    .catch(err => {
      ElMessage.error('获取申请记录失败: ' + (err.response?.data?.message || err.message))
    })
}

watch(dialogVisible, v => {
  emit('update:visible', v)
  if (v) fetchRecords()
  if (!v) emit('close')
})

function handleApprove(row) {
  axios.post(`http://localhost:8080/api/applications/governance/approve/${row.id}`, null, { withCredentials: true })
    .then(res => {
      if (res.data && res.data.code === 1) {
        row.governanceAgreed = true;
        ElMessage.success(`治理方同意ID为${row.id}的申请`);
      } else {
        ElMessage.error(res.data.msg || '审批失败')
      }
    })
    .catch(err => {
      ElMessage.error('审批失败: ' + (err.response?.data?.message || err.message))
    })
}
function handleReject(row) {
  axios.post(`http://localhost:8080/api/applications/governance/reject/${row.id}`, null, { withCredentials: true })
    .then(res => {
      if (res.data && res.data.code === 1) {
        row.governanceAgreed = false;
        ElMessage.warning(`已拒绝ID为${row.id}的申请`);
      } else {
        ElMessage.error(res.data.msg || '拒绝失败')
      }
    })
    .catch(err => {
      ElMessage.error('拒绝失败: ' + (err.response?.data?.message || err.message))
    })
}
function getStatusText(row) {
  if (row.governanceAgreed === true) return '治理方同意'
  return '治理方拒绝或未处理'
}
function getStatusTagType(status) {
  if (!status) return 'info'
  if (status.includes('同意')) return 'success'
  if (status.includes('拒绝')) return 'danger'
  return 'info'
}
</script> 