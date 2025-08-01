<template>
  <div class="directory-container">
<!--     
    <div class="search-bar">
      <el-input 
        v-model="searchKeyword" 
        placeholder="搜索ID、实体、约束条件、传输控制操作" 
        clearable
        class="search-input"
      >
        <template #suffix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
    </div> -->
    
    <div class="directory-table">

      <div v-if="loading" class="loading-container">
        <el-loading :fullscreen="false" text="加载数据中..." />
      </div>
      <div v-else>
        <el-table 
          :data="filteredTableData" 
          border 
          stripe 
          style="width: 100%"
          max-height="650px"
          ref="directoryTableRef"
          :row-key="row => row.id"
          :span-method="spanMethod"
        >
          <el-table-column prop="id" label="ID" width="320" show-overflow-tooltip>
            <template #default="scope">
              <div class="id-cell">{{ scope.row.id }}</div>
            </template>
          </el-table-column>
          <el-table-column prop="entity" label="实体" width="180">
            <template #default="scope">
              <span class="entity-text">{{ scope.row.entity }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="constraint" label="约束条件" width="400" align="center">
            <template #default="scope">
              <div class="constraint-container">
                <template v-if="scope.row.constraint && scope.row.constraint.length">
                  <div 
                    v-for="(_, rowIndex) in Math.ceil((Array.isArray(scope.row.constraint) ? scope.row.constraint : [scope.row.constraint]).length / 2)" 
                    :key="rowIndex"
                    class="constraint-row"
                  >
                    <!-- 第一项 -->
                    <div class="constraint-item-pair">
                      <span v-if="(Array.isArray(scope.row.constraint) ? scope.row.constraint : [scope.row.constraint])[rowIndex * 2]" 
                            v-html="formatConstraintText((Array.isArray(scope.row.constraint) ? scope.row.constraint : [scope.row.constraint])[rowIndex * 2])"></span>
                    </div>
                    
                    <!-- 第二项 -->
                    <div class="constraint-item-pair">
                      <span v-if="(Array.isArray(scope.row.constraint) ? scope.row.constraint : [scope.row.constraint])[rowIndex * 2 + 1]" 
                            v-html="formatConstraintText((Array.isArray(scope.row.constraint) ? scope.row.constraint : [scope.row.constraint])[rowIndex * 2 + 1])"></span>
                    </div>
                  </div>
                </template>
                <template v-else>-</template>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="传输控制操作" width="200">
            <template #default="scope">
              <div class="control-container">
                <template v-if="scope.row.transferControl && scope.row.transferControl.length">
                  <el-tag
                    v-for="(item, index) in (Array.isArray(scope.row.transferControl) ? scope.row.transferControl : [scope.row.transferControl])"
                    :key="index"
                    size="small"
                    type="primary"
                    effect="plain"
                    class="control-tag"
                  >
                    {{ item }}
                  </el-tag>
                </template>
                <template v-else>-</template>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" align="center">
            <template #default="scope">
              <div class="operation-buttons">
                <el-button type="primary" plain @click="handleApplyForGroup" size="small">申请</el-button>
                <el-button type="success" plain @click="handleDecryptForGroup" size="small">解密</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
        
        <div class="pagination-area">
          <div class="group-info" style="margin-bottom: 10px; text-align: center; color: #606266;">
            <span v-if="currentGroup">当前显示：{{ currentGroup.groupId }} - 申请时间：{{ currentGroup.applyTime }}</span>
          </div>
          <el-pagination
            v-model:current-page="currentPage"
            :page-size="1"
            layout="total, prev, pager, next, jumper"
            :total="totalCount"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import axios from 'axios'

// 属性定义
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['close', 'view-detail', 'show-decrypt'])

const tableData = ref([])
const groupData = ref([])
const loading = ref(false)
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(1) // 每页显示一组
const selectedRows = ref([])

// 当前显示的组
const currentGroup = computed(() => {
  const groupIndex = currentPage.value - 1
  return groupData.value[groupIndex] || null
})

// 当前组的实体数据（用于表格显示）
const filteredTableData = computed(() => {
  if (!currentGroup.value) return []
  
  let result = currentGroup.value.entities || []
  
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(item => {
      if ((item.id && item.id.toString().toLowerCase().includes(keyword)) || 
          (item.entity && item.entity.toLowerCase().includes(keyword))) {
        return true;
      }
      
      if (item.constraint) {
        if (Array.isArray(item.constraint)) {
          if (item.constraint.some(constraint => 
            constraint && constraint.toString().toLowerCase().includes(keyword)
          )) {
            return true;
          }
        } 
        else if (typeof item.constraint === 'string' && 
                item.constraint.toLowerCase().includes(keyword)) {
          return true;
        }
      }
      
      if (item.transferControl) {
        if (Array.isArray(item.transferControl)) {
          if (item.transferControl.some(control => 
            control && control.toString().toLowerCase().includes(keyword)
          )) {
            return true;
          }
        }
        else if (typeof item.transferControl === 'string' && 
                item.transferControl.toLowerCase().includes(keyword)) {
          return true;
        }
      }
      
      return false;
    });
  }
  
  return result
})


const totalCount = computed(() => {
  return groupData.value.length
})




// 页码变化处理
const handleCurrentChange = (val) => {
  currentPage.value = val
  // 清空选择
  selectedRows.value = []
}

// 格式化约束条件文本
const formatConstraintText = (text) => {
  if (!text) return text
  
  if (text.includes(':')) {
    const parts = text.split(':')
    return `<span class="constraint-prefix">${parts[0]}:</span>${parts[1]}`
  }
  
  return text
}


const handleViewDetail = (row) => {
  emit('view-detail', row)
}


// 获取申请记录数据
const fetchApplicationRecords = async () => {
  try {
    const response = await axios.get('http://localhost:8083/api/application-records')
    
    if (response.data && response.data.code === 1 && response.data.data) {

      const currentUsername = localStorage.getItem('username')
      let filteredRecords = response.data.data
      if (currentUsername) {
        filteredRecords = filteredRecords.filter(record => record.applicant === currentUsername)
      }
      return filteredRecords.filter(record => 
        record.sourceAgreed === true && 
        record.governanceAgreed1 === true && 
        record.governanceAgreed2 === true
      )
    } else {
      console.warn('获取申请记录失败:', response.data?.msg || '未知错误')
      return []
    }
  } catch (error) {
    console.error('获取申请记录失败:', error)
    ElMessage.error('获取申请记录失败')
    return []
  }
}

// 获取对象详情数据
const fetchObjectDetails = async (objectIds) => {
  try {
    const response = await axios.get('http://localhost:8081/api/objects/list')
    
    if (response.data && response.data.code === 1 && response.data.data) {
      const allObjects = response.data.data
      // 根据objectIds的顺序筛选对象，保持原有顺序
      const orderedObjects = objectIds.map(id => {
        return allObjects.find(obj => obj.id === id)
      }).filter(obj => obj !== undefined) // 过滤掉未找到的对象
      return orderedObjects
    } else {
      console.warn('获取对象详情失败:', response.data?.msg || '未知错误')
      return []
    }
  } catch (error) {
    console.error('获取对象详情失败:', error)
    return []
  }
}

// 格式化约束条件
const formatConstraints = (constraintSet) => {
  if (!constraintSet || !constraintSet.constraints || !constraintSet.constraints.length) {
    return []
  }
  
  const constraint = constraintSet.constraints[0]
  return [
    `格式约束: ${constraint.formatConstraint || '-'}`,
    `访问约束: ${constraint.accessConstraint || '-'}`,
    `路径约束: ${constraint.pathConstraint || '-'}`,
    `区域约束: ${constraint.regionConstraint || '-'}`,
    `共享约束: ${constraint.shareConstraint || '-'}`
  ]
}

// 格式化传输控制操作
const formatTransferControl = (propagationControl) => {
  if (!propagationControl || !propagationControl.selectedOperations) {
    return []
  }
  
  const operations = propagationControl.selectedOperations
  const controls = []
  
  if (operations.read) controls.push('可读取')
  if (operations.modify) controls.push('可修改')
  if (operations.share) controls.push('可共享')
  if (operations.delegate) controls.push('可委托')
  if (operations.destroy) controls.push('可销毁')
  
  return controls
}

// 加载数据
const fetchData = async () => {
  try {
    loading.value = true
    
    // 获取申请记录
    const applicationRecords = await fetchApplicationRecords()
    
    if (applicationRecords.length === 0) {
      groupData.value = []
      ElMessage.info('暂无已完成审批的申请记录')
      return
    }
    
    // 处理每个申请记录
    const processedGroups = await Promise.all(
      applicationRecords.map(async (record, index) => {
        // 解析objectIds
        const objectIds = record.objectIds ? record.objectIds.split(',') : []
        
        // 获取对象详情
        const objectDetails = await fetchObjectDetails(objectIds)
        
        // 构建实体数据
        const entities = objectDetails.map(obj => ({
          id: obj.id,
          entity: obj.dataEntity?.entity || '未知实体',
          constraint: formatConstraints(obj.constraintSet),
          transferControl: formatTransferControl(obj.propagationControl),
          status: obj.dataEntity?.status || '未知状态'
        }))
        
        return {
          groupId: `组${String(index + 1).padStart(3, '0')}`,
          applyTime: record.applyTime ? new Date(record.applyTime).toLocaleString('zh-CN') : '-',
          applicant: record.applicant || '未知申请方',
          entities: entities
        }
      })
    )
    
    groupData.value = processedGroups
    
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}


onMounted(() => {
  fetchData()
})


watch(() => props.visible, (newValue) => {
  if (newValue) {
    fetchData()
  }
})


// function handleSelectionChange(val) {
//   selectedRows.value = val
// }

// function handleApply() {
//   if (selectedRows.value.length === 0) {
//     ElMessage.info('请先勾选要申请的数据对象')
//     return
//   }
  
//   loading.value = true
//   const ids = selectedRows.value.map(row => row.id).join(',')
  
//   axios.get(`http://localhost:8083/api/selectIds?ids=${encodeURIComponent(ids)}`, { withCredentials: true })
//     .then(res => {
//       console.log('申请接口返回结果:', res.data)
//       ElMessage.success('申请成功')
      
//       selectedRows.value.forEach(row => {
//         row.applied = true
//         row.applyStatus = '待处理'
//       })
//     })
//     .catch(err => {
//       console.error('申请接口出错:', err)
//       ElMessage.error(`请勿重复申请`)
//     })
//     .finally(() => {
//       loading.value = false
//     })
// }

// function handleDecrypt() {
//   if (selectedRows.value.length === 0) {
//     ElMessage.info('请先勾选要解密的数据对象')
//     return
//   }
  
//   loading.value = true
//   const ids = selectedRows.value.map(row => row.id)
  
//   axios.post('http://localhost:8083/api/decrypt', { ids }, { withCredentials: true })
//     .then(res => {
//       console.log('解密接口返回结果:', res.data)
      
//       // 检查新的响应格式
//       if (res.data && res.data.code === 1 && res.data.data) {
//         ElMessage.success('解密成功')
//         // 传递解密后的完整数据给父组件
//         emit('show-decrypt', { ids, decryptedData: res.data.data })
//       } else {
//         ElMessage.error(`解密失败: ${res.data?.msg || '未知错误'}`)
//       }
//     })
//     .catch(err => {
//       console.error('解密接口出错:', err)
//       ElMessage.error(`解密失败: ${err.response?.data?.message || err.message}`)
//     })
//     .finally(() => {
//       loading.value = false
//     })
// }

// 为当前组申请
function handleApplyForGroup() {
  if (!currentGroup.value || !currentGroup.value.entities || currentGroup.value.entities.length === 0) {
    ElMessage.info('当前组没有可申请的数据对象')
    return
  }
  
  loading.value = true
  const ids = currentGroup.value.entities.map(row => row.id).join(',')
  
  axios.get(`http://localhost:8083/api/selectIds?ids=${encodeURIComponent(ids)}`, { withCredentials: true })
    .then(res => {
      console.log('申请接口返回结果:', res.data)
      
      // 检查返回的code，如果为0则表示错误
      if (res.data && res.data.code === 0) {
        ElMessage.error(res.data.msg || '申请失败')
        return
      }
      
      ElMessage.success('申请成功')
      
      currentGroup.value.entities.forEach(row => {
        row.applied = true
        row.applyStatus = '待处理'
      })
    })
    .catch(err => {
      console.error('申请接口出错:', err)
      ElMessage.error('网络请求失败，请稍后重试')
    })
    .finally(() => {
      loading.value = false
    })
}

// 为当前组解密
function handleDecryptForGroup() {
  if (!currentGroup.value || !currentGroup.value.entities || currentGroup.value.entities.length === 0) {
    ElMessage.info('当前组没有可解密的数据对象')
    return
  }
  
  loading.value = true
  const ids = currentGroup.value.entities.map(row => row.id)
  
  axios.post('http://localhost:8083/api/decrypt', {}, { withCredentials: true })
    .then(res => {
      console.log('解密接口返回结果:', res.data)
      
      // 检查新的响应格式
      if (res.data && res.data.code === 1 && res.data.data) {
        ElMessage.success('解密成功')
        // 传递解密后的完整数据给父组件
        emit('show-decrypt', { ids, decryptedData: res.data.data })
      } else {
        ElMessage.error(`解密失败,请等待治理方生成发送数据胶囊`)
      }
    })
    .catch(err => {
      console.error('解密接口出错:', err)
      ElMessage.error(`解密失败,请等待治理方生成发送数据胶囊`)
    })
    .finally(() => {
      loading.value = false
    })
}

// 单元格合并方法
function spanMethod({ row, column, rowIndex, columnIndex }) {
  // 只对操作列（最后一列）进行合并
  if (column.label === '操作') {
    const currentGroupId = row.groupId
    const data = filteredTableData.value
    
    // 找到当前组的第一行索引
    const firstRowIndex = data.findIndex(item => item.groupId === currentGroupId)
    
    // 如果当前行是该组的第一行，计算该组的行数
    if (rowIndex === firstRowIndex) {
      const groupRowCount = data.filter(item => item.groupId === currentGroupId).length
      return {
        rowspan: groupRowCount,
        colspan: 1
      }
    } else {
      // 如果不是第一行，则隐藏该单元格
      return {
        rowspan: 0,
        colspan: 0
      }
    }
  }
  
  // 其他列不合并
  return {
    rowspan: 1,
    colspan: 1
  }
}
</script>

<style scoped>
.directory-container {
  padding: 20px;
}

.directory-header {
  margin-bottom: 20px;
  text-align: center;
}

.directory-header h3 {
  margin: 0;
  font-size: 20px;
  color: #303133;
}

.directory-info {
  color: #909399;
  font-size: 14px;
  margin-top: 8px;
}

.search-bar {
  margin-bottom: 16px;
}

.search-input {
  width: 300px;
}

.directory-table {
  margin-bottom: 16px;
  position: relative;
  min-height: 200px;
}

.loading-container {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgba(255, 255, 255, 0.6);
}

.constraint-text {
  text-align: left;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: normal;
  word-break: break-all;
  line-height: 1.5;
  padding: 5px 8px;
}

.id-cell {
  text-align: center;
  word-break: break-all;
  padding: 0 2px;
}

.entity-text {
  color: #606266;
  font-size: 14px;
  padding: 0 5px;
}

.control-container {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 6px;
}

.control-tag {
  margin: 2px;
}

.constraint-container {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 4px;
}

.constraint-row {
  display: flex;
  justify-content: space-between;
  gap: 16px;
}

.constraint-item-pair {
  flex: 1;
  text-align: left;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  padding-left: 15px;
}

.operation-buttons {
  display: flex;
  gap: 8px;
  justify-content: center;
  align-items: center;
}

.constraint-prefix {
  font-weight: bold;
  color: #409EFF;
}

.pagination-area {
  margin-top: 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.group-info {
  font-size: 14px;
  font-weight: 500;
}
</style>