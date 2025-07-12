<template>
  <div class="datasource-container">
    <AppHeader role-name="税务局(数源方)" @logout="logout" />
    
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
              <li>确保后端服务在 http://localhost:8081 正常运行</li>
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
      <div class="content-card">
        <div class="table-title">我的数据对象列表</div>
        <!-- ObjectList组件紧跟标题下方 -->
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
          @visualization="showVisualization"
          @show-application-list="applicationListVisible = true"
        />
      </div>
    </div>
  </div>
  
  <el-dialog
    v-model="editDialogVisible"
    title="编辑数据对象"
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
            <el-button type="primary">上传</el-button>
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
      
      <el-form-item label="定位信息：" prop="locationInfoInput">
        <el-input v-model="editForm.locationInfoInput" placeholder="库名,表名,字段1,字段2..." style="width: 300px;" />
        <span style="color: #aaa; margin-left: 10px;">格式：库名,表名,字段1,字段2...</span>
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
      <!-- 分类分级值按钮（大按钮，带文字） -->
      <el-form-item label="分类分级值：">
        <el-button type="primary" class="generate-btn" style="width: 140px; height: 30px; font-size: 14px;" @click="openClassificationLevelDialog">生成分类分级值</el-button>
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
    :title="'新建数据对象'"
    v-model:modelValue="createForm"
    @save="saveCreateObject"
    @cancel="cancelCreate"
  />

  <!-- Excel预览对话框 -->
  <ObjectPreviewDialog
    v-model:visible="previewDialogVisible"
    :object="previewForm"
    :excelData="excelTableData"
  />

  <!-- 添加调试指示器 -->
  <div v-if="showDebugTools" class="debug-dialog-status">
    <p>编辑对话框状态: {{ editDialogVisible ? '可见' : '隐藏' }}</p>
    <p>编辑ID: {{ currentEditId || '无' }}</p>
    <el-button @click="testEditDialog">测试打开编辑对话框</el-button>
  </div>

  <!-- 在底部添加可视化对话框组件 -->
  <VisualizationDialog v-model:visible="visualizationVisible" />

  <ApplicationListDialog v-model:visible="applicationListVisible" />

  <!-- 在template底部添加分类分级对话框 -->
  <ClassificationLevelDialog
    v-model:visible="classificationLevelDialogVisible"
    :object-id="editForm.id"
    :model-value="classificationLevelData"
    @confirm="handleClassificationLevelConfirm"
  />

</template>

<script setup>
import { ref, computed, reactive, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus'
import { Search, Document, RefreshRight, DataAnalysis } from '@element-plus/icons-vue'
import * as XLSX from 'xlsx'
import CreateObjectDialog from '../../components/source/CreateObjectDialog.vue'
import ObjectList from '../../components/source/ObjectList.vue'
import AppHeader from '../../components/AppHeader.vue'
import CommonPagination from '@/components/CommonPagination.vue'
import dataObjectService from '@/services/dataObjectService'
import axios from 'axios'
import { API_URL, axiosInstance, testApiConnection } from '@/services/apiConfig'
import VisualizationDialog from '../../components/visualization/VisualizationDialog.vue'
import ApplicationListDialog from '@/components/source/ApplicationListDialog.vue'
import ObjectPreviewDialog from '@/components/ObjectPreviewDialog.vue'
import ClassificationLevelDialog from '@/components/source/ClassificationLevelDialog.vue'

const router = useRouter()
const activeTab = ref('objectList')
const currentStatus = ref('') 
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(5)
const totalCount = ref(0)
const selectedRows = ref([])
const normalWeight = ref(1.0)
const importantWeight = ref(2.0)
const criticalWeight = ref(3.0)
const isQualifiedStatus = computed(() => currentStatus.value === '已合格')
const editDialogVisible = ref(false)
const createDialogVisible = ref(false)
const currentEditId = ref('') 
const metadataDataName = ref('')
const metadataSourceUnit = ref('')
const metadataContactPerson = ref('')
const metadataContactPhone = ref('')


const editForm = reactive({
  id: '',
  entity: '',
  locationInfoInput: '',
  locationInfo: {},
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

const editingIndex = ref(-1)
const editFormRef = ref(null)
const createFormRef = ref(null)
const tableData = ref(dataObjectService.getAllDataObjects())

const sortState = reactive({
  prop: '',
  order: ''
})

const ensureArray = (value) => {
  if (Array.isArray(value)) {
    return [...value]
  }
  return value ? [value] : []
}

const filteredTableData = computed(() => {
  let result = tableData.value

  if (currentStatus.value === '待校验') {
    result = result.filter(item => item.status === '待校验' || item.status === '待检验')
  } else if (currentStatus.value === '待生成分类分级值') {
    result = result.filter(item => item.status === '待生成分类分级值')
  } else if (currentStatus.value) {
    result = result.filter(item => item.status === currentStatus.value)
  }

  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(item => {
      
      if (item.id.toString().includes(keyword) || 
          item.entity.toLowerCase().includes(keyword)) {
        return true
      }
      
      
      const constraints = ensureArray(item.constraint)
      if (constraints.some(c => c && c.toLowerCase().includes(keyword))) {
        return true
      }
      
      
      const transferControls = ensureArray(item.transferControl)
      if (transferControls.some(t => t && t.toLowerCase().includes(keyword))) {
        return true
      }
      
      return false
    })
  }
  if (sortState.prop === 'id') {
    if (sortState.order === 'ascending') {
      result = [...result].sort((a, b) => a.id - b.id)
    } else if (sortState.order === 'descending') {
      result = [...result].sort((a, b) => b.id - a.id)
    }
  }

  totalCount.value = result.length
  return result
})

const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

const handleEdit = async (row) => {
  const sourceObj = JSON.parse(JSON.stringify(row))
  
  resetEditForm()
  
  editForm.id = sourceObj.id
  editForm.entity = sourceObj.entity

  if (sourceObj.locationInfoJson) {
    try {
      const infoObj = typeof sourceObj.locationInfoJson === 'string'
        ? JSON.parse(sourceObj.locationInfoJson)
        : sourceObj.locationInfoJson;
      const { databaseName = '', tableName = '', selectFields = '' } = infoObj;
      let fieldsArr = [];
      if (Array.isArray(selectFields)) {
        fieldsArr = selectFields;
      } else if (typeof selectFields === 'string' && selectFields) {
        fieldsArr = selectFields.split(',').map(s => s.trim());
      }
      editForm.locationInfoInput = [databaseName, tableName, ...fieldsArr].filter(Boolean).join(',');
    } catch (e) {
      if (sourceObj.locationInfo && typeof sourceObj.locationInfo === 'object') {
        const { databaseName = '', tableName = '', selectFields = '' } = sourceObj.locationInfo
        let fieldsArr = []
        if (Array.isArray(selectFields)) {
          fieldsArr = selectFields
        } else if (typeof selectFields === 'string' && selectFields) {
          fieldsArr = selectFields.split(',').map(s => s.trim())
        }
        editForm.locationInfoInput = [databaseName, tableName, ...fieldsArr].filter(Boolean).join(',')
      } else if (typeof sourceObj.locationInfo === 'string') {
        editForm.locationInfoInput = sourceObj.locationInfo
      } else {
        editForm.locationInfoInput = ''
      }
    }
  } else if (sourceObj.locationInfo && typeof sourceObj.locationInfo === 'object') {
    const { databaseName = '', tableName = '', selectFields = '' } = sourceObj.locationInfo
    let fieldsArr = []
    if (Array.isArray(selectFields)) {
      fieldsArr = selectFields
    } else if (typeof selectFields === 'string' && selectFields) {
      fieldsArr = selectFields.split(',').map(s => s.trim())
    }
    editForm.locationInfoInput = [databaseName, tableName, ...fieldsArr].filter(Boolean).join(',')
  } else if (typeof sourceObj.locationInfo === 'string') {
    editForm.locationInfoInput = sourceObj.locationInfo
  } else {
    editForm.locationInfoInput = ''
  }
  
  editForm.metadata = extractMetadata(sourceObj)
  
  editForm.constraint = []

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
  

  if (sourceObj.constraint) {
    if (Array.isArray(sourceObj.constraint)) {

      sourceObj.constraint.forEach(constraint => {
        if (!editForm.constraint.includes(constraint)) {
          editForm.constraint.push(constraint)
        }
        
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

  if (sourceObj.transferControl) {
    editForm.transferControl = Array.isArray(sourceObj.transferControl) ? 
      sourceObj.transferControl : [sourceObj.transferControl]
  } else {
    editForm.transferControl = []
  }

  if (!Array.isArray(editForm.transferControl)) {
    editForm.transferControl = []
  }
  
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
  
  editForm.status = sourceObj.status || ''
  editForm.feedback = sourceObj.feedback || ''

  if (sourceObj.status === '不合格' || sourceObj.status === '已合格') {
    editForm.status = '待检验';
    if (editForm.dataEntity && typeof editForm.dataEntity === 'object') {
      editForm.dataEntity.status = '待检验';
    }
  }
  
  editForm.auditInfo = sourceObj.auditInfo || ''
  
  editForm.excelData = sourceObj.excelData || null

  editForm.dataItems = sourceObj.dataItems || []
  
  if (sourceObj.dataContent) {
    try {
      let contentObj = null
      
      if (typeof sourceObj.dataContent === 'string') {
        try {
          contentObj = JSON.parse(sourceObj.dataContent);
        } catch (jsonError) {

          
          const feedbackMatch = sourceObj.dataContent.match(/"feedback"\s*:\s*"([^"]*)"/);
          if (feedbackMatch && feedbackMatch[1]) {
            editForm.feedback = feedbackMatch[1];
          }
          
          const statusMatch = sourceObj.dataContent.match(/"status"\s*:\s*"([^"]*)"/);
          if (statusMatch && statusMatch[1]) {
            editForm.status = statusMatch[1];
          }
        }
      } else {
        contentObj = sourceObj.dataContent;
      }
      
      if (contentObj) {      
        if (contentObj.status) {
          editForm.status = contentObj.status;
        }
        
        if (contentObj.feedback) {
          editForm.feedback = contentObj.feedback;
        } else if (contentObj.data && contentObj.data.feedback) {
          editForm.feedback = contentObj.data.feedback;
        }

        if (contentObj.dataItems && Array.isArray(contentObj.dataItems)) {
          editForm.dataItems = contentObj.dataItems;
        }
      }
    } catch (e) {
      console.warn(`解析 ${sourceObj.entity} 的dataContent失败:`, e);
    }
  }
  
  try {
    const apiUrl = 'http://localhost:8081/api/objects/list';
    const response = await axios.get(apiUrl);
    
    let targetObject = null;
    let fetchedDataItems = null;
    
    
    if (response.data && Array.isArray(response.data)) {
      targetObject = response.data.find(item => item.id === sourceObj.id);
    } else if (response.data && response.data.list && Array.isArray(response.data.list)) {
      targetObject = response.data.list.find(item => item.id === sourceObj.id);
    } else if (response.data && response.data.data && Array.isArray(response.data.data)) {
      targetObject = response.data.data.find(item => item.id === sourceObj.id);
    }
    
    if (targetObject) {
      if (targetObject.dataItems && Array.isArray(targetObject.dataItems)) {
        fetchedDataItems = targetObject.dataItems;
      } else if (targetObject.dataContent) {
        try {
          const dataContent = typeof targetObject.dataContent === 'string' 
            ? JSON.parse(targetObject.dataContent) 
            : targetObject.dataContent;
          
          if (dataContent && dataContent.dataItems && Array.isArray(dataContent.dataItems)) {
            fetchedDataItems = dataContent.dataItems;
          }
        } catch (e) {
          console.error('解析dataContent失败:', e);
        }
      }
      

      if (fetchedDataItems && fetchedDataItems.length > 0) {
        editForm.dataItems = fetchedDataItems;
      }
    } else if (response.data && response.data.dataItems && Array.isArray(response.data.dataItems)) {
      const filteredItems = response.data.dataItems.filter(item => 
        item.objectId === sourceObj.id || 
        item.id === sourceObj.id ||
        (item.对象ID && item.对象ID === sourceObj.id)
      );
      
      if (filteredItems.length > 0) {
        editForm.dataItems = filteredItems;
      }
    }
  } catch (error) {
    console.error(`获取dataItems失败:`, error);
  }
  

  editDialogVisible.value = true
  

  if (sourceObj.weights) {
    normalWeight.value = sourceObj.weights.normalWeight || 1.0
    importantWeight.value = sourceObj.weights.importantWeight || 2.0
    criticalWeight.value = sourceObj.weights.criticalWeight || 3.0
    editForm.weights = { ...sourceObj.weights }
  } else {
    normalWeight.value = 1.0
    importantWeight.value = 2.0
    criticalWeight.value = 3.0
    editForm.weights = {
      normalWeight: 1.0,
      importantWeight: 2.0,
      criticalWeight: 3.0
    }
  }

  if (sourceObj.status === '不合格' || sourceObj.status === '已合格') {
    editForm.status = '待检验';
    if (editForm.dataEntity && typeof editForm.dataEntity === 'object') {
      editForm.dataEntity.status = '待检验';
    }
  }
}


const cancelEdit = () => {
  editDialogVisible.value = false
  currentEditId.value = ''
  editingIndex.value = -1
}


const saveEditObject = async (updatedObject) => {
  const objectId = updatedObject.id
  
  try {
    if (!updatedObject.dataItems || updatedObject.dataItems.length === 0) {
      try {
        const apiUrl = 'http://localhost:8081/api/objects/list';
        const response = await axios.get(apiUrl);
        
        let targetObject = null;
        let fetchedDataItems = null;
        

        if (response.data && Array.isArray(response.data)) {
          targetObject = response.data.find(item => item.id === objectId);
        } else if (response.data && response.data.list && Array.isArray(response.data.list)) {
          targetObject = response.data.list.find(item => item.id === objectId);
        } else if (response.data && response.data.data && Array.isArray(response.data.data)) {
          targetObject = response.data.data.find(item => item.id === objectId);
        }

        if (targetObject) {
          if (targetObject.dataItems && Array.isArray(targetObject.dataItems)) {
            fetchedDataItems = targetObject.dataItems;
          } else if (targetObject.dataContent) {

            try {
              const dataContent = typeof targetObject.dataContent === 'string' 
                ? JSON.parse(targetObject.dataContent) 
                : targetObject.dataContent;
              
              if (dataContent && dataContent.dataItems && Array.isArray(dataContent.dataItems)) {
                fetchedDataItems = dataContent.dataItems;
              }
            } catch (e) {
              console.error('解析dataContent失败:', e);
            }
          }
          
          if (fetchedDataItems && fetchedDataItems.length > 0) {
            updatedObject.dataItems = fetchedDataItems;
          }
        }
      } catch (error) {
        console.error(`获取dataItems失败:`, error);
      }
    }

    if (updatedObject.locationInfo) {
      if (typeof updatedObject.locationInfo === 'object' && 
          (updatedObject.locationInfo.row === undefined || updatedObject.locationInfo.col === undefined)) {
        updatedObject.locationInfo = {
          row: '',
          col: ''
        }
      }
    }
    

    if (!updatedObject.metadata) {
      updatedObject.metadata = createDefaultMetadata(updatedObject.entity)
    }
    

    let dataContent = {}
    try {

      if (typeof updatedObject.dataContent === 'string') {
        dataContent = JSON.parse(updatedObject.dataContent)
      } else if (updatedObject.dataContent) {
        dataContent = updatedObject.dataContent
      }
    } catch (e) {
      console.warn('解析现有dataContent失败，创建新对象', e)
      dataContent = {}
    }
    

    dataContent.entity = updatedObject.entity
    dataContent.status = updatedObject.status
    dataContent.feedback = updatedObject.feedback
    

    if (updatedObject.dataItems && updatedObject.dataItems.length > 0) {
      dataContent.dataItems = updatedObject.dataItems
    } else {
      if (dataContent.dataItems && Array.isArray(dataContent.dataItems) && dataContent.dataItems.length > 0) {
        updatedObject.dataItems = dataContent.dataItems;
      }
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


const handleDelete = (row) => {
  const objectId = row.id;
  
  ElMessageBox.confirm(`确定要删除"${row.entity}"吗?`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      const result = await dataObjectService.deleteDataObjectViaApi(objectId)
      dataObjectService.deleteDataObject(objectId)
      ElMessage.success(`已删除: ${row.entity}`)
      
      refreshData()
    } catch (error) {
      console.error('删除对象时出错:', error)

      try {
        dataObjectService.deleteDataObject(objectId)
        ElMessage.success(`已删除: ${row.entity}`)
        refreshData()
      } catch (localError) {
        ElMessage.error('本地删除失败，请稍后再试')
      }
    }
  }).catch(() => {
    ElMessage.info('已取消删除')
  })
}

const logout = () => {
  localStorage.removeItem('role')
  router.push('/login')
}

const showCreateDialog = () => {
  console.log('触发显示创建对话框')
  createDialogVisible.value = true
  console.log('createDialogVisible设置为:', createDialogVisible.value)
  
  setTimeout(() => {
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
    createForm.status = '待生成分类分级值'
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
  status: '待生成分类分级值',
  auditInfo: '',
  excelData: null 
})

// 表单校验规则
const formRules = {
  entity: [
    { required: true, message: '请输入实体名称', trigger: 'blur' }
  ],
  locationInfo: [
    { 
      validator: (rule, value, callback) => {
      
        console.log('验证定位信息: ', {
          isEditDialogVisible: editDialogVisible.value,
          'editForm.locationInfo': editForm.locationInfo,
          value: value,
          'form.locationInfo': value
        });
        
       
        if ((value && value.row && value.col) || (!value || (!value.row && !value.col))) {
          callback()
        } else {
         
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

// 保存编辑表单的方法
const handleSaveEditManually = () => {

  if (editFormRef.value) {

    if (!editForm.entity) {
      ElMessage.warning('请输入实体名称');
      return;
    }

    if (editForm.locationInfo) {
      if ((editForm.locationInfo.row && !editForm.locationInfo.col) || 
          (!editForm.locationInfo.row && editForm.locationInfo.col)) {
        ElMessage.warning('请同时填写定位信息的行和列，或者都不填');
        return;
      }
    }

    editFormRef.value.validate((valid, fields) => {
      if (valid) {

        const constraintArray = []
        if (editForm.formatConstraint) constraintArray.push(`格式约束:${editForm.formatConstraint}`)
        if (editForm.accessConstraint) constraintArray.push(`访问权限:${editForm.accessConstraint}`)
        if (editForm.pathConstraint) constraintArray.push(`传输路径约束:${editForm.pathConstraint}`)
        if (editForm.regionConstraint) constraintArray.push(`地域性约束:${editForm.regionConstraint}`)
        if (editForm.shareConstraint) constraintArray.push(`共享约束:${editForm.shareConstraint}`)

        editForm.constraint = constraintArray

        const propagationControl = {
          canRead: editForm.transferControl.includes('可读'),
          canModify: editForm.transferControl.includes('可修改'),
          canDestroy: editForm.transferControl.includes('可销毁'),
          canShare: editForm.transferControl.includes('可共享'),
          canDelegate: editForm.transferControl.includes('可委托')
        }

        let statusToSave = editForm.status;
        if (statusToSave === '待校验') {
          statusToSave = '待检验';
        }
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
          status: statusToSave || '',
          feedback: editForm.feedback || '',
          excelData: editForm.excelData,
          dataItems: editForm.dataItems || [],
          locationInfoInput: editForm.locationInfoInput,
          dataEntity: {
            ...(editForm.dataEntity || {}),
            status: statusToSave
          }
        }
        if (editForm.locationInfoInput) {
          const arr = editForm.locationInfoInput.split(',').map(s => s.trim())
          if (arr.length >= 2) {
            updatedObject.locationInfo = {
              ...updatedObject.locationInfo,
              databaseName: arr[0] || '',
              tableName: arr[1] || '',
              selectFields: arr.length > 2 ? arr.slice(2).join(',') : ''
            }
          }
        }

        saveEditObject(updatedObject)

        editDialogVisible.value = false
      } else {

        console.error('表单验证错误:', fields);

        let firstErrorField = '';
        let firstErrorMessage = '';
        
        if (fields) {
          for (const key in fields) {
            if (fields[key] && fields[key][0]) {
              firstErrorField = key;
              firstErrorMessage = fields[key][0].message;
              break;
            }
          }
        }
        
        if (firstErrorField && firstErrorMessage) {
          ElMessage.warning(`${firstErrorMessage} (字段: ${firstErrorField})`);
        } else {
          ElMessage.warning('表单验证失败，请检查必填字段');
        }
        return false;
      }
    })
  } else {
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
  totalCategoryValue: '',
  totalGradeValue: '',
  classificationValue: '',
  levelValue: '',
  metadata: null, 
  locationInfoJson: '',
  selectFields: ''
})

// Excel表格数据
const excelTableColumns = ref([])
const excelTableData = ref([])
const isExcelLoading = ref(false)
const excelSheets = ref([]) 
const activeSheet = ref('') 
const currentWorkbook = ref(null) 


const currentExcelFile = ref(null)

/**
 * Excel数据预览功能增强说明：
 * 1. 预览实体数据时，先尝试使用本地缓存的Excel数据
 * 2. 如果没有本地数据，则从API获取数据 - 使用/objects/list接口
 * 3. 从列表中过滤出当前对象ID对应的数据
 * 4. 支持多种数据结构解析，能够从不同的数据结构中提取表格数据
 * 5. 如果API获取失败或没有数据，使用模拟数据进行展示
 * 6. 提供详细的日志记录，便于调试
 */
const previewEntity = (row) => {
  console.log('预览实体数据:', row)
  
  // 深拷贝row，防止后续操作影响原始数据
  const rowCopy = JSON.parse(JSON.stringify(row))

  previewForm.id = rowCopy.id
  previewForm.entity = rowCopy.entity || (rowCopy.dataEntity && rowCopy.dataEntity.entity)
  previewForm.locationInfo = rowCopy.locationInfo
  previewForm.locationInfoJson = rowCopy.locationInfoJson
  previewForm.constraint = ensureArray(rowCopy.constraint)
  previewForm.transferControl = ensureArray(rowCopy.transferControl)
  previewForm.status = rowCopy.status || (rowCopy.dataEntity && rowCopy.dataEntity.status)

  previewForm.totalCategoryValue = ''
  previewForm.totalGradeValue = ''
  previewForm.classificationValue = ''
  previewForm.levelValue = ''

  if (rowCopy.metadata && typeof rowCopy.metadata === 'object') {
    previewForm.metadata = { ...rowCopy.metadata }
  } else if (rowCopy.dataEntity && rowCopy.dataEntity.metadata) {
    previewForm.metadata = { ...rowCopy.dataEntity.metadata }
  } else {
    previewForm.metadata = extractMetadata(rowCopy)
  }

  // 保存原始dataItems数据 - 优先从dataEntity中提取
  if (rowCopy.dataEntity && Array.isArray(rowCopy.dataEntity.dataItems)) {
    previewForm.dataItems = [...rowCopy.dataEntity.dataItems]
    console.log('从dataEntity中获取到dataItems:', previewForm.dataItems.length)
  } else if (Array.isArray(rowCopy.dataItems)) {
    previewForm.dataItems = [...rowCopy.dataItems]
    console.log('从顶层获取到dataItems:', previewForm.dataItems.length)
  } else {
    previewForm.dataItems = []
  }

  currentExcelFile.value = null
  excelTableData.value = []
  excelTableColumns.value = []
  excelSheets.value = []
  
  previewDialogVisible.value = true
  
  if (rowCopy.excelData) {
    ElMessage.info('正在准备Excel数据，请稍候...')
    isExcelLoading.value = true
  
    setTimeout(() => {
      try {
        currentExcelFile.value = rowCopy.excelData
      } catch (error) {
        fetchExcelDataFromApi(rowCopy.id, previewForm.dataItems)
      }
    }, 100)
  } else {
    console.log('没有本地Excel数据，尝试从API获取')
    fetchExcelDataFromApi(rowCopy.id, previewForm.dataItems)
  }
}

const fetchExcelDataFromApi = async (objectId, originalDataItems = []) => {
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
      extractClassificationValues(targetObject)
      
      // 优先检查dataEntity内部的dataItems
      if (targetObject.dataEntity && Array.isArray(targetObject.dataEntity.dataItems)) {
        dataItems = targetObject.dataEntity.dataItems
        console.log('从dataEntity内部获取到dataItems:', dataItems.length)
      } 
      // 然后检查顶层的dataItems
      else if (targetObject.dataItems && Array.isArray(targetObject.dataItems)) {
        dataItems = targetObject.dataItems
        console.log('从顶层获取到dataItems:', dataItems.length)
      } 
      // 再检查dataContent内的数据
      else if (targetObject.dataContent) {
        try {
          const dataContent = typeof targetObject.dataContent === 'string' 
            ? JSON.parse(targetObject.dataContent) 
            : targetObject.dataContent
            
          if (dataContent && dataContent.dataItems && Array.isArray(dataContent.dataItems)) {
            dataItems = dataContent.dataItems
            console.log('从dataContent中获取到dataItems:', dataItems.length)
          } else if (dataContent && dataContent.dataEntity && dataContent.dataEntity.dataItems) {
            dataItems = dataContent.dataEntity.dataItems
            console.log('从dataContent.dataEntity中获取到dataItems:', dataItems.length)
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
    }
    
    // 如果API没有返回dataItems，则保留原始dataItems
    if (!dataItems || dataItems.length === 0) {
      // 使用传入的原始dataItems
      dataItems = originalDataItems && originalDataItems.length > 0 ? originalDataItems : null;
      console.log('使用原始dataItems数据:', dataItems ? dataItems.length : 0)
    }
    
    // 如果仍然没有数据，才使用模拟数据
    if (!dataItems || dataItems.length === 0) {
      ElMessage.info(`未找到ID为${objectId}的Excel数据，显示示例数据`)
      dataItems = generateMockDataForObject(objectId)
      console.log('使用模拟数据:', dataItems.length)
    }
    
    createExcelFromDataItems(dataItems)
  } catch (error) {
    console.error('获取Excel数据失败:', error.message)
    ElMessage.error(`获取Excel数据失败: ${error.message}`)
    isExcelLoading.value = false
    
    // 出错时，优先使用原始数据
    if (originalDataItems && originalDataItems.length > 0) {
      console.log('API请求出错，使用原始dataItems:', originalDataItems.length)
      createExcelFromDataItems(originalDataItems)
    } else {
      console.log('API请求出错，使用模拟数据')
      const mockData = generateMockDataForObject(objectId)
      createExcelFromDataItems(mockData)
    }
  }
}

const extractClassificationValues = (obj) => {
  if (!obj) return
  
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
  
  if (obj.dataContent) {
    let dataContent = obj.dataContent
    if (typeof dataContent === 'string') {
      try {
        dataContent = JSON.parse(dataContent)
      } catch (e) {
          // 忽略解析错误
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

// 生成不同的模拟数据
const generateMockDataForObject = (objectId) => {
  const idNum = parseInt(objectId.slice(-2), 10) || 1
  
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
 
    const wb = XLSX.utils.book_new()
    const ws = XLSX.utils.json_to_sheet(dataItems)
    XLSX.utils.book_append_sheet(wb, ws, "数据")
    
    const excelBuffer = XLSX.write(wb, { bookType: 'xlsx', type: 'array' })
    const blob = new Blob([excelBuffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
 
    currentExcelFile.value = blob

    excelTableData.value = dataItems
    excelTableColumns.value = dataItems.length > 0 ? Object.keys(dataItems[0]).map(key => ({
      label: key,
      prop: key
    })) : []
    
    isExcelLoading.value = false
    ElMessage.success(`成功获取${dataItems.length}条数据记录`)
  } catch (error) {
    console.error('创建Excel数据失败:', error)
    ElMessage.error(`创建Excel数据失败: ${error.message}`)
    isExcelLoading.value = false
  }
}

// 获取模拟数据
const getMockDataItems = (objectId) => {
  const shortId = objectId ? objectId.substring(0, 4) : 'MOCK'
  return [
    {
      "产品ID": `P${shortId}-001`,
      "名称": "手机",
      "库存量": "200",
      "对象ID": objectId
    },
    {
      "产品ID": `P${shortId}-002`,
      "名称": "耳机",
      "库存量": "500",
      "对象ID": objectId
    },
    {
      "产品ID": `P${shortId}-003`,
      "名称": "充电器",
      "库存量": "300",
      "对象ID": objectId
    }
  ]
}

// 添加新的处理方法
const handleExcelDataLoaded = (data) => {
  console.log('Excel数据加载完成:', data)
  

  const isUserUploadedFile = tableData.value.some(row => 
    row.id === previewForm.id && row.excelData && row.excelData === currentExcelFile.value);
  const isApiData = excelTableData.value && excelTableData.value.length > 0;
  if (!isUserUploadedFile && !isApiData) {
    console.warn('检测到非用户上传且非API获取的Excel数据，已屏蔽');
    excelTableColumns.value = [];
    excelTableData.value = [];
    excelSheets.value = [];
    isExcelLoading.value = false;
    currentExcelFile.value = null;
    return;
  }
  
  const { headers, data: excelRows, sheets } = data;

  if (isApiData) {
    excelSheets.value = sheets || [];
    console.log('使用API获取的数据，保留已有表格数据');
  } else {

    excelTableColumns.value = headers || [];
    excelTableData.value = excelRows || [];
    excelSheets.value = sheets || [];
  }
  
  isExcelLoading.value = false;
  
  if (excelRows && excelRows.length) {
    ElMessage.success(`已成功加载 ${excelRows.length} 行数据`);
  } else if (excelTableData.value && excelTableData.value.length) {
    ElMessage.success(`已成功加载 ${excelTableData.value.length} 行数据`);
  } else {
    console.warn('加载的Excel数据为空');
  }
}

const handleExcelError = (error) => {
  console.error('Excel加载错误:', error)
  isExcelLoading.value = false
  ElMessage.error(`加载Excel时出错: ${error}`)
}

const handleExport = () => {
  ElMessage.success('导出功能待实现')
}

const clearAllTestData = () => {
  console.log('清除所有测试数据')

  tableData.value.forEach(row => {
    row.excelData = null
  })
  

  currentExcelFile.value = null
  excelTableData.value = []
  excelTableColumns.value = []
  excelSheets.value = []
}

// 专门处理客户反馈数据
const fixCustomerFeedbackData = () => {
  // 遍历所有数据行
  for (let i = 0; i < tableData.value.length; i++) {
    const row = tableData.value[i];
    
    // 找到客户反馈实体
    if (row.entity === '客户反馈') {
      // 直接在Vue响应式对象上设置属性
      if (!row.feedback && row.dataContent) {
        // 判断类型并提取feedback
        if (typeof row.dataContent === 'string') {
          // 使用正则表达式提取
          const match = row.dataContent.match(/"feedback"\s*:\s*"([^"]*)"/);
          if (match && match[1]) {
            // 直接更新数据对象的属性
            tableData.value[i] = {
              ...row,
              feedback: match[1],
              status: '不合格'
            };
            
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
        }
      }
    }
  }
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

// 处理新获取的数据，确保反馈意见能够正确显示
const processNewlyFetchedData = () => {
  // 如果没有数据，提前返回
  if (!tableData.value || tableData.value.length === 0) {
    return;
  }
  
  tableData.value.forEach(row => {
    // 特别处理客户反馈实体
    if (row.entity === '客户反馈') {
      // 直接从dataContent中提取feedback
      if (row.dataContent) {
        let feedbackValue = null;
        
        // 对字符串类型的dataContent进行处理
        if (typeof row.dataContent === 'string') {
          // 使用正则表达式直接提取feedback值
          const match = row.dataContent.match(/"feedback"\s*:\s*"([^"]*)"/);
          if (match && match[1]) {
            feedbackValue = match[1];
          } else if (row.dataContent.includes('数据格式错误')) {
            feedbackValue = '数据格式错误';
          }
        } 
        // 对对象类型的dataContent进行处理
        else if (typeof row.dataContent === 'object') {
          if (row.dataContent.feedback) {
            feedbackValue = row.dataContent.feedback;
          }
        }
        
        // 如果提取到了feedback值，直接设置到row上
        if (feedbackValue) {
          row.feedback = feedbackValue;
          row.status = '不合格';
        }
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
        }
      }
      // 尝试从对象类型的dataContent中提取feedback
      else if (typeof row.dataContent === 'object' && row.dataContent.feedback) {
        feedbackValue = row.dataContent.feedback;
      }
      
      // 如果提取到了feedback值，设置到row上
      if (feedbackValue) {
        row.feedback = feedbackValue;
        if (!row.status) {
          row.status = '不合格';
        }
      }
    }
  });
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
      // 解析失败
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
      // JSON解析失败
    }
  }
  
  return null
}


const navigateToHome = () => {
  // 重置当前状态
  currentStatus.value = ''
  searchKeyword.value = ''
  currentPage.value = 1
  
  
  activeTab.value = 'objectList'
  

  refreshData()
  

  ElMessage.success('已成功保存编辑并返回主页')
}


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
  createForm.status = '待生成分类分级值'
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
  
  // 不再强制修改状态，保留由dataObjectService设置的默认状态"待生成分类分级值"
  console.log('当前对象状态值:', newObject.status);
  
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
        status: newObject.status,
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
          status: newObject.status || '待校验'
        };
      }
      
      // 添加excelFileId
      dataContentObj.excelFileId = newObject.excelFileId;
      
      // 确保使用当前对象的状态，而不是硬编码
      dataContentObj.status = newObject.status;
      
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
        status: newObject.status,
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
    
    // 增加定位信息日志输出
    console.log('[新建数字对象] 即将上传的定位信息:', newObject.locationInfo);
    if (newObject.locationInfoJson) {
      console.log('[新建数字对象] locationInfoJson:', newObject.locationInfoJson);
    }
    
    // 调用API添加数据对象，传递excelFileId参数
    const result = await dataObjectService.addDataObjectViaApi(newObject, requestParams);
    
    if (result.success) {
      ElMessage.success('数据对象添加成功');
      
      // 刷新表格数据
      loadDataFromBackend();
      
      // 关闭对话框
      createDialogVisible.value = false;
    } else {
      console.error('添加数据对象失败:', result.message, result);
      
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
    console.error('添加数据对象时发生异常:', error);
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

    const locString = locationInfoString.toString().trim();
    if (locString.startsWith('(') && locString.endsWith(')')) {
      const parts = locString.substring(1, locString.length - 1).split(',').map(s => s.trim());
      if (parts.length >= 3) {

        return { 
          row: parts[1], 
          col: parts[2] 
        };
      }
    }
    
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
  

  return { row: '', col: '' };
};

// 处理刷新按钮点击
const handleRefreshClick = () => {
  console.log('手动刷新数据');
  ElMessage.info('正在刷新数据...');

  refreshData();

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
    status: '待校验',
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
  try {
    console.log('收到数据更新事件，数据项数量:', newData ? newData.length : 0);
    

    if (!newData || newData.length === 0) {
      console.warn('接收到的更新数据为空，尝试从dataObjectService获取数据');
      tableData.value = dataObjectService.getAllDataObjects();
    } else {
      tableData.value = newData;
    }

    dataObjectService.syncDataObjects(tableData.value);

    if (!tableData.value || tableData.value.length === 0) {
      console.warn('同步后表格数据仍为空，尝试刷新数据');
      refreshData();
    } else {

      totalCount.value = getFilteredDataCount();
    }
  } catch (error) {

    ElMessage.warning('数据更新处理失败，尝试刷新页面');
    refreshData();
  }
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


const getObjectKeys = (dataArray) => {
  if (!dataArray || !Array.isArray(dataArray) || dataArray.length === 0) {
    return [];
  }
  
  const keySets = dataArray.map(item => {
    if (item && typeof item === 'object') {
      return Object.keys(item);
    }
    return [];
  });

  const allKeys = [...new Set(keySets.flat())];
  
  return allKeys;
}

const handleExportExcel = () => {
  if (excelTableData.value.length === 0) {
    ElMessage.warning('没有数据可导出');
    return;
  }
  
  try {

    const wb = XLSX.utils.book_new();
    const ws = XLSX.utils.json_to_sheet(excelTableData.value);
    XLSX.utils.book_append_sheet(wb, ws, 'Sheet1');
    
    const fileName = `${previewForm.entity || 'excel_data'}.xlsx`;
    
    XLSX.writeFile(wb, fileName);
    
    ElMessage.success(`已成功导出 ${fileName}`);
  } catch (error) {
    console.error('导出Excel失败:', error);
    ElMessage.error(`导出Excel失败: ${error.message}`);
  }
}

// 添加可视化对话框控制变量
const visualizationVisible = ref(false);

// 添加显示可视化方法
const showVisualization = () => {
  visualizationVisible.value = true;
};

const applicationListVisible = ref(false)


function getLocationInfoObj(locationInfo, locationInfoJson) {
  // 优先用 locationInfo
  if (typeof locationInfo === 'string') {
    try {
      locationInfo = JSON.parse(locationInfo)
    } catch (e) {
      locationInfo = null
    }
  }
  if ((!locationInfo || typeof locationInfo !== 'object') && locationInfoJson) {
    try {
      locationInfo = JSON.parse(locationInfoJson)
    } catch (e) {
      locationInfo = null
    }
  }
  if (!locationInfo || typeof locationInfo !== 'object') return null
  return locationInfo
}


function isSelectFieldsLong(selectFields) {
  return typeof selectFields === 'string' && selectFields.length > 30
}


const classificationLevelDialogVisible = ref(false)
const classificationLevelData = ref({})


const openClassificationLevelDialog = () => {
  classificationLevelData.value = {
    classificationValue: editForm.classificationValue || editForm.totalCategoryValue || '',
    industryCategory: editForm.industryCategory || '',
    dataTimeliness: editForm.dataTimeliness || '',
    dataSource: editForm.dataSource || '',
    levelValue: editForm.levelValue || editForm.totalGradeValue || '',
    dbGrade: editForm.dbGrade !== undefined ? editForm.dbGrade : 0,
    tableGrade: editForm.tableGrade !== undefined ? editForm.tableGrade : 0,
    rowGrades: editForm.rowGrades || [0, 0],
    columnGrades: editForm.columnGrades || [0, 0]
  }
  classificationLevelDialogVisible.value = true
}

// 分类分级对话框确认回调
const handleClassificationLevelConfirm = async (data) => {

  editForm.classificationValue = data.classificationValue
  editForm.totalCategoryValue = data.classificationValue
  editForm.industryCategory = data.industryCategory
  editForm.dataTimeliness = data.dataTimeliness
  editForm.dataSource = data.dataSource
  editForm.levelValue = data.levelValue
  editForm.totalGradeValue = data.levelValue
  editForm.dbGrade = data.dbGrade
  editForm.tableGrade = data.tableGrade
  editForm.rowGrades = data.rowGrades
  editForm.columnGrades = data.columnGrades

  // 构建分类值数据
  const categoryData = {
    industryCategory: data.industryCategory || '',
    processingTimeCategory: data.dataTimeliness || '',
    dataSourceCategory: data.dataSource || ''
  }
  try {
    // 只在这里单独上传分类分级值
    const categoriesResponse = await fetch(`http://localhost:8081/api/objects/${editForm.id}/categories`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(categoryData)
    })
    if (!categoriesResponse.ok) {
      console.warn(`分类数据提交状态: ${categoriesResponse.status} ${categoriesResponse.statusText}`)
    } else {
      console.log('分类数据提交成功:', await categoriesResponse.text())
    }
  } catch (apiError) {
    console.error('分类数据API提交失败:', apiError)
  }
  ElMessage.success('分类分级值已更新')
  classificationLevelDialogVisible.value = false
}
</script>

<style scoped>
/* 全局样式 */
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
  min-height: 320px;
  max-height: 60vh;
  overflow: auto;
  display: flex;
  flex-direction: column;
}

/* 状态标签样式 */
.status-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
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

/* 移除按钮点击后的黑色边框 */
.el-button:focus {
  outline: none !important;
  box-shadow: none !important;
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

.basic-info-table.two-rows {
  flex-direction: column;
  gap: 10px;
}

.info-row {
  display: flex;
  flex-wrap: nowrap;
  gap: 20px;
  justify-content: center;
  width: 100%;
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

/* 加载中样式 */
.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 150px;
}

/* Excel表格数据样式 */
.excel-data-section {
  padding: 0 15px 15px;
}

.excel-table-container {
  padding: 0;
  background-color: #ffffff;
  border-radius: 4px;
}

.data-info {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

/* 无数据样式 */
.no-data-message {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  color: #909399;
}

/* 数据预览标题样式 */
.section-title {
  font-size: 18px;
  color: #333;
  margin: 10px 0 15px;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
  text-align: center;
}

.visualization-button-container {
  display: none;
}

.visualization-btn {
  display: none;
}

.table-title {
  font-size: 33px;
  font-weight: bold;
  text-align: center;
  margin-bottom: 5px;
  color: #222;
}

.select-fields-link {
  text-decoration: underline;
}

.weight-form {
  display: flex;
  gap: 10px;
}

.weight-item {
  display: flex;
  flex-direction: column;
}

.weight-label {
  font-weight: bold;
}

.weight-actions {
  display: flex;
  gap: 10px;
}
</style> 