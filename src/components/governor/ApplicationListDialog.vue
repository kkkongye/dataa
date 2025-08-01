<template>
  <el-dialog v-model="dialogVisible" title="申请列表" width="1200px" :close-on-click-modal="false">
    <el-table 
      :data="currentGroupData" 
      border 
      stripe 
      style="width: 100%"
      :span-method="spanMethod"
      :row-style="{ height: '60px' }"
      v-loading="loading"
      element-loading-text="正在加载数据..."
    >
      <el-table-column prop="groupId" label="组序号" width="120" align="center">
        <template #default="scope">
          <span style="font-weight: bold;">{{ scope.row.groupId }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="objectId" label="ID" min-width="240" align="center" />
      <el-table-column prop="entity" label="实体名" min-width="120"  align="center"/>
      <el-table-column prop="applicant" label="申请人" min-width="120" align="center" />
      <el-table-column prop="applyTime" label="申请时间" min-width="160" align="center" />
      <el-table-column label="操作" width="150" align="center">
        <template #default="scope">
          <el-button 
            type="primary" 
            plain 
            @click="handleGenerateOrgVouchers" 
            size="small"
            :disabled="scope.row.orgCredentialStatus === true"
          >
            生成组织机构凭证
          </el-button>
        </template>
      </el-table-column>
      <el-table-column label="申请状态" min-width="200" align="center">
        <template #default="scope">
          <el-tag :type="getStatusTagType(getStatusText(scope.row))">
            {{ getStatusText(scope.row) }}
          </el-tag>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 分页组件 -->
    <div class="pagination-area" style="margin-top: 20px; display: flex; justify-content: space-between; align-items: center;">
      <el-button type="primary" plain @click="handleRefresh" :loading="loading" size="small">
        <el-icon><Refresh /></el-icon>
        刷新数据
      </el-button>
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="1"
        layout="prev, pager, next, total"
        :total="totalGroups"
        @current-change="handlePageChange"
        background
      />
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, defineProps, watch, computed } from 'vue'
import { ElMessage, ElLoading, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import axios from 'axios'

const props = defineProps({
  visible: Boolean
})
const emit = defineEmits(['update:visible', 'close'])

const dialogVisible = ref(props.visible)
watch(() => props.visible, v => dialogVisible.value = v)

// 分页相关
const currentPage = ref(1)
const totalGroups = ref(0)

// 后端数据
const groupData = ref([])
const loading = ref(false)

// 获取申请记录数据
const fetchApplicationRecords = async () => {
  try {
    loading.value = true
    const response = await axios.get('http://localhost:8083/api/application-records')
    
    if (response.data && response.data.code === 1 && response.data.data) {
      // 治理方只处理已通过数源方审批的申请（sourceAgreed为true）
      return response.data.data.filter(record => record.sourceAgreed === true)
    } else {
      console.warn('获取申请记录失败:', response.data?.msg || '未知错误')
      return []
    }
  } catch (error) {
    console.error('获取申请记录失败:', error)
    ElMessage.error('获取申请记录失败')
    return []
  } finally {
    loading.value = false
  }
}

// 获取对象详情数据
const fetchObjectDetails = async (objectIds) => {
  try {
    const response = await axios.get('http://localhost:8081/api/objects/list')
    
    if (response.data && response.data.code === 1 && response.data.data) {
      const allObjects = response.data.data
      // 根据objectIds筛选对象
      const filteredObjects = allObjects.filter(obj => objectIds.includes(obj.id))
      return filteredObjects
    } else {
      console.warn('获取对象详情失败:', response.data?.msg || '未知错误')
      return []
    }
  } catch (error) {
    console.error('获取对象详情失败:', error)
    return []
  }
}

// 加载数据
const loadData = async () => {
  try {
    loading.value = true
    
    // 获取申请记录
    const applicationRecords = await fetchApplicationRecords()
    
    if (applicationRecords.length === 0) {
      groupData.value = []
      totalGroups.value = 0
      return
    }
    
    // 处理每个申请记录
    const processedGroups = await Promise.all(
      applicationRecords.map(async (record, index) => {
        // 解析objectIds
        const objectIds = record.objectIds ? record.objectIds.split(',') : []
        
        // 获取对象详情
        const objectDetails = await fetchObjectDetails(objectIds)
        
        // 构建申请数据
        const applications = objectDetails.map((obj, objIndex) => ({
          id: objIndex + 1,
          objectId: obj.id,
          applicant: record.applicant || '未知申请方',
          entity: obj.dataEntity?.entity || '未知实体',
          applyTime: record.applyTime ? new Date(record.applyTime).toLocaleString('zh-CN') : '-'
        }))
        
        return {
          groupId: `G${String(index + 1).padStart(3, '0')}`,
          applyTime: record.applyTime ? new Date(record.applyTime).toLocaleString('zh-CN') : '-',
          originalApplyTime: record.applyTime, // 保存原始申请时间用于API调用
          dataCredentialStatus: record.sourceAgreed || false,
          orgCredentialStatus: record.governanceAgreed1 || false,
          applications: applications
        }
      })
    )
    
    groupData.value = processedGroups
    totalGroups.value = processedGroups.length
    
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 当前组数据
const currentGroupData = computed(() => {
  const groupIndex = currentPage.value - 1
  const group = groupData.value[groupIndex]
  if (!group) return []
  
  return group.applications.map(app => ({
    ...app,
    groupId: group.groupId,
    applyTime: group.applyTime,
    dataCredentialStatus: group.dataCredentialStatus,
    orgCredentialStatus: group.orgCredentialStatus
  }))
})

const tableData = ref([])

// 单元格合并方法
const spanMethod = ({ row, column, rowIndex, columnIndex }) => {
  // 组序号列(第0列)、申请人列(第3列)、申请时间列(第4列)、操作列(第5列)、申请状态列(第6列)需要合并
  if (columnIndex === 0 || columnIndex === 3 || columnIndex === 4 || columnIndex === 5 || columnIndex === 6) {
    if (rowIndex === 0) {
      return {
        rowspan: currentGroupData.value.length,
        colspan: 1
      }
    } else {
      return {
        rowspan: 0,
        colspan: 0
      }
    }
  }
  return {
    rowspan: 1,
    colspan: 1
  }
}

// 分页处理
const handlePageChange = (page) => {
  currentPage.value = page
}

async function fetchRecords() {
  // 从后端获取数据
  await loadData()
  currentPage.value = 1
}

// 刷新数据
const handleRefresh = async () => {
  await loadData()
  ElMessage.success('数据已刷新')
}

watch(dialogVisible, v => {
  emit('update:visible', v)
  if (v) fetchRecords()
  if (!v) emit('close')
})

// 生成组织机构凭证方法
const handleGenerateOrgVouchers = async () => {
  // 获取当前组的申请人和申请时间
  const groupIndex = currentPage.value - 1
  const group = groupData.value[groupIndex]
  
  if (!group || !group.applications || group.applications.length === 0) {
    ElMessage.error('当前组数据不完整')
    return
  }
  
  const applicant = group.applications[0].applicant
  const applyTime = group.originalApplyTime || group.applyTime
  
  if (!applicant || !applyTime) {
    ElMessage.error('申请人或申请时间不能为空')
    return
  }
  
  const loading = ElLoading.service({ fullscreen: true, text: '正在校验...' })
  try {
    const res1 = await axios.post('http://localhost:8082/api/decrypt-verify')
    if (res1.data && res1.data.code === 0 && res1.data.msg === '尚未接收到加密数据') {
      loading.close()
      ElMessage.error('解密校验失败：尚未接收到加密数据，请先确保数据已正确接收')
    } else if (res1.data && res1.data.code === 1) {
      loading.close()
      await ElMessageBox.confirm('解密校验成功，是否生成组织机构凭证？', '提示', {
        confirmButtonText: '生成',
        cancelButtonText: '取消',
        type: 'info',
      })
      // 第二步：生成组织机构凭证，传递申请人和申请时间参数
      const loading2 = ElLoading.service({ fullscreen: true, text: '正在生成组织机构凭证...' })
      const res2 = await axios.post(`http://localhost:8082/api/generate-org-vouchers?applicant=${encodeURIComponent(applicant)}&applyTime=${encodeURIComponent(applyTime)}`)
      loading2.close()
      if (res2.data && res2.data.code === 0) {
        ElMessage.error('生成组织机构凭证失败：' + (res2.data?.msg || res2.data?.message || '未知错误'))
      } else if (res2.data && res2.data.code === 1) {
        // 更新当前组的组织机构凭证状态
        const groupIndex = currentPage.value - 1
        const group = groupData.value[groupIndex]
        if (group) {
          group.orgCredentialStatus = true
        }
        
        await ElMessageBox.confirm('组织机构凭证生成成功，是否发送共享证书给使用方？', '提示', {
          confirmButtonText: '发送',
          cancelButtonText: '取消',
          type: 'info',
        })
        // 第三步：发送共享证书
        const loading3 = ElLoading.service({ fullscreen: true, text: '正在发送共享证书...' })
        const res3 = await axios.post('http://localhost:8082/api/send-sc-to-du')
        loading3.close()
        if (res3.data && res3.data.code === 0) {
          ElMessage.error('发送共享证书失败：' + (res3.data?.msg || res3.data?.message || '未知错误'))
        } else if (res3.data && res3.data.code === 1) {
          ElMessage.success('共享证书已成功发送给使用方！')
        } else {
          ElMessage.error('发送共享证书失败：' + (res3.data?.msg || res3.data?.message || '未知错误'))
        }
      } else {
        ElMessage.error('生成组织机构凭证失败：' + (res2.data?.msg || res2.data?.message || '未知错误'))
      }
    } else {
      loading.close()
      ElMessage.error('解密校验失败：' + (res1.data?.msg || res1.data?.message || '未知错误'))
    }
  } catch (err) {
    loading.close()
    if (err && err.message && err.message.includes('cancel')) {
      ElMessage.info('操作已取消')
    } else {
      ElMessage.error('操作失败：' + (err?.response?.data?.msg || err?.message || '未知错误'))
    }
  }
}
function getStatusText(row) {
  if (row.orgCredentialStatus === true) return '治理方已生成组织机构凭证'
  return '待治理方生成组织机构凭证'
}
function getStatusTagType(status) {
  if (!status) return 'info'
  if (status.includes('已')) return 'success'
  if (status.includes('待')) return 'danger'
  return 'info'
}
</script>