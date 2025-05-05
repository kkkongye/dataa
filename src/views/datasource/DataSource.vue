<template>
  <div class="datasource-container">
    <!-- 头部导航 -->
    <AppHeader role-name="数源方" @logout="logout" />
    
    <!-- API错误提示 -->
    <div v-if="apiErrorVisible" class="api-error-alert">
      <el-alert
        title="接口连接错误"
        type="warning"
        description="无法连接到后端API服务，可能原因: 1.后端服务未启动 2.跨域(CORS)限制 3.网络连接问题"
        show-icon
        :closable="true"
        @close="apiErrorVisible = false"
      >
        <template #default>
          <div class="api-error-content">
            <p>可能的解决方案:</p>
            <ol>
              <li>确保后端服务在 http://localhost:8080 正常运行</li>
            </ol>
            <div class="api-error-actions">
              <el-button size="small" @click="apiErrorVisible = false">知道了</el-button>
              <el-button size="small" type="primary" @click="refreshData">重试连接</el-button>
            </div>
          </div>
        </template>
      </el-alert>
    </div>
    
    <!-- 主内容区域 -->
    <div class="main-content">
      <!-- 标签页 -->
      <div class="content-card">
        <!-- 删除刷新按钮容器 -->
        <el-tabs v-model="activeTab">
          <el-tab-pane label="数字对象列表" name="objectList">
            <!-- 使用ObjectList组件代替原有的列表内容 -->
            <ObjectList 
              :data="filteredTableData"
              v-model:current-status="currentStatus"
              v-model:search-keyword="searchKeyword"
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :total-count="totalCount"
              :is-qualified-status="isQualifiedStatus"
              @selection-change="handleSelectionChange"
              @sort-change="handleSortChange"
              @edit="handleEdit"
              @delete="handleDelete"
              @preview="previewEntity"
              @create="showCreateDialog"
              @export="handleExport"
              @update:data="handleDataUpdate"
            />
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </div>
  
  <!-- 使用Element Plus直接实现的编辑对话框 -->
  <el-dialog
    v-model="editDialogVisible"
    title="编辑数字对象"
    width="40%"
    :close-on-click-modal="false"
    draggable
  >
    <el-form :model="editForm" label-width="150px" ref="editFormRef" :rules="formRules">
      <el-form-item label="ID：" v-if="editForm.id !== undefined && editForm.id !== null">
        <el-input v-model="editForm.id" disabled placeholder="自动生成" style="width: 300px;"></el-input>
      </el-form-item>
      <el-form-item label="实体：" prop="entity">
        <div style="display: flex; align-items: center; gap: 10px;">
          <el-input v-model="editForm.entity" placeholder="请输入实体名称" style="width: 300px;"></el-input>
          <el-upload
            action="#"
            :auto-upload="false"
            :show-file-list="false"
            :limit="1"
            :on-change="handleEditFileChange"
            :before-upload="beforeUpload"
          >
            <el-button type="primary">上传Excel</el-button>
          </el-upload>
        </div>
      </el-form-item>
      
      <!-- 元数据区域 -->
      <el-form-item label="数据名称：" prop="metadata.dataName">
        <el-input v-model="editForm.metadata.dataName" placeholder="请输入数据名称" style="width: 300px;"></el-input>
      </el-form-item>
      <el-form-item label="来源单位：" prop="metadata.sourceUnit">
        <el-input v-model="editForm.metadata.sourceUnit" placeholder="请输入来源单位" style="width: 300px;"></el-input>
      </el-form-item>
      <el-form-item label="联系人：" prop="metadata.contactPerson">
        <el-input v-model="editForm.metadata.contactPerson" placeholder="请输入联系人" style="width: 300px;"></el-input>
      </el-form-item>
      <el-form-item label="联系电话：" prop="metadata.contactPhone">
        <el-input v-model="editForm.metadata.contactPhone" placeholder="请输入联系电话" style="width: 300px;"></el-input>
      </el-form-item>
      
      <el-form-item label="资源摘要：" prop="metadata.resourceSummary">
        <el-input v-model="editForm.metadata.resourceSummary" placeholder="请输入资源摘要" style="width: 300px;"></el-input>
      </el-form-item>
      
      <el-form-item label="领域分类：" prop="metadata.fieldClassification">
        <el-input v-model="editForm.metadata.fieldClassification" placeholder="请输入领域分类" style="width: 300px;"></el-input>
      </el-form-item>
      
      <el-form-item label="定位信息：" prop="locationInfo">
        <div style="display: flex; align-items: center; gap: 10px;">
          <el-input v-model="editForm.locationInfo.row" placeholder="例：0-4" style="width: 120px;"></el-input>
          <span>行</span>
          <el-input v-model="editForm.locationInfo.col" placeholder="例：0-4" style="width: 120px;"></el-input>
          <span>列</span>
        </div>
      </el-form-item>
      
      <el-form-item label="约束条件：">
        <div class="constraint-section">
          <el-form-item prop="formatConstraint">
            <div class="constraint-item">
              <label>格式约束：</label>
              <el-select v-model="editForm.formatConstraint" placeholder="请选择格式" style="width: 300px;">
                <el-option label="jpg" value="jpg"></el-option>
                <el-option label="xlsx" value="xlsx"></el-option>
                <el-option label="json" value="json"></el-option>
                <el-option label="csv" value="csv"></el-option>
                <el-option label="pdf" value="pdf"></el-option>
                <el-option label="txt" value="txt"></el-option>
              </el-select>
            </div>
          </el-form-item>
          
          <el-form-item prop="accessConstraint">
            <div class="constraint-item">
              <label>访问权限：</label>
              <el-select v-model="editForm.accessConstraint" placeholder="请选择访问权限" style="width: 300px;">
                <el-option label="只允许管理方获取" value="只允许管理方获取"></el-option>
                <el-option label="全部允许" value="全部允许"></el-option>
              </el-select>
            </div>
          </el-form-item>
          
          <el-form-item prop="pathConstraint">
            <div class="constraint-item">
              <label>传输路径约束：</label>
              <el-select v-model="editForm.pathConstraint" placeholder="请选择传输路径" style="width: 300px;">
                <el-option label="点对点" value="点对点"></el-option>
                <el-option label="广播" value="广播"></el-option>
              </el-select>
            </div>
          </el-form-item>
          
          <el-form-item prop="regionConstraint">
            <div class="constraint-item">
              <label>地域性约束：</label>
              <el-select v-model="editForm.regionConstraint" placeholder="请选择地域性约束" style="width: 300px;">
                <el-option label="内网" value="内网"></el-option>
                <el-option label="外网" value="外网"></el-option>
              </el-select>
            </div>
          </el-form-item>
          
          <el-form-item prop="shareConstraint">
            <div class="constraint-item">
              <label>共享约束：</label>
              <el-select v-model="editForm.shareConstraint" placeholder="请选择共享约束" style="width: 300px;">
                <el-option label="不允许共享" value="不允许共享"></el-option>
                <el-option label="允许共享" value="允许共享"></el-option>
              </el-select>
            </div>
          </el-form-item>
        </div>
      </el-form-item>
      
      <el-form-item label="传输控制操作：" prop="transferControl">
        <el-select v-model="editForm.transferControl" multiple placeholder="请选择传输控制操作" style="width: 300px;">
          <el-option label="可读" value="可读"></el-option>
          <el-option label="可修改" value="可修改"></el-option>
          <el-option label="可销毁" value="可销毁"></el-option>
          <el-option label="可共享" value="可共享"></el-option>
          <el-option label="可委托" value="可委托"></el-option>
        </el-select>
      </el-form-item>
    </el-form>
    
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="cancelEdit">取消</el-button>
        <el-button type="primary" @click="handleSaveEditManually">保存</el-button>
      </span>
    </template>
  </el-dialog>

  <!-- 新建对象弹窗 -->
  <CreateObjectDialog
    v-model:visible="createDialogVisible"
    :title="'新建数字对象'"
    v-model:modelValue="createForm"
    @save="saveCreateObject"
    @cancel="cancelCreate"
  />

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
        <!-- 基本信息表格 -->
        <div class="basic-info-table">
          <span class="info-item"><strong>实体：</strong>{{ previewForm.entity }}</span>
          <span class="info-item"><strong>定位信息：</strong>{{ previewForm.locationInfo }}</span>
          <span class="info-item constraint-info" :title="Array.isArray(previewForm.constraint) ? previewForm.constraint.join(', ') : previewForm.constraint"><strong>约束条件：</strong>{{ Array.isArray(previewForm.constraint) ? previewForm.constraint.join(', ') : previewForm.constraint }}</span>
          <span class="info-item"><strong>传输控制操作：</strong>{{ Array.isArray(previewForm.transferControl) ? previewForm.transferControl.join(', ') : previewForm.transferControl }}</span>
          <span class="info-item"><strong>状态：</strong>{{ previewForm.status }}</span>
        </div>
        <!-- 元数据信息显示 -->
        <div v-if="previewForm.metadata" class="metadata-section">
          <div class="metadata-items">
            <!-- 元数据项在一行显示 -->
            <div class="metadata-item">数据名称: <strong>{{ previewForm.metadata.dataName || previewForm.entity }}</strong></div>
            <div class="metadata-item">来源单位: <strong>{{ previewForm.metadata.sourceUnit || '数据部' }}</strong></div>
            <div class="metadata-item">联系人: <strong>{{ previewForm.metadata.contactPerson || '未指定' }}</strong></div>
            <div class="metadata-item">联系电话: <strong>{{ previewForm.metadata.contactPhone || '未提供' }}</strong></div>
            <div class="metadata-item">资源摘要: <strong>{{ previewForm.metadata.resourceSummary|| '无' }}</strong></div>
            <div class="metadata-item">领域分类: <strong>{{ previewForm.metadata.fieldClassification || '未分类' }}</strong></div>
            <div class="metadata-item">更新时间: <strong>{{ getCurrentDateTime() }}</strong></div>
          </div>
          <!-- 添加分类分级值显示 -->
          <div class="classification-level-section">
            <div class="classification-level-items">
              <div class="classification-level-item">
                <span class="label">分类值：</span>
                <span class="value">{{ previewForm.classificationValue || '未分类' }}</span>
              </div>
              <div class="classification-level-item">
                <span class="label">分级值：</span>
                <span class="value">{{ previewForm.levelValue || '未分级' }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <ExcelPreview
      :file="currentExcelFile"
      :title="previewForm.entity"
      :use-web-worker="true"
      :max-visible-columns="30"
      @data-loaded="handleExcelDataLoaded"
      @error="handleExcelError"
    />
    
    <template #footer v-if="currentExcelFile">
      <span class="dialog-footer">
        <el-button @click="previewDialogVisible = false">关闭</el-button>
      </span>
    </template>
  </el-dialog>

  <!-- 添加调试指示器 -->
  <div v-if="showDebugTools" class="debug-dialog-status">
    <p>编辑对话框状态: {{ editDialogVisible ? '可见' : '隐藏' }}</p>
    <p>编辑ID: {{ currentEditId || '无' }}</p>
    <el-button @click="testEditDialog">测试打开编辑对话框</el-button>
  </div>

</template>

<script setup>
import { ref, computed, reactive, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus'
import { Search, Document, RefreshRight } from '@element-plus/icons-vue'
import * as XLSX from 'xlsx'
import ExcelPreview from '@/components/ExcelPreview.vue'
import CreateObjectDialog from '@/components/source/CreateObjectDialog.vue'
import ObjectList from '@/components/source/ObjectList.vue'
import AppHeader from '@/components/AppHeader.vue'
import CommonPagination from '@/components/CommonPagination.vue'
import dataObjectService from '@/services/dataObjectService'
import axios from 'axios'
import { API_URL, axiosInstance, testApiConnection } from '@/services/apiConfig'

const router = useRouter()
const activeTab = ref('objectList')
const currentStatus = ref('') // 默认显示全部数字对象
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(5)
const totalCount = ref(0)
const selectedRows = ref([])

// 添加计算属性判断是否为已合格状态
const isQualifiedStatus = computed(() => currentStatus.value === '已合格')

// 编辑对话框可见性
const editDialogVisible = ref(false)
// 新建对话框可见性
const createDialogVisible = ref(false)
// 当前编辑的对象ID
const currentEditId = ref('') 

// 为元数据字段创建单独的响应式引用
const metadataDataName = ref('')
const metadataSourceUnit = ref('')
const metadataContactPerson = ref('')
const metadataContactPhone = ref('')

// 编辑表单数据 - 使用reactive直接创建响应式对象
const editForm = reactive({
  id: '',
  entity: '',
  locationInfo: {
    row: '',
    col: ''
  },
  metadata: {
    dataName: '',
    sourceUnit: '',
    contactPerson: '',
    contactPhone: '',
    resourceSummary: '',
    fieldClassification: '',
    headers: []
  },
  constraint: [],
  formatConstraint: '',
  accessConstraint: '',
  pathConstraint: '',
  regionConstraint: '',
  shareConstraint: '',
  transferControl: [],
  auditInfo: '',
  status: '',
  feedback: '',
  excelData: null,
  dataItems: []
})

// 记录编辑索引
const editingIndex = ref(-1)
// 表单引用
const editFormRef = ref(null)
const createFormRef = ref(null)

// 表格数据 - 从共享服务获取
const tableData = ref(dataObjectService.getAllDataObjects())

// 排序状态
const sortState = reactive({
  prop: '',
  order: ''
})

// 确保数据是数组格式
const ensureArray = (value) => {
  if (Array.isArray(value)) {
    return [...value]
  }
  return value ? [value] : []
}

// 根据状态和搜索条件过滤数据
const filteredTableData = computed(() => {
  let result = tableData.value

  // 状态过滤
  if (currentStatus.value) {
    result = result.filter(item => item.status === currentStatus.value)
  }

  // 关键字搜索
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(item => {
      // 检查ID和实体
      if (item.id.toString().includes(keyword) || 
          item.entity.toLowerCase().includes(keyword)) {
        return true
      }
      
      // 确保约束条件是数组并检查
      const constraints = ensureArray(item.constraint)
      if (constraints.some(c => c && c.toLowerCase().includes(keyword))) {
        return true
      }
      
      // 确保传输控制操作是数组并检查
      const transferControls = ensureArray(item.transferControl)
      if (transferControls.some(t => t && t.toLowerCase().includes(keyword))) {
        return true
      }
      
      return false
    })
  }

  // 排序
  if (sortState.prop === 'id') {
    if (sortState.order === 'ascending') {
      result = [...result].sort((a, b) => a.id - b.id)
    } else if (sortState.order === 'descending') {
      result = [...result].sort((a, b) => b.id - a.id)
    }
  }

  // 更新总数据量
  totalCount.value = result.length

  // 分页计算已经由ObjectList组件处理
  return result
})

// 处理表格选择变更
const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

// 编辑指定的对象
const handleEdit = (row) => {
  console.log('编辑对象:', row)
  
  // 克隆对象以避免直接修改原始引用
  const sourceObj = JSON.parse(JSON.stringify(row))
  console.log('克隆后的源对象:', sourceObj)
  
  // 重置编辑表单
  resetEditForm()
  
  // 设置基本字段
  editForm.id = sourceObj.id
  editForm.entity = sourceObj.entity
  
  // 处理定位信息
  if (sourceObj.locationInfo) {
    if (typeof sourceObj.locationInfo === 'object') {
      editForm.locationInfo = { 
        row: sourceObj.locationInfo.row || '', 
        col: sourceObj.locationInfo.col || '' 
      }
    } else if (typeof sourceObj.locationInfo === 'string') {
      // 尝试解析字符串形式的定位信息
      const locationInfo = parseLocationInfoString(sourceObj.locationInfo)
      editForm.locationInfo = locationInfo
    }
  }
  
  // 处理元数据
  editForm.metadata = extractMetadata(sourceObj)
  console.log('已设置编辑表单的元数据:', editForm.metadata)
  
  // 处理约束条件 - 同时处理单个字段和约束数组
  editForm.constraint = []
  
  // 先处理前端单独字段
  if (sourceObj.formatConstraint) {
    editForm.formatConstraint = sourceObj.formatConstraint
    if (!editForm.constraint.includes(`格式约束:${sourceObj.formatConstraint}`)) {
      editForm.constraint.push(`格式约束:${sourceObj.formatConstraint}`)
    }
  }
  
  if (sourceObj.accessConstraint) {
    editForm.accessConstraint = sourceObj.accessConstraint
    if (!editForm.constraint.includes(`访问权限:${sourceObj.accessConstraint}`)) {
      editForm.constraint.push(`访问权限:${sourceObj.accessConstraint}`)
    }
  }
  
  if (sourceObj.pathConstraint) {
    editForm.pathConstraint = sourceObj.pathConstraint
    if (!editForm.constraint.includes(`传输路径约束:${sourceObj.pathConstraint}`)) {
      editForm.constraint.push(`传输路径约束:${sourceObj.pathConstraint}`)
    }
  }
  
  if (sourceObj.regionConstraint) {
    editForm.regionConstraint = sourceObj.regionConstraint
    if (!editForm.constraint.includes(`地域性约束:${sourceObj.regionConstraint}`)) {
      editForm.constraint.push(`地域性约束:${sourceObj.regionConstraint}`)
    }
  }
  
  if (sourceObj.shareConstraint) {
    editForm.shareConstraint = sourceObj.shareConstraint
    if (!editForm.constraint.includes(`共享约束:${sourceObj.shareConstraint}`)) {
      editForm.constraint.push(`共享约束:${sourceObj.shareConstraint}`)
    }
  }
  
  // 处理约束条件数组
  if (sourceObj.constraint) {
    if (Array.isArray(sourceObj.constraint)) {
      // 将所有约束条件添加到数组
      sourceObj.constraint.forEach(constraint => {
        if (!editForm.constraint.includes(constraint)) {
          editForm.constraint.push(constraint)
        }
        
        // 同时更新相应的单独字段
        if (constraint.includes('格式约束:')) {
          editForm.formatConstraint = constraint.split(':')[1]
        } else if (constraint.includes('访问权限:')) {
          editForm.accessConstraint = constraint.split(':')[1]
        } else if (constraint.includes('传输路径约束:')) {
          editForm.pathConstraint = constraint.split(':')[1]
        } else if (constraint.includes('地域性约束:')) {
          editForm.regionConstraint = constraint.split(':')[1]
        } else if (constraint.includes('共享约束:')) {
          editForm.shareConstraint = constraint.split(':')[1]
        }
      })
    } else if (typeof sourceObj.constraint === 'string') {
      const constraint = sourceObj.constraint
      if (!editForm.constraint.includes(constraint)) {
        editForm.constraint.push(constraint)
      }
      
      // 处理单个约束字符串
      if (constraint.includes('格式约束:')) {
        editForm.formatConstraint = constraint.split(':')[1]
      } else if (constraint.includes('访问权限:')) {
        editForm.accessConstraint = constraint.split(':')[1]
      } else if (constraint.includes('传输路径约束:')) {
        editForm.pathConstraint = constraint.split(':')[1]
      } else if (constraint.includes('地域性约束:')) {
        editForm.regionConstraint = constraint.split(':')[1]
      } else if (constraint.includes('共享约束:')) {
        editForm.shareConstraint = constraint.split(':')[1]
      }
    }
  }
  
  // 处理传输控制操作
  if (sourceObj.transferControl) {
    editForm.transferControl = Array.isArray(sourceObj.transferControl) ? 
      sourceObj.transferControl : [sourceObj.transferControl]
  } else {
    editForm.transferControl = []
  }
  
  // 确保传输控制为数组
  if (!Array.isArray(editForm.transferControl)) {
    editForm.transferControl = []
  }
  
  // 检查传播控制对象（如果存在）
  if (sourceObj.propagationControl) {
    if (sourceObj.propagationControl.canRead && !editForm.transferControl.includes('可读')) {
      editForm.transferControl.push('可读')
    }
    if (sourceObj.propagationControl.canModify && !editForm.transferControl.includes('可修改')) {
      editForm.transferControl.push('可修改')
    }
    if (sourceObj.propagationControl.canDestroy && !editForm.transferControl.includes('可销毁')) {
      editForm.transferControl.push('可销毁')
    }
    if (sourceObj.propagationControl.canShare && !editForm.transferControl.includes('可共享')) {
      editForm.transferControl.push('可共享')
    }
    if (sourceObj.propagationControl.canDelegate && !editForm.transferControl.includes('可委托')) {
      editForm.transferControl.push('可委托')
    }
  }
  
  // 设置状态和反馈
  editForm.status = sourceObj.status || ''
  editForm.feedback = sourceObj.feedback || ''
  
  // 设置审计信息
  editForm.auditInfo = sourceObj.auditInfo || ''
  
  // 设置Excel数据
  editForm.excelData = sourceObj.excelData || null
  
  // 设置dataItems数组
  editForm.dataItems = sourceObj.dataItems || []
  
  // 处理dataContent字段
  if (sourceObj.dataContent) {
    try {
      let contentObj;
      
      // 处理dataContent是字符串的情况
      if (typeof sourceObj.dataContent === 'string') {
        // 尝试解析JSON字符串
        try {
          contentObj = JSON.parse(sourceObj.dataContent);
        } catch (jsonError) {
          console.warn(`JSON解析失败: ${jsonError}，尝试正则提取`);
          
          // 如果JSON解析失败，尝试用正则表达式提取feedback
          const feedbackMatch = sourceObj.dataContent.match(/"feedback"\s*:\s*"([^"]*)"/);
          if (feedbackMatch && feedbackMatch[1]) {
            editForm.feedback = feedbackMatch[1];
            console.log(`通过正则提取到feedback: ${editForm.feedback}`);
          }
          
          // 尝试提取status
          const statusMatch = sourceObj.dataContent.match(/"status"\s*:\s*"([^"]*)"/);
          if (statusMatch && statusMatch[1]) {
            editForm.status = statusMatch[1];
            console.log(`通过正则提取到status: ${editForm.status}`);
          }
        }
      } else {
        contentObj = sourceObj.dataContent;
      }
      
      if (contentObj) {
        console.log(`处理数据项 ${sourceObj.entity} 的dataContent:`, contentObj);
        
        // 直接更新status，不管它是什么值
        if (contentObj.status) {
          editForm.status = contentObj.status;
          console.log(`从dataContent提取状态: ${sourceObj.entity} - ${editForm.status}`);
        }
        
        // 查找反馈意见 - 不管status是什么值都提取feedback
        if (contentObj.feedback) {
          editForm.feedback = contentObj.feedback;
          console.log(`从dataContent直接提取反馈信息: ${sourceObj.entity} - ${editForm.feedback}`);
        } else if (contentObj.data && contentObj.data.feedback) {
          editForm.feedback = contentObj.data.feedback;
          console.log(`从dataContent.data提取反馈信息: ${sourceObj.entity} - ${editForm.feedback}`);
        }
        
        // 提取dataItems如果存在
        if (contentObj.dataItems && Array.isArray(contentObj.dataItems)) {
          editForm.dataItems = contentObj.dataItems;
          console.log(`从dataContent提取数据项: ${sourceObj.entity} - ${contentObj.dataItems.length}项`);
        }
      }
    } catch (e) {
      console.warn(`解析 ${sourceObj.entity} 的dataContent失败:`, e);
    }
  }
  
  console.log('最终编辑表单数据:', JSON.stringify(editForm))
  
  // 显示编辑对话框
  editDialogVisible.value = true
}

// 取消编辑
const cancelEdit = () => {
  editDialogVisible.value = false
  currentEditId.value = ''
  editingIndex.value = -1
}

// 保存编辑的对象
const saveEditObject = async (updatedObject) => {
  console.log('保存编辑对象:', updatedObject)
  const objectId = updatedObject.id
  
  try {
    // 修复位置信息
    if (updatedObject.locationInfo) {
      if (typeof updatedObject.locationInfo === 'object' && 
          (updatedObject.locationInfo.row === undefined || updatedObject.locationInfo.col === undefined)) {
        updatedObject.locationInfo = {
          row: '',
          col: ''
        }
      }
    }
    
    // 修复元数据
    if (!updatedObject.metadata) {
      updatedObject.metadata = createDefaultMetadata(updatedObject.entity)
    }
    
    // 构建数据内容
    let dataContent = {}
    try {
      // 尝试解析现有的dataContent
      if (typeof updatedObject.dataContent === 'string') {
        dataContent = JSON.parse(updatedObject.dataContent)
      } else if (updatedObject.dataContent) {
        dataContent = updatedObject.dataContent
      }
    } catch (e) {
      console.warn('解析现有dataContent失败，创建新对象', e)
      dataContent = {}
    }
    
    // 更新dataContent
    dataContent.entity = updatedObject.entity
    dataContent.status = updatedObject.status
    dataContent.feedback = updatedObject.feedback
    
    // 保留dataItems
    if (updatedObject.dataItems) {
      dataContent.dataItems = updatedObject.dataItems
    }
    
    // 如果元数据存在，添加到dataContent
    if (updatedObject.metadata) {
      dataContent.metadata = updatedObject.metadata
    }
    
    // 将dataContent转为字符串
    updatedObject.dataContent = JSON.stringify(dataContent)
    
    // 创建约束条件数组
    if (!Array.isArray(updatedObject.constraint)) {
      updatedObject.constraint = []
    }
    
    if (updatedObject.formatConstraint && !updatedObject.constraint.includes(`格式约束:${updatedObject.formatConstraint}`)) {
      updatedObject.constraint.push(`格式约束:${updatedObject.formatConstraint}`)
    }
    
    if (updatedObject.accessConstraint && !updatedObject.constraint.includes(`访问权限:${updatedObject.accessConstraint}`)) {
      updatedObject.constraint.push(`访问权限:${updatedObject.accessConstraint}`)
    }
    
    if (updatedObject.pathConstraint && !updatedObject.constraint.includes(`传输路径约束:${updatedObject.pathConstraint}`)) {
      updatedObject.constraint.push(`传输路径约束:${updatedObject.pathConstraint}`)
    }
    
    if (updatedObject.regionConstraint && !updatedObject.constraint.includes(`地域性约束:${updatedObject.regionConstraint}`)) {
      updatedObject.constraint.push(`地域性约束:${updatedObject.regionConstraint}`)
    }
    
    if (updatedObject.shareConstraint && !updatedObject.constraint.includes(`共享约束:${updatedObject.shareConstraint}`)) {
      updatedObject.constraint.push(`共享约束:${updatedObject.shareConstraint}`)
    }
    
    // 确保传输控制为数组
    if (!Array.isArray(updatedObject.transferControl)) {
      updatedObject.transferControl = []
    }
    
    console.log('处理后的更新对象:', JSON.stringify(updatedObject))
    
    // 尝试通过API保存
    ElMessage.info('正在向后端保存数据...')
    const result = await dataObjectService.updateDataObjectViaApi(objectId, updatedObject)
    
    if (result) {
      ElMessage.success(`已保存更改: ${updatedObject.entity}`)
      
      // 刷新数据列表
      refreshData()
    } else {
      // 即使API保存失败，我们也更新本地数据并显示成功消息
      dataObjectService.updateDataObject(updatedObject)
      ElMessage({
        message: `已在本地保存: ${updatedObject.entity}，服务器保存失败`,
        type: 'warning',
        duration: 3000
      })
      
      // 仍然刷新数据
      refreshData()
    }
  } catch (error) {
    console.error('保存编辑时出错:', error)
    ElMessage.error(`保存编辑失败: ${error.message || '未知错误'}`)
    
    // 尝试在本地保存
    try {
      dataObjectService.updateDataObject(updatedObject)
      ElMessage({
        message: `已在本地保存: ${updatedObject.entity}，但服务器保存失败`,
        type: 'warning',
        duration: 3000
      })
    } catch (localError) {
      ElMessage.error('本地保存也失败，请稍后再试')
    }
  }
}

// 删除对象
const handleDelete = (row) => {
  const objectId = row.id;
  console.log('删除对象，ID:', objectId);
  
  ElMessageBox.confirm(`确定要删除"${row.entity}"吗?`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      // 尝试通过API删除
      const result = await dataObjectService.deleteDataObjectViaApi(objectId)
      
      // 无论API删除是否成功，都尝试本地删除并显示成功信息
      if (!result) {
        // API删除失败，尝试本地删除
        dataObjectService.deleteDataObject(objectId)
      }
      
      // 不管API是否成功，都显示成功删除的消息
      ElMessage.success(`已删除: ${row.entity}`)
      
      // 刷新数据
      refreshData()
    } catch (error) {
      console.error('删除对象时出错:', error)
      
      // 即使出错，也尝试本地删除
      const localDeleted = dataObjectService.deleteDataObject(objectId)
      if (localDeleted) {
        ElMessage.success(`已删除: ${row.entity}`)
      } else {
        ElMessage.error(`删除失败: ${row.entity}`)
      }
      
      // 刷新数据
      refreshData()
    }
  }).catch(() => {
    ElMessage.info('已取消删除')
  })
}

// 退出登录
const logout = () => {
  localStorage.removeItem('role')
  router.push('/login')
}

// 显示创建对象对话框
const showCreateDialog = () => {
  // 直接设置对话框可见性为true
  console.log('触发显示创建对话框')
  createDialogVisible.value = true
  console.log('createDialogVisible设置为:', createDialogVisible.value)
  
  // 重置表单为空值
  setTimeout(() => {
    // 重置所有表单项为空
    createForm.entity = ''
    createForm.locationInfo = {
      row: '',
      col: ''
    }
    createForm.metadata = {
      dataName: '',
      sourceUnit: '',
      contactPerson: '',
      contactPhone: '',
      resourceSummary: '',
      fieldClassification: '',
      headers: []
    }
    createForm.constraint = []
    createForm.formatConstraint = ''
    createForm.accessConstraint = ''
    createForm.pathConstraint = ''
    createForm.regionConstraint = ''
    createForm.shareConstraint = ''
    createForm.transferControl = []
    createForm.status = '待检验'
    createForm.auditInfo = ''
  }, 100)
}

// 创建表单数据
const createForm = reactive({
  entity: '',
  locationInfo: {
    row: '',
    col: ''
  },
  constraint: [],
  formatConstraint: '',
  accessConstraint: '',
  pathConstraint: '',
  regionConstraint: '',
  shareConstraint: '',
  transferControl: [],
  status: '待检验',
  auditInfo: '',
  excelData: null // 新增保存Excel文件数据
})

// 表单校验规则
const formRules = {
  entity: [
    { required: true, message: '请输入实体名称', trigger: 'blur' }
  ],
  locationInfo: [
    { 
      validator: (rule, value, callback) => {
        // 这里我们不再强制要求定位信息
        console.log('验证定位信息: ', {
          isEditDialogVisible: editDialogVisible.value,
          'editForm.locationInfo': editForm.locationInfo,
          value: value,
          'form.locationInfo': value
        });
        
        // 如果两个字段都有值或者都没有值，则通过验证
        if ((value && value.row && value.col) || (!value || (!value.row && !value.col))) {
          callback()
        } else {
          // 如果只填了一个，则提示需要同时填写
          callback(new Error('请同时填写行和列，或者都不填'))
        }
      },
      trigger: 'blur'
    }
  ],
  formatConstraint: [
    { required: false, message: '请选择格式约束', trigger: 'change' }
  ],
  accessConstraint: [
    { required: false, message: '请选择访问权限', trigger: 'change' }
  ],
  pathConstraint: [
    { required: false, message: '请选择传输路径约束', trigger: 'change' }
  ],
  regionConstraint: [
    { required: false, message: '请选择地域性约束', trigger: 'change' }
  ],
  shareConstraint: [
    { required: false, message: '请选择共享约束', trigger: 'change' }
  ],
  transferControl: [
    { type: 'array', required: false, message: '请选择传输控制操作', trigger: 'change' }
  ]
}

// 添加手动保存编辑表单的方法
const handleSaveEditManually = () => {
  // 验证表单
  if (editFormRef.value) {
    // 先检查是否有必填字段为空
    if (!editForm.entity) {
      ElMessage.warning('请输入实体名称');
      return;
    }
    
    // 保存前检查定位信息是否合理
    if (editForm.locationInfo) {
      if ((editForm.locationInfo.row && !editForm.locationInfo.col) || 
          (!editForm.locationInfo.row && editForm.locationInfo.col)) {
        ElMessage.warning('请同时填写定位信息的行和列，或者都不填');
        return;
      }
    }
    
    // 进行表单验证
    editFormRef.value.validate((valid, fields) => {
      if (valid) {
        // 构建约束条件数组
        const constraintArray = []
        if (editForm.formatConstraint) constraintArray.push(`格式约束:${editForm.formatConstraint}`)
        if (editForm.accessConstraint) constraintArray.push(`访问权限:${editForm.accessConstraint}`)
        if (editForm.pathConstraint) constraintArray.push(`传输路径约束:${editForm.pathConstraint}`)
        if (editForm.regionConstraint) constraintArray.push(`地域性约束:${editForm.regionConstraint}`)
        if (editForm.shareConstraint) constraintArray.push(`共享约束:${editForm.shareConstraint}`)
        
        // 更新约束数组
        editForm.constraint = constraintArray
        
        // 构建传播控制对象，与transferControl数组对应
        const propagationControl = {
          canRead: editForm.transferControl.includes('可读'),
          canModify: editForm.transferControl.includes('可修改'),
          canDestroy: editForm.transferControl.includes('可销毁'),
          canShare: editForm.transferControl.includes('可共享'),
          canDelegate: editForm.transferControl.includes('可委托')
        }
        
        // 构建更新后的对象
        const updatedObject = {
          id: editForm.id,
          entity: editForm.entity,
          locationInfo: {
            row: editForm.locationInfo.row || '',
            col: editForm.locationInfo.col || ''
          },
          metadata: {
            dataName: editForm.metadata.dataName || editForm.entity,
            sourceUnit: editForm.metadata.sourceUnit || '',
            contactPerson: editForm.metadata.contactPerson || '',
            contactPhone: editForm.metadata.contactPhone || '',
            resourceSummary: editForm.metadata.resourceSummary || '',
            fieldClassification: editForm.metadata.fieldClassification || '',
            headers: editForm.metadata.headers || []
          },
          constraint: constraintArray,
          formatConstraint: editForm.formatConstraint || '',
          accessConstraint: editForm.accessConstraint || '',
          pathConstraint: editForm.pathConstraint || '',
          regionConstraint: editForm.regionConstraint || '',
          shareConstraint: editForm.shareConstraint || '',
          transferControl: editForm.transferControl || [],
          propagationControl: propagationControl,
          auditInfo: editForm.auditInfo || '',
          status: editForm.status || '',
          feedback: editForm.feedback || '',
          excelData: editForm.excelData,
          dataItems: editForm.dataItems || [] // 添加数据项
        }
        
        console.log('准备保存更新的对象:', updatedObject)
        
        // 调用保存函数
        saveEditObject(updatedObject)
        
        // 关闭对话框
        editDialogVisible.value = false
      } else {
        // 显示具体的验证错误
        console.error('表单验证错误:', fields);
        
        // 获取第一个错误字段的信息
        let firstErrorField = '';
        let firstErrorMessage = '';
        
        if (fields) {
          // 遍历错误字段
          for (const key in fields) {
            if (fields[key] && fields[key][0]) {
              firstErrorField = key;
              firstErrorMessage = fields[key][0].message;
              break;
            }
          }
        }
        
        // 显示具体的错误提示
        if (firstErrorField && firstErrorMessage) {
          ElMessage.warning(`${firstErrorMessage} (字段: ${firstErrorField})`);
        } else {
          ElMessage.warning('表单验证失败，请检查必填字段');
        }
        return false;
      }
    })
  } else {
    console.error('表单引用不存在')
    ElMessage.error('表单引用不存在，无法验证表单')
  }
}

// 处理排序变化
const handleSortChange = (column) => {
  sortState.prop = column.prop
  sortState.order = column.order
}

// Excel预览相关
const previewDialogVisible = ref(false)
const previewForm = reactive({
  id: '',
  entity: '',
  locationInfo: '',
  constraint: [],
  transferControl: [],
  status: '',
  metadata: null // 添加元数据字段
})

// Excel表格数据
const excelTableColumns = ref([])
const excelTableData = ref([])
const isExcelLoading = ref(false)
const excelSheets = ref([]) // 存储工作表名称
const activeSheet = ref('') // 当前激活的工作表
const currentWorkbook = ref(null) // 当前工作簿对象

// 在script setup部分添加新的变量和方法
const currentExcelFile = ref(null)

// 修改previewEntity方法
const previewEntity = (row) => {
  console.log('预览实体数据:', row)
  
  // 设置预览表单数据
  previewForm.id = row.id
  previewForm.entity = row.entity
  previewForm.locationInfo = row.locationInfo
  previewForm.constraint = ensureArray(row.constraint)
  previewForm.transferControl = ensureArray(row.transferControl)
  previewForm.status = row.status
  
  // 解析元数据 - 直接使用原始元数据，不尝试提取
  if (row.metadata && typeof row.metadata === 'object') {
    console.log('使用原始元数据，不进行提取:', row.metadata)
    previewForm.metadata = { ...row.metadata }
  } else {
    // 如果没有元数据，才使用提取方法
    console.log('原始元数据不存在，尝试提取')
    previewForm.metadata = extractMetadata(row)
  }
  console.log('已设置预览元数据:', previewForm.metadata)
  
  // 清空当前Excel数据
  currentExcelFile.value = null
  excelTableData.value = []
  excelTableColumns.value = []
  excelSheets.value = []
  
  // 显示预览对话框
  previewDialogVisible.value = true
  
  // 检查是否有实际的Excel数据
  if (row.excelData) {
    console.log('有Excel数据，开始加载')
    ElMessage.info('正在准备Excel数据，请稍候...')
    isExcelLoading.value = true
    
    // 使用setTimeout避免UI阻塞
    setTimeout(() => {
      try {
        currentExcelFile.value = row.excelData
      } catch (error) {
        console.error('加载Excel数据出错:', error)
        ElMessage.error(`加载Excel数据出错: ${error.message}`)
        isExcelLoading.value = false
        currentExcelFile.value = null
      }
    }, 100)
  } else {
    console.log('没有Excel数据，显示空状态')
    currentExcelFile.value = null
    isExcelLoading.value = false
  }
}

// 添加新的处理方法
const handleExcelDataLoaded = (data) => {
  console.log('Excel数据加载完成:', data)
  
  // 检查是否为真实上传的Excel文件
  const isUserUploadedFile = tableData.value.some(row => 
    row.id === previewForm.id && row.excelData && row.excelData === currentExcelFile.value);
  
  // 只有当不是用户上传的文件时才禁用数据显示
  if (!isUserUploadedFile) {
    console.warn('检测到非用户上传的Excel数据，已屏蔽');
    excelTableColumns.value = [];
    excelTableData.value = [];
    excelSheets.value = [];
    isExcelLoading.value = false;
    currentExcelFile.value = null;
    return;
  }
  
  // 可以在这里处理加载完的数据，例如根据定位信息高亮显示特定单元格
  const { headers, data: excelRows, sheets } = data;
  
  // 存储Excel表格数据，以便后续可能的操作
  excelTableColumns.value = headers || [];
  excelTableData.value = excelRows || [];
  excelSheets.value = sheets || [];
  
  isExcelLoading.value = false;
  
  if (excelRows && excelRows.length) {
    ElMessage.success(`已成功加载 ${excelRows.length} 行数据`);
  } else {
    console.warn('加载的Excel数据为空');
  }
}

const handleExcelError = (error) => {
  console.error('Excel加载错误:', error)
  isExcelLoading.value = false
  ElMessage.error(`加载Excel时出错: ${error}`)
}

// 处理导出功能
const handleExport = () => {
  ElMessage.success('导出功能待实现')
}

// 清除所有测试数据
const clearAllTestData = () => {
  console.log('清除所有测试数据')
  // 确保所有表格行的excelData为null
  tableData.value.forEach(row => {
    row.excelData = null
  })
  
  // 清空当前Excel文件和相关数据
  currentExcelFile.value = null
  excelTableData.value = []
  excelTableColumns.value = []
  excelSheets.value = []
}

// 专门处理客户反馈数据
const fixCustomerFeedbackData = () => {
  console.log("开始特殊处理客户反馈数据");
  
  // 遍历所有数据行
  for (let i = 0; i < tableData.value.length; i++) {
    const row = tableData.value[i];
    
    // 找到客户反馈实体
    if (row.entity === '客户反馈') {
      console.log(`找到客户反馈实体 [ID: ${row.id}]`);
      
      // 直接在Vue响应式对象上设置属性
      if (!row.feedback && row.dataContent) {
        console.log("原始dataContent:", typeof row.dataContent === 'string' ? 
                   row.dataContent.substring(0, 100) + "..." : "对象类型");
        
        // 判断类型并提取feedback
        if (typeof row.dataContent === 'string') {
          // 使用正则表达式提取
          const match = row.dataContent.match(/"feedback"\s*:\s*"([^"]*)"/);
          if (match && match[1]) {
            console.log(`直接提取到feedback: "${match[1]}"`);
            
            // 直接更新数据对象的属性
            tableData.value[i] = {
              ...row,
              feedback: match[1],
              status: '不合格'
            };
            
            console.log(`已强制更新客户反馈数据: feedback="${match[1]}", status="不合格"`);
            continue;
          }
        } 
        // 处理对象类型
        else if (typeof row.dataContent === 'object' && row.dataContent !== null) {
          if (row.dataContent.feedback) {
            // 直接更新数据对象的属性
            tableData.value[i] = {
              ...row,
              feedback: row.dataContent.feedback,
              status: '不合格'
            };
            
            console.log(`已从对象中提取并更新: feedback="${row.dataContent.feedback}", status="不合格"`);
            continue;
          }
        }
        
        // 强制设置默认值
        if (row.dataContent.includes && row.dataContent.includes('数据格式错误')) {
          tableData.value[i] = {
            ...row,
            feedback: '数据格式错误',
            status: '不合格'
          };
          
          console.log("已强制设置默认反馈: 数据格式错误");
        }
      }
    }
  }
  
  console.log("客户反馈数据处理完成");
}

// 在组件挂载后执行清理
onMounted(() => {
  clearAllTestData()
  // 添加数据变化监听器
  dataObjectService.addChangeListener((newData) => {
    // 无需手动更新tableData，因为是响应式引用
  })
  
  // 从后端API加载数据
  loadDataFromBackend().then(() => {
    // 在数据加载完成后执行特殊处理
    fixCustomerFeedbackData();
  });
  
  // 直接处理特定实体的反馈信息
  setTimeout(() => {
    fixCustomerFeedbackData();
  }, 2000); // 给足够的时间加载数据
  
  // 测试API连接
  testApiConnection().then(isConnected => {
    if (!isConnected) {
      apiErrorVisible.value = true;
      ElMessage.warning('无法连接到后端API，请检查服务器是否正在运行');
    } else {
      apiErrorVisible.value = false;
    }
  }).catch(error => {
    console.error('API连接测试失败:', error);
    apiErrorVisible.value = true;
  });
})

// 添加新的变量和方法
const apiErrorVisible = ref(false)

// 添加调试相关功能
const showDebugTools = ref(false) // 设置为false隐藏调试工具
const lastReceivedApiData = ref(null)

// 复制调试数据到剪贴板
const copyDebugData = () => {
  const jsonStr = JSON.stringify(lastReceivedApiData.value, null, 2)
  navigator.clipboard.writeText(jsonStr)
    .then(() => {
      ElMessage.success('已复制到剪贴板')
    })
    .catch(err => {
      ElMessage.error('复制失败: ' + err)
      console.error('复制失败:', err)
    })
}

// 格式化JSON
const prettifyJson = (json) => {
  if (!json) return '暂无数据'
  try {
    return JSON.stringify(json, null, 2)
  } catch (e) {
    return '无法格式化: ' + e.message
  }
}

// 从后端加载数据
const loadDataFromBackend = async () => {
  try {
    console.log('开始从后端加载数据...')
    ElMessage.info('正在从后端加载数据...')
    await dataObjectService.fetchDataObjectsFromBackend()
    
    // 获取最后接收的API数据
    lastReceivedApiData.value = dataObjectService.getLastReceivedApiData()
    
    // 处理刚刚获取的数据，确保反馈意见能够正确显示
    processNewlyFetchedData();
    
    console.log('后端数据加载完成')
    ElMessage.success('数据加载成功')
    
    // 成功后隐藏错误提示
    apiErrorVisible.value = false
  } catch (error) {
    console.error('从后端加载数据失败:', error)
    
    // 判断是否为跨域错误
    const isCORSError = error.message && (
      error.message.includes('NetworkError') || 
      error.message.includes('Network Error') ||
      error.message.includes('CORS') || 
      error.message.includes('cross-origin')
    )
    
    if (isCORSError) {
      ElMessage.error('跨域请求失败，请确保后端已开启CORS支持')
      apiErrorVisible.value = true
    } else {
      ElMessage.warning('从后端加载数据失败，已切换到本地模拟数据')
      apiErrorVisible.value = true
    }
    
    // 如果当前没有数据，则使用模拟数据
    if (tableData.value.length === 0) {
      console.log('使用本地模拟数据')
    }
  }
}

// 处理刚刚获取的数据，确保反馈意见能够正确显示
const processNewlyFetchedData = () => {
  if (!tableData.value || !tableData.value.length) return;

  console.log("开始处理表格数据，总行数:", tableData.value.length);
  
  tableData.value.forEach(row => {
    console.log(`处理行 [${row.id}] [${row.entity}]`);
    
    // 特别处理客户反馈实体
    if (row.entity === '客户反馈') {
      console.log(`找到客户反馈实体: ID=${row.id}`);
      
      // 直接从dataContent中提取feedback
      if (row.dataContent) {
        console.log(`dataContent类型: ${typeof row.dataContent}`);
        
        let feedbackValue = null;
        
        // 对字符串类型的dataContent进行处理
        if (typeof row.dataContent === 'string') {
          console.log(`dataContent内容: ${row.dataContent.substring(0, 100)}...`);
          
          // 使用正则表达式直接提取feedback值
          const match = row.dataContent.match(/"feedback"\s*:\s*"([^"]*)"/);
          if (match && match[1]) {
            feedbackValue = match[1];
            console.log(`通过正则表达式提取到feedback: "${feedbackValue}"`);
          } else if (row.dataContent.includes('数据格式错误')) {
            feedbackValue = '数据格式错误';
            console.log(`直接从字符串中提取到: "${feedbackValue}"`);
          }
        } 
        // 对对象类型的dataContent进行处理
        else if (typeof row.dataContent === 'object') {
          if (row.dataContent.feedback) {
            feedbackValue = row.dataContent.feedback;
            console.log(`从对象中提取到feedback: "${feedbackValue}"`);
          }
        }
        
        // 如果提取到了feedback值，直接设置到row上
        if (feedbackValue) {
          row.feedback = feedbackValue;
          row.status = '不合格';
          console.log(`成功设置反馈: feedback="${row.feedback}", status="${row.status}"`);
        } else {
          console.warn(`未能从dataContent中提取到feedback值`);
        }
      } else {
        console.warn(`客户反馈实体没有dataContent`);
      }
    }
    
    // 一般的处理逻辑，适用于所有实体
    if (row.dataContent && !row.feedback) {
      let feedbackValue = null;
      
      // 尝试从字符串类型的dataContent中提取feedback
      if (typeof row.dataContent === 'string') {
        const match = row.dataContent.match(/"feedback"\s*:\s*"([^"]*)"/);
        if (match && match[1]) {
          feedbackValue = match[1];
          console.log(`为${row.entity}提取到feedback: "${feedbackValue}"`);
        }
      }
      // 尝试从对象类型的dataContent中提取feedback
      else if (typeof row.dataContent === 'object' && row.dataContent.feedback) {
        feedbackValue = row.dataContent.feedback;
        console.log(`为${row.entity}从对象中提取到feedback: "${feedbackValue}"`);
      }
      
      // 如果提取到了feedback值，设置到row上
      if (feedbackValue) {
        row.feedback = feedbackValue;
        if (!row.status) {
          row.status = '不合格';
        }
        console.log(`为${row.entity}设置反馈: ${row.feedback}`);
      }
    }
    
    // 最后做一次日志记录
    console.log(`行 [${row.entity}] 最终状态: feedback=${row.feedback || '无'}, status=${row.status || '无'}`);
  });
  
  console.log("数据处理完成");
}

// 添加刷新数据的方法
// 添加loadTableData作为refreshData的别名
const loadTableData = () => {
  // 调用refreshData作为实际实现
  refreshData()
}

const refreshData = async () => {
  try {
    ElMessage.info('正在从后端刷新数据...')
    await dataObjectService.fetchDataObjectsFromBackend()
    
    // 获取最后接收的API数据
    lastReceivedApiData.value = dataObjectService.getLastReceivedApiData()
    
    // 处理刚刚获取的数据，确保反馈意见能够正确显示
    processNewlyFetchedData();
    
    ElMessage.success('数据刷新成功')
    
    // 成功后隐藏错误提示
    apiErrorVisible.value = false
  } catch (error) {
    console.error('刷新数据失败:', error)
    
    // 判断是否为跨域错误
    const isCORSError = error.message && (
      error.message.includes('NetworkError') || 
      error.message.includes('Network Error') ||
      error.message.includes('CORS') || 
      error.message.includes('cross-origin')
    )
    
    if (isCORSError) {
      ElMessage.error('跨域请求失败，请确保后端已开启CORS支持并且服务正常运行')
      apiErrorVisible.value = true
    } else if (error.response && error.response.status) {
      // 处理HTTP错误
      ElMessage.error(`请求服务器失败: ${error.response.status} ${error.response.statusText || ''}`)
      apiErrorVisible.value = true
    } else {
      ElMessage.error('刷新数据失败，请检查后端服务是否正常运行')
      apiErrorVisible.value = true
    }
  }
}

// 处理每页显示数量变化
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
}

// 添加新的位置信息提取方法
const extractLocationInfo = (data) => {
  if (!data) return null
  
  // 如果是数组，处理第一个元素
  if (Array.isArray(data)) {
    if (data.length === 0) return null
    return extractLocationInfo(data[0])
  }
  
  // 处理 locationInfo 字段（已格式化的字符串）
  if (data.locationInfo && typeof data.locationInfo === 'string') {
    try {
      const matches = data.locationInfo.match(/\((.*?),\s*(.*?),\s*(.*?)\)/)
      if (matches && matches.length > 3) {
        const [_, entity, row, col] = matches
        return `实体: ${entity}, 行: ${row}, 列: ${col}`
      }
    } catch (e) {
      console.error('解析locationInfo字符串失败:', e)
    }
  }
  
  // 处理 locationInfoJson 字段（JSON字符串）
  if (data.locationInfoJson) {
    try {
      const locationObj = JSON.parse(data.locationInfoJson)
      if (locationObj && locationObj.locations && locationObj.locations.length > 0) {
        const location = locationObj.locations[0]
        return `工作表: ${location.sheet || '-'}, 行范围: ${location.startRow || '-'}-${location.endRow || '-'}, 列范围: ${location.startColumn || '-'}-${location.endColumn || '-'}`
      }
    } catch (e) {
      console.error('解析locationInfoJson失败:', e)
    }
  }
  
  return null
}

// 导航到主页
const navigateToHome = () => {
  console.log('导航回主页')
  // 重置当前状态
  currentStatus.value = ''
  searchKeyword.value = ''
  currentPage.value = 1
  
  // 如果使用的是选项卡，可以切换到主选项卡
  activeTab.value = 'objectList'
  
  // 刷新数据
  refreshData()
  
  // 显示成功消息
  ElMessage.success('已成功保存编辑并返回主页')
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

// 添加缺失的resetCreateForm函数
const resetCreateForm = () => {
  createForm.entity = ''
  createForm.locationInfo = {
    row: '',
    col: ''
  }
  createForm.constraint = []
  createForm.formatConstraint = ''
  createForm.accessConstraint = ''
  createForm.pathConstraint = ''
  createForm.regionConstraint = ''
  createForm.shareConstraint = ''
  createForm.transferControl = []
  createForm.status = '待检验'
  createForm.auditInfo = ''
  
  if (createFormRef.value) {
    createFormRef.value.resetFields()
  }
}

// 添加测试方法
const testEditDialog = () => {
  console.log('测试打开编辑对话框')
  
  // 使用第一行数据作为测试数据
  if (tableData.value && tableData.value.length > 0) {
    const testRow = tableData.value[0]
    console.log('使用测试数据:', testRow)
    handleEdit(testRow)
  } else {
    console.error('没有可用数据用于测试')
    ElMessage.error('没有可用数据用于测试')
  }
}

// 处理文件变更
const handleFileChange = (file) => {
  // 验证文件类型
  const isExcel = file.raw.type === 'application/vnd.ms-excel' || 
                 file.raw.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet';
  if (!isExcel) {
    ElMessage.warning('请上传Excel格式的文件（.xls或.xlsx）');
    return false;
  }
  
  // 设置实体名称为文件名（不带扩展名）
  const fileName = file.name;
  const fileNameWithoutExt = fileName.substring(0, fileName.lastIndexOf('.')) || fileName;
  createForm.entity = fileNameWithoutExt;
  
  // 读取并保存Excel文件内容
  const reader = new FileReader();
  reader.onload = (e) => {
    try {
      // 保存文件的二进制数据
      createForm.excelData = e.target.result;
      ElMessage.success(`已选择Excel表格"${fileName}"`);
    } catch (error) {
      console.error('读取Excel文件失败:', error);
      ElMessage.error('读取Excel文件失败');
    }
  };
  reader.onerror = () => {
    ElMessage.error('读取文件失败');
  };
  reader.readAsBinaryString(file.raw);
}

// 处理编辑框中的文件变更
const handleEditFileChange = (file) => {
  // 验证文件类型
  const isExcel = file.raw.type === 'application/vnd.ms-excel' || 
                 file.raw.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet';
  if (!isExcel) {
    ElMessage.warning('请上传Excel格式的文件（.xls或.xlsx）');
    return false;
  }
  
  // 如果没有手动输入实体名称，则使用文件名作为实体名称
  const fileName = file.name;
  const fileNameWithoutExt = fileName.substring(0, fileName.lastIndexOf('.')) || fileName;
  
  if (!editForm.entity) {
    editForm.entity = fileNameWithoutExt;
  }
  
  // 读取并保存Excel文件内容
  const reader = new FileReader();
  reader.onload = (e) => {
    try {
      // 保存文件的二进制数据到编辑表单
      editForm.excelData = e.target.result;
      ElMessage.success(`已选择Excel表格"${file.name}"`);
    } catch (error) {
      console.error('读取Excel文件失败:', error);
      ElMessage.error('读取Excel文件失败');
    }
  };
  reader.onerror = () => {
    ElMessage.error('读取文件失败');
  };
  reader.readAsBinaryString(file.raw);
}

// 上传前验证文件类型
const beforeUpload = (file) => {
  const isExcel = file.type === 'application/vnd.ms-excel' || 
                 file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet';
  if (!isExcel) {
    ElMessage.warning('请上传Excel格式的文件（.xls或.xlsx）');
    return false;
  }
  return true;
}

// 保存新建对象
const saveCreateObject = async (newObject) => {
  console.log('保存新建对象:', newObject);
  console.log('用户输入的元数据:', newObject.metadata);
  
  // 保存原始元数据副本
  const originalMetadata = newObject.metadata ? { ...newObject.metadata } : null;
  if (!newObject.originalMetadata && originalMetadata) {
    newObject.originalMetadata = originalMetadata;
    console.log('添加原始元数据副本:', newObject.originalMetadata);
  }
  
  // 确保状态为"待检验"
  if (newObject.status !== '待检验') {
    console.log('修正状态值从', newObject.status, '到"待检验"');
    newObject.status = '待检验';
  }
  
  // 检查是否有excelFileId（从服务器上传时获取）
  if (!newObject.excelFileId && newObject.excelData) {
    console.warn('没有发现Excel文件ID，但有Excel数据，这可能是客户端上传导致的');
    newObject.excelFileId = `temp-${Date.now()}`;
    console.log('使用临时文件ID:', newObject.excelFileId);
  }
  
  // 创建dataContent字段
  if (!newObject.dataContent) {
    try {
      // 确保元数据被正确包含 - 使用原始元数据
      const metadata = newObject.originalMetadata || newObject.metadata || {};
      
      // 确保metadata是一个有效对象，移除可能导致JSON解析错误的属性
      const cleanMetadata = {
        dataName: metadata.dataName || newObject.entity || '',
        sourceUnit: metadata.sourceUnit || '',
        contactPerson: metadata.contactPerson || '',
        contactPhone: metadata.contactPhone || '',
        resourceSummary: metadata.resourceSummary || '',
        fieldClassification: metadata.fieldClassification || '',
        headers: metadata.headers || []
      };
      
      // 创建dataContent对象
      const dataContentObj = {
        entity: newObject.entity,
        status: newObject.status,
        metadata: cleanMetadata,
        originalMetadata: cleanMetadata, // 使用清理后的元数据
        dataItems: newObject.dataItems || [],
        excelFileId: newObject.excelFileId
      };
      
      // 验证dataContent对象是否可以正确序列化
      const testJson = JSON.stringify(dataContentObj);
      JSON.parse(testJson); // 测试能否成功解析回对象
      
      // 验证通过后，设置dataContent
      newObject.dataContent = testJson;
      console.log('已生成并验证dataContent字段，包含元数据和excelFileId:', newObject.excelFileId);
    } catch (error) {
      console.error('生成dataContent失败:', error);
      // 创建一个最简单的dataContent，确保不会导致解析错误
      newObject.dataContent = JSON.stringify({
        entity: newObject.entity,
        status: '待检验',
        metadata: {
          dataName: newObject.entity || '未命名数据',
          sourceUnit: '数据部',
          contactPerson: '未指定',
          contactPhone: '未提供',
          resourceSummary: '数据资源',
          fieldClassification: '未分类',
          headers: []
        }
      });
      console.log('使用简化的dataContent作为备选');
    }
  } else if (newObject.dataContent && typeof newObject.dataContent === 'string' && !newObject.dataContent.includes('excelFileId')) {
    // 如果dataContent已存在但不包含excelFileId，尝试添加
    try {
      // 首先验证现有dataContent是否为有效JSON
      let dataContentObj;
      try {
        dataContentObj = JSON.parse(newObject.dataContent);
      } catch (jsonError) {
        console.error('现有dataContent不是有效JSON，重新创建:', jsonError);
        // 如果现有dataContent无效，重新创建一个
        dataContentObj = {
          entity: newObject.entity,
          status: newObject.status || '待检验'
        };
      }
      
      // 添加excelFileId
      dataContentObj.excelFileId = newObject.excelFileId;
      
      // 确保元数据被正确包含 - 使用原始元数据
      if (newObject.originalMetadata || newObject.metadata) {
        const metadata = newObject.originalMetadata || newObject.metadata || {};
        
        // 创建干净的元数据对象
        dataContentObj.metadata = {
          dataName: metadata.dataName || newObject.entity || '',
          sourceUnit: metadata.sourceUnit || '',
          contactPerson: metadata.contactPerson || '',
          contactPhone: metadata.contactPhone || '',
          resourceSummary: metadata.resourceSummary || '',
          fieldClassification: metadata.fieldClassification || '',
          headers: metadata.headers || []
        };
        
        // 保存原始元数据
        dataContentObj.originalMetadata = { ...dataContentObj.metadata };
      }
      
      // 验证新的dataContent是否可以正确序列化
      const testJson = JSON.stringify(dataContentObj);
      JSON.parse(testJson); // 测试能否成功解析回对象
      
      // 验证通过后，更新dataContent
      newObject.dataContent = testJson;
      console.log('向现有dataContent添加excelFileId和元数据:', newObject.excelFileId);
    } catch (error) {
      console.error('修改现有dataContent失败:', error);
      // 出错时创建一个新的简单dataContent
      newObject.dataContent = JSON.stringify({
        entity: newObject.entity,
        status: newObject.status || '待检验',
        excelFileId: newObject.excelFileId
      });
    }
  }
  
  // 确保excelFileId作为对象的顶级属性存在
  newObject.excelFileId = newObject.excelFileId || `fallback-${Date.now()}`;
  
  // 准备特殊请求参数，确保后端能找到上传的文件
  const requestParams = {
    excelFileId: newObject.excelFileId
  };
  
  // 【新增】确保元数据被正确传递
  if (newObject.metadata) {
    // 创建元数据JSON字符串
    const metadataJsonStr = JSON.stringify(newObject.metadata);
    
    // 添加metadataJson参数，增加成功率
    requestParams.metadataJson = metadataJsonStr;
    console.log('将元数据添加到请求参数:', metadataJsonStr);
    
    // 将元数据存储到dataContent中，作为备份
    if (newObject.dataContent && typeof newObject.dataContent === 'string') {
      try {
        const dataContentObj = JSON.parse(newObject.dataContent);
        dataContentObj.metadataJson = metadataJsonStr;
        newObject.dataContent = JSON.stringify(dataContentObj);
      } catch (e) {
        console.error('无法更新dataContent中的元数据:', e);
      }
    }
  }
  
  try {
    console.log('调用API添加数字对象:', {
      entity: newObject.entity,
      status: newObject.status,
      excelFileId: newObject.excelFileId,
      hasDataContent: !!newObject.dataContent
    });
    
    // 调用API添加数据对象，传递excelFileId参数
    const result = await dataObjectService.addDataObjectViaApi(newObject, requestParams);
    
    if (result.success) {
      ElMessage.success('数字对象添加成功');
      
      // 刷新表格数据
      loadDataFromBackend();
      
      // 关闭对话框
      createDialogVisible.value = false;
    } else {
      console.error('添加数字对象失败:', result.message, result);
      
      // 捕获具体错误提示
      let errorDetail = '';
      if (result.error && result.error.response) {
        if (result.error.response.data) {
          if (typeof result.error.response.data === 'string') {
            errorDetail = result.error.response.data.substring(0, 100);
          } else if (result.error.response.data.message) {
            errorDetail = result.error.response.data.message;
          }
        }
      }
      
      // 显示详细错误消息
      ElMessage.error(`添加失败: ${result.message}${errorDetail ? ` (${errorDetail})` : ''}`);
      
      // 如果API调用失败，尝试添加到本地数据
      console.log('尝试添加到本地数据');
      dataObjectService.addDataObject(newObject);
      
      // 仍然关闭对话框，但不刷新数据
      createDialogVisible.value = false;
    }
  } catch (error) {
    console.error('添加数字对象时发生异常:', error);
    ElMessage.error(`添加过程中发生错误: ${error.message || '未知错误'}`);
    
    // 如果异常，尝试添加到本地数据
    dataObjectService.addDataObject(newObject);
    createDialogVisible.value = false;
  }
}

// 取消新建
const cancelCreate = () => {
  // 对话框会自动关闭，不需要额外处理
}

// 重置编辑表单
const resetEditForm = () => {
  editForm.value = {
    id: '',
    entity: '',
    locationInfo: { row: '', col: '' },
    format: '',
    resourcePath: '',
    description: '',
    metadata: {
      dataName: '',
      sourceUnit: '',
      contactPerson: '',
      contactPhone: '',
      resourceSummary: '',
      fieldClassification: '',
      headers: []
    }
  }
}

// 处理元数据字符串的函数
const processMetadataString = (metadataString) => {
  console.log('处理元数据字符串，原始输入:', metadataString)
  
  if (!metadataString) {
    console.warn('元数据字符串为空')
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
    console.log('元数据已经是对象，无需解析')
    return {
      ...metadataString,
      contactPhone: metadataString.contactPhone || '未提供'  // 确保contactPhone字段存在
    }
  }
  
  // 修复JSON字符串中可能存在的常见问题
  let cleanString = metadataString.toString()
  
  try {
    // 处理双重转义的情况 (例如: "{\"key\":\"value\"}")
    
    // 首先尝试去掉外层引号，处理字符串形式的JSON
    if (cleanString.startsWith('"') && cleanString.endsWith('"')) {
      const unquoted = cleanString.slice(1, -1).replace(/\\"/g, '"')
      console.log('移除外层引号后:', unquoted)
      cleanString = unquoted
    }
    
    // 处理被转义多次的情况
    if (cleanString.includes('\\\"') || cleanString.includes('\\\\')) {
      cleanString = cleanString.replace(/\\\\/g, '\\').replace(/\\"/g, '"')
      console.log('处理转义字符后:', cleanString)
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
    
    console.log('清理后的字符串:', cleanString)
    
    // 尝试直接解析清理后的字符串
    try {
      const parsed = JSON.parse(cleanString)
      console.log('解析成功:', parsed)
      
      // 确保返回对象包含预期的字段
      return {
        dataName: parsed.dataName || '未知数据',
        sourceUnit: parsed.sourceUnit || '未知来源',
        contactPerson: parsed.contactPerson || '未指定',
        contactPhone: parsed.contactPhone || '未提供',  // 确保包含contactPhone
        resourceSummary: parsed.resourceSummary || '无描述',
        fieldClassification: parsed.fieldClassification || '未分类',
        headers: parsed.headers || []
      }
    } catch (parseError) {
      console.warn('JSON解析失败，尝试其他方法:', parseError)
      
      // 尝试使用正则表达式提取键值对
      const keyValuePairs = {}
      const regex = /"([^"]+)"\s*:\s*"([^"]*)"/g
      let match
      
      while ((match = regex.exec(cleanString)) !== null) {
        keyValuePairs[match[1]] = match[2]
      }
      
      if (Object.keys(keyValuePairs).length > 0) {
        console.log('使用正则表达式提取的键值对:', keyValuePairs)
        return {
          dataName: keyValuePairs.dataName || '未知数据',
          sourceUnit: keyValuePairs.sourceUnit || '未知来源',
          contactPerson: keyValuePairs.contactPerson || '未指定',
          contactPhone: keyValuePairs.contactPhone || '未提供',  // 确保包含contactPhone
          resourceSummary: keyValuePairs.resourceSummary || '无描述',
          fieldClassification: keyValuePairs.fieldClassification || '未分类'
        }
      }
      
      // 如果所有尝试都失败，返回默认元数据
      console.warn('所有解析方法都失败，返回默认元数据')
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
    console.error('处理元数据字符串时出错:', e)
    return {
      dataName: '解析错误',
      sourceUnit: '数据部',
      contactPerson: '未知',
      contactPhone: '未知',  // 确保默认值一致
      resourceSummary: '元数据解析失败: ' + e.message,
      fieldClassification: '未分类'
    }
  }
}

// 检查数据的各种可能位置，提取元数据
const extractMetadata = (row) => {
  if (!row) {
    console.warn('提取元数据时收到空对象')
    return createDefaultMetadata('未知实体')
  }
  
  console.log('开始提取元数据，数据源:', row)
  
  // 直接检查row中的metadata对象
  if (row.metadata && typeof row.metadata === 'object') {
    console.log('直接使用row.metadata对象:', row.metadata)
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
    console.log('从row.metadataJson提取元数据')
    try {
      const parsedMetadata = processMetadataString(row.metadataJson)
      console.log('成功解析metadataJson:', parsedMetadata)
      return parsedMetadata
    } catch (e) {
      console.warn('解析row.metadataJson失败:', e)
    }
  }
  
  // 检查dataContent字段中的元数据
  if (row.dataContent) {
    console.log('检查row.dataContent中的元数据')
    try {
      // 尝试解析dataContent
      const contentObj = typeof row.dataContent === 'string' ? 
        JSON.parse(row.dataContent) : row.dataContent
      
      // 更新status和feedback信息（如果存在）
      if (contentObj && contentObj.status) {
        row.status = contentObj.status;
        console.log('从dataContent更新状态:', row.status);
      }
      
      if (contentObj && contentObj.feedback) {
        row.feedback = contentObj.feedback;
        console.log('从dataContent提取反馈信息:', row.feedback);
      }

      // 更新dataItems（如果存在）
      if (contentObj && contentObj.dataItems) {
        row.dataItems = contentObj.dataItems;
        console.log('从dataContent提取数据项数组, 共', contentObj.dataItems.length, '项');
      }
      
      if (contentObj && contentObj.metadataJson) {
        console.log('从row.dataContent.metadataJson提取元数据')
        const parsedMetadata = processMetadataString(contentObj.metadataJson)
        console.log('成功从dataContent.metadataJson解析元数据:', parsedMetadata)
        return parsedMetadata
      }
      
      // 直接从dataContent中提取元数据字段
      if (contentObj && (contentObj.metadata || contentObj.dataName || contentObj.sourceUnit || 
          contentObj.contactPerson || contentObj.contactPhone)) {
        console.log('直接从dataContent中获取元数据字段')
        
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

      // 即使没有找到元数据，也尝试创建一个与entity相关的元数据
      if (contentObj && contentObj.entity) {
        console.log('从dataContent.entity创建基本元数据');
        return createDefaultMetadata(contentObj.entity);
      }
    } catch (e) {
      console.warn('解析dataContent失败:', e)
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
  }
  
  console.log('创建默认元数据，实体名称:', entityName)
  return {
    dataName: entityName,
    sourceUnit: sourceUnit,
    contactPerson: contactPerson,
    contactPhone: "123-456789",
    resourceSummary: `${entityName}数据资源`,
    fieldClassification: entityName.includes('用户') ? '用户数据' : 
                        (entityName.includes('订单') ? '订单数据' : '基础数据'),
    headers: []
  }
}

// 添加新的位置信息提取方法
const parseLocationInfoString = (locationInfoString) => {
  if (!locationInfoString) {
    return { row: '', col: '' };
  }
  
  if (typeof locationInfoString === 'object') {
    return {
      row: locationInfoString.row || '',
      col: locationInfoString.col || ''
    };
  }
  
  try {
    // 处理标准的格式化字符串，例如 (entityName, row, col)
    const locString = locationInfoString.toString().trim();
    if (locString.startsWith('(') && locString.endsWith(')')) {
      // 去掉括号并分割
      const parts = locString.substring(1, locString.length - 1).split(',').map(s => s.trim());
      if (parts.length >= 3) {
        // 第一部分是实体名称，第二部分是行，第三部分是列
        return { 
          row: parts[1], 
          col: parts[2] 
        };
      }
    }
    
    // 处理简单格式的"行x列"字符串
    if (locString.includes('行') && locString.includes('列')) {
      const rowMatch = locString.match(/(\d+)[^\d]*行/);
      const colMatch = locString.match(/(\d+)[^\d]*列/);
      
      return {
        row: rowMatch ? rowMatch[1] : '',
        col: colMatch ? colMatch[1] : ''
      };
    }
  } catch (e) {
    console.warn('解析位置信息字符串失败:', e);
  }
  
  // 返回默认空值
  return { row: '', col: '' };
};

// 处理刷新按钮点击
const handleRefreshClick = () => {
  console.log('手动刷新数据');
  ElMessage.info('正在刷新数据...');
  
  // 刷新数据
  refreshData();
  
  // 强制处理特定实体的反馈信息
  setTimeout(() => {
    console.log('检查并强制设置客户反馈实体的反馈信息');
    if (tableData.value && tableData.value.length) {
      tableData.value.forEach(row => {
        if (row.entity === '客户反馈') {
          if (row.dataContent && typeof row.dataContent === 'string' && 
              row.dataContent.includes('数据格式错误')) {
            console.log(`找到客户反馈实体，设置反馈信息`);
            row.feedback = '数据格式错误';
            row.status = '不合格';
          }
        }
      });
    }
    ElMessage.success('数据刷新完成');
  }, 1000);
}

// 编辑表单的初始值
const getDefaultEditForm = () => {
  return {
    entity: '',
    locationInfo: {
      row: '',
      col: ''
    },
    formatConstraint: '',
    accessConstraint: '',
    pathConstraint: '',
    regionConstraint: '',
    shareConstraint: '',
    transferControl: [],
    status: '待检验',
    metadata: {
      dataName: '',
      sourceUnit: '',
      contactPerson: '',
      contactPhone: '',
      resourceSummary: '',
      fieldClassification: ''
    },
    classificationValue: '未分类',
    levelValue: '未分级'
  }
}

// 处理数据更新
const handleDataUpdate = (newData) => {
  // 更新表格数据
  tableData.value = newData
  
  // 尝试保存更新到数据服务
  dataObjectService.syncDataObjects(tableData.value)
  
  // 更新总数
  totalCount.value = getFilteredDataCount()
}

// 获取过滤后的数据总数
const getFilteredDataCount = () => {
  let count = 0;
  for (let row of tableData.value) {
    if (row.status === currentStatus.value) {
      count++;
    }
  }
  return count;
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
  margin-bottom: 20px; /* 增加底部边距 */
  height: 32px;
  position: relative; /* 确保定位上下文 */
  z-index: 1; /* 提高层级 */
}

.total-text {
  font-size: 14px;
  color: #8c8c8c;
}

/* 预览对话框样式 */
.custom-dialog :deep(.el-dialog__body) {
  padding: 0;
  height: calc(90vh - 100px);
  max-height: calc(90vh - 100px);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.preview-header {
  padding: 15px 20px;
  background-color: transparent;
  border-bottom: none;
}

.preview-info {
  display: flex;
  flex-direction: column;
  gap: 10px;
  background-color: #f8f9fa;
  border-radius: 4px;
  padding: 10px;
  width: 100%;
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

.info-item strong {
  font-weight: bold;
  color: #606266;
  margin-right: 5px;
}

.constraint-info {
  max-width: 500px;
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

.excel-preview-wrapper {
  flex: 1;
  display: flex;
  overflow: hidden;
}

:deep(.excel-preview-container) {
  flex: 1;
  width: 100%;
  height: 100%;
}

/* 创建数字对象弹窗 */
.create-dialog .upload-region {
  border: 1px dashed #d9d9d9;
  border-radius: 4px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  padding: 20px;
  text-align: center;
  margin-top: 10px;
}

.create-dialog .upload-icon {
  font-size: 32px;
  color: #c0c4cc;
  margin-bottom: 8px;
}

.create-dialog .upload-text {
  color: #606266;
  font-size: 14px;
  margin-bottom: 8px;
}

.create-dialog .upload-tip {
  color: #909399;
  font-size: 12px;
}

/* 数据锁定状态占位符 */
.data-locked-placeholder {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100%;
}

.locked-icon {
  font-size: 48px;
  color: #909399;
  margin-bottom: 16px;
}

.data-locked-placeholder p {
  color: #606266;
  font-size: 16px;
}

/* 纯文本样式 */
.plain-text-container {
  color: #333;
  text-align: center;
  line-height: 1.5;
  padding: 2px 0;
}

/* 刷新按钮容器 */
.refresh-container {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 10px;
}

/* API错误提示样式 */
.api-error-alert {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  padding: 10px;
  z-index: 1000;
}

.api-error-content {
  background-color: #fff;
  padding: 10px;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.api-error-content p {
  margin-bottom: 10px;
}

.api-error-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* 调试工具样式 */
.debug-data-container {
  background-color: #f8f8f8;
  border-radius: 4px;
  padding: 10px;
}

.debug-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  font-weight: bold;
  color: #606266;
}

.debug-header span {
  font-size: 14px;
}

.debug-location-info {
  background-color: #e6f7ff;
  padding: 10px;
  border-radius: 4px;
  margin-bottom: 10px;
  border-left: 3px solid #1890ff;
}

.debug-location-info h4 {
  margin-top: 0;
  margin-bottom: 8px;
  color: #1890ff;
  font-size: 14px;
}

.debug-location-info pre {
  background-color: #f0f9ff;
  color: #1890ff;
  border: 1px solid #b3e0ff;
}

pre {
  background-color: #2d2d2d;
  color: #e6e6e6;
  padding: 10px;
  border-radius: 4px;
  white-space: pre-wrap;
  word-break: break-word;
}

/* 约束条件悬浮显示样式 */
.constraint-info {
  position: relative;
  cursor: help;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 100%;
}

.constraint-info:hover {
  overflow: visible;
  white-space: normal;
  background-color: #f0f9ff;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  z-index: 10;
  transition: all 0.3s;
}

/* 添加调试指示器 */
.debug-dialog-status {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  padding: 10px;
  z-index: 1000;
  background-color: #ffffff;
  border-top: 1px solid #e6e6e6;
}

.debug-dialog-status p {
  margin-bottom: 10px;
}

.form-label {
  font-weight: bold;
  margin-bottom: 10px;
}

/* 约束条件样式 */
.constraint-section {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.constraint-item {
  display: flex;
  align-items: center;
}

.constraint-item label {
  min-width: 120px;
  text-align: right;
  margin-right: 10px;
}

.selected-value {
  margin-left: 10px;
  padding: 2px 8px;
  background-color: #ecf5ff;
  color: #409eff;
  border-radius: 4px;
}

.custom-select-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.transfer-control-wrapper {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.transfer-control-tag {
  padding: 5px 10px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  background-color: #f5f7fa;
  color: #606266;
  cursor: pointer;
}

/* Excel上传区域样式 */
.upload-excel {
  width: 300px;
}

.upload-tip {
  color: #909399;
  font-size: 12px;
  margin-top: 5px;
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
</style> 