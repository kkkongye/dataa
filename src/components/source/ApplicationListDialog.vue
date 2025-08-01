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
      <el-table-column prop="objectId" label="ID" min-width="240"  align="center"/>
      <el-table-column prop="entity" label="实体名" min-width="120" align="center" />
      <el-table-column prop="applicant" label="申请人" min-width="120" align="center" />
      <el-table-column prop="applyTime" label="申请时间" min-width="160" align="center" />
      <el-table-column label="操作" width="160" align="center">
        <template #default="scope">
          <el-button 
            type="primary" 
            plain 
            @click="handleGenerateDV" 
            size="small"
            :disabled="scope.row.dataCredentialStatus === true"
          >
            生成数据凭证
          </el-button>
        </template>
      </el-table-column>
      <el-table-column label="申请状态" min-width="160" align="center">
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
      return response.data.data
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
          groupId: `组${String(index + 1).padStart(3, '0')}`,
          applyTime: record.applyTime ? new Date(record.applyTime).toLocaleString('zh-CN') : '-',
          originalApplyTime: record.applyTime,
          dataCredentialStatus: record.sourceAgreed || false,
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
    dataCredentialStatus: group.dataCredentialStatus
  }))
})

const tableData = ref([])

// 单元格合并方法
const spanMethod = ({ row, column, rowIndex, columnIndex }) => {
  // 组序号列(第0列)、用户名列(第3列)、操作列(第4列)、申请状态列(第5列)需要合并
  if (columnIndex === 0 || columnIndex === 3 || columnIndex === 4 || columnIndex === 5|| columnIndex === 6) {
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

// 生成数据凭证方法
const handleGenerateDV = async () => {
  try {
    // 获取当前组的申请信息
    const groupIndex = currentPage.value - 1
    const group = groupData.value[groupIndex]
    if (!group || !group.applications || group.applications.length === 0) {
      ElMessage.error('无法获取申请信息')
      return
    }
    
    const applicant = group.applications[0].applicant
    const applyTime = group.originalApplyTime || group.applyTime
    
    if (!applicant || !applyTime) {
      ElMessage.error('申请人或申请时间信息缺失')
      return
    }
    
    // 直接使用接口返回的原始时间，不进行任何转换
    const originalApplyTime = applyTime
    
    if (!originalApplyTime) {
      ElMessage.error('申请时间不能为空')
      return
    }
    
    // 显示加载状态
    const loadingInstance = ElLoading.service({
      fullscreen: true,
      text: '正在生成数据凭证...',
      background: 'rgba(0, 0, 0, 0.7)'
    });
    
    // 调用生成数据凭证接口，传递申请人和申请时间参数
    const response = await axios.post(`http://localhost:8081/api/generate-dv?applicant=${encodeURIComponent(applicant)}&applyTime=${encodeURIComponent(originalApplyTime)}`);
    
    loadingInstance.close();
    
    if (response.data && response.data.code === 0 && response.data.msg && response.data.msg.includes("生成DV失败")) {
      // 生成DV失败的情况，直接显示错误信息，不弹出加密确认框
      ElMessage.error(response.data.msg || '生成DV失败');
    } else if (response.data && (response.data.code === 1 || response.data.success === true)) {
      // 更新当前组的数据凭证状态
      const groupIndex = currentPage.value - 1
      const group = groupData.value[groupIndex]
      if (group) {
        group.dataCredentialStatus = true
      }
      
      // 生成凭证成功后询问是否加密并签名
      ElMessageBox.confirm('数据凭证生成成功，是否加密签名并发送?', '确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info',
      }).then(async () => {
        try {
          const encryptLoading = ElLoading.service({
            fullscreen: true,
            text: '正在加密并签名...',
            background: 'rgba(0, 0, 0, 0.7)'
          });
          
          // 调用加密并签名接口
          const encryptResponse = await axios.post('http://localhost:8081/api/encrypt-and-send');
          
          encryptLoading.close();
          
          if (encryptResponse.data && encryptResponse.data.code === 0 && encryptResponse.data.msg && encryptResponse.data.msg.includes("加密发送失败")) {
            // 加密发送失败的特殊错误处理
            ElMessage.error(encryptResponse.data.msg);
          } else if (encryptResponse.data && (encryptResponse.data.code === 1 || encryptResponse.data.success === true)) {
            ElMessage.success('加密并签名成功');
          } else {
            ElMessage.warning(`加密失败: ${encryptResponse.data?.message || encryptResponse.data?.msg || '未知错误'}`);
          }
        } catch (encryptError) {
          console.error('加密并签名失败:', encryptError);
          
          if (encryptError.response) {
            if (encryptError.response.status === 404) {
              ElMessage.error('加密服务未启动或接口不存在');
            } else if (encryptError.response.status === 500) {
              ElMessage.error(`加密服务错误: ${encryptError.response.data?.message || '内部服务器错误'}`);
            } else {
              ElMessage.error(`加密失败 (${encryptError.response.status}): ${encryptError.response.data?.message || encryptError.message}`);
            }
          } else if (encryptError.request) {
            ElMessage.error('无法连接到加密服务，请确保服务已启动');
          } else {
            ElMessage.error(`加密并签名失败: ${encryptError.message || '未知错误'}`);
          }
        }
      }).catch(() => {
        // 用户取消加密操作
        ElMessage.info('已取消加密操作');
      });
    } else {
      if (response.data && response.data.code === 0) {
        ElMessage.error('生成DV失败');
      } else {
        ElMessage.warning(`生成数据凭证失败: ${response.data?.message || response.data?.msg || '未知错误'}`);
      }
    }
  } catch (error) {
    console.error('生成数据凭证失败:', error);
    
    if (error.response) {
      if (error.response.status === 404) {
        ElMessage.error('凭证服务未启动或接口不存在');
      } else if (error.response.status === 500) {
        ElMessage.error(`凭证服务错误: ${error.response.data?.message || '内部服务器错误'}`);
      } else {
        ElMessage.error(`生成失败 (${error.response.status}): ${error.response.data?.message || error.message}`);
      }
    } else if (error.request) {
      ElMessage.error('无法连接到凭证服务，请确保服务已启动');
    } else {
      ElMessage.error(`生成数据凭证失败: ${error.message || '未知错误'}`);
    }
  }
}
function getStatusText(row) {
  if (row.dataCredentialStatus === true) return '数源方已生成数据凭证'
  return '待数源方生成数据凭证'
}
function getStatusTagType(status) {
  if (!status) return 'info'
  if (status.includes('已')) return 'success'
  if (status.includes('待')) return 'danger'
  return 'info'
}
</script>