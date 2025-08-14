<template>
  <div class="datasource-container watermark-bg">
    <!-- 头部导航 -->
    <AppHeader @logout="logout" />
    
    <!-- 主内容区域 -->
    <div class="main-content">
      <!-- 标签页 -->
      <div class="content-card">
        <div class="table-title">我的数据对象列表</div>
        <!-- 搜索和操作区 -->
        <div class="action-bar">
          <div v-if="isDecrypted" class="search-area">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索实体名、约束条件、传输控制操作"
              class="search-input"
            >
              <template #suffix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </div>
          <div class="action-buttons">
            <el-button type="success" plain @click="handleRefresh" :loading="loading">
              <el-icon><Refresh /></el-icon>
              刷新数据
            </el-button>
            <el-button type="danger" plain @click="clearDatabase"><el-icon><Delete /></el-icon>清空使用方数据库</el-button>
            <el-button type="primary" plain @click="showVisualization" class="visualization-btn">
              <el-icon><DataAnalysis /></el-icon>
              三维数据可视化
            </el-button>
            <!-- <el-button type="primary" plain @click="handleVerifySC"> 验证机构凭证</el-button> -->
            <el-button type="info" plain @click="showDirectoryDialog">可解密的数据对象目录</el-button>
            <!-- <el-button v-if="isDecrypted" type="warning" plain @click="resetDecryption">重新解密</el-button> -->
            <el-button type="warning" plain @click="goToUserMain">
              <el-icon><Back /></el-icon>
              返回查看请求的数据对象
            </el-button>
          </div>
        </div>
        
        <!-- 数据表格 -->
        <div class="table-container">
          <el-table
            :data="filteredTableData"
            style="width: 100%"
            @selection-change="handleSelectionChange"
            border
            height="100%"
            fit
            :row-style="{ height: '45px' }"
            :header-cell-style="headerCellStyle"
            v-loading="loading"
            element-loading-text="正在加载数据..."
          >
            <el-table-column prop="entity" label="实体" width="260" align="center">
              <template #default="scope">
                <el-link type="primary" @click="previewEntity(scope.row)" class="entity-link">{{ scope.row.entity }}</el-link>
              </template>
            </el-table-column>
            <el-table-column prop="metadata" label="元数据信息" min-width="250" align="center">
              <template #default="scope">
                <div class="metadata-container">
                  <template v-if="scope.row.metadata && Object.keys(scope.row.metadata).length">
                    <div 
                      v-for="(_, rowIndex) in Math.ceil(Object.entries(scope.row.metadata).filter(([key, value]) => key !== 'headers' && value !== null && value !== undefined && value !== '').length / 2)" 
                      :key="rowIndex"
                      class="metadata-row"
                    >
                      <!-- 第一项 -->
                      <div class="metadata-item-pair">
                        <span v-if="Object.entries(scope.row.metadata).filter(([key, value]) => key !== 'headers' && value !== null && value !== undefined && value !== '')[rowIndex * 2]" 
                              v-html="formatMetadataText(Object.entries(scope.row.metadata).filter(([key, value]) => key !== 'headers' && value !== null && value !== undefined && value !== '')[rowIndex * 2])"></span>
                      </div>
                      
                      <!-- 第二项 -->
                      <div class="metadata-item-pair">
                        <span v-if="Object.entries(scope.row.metadata).filter(([key, value]) => key !== 'headers' && value !== null && value !== undefined && value !== '')[rowIndex * 2 + 1]" 
                              v-html="formatMetadataText(Object.entries(scope.row.metadata).filter(([key, value]) => key !== 'headers' && value !== null && value !== undefined && value !== '')[rowIndex * 2 + 1])"></span>
                      </div>
                    </div>
                  </template>
                  <template v-else>-</template>
                </div>
              </template>
            </el-table-column>
            <!-- <el-table-column prop="locationInfo" label="定位信息" min-width="200" align="center">
              <template #default="scope">
                <span v-if="getLocationInfoObj(scope.row.locationInfo, scope.row.locationInfoJson)">
                  ({{ getLocationInfoObj(scope.row.locationInfo, scope.row.locationInfoJson).databaseName || '-' }},
                  {{ getLocationInfoObj(scope.row.locationInfo, scope.row.locationInfoJson).tableName || '-' }},
                  <el-popover placement="top" trigger="click">
                    <template #reference>
                      <span class="select-fields-link" style="color:#409EFF;cursor:pointer;">"select字段"</span>
                    </template>
                    <div style="max-width:400px;word-break:break-all;">{{ getLocationInfoObj(scope.row.locationInfo, scope.row.locationInfoJson).selectFields }}</div>
                  </el-popover>
                  )
                </span>
                <span v-else>-</span>
              </template>
            </el-table-column> -->
            <el-table-column prop="constraint" label="约束条件" min-width="180" align="center">
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
            <el-table-column prop="transferControl" label="传输控制操作" min-width="140" align="center">
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
            
            <!-- 添加分类分级值列 -->
            <el-table-column prop="classificationLevelValue" label="分类分级值" width="180" align="center">
              <template #default="scope">
                <div class="classification-level-container">
                  <div class="classification-level-item">
                    <span class="value">{{ 
                      (() => {
                        const sum = (parseFloat(scope.row.totalCategoryValue) || 0) + (parseFloat(scope.row.totalGradeValue) || 0);
                        return sum === 0 ? '未生成分类分级值' : sum.toFixed(4);
                      })()
                    }}</span>
                  </div>
                  <!-- <div class="classification-level-item">
                    <span class="label">分级值：</span>
                    <span class="value">{{ scope.row.totalGradeValue || scope.row.levelValue || '未分级' }}</span>
                  </div> -->
                </div>
              </template>
            </el-table-column>

            <el-table-column prop="creatorName" label="数源方" width="150" align="center">
              <template #default="scope">
                <span class="creator-name">{{ scope.row.creatorName || '浙江省税务局' }}</span>
              </template>
            </el-table-column>
          </el-table>
        </div>
        
        <!-- 分页 -->
        <div class="pagination-area">
          <CommonPagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total-count="totalCount"
            :page-sizes="[5, 10, 20, 30, 50]"
            background
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </div>
  </div>
  

  <!-- 解密对话框 -->
  <el-dialog
    v-model="decryptDialogVisible"
    title="解密"
    width="500px"
    append-to-body
    destroy-on-close
    :close-on-click-modal="false"
    :show-close="true"
    draggable
    class="decrypt-dialog"
  >
    <div style="margin-bottom: 16px; font-size: 16px;">
      <span style="font-weight: bold;">数据对象ID：</span>
      <span style="word-break: break-all;">{{ decryptForm.objectId }}</span>
    </div>
    <template #footer>
      <el-button type="primary" plain @click="handleGenerateCapsule">获取封装的数据对象</el-button>
      <el-button type="primary" @click="handleDecrypt">确定</el-button>
      <el-button @click="decryptDialogVisible = false">取消</el-button>
    </template>
  </el-dialog>

  <!-- Excel预览对话框 -->
  <ObjectPreviewDialog
    v-model:visible="previewDialogVisible"
    :object="previewForm"
    :excelData="excelTableData"
  />

  <!-- 添加目录对话框 -->
  <el-dialog
    v-model="directoryDialogVisible"
    title="目录"
    width="75%"
    :close-on-click-modal="false"
    :show-close="true"
    draggable
    class="directory-dialog"
    destroy-on-close
  >
    <DirectoryTable 
      :visible="directoryDialogVisible"
      @close="directoryDialogVisible = false"
      @view-detail="handleViewDirectoryItem"
      @show-decrypt="showDecryptDialog"
    />
  </el-dialog>

  <!-- 添加三维可视化对话框组件 -->
  <VisualizationDialog v-model:visible="visualizationVisible" :source-page="'user'" />

  <!-- 构造共享证书申请对话框 -->
  <el-dialog v-model="scrDialogVisible" title="构造共享证书申请" width="500px">
    <el-form :model="scrForm" label-width="80px">
      <el-form-item label="metaData">
        <el-input v-model="scrForm.metaData" />
      </el-form-item>
      <el-form-item label="fno">
        <el-input v-model="scrForm.fno" />
      </el-form-item>
      <el-form-item label="sfno">
        <el-input v-model="scrForm.sfno" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="scrDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="submitScrForm">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, reactive, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus'
import { Search, Lock, Document, DataAnalysis, Back, Delete, Refresh } from '@element-plus/icons-vue'
import * as XLSX from 'xlsx'
import ExcelPreview from '@/components/ExcelPreview.vue'
import AppHeader from '@/components/AppHeader.vue'
import CommonPagination from '@/components/CommonPagination.vue'
import DirectoryTable from '@/components/user/DirectoryTable.vue'
import dataObjectService from '@/services/dataObjectService'
import { ensureArray, advancedSearch } from '@/utils/searchUtils';
import axios from 'axios'
import VisualizationDialog from '@/components/visualization/VisualizationDialog.vue'
import ObjectPreviewDialog from '@/components/ObjectPreviewDialog.vue'

const router = useRouter()
const activeTab = ref('objectList')
const currentStatus = ref('') // 默认显示全部数字对象
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(5) // 改为默认显示5条
const isDecrypted = ref(false)
const selectedRows = ref([])
const decryptedObjectId = ref('')


const tableData = ref([])
const loading = ref(false)

// 获取ID列表
const fetchObjectIds = async () => {
  try {
    const response = await axios.get('http://localhost:8083/api/objects')
    if (response.data && response.data.code === 1 && response.data.data) {
      return response.data.data
    }
    return []
  } catch (error) {
    console.error('获取对象ID列表失败:', error)
    return []
  }
}

// 获取对象详细信息
const fetchObjectDetails = async () => {
  try {
    const response = await axios.get('http://localhost:8081/api/objects/list1')
    if (response.data && response.data.code === 1 && response.data.data) {
      return response.data.data
    }
    return []
  } catch (error) {
    console.error('获取对象详细信息失败:', error)
    return []
  }
}

// 加载数据
const loadTableData = async () => {
  try {
    loading.value = true
    
    // 获取ID列表和详细信息
    const [idList, detailList] = await Promise.all([
      fetchObjectIds(),
      fetchObjectDetails()
    ])
    
    // 合并数据
    const mergedData = idList.map(idItem => {
      // 从详细信息中找到对应的对象
      const detailItem = detailList.find(detail => detail.id === idItem.id)
      
      if (detailItem) {
        return {
          id: idItem.id,
          entity: detailItem.dataEntity?.entity || '未知实体',
          // 从idItem获取分类分级值
          totalCategoryValue: idItem.totalCategoryValue,
          totalGradeValue: idItem.totalGradeValue,
          classificationValue: idItem.totalCategoryValue,
          levelValue: idItem.totalGradeValue,
          // 从detailItem获取其他信息
          constraint: extractConstraintArray(detailItem.constraintSet),
          constraintSet: detailItem.constraintSet || {},
          transferControl: extractTransferControlArray(detailItem.propagationControl),
          propagationControl: detailItem.propagationControl || {},
          metadata: detailItem.dataEntity?.metadata || {},
          dataItems: detailItem.dataEntity?.dataItems || [],
          status: detailItem.dataEntity?.status || '未知状态',
          feedback: detailItem.dataEntity?.feedback || '',
          locationInfo: detailItem.locationInfo || {},
          auditInfo: detailItem.auditInfo || {},
          creatorName: detailItem.creatorName || '浙江省税务局',
          createdAt: detailItem.createdAt || detailItem.dataEntity?.createdAt || idItem.createdAt
        }
      } else {
        // 如果没有找到详细信息，只使用ID信息
        return {
          id: idItem.id,
          entity: '未知实体',
          totalCategoryValue: idItem.totalCategoryValue,
          totalGradeValue: idItem.totalGradeValue,
          classificationValue: idItem.totalCategoryValue,
          levelValue: idItem.totalGradeValue,
          constraint: [],
          constraintSet: {},
          transferControl: extractTransferControlArray(idItem.propagationControl),
          propagationControl: idItem.propagationControl || {},
          metadata: {},
          dataItems: [],
          status: '未知状态',
          feedback: '',
          locationInfo: {},
          auditInfo: {},
          creatorName: '浙江省税务局',
          createdAt: idItem.createdAt
        }
      }
    })
    
    tableData.value = mergedData
    
    tableData.value.sort((a, b) => {
      const dateA = new Date(a.createdAt || 0)
      const dateB = new Date(b.createdAt || 0)
      return dateB - dateA // 降序排列，最新的在前面
    })
    
    console.log('数据加载完成，共', mergedData.length, '条记录')
    
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 刷新数据
const handleRefresh = async () => {
  await loadTableData()
  ElMessage.success('数据刷新成功')
}

// 监听共享服务数据变化
onMounted(() => {
  loadTableData()
  dataObjectService.addChangeListener((newData) => {
    // 可以选择是否重新加载数据
  })
})

// 计算实际数据量
const totalCount = computed(() => {
  let result = tableData.value;
  
  if (isDecrypted.value && decryptedObjectIds.value.length > 0) {
    result = result.filter(item => decryptedObjectIds.value.includes(item.id));
    return result.length;
  } else if (isDecrypted.value && decryptedObjectId.value) {
    result = result.filter(item => item.id === decryptedObjectId.value);
    return result.length;
  }
  
  if (currentStatus.value) {
    result = result.filter(item => item.status === currentStatus.value);
  }
  
  if (searchKeyword.value) {
    result = advancedSearch(result, searchKeyword.value);
  }
  
  return result.length;
});

// 根据状态和搜索条件过滤数据
const filteredTableData = computed(() => {
  let result = tableData.value;

  if (isDecrypted.value && decryptedObjectIds.value.length > 0) {
    result = result.filter(item => decryptedObjectIds.value.includes(item.id));
  } else if (isDecrypted.value && decryptedObjectId.value) {
    result = result.filter(item => item.id === decryptedObjectId.value);
  } else if (currentStatus.value) {
    result = result.filter(item => item.status === currentStatus.value);
  }

  if (searchKeyword.value) {
    result = advancedSearch(result, searchKeyword.value);
  }

  const startIndex = (currentPage.value - 1) * pageSize.value;
  const endIndex = startIndex + pageSize.value;
  return result.slice(startIndex, endIndex);
});

// 设置当前状态
const setStatus = (status) => {
  currentStatus.value = status
}

// 处理表格选择变更
const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}




// 退出登录
const logout = async () => {
  try {
    const loadingInstance = ElLoading.service({
      fullscreen: true,
      text: '正在清除数据并退出...',
      background: 'rgba(0, 0, 0, 0.7)'
    });
    const response = await axios.delete('http://localhost:8083/api/clear-database');
    
    loadingInstance.close();
    
    if (response.data && (response.data.code === 1 || response.data.success === true)) {
      ElMessage.success(response.data.data || '成功清除所有数据');
    } else {
      ElMessage.warning('清除数据可能未完全成功，但仍将退出系统');
    }
  } catch (error) {
    console.error('清除数据失败:', error);
    ElMessage.error('清除数据失败，但仍将退出系统');
  } finally {
    localStorage.removeItem('role');
    router.push('/login');
  }
}

// 处理每页显示数量变化
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
}

// 处理当前页变化
const handleCurrentChange = (val) => {
  currentPage.value = val
}

// 解密状态和表单
const decryptDialogVisible = ref(false)
const decryptFormRef = ref(null)
const decryptForm = reactive({
  objectId: '',
  dataCapsule: ''
})
const decryptFormRules = {
  objectId: [{ required: true, message: '请输入数据对象ID', trigger: 'blur' }],
}
const decryptedObjectIds = ref([])

const isRequestingToken = ref(false)
const isGeneratingCapsule = ref(false)

// 显示解密对话框
const showDecryptDialog = (data) => {
  if (typeof data === 'object' && data.ids && data.decryptedData) {
    // 新格式：包含解密后的数据
    decryptForm.objectId = data.ids.join(', ')
    decryptedObjectIds.value = data.ids
    
    // 关闭目录对话框
    directoryDialogVisible.value = false
    
    // 刷新页面数据而不是展示解密数据
    loadTableData()
  } else {
    // 兼容旧格式：只有ids数组
    const ids = Array.isArray(data) ? data : [data]
    decryptForm.objectId = ids.join(', ')
    decryptedObjectIds.value = ids
    
    // 直接调用解密函数，不显示弹窗
    handleDecrypt()
  }
}

// 提取约束条件数组
const extractConstraintArray = (constraintSet) => {
  if (!constraintSet || typeof constraintSet !== 'object') return []
  
  const constraints = []
  
  // 中文字段映射
  const fieldMapping = {
    'formatConstraint': '格式约束',
    'accessConstraint': '访问约束', 
    'pathConstraint': '路径约束',
    'regionConstraint': '区域约束',
    'shareConstraint': '共享约束'
  }
  
  // 处理新的数据结构：constraintSet.constraints数组
  if (constraintSet.constraints && Array.isArray(constraintSet.constraints)) {
    constraintSet.constraints.forEach(constraintObj => {
      Object.entries(constraintObj).forEach(([key, value]) => {
        if (value !== null && value !== undefined && value !== '') {
          const chineseKey = fieldMapping[key] || key
          constraints.push(`${chineseKey}: ${value}`)
        }
      })
    })
  } else {
    // 兼容旧的直接键值对结构
    Object.entries(constraintSet).forEach(([key, value]) => {
      if (value !== null && value !== undefined && value !== '') {
        const chineseKey = fieldMapping[key] || key
        constraints.push(`${chineseKey}: ${value}`)
      }
    })
  }
  
  return constraints
}

// 提取传输控制操作数组
const extractTransferControlArray = (propagationControl) => {
  if (!propagationControl || typeof propagationControl !== 'object') return []
  
  const controlsSet = new Set()
  
  // 中文操作映射
  const operationMapping = {
    'delegate': '可委托',
    'modify': '可修改', 
    'read': '可读取',
    'destroy': '可销毁',
    'share': '可共享',
    'canShare': '可共享',
    'canRead': '可读取', 
    'canModify': '可修改',
    'canDelegate': '可委托',
    'canDestroy': '可销毁'
  }
  
  // 处理selectedOperations对象
  if (propagationControl.selectedOperations && typeof propagationControl.selectedOperations === 'object') {
    Object.entries(propagationControl.selectedOperations).forEach(([key, value]) => {
      if (typeof value === 'boolean' && value === true) {
        const chineseOperation = operationMapping[key] || key
        controlsSet.add(chineseOperation)
      }
    })
  }
  
  // 处理其他直接的布尔值字段
  const directFields = ['canShare', 'canRead', 'canModify', 'canDelegate', 'canDestroy']
  directFields.forEach(field => {
    if (propagationControl[field] === true) {
      const chineseOperation = operationMapping[field] || field
      controlsSet.add(chineseOperation)
    }
  })
  
  // 兼容旧的直接键值对结构
  if (controlsSet.size === 0) {
    Object.entries(propagationControl).forEach(([key, value]) => {
      if (value === true || (typeof value === 'string' && value !== '')) {
        const chineseOperation = operationMapping[key] || key
        controlsSet.add(chineseOperation)
      }
    })
  }
  
  return Array.from(controlsSet)
}

// 处理解密后的数据
const handleDecryptedData = (decryptedData) => {
  try {
    console.log('处理解密后的数据:', decryptedData)
    
    // 将解密后的数据转换为表格格式
    const processedData = decryptedData.map(item => {
      const processedItem = {
        id: item.id,
        entity: item.dataEntity?.entity || '',
        status: item.dataEntity?.status || '',
        feedback: item.dataEntity?.feedback || '',
        industryCategory: item.industryCategory || '',
        processingTimeCategory: item.processingTimeCategory || '',
        dataSourceCategory: item.dataSourceCategory || '',
        totalCategoryValue: item.totalCategoryValue || '',
        totalGradeValue: item.totalGradeValue || '',
        dbGrade: item.dbGrade || 0,
        tableGrade: item.tableGrade || 0,
        locationInfo: {
          databaseName: item.locationInfo?.databaseName || '',
          tableName: item.locationInfo?.tableName || '',
          selectFields: item.locationInfo?.selectFields || ''
        },
        // 处理约束条件 - 转换为表格显示格式
        constraint: extractConstraintArray(item.constraintSet),
        constraintSet: item.constraintSet || {},
        // 处理传输控制 - 转换为表格显示格式
        transferControl: extractTransferControlArray(item.propagationControl),
        propagationControl: item.propagationControl || {},
        // 处理审计信息
        auditInfo: item.auditInfo || {},
        // 处理数据项
        dataItems: item.dataEntity?.dataItems || [],
        // 处理元数据
        metadata: item.dataEntity?.metadata || {}
      }
      
      return processedItem
    })
    
    // 更新表格数据
    tableData.value = processedData
    
    // 设置解密状态
    isDecrypted.value = true
    const idList = decryptForm.objectId.split(',').map(id => id.trim()).filter(id => id)
    decryptedObjectIds.value = idList
    decryptedObjectId.value = idList.length === 1 ? idList[0] : ''
    
    // 关闭目录弹窗
    directoryDialogVisible.value = false
    
    ElMessage.success(`解密成功，共获取 ${processedData.length} 条数据`)
  } catch (error) {
    console.error('处理解密数据失败:', error)
    ElMessage.error(`处理解密数据失败: ${error.message}`)
  }
}

// 处理解密操作
const handleDecrypt = async () => {
  try {
    isDecrypted.value = true
    const idList = decryptForm.objectId.split(',').map(id => id.trim()).filter(id => id)
    decryptedObjectIds.value = idList
    decryptedObjectId.value = idList.length === 1 ? idList[0] : ''
    
    // 关闭目录弹窗
    directoryDialogVisible.value = false
    
    ElMessage.success('解密成功')
  } catch (error) {
    ElMessage.error(`解密失败`)
  }
}


const handleGenerateCapsule = async () => {
  if (!decryptForm.objectId) {
    ElMessage.warning('请选择数据对象ID')
    return
  }
  isGeneratingCapsule.value = true
  try {
    const ids = decryptForm.objectId.split(',').map(id => id.trim()).filter(id => id).join(',')
    const apiUrl = `http://localhost:8083/api/selectIds?ids=${encodeURIComponent(ids)}`
    const response = await fetch(apiUrl, {
      method: 'GET',
      headers: { 'Accept': 'application/json' }
    })
    if (!response.ok) {
      throw new Error(`请求失败: ${response.status}`)
    }
    const data = await response.json()
    if (data && data.code === 1 && data.msg === 'success' && data.data) {
      decryptForm.dataCapsule = data.data
      ElMessage.success('成功生成数据胶囊')
    } else {
      throw new Error('返回数据格式不符合预期')
    }
  } catch (error) {
    console.error('生成数据胶囊失败:', error)
    ElMessage.error(`生成数据胶囊失败: ${error.message}`)
  } finally {
    isGeneratingCapsule.value = false
  }
}

// Excel预览相关
const previewDialogVisible = ref(false)
const previewForm = reactive({
  id: '',
  entity: '',
  locationInfo: '',
  locationInfoJson: '',
  constraint: '',
  transferControl: '',
  status: '',
  totalCategoryValue: '',
  totalGradeValue: '',
  classificationValue: '',
  levelValue: '',
  metadata: null
})

const excelBinaryData = ref(null)
const excelTableData = ref([])
const isExcelLoading = ref(false)


const fetchExcelDataFromApi = async (objectId) => {
  if (!objectId) {
    ElMessage.warning('无法获取对象ID，无法显示Excel数据')
    return
  }
  isExcelLoading.value = true
  const apiUrl = 'http://localhost:8081/api/objects/list'

  
  try {
    const response = await axios.get(apiUrl)
    

    let targetObject = null
    let dataItems = null
    
    if (response.data && Array.isArray(response.data)) {
      targetObject = response.data.find(item => item.id === objectId)
    } else if (response.data && response.data.list && Array.isArray(response.data.list)) {
      targetObject = response.data.list.find(item => item.id === objectId)
    } else if (response.data && response.data.data && Array.isArray(response.data.data)) {
      targetObject = response.data.data.find(item => item.id === objectId)
    }
    
    if (targetObject) {
      console.log(`【Excel数据】找到ID为${objectId}的对象:`, targetObject)
      

      extractClassificationValues(targetObject)
      
      // 检查 dataEntity.dataItems
      if (targetObject.dataEntity && targetObject.dataEntity.dataItems && Array.isArray(targetObject.dataEntity.dataItems)) {
        dataItems = targetObject.dataEntity.dataItems
        console.log(`【Excel数据】从对象的dataEntity中提取到${dataItems.length}条dataItems`)
      } else if (targetObject.dataItems && Array.isArray(targetObject.dataItems)) {
        dataItems = targetObject.dataItems
        console.log(`【Excel数据】从对象中直接提取到${dataItems.length}条dataItems`)
      } else if (targetObject.dataContent) {
        try {
          const dataContent = typeof targetObject.dataContent === 'string' 
            ? JSON.parse(targetObject.dataContent) 
            : targetObject.dataContent
            
          if (dataContent && dataContent.dataItems && Array.isArray(dataContent.dataItems)) {
            dataItems = dataContent.dataItems
            console.log(`【Excel数据】从dataContent中提取到${dataItems.length}条dataItems`)
          } else if (dataContent && dataContent.dataEntity && dataContent.dataEntity.dataItems && Array.isArray(dataContent.dataEntity.dataItems)) {
            dataItems = dataContent.dataEntity.dataItems
            console.log(`【Excel数据】从dataContent.dataEntity中提取到${dataItems.length}条dataItems`)
          }
        } catch (e) {
          console.error('解析dataContent失败:', e)
        }
      }
    } 

    else if (response.data && response.data.dataItems && Array.isArray(response.data.dataItems)) {
      dataItems = response.data.dataItems.filter(item => 
        item.objectId === objectId || 
        item.id === objectId ||
        (item.对象ID && item.对象ID === objectId)
      )
      
      if (dataItems.length > 0) {
        console.log(`【Excel数据】从全局dataItems中过滤出${dataItems.length}条与ID ${objectId}相关的数据`)
      } else {
        console.log('未找到与对象ID相关的数据，显示所有dataItems')
        dataItems = response.data.dataItems
      }
    }

    if (!dataItems || dataItems.length === 0) {
      console.log(`【Excel数据】未找到ID为${objectId}的对象数据`)
      ElMessage.warning(`未找到ID为${objectId}的Excel数据`)
      excelTableData.value = []
      excelTableColumns.value = []
      isExcelLoading.value = false
      return
    }
    
    // 创建Excel数据
    createExcelFromDataItems(dataItems)
  } catch (error) {
    console.error('【Excel数据】API请求失败:', error.message)
    ElMessage.error(`获取Excel数据失败: ${error.message}`)
    
    // 错误时不显示任何数据
    excelTableData.value = []
    excelTableColumns.value = []
    isExcelLoading.value = false
  }
}

// 提取对象中的分类分级值
const extractClassificationValues = (obj) => {
  if (!obj) return
  
  // 直接提取顶层字段
  if (obj.totalCategoryValue !== undefined) {
    previewForm.totalCategoryValue = obj.totalCategoryValue
  } else if (obj.classificationValue !== undefined) {
    previewForm.classificationValue = obj.classificationValue
  }
  
  if (obj.totalGradeValue !== undefined) {
    previewForm.totalGradeValue = obj.totalGradeValue
  } else if (obj.levelValue !== undefined) {
    previewForm.levelValue = obj.levelValue
  }
  
  // 尝试从dataContent中获取
  if (obj.dataContent) {
    let dataContent = obj.dataContent
    if (typeof dataContent === 'string') {
      try {
        dataContent = JSON.parse(dataContent)
      } catch (e) {
        console.warn('解析dataContent失败:', e)
      }
    }
    
    if (dataContent && typeof dataContent === 'object') {
      if (dataContent.totalCategoryValue !== undefined) {
        previewForm.totalCategoryValue = dataContent.totalCategoryValue
      } else if (dataContent.classificationValue !== undefined) {
        previewForm.classificationValue = dataContent.classificationValue
      }
      
      if (dataContent.totalGradeValue !== undefined) {
        previewForm.totalGradeValue = dataContent.totalGradeValue
      } else if (dataContent.levelValue !== undefined) {
        previewForm.levelValue = dataContent.levelValue
      }
    }
  }
}


// 创建Excel数据
const createExcelFromDataItems = (dataItems) => {
  try {
    // 处理dataItems中的字段值，移除前缀（字段名称:）
    const processedItems = dataItems.map(item => {
      const processedItem = {};
      
      for (const key in item) {
        // 如果值是字符串，并且包含"key名:"格式的前缀，则去掉前缀
        if (typeof item[key] === 'string' && item[key].startsWith(`${key}：`)) {
          // 使用中文冒号分割，取冒号后面的部分
          processedItem[key] = item[key].substring(item[key].indexOf('：') + 1).trim();
        } else if (typeof item[key] === 'string' && item[key].startsWith(`${key}:`)) {
          // 使用英文冒号分割，取冒号后面的部分
          processedItem[key] = item[key].substring(item[key].indexOf(':') + 1).trim();
        } else {
          // 其他情况保持不变
          processedItem[key] = item[key];
        }
      }
      
      return processedItem;
    });

    // 使用处理后的数据创建Excel
    const wb = XLSX.utils.book_new();
    const ws = XLSX.utils.json_to_sheet(processedItems);
    XLSX.utils.book_append_sheet(wb, ws, "数据");
    
    const excelBuffer = XLSX.write(wb, { bookType: 'xlsx', type: 'array' });
    const blob = new Blob([excelBuffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });

    excelBinaryData.value = blob;
    excelTableData.value = processedItems;
    
    isExcelLoading.value = false;
  } catch (error) {
    console.error('【Excel数据】创建Excel数据失败:', error);
    ElMessage.error(`创建Excel数据失败: ${error.message}`);
    isExcelLoading.value = false;
  }
}

// 预览实体
const previewEntity = (row) => {
  previewForm.id = row.id
  previewForm.entity = row.entity
  previewForm.locationInfo = row.locationInfo
  previewForm.locationInfoJson = row.locationInfoJson
  previewForm.constraint = row.constraint
  previewForm.transferControl = row.transferControl
  previewForm.status = row.status || ''
  

  previewForm.totalCategoryValue = ''
  previewForm.totalGradeValue = ''
  previewForm.classificationValue = ''
  previewForm.levelValue = ''
  previewForm.metadata = extractMetadata(row)
  
  excelBinaryData.value = null
  excelTableData.value = []
  
  previewDialogVisible.value = true

  fetchExcelDataFromApi(row.id)
}

// 处理元数据字符串的函数
const processMetadataString = (metadataString) => {
  if (!metadataString) {
    return { 
      dataName: '未知数据',
      sourceUnit: '未知来源',
      contactPerson: '未指定',
      contactPhone: '未提供',
      resourceSummary: '无描述',
      fieldClassification: '未分类'
    }
  }

  if (typeof metadataString === 'object') {
    return metadataString
  }

  let cleanString = metadataString.toString()
  
  try {
    if (cleanString.startsWith('"') && cleanString.endsWith('"')) {
      const unquoted = cleanString.slice(1, -1).replace(/\\"/g, '"')
      cleanString = unquoted
    }

    if (cleanString.includes('\\\"') || cleanString.includes('\\\\')) {
      cleanString = cleanString.replace(/\\\\/g, '\\').replace(/\\"/g, '"')
    }

    if (cleanString.includes('"]}"') && !cleanString.endsWith('"}')) {
      cleanString = cleanString.replace('"]}"', '"}')
    }
    if (cleanString.includes('"]}",')) {
      cleanString = cleanString.replace('"]}",', '"}')
    }

    if (!cleanString.startsWith('{') && cleanString.includes('":"')) {
      cleanString = '{' + cleanString
    }

    if (!cleanString.endsWith('}') && cleanString.includes('":"')) {
      cleanString = cleanString + '}'
    }

    try {
      const parsed = JSON.parse(cleanString)

      return {
        dataName: parsed.dataName || '未知数据',
        sourceUnit: parsed.sourceUnit || '未知来源',
        contactPerson: parsed.contactPerson || '未指定',
        contactPhone: parsed.contactPhone || '未提供',
        resourceSummary: parsed.resourceSummary || '无描述',
        fieldClassification: parsed.fieldClassification || '未分类',
        headers: parsed.headers || []
      }
    } catch (parseError) {
      const keyValuePairs = {}
      const regex = /"([^"]+)"\s*:\s*"([^"]*)"/g
      let match
      
      while ((match = regex.exec(cleanString)) !== null) {
        keyValuePairs[match[1]] = match[2]
      }
      
      if (Object.keys(keyValuePairs).length > 0) {
        return {
          dataName: keyValuePairs.dataName || '未知数据',
          sourceUnit: keyValuePairs.sourceUnit || '未知来源',
          contactPerson: keyValuePairs.contactPerson || '未指定',
          contactPhone: keyValuePairs.contactPhone || '未提供',
          resourceSummary: keyValuePairs.resourceSummary || '无描述',
          fieldClassification: keyValuePairs.fieldClassification || '未分类'
        }
      }

      return {
        dataName: '解析错误',
        sourceUnit: '数据部',
        contactPerson: '未知',
        contactPhone: '未知',
        resourceSummary: '元数据解析失败: ' + cleanString.substring(0, 50) + '...',
        fieldClassification: '未分类'
      }
    }
  } catch (e) {
    return {
      dataName: '解析错误',
      sourceUnit: '数据部',
      contactPerson: '未知',
      contactPhone: '未知',
      resourceSummary: '元数据解析失败: ' + e.message,
      fieldClassification: '未分类'
    }
  }
}

const extractMetadata = (row) => {
  if (!row) {
    return createDefaultMetadata('未知实体')
  }

  if (row.metadata && typeof row.metadata === 'object') {

    return {
      dataName: row.metadata.dataName || row.entity || '未知数据',
      sourceUnit: row.metadata.sourceUnit || '数据部',
      contactPerson: row.metadata.contactPerson || '未指定',
      contactPhone: row.metadata.contactPhone || '未提供',
      resourceSummary: row.metadata.resourceSummary || '无',
      fieldClassification: row.metadata.fieldClassification || '未分类',
      headers: Array.isArray(row.metadata.headers) ? row.metadata.headers : []
    }
  }

  if (row.metadataJson) {
    try {
      const parsedMetadata = processMetadataString(row.metadataJson)
      return parsedMetadata
    } catch (e) {
      console.warn('解析row.metadataJson失败:', e)
    }
  }
  

  if (row.dataContent) {
    try {
      const contentObj = typeof row.dataContent === 'string' ? 
        JSON.parse(row.dataContent) : row.dataContent
      
      if (contentObj && contentObj.metadataJson) {
        const parsedMetadata = processMetadataString(contentObj.metadataJson)
        return parsedMetadata
      }

      if (contentObj && (contentObj.metadata || contentObj.dataName || contentObj.sourceUnit || 
          contentObj.contactPerson || contentObj.contactPhone)) {

        if (contentObj.metadata && typeof contentObj.metadata === 'object') {
          return {
            dataName: contentObj.metadata.dataName || row.entity || '未知数据',
            sourceUnit: contentObj.metadata.sourceUnit || '数据部',
            contactPerson: contentObj.metadata.contactPerson || '未指定',
            contactPhone: contentObj.metadata.contactPhone || '未提供',
            resourceSummary: contentObj.metadata.resourceSummary || '无',
            fieldClassification: contentObj.metadata.fieldClassification || '未分类',
            headers: contentObj.metadata.headers || []
          };
        }
        
        return {
          dataName: contentObj.dataName || row.entity || '未知数据',
          sourceUnit: contentObj.sourceUnit || '数据部',
          contactPerson: contentObj.contactPerson || '未指定',
          contactPhone: contentObj.contactPhone || '未提供',
          resourceSummary: contentObj.resourceSummary || '无',
          fieldClassification: contentObj.fieldClassification || '未分类',
          headers: contentObj.headers || []
        }
      }
    } catch (e) {
      console.warn('解析dataContent失败:', e)
    }
  }
  
  // 解析位置信息
  if (row.locationInfoJson) {
    try {
      const locationInfo = JSON.parse(row.locationInfoJson)
      if (locationInfo && locationInfo.locations) {
        const locations = locationInfo.locations
        const locationStrings = locations.map(loc => 
          `${loc.sheet || '默认'}: ${loc.startRow || '1'}-${loc.endRow || '*'} 行, ${loc.startColumn || 'A'}-${loc.endColumn || '*'} 列`
        )
        previewForm.locationInfo = locationStrings.join('; ')
      }
    } catch (e) {
      console.warn('解析位置信息JSON失败:', e)
    }
  }

  return createDefaultMetadata(row.entity)
}

// 创建默认元数据的辅助函数
const createDefaultMetadata = (entityName) => {
  entityName = entityName || '未知实体'
  let sourceUnit = '数据部'
  let contactPerson = '王主任'
  

  if (entityName.includes('用户')) {
    sourceUnit = '用户管理部'
  } else if (entityName.includes('订单')) {
    sourceUnit = '订单管理部'
    contactPerson = '李经理'
  } else if (entityName.includes('产品')) {
    sourceUnit = '产品部'
    contactPerson = '张总监'
  } else if (entityName.includes('库存')) {
    sourceUnit = '仓储部'
    contactPerson = '张管理员'
  }
  
  return {
    dataName: entityName,
    sourceUnit: sourceUnit,
    contactPerson: contactPerson,
    contactPhone: "123-456789",
    resourceSummary: `${entityName}数据资源`,
    fieldClassification: entityName.includes('用户') ? '用户数据' : 
                        (entityName.includes('订单') ? '订单数据' : 
                         (entityName.includes('库存') ? '运营数据' : '基础数据')),
    headers: []
  }
}

// 获取元数据字段值的辅助函数
const getMetadataValue = (fieldName) => {

  if (!previewForm.metadata) return null
  

  if (previewForm.metadata[fieldName]) {
    return previewForm.metadata[fieldName]
  }
  

  const checkNestedObject = (obj, field) => {

    if (typeof obj !== 'object' || obj === null) return null
    if (obj[field] !== undefined) return obj[field]

    for (const key in obj) {
      if (typeof obj[key] === 'object' && obj[key] !== null) {
        const result = checkNestedObject(obj[key], field)
        if (result !== null) return result
      }
    }
    
    return null
  }
  

  return checkNestedObject(previewForm.metadata, fieldName)
}

// 获取当前日期时间的格式化字符串
const getCurrentDateTime = () => {
  const now = new Date()
  return now.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false
  })
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

// 格式化元数据信息文本
const formatMetadataText = (entry) => {
  if (!entry || !Array.isArray(entry) || entry.length < 2) return ''
  
  const [key, value] = entry
  if (value === null || value === undefined || value === '') return ''
  
  // 中文字段映射
  const metadataMapping = {
    'dataName': '数据名称',
    'sourceUnit': '来源单位',
    'contactPerson': '联系人',
    'contactPhone': '联系电话',
    'resourceSummary': '资源摘要',
    'fieldClassification': '字段分类'
  }
  
  const chineseKey = metadataMapping[key] || key
  return `<span class="constraint-prefix">${chineseKey}:</span>${value}`
}

// 处理下载数字对象
const handleDownload = () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要下载的数据对象');
    return;
  }
  selectedRows.value.forEach(row => {
    if (row.excelData) {
      downloadExcelFile(row);
    } else {
      ElMessage.info(`${row.entity} 没有可下载的数据，请先点击实体名进行预览`);
    }
  });
}

// 下载Excel文件
const downloadExcelFile = (row) => {
  try {

    const blob = new Blob([row.excelData], { 
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' 
    });

    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = `${row.entity}.xlsx`;

    document.body.appendChild(link);
    link.click();

    document.body.removeChild(link);
    URL.revokeObjectURL(link.href);
    
    ElMessage.success(`${row.entity} 已下载`);
  } catch (error) {
    console.error('下载文件时出错:', error);
    ElMessage.error(`下载 ${row.entity} 时出错: ${error.message}`);
  }
}

// 重置解密状态
const resetDecryption = () => {
  ElMessageBox.confirm('确定要重新解密？当前解密的数据将不再显示。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    isDecrypted.value = false;
    decryptedObjectId.value = '';
    decryptedObjectIds.value = [];
    decryptForm.objectId = '';
    decryptForm.dataCapsule = '';
    decryptDialogVisible.value = false;
  }).catch(() => {
  });
}

const fetchLatestDataFromApi = async () => {
  try {
    ElMessage.info('正在从API获取最新数据...')
    const apiUrl = 'http://localhost:8083/api/simplified-objects'
    const response = await axios.get(apiUrl)
    
    if (response.data) {

      let newData = []
      
      if (Array.isArray(response.data)) {
        newData = response.data
      } else if (response.data.list && Array.isArray(response.data.list)) {
        newData = response.data.list
      } else if (response.data.data && Array.isArray(response.data.data)) {
        newData = response.data.data
      }
      
      if (newData.length > 0) {
        // 更新共享服务中的数据
        dataObjectService.updateDataObjects(newData)
        ElMessage.success(`成功获取最新数据`)
      } 
    }
  } catch (error) {
    console.error('获取最新数据失败:', error)
    ElMessage.error(`获取最新数据失败: ${error.message}`)
  }
}

const directoryDialogVisible = ref(false)

const showDirectoryDialog = () => {

  fetchLatestDataFromApi()
    .then(() => {
      console.log('成功获取最新数据，显示目录对话框')
      directoryDialogVisible.value = true
    })
    .catch(error => {
      console.error('获取数据失败，但仍然显示目录对话框:', error)
      directoryDialogVisible.value = true
    })
}

// 处理查看目录项目
const handleViewDirectoryItem = (item) => {

  directoryDialogVisible.value = false
  previewEntity(item)
}

// 添加三维可视化相关
const visualizationVisible = ref(false)
const showVisualization = () => {
  visualizationVisible.value = true
}

// 跳转到user-main页面
const goToUserMain = () => {
  router.push('/user-main')
}

// 清空使用方数据库
const clearDatabase = async () => {
  try {
    // 显示确认对话框
    await ElMessageBox.confirm(
      '此操作将清空所有数据对象，是否继续？',
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    const loadingInstance = ElLoading.service({
      fullscreen: true,
      text: '正在清空数据库...',
      background: 'rgba(0, 0, 0, 0.7)'
    })
    
    const response = await axios.delete('http://localhost:8083/api/objects')
    
    loadingInstance.close()
    
    if (response.data && response.data.code === 1) {
      ElMessage.success(response.data.data || '所有数据对象已清空')
      // 清空后重新加载数据
      await loadTableData()
    } else {
      ElMessage.error(`清空失败: ${response.data?.message || '未知错误'}`)
    }
  } catch (error) {
    if (error === 'cancel') {
      ElMessage.info('已取消清空操作')
      return
    }
    
    console.error('清空数据库失败:', error)
    
    if (error.response) {
      if (error.response.status === 404) {
        ElMessage.error('后端服务未启动或接口不存在')
      } else if (error.response.status === 500) {
        ElMessage.error(`服务器错误: ${error.response.data?.message || '内部服务器错误'}`)
      } else {
        ElMessage.error(`清空失败 (${error.response.status}): ${error.response.data?.message || error.message}`)
      }
    } else if (error.request) {
      ElMessage.error('无法连接到后端服务，请确保服务已启动')
    } else {
      ElMessage.error(`清空数据库失败: ${error.message || '未知错误'}`)
    }
  }
}



const headerCellStyle = ({ column }) => {
  const blueProps = [
    'id',
    'entity',
    'metadata',
    'locationInfo',
    'constraint',
    'transferControl',
    'auditInfo',
    'classificationLevelValue',
    'creatorName'
  ];
  if (blueProps.includes(column.property)) {
    return {
      background: '#eaf6ff',
      color: '#1677c7',
      fontWeight: 'bold',
      fontSize: '24px',
      textAlign: 'center',
      padding: '10px 0'
    };
  }

  return {
    background: '#f5f7fa',
    color: '#606266',
    fontWeight: 'bold',
    fontSize: '24px',
    textAlign: 'center',
    padding: '10px 0'
  };
};

function getLocationInfoObj(locationInfo, locationInfoJson) {
  if (locationInfoJson) {
    try {
      return typeof locationInfoJson === 'object' ? locationInfoJson : JSON.parse(locationInfoJson);
    } catch {}
  }
  if (locationInfo) {
    if (typeof locationInfo === 'object') return locationInfo;
    try {
      return JSON.parse(locationInfo);
    } catch {}
  }
  return null;
}

const scrDialogVisible = ref(false)
const scrForm = ref({
  metaData: '',
  fno: '',
  sfno: ''
})
function openScrDialog() {
  scrForm.value = { metaData: '', fno: '', sfno: '' }
  scrDialogVisible.value = true
}
async function submitScrForm() {
  const scrLoading = ElLoading.service({
    fullscreen: true,
    text: '正在构造共享证书申请...',
    background: 'rgba(0, 0, 0, 0.7)'
  })
  try {
    const scrResponse = await axios.post('http://localhost:8083/api/generate-scr', {
      metaData: scrForm.value.metaData,
      fno: scrForm.value.fno,
      sfno: scrForm.value.sfno
    })
    scrLoading.close()
    scrDialogVisible.value = false
    if (scrResponse.data && (scrResponse.data.code === 1 || scrResponse.data.success === true)) {
      ElMessage.success('共享证书申请构造成功')
    } else {
      ElMessage.warning(`共享证书申请构造失败: ${scrResponse.data?.message || scrResponse.data?.msg || '未知错误'}`)
    }
  } catch (e) {
    scrLoading.close()
    ElMessage.error('请求失败: ' + e.message)
  }
}

function setWatermark(text) {
  const id = 'global-watermark-bg'
  let wm = document.getElementById(id)
  if (wm) {
    wm.parentNode.removeChild(wm)
  }
  const can = document.createElement('canvas')
  can.width = 600
  can.height = 400
  const ctx = can.getContext('2d')
  ctx.rotate(-30 * Math.PI / 180)
  ctx.font = '50px Microsoft YaHei'
  ctx.fillStyle = 'rgba(150,150,150,0.15)'
  ctx.textAlign = 'left'
  ctx.textBaseline = 'middle'
  ctx.fillText(text, 80, 200)
  const base64Url = can.toDataURL()
  const div = document.createElement('div')
  div.id = id
  div.style.pointerEvents = 'none'
  div.style.position = 'fixed'
  div.style.top = '0'
  div.style.left = '0'
  div.style.width = '100vw'
  div.style.height = '100vh'
  div.style.zIndex = '9999'
  div.style.background = `url(${base64Url}) left top repeat`
  document.body.appendChild(div)
}

function removeWatermark() {
  const wm = document.getElementById('global-watermark-bg')
  if (wm) wm.parentNode.removeChild(wm)
}

onMounted(() => {
  setWatermark('使  用  方')
  window.addEventListener('resize', () => setWatermark('使  用  方'))
})
onBeforeUnmount(() => {
  removeWatermark()
  window.removeEventListener('resize', () => setWatermark('使  用  方'))
})
</script>

<style scoped>
.datasource-container {
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-image: url('/background.jpg');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  background-attachment: fixed;
  overflow: hidden;
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
}

/* 主内容区域样式 */
.main-content {
  flex: 1;
  padding: 16px;
  background-color: transparent;
  overflow: auto;
  box-sizing: border-box;
}

.content-card {
  background-color: rgba(255, 255, 255, 0.3);
  border-radius: 4px;
  padding: 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
  height: 100%;
  width: 100%;
}

/* 状态筛选按钮区域 */
.status-filter {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.status-btn {
  border: none;
}

.status-btn.active {
  background-color: #1890ff;
  color: #ffffff;
}

/* 搜索和操作区域 */
.action-bar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 12px;
  gap: 12px;
}

.search-area {
  margin-right: auto;
}

.search-input {
  width: 300px;
}

:deep(.search-input .el-input__inner) {
  font-size: 18px !important;
  padding: 14px 18px !important;
  height: 30px !important;
}

:deep(.search-input .el-input__wrapper) {
  padding: 14px 18px !important;
}

.action-buttons {
  display: flex;
  gap: 12px;
}

.action-buttons .el-button {
  font-size: 18px;
  font-weight: 700;
  padding: 14px 24px;
  height: 48px;
}

/* 表格容器区域 */
.table-container {
  margin-bottom: 16px;
  height: calc(100vh - 340px);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

/* 表格样式优化 */
:deep(.el-table) {
  width: 100% !important;
  height: 100% !important;
}

:deep(.el-table__header),
:deep(.el-table__body),
:deep(.el-table__footer) {
  width: 100% !important;
  table-layout: fixed !important;
  display: table !important;
}

:deep(.el-table__header-wrapper),
:deep(.el-table__body-wrapper),
:deep(.el-table__footer-wrapper) {
  width: 100% !important;
}

:deep(.el-table__cell) {
  text-align: center;
  padding: 8px 0;
  box-sizing: border-box;
}

:deep(.el-table .el-table__cell .cell) {
  padding: 0 5px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  box-sizing: border-box;
  width: 100%;
  display: inline-block;
  font-size: 16px;
  font-weight: 600;
}

/* 确保ID单元格不被全局样式覆盖 */
:deep(.el-table .el-table__cell .id-cell) {
  white-space: normal;
  overflow: visible;
  text-overflow: clip;
  word-break: break-all;
}

:deep(.el-table .el-table__cell:last-child .cell) {
  padding-right: 5px;
}

:deep(.el-table__row) {
  height: 45px !important;
}

:deep(.el-table__header tr) {
  height: 45px !important;
}

:deep(.el-table__header th.el-table__cell) {
  background-color: #f5f7fa;
  color: #606266;
  font-weight: bold;
  font-size: 24px !important;
  padding: 8px 0;
  text-align: center;
}

/* 表格整体字体样式 */
:deep(.el-table) {
  font-size: 16px;
  font-weight: 600;
}

:deep(.el-table th) {
  font-size: 24px !important;
  font-weight: 700;
}

:deep(.el-table td) {
  font-size: 18px;
  font-weight: 600;
  padding: 12px 8px;
}

:deep(.el-table .cell) {
  font-weight: 600;
  line-height: 1.5;
}

/* 状态标签样式 */
.status-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
}

/* 移除按钮点击后的黑色边框 */
.el-button:focus {
  outline: none !important;
  box-shadow: none !important;
}

.status-success {
  background-color: #f6ffed;
  color: #52c41a;
}

.status-error {
  background-color: #fff2f0;
  color: #ff4d4f;
}

.status-pending {
  background-color: #f4f4f5;
  color: #909399;
}

/* 分页区域 */
.pagination-area {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
  margin-bottom: 20px;
  height: 32px;
  position: relative;
  z-index: 1;
}

.total-text {
  font-size: 14px;
  color: #8c8c8c;
}

/* 对话框样式 */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 20px;
}

/* 数据锁定占位符样式 */
.data-locked-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  width: 100%;
  background-color: #f8f9fa;
  border-radius: 4px;
  border: 1px dashed #dcdfe6;
  padding: 40px 0;
}

.locked-icon {
  font-size: 60px;
  color: #909399;
  margin-bottom: 20px;
}

.data-locked-placeholder p {
  font-size: 16px;
  color: #606266;
  text-align: center;
  line-height: 1.6;
  max-width: 80%;
}

.locked-subtitle {
  font-size: 14px;
  color: #8c8c8c;
  text-align: center;
  line-height: 1.6;
  max-width: 80%;
}

/* 全局对话框样式 - 放在样式的最底部以确保最高优先级 */
:deep(.el-overlay) {
  overflow: hidden;
}

:deep(.decrypt-dialog) {
  display: flex;
  align-items: center;
  justify-content: center;
}

:deep(.decrypt-dialog .el-dialog) {
  margin: 0 auto !important;
  position: relative !important;
  top: 0 !important;
  transform: none !important;
  max-width: 90%;
}

:deep(.edit-dialog) {
  display: flex;
  align-items: center;
  justify-content: center;
}

:deep(.edit-dialog .el-dialog) {
  margin: 0 auto !important;
  position: relative !important;
  top: 0 !important;
  transform: none !important;
  max-width: 90%;
}

/* 确保对话框居中显示 */
:deep(.el-dialog) {
  margin: 0 auto !important;
  position: fixed !important;
  top: 50% !important;
  left: 50% !important;
  transform: translate(-50%, -50%) !important;
  max-width: 90%;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

:deep(.el-dialog__header) {
  padding: 20px;
  text-align: center;
  border-bottom: 1px solid #f0f0f0;
  cursor: move;
  font-weight: bold;
}

:deep(.el-dialog__body) {
  padding: 30px 20px;
}

:deep(.el-dialog__footer) {
  padding: 10px 20px 20px;
  text-align: center;
}

/* 添加预览相关的样式 */
.preview-header {
  margin-bottom: 15px;
}

.preview-info {
  display: flex;
  flex-direction: column;
  gap: 10px;
  background-color: #f8f9fa;
  border-radius: 4px;
  padding: 10px;
}

/* 新的基本信息表格样式 */
.basic-info-table {
  width: 100%;
  display: flex;
  flex-wrap: nowrap;
  gap: 20px;
  justify-content: center;
  margin-bottom: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
  padding: 12px 15px;
  overflow-x: auto;
  white-space: nowrap;
}

.info-item {
  display: inline-block;
  padding: 0 10px;
  color: #333;
  font-size: 14px;
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  text-align: center;
}

.constraint-info {
  max-width: 500px;
}

.info-item strong {
  font-weight: bold;
  color: #606266;
  margin-right: 5px;
}

/* 元数据部分样式 */
.metadata-section {
  margin: 10px auto 5px;
  padding: 8px 10px;
  background-color: #f9f9f9;
  border-radius: 4px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  border: 1px solid #eaeaea;
}

.metadata-items {
  display: flex;
  flex-wrap: nowrap;
  justify-content: center;
  overflow-x: auto;
  padding-bottom: 3px;
  scrollbar-width: thin;
}

.metadata-items::-webkit-scrollbar {
  height: 3px;
}

.metadata-items::-webkit-scrollbar-thumb {
  background-color: rgba(0, 0, 0, 0.1);
  border-radius: 3px;
}

.metadata-item {
  padding: 4px 8px;
  margin: 0 8px;
  white-space: nowrap;
  flex-shrink: 0;
  font-size: 13px;
}

/* 表头信息部分样式 */
.headers-section {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px dashed #e0e0e0;
}

.headers-title {
  font-size: 13px;
  color: #606266;
  margin-bottom: 8px;
  text-align: center;
  font-weight: bold;
}

.headers-list {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 5px;
  padding: 0 15px;
}

.header-tag {
  margin: 2px;
  font-size: 12px;
}

/* ID单元格样式 */
.id-cell {
  width: 100%;
  overflow: visible;
  white-space: normal;
  word-break: break-all;
  padding: 2px 5px;
  text-align: left;
  text-align: center;
}

/* 约束条件相关样式 */
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
}

:deep(.constraint-prefix) {
  font-weight: bold !important;
  color: #303133 !important;
  margin-right: 4px !important;
}

/* 元数据信息相关样式 */
.metadata-container {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 4px;
}

.metadata-row {
  display: flex;
  justify-content: space-between;
  gap: 16px;
}

.metadata-item-pair {
  flex: 1;
  text-align: left;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 传输控制操作样式 */
.control-container {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 6px;
  padding: 4px;
}

.control-tag {
  margin: 2px;
  font-size: 15px;
  font-weight: 700;
  min-height: 32px;
  color: black;
  border: 2px solid #409EFF;
  border-radius: 6px;
}

/* 数源方样式 */
.creator-name {
  font-size: 20px;
  font-weight: 700;
  color: #303133;
}

/* 分类分级值样式 */
.classification-level-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5px;
}

.classification-level-item {
  display: flex;
  gap: 5px;
  font-size: 20px;
  line-height: 1.5;
}

.classification-level-item .label {
  font-weight: 700;
  color: #606266;
}

.classification-level-item .value {
  color: #2162de;
  font-weight: 700;
}

/* 分类分级值样式 */
.classification-level-section {
  margin-top: 10px;
  padding-top: 5px;
  border-top: 1px dashed #eaeaea;
  text-align: center;
}

.classification-level-items {
  display: flex;
  justify-content: center;
  gap: 30px;
  padding: 5px 0;
}

.classification-level-item {
  display: flex;
  align-items: center;
  gap: 5px;
}

.classification-level-item .label {
  font-weight: bold;
  color: #606266;
}

.classification-level-item .value {
    color: #2f58d0;
  font-weight: bold;
}

/* 分页区域样式 */
.pagination-area {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
  margin-bottom: 20px;
  height: 32px;
  position: relative;
  z-index: 1;
}

/* 表头信息部分样式 */
.table-title {
  font-size: 33px;
  font-weight: bold;
  text-align: center;
  margin-bottom: 12px;
  color: #222;
}

.select-fields-link {
  text-decoration: underline;
}

/* 实体列换行样式 */
.entity-link {
  white-space: normal;
  word-wrap: break-word;
  word-break: break-all;
  line-height: 1.4;
  display: inline-block;
  max-width: 100%;
  font-size: 18px;
  font-weight: 700;
  color: #2f58d0;
}
</style>