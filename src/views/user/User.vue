<template>
  <div class="datasource-container">
    <!-- 头部导航 -->
    <AppHeader role-name="使用方" @logout="logout" />
    
    <!-- 主内容区域 -->
    <div class="main-content">
      <!-- 标签页 -->
      <div class="content-card">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="数字对象列表" name="objectList">
            

            
            <!-- 搜索和操作区 -->
            <div class="action-bar">
              <div class="search-area">
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
                <el-button type="primary" :disabled="selectedRows.length === 0" @click="handleDownload">下载数字对象</el-button>
                <el-button v-if="!isDecrypted" type="primary" plain @click="showDecryptDialog">解密</el-button>
                <el-button v-else type="warning" plain @click="resetDecryption">重新解密</el-button>
              </div>
            </div>
            
            <!-- 数据表格 -->
            <div class="table-container">
              <div v-if="!isDecrypted" class="data-locked-placeholder">
                <el-icon class="locked-icon"><Lock /></el-icon>
                <p>数据已加密，请点击右上角"解密"按钮并输入正确的数字对象ID和Token进行解密</p>
                <p class="locked-subtitle">解密后将显示所有匹配ID的数字对象数据</p>
              </div>
              <el-table
                v-else
                :data="filteredTableData"
                style="width: 100%"
                @selection-change="handleSelectionChange"
                border
                height="100%"
                fit
                :row-style="{ height: '45px' }"
                :header-row-style="{ height: '45px' }"
              >
                <el-table-column type="selection" width="55" align="center" fixed />
                <el-table-column prop="id" label="ID" width="400" align="center" fixed>
                  <template #default="scope">
                    <div class="id-cell">{{ scope.row.id }}</div>
                  </template>
                </el-table-column>
                <el-table-column prop="entity" label="实体" width="120" align="center">
                  <template #default="scope">
                    <el-link type="primary" @click="previewEntity(scope.row)">{{ scope.row.entity }}</el-link>
                  </template>
                </el-table-column>
                <el-table-column prop="locationInfo" label="定位信息" min-width="150" align="center" />
                <el-table-column prop="constraint" label="约束条件" min-width="250" align="center">
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
                <el-table-column prop="transferControl" label="传输控制操作" min-width="180" align="center">
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
                        <span class="label">分类值：</span>
                        <span class="value">{{ scope.row.totalCategoryValue || scope.row.classificationValue || '未分类' }}</span>
                      </div>
                      <div class="classification-level-item">
                        <span class="label">分级值：</span>
                        <span class="value">{{ scope.row.totalGradeValue || scope.row.levelValue || '未分级' }}</span>
                      </div>
                    </div>
                  </template>
                </el-table-column>
              </el-table>
            </div>
            
            <!-- 分页 -->
            <div class="pagination-area">
              <CommonPagination
                v-if="isDecrypted"
                v-model:current-page="currentPage"
                v-model:page-size="pageSize"
                :total-count="totalCount"
                :page-sizes="[5, 10, 20]"
                :disabled="!isDecrypted"
                background
                @size-change="handleSizeChange"
              />
            </div>
          </el-tab-pane>
        </el-tabs>
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
    <el-form :model="decryptForm" label-width="120px" ref="decryptFormRef" :rules="decryptFormRules">
      <el-form-item label="数字对象ID:" prop="objectId">
        <el-input 
          v-model="decryptForm.objectId" 
          placeholder="请输入ID，多个ID请用逗号分隔"
          type="textarea"
          :rows="3"
        ></el-input>
      </el-form-item>
      <el-form-item label="token:" prop="token">
        <el-input 
          v-model="decryptForm.token" 
          placeholder="请输入token"
          type="textarea"
          :rows="2"
        ></el-input>
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button type="info" plain @click="handleRequestToken">申请token</el-button>
        <el-button type="primary" @click="handleDecrypt">确定</el-button>
        <el-button @click="decryptDialogVisible = false">取消</el-button>
      </span>
    </template>
  </el-dialog>

  <!-- Excel预览对话框 -->
  <el-dialog
    v-model="previewDialogVisible"
    :title="`预览Excel - ${previewForm.entity}`"
    width="90%"
    :close-on-click-modal="false"
    draggable
    class="custom-dialog"
    top="5vh"
  >
    <div class="preview-header">
      <div class="preview-info">
        <!-- 修改基本信息样式为单行显示 -->
        <div class="basic-info-table">
          <span class="info-item"><strong>实体：</strong>{{ previewForm.entity }}</span>
          <span class="info-item"><strong>定位信息：</strong>{{ previewForm.locationInfo }}</span>
          <span class="info-item constraint-info" :title="Array.isArray(previewForm.constraint) ? previewForm.constraint.join(', ') : previewForm.constraint"><strong>约束条件：</strong>{{ Array.isArray(previewForm.constraint) ? previewForm.constraint.join(', ') : previewForm.constraint }}</span>
          <span class="info-item"><strong>传输控制操作：</strong>{{ Array.isArray(previewForm.transferControl) ? previewForm.transferControl.join(', ') : previewForm.transferControl }}</span>
        </div>
        <!-- 添加元数据信息显示 -->
        <div v-if="previewForm.metadata" class="metadata-section">
          <div class="metadata-items">
            <!-- 所有元数据项在一行显示 -->
            <div class="metadata-item">数据名称: <strong>{{ previewForm.metadata.dataName || previewForm.entity }}</strong></div>
            <div class="metadata-item">来源单位: <strong>{{ previewForm.metadata.sourceUnit || '数据部' }}</strong></div>
            <div class="metadata-item">联系人: <strong>{{ previewForm.metadata.contactPerson || '未指定' }}</strong></div>
            <div class="metadata-item">联系电话: <strong>{{ previewForm.metadata.contactPhone || '未提供' }}</strong></div>
            <div class="metadata-item">资源摘要: <strong>{{ previewForm.metadata.resourceSummary || '无' }}</strong></div>
            <div class="metadata-item">领域分类: <strong>{{ previewForm.metadata.fieldClassification || '未分类' }}</strong></div>
            <div class="metadata-item">更新时间: <strong>{{ getCurrentDateTime() }}</strong></div>
          </div>
          <!-- 添加分类分级值显示 -->
          <div class="classification-level-section">
            <div class="classification-level-items">
              <div class="classification-level-item">
                <span class="label">分类值：</span>
                <span class="value">{{ previewForm.totalCategoryValue || previewForm.classificationValue || '未分类' }}</span>
              </div>
              <div class="classification-level-item">
                <span class="label">分级值：</span>
                <span class="value">{{ previewForm.totalGradeValue || previewForm.levelValue || '未分级' }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 直接显示Excel数据表格 -->
    <div class="excel-data-section">
      <h3 class="section-title">数据预览</h3>
      
      <div v-if="isExcelLoading" class="loading-container">
        <el-loading :fullscreen="false" text="正在加载Excel数据..." />
      </div>
      
      <div v-else-if="excelTableData.length > 0" class="excel-table-container">
        <div class="data-info">找到 {{ excelTableData.length }} 条记录</div>
        <el-table :data="excelTableData" border stripe style="width: 100%">
          <el-table-column 
            v-for="(key, index) in getObjectKeys(excelTableData)" 
            :key="index"
            :prop="key"
            :label="key"
            :align="'center'"
            :min-width="100"
          />
        </el-table>
      </div>
      
      <div v-else class="no-data-message">
        <el-empty description="暂无数据" />
      </div>
    </div>
    
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="previewDialogVisible = false">关闭</el-button>
        <el-button type="primary" v-if="excelTableData.length > 0" @click="handleExportExcel">导出Excel</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Lock, Document } from '@element-plus/icons-vue'
import * as XLSX from 'xlsx'
import ExcelPreview from '@/components/ExcelPreview.vue'
import AppHeader from '@/components/AppHeader.vue'
import CommonPagination from '@/components/CommonPagination.vue'
import dataObjectService from '@/services/dataObjectService'
import { ensureArray, advancedSearch } from '@/utils/searchUtils';
import axios from 'axios'

const router = useRouter()
const activeTab = ref('objectList')
const currentStatus = ref('') // 默认显示全部数字对象
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(5) // 改为默认显示5条
const isDecrypted = ref(false)
const selectedRows = ref([])
const decryptedObjectId = ref('')

// 添加计算属性判断是否为已合格状态
const isQualifiedStatus = computed(() => currentStatus.value === '已合格')

const editDialogVisible = ref(false)
const editFormRef = ref(null)
const editForm = reactive({
  id: '',
  entity: '',
  locationInfo: '',
  constraint: '',
  transferControl: '',
  auditInfo: '',
  status: '',
  feedback: ''
})
const editingIndex = ref(-1)

// 表格数据 - 从共享服务获取
const tableData = ref(dataObjectService.getAllDataObjects())

// 监听共享服务数据变化
onMounted(() => {
  // 添加数据变化监听器
  dataObjectService.addChangeListener((newData) => {
    console.log('使用方收到数据变化:', newData)
    // 无需手动更新tableData，因为是响应式引用
  })
})

// 计算实际数据量
const totalCount = computed(() => {
  let result = tableData.value;
  
  // 如果已解密且有指定的对象ID列表，则只计算匹配ID的条目数
  if (isDecrypted.value && decryptedObjectIds.value.length > 0) {
    result = result.filter(item => decryptedObjectIds.value.includes(item.id));
    return result.length;
  } else if (isDecrypted.value && decryptedObjectId.value) {
    // 兼容旧代码，如果decryptedObjectIds为空但有单个ID
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

  // 如果已解密且有指定的对象ID列表，则只显示这些ID的对象
  if (isDecrypted.value && decryptedObjectIds.value.length > 0) {
    result = result.filter(item => decryptedObjectIds.value.includes(item.id));
  } else if (isDecrypted.value && decryptedObjectId.value) {
    // 兼容旧代码，如果decryptedObjectIds为空但有单个ID
    result = result.filter(item => item.id === decryptedObjectId.value);
  } else if (currentStatus.value) {
    // 状态过滤
    result = result.filter(item => item.status === currentStatus.value);
  }

  // 关键字搜索
  if (searchKeyword.value) {
    result = advancedSearch(result, searchKeyword.value);
  }

  // 分页处理
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

// 获取状态对应的样式类名
const getStatusClass = (status) => {
  switch (status) {
    case '已合格': return 'status-success'
    case '不合格': return 'status-error'
    case '待检验': return 'status-pending'
    default: return ''
  }
}

// 编辑对象
const handleEdit = (row) => {
  editingIndex.value = tableData.value.findIndex(item => item.id === row.id)
  
  // 深拷贝行数据到编辑表单
  Object.keys(editForm).forEach(key => {
    editForm[key] = row[key]
  })
  
  editDialogVisible.value = true
}

// 取消编辑
const cancelEdit = () => {
  editDialogVisible.value = false
  // 重置表单
  Object.keys(editForm).forEach(key => {
    editForm[key] = ''
  })
  editingIndex.value = -1
}

// 保存编辑
const saveEdit = () => {
  // 如果状态为"已合格"，清空反馈意见
  if (editForm.status === '已合格') {
    editForm.feedback = ''
  }
  
  // 更新表格数据
  if (editingIndex.value > -1) {
    tableData.value[editingIndex.value] = { ...editForm }
  }
  
  ElMessage.success(`已保存对 ${editForm.entity} 的编辑`)
  editDialogVisible.value = false
}

// 删除对象
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除"${row.entity}"吗?`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    ElMessage.success(`已删除: ${row.entity}`)
  }).catch(() => {
    ElMessage.info('已取消删除')
  })
}

// 退出登录
const logout = () => {
  localStorage.removeItem('role')
  router.push('/login')
}

// 处理每页显示数量变化
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
}

// 解密状态和表单
const decryptDialogVisible = ref(false)
const decryptFormRef = ref(null)
const decryptForm = reactive({
  objectId: '',
  token: ''
})
const decryptFormRules = {
  objectId: [{ required: true, message: '请输入数字对象ID', trigger: 'blur' }],
  token: [{ required: true, message: '请输入token', trigger: 'blur' }]
}
// 存储解密后的对象ID列表
const decryptedObjectIds = ref([])

// 显示解密对话框
const showDecryptDialog = () => {
  decryptDialogVisible.value = true
}

// 处理解密操作
const handleDecrypt = () => {
  decryptFormRef.value.validate((valid) => {
    if (valid) {
      // 获取之前从后端接收到的token
      const receivedToken = localStorage.getItem('receivedToken')
      
      if (!receivedToken) {
        ElMessage.error('未找到有效token，请先申请token')
        return false
      }
      
      // 比对输入的token与接收到的token是否一致
      if (decryptForm.token === receivedToken) {
        // token一致，解密成功
        isDecrypted.value = true
        
        // 处理多个ID，分割并去除空格
        const idList = decryptForm.objectId.split(',').map(id => id.trim()).filter(id => id);
        
        // 保存解密对象ID列表
        decryptedObjectIds.value = idList
        // 为了兼容现有代码，如果只有一个ID，也设置单独的变量
        decryptedObjectId.value = idList.length === 1 ? idList[0] : ''
        
        decryptDialogVisible.value = false
        localStorage.removeItem('receivedToken') // 清除已使用的token
        
        // 从API获取最新数据
        fetchLatestDataFromApi()
        
        ElMessage.success('解密成功')
      } else {
        // token不一致，解密失败
        ElMessage.error('解密失败：token无效')
      }
    } else {
      ElMessage.error('请填写完整的解密信息')
      return false
    }
  })
}

// 处理申请token操作
const handleRequestToken = () => {
  // 检查是否已填写数字对象ID
  if (!decryptForm.objectId) {
    ElMessage.warning('请先填写数字对象ID')
    return
  }
  
  // 显示申请中信息
  ElMessage.info(`正在申请token，请稍候...`)
  
  // 使用后端API获取token
  const apiUrl = 'http://localhost:8080/api/getToken'
  console.log('正在请求token，URL:', apiUrl)
  
  fetch(apiUrl, {
    method: 'GET',
    headers: {
      'Accept': 'application/json'
    }
  })
  .then(response => {
    if (!response.ok) {
      throw new Error(`请求失败: ${response.status}`)
    }
    return response.json()
  })
  .then(data => {
    console.log('收到API响应:', data)
    
    // 验证响应格式并提取token
    if (data && data.code === 1 && data.msg === 'success' && data.data) {
      // 提取token值
      const token = data.data
      console.log('成功获取token:', token)
      
      // 将token填入输入框
      decryptForm.token = token
      
      // 保存token到localStorage用于验证
      localStorage.setItem('receivedToken', token)
      
      ElMessage.success('成功获取token')
    } else {
      throw new Error('返回数据格式不符合预期')
    }
  })
  .catch(error => {
    console.error('获取token失败:', error)
    ElMessage.error(`获取token失败: ${error.message}`)
  })
}

// Excel预览相关
const previewDialogVisible = ref(false)
const previewForm = reactive({
  id: '',
  entity: '',
  locationInfo: '',
  constraint: '',
  transferControl: '',
  status: '',
  totalCategoryValue: '',
  totalGradeValue: '',
  classificationValue: '',
  levelValue: '',
  metadata: null
})

// 存储当前预览的Excel二进制数据
const excelBinaryData = ref(null)

// Excel表格数据
const excelTableData = ref([])
const isExcelLoading = ref(false)

// 处理Excel数据加载完成事件
const handleExcelDataLoaded = (data) => {
  console.log('Excel数据加载完成:', data)
}

// 处理Excel加载错误事件
const handleExcelError = (error) => {
  console.error('Excel加载错误:', error)
  ElMessage.error('加载Excel数据时出错: ' + error)
}

// 获取对象的所有键
const getObjectKeys = (dataArray) => {
  if (!dataArray || !Array.isArray(dataArray) || dataArray.length === 0) {
    return [];
  }
  
  // 获取所有对象的所有键
  const keySets = dataArray.map(item => {
    if (item && typeof item === 'object') {
      return Object.keys(item);
    }
    return [];
  });
  
  // 合并所有键集并去重
  const allKeys = [...new Set(keySets.flat())];
  
  return allKeys;
}

// 处理导出Excel功能
const handleExportExcel = () => {
  if (excelTableData.value.length === 0) {
    ElMessage.warning('没有数据可导出');
    return;
  }
  
  try {
    // 创建工作簿
    const wb = XLSX.utils.book_new();
    
    // 创建工作表
    const ws = XLSX.utils.json_to_sheet(excelTableData.value);
    
    // 添加工作表到工作簿
    XLSX.utils.book_append_sheet(wb, ws, 'Sheet1');
    
    // 导出文件名
    const fileName = `${previewForm.entity || 'excel_data'}.xlsx`;
    
    // 保存文件
    XLSX.writeFile(wb, fileName);
    
    ElMessage.success(`已成功导出 ${fileName}`);
  } catch (error) {
    console.error('导出Excel失败:', error);
    ElMessage.error(`导出Excel失败: ${error.message}`);
  }
}

// 从API获取Excel数据
const fetchExcelDataFromApi = async (objectId) => {
  if (!objectId) {
    console.error('【Excel数据】错误: 无法获取对象ID')
    ElMessage.warning('无法获取对象ID，无法显示Excel数据')
    return
  }
  
  console.log(`【Excel数据】正在从API获取数据，对象ID:`, objectId)
  isExcelLoading.value = true
  
  // 使用对象列表API
  const apiUrl = 'http://localhost:8080/api/objects/list'
  console.log('【Excel数据】API请求URL:', apiUrl)
  
  try {
    const response = await axios.get(apiUrl)
    console.log('【Excel数据】API响应状态码:', response.status)
    
    // 从响应中查找特定ID的对象数据
    let targetObject = null
    let dataItems = null
    
    // 1. 首先在列表中查找目标对象
    if (response.data && Array.isArray(response.data)) {
      targetObject = response.data.find(item => item.id === objectId)
    } else if (response.data && response.data.list && Array.isArray(response.data.list)) {
      targetObject = response.data.list.find(item => item.id === objectId)
    } else if (response.data && response.data.data && Array.isArray(response.data.data)) {
      targetObject = response.data.data.find(item => item.id === objectId)
    }
    
    // 2. 如果找到了目标对象，尝试提取其dataItems和分类分级值
    if (targetObject) {
      console.log(`【Excel数据】找到ID为${objectId}的对象:`, targetObject)
      
      // 提取分类分级值
      extractClassificationValues(targetObject)
      
      // 从对象中提取dataItems
      if (targetObject.dataItems && Array.isArray(targetObject.dataItems)) {
        dataItems = targetObject.dataItems
        console.log(`【Excel数据】从对象中直接提取到${dataItems.length}条dataItems`)
      } else if (targetObject.dataContent) {
        // 尝试从dataContent中解析
        try {
          const dataContent = typeof targetObject.dataContent === 'string' 
            ? JSON.parse(targetObject.dataContent) 
            : targetObject.dataContent
            
          if (dataContent && dataContent.dataItems && Array.isArray(dataContent.dataItems)) {
            dataItems = dataContent.dataItems
            console.log(`【Excel数据】从dataContent中提取到${dataItems.length}条dataItems`)
          }
        } catch (e) {
          console.error('解析dataContent失败:', e)
        }
      }
    } 
    // 3. 如果找不到目标对象，尝试从全局dataItems中过滤
    else if (response.data && response.data.dataItems && Array.isArray(response.data.dataItems)) {
      // 尝试从全局dataItems中查找与对象ID相关的数据
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
    
    // 4. 如果仍然没有找到数据，使用带有对象ID的模拟数据
    if (!dataItems || dataItems.length === 0) {
      console.log(`【Excel数据】未找到ID为${objectId}的对象数据，使用模拟数据`)
      ElMessage.info(`未找到ID为${objectId}的Excel数据，显示示例数据`)
      
      // 根据对象ID生成不同的模拟数据
      dataItems = generateMockDataForObject(objectId)
    }
    
    // 创建Excel数据
    createExcelFromDataItems(dataItems)
  } catch (error) {
    console.error('【Excel数据】API请求失败:', error.message)
    ElMessage.error(`获取Excel数据失败: ${error.message}`)
    
    // 使用带有对象ID的模拟数据
    const mockData = generateMockDataForObject(objectId)
    createExcelFromDataItems(mockData)
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

// 根据对象ID生成不同的模拟数据
const generateMockDataForObject = (objectId) => {
  // 获取ID的最后两位作为数字（用于生成不同的数据）
  const idNum = parseInt(objectId.slice(-2), 10) || 1
  
  // 根据ID生成不同类型的数据
  if (objectId.includes('user') || objectId.includes('用户')) {
    return [
      { "用户ID": "U10001", "用户名": "张三", "年龄": "28", "性别": "男", "注册日期": "2023-01-15" },
      { "用户ID": "U10002", "用户名": "李四", "年龄": "34", "性别": "男", "注册日期": "2023-02-22" },
      { "用户ID": "U10003", "用户名": "王五", "年龄": "26", "性别": "女", "注册日期": "2023-03-08" },
      { "用户ID": "U10004", "用户名": "赵六", "年龄": "31", "性别": "男", "注册日期": "2023-04-19" },
      { "用户ID": "U10005", "用户名": "钱七", "年龄": "29", "性别": "女", "注册日期": "2023-05-25" }
    ]
  } else if (objectId.includes('order') || objectId.includes('订单')) {
    return [
      { "订单ID": "O20001", "用户ID": "U10001", "商品": "笔记本电脑", "金额": "6999", "下单日期": "2023-06-12" },
      { "订单ID": "O20002", "用户ID": "U10002", "商品": "手机", "金额": "4299", "下单日期": "2023-06-18" },
      { "订单ID": "O20003", "用户ID": "U10003", "商品": "耳机", "金额": "799", "下单日期": "2023-06-25" },
      { "订单ID": "O20004", "用户ID": "U10004", "商品": "平板电脑", "金额": "3599", "下单日期": "2023-07-03" },
      { "订单ID": "O20005", "用户ID": "U10005", "商品": "智能手表", "金额": "1599", "下单日期": "2023-07-10" }
    ]
  } else if (objectId.includes('product') || objectId.includes('产品')) {
    return [
      { "产品ID": "P30001", "产品名称": "华为MateBook", "类别": "笔记本电脑", "价格": "6999", "库存": "120" },
      { "产品ID": "P30002", "产品名称": "iPhone 14", "类别": "手机", "价格": "5999", "库存": "350" },
      { "产品ID": "P30003", "产品名称": "AirPods Pro", "类别": "耳机", "价格": "1999", "库存": "500" },
      { "产品ID": "P30004", "产品名称": "iPad Air", "类别": "平板电脑", "价格": "4599", "库存": "230" },
      { "产品ID": "P30005", "产品名称": "Apple Watch", "类别": "智能手表", "价格": "2999", "库存": "180" }
    ]
  } else if (objectId.includes('inventory') || objectId.includes('库存')) {
    return [
      { "仓库编号": "WH001", "产品ID": "P30001", "产品名称": "华为MateBook", "库存数量": "120", "更新日期": "2023-07-01" },
      { "仓库编号": "WH001", "产品ID": "P30002", "产品名称": "iPhone 14", "库存数量": "350", "更新日期": "2023-07-01" },
      { "仓库编号": "WH001", "产品ID": "P30003", "产品名称": "AirPods Pro", "库存数量": "500", "更新日期": "2023-07-01" },
      { "仓库编号": "WH002", "产品ID": "P30004", "产品名称": "iPad Air", "库存数量": "230", "更新日期": "2023-07-01" },
      { "仓库编号": "WH002", "产品ID": "P30005", "产品名称": "Apple Watch", "库存数量": "180", "更新日期": "2023-07-01" }
    ]
  } else {
    // 通用数据
    return [
      { "姓名": `${idNum}-张三`, "rowNumber": "1", "性别": "男", "对象ID": objectId },
      { "姓名": `${idNum}-李四`, "rowNumber": "2", "性别": "男", "对象ID": objectId },
      { "姓名": `${idNum}-王五`, "rowNumber": "3", "性别": "女", "对象ID": objectId },
      { "姓名": `${idNum}-赵六`, "rowNumber": "4", "性别": "男", "对象ID": objectId },
      { "姓名": `${idNum}-钱七`, "rowNumber": "5", "性别": "女", "对象ID": objectId }
    ]
  }
}

// 创建Excel数据
const createExcelFromDataItems = (dataItems) => {
  try {
    // 创建工作簿和工作表
    const wb = XLSX.utils.book_new()
    const ws = XLSX.utils.json_to_sheet(dataItems)
    XLSX.utils.book_append_sheet(wb, ws, "数据")
    
    // 转换为二进制数据
    const excelBuffer = XLSX.write(wb, { bookType: 'xlsx', type: 'array' })
    const blob = new Blob([excelBuffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    
    // 设置Excel文件
    excelBinaryData.value = blob
    
    // 更新表格数据，用于直接显示
    excelTableData.value = dataItems
    
    isExcelLoading.value = false
    ElMessage.success(`成功获取${dataItems.length}条数据记录`)
  } catch (error) {
    console.error('【Excel数据】创建Excel数据失败:', error)
    ElMessage.error(`创建Excel数据失败: ${error.message}`)
    isExcelLoading.value = false
  }
}

// 预览实体
const previewEntity = (row) => {
  // 设置预览表单数据
  previewForm.id = row.id
  previewForm.entity = row.entity
  previewForm.locationInfo = row.locationInfo
  previewForm.constraint = row.constraint
  previewForm.transferControl = row.transferControl
  previewForm.status = row.status || ''
  
  // 清空分类分级值，等待API数据填充
  previewForm.totalCategoryValue = ''
  previewForm.totalGradeValue = ''
  previewForm.classificationValue = ''
  previewForm.levelValue = ''
  
  // 提取元数据
  previewForm.metadata = extractMetadata(row)
  
  // 清空当前Excel数据
  excelBinaryData.value = null
  excelTableData.value = []
  
  // 显示预览对话框
  previewDialogVisible.value = true
  
  // 从API获取Excel数据
  fetchExcelDataFromApi(row.id)
}

// 处理元数据字符串的函数
const processMetadataString = (metadataString) => {
  if (!metadataString) {
    return {  // 返回一个默认的元数据对象，而不是空对象
      dataName: '未知数据',
      sourceUnit: '未知来源',
      contactPerson: '未指定',
      contactPhone: '未提供',
      resourceSummary: '无描述',
      fieldClassification: '未分类'
    }
  }
  
  // 检查是否已经是对象
  if (typeof metadataString === 'object') {
    return metadataString
  }
  
  // 修复JSON字符串中可能存在的常见问题
  let cleanString = metadataString.toString()
  
  try {
    // 处理双重转义的情况 (例如: "{\"key\":\"value\"}")
    
    // 首先尝试去掉外层引号，处理字符串形式的JSON
    if (cleanString.startsWith('"') && cleanString.endsWith('"')) {
      const unquoted = cleanString.slice(1, -1).replace(/\\"/g, '"')
      cleanString = unquoted
    }
    
    // 处理被转义多次的情况
    if (cleanString.includes('\\\"') || cleanString.includes('\\\\')) {
      cleanString = cleanString.replace(/\\\\/g, '\\').replace(/\\"/g, '"')
    }
    
    // 修复结尾多余的]}问题
    if (cleanString.includes('"]}"') && !cleanString.endsWith('"}')) {
      cleanString = cleanString.replace('"]}"', '"}')
    }
    if (cleanString.includes('"]}",')) {
      cleanString = cleanString.replace('"]}",', '"}')
    }
    
    // 修复开头缺少{的问题
    if (!cleanString.startsWith('{') && cleanString.includes('":"')) {
      cleanString = '{' + cleanString
    }
    
    // 修复结尾缺少}的问题
    if (!cleanString.endsWith('}') && cleanString.includes('":"')) {
      cleanString = cleanString + '}'
    }
    
    // 尝试直接解析清理后的字符串
    try {
      const parsed = JSON.parse(cleanString)
      
      // 确保返回对象包含预期的字段
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
      // 尝试使用正则表达式提取键值对
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
      
      // 如果所有尝试都失败，返回默认元数据
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

// 检查数据的各种可能位置，提取元数据
const extractMetadata = (row) => {
  if (!row) {
    return createDefaultMetadata('未知实体')
  }
  
  // 直接检查row中的metadata对象
  if (row.metadata && typeof row.metadata === 'object') {
    // 确保所有必要字段都存在
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
  
  // 检查row中的metadataJson字段
  if (row.metadataJson) {
    try {
      const parsedMetadata = processMetadataString(row.metadataJson)
      return parsedMetadata
    } catch (e) {
      console.warn('解析row.metadataJson失败:', e)
    }
  }
  
  // 检查dataContent字段中的元数据
  if (row.dataContent) {
    try {
      // 尝试解析dataContent
      const contentObj = typeof row.dataContent === 'string' ? 
        JSON.parse(row.dataContent) : row.dataContent
      
      if (contentObj && contentObj.metadataJson) {
        const parsedMetadata = processMetadataString(contentObj.metadataJson)
        return parsedMetadata
      }
      
      // 直接从dataContent中提取元数据字段
      if (contentObj && (contentObj.metadata || contentObj.dataName || contentObj.sourceUnit || 
          contentObj.contactPerson || contentObj.contactPhone)) {
        
        // 优先使用metadata对象（如果存在）
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
      // 处理位置信息JSON字符串
      const locationInfo = JSON.parse(row.locationInfoJson)
      if (locationInfo && locationInfo.locations) {
        // 更新locationInfo显示格式
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
  
  // 创建默认元数据
  return createDefaultMetadata(row.entity)
}

// 创建默认元数据的辅助函数
const createDefaultMetadata = (entityName) => {
  entityName = entityName || '未知实体'
  let sourceUnit = '数据部'
  let contactPerson = '王主任'
  
  // 根据实体名称定制一些元数据
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
  // 防止未定义
  if (!previewForm.metadata) return null
  
  // 直接检查顶级对象
  if (previewForm.metadata[fieldName]) {
    return previewForm.metadata[fieldName]
  }
  
  // 检查嵌套对象
  const checkNestedObject = (obj, field) => {
    // 不是对象，返回null
    if (typeof obj !== 'object' || obj === null) return null
    
    // 直接检查当前对象是否有该字段
    if (obj[field] !== undefined) return obj[field]
    
    // 递归检查所有属性
    for (const key in obj) {
      if (typeof obj[key] === 'object' && obj[key] !== null) {
        const result = checkNestedObject(obj[key], field)
        if (result !== null) return result
      }
    }
    
    return null
  }
  
  // 尝试在嵌套对象中查找
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
  
  // 如果包含冒号，分离前缀和内容
  if (text.includes(':')) {
    const parts = text.split(':')
    return `<span class="constraint-prefix">${parts[0]}:</span>${parts[1]}`
  }
  
  return text
}

// 处理下载数字对象
const handleDownload = () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要下载的数字对象');
    return;
  }

  // 对每个选中的对象进行处理
  selectedRows.value.forEach(row => {
    if (row.excelData) {
      // 如果有Excel数据，直接下载
      downloadExcelFile(row);
    } else {
      // 如果没有Excel数据，提示用户先点击实体名预览
      ElMessage.info(`${row.entity} 没有可下载的数据，请先点击实体名进行预览`);
    }
  });
}

// 下载Excel文件
const downloadExcelFile = (row) => {
  try {
    // 创建Blob对象
    const blob = new Blob([row.excelData], { 
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' 
    });
    
    // 创建下载链接
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = `${row.entity}.xlsx`;
    
    // 添加到文档并触发点击
    document.body.appendChild(link);
    link.click();
    
    // 清理
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
    decryptForm.token = '';
    showDecryptDialog();
  }).catch(() => {
    // 用户取消操作
  });
}

// 从API获取最新数据
const fetchLatestDataFromApi = async () => {
  try {
    ElMessage.info('正在从API获取最新数据...')
    
    // 使用API获取最新对象数据
    const apiUrl = 'http://localhost:8080/api/objects/list'
    const response = await axios.get(apiUrl)
    
    if (response.data) {
      // 更新表格数据
      let newData = []
      
      if (Array.isArray(response.data)) {
        newData = response.data
      } else if (response.data.list && Array.isArray(response.data.list)) {
        newData = response.data.list
      } else if (response.data.data && Array.isArray(response.data.data)) {
        newData = response.data.data
      }
      
      if (newData.length > 0) {
        // 更新服务中的数据
        dataObjectService.updateDataObjects(newData)
        ElMessage.success(`成功获取${newData.length}条最新数据`)
      } else {
        ElMessage.warning('API返回的数据为空')
      }
    }
  } catch (error) {
    console.error('获取最新数据失败:', error)
    ElMessage.error(`获取最新数据失败: ${error.message}`)
  }
}
</script>

<style scoped>
.datasource-container {
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #ffffff;
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
  background-color: #ffffff;
  overflow: auto;
  box-sizing: border-box;
}

.content-card {
  background-color: #ffffff;
  border-radius: 4px;
  padding: 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
  height: calc(100vh - 92px);
  width: 100%;
  box-sizing: border-box;
  overflow: auto;
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
  justify-content: space-between;
  margin-bottom: 12px;
}

.search-input {
  width: 300px;
}

.action-buttons {
  display: flex;
  gap: 12px;
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
  padding: 8px 0;
  text-align: center;
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
  background-color: #f5f5f5;
  color: #8c8c8c;
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
  font-size: 12px;
  line-height: 1.5;
}

.classification-level-item .label {
  font-weight: bold;
  color: #606266;
}

.classification-level-item .value {
  color: #409EFF;
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
  color: #409EFF;
  font-weight: bold;
}

/* 表头信息部分样式 */
</style> 