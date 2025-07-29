<template>
  <div class="directory-container">
    
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
    </div>
    
    <div class="directory-table">
      <div class="directory-table-header" style="display: flex; justify-content: flex-end; align-items: center; margin-bottom: 10px;">
        <el-button type="primary" plain @click="handleApply">申请</el-button>
        <el-button type="primary" plain @click="handleDecrypt">解密</el-button>
      </div>
      <div v-if="loading" class="loading-container">
        <el-loading :fullscreen="false" text="加载数据中..." />
      </div>
      <div v-else>
        <el-table 
          :data="filteredTableData" 
          border 
          stripe 
          style="width: 100%"
          max-height="300px"
          @selection-change="handleSelectionChange"
          ref="directoryTableRef"
          :row-key="row => row.id"
        >
          <el-table-column type="selection" width="35" />
          <el-table-column prop="id" label="ID" width="320" show-overflow-tooltip>
            <template #default="scope">
              <div class="id-cell">{{ scope.row.id }}</div>
            </template>
          </el-table-column>
          <el-table-column prop="entity" label="实体" width="150">
            <template #default="scope">
              <span class="entity-text">{{ scope.row.entity }}</span>
            </template>
          </el-table-column>
          <!-- <el-table-column label="约束条件" min-width="250" show-overflow-tooltip>
            <template #default="scope">
              <div class="constraint-text">
                {{ formatConstraints(scope.row.constraint) }}
              </div>
            </template>
          </el-table-column> -->
          <el-table-column label="传输控制操作" width="180">
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
          <el-table-column prop="status" label="状态" width="80">
            <template #default="scope">
              <el-tag type="success" effect="plain">{{ scope.row.status }}</el-tag>
            </template>
          </el-table-column>
          <!-- <el-table-column label="申请状态" width="200">
            <template #default="scope">
              <el-tag :type="getApplyStatusTagType(scope.row)">{{ getApplyStatusText(scope.row) }}</el-tag>
            </template>
          </el-table-column> -->
        </el-table>
        
        <div class="pagination-area">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[5, 10, 15, 20]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="totalCount"
            @size-change="handleSizeChange"
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
const loading = ref(false)
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const selectedRows = ref([])

// 计算属性：过滤后的表格数据
const filteredTableData = computed(() => {
  let result = tableData.value.filter(item => item.status === '已合格')
  
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

        else if (typeof item.constraint === 'object') {
          try {
            const entries = Object.entries(item.constraint)
            if (entries.some(([key, value]) => 
              (key && key.toLowerCase().includes(keyword)) || 
              (value && value.toString().toLowerCase().includes(keyword))
            )) {
              return true;
            }
          } catch (e) {
            console.error('搜索约束条件对象失败:', e)
          }
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
  
  const startIndex = (currentPage.value - 1) * pageSize.value
  const endIndex = startIndex + pageSize.value
  return result.slice(startIndex, endIndex)
})


const totalCount = computed(() => {
  const qualified = tableData.value.filter(item => item.status === '已合格')
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    return qualified.filter(item => {

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

        else if (typeof item.constraint === 'object') {
          try {
            const entries = Object.entries(item.constraint)
            if (entries.some(([key, value]) => 
              (key && key.toLowerCase().includes(keyword)) || 
              (value && value.toString().toLowerCase().includes(keyword))
            )) {
              return true;
            }
          } catch (e) {
            console.error('搜索约束条件对象失败:', e)
          }
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
    }).length
  }
  return qualified.length
})


const formatConstraints = (constraints) => {
  if (!constraints) return '-'
  

  if (Array.isArray(constraints)) {

    if (constraints.some(item => typeof item === 'string' && item.includes(':'))) {
      return constraints.join(', ')
    }
    
    return constraints.join(', ')
  }
  

  if (typeof constraints === 'string') {

    if (constraints.includes(',') && !constraints.includes(', ')) {
      return constraints.replace(/,/g, ', ')
    }
    return constraints
  }
  

  if (typeof constraints === 'object') {
    try {
      const entries = Object.entries(constraints)
      if (entries.length > 0) {
        return entries.map(([key, value]) => `${key}: ${value}`).join(', ')
      }
    } catch (e) {
      console.error('格式化约束条件对象失败:', e)
    }
  }
  

  return String(constraints)
}

// 页码变化处理
const handleCurrentChange = (val) => {
  currentPage.value = val
}

// 每页显示数量变化处理
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
}


const handleViewDetail = (row) => {
  emit('view-detail', row)
}


const fetchData = async () => {
  loading.value = true
  try {
    const response = await axios.get('http://localhost:8083/api/simplified-objects')
    
    if (response.data && response.data.code === 1 && response.data.data) {
      let dataArray = response.data.data
      
      if (dataArray.length > 0) {
        dataArray = dataArray.map(item => {
          // 防止空值或非对象类型
          if (!item || typeof item !== 'object') return item
          
          // 处理实体信息
          if (item.dataEntity && item.dataEntity.entity) {
            item.entity = item.dataEntity.entity;
          }

          // 处理状态信息
          if (item.dataEntity && item.dataEntity.status) {
            item.status = item.dataEntity.status;
          }

          // 处理约束条件信息 - 简化版本暂时设置为空
          item.constraint = [];

          // 处理传输控制操作 - 解析propagationControlJson
          if (item.propagationControlJson) {
            try {
              const propagationControl = JSON.parse(item.propagationControlJson);
              const controls = [];
              if (propagationControl.canRead === true || 
                  (propagationControl.selectedOperations && 
                  propagationControl.selectedOperations.read === true)) {
                controls.push('可读');
              }
              if (propagationControl.canShare === true || 
                  (propagationControl.selectedOperations && 
                  propagationControl.selectedOperations.share === true)) {
                controls.push('可共享');
              }
              if (propagationControl.canModify === true || 
                  (propagationControl.selectedOperations && 
                  propagationControl.selectedOperations.modify === true)) {
                controls.push('可修改');
              }
              if (propagationControl.canDestroy === true || 
                  (propagationControl.selectedOperations && 
                  propagationControl.selectedOperations.destroy === true)) {
                controls.push('可销毁');
              }
              if (propagationControl.canDelegate === true || 
                  (propagationControl.selectedOperations && 
                  propagationControl.selectedOperations.delegate === true)) {
                controls.push('可委托');
              }
              if (controls.length > 0) {
                item.transferControl = controls;
              }
            } catch (e) {
              console.error('解析propagationControlJson失败:', e);
              item.transferControl = [];
            }
          }
          
          if (item.qualified === true || 
              item.isQualified === true || 
              (item.status && (item.status === 'QUALIFIED' || 
                             item.status === 'qualified' ||
                             item.status === '合格' ||
                             item.status === '已合格' ||
                             item.status === '通过' ||
                             item.status === 'pass' ||
                             item.status === 'PASS'))) {
            item.status = '已合格'
          }
          
          return item
        })
        
        tableData.value = dataArray
        assignRandomApplyStatus()
        
        const qualifiedCount = dataArray.filter(item => item.status === '已合格').length
        
        if (qualifiedCount > 0) {
          ElMessage.success(`成功获取${dataArray.length}条数据，其中${qualifiedCount}条已合格`)
        } else {
          ElMessage.warning('未找到已合格的数据')
        }
      } else {
        // ElMessage.warning('未找到可用的数据对象')
      }
    } else {
      ElMessage.warning('API返回的数据格式不正确')
    }

  } catch (error) {
    console.error('获取数据失败:', error)
    ElMessage.error(`获取数据失败: ${error.message}`)
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




function assignRandomApplyStatus() {
  tableData.value.forEach((item, idx) => {
     if (idx === 0) {
       item.applyStatus = '治理方已同意,请解密查看'
      } else if (idx === 1) {
        item.applyStatus = '数源方已同意,等待治理方处理'
      } else {
       item.applyStatus = '拒绝申请'
     }
   })
}





function handleSelectionChange(val) {
  selectedRows.value = val
}

// 移除行选择限制
// function isRowSelectable(row) {
//   return row.sourceAgreed === true && row.governanceAgreed === true;
// }

function handleApply() {
  if (selectedRows.value.length === 0) {
    ElMessage.info('请先勾选要申请的数据对象')
    return
  }
  
  loading.value = true
  const ids = selectedRows.value.map(row => row.id).join(',')
  
  axios.get(`http://localhost:8083/api/selectIds?ids=${encodeURIComponent(ids)}`, { withCredentials: true })
    .then(res => {
      console.log('申请接口返回结果:', res.data)
      ElMessage.success('申请成功')
      
      selectedRows.value.forEach(row => {
        row.applied = true
        row.applyStatus = '待处理'
      })
    })
    .catch(err => {
      console.error('申请接口出错:', err)
      ElMessage.error(`请勿重复申请`)
    })
    .finally(() => {
      loading.value = false
    })
}

function handleDecrypt() {
  if (selectedRows.value.length === 0) {
    ElMessage.info('请先勾选要解密的数据对象')
    return
  }
  
  loading.value = true
  const ids = selectedRows.value.map(row => row.id)
  
  axios.post('http://localhost:8083/api/decrypt', { ids }, { withCredentials: true })
    .then(res => {
      console.log('解密接口返回结果:', res.data)
      
      // 检查新的响应格式
      if (res.data && res.data.code === 1 && res.data.data) {
        ElMessage.success('解密成功')
        // 传递解密后的完整数据给父组件
        emit('show-decrypt', { ids, decryptedData: res.data.data })
      } else {
        ElMessage.error(`解密失败: ${res.data?.msg || '未知错误'}`)
      }
    })
    .catch(err => {
      console.error('解密接口出错:', err)
      ElMessage.error(`解密失败: ${err.response?.data?.message || err.message}`)
    })
    .finally(() => {
      loading.value = false
    })
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

.pagination-area {
  margin-top: 16px;
  display: flex;
  justify-content: center;
}
</style>