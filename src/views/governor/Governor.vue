<template>
  <div class="datasource-container">
    <!-- 头部导航 -->
    <AppHeader role-name="治理方" @logout="logout" />
    
    <!-- 主内容区域 -->
    <div class="main-content">
      <!-- 标签页 -->
      <div class="content-card">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="数字对象列表" name="objectList">
            
            <!-- 状态筛选按钮 -->
            <div class="status-filter">
              <el-button 
                :class="['status-btn', { active: currentStatus === '' }]" 
                @click="setStatus('')"
              >全部数字对象</el-button>
              <el-button 
                :class="['status-btn', { active: currentStatus === '待检验' }]" 
                @click="setStatus('待检验')"
              >待检验</el-button>
              <el-button 
                :class="['status-btn', { active: currentStatus === '已合格' }]" 
                @click="setStatus('已合格')"
              >已合格</el-button>
              <el-button 
                :class="['status-btn', { active: currentStatus === '不合格' }]" 
                @click="setStatus('不合格')"
              >不合格</el-button>
            </div>
            
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
                <el-button type="primary" plain @click="showDecryptDialog">解密</el-button>
                <!-- <el-button type="primary">导出检验</el-button> -->
              </div>
            </div>
            
            <!-- 数据表格 -->
            <div class="table-container">
              <div v-if="!isDecrypted" class="data-locked-placeholder">
                <el-icon class="locked-icon"><Lock /></el-icon>
                <p>数据已加密，请点击"解密"按钮进行解密操作</p>
              </div>
              <el-table
                v-else
                :data="filteredTableData"
                style="width: 100%"
                :cell-style="{ padding: '8px 0', textAlign: 'center' }"
                :header-cell-style="{ padding: '10px 0', background: '#f5f7fa', color: '#606266', fontWeight: 'bold', textAlign: 'center' }"
                border
                height="100%"
                fit
              >
                <el-table-column prop="id" label="ID" width="240" align="center">
                  <template #default="scope">
                    <div class="id-cell">{{ scope.row.id }}</div>
                  </template>
                </el-table-column>
                <el-table-column prop="entity" label="实体" width="100" align="center">
                  <template #default="scope">
                    <el-link type="primary" @click="previewEntity(scope.row)">{{ scope.row.entity }}</el-link>
                  </template>
                </el-table-column>
                <el-table-column prop="locationInfo" label="定位信息" min-width="150" align="center" />
                <el-table-column prop="constraint" label="约束条件" min-width="350" align="center">
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
                <el-table-column prop="auditInfo" label="审计控制信息" width="130" align="center">
                  <template #default="scope">
                    <el-link type="primary">{{ scope.row.auditInfo }}</el-link>
                  </template>
                </el-table-column>
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
                <el-table-column prop="status" label="状态" width="100" align="center">
                  <template #default="scope">
                    <span :class="['status-tag', getStatusClass(scope.row.status)]">
                      {{ scope.row.status }}
                    </span>
                  </template>
                </el-table-column>
                <el-table-column v-if="!isQualifiedStatus && currentStatus !== '待检验'" prop="feedback" label="反馈意见" min-width="160" align="center">
                  <template #default="scope">
                    <!-- 优先使用row.feedback -->
                    <span v-if="scope.row.feedback" :class="['feedback-text', getFeedbackClass(scope.row.status)]">
                      {{ scope.row.feedback }}
                    </span>
                    
                    <!-- 其次尝试从dataContent中提取 -->
                    <span v-else-if="scope.row.dataContent" :class="['feedback-text', getFeedbackClass(scope.row.status)]">
                      {{ extractFeedback(scope.row.dataContent) }}
                    </span>
                    
                    <!-- 如果是客户反馈实体且状态为不合格，强制显示 -->
                    <span v-else-if="scope.row.entity === '客户反馈' && scope.row.status === '不合格'" :class="['feedback-text', getFeedbackClass(scope.row.status)]">
                      数据格式错误
                    </span>
                    
                    <!-- 没有反馈信息 -->
                    <span v-else>-</span>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="210" align="center">
                  <template #default="scope">
                    <div class="status-buttons">
                      <el-button type="primary" size="small" plain @click="updateStatus(scope.row, '审查中')">审查</el-button>
                      <el-button type="success" size="small" plain :disabled="scope.row.status === '已合格'" @click="updateStatus(scope.row, '已合格')">正确</el-button>
                      <el-button type="danger" size="small" plain :disabled="scope.row.status === '不合格'" @click="updateStatus(scope.row, '不合格')">错误</el-button>
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
    width="30%"
    :close-on-click-modal="false"
    :show-close="true"
    draggable
    class="decrypt-dialog"
  >
    <el-form :model="decryptForm" label-width="120px" ref="decryptFormRef" :rules="decryptFormRules">
      <el-form-item label="token:" prop="token">
        <el-input v-model="decryptForm.token" placeholder="请输入token"></el-input>
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
        <!-- 新的基本信息表格样式 -->
        <div class="basic-info-table">
          <span class="info-item"><strong>实体：</strong>{{ previewForm.entity }}</span>
          <span class="info-item"><strong>定位信息：</strong>{{ previewForm.locationInfo }}</span>
          <span class="info-item constraint-info" :title="Array.isArray(previewForm.constraint) ? previewForm.constraint.join(', ') : previewForm.constraint"><strong>约束条件：</strong>{{ Array.isArray(previewForm.constraint) ? previewForm.constraint.join(', ') : previewForm.constraint }}</span>
          <span class="info-item"><strong>传输控制操作：</strong>{{ Array.isArray(previewForm.transferControl) ? previewForm.transferControl.join(', ') : previewForm.transferControl }}</span>
          <span class="info-item"><strong>状态：</strong>{{ previewForm.status }}</span>
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

  <!-- 审核报告弹窗 -->
  <ReviewReport v-model:visible="reportDialogVisible" />
</template>

<script setup>
import { ref, computed, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Lock, Document, UploadFilled, Download } from '@element-plus/icons-vue'
import * as XLSX from 'xlsx'
import ExcelPreview from '../../components/ExcelPreview.vue'
import AppHeader from '../../components/AppHeader.vue'
import CommonPagination from '../../components/CommonPagination.vue'
import ReviewReport from '@/components/governor/ReviewReport.vue'
import dataObjectService from '../../services/dataObjectService'
import reportService from '../../services/reportService'
import { ensureArray, advancedSearch } from '../../utils/searchUtils'
import axios from 'axios'

const router = useRouter()
const activeTab = ref('objectList')
const currentStatus = ref('') // 默认显示全部数字对象
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(5) // 改为默认显示5条
const isDecrypted = ref(false)
const selectedRows = ref([])

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
    console.log('治理方收到数据变化:', newData)
    // 无需手动更新tableData，因为是响应式引用
  })
})

// 计算实际数据量
const totalCount = computed(() => {
  let result = tableData.value;
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

  // 状态过滤
  if (currentStatus.value) {
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

// 更新数字对象状态
const updateStatus = async (row, newStatus) => {
  // 本地模式标志，改为false以启用调用后端API
  const localModeOnly = false; // 设置为false表示将更新发送到后端数据库
  
  // 特殊处理"库存管理"实体的审查功能
  if (newStatus === '审查中' && row.entity === '库存管理') {
    try {
      // 直接打开报告对话框，不显示加载提示
      reportDialogVisible.value = true;
      
      // 更新UI中的状态
      const index = tableData.value.findIndex(item => item.id === row.id);
      if (index !== -1) {
        // 保存原始状态，仅在UI中短暂显示"审查中"状态
        const originalStatus = tableData.value[index].status;
        tableData.value[index].status = '审查中';
        
        // 延迟后恢复原始状态，以避免数据不一致
        setTimeout(() => {
          if (tableData.value[index]) {
            tableData.value[index].status = originalStatus;
          }
        }, 5000); // 5秒后恢复状态
      }
    } catch (error) {
      console.error('审查过程出错:', error);
      ElMessage.error(`审查失败: ${error.message || '未知错误'}`);
    }
  }
  // 处理已合格或待检验状态
  else if (newStatus === '已合格' || newStatus === '待检验') {
    try {
      const result = await dataObjectService.updateObjectStatusViaApi(row.id, newStatus, '', localModeOnly)
      if (result) {
        ElMessage.success(`${row.entity} 已更新为"${newStatus}"状态${localModeOnly ? '（本地模式）' : '，并已保存到后端数据库'}`)
      } else {
        ElMessage.warning(`${row.entity} 状态更新失败`)
      }
    } catch (error) {
      console.error('更新状态时出错:', error)
      ElMessage.error(`更新 ${row.entity} 状态失败: ${error.message || '未知错误'}`)
    }
  } 
  // 如果是不合格状态，弹出对话框要求输入反馈意见
  else if (newStatus === '不合格') {
    ElMessageBox.prompt('请输入不合格的反馈意见', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputValue: row.feedback || '',
      customClass: 'feedback-dialog',
      inputType: 'textarea',
      inputPlaceholder: '请详细描述不合格的原因...',
      inputValidator: (value) => {
        return value.trim() !== '' || '反馈意见不能为空'
      }
    }).then(async ({ value }) => {
      try {
        const result = await dataObjectService.updateObjectStatusViaApi(row.id, newStatus, value, localModeOnly)
        if (result) {
          ElMessage.success(`${row.entity} 已更新为"不合格"状态${localModeOnly ? '（本地模式）' : '，并已保存到后端数据库'}`)
        } else {
          ElMessage.warning(`${row.entity} 状态更新失败`)
        }
      } catch (error) {
        console.error('更新状态时出错:', error)
        ElMessage.error(`更新 ${row.entity} 状态失败: ${error.message || '未知错误'}`)
      }
    }).catch(() => {
      ElMessage.info('已取消状态更新')
    })
  }
  // 其他情况，如普通的审查状态更新（非库存管理实体）
  else if (newStatus === '审查中') {
    try {
      const result = await dataObjectService.updateObjectStatusViaApi(row.id, newStatus, '审查进行中', localModeOnly)
      if (result) {
        ElMessage.success(`${row.entity} 已更新为"审查中"状态${localModeOnly ? '（本地模式）' : '，并已保存到后端数据库'}`)
      } else {
        ElMessage.warning(`${row.entity} 状态更新失败`)
      }
    } catch (error) {
      console.error('更新状态时出错:', error)
      ElMessage.error(`更新 ${row.entity} 状态失败: ${error.message || '未知错误'}`)
    }
  }
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
  token: ''
})
const decryptFormRules = {
  token: [{ required: true, message: '请输入token', trigger: 'blur' }]
}

// 显示解密对话框
const showDecryptDialog = () => {
  decryptDialogVisible.value = true
}

// 处理申请token操作
const handleRequestToken = () => {
  // 显示申请中信息
  ElMessage.info('正在申请token，请稍候...')
  
  // 直接使用完整的URL确保能够正确连接
  const apiUrl = 'http://localhost:8080/api/getToken'
  console.log('正在请求token，URL:', apiUrl)
  
  // 简单直接的fetch请求
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
        decryptDialogVisible.value = false
        localStorage.removeItem('receivedToken') // 清除已使用的token
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
  metadata: null // 添加元数据字段
})

// 存储当前预览的Excel二进制数据
const excelBinaryData = ref(null)

// 处理Excel数据加载完成事件
const handleExcelDataLoaded = (data) => {
  console.log('Excel数据加载完成:', data)
}

// 处理Excel加载错误事件
const handleExcelError = (error) => {
  console.error('Excel加载错误:', error)
  ElMessage.error('加载Excel数据时出错: ' + error)
}

// Excel表格数据
const excelTableData = ref([])
const isExcelLoading = ref(false)

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
  previewForm.status = row.status
  
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
    return {
      dataName: '未知数据',
      sourceUnit: '未知来源',
      contactPerson: '未指定',
      contactPhone: '未提供',
      resourceSummary: '无',
      fieldClassification: '未分类'
    }
  }
  
  // 检查是否已经是对象
  if (typeof metadataString === 'object') {
    return {
      ...metadataString,
      contactPhone: metadataString.contactPhone || '未提供' // 确保contactPhone字段存在
    }
  }
  
  // 清理字符串
  let cleanString = metadataString.toString()
  
  // 处理外层引号问题
  if (cleanString.startsWith('"') && cleanString.endsWith('"')) {
    cleanString = cleanString.slice(1, -1).replace(/\\"/g, '"')
  }
  
  // 处理转义问题
  if (cleanString.includes('\\\"') || cleanString.includes('\\\\')) {
    cleanString = cleanString.replace(/\\\\/g, '\\').replace(/\\"/g, '"')
  }
  
  try {
    // 尝试解析JSON
    const parsed = JSON.parse(cleanString)
    return {
      dataName: parsed.dataName || '未知数据',
      sourceUnit: parsed.sourceUnit || '未知来源',
      contactPerson: parsed.contactPerson || '未指定',
      contactPhone: parsed.contactPhone || '未提供', // 确保包含contactPhone
      resourceSummary: parsed.resourceSummary || '无',
      fieldClassification: parsed.fieldClassification || '未分类',
      headers: parsed.headers || []
    }
  } catch (e) {
    // 正则提取键值对
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
        contactPhone: keyValuePairs.contactPhone || '未提供', // 确保包含contactPhone
        resourceSummary: keyValuePairs.resourceSummary || '无',
        fieldClassification: keyValuePairs.fieldClassification || '未分类'
      }
    }
    
    // 返回默认值
    return {
      dataName: '解析错误',
      sourceUnit: '数据部',
      contactPerson: '未知',
      contactPhone: '未知',
      resourceSummary: '元数据解析失败',
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

// 提取反馈信息
const extractFeedback = (dataContent) => {
  try {
    // 如果是字符串类型
    if (typeof dataContent === 'string') {
      // 1. 先尝试JSON解析
      try {
        const parsed = JSON.parse(dataContent);
        if (parsed && parsed.feedback) {
          return parsed.feedback;
        }
      } catch (e) {
        // JSON解析失败，继续尝试其他方法
      }
      
      // 2. 使用正则表达式提取 - 标准格式
      const match1 = dataContent.match(/"feedback"\s*:\s*"([^"]*)"/);
      if (match1 && match1[1]) {
        return match1[1];
      }
      
      // 3. 使用正则表达式提取 - 带转义的格式
      const match2 = dataContent.match(/\\"feedback\\"\\s*:\\s*\\"([^\\"]*?)\\"/);
      if (match2 && match2[1]) {
        return match2[1];
      }
      
      // 4. 直接查找关键词
      if (dataContent.includes('数据格式错误')) {
        return '数据格式错误';
      }
    } 
    // 如果是对象类型
    else if (typeof dataContent === 'object' && dataContent !== null) {
      // 直接访问feedback属性
      if (dataContent.feedback) {
        return dataContent.feedback;
      }
      
      // 尝试从数据子对象中获取
      if (dataContent.data && dataContent.data.feedback) {
        return dataContent.data.feedback;
      }
    }
    
    // 没有找到任何反馈信息
    return '-';
  } catch (e) {
    return '提取失败';
  }
}

// 获取当前格式化的日期时间
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

// 审核报告弹窗
const reportDialogVisible = ref(false)

// 显示审核报告
const showReportDialog = () => {
  reportDialogVisible.value = true
}

// 添加根据状态获取反馈意见类名的方法
const getFeedbackClass = (status) => {
  switch (status) {
    case '已合格': return 'status-success'
    case '不合格': return 'status-error'
    case '待检验': return 'status-pending'
    default: return ''
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
  background-color: #e1f3d8;
  color: #67c23a;
}

.status-error {
  background-color: #fde2e2;
  color: #f56c6c;
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
  gap: 12px;
}

/* 按钮组样式 */
.status-buttons {
  display: flex;
  gap: 2px;
  justify-content: center;
}

.status-buttons :deep(.el-button--small) {
  padding: 5px 8px;
  min-width: 44px;
}

/* 自定义禁用按钮样式 */
.status-buttons :deep(.el-button--success.is-disabled) {
  color: #67c23a !important;
  background-color: #c2e7b0 !important;
  border-color: #c2e7b0 !important;
}

.status-buttons :deep(.el-button--danger.is-disabled) {
  color: #f56c6c !important;
  background-color: #fab6b6 !important;
  border-color: #fab6b6 !important;
}

/* 确保禁用状态下鼠标悬停样式保持一致 */
.status-buttons :deep(.el-button--success.is-disabled:hover),
.status-buttons :deep(.el-button--success.is-disabled:focus) {
  color: #67c23a !important;
  background-color: #c2e7b0 !important;
  border-color: #c2e7b0 !important;
}

.status-buttons :deep(.el-button--danger.is-disabled:hover),
.status-buttons :deep(.el-button--danger.is-disabled:focus) {
  color: #f56c6c !important;
  background-color: #fab6b6 !important;
  border-color: #fab6b6 !important;
}

/* 自定义反馈意见弹窗样式 */
:deep(.feedback-dialog) {
  width: 500px !important;
}

:deep(.feedback-dialog .el-message-box__input) {
  padding-top: 8px;
}

:deep(.feedback-dialog .el-textarea__inner) {
  min-height: 120px !important;
  font-size: 14px;
}

:deep(.feedback-dialog .el-message-box__header) {
  padding-bottom: 15px;
}

:deep(.feedback-dialog .el-message-box__content) {
  padding: 20px;
}

/* 数据锁定占位样式 */
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

/* 解密对话框样式 */
:deep(.el-overlay) {
  overflow: hidden;
}

:deep(.el-dialog) {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  position: absolute;
  top: 55%;
  left: 50%;
  transform: translate(-50%, -50%);
  margin: 0 !important;
  transition: all 0.3s ease-in-out;
  max-height: 90vh;
  max-width: 95vw;
}

:deep(.el-overlay-dialog) {
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: auto;
  position: fixed;
}

:deep(.el-dialog__header) {
  padding: 20px;
  margin-right: 0;
  text-align: center;
  border-bottom: 1px solid #f0f0f0;
  cursor: move;
}

:deep(.el-dialog__headerbtn) {
  top: 18px;
}

:deep(.el-dialog__title) {
  font-weight: bold;
  font-size: 18px;
}

:deep(.el-dialog__body) {
  padding: 30px 20px;
}

:deep(.el-dialog__footer) {
  padding: 10px 20px 20px;
  text-align: center;
}

:deep(.el-button--primary) {
  min-width: 90px;
}

/* 弹出对话框定位优化 */
:deep(.decrypt-dialog),
:deep(.edit-dialog) {
  display: flex;
  align-items: center;
  justify-content: center;
}

:deep(.decrypt-dialog .el-dialog),
:deep(.edit-dialog .el-dialog) {
  margin-top: 0 !important;
}

/* 确保解密对话框在小屏幕上也显示适当大小 */
@media screen and (max-width: 768px) {
  :deep(.decrypt-dialog .el-dialog) {
    width: 90% !important;
  }
  
  :deep(.edit-dialog .el-dialog) {
    width: 95% !important;
  }
}

/* 确保弹窗在出现时有平滑的动画效果 */
:deep(.el-dialog-fade-enter-from),
:deep(.el-dialog-fade-leave-to) {
  opacity: 0;
  transform: translate(-50%, -40%);
}

:deep(.el-dialog-fade-enter-active),
:deep(.el-dialog-fade-leave-active) {
  transition: all 0.3s ease-out;
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
  width: 98%;
  max-width: 1200px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  border: 1px solid #eaeaea;
}

.metadata-items {
  display: flex;
  flex-wrap: nowrap; /* 防止换行 */
  justify-content: center;
  overflow-x: auto; /* 如果内容溢出，允许水平滚动 */
  padding-bottom: 3px; /* 为滚动条留出空间 */
  scrollbar-width: thin;
  -ms-overflow-style: none; /* IE and Edge */
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
  background-color: transparent;
  border-radius: 0;
  box-shadow: none;
  border: none;
  margin: 0 8px;
  white-space: nowrap; /* 防止内容自动换行 */
  flex-shrink: 0; /* 防止项目被压缩 */
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

/* 约束条件相关样式 */
.constraint-container {
  text-align: left;
  padding: 4px 8px;
}

.constraint-row {
  display: flex;
  margin-bottom: 8px;
  gap: 20px;
}

.constraint-item-pair {
  flex: 1;
  min-width: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  padding: 0 5px;
}

:deep(.constraint-prefix) {
  font-weight: bold;
  color: #303133;
}

/* 反馈意见样式 */
.feedback-text {
  font-weight: 500;
  font-size: 13px;
  display: inline-block;
  padding: 2px 6px;
  border-radius: 4px;
}

/* 反馈意见状态样式 */
.feedback-text.status-success {
  color: #67c23a;
  background-color: #f0f9eb;
}

.feedback-text.status-error {
  color: #f56c6c;
  background-color: #fef0f0;
}

.feedback-text.status-pending {
  color: #e6a23c;
  background-color: #fdf6ec;
}

/* ID列样式 */
.id-cell {
  white-space: normal;
  overflow: visible;
  text-overflow: clip;
  max-width: 240px;
  width: 100%;
  padding: 0 8px;
  font-family: monospace;
  font-size: 12px;
  font-weight: bold;
  word-break: break-all;
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
  margin: 2px 5px;
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