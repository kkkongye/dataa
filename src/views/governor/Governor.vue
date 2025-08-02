<template>
  <div class="datasource-container watermark-bg">
    <AppHeader @logout="logout" />
    <div class="main-content">
      <div class="content-card">
        <div class="table-title">待治理的数据对象列表</div>
        <div class="status-filter">
          <el-button :class="['status-btn', { active: currentStatus === '' }]" @click="setStatus('')">全部数据对象</el-button>
          <el-button :class="['status-btn', { active: currentStatus === '待校验' }]" @click="setStatus('待校验')">待校验</el-button>
          <el-button :class="['status-btn', { active: currentStatus === '已合格' }]" @click="setStatus('已合格')">已合格</el-button>
          <el-button :class="['status-btn', { active: currentStatus === '不合格' }]" @click="setStatus('不合格')">不合格</el-button>
        </div>
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
            <el-button type="success" plain @click="refreshTableData"><el-icon><Refresh /></el-icon>刷新数据</el-button>
            <!-- <el-button type="primary" plain @click="handleGenerateOrgVouchers">生成组织机构凭证</el-button> -->
            <el-button type="primary" plain @click="handleGenerateAndSendCapsule">生成并发送数据胶囊给使用方</el-button>
            <el-button 
              :type="hasGovernanceApplications ? 'warning' : 'info'" 
              plain 
              @click="applicationListVisible = true"
            >
              {{ hasGovernanceApplications ? '申请列表（有新的申请）' : '申请列表' }}
            </el-button>
          </div>
        </div>
        
        <!-- 数据表格 -->
        <div class="table-container">
          <el-table
            :data="filteredTableData"
            style="width: 100%"
            :cell-style="cellStyle"
            :header-cell-style="headerCellStyle"
            border
            height="100%"
            fit
          >
            <el-table-column prop="id" label="ID" width="200" align="center">
              <template #default="scope">
                <div class="id-cell">{{ scope.row.id }}</div>
              </template>
            </el-table-column>
            <el-table-column prop="entity" label="实体" width="120" align="center">
              <template #default="scope">
                <el-link type="primary" @click="previewEntity(scope.row)" class="entity-link">{{ scope.row.entity }}</el-link>
              </template>
            </el-table-column><el-table-column prop="locationInfo" label="定位信息" min-width="120" align="center">
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
            </el-table-column>
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
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="transferControl" label="传输控制操作" min-width="200" align="center">
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
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="auditInfo" label="审计控制信息" width="150" align="center">
              <template #default="scope">
                <el-link type="primary" @click="showAuditLogDialog(scope.row)">查看日志</el-link>
              </template>
            </el-table-column>
            <el-table-column prop="classificationLevelValue" label="分类分级值" width="180" align="center">
              <template #default="scope">
                <div class="classification-level-container">
                  <div class="classification-level-item">
                    <!-- <span class="label">分类值：</span> -->
                    <span class="value">{{ 
                      (() => {
                        const sum = (parseFloat(scope.row.totalCategoryValue) || 0) + (parseFloat(scope.row.totalGradeValue) || 0);
                        return sum === 0 ? '未生成分类分级值' : sum.toFixed(4);
                      })()
                    }}</span>
                    <!-- <span class="value">{{ (parseFloat(scope.row.totalCategoryValue) || 0) + (parseFloat(scope.row.totalGradeValue) || 0) || '未生成分类分级值' }}</span> -->
                  </div>
                  <!-- <div class="classification-level-item">
                    <span class="label">分级值：</span>
                    <span class="value">{{ scope.row.totalGradeValue || scope.row.levelValue || '未分级' }}</span>
                  </div> -->
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100" align="center">
              <template #default="scope">
                <span :class="['status-tag', getStatusClass(scope.row.status)]">
                  {{ (scope.row.status === '待检验' || scope.row.status === '待校验') ? '待校验' : scope.row.status }}
                </span>
              </template>
            </el-table-column>
            <el-table-column v-if="!isQualifiedStatus && currentStatus !== '待校验'" prop="feedback" label="反馈意见" min-width="170" align="center">
              <template #default="scope">
                <div style="display: flex; flex-direction: column; align-items: center;">
                  <span v-if="scope.row.feedback" :class="['feedback-text', getFeedbackClass(scope.row.status)]" style="margin-bottom: 10px;">
                    {{ scope.row.feedback }}
                  </span>
                  <span v-else-if="scope.row.dataContent" :class="['feedback-text', getFeedbackClass(scope.row.status)]" style="margin-bottom: 10px;">
                    {{ extractFeedback(scope.row.dataContent) }}
                  </span>
                  <span v-else style="margin-bottom: 10px;"></span>
                  <el-button v-if="scope.row.auditReport" link type="info" size="small" style="margin-top: 0;" @click="showReviewReport(scope.row)">自动化审查报告</el-button>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="230" align="center">
              <template #default="scope">
                <div class="status-buttons">
                  <el-button type="primary" size="small" plain @click="handleReview(scope.row)">自动化审查</el-button>
                  <!-- <el-button type="success" size="small" plain :disabled="scope.row.status === '已合格'" @click="updateStatus(scope.row, '已合格')">正确</el-button>
                  <el-button type="danger" size="small" plain :disabled="scope.row.status === '不合格'" @click="updateStatus(scope.row, '不合格')">错误</el-button> -->
                  <el-button type="warning" size="small" plain @click="previewEntity(scope.row)">手工审查</el-button>
                </div>
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
            :page-sizes="[5, 10, 20]"
            background
            @size-change="handleSizeChange"
          />
        </div>
      </div>
    </div>
  </div>
  
  <!-- Excel预览对话框 -->
    <ObjectPreviewDialog
    v-model:visible="previewDialogVisible"
    :object="previewForm"
    :excelData="excelTableData"
  >
    <template v-slot:footer>
      <span class="dialog-footer">
        <el-button v-if="showJudgeButtonsForPreview" type="success" plain @click="handlePreviewJudge('pass')">合格</el-button>
        <el-button v-if="showJudgeButtonsForPreview" type="danger" plain @click="handlePreviewJudge('fail')">不合格</el-button>
      </span>
    </template>
  </ObjectPreviewDialog>

  <!-- 审核报告弹窗 -->
  <ReportViewer ref="reportViewer" v-model:visible="reportDialogVisible" :object-id="currentReviewObjectId">
    <template #footer>
      <span class="dialog-footer">
        <template v-if="showJudgeButtons">
          <el-button type="success" plain @click="handleJudge('pass')">合格小结</el-button>
          <el-button type="danger" plain @click="handleJudge('fail')">不合格反馈意见</el-button>
        </template>
        <el-button type="warning" plain @click="handleSendAuditReport">发送审查报告至数源方</el-button>
        <el-button type="primary" @click="$refs.reportViewer.exportReport()">导出报告</el-button>
      </span>
    </template>
  </ReportViewer>

  <AuditLogDialog 
    :visible="auditLogVisible" 
    :object-id="currentRow?.id"
    :entity-name="currentRow?.entity || ''"
    @close="auditLogVisible = false" 
  />

  <ApplicationListDialog v-model:visible="applicationListVisible" />

  <!-- 请求通知弹窗 -->
  <el-dialog
    v-model="requestNotificationVisible"
    title="收到数据胶囊申请"
    width="500px"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    :show-close="false"
    class="centered-dialog"
  >
    <div class="request-notification-content">
      <div class="notification-info">
        <p><strong>申请人：</strong>{{ pendingRequest?.applicant || '未知' }}</p>
        <div v-if="pendingRequest?.ids">
          <p><strong>申请数据对象：</strong></p>
          <template v-if="Array.isArray(pendingRequest.ids)">
            <p v-for="(id, index) in pendingRequest.ids" :key="index" style="margin-top: 5px;">
              {{ id }}
            </p>
          </template>
          <template v-else-if="typeof pendingRequest.ids === 'string' && pendingRequest.ids.includes(',')">
            <p v-for="(id, index) in pendingRequest.ids.split(',')" :key="index" style="margin-top: 5px;">
              {{ id.trim() }}
            </p>
          </template>
          <template v-else>
            <p style="margin-top: 5px;">{{ pendingRequest.ids }}</p>
          </template>
        </div>
        <p v-else><strong>申请数据对象：</strong>未知</p>
        <div v-if="pendingRequest?.entityName">
          <p><strong>实体名：</strong></p>
          <template v-if="Array.isArray(pendingRequest.entityName)">
            <p v-for="(name, index) in pendingRequest.entityName" :key="index" style="margin-top: 5px;">
              {{ name }}
            </p>
          </template>
          <template v-else-if="typeof pendingRequest.entityName === 'string' && pendingRequest.entityName.includes(',')">
            <p v-for="(name, index) in pendingRequest.entityName.split(',')" :key="index" style="margin-top: 5px;">
              {{ name.trim() }}
            </p>
          </template>
          <template v-else>
            <p style="margin-top: 5px;">{{ pendingRequest.entityName }}</p>
          </template>
        </div>
        <p v-else><strong>实体名：</strong>未知实体</p>
        <p><strong>申请时间：</strong>{{ new Date().toLocaleString() }}</p>
      </div>
    </div>
    
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleRequestNotification('later')">稍后处理</el-button>
        <el-button type="primary" @click="handleRequestNotification('generate')">
          立即生成并发送数据胶囊
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, reactive, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus'
import { Search, Lock, Document, UploadFilled, Download, Refresh } from '@element-plus/icons-vue'
import * as XLSX from 'xlsx'
import ExcelPreview from '../../components/ExcelPreview.vue'
import AppHeader from '../../components/AppHeader.vue'
import CommonPagination from '../../components/CommonPagination.vue'
import ReportViewer from '@/components/governor/ReportViewer.vue'
import dataObjectService from '../../services/dataObjectService'
import reportService from '../../services/reportService'
import { ensureArray, advancedSearch } from '../../utils/searchUtils'
import axios from 'axios'
import AuditLogDialog from '@/components/source/AuditLogDialog.vue'
import ApplicationListDialog from '@/components/governor/ApplicationListDialog.vue'
import ObjectPreviewDialog from '@/components/ObjectPreviewDialog.vue'

const router = useRouter()
const currentStatus = ref('') 
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(5) 

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
const tableData = ref([])

// 添加申请记录状态检查
const applicationStatus = ref({})

// 检查申请记录状态
const checkApplicationStatus = async () => {
  try {
    const response = await axios.get('http://localhost:8083/api/application-records')
    if (response.data && response.data.code === 1 && Array.isArray(response.data.data)) {
      const applications = response.data.data
      const statusMap = {}
      applications.forEach(app => {
        // 处理objectIds字段，可能包含多个ID用逗号分隔
        if (app.objectIds) {
          const objectIds = app.objectIds.split(',')
          objectIds.forEach(objectId => {
            statusMap[objectId.trim()] = {
              sourceAgreed: app.sourceAgreed,
              governanceAgreed1: app.governanceAgreed1,
              governanceAgreed2: app.governanceAgreed2
            }
          })
        }
      })
      applicationStatus.value = statusMap
      console.log('治理方申请记录状态:', statusMap)
    }
  } catch (error) {
    console.error('检查申请记录状态失败:', error)
  }
}

// 计算是否有需要治理方处理的申请
const hasGovernanceApplications = computed(() => {
  return Object.values(applicationStatus.value).some(status => 
    status.sourceAgreed === true && status.governanceAgreed1 === false
  )
})

// 轮询相关变量
const pollingTimer = ref(null)
const requestNotificationVisible = ref(false)
const pendingRequest = ref(null)
const pollingInterval = ref(5000) // 轮询间隔，默认5秒

// 从localStorage初始化已处理的请求ID集合
const initProcessedRequestIds = () => {
  try {
    const stored = localStorage.getItem('processedRequestIds')
    return stored ? new Set(JSON.parse(stored)) : new Set()
  } catch (error) {
    console.warn('读取localStorage中的processedRequestIds失败:', error)
    return new Set()
  }
}

const processedRequestIds = ref(initProcessedRequestIds()) // 已处理的请求ID集合，防止重复弹窗

// 保存已处理ID集合到localStorage
const saveProcessedRequestIds = () => {
  try {
    localStorage.setItem('processedRequestIds', JSON.stringify([...processedRequestIds.value]))
  } catch (error) {
    console.warn('保存processedRequestIds到localStorage失败:', error)
  }
} 

// 适配后端数据到前端格式（增强版）
function adaptBackendData(backendItem) {
  if (!backendItem) {
    return {
      id: '',
      entity: '',
      locationInfo: '',
      locationInfoJson: '',
      constraint: [],
      transferControl: [],
      propagationControl: null,
      auditInfo: '',
      status: '',
      feedback: '',
      totalCategoryValue: '',
      totalGradeValue: '',
      metadata: null,
      dataContent: '',
      hasReview: false,
      auditReport: '' // 新增
    }
  }

  let parsedLocation = null
  if (backendItem.locationInfoJson) {
    try {
      parsedLocation = JSON.parse(backendItem.locationInfoJson)
    } catch (e) {
      console.warn('解析locationInfoJson失败:', e)
    }
  } else if (backendItem.locationInfo && typeof backendItem.locationInfo === 'object') {
    parsedLocation = backendItem.locationInfo
  }

  const constraintArray = backendItem.constraintSet && backendItem.constraintSet.constraints
    ? backendItem.constraintSet.constraints.map(c => [
        `格式约束: ${c.formatConstraint}`,
        `访问约束: ${c.accessConstraint}`,
        `路径约束: ${c.pathConstraint}`,
        `区域约束: ${c.regionConstraint}`,
        `共享约束: ${c.shareConstraint}`
      ]).flat()
    : []

  let transferControlArray = [];
  let propagationControlObj = null;
  
  // 增强的传播控制处理
  if (backendItem.propagationControl) {
    propagationControlObj = backendItem.propagationControl;
  } else if (backendItem.propagationControlJson) {
    try {
      propagationControlObj = typeof backendItem.propagationControlJson === 'string'
        ? JSON.parse(backendItem.propagationControlJson)
        : backendItem.propagationControlJson;
    } catch (e) {
      console.warn('解析propagationControlJson失败:', e);
    }
  }
  
  if (propagationControlObj) {
    if (propagationControlObj.canRead) transferControlArray.push('可读');
    if (propagationControlObj.canModify) transferControlArray.push('可修改');
    if (propagationControlObj.canShare) transferControlArray.push('可共享');
    if (propagationControlObj.canDelegate) transferControlArray.push('可委托');
    if (propagationControlObj.canDestroy) transferControlArray.push('可销毁');
  } else if (backendItem.constraintSet && backendItem.constraintSet.constraints) {
    const constraints = backendItem.constraintSet.constraints[0] || {};
    if (constraints.accessConstraint && constraints.accessConstraint.includes('允许')) transferControlArray.push('可读');
    if (constraints.shareConstraint && constraints.shareConstraint.includes('允许')) transferControlArray.push('可共享');
    if (constraints.pathConstraint && constraints.pathConstraint.includes('点对点')) transferControlArray.push('可委托');
  }

  const auditInfo = backendItem.auditInfo ? '查看日志' : ''

  // 处理locationInfo
  let locationInfo = ''
  if (parsedLocation && parsedLocation.locations) {
    locationInfo = parsedLocation.locations.map(loc =>
      `(${loc.sheet || '默认'}, ${loc.startRow || '1'}-${loc.endRow || '*'}, ${loc.startColumn || 'A'}-${loc.endColumn || '*'})`
    ).join('; ')
  } else if (parsedLocation) {
    // 直接使用locationInfo对象格式
    locationInfo = `(${parsedLocation.databaseName || '-'}, ${parsedLocation.tableName || '-'}, "select字段")`
  }

  // 处理反馈信息
  let feedback = ''
  if (backendItem.dataEntity && backendItem.dataEntity.feedback) {
    feedback = backendItem.dataEntity.feedback
  } else if (backendItem.feedback) {
    feedback = backendItem.feedback
  }

  // 处理元数据
  let metadata = null
  if (backendItem.dataEntity && backendItem.dataEntity.metadata) {
    metadata = backendItem.dataEntity.metadata
  } else if (backendItem.metadataJson) {
    try {
      metadata = JSON.parse(backendItem.metadataJson)
    } catch (e) {
      console.warn('解析metadataJson失败:', e);
    }
  } else if (backendItem.metadata) {
    metadata = backendItem.metadata
  }

  // 处理状态
  const status = backendItem.dataEntity && backendItem.dataEntity.status
    ? backendItem.dataEntity.status
    : backendItem.status || ''

  // 处理分类分级值
  const totalCategoryValue = backendItem.totalCategoryValue || 
                            (backendItem.dataEntity && backendItem.dataEntity.totalCategoryValue) || 
                            backendItem.classificationValue || 
                            ''
  
  const totalGradeValue = backendItem.totalGradeValue || 
                          (backendItem.dataEntity && backendItem.dataEntity.totalGradeValue) || 
                          backendItem.levelValue || 
                          ''

  // 增强的实体名称处理
  let entity = '';
  if (backendItem.dataEntity && backendItem.dataEntity.entity) {
    entity = backendItem.dataEntity.entity;
  } else if (backendItem.entity) {
    entity = backendItem.entity;
  } else if (backendItem.dataContent) {
    try {
      const dataContent = typeof backendItem.dataContent === 'string' 
        ? JSON.parse(backendItem.dataContent) 
        : backendItem.dataContent;
      if (dataContent && dataContent.entity) {
        entity = dataContent.entity;
      } else if (dataContent && dataContent.dataEntity && dataContent.dataEntity.entity) {
        entity = dataContent.dataEntity.entity;
      }
    } catch (e) {
      console.warn('从dataContent解析实体名称失败:', e);
    }
  }
  
  // 处理dataContent
  const dataContent = backendItem.dataContent || 
                     (backendItem.dataEntity && backendItem.dataEntity.dataContent) || 
                     JSON.stringify(backendItem.dataEntity || {})
  
  const result = {
    id: backendItem.id || '',
    entity,
    locationInfo,
    locationInfoJson: backendItem.locationInfoJson || 
                      (parsedLocation ? JSON.stringify(parsedLocation) : ''),
    constraint: constraintArray,
    transferControl: transferControlArray,
    propagationControl: propagationControlObj,
    auditInfo,
    status,
    feedback,
    totalCategoryValue,
    totalGradeValue,
    metadata,
    dataContent,
    hasReview: backendItem.hasReview || false,
    auditReport: backendItem.auditReport || '' // 新增
  }
  
  return result;
}

// 页面加载时从后端获取数据并适配
const loadDataFromBackend = async () => {
  try {
    const response = await axios.get('http://localhost:8082/api/objects')
    let dataArray = []
    if (Array.isArray(response.data)) {
      dataArray = response.data
    } else if (response.data.data && Array.isArray(response.data.data)) {
      dataArray = response.data.data
    } else if (response.data.list && Array.isArray(response.data.list)) {
      dataArray = response.data.list
    } else {
      console.warn('未识别的数据格式:', response.data)
      ElMessage.warning('数据格式不符合预期，请检查控制台')
    }
    
    tableData.value = dataArray.map(item => adaptBackendData(item))
    
    if (tableData.value.length === 0) {
      ElMessage.warning('没有获取到数据对象,请等待数源方发送')
    } else {
      ElMessage.success(`成功加载数据对象`)
    }
  } catch (error) {
    console.error('获取数据失败:', error)
    ElMessage.error('获取数据失败: ' + (error.message || '未知错误'))
  }
}

// 刷新表格数据
const refreshTableData = async () => {
  try {
    ElMessage.info('正在刷新数据...')
    await loadDataFromBackend()
    ElMessage.success('数据刷新完成')
  } catch (error) {
    console.error('刷新数据失败:', error)
    ElMessage.error('刷新数据失败: ' + (error.message || '未知错误'))
  }
}

onMounted(() => {
  setWatermark('治  理  方')
  window.addEventListener('resize', () => setWatermark('治  理  方'))
  const currentRole = localStorage.getItem('role');
  if (currentRole === 'governor') { 
    handleInitSystem();
  } else {
    loadDataFromBackend();
  }
  // 检查申请记录状态
  checkApplicationStatus()
})
onBeforeUnmount(() => {
  removeWatermark()
  window.removeEventListener('resize', () => setWatermark('治  理  方'))
})

// 计算实际数据量
const totalCount = computed(() => {
  let result = tableData.value;

  result = result.filter(item => item.status !== '待生成分类分级值');
  if (currentStatus.value === '待校验') {
    result = result.filter(item => item.status === '待校验' || item.status === '待检验');
  } else if (currentStatus.value && currentStatus.value !== '待生成分类分级值') {
    result = result.filter(item => item.status === currentStatus.value);
  }
  if (searchKeyword.value) {
    result = advancedSearch(result, searchKeyword.value);
  }
  return result.length;
});


const filteredTableData = computed(() => {
  let result = tableData.value;

  result = result.filter(item => item.status !== '待生成分类分级值');
  if (currentStatus.value === '待校验') {
    result = result.filter(item => item.status === '待校验' || item.status === '待检验');
  } else if (currentStatus.value === '待生成分类分级值') {

    result = [];
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

// 获取状态对应的样式类名
const getStatusClass = (status) => {
  if (status === '待校验' || status === '待检验') return 'status-pending'
  if (status === '已合格') return 'status-success'
  if (status === '不合格') return 'status-error'
  if (status === '待生成分类分级值') return 'status-to-generate'
  return ''
}



/// 处理审查功能
const handleReview = async (row) => {
  try {
    currentReviewObjectId.value = row.id;
    await ElMessageBox.confirm('是否进行审查？', '提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'info',
    });
    const isBasicRegistrationData = row.entity === '基本登记信息模拟数据';
    const reportApi = isBasicRegistrationData ? 'baogao1' : 'baogao2';
    const fillReportApi = isBasicRegistrationData ? 'fill-audit-report1' : 'fill-audit-report2';
    
    try {
      await axios.get(`http://localhost:8082/api/${reportApi}`);
    } catch (e) {
      ElMessage.error('8082接口调用失败: ' + (e.message || '未知错误'));
      return;
    }
    const loading2 = ElLoading.service({
      fullscreen: true,
      text: '正在进行审查...',
      background: 'rgba(0, 0, 0, 0.7)'
    });
    let updateResponse;
    try {
      updateResponse = await axios.post(`http://localhost:8082/api/objects/${row.id}/${fillReportApi}`);
    } catch (error) {
      console.error('审查接口调用失败:', error);
      ElMessage.error(`审查失败: ${error.message || '未知错误'}`);
      loading2.close();
      return;
    }
    loading2.close();
    reportDialogVisible.value = true;
    showJudgeButtons.value = true;
    judgeRow.value = row;
  } catch (err) {
    if (err && err.message && err.message.includes('cancel')) {
      ElMessage.info('操作已取消');
    } else {
      console.error('审查过程出错:', err);
      ElMessage.error(`审查失败: ${err.message || '未知错误'}`);
    }
  }
};

const handleJudge = async (result) => {
  if (!judgeRow.value) return;
  
  const currentRow = judgeRow.value; // 保存当前行数据
  
  if (result === 'pass') {
    // 弹出合格小结弹窗
    ElMessageBox.prompt('请输入合格小结', '合格小结', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputValue: '',
      customClass: 'feedback-dialog',
      inputType: 'textarea',
      inputPlaceholder: '请详细描述合格的原因和小结...',
      inputValidator: (value) => {
        return value.trim() !== '' || '合格小结不能为空';
      }
    }).then(async ({ value }) => {
      try {
        // 以待校验状态和合格小结上传到后端
        const result = await updateStatusViaBothPorts(currentRow.id, '待校验', `自动化审查合格小结：${value}`);
        if (result) {
          // 弹出确认对话框而不是使用ElMessage
          await ElMessageBox.alert('自动化审查已合格，请继续手工审查', '提示', {
            confirmButtonText: '好的',
            type: 'success'
          });
          loadDataFromBackend(); // 重新加载数据
        } else {
          ElMessage.warning(`${currentRow.entity} 状态更新失败`);
        }
      } catch (error) {
        console.error('更新状态时出错:', error);
        ElMessage.error(`更新 ${currentRow.entity} 状态失败: ${error.message || '未知错误'}`);
      }
      // 操作完成后清理状态
      showJudgeButtons.value = false;
      judgeRow.value = null;
    }).catch(() => {
      ElMessage.info('已取消操作');
      // 取消操作后也要清理状态
      showJudgeButtons.value = false;
      judgeRow.value = null;
    });
  } else if (result === 'fail') {
    await updateStatus(currentRow, '不合格');
    showJudgeButtons.value = false;
    judgeRow.value = null;
  }
};

const handleSendAuditReport = async () => {
  // 优先用 judgeRow，否则用 currentReviewObjectId
  const rowId = judgeRow.value?.id || currentReviewObjectId.value;
  if (!rowId) {
    ElMessage.error('无法获取对象ID，无法发送审查报告');
    return;
  }
  const loading = ElLoading.service({
    fullscreen: true,
    text: '正在发送审查报告...',
    background: 'rgba(0, 0, 0, 0.7)'
  });
  try {
    await axios.post(`http://localhost:8082/api/objects/${rowId}/update-pro-report`);
    ElMessage.success('审查报告已成功发送给数源方');
    // 不自动关闭弹窗
  } catch (error) {
    ElMessage.error('发送报告失败: ' + (error.message || '未知错误'));
  } finally {
    loading.close();
  }
};

// 显示审查报告
const showReviewReport = async (row) => {
  try {
    currentReviewObjectId.value = row.id;
    
    const loading = ElLoading.service({
      fullscreen: true,
      text: '正在获取审查报告...',
      background: 'rgba(0, 0, 0, 0.7)'
    });
    
    // 从API获取对象信息，包含审计报告
    const response = await axios.get(`http://localhost:8082/api/objects/${row.id}`);
    loading.close();
    
    let auditReport = '';
    
    // 尝试从响应中提取审计报告
    if (response.data) {
      if (response.data.auditReport) {
        auditReport = response.data.auditReport;
      } else if (response.data.data && response.data.data.auditReport) {
        auditReport = response.data.data.auditReport;
      }
    }
    
    // 如果审计报告为空，设置默认信息
    if (!auditReport || auditReport.trim() === '') {
      ElMessage.info('该对象尚未生成审查报告，等待治理方审查');
      auditReport = '等待治理方审查';
    }
    
    // 将审计报告存储到ReportViewer组件可以访问的地方
    // 这里我们假设ReportViewer组件可以通过props接收报告内容
    localStorage.setItem('currentAuditReport', auditReport);
    
    // 显示报告对话框
    reportDialogVisible.value = true;
  } catch (error) {
    ElMessage.error(`获取审查报告失败: ${error.message || '未知错误'}`);
    console.error('获取审查报告失败:', error);
  }
};

// 新的状态更新方法，直接使用8082接口
const updateStatus = async (row, newStatus) => {
  if (newStatus === '审查中') {
    try {
      handleReview(row);
    } catch (error) {
      console.error('审查过程出错:', error);
      ElMessage.error(`审查失败: ${error.message || '未知错误'}`);
    }
    return;
  }

  // 处理已合格或待校验状态
  if (newStatus === '已合格' || newStatus === '待校验') {
    try {
      // 保留现有的反馈意见，不覆盖为空
      const existingFeedback = row.feedback || (row.dataContent ? extractFeedback(row.dataContent) : '') || '';
      const result = await updateStatusViaBothPorts(row.id, newStatus, existingFeedback);
      if (result) {
        ElMessage.success(`${row.entity} 已更新为"${newStatus}"状态`);
        loadDataFromBackend(); // 重新加载数据
      } else {
        ElMessage.warning(`${row.entity} 状态更新失败`);
      }
    } catch (error) {
      console.error('更新状态时出错:', error);
      ElMessage.error(`更新 ${row.entity} 状态失败: ${error.message || '未知错误'}`);
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
        return value.trim() !== '' || '反馈意见不能为空';
      }
    }).then(async ({ value }) => {
      try {
        const result = await updateStatusViaBothPorts(row.id, newStatus, value);
        if (result) {
          ElMessage.success(`${row.entity} 已更新为"不合格"状态`);
          loadDataFromBackend(); // 重新加载数据
        } else {
          ElMessage.warning(`${row.entity} 状态更新失败`);
        }
      } catch (error) {
        console.error('更新状态时出错:', error);
        ElMessage.error(`更新 ${row.entity} 状态失败: ${error.message || '未知错误'}`);
      }
    }).catch(() => {
      ElMessage.info('已取消状态更新');
    });
  }
}

// 同时调用8081和8082端口更新状态的方法
const updateStatusViaBothPorts = async (objectId, newStatus, feedback = '') => {
  const results = {
    port8081: false,
    port8082: false
  };

  // 并行调用两个端口
  const promises = [
    // 8081端口调用
    (async () => {
      try {
        const result = await dataObjectService.updateObjectStatusViaApi(objectId, newStatus, feedback, false);
        results.port8081 = result;
        return result;
      } catch (error) {
        console.error('8081端口更新失败:', error);
        results.port8081 = false;
        return false;
      }
    })(),
    
    // 8082端口调用
    (async () => {
              try {
          // 首先获取当前对象数据
          const getResponse = await axios.get(`http://localhost:8082/api/objects/${objectId}`);
          
          if (!getResponse.data) {
            console.error('8082端口获取对象数据失败');
            results.port8082 = false;
            return false;
          }

          // 处理8082端口的数据结构
          let currentObject = null;
          if (getResponse.data.data) {
            // 如果返回的是 {code: 1, data: {...}} 格式
            currentObject = getResponse.data.data;
          } else if (getResponse.data.id) {
            // 如果直接返回对象
            currentObject = getResponse.data;
          } else {
            console.error('8082端口返回的数据格式不正确:', getResponse.data);
            results.port8082 = false;
            return false;
          }
        // 构建更新数据 - 保持所有原有字段
        const updateData = {
          ...currentObject,
          status: newStatus,
          feedback: feedback,
          dataEntity: {
            ...(currentObject.dataEntity || {}),
            status: newStatus,
            feedback: feedback
          },
          // 确保保留传播控制相关字段
          propagationControl: currentObject.propagationControl,
          constraintSet: currentObject.constraintSet,
          locationInfo: currentObject.locationInfo,
          metadata: currentObject.metadata,
          metadataJson: currentObject.metadataJson,
          totalCategoryValue: currentObject.totalCategoryValue,
          totalGradeValue: currentObject.totalGradeValue,
          dataItems: currentObject.dataItems,
          dataContent: currentObject.dataContent
        };

        // 发送PUT请求更新状态
        const putResponse = await axios.put(
          `http://localhost:8082/api/objects/${objectId}`,
          updateData,
          {
            headers: {
              'Content-Type': 'application/json'
            }
          }
        );

        // 检查响应状态
        if (putResponse.status === 200 || putResponse.status === 204 ||
            (putResponse.data && (putResponse.data.code === 1 || putResponse.data.success === true))) {
          results.port8082 = true;
          return true;
        } else {
          console.error('8082端口状态更新响应异常:', putResponse);
          results.port8082 = false;
          return false;
        }
      } catch (error) {
        console.error('8082端口更新失败:', error);
        results.port8082 = false;
        return false;
      }
    })()
  ];

  try {
    // 等待所有请求完成
    await Promise.allSettled(promises);
    
    // 检查结果
    const successCount = Object.values(results).filter(result => result).length;
    const totalCount = Object.keys(results).length;
    
    // 如果至少有一个端口成功，就认为更新成功
    if (successCount > 0) {
      return true;
    } else {
      console.error('所有端口都更新失败');
      return false;
    }
  } catch (error) {
    console.error('并行更新过程中出错:', error);
    return false;
  }
}

// 添加刷新数据列表的函数
const refreshDataList = async () => {
  try {
    const response = await fetch('http://localhost:8082/api/objects', {
      method: 'GET',
      headers: {
        'Accept': 'application/json'
      }
    });

    if (!response.ok) {
      throw new Error(`请求失败: ${response.status}`);
    }

    const data = await response.json();
    if (data && data.code === 1 && data.data) {
      // 更新表格数据
      const updatedData = data.data.map(item => ({
        id: item.id,
        entity: item.dataEntity?.entity || '',
        locationInfo: formatLocationInfo(item.locationInfo),
        constraint: formatConstraints(item.constraintSet?.constraints),
        transferControl: formatTransferControl(item.propagationControl),
        auditInfo: '查看日志',
        status: item.dataEntity?.status || '',
        feedback: item.dataEntity?.feedback || '',
        totalCategoryValue: item.totalCategoryValue,
        totalGradeValue: item.totalGradeValue,
        metadata: item.dataEntity?.metadata,
        dataContent: item.dataContent,
        auditReport: item.auditReport || '' // 新增
      }));
      
      tableData.value = updatedData;
      ElMessage.success('数据列表已更新');
    } else {
      throw new Error('返回数据格式不符合预期');
    }
  } catch (error) {
    console.error('获取数据列表失败:', error);
    ElMessage.error(`获取数据列表失败: ${error.message}`);
  }
}

// 退出登录
const logout = async () => {
  try {
    // 显示加载提示
    const loadingInstance = ElLoading.service({
      fullscreen: true,
      text: '正在清除数据并退出...',
      background: 'rgba(0, 0, 0, 0.7)'
    });
    
    // 调用接口清除所有对象
    const response = await axios.delete('http://localhost:8082/api/objects');
    
    loadingInstance.close();
    
    // 处理响应结果
    if (response.data && (response.data.code === 1 || response.data.success === true)) {
      ElMessage.success(response.data.data || '成功清除所有数据对象');
    } else {
      ElMessage.warning('清除数据可能未完全成功，但仍将退出系统');
    }
  } catch (error) {
    console.error('清除数据失败:', error);
    ElMessage.error('清除数据失败，但仍将退出系统');
  } finally {
    // 无论成功失败，都执行退出登录
    localStorage.removeItem('role');
    router.push('/login');
  }
}

// 处理每页显示数量变化
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
}

// 格式化定位信息
const formatLocationInfo = (locationInfo) => {
  if (!locationInfo || !locationInfo.locations || !locationInfo.locations.length) {
    return ''
  }
  
  return locationInfo.locations.map(loc => 
    `(${loc.sheet || '默认'}, ${loc.startRow || '1'}-${loc.endRow || '*'}, ${loc.startColumn || 'A'}-${loc.endColumn || '*'})`
  ).join('; ')
}

const formatConstraints = (constraints) => {
  if (!constraints || !constraints.length) {
    return []
  }
  
  return constraints.map(c => [
    `格式约束: ${c.formatConstraint}`,
    `访问约束: ${c.accessConstraint}`,
    `路径约束: ${c.pathConstraint}`,
    `区域约束: ${c.regionConstraint}`,
    `共享约束: ${c.shareConstraint}`
  ]).flat()
}

const formatTransferControl = (control) => {
  if (!control || !control.selectedOperations) {
    return []
  }
  
  const operations = []
  if (control.canRead) operations.push('可读')
  if (control.canModify) operations.push('可修改')
  if (control.canShare) operations.push('可共享')
  if (control.canDelegate) operations.push('可委托')
  if (control.canDestroy) operations.push('可销毁')
  
  return operations
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


const showJudgeButtonsForPreview = ref(false)

// 处理预览对话框中的判断操作
const handlePreviewJudge = async (result) => {
  // 从tableData中找到对应的行数据
  const targetRow = tableData.value.find(row => row.id === previewForm.id)
  if (!targetRow) {
    ElMessage.error('无法找到对应的数据对象')
    return
  }
  
  if (result === 'pass') {
    await updateStatus(targetRow, '已合格')
  } else if (result === 'fail') {
    await updateStatus(targetRow, '不合格')
  }
  showJudgeButtonsForPreview.value = false
  previewDialogVisible.value = false
}

const excelBinaryData = ref(null)


const handleExcelDataLoaded = (data) => {
  console.log('Excel数据加载完成:', data)
}

const handleExcelError = (error) => {
  console.error('Excel加载错误:', error)
  ElMessage.error('加载Excel数据时出错: ' + error)
}

const excelTableData = ref([])
const isExcelLoading = ref(false)

const fetchExcelDataFromApi = async (objectId) => {
  if (!objectId) {
    ElMessage.warning('无法获取对象ID，无法显示Excel数据')
    return
  }
  
  isExcelLoading.value = true
  
  const apiUrl = 'http://localhost:8082/api/objects'
  
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
      
      // 检查dataEntity.dataItems
      if (targetObject.dataEntity && targetObject.dataEntity.dataItems && Array.isArray(targetObject.dataEntity.dataItems)) {
        dataItems = targetObject.dataEntity.dataItems
      } else if (targetObject.dataItems && Array.isArray(targetObject.dataItems)) {
        dataItems = targetObject.dataItems
      } else if (targetObject.dataContent) {
        try {
          const dataContent = typeof targetObject.dataContent === 'string' 
            ? JSON.parse(targetObject.dataContent) 
            : targetObject.dataContent
            
          if (dataContent && dataContent.dataItems && Array.isArray(dataContent.dataItems)) {
            dataItems = dataContent.dataItems
          } else if (dataContent && dataContent.dataEntity && dataContent.dataEntity.dataItems && Array.isArray(dataContent.dataEntity.dataItems)) {
            dataItems = dataContent.dataEntity.dataItems
          }
        } catch (e) {
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
      } else {
        dataItems = response.data.dataItems
      }
    }
    

    if (!dataItems || dataItems.length === 0) {
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

  // 直接从对象提取
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
  
  // 从dataEntity提取
  if (obj.dataEntity) {
    if (obj.dataEntity.totalCategoryValue !== undefined) {
      previewForm.totalCategoryValue = obj.dataEntity.totalCategoryValue
    } else if (obj.dataEntity.classificationValue !== undefined) {
      previewForm.classificationValue = obj.dataEntity.classificationValue
    }
    
    if (obj.dataEntity.totalGradeValue !== undefined) {
      previewForm.totalGradeValue = obj.dataEntity.totalGradeValue
    } else if (obj.dataEntity.levelValue !== undefined) {
      previewForm.levelValue = obj.dataEntity.levelValue
    }
  }
  
  // 从dataContent提取
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
      
      // 从dataContent.dataEntity提取
      if (dataContent.dataEntity) {
        if (dataContent.dataEntity.totalCategoryValue !== undefined) {
          previewForm.totalCategoryValue = dataContent.dataEntity.totalCategoryValue
        } else if (dataContent.dataEntity.classificationValue !== undefined) {
          previewForm.classificationValue = dataContent.dataEntity.classificationValue
        }
        
        if (dataContent.dataEntity.totalGradeValue !== undefined) {
          previewForm.totalGradeValue = dataContent.dataEntity.totalGradeValue
        } else if (dataContent.dataEntity.levelValue !== undefined) {
          previewForm.levelValue = dataContent.dataEntity.levelValue
        }
      }
    }
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

    excelBinaryData.value = blob

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
  previewForm.id = row.id
  previewForm.entity = row.entity
  previewForm.locationInfo = row.locationInfo
  previewForm.locationInfoJson = row.locationInfoJson // 新增
  previewForm.constraint = row.constraint
  previewForm.transferControl = row.transferControl
  previewForm.status = row.status
  
  // 保留原有的分类分级值，不要重置为空
  previewForm.totalCategoryValue = row.totalCategoryValue || ''
  previewForm.totalGradeValue = row.totalGradeValue || ''
  previewForm.classificationValue = row.classificationValue || ''
  previewForm.levelValue = row.levelValue || ''

  previewForm.metadata = extractMetadata(row)

  excelBinaryData.value = null
  excelTableData.value = []

  previewDialogVisible.value = true
  
  showJudgeButtonsForPreview.value = true

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
  

  if (typeof metadataString === 'object') {
    return {
      ...metadataString,
      contactPhone: metadataString.contactPhone || '未提供' // 确保contactPhone字段存在
    }
  }
  

  let cleanString = metadataString.toString()
  

  if (cleanString.startsWith('"') && cleanString.endsWith('"')) {
    cleanString = cleanString.slice(1, -1).replace(/\\"/g, '"')
  }
  
  if (cleanString.includes('\\\"') || cleanString.includes('\\\\')) {
    cleanString = cleanString.replace(/\\\\/g, '\\').replace(/\\"/g, '"')
  }
  
  try {

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

    if (typeof dataContent === 'string') {
      try {
        const parsed = JSON.parse(dataContent);
        if (parsed && parsed.feedback) {
          return parsed.feedback;
        }
      } catch (e) {

      }
      
      const match1 = dataContent.match(/"feedback"\s*:\s*"([^"]*)"/);
      if (match1 && match1[1]) {
        return match1[1];
      }

      const match2 = dataContent.match(/\\"feedback\\"\\s*:\\s*\\"([^\\"]*?)\\"/);
      if (match2 && match2[1]) {
        return match2[1];
      }

      if (dataContent.includes('数据格式错误')) {
        return '数据格式错误';
      }
    } 

    else if (typeof dataContent === 'object' && dataContent !== null) {

      if (dataContent.feedback) {
        return dataContent.feedback;
      }
      
      if (dataContent.data && dataContent.data.feedback) {
        return dataContent.data.feedback;
      }
    }
    

    return '';
  } catch (e) {
    return ''; 
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

// 审核报告弹窗
const reportDialogVisible = ref(false)
const currentReviewObjectId = ref('')
const showJudgeButtons = ref(false)
const judgeRow = ref(null)

// 显示审核报告
const showReportDialog = () => {
  reportDialogVisible.value = true
}

// 添加根据状态获取反馈意见类名的方法
const getFeedbackClass = (status) => {
  if (status === '待校验' || status === '待检验') return 'status-pending'
  if (status === '已合格') return 'status-success'
  if (status === '不合格') return 'status-error'
  return ''
}

const hasToken = ref(false)
const isRequestingToken = ref(false)
const isGeneratingCapsule = ref(false)

const auditLogVisible = ref(false)
const currentRow = ref(null)
const showAuditLogDialog = (row) => {
  currentRow.value = row
  auditLogVisible.value = true
}


const headerCellStyle = ({ column }) => {
  const blueProps = [
    'id',
    'entity',
    'locationInfo',
    'constraint',
    'transferControl',
    'auditInfo',
    'classificationLevelValue'
  ];
  const grayProps = ['status', 'feedback', 'operation'];
  if (blueProps.includes(column.property)) {
    return {
      background: '#eaf6ff',
      color: '#1677c7',
      fontWeight: 'bold',
      fontSize: '16px',
      textAlign: 'center',
      padding: '10px 0'
    };
  }
  if (grayProps.includes(column.property) || column.label === '操作') {
    return {
      background: '#e0e2e6',
      color: '#000000',
      fontWeight: 'bold',
      fontSize: '15px',
      textAlign: 'center',
      padding: '10px 0'
    };
  }
  // 其他列
  return {
    background: '#f5f7fa',
    color: '#606266',
    fontWeight: 'bold',
    fontSize: '15px',
    textAlign: 'center',
    padding: '10px 0'
  };
};

// 新增内容单元格样式方法
const cellStyle = ({ column }) => {
  const grayProps = ['status', 'feedback', 'operation'];
  if (grayProps.includes(column.property) || column.label === '操作') {
    return {
      background: '#fafafa'
    };
  }
  return {};
};

const applicationListVisible = ref(false)

// 解析 locationInfo 字段为对象
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

// 判断 selectFields 是否过长
function isSelectFieldsLong(selectFields) {
  if (!selectFields) return false;
  return selectFields.length > 30;
}

// 处理初始化系统方法
const handleInitSystem = async () => {
  try {
    const loadingInstance = ElLoading.service({
      fullscreen: true,
      text: '正在初始化系统...',
      background: 'rgba(0, 0, 0, 0.7)'
    });
    
    const response = await axios.post('http://localhost:8082/api/init-system');
    
    loadingInstance.close();
    
    if (response.data && (response.data.code === 1 || response.data.success === true)) {
      ElMessage.success('系统初始化成功');
      // 刷新数据
      loadDataFromBackend();
    } else {
      ElMessage.warning(`系统初始化失败: ${response.data?.message || response.data?.msg || '未知错误'}`);
    }
  } catch (error) {
    console.error('系统初始化失败:', error);
    
    if (error.response) {
      if (error.response.status === 404) {
        ElMessage.error('系统服务未启动或接口不存在');
      } else if (error.response.status === 500) {
        ElMessage.error(`系统服务错误: ${error.response.data?.message || '内部服务器错误'}`);
      } else {
        ElMessage.error(`初始化失败 (${error.response.status}): ${error.response.data?.message || error.message}`);
      }
    } else if (error.request) {
      ElMessage.error('无法连接到系统服务，请确保服务已启动');
    } else {
      ElMessage.error(`系统初始化失败: ${error.message || '未知错误'}`);
    }
  }
};

// 生成组织机构凭证三步操作
const handleGenerateOrgVouchers = async () => {
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
      // 第二步：生成组织机构凭证
      const loading2 = ElLoading.service({ fullscreen: true, text: '正在生成组织机构凭证...' })
      const res2 = await axios.post('http://localhost:8082/api/generate-org-vouchers')
      loading2.close()
      if (res2.data && res2.data.code === 0) {
        ElMessage.error('生成组织机构凭证失败：' + (res2.data?.msg || res2.data?.message || '未知错误'))
      } else if (res2.data && res2.data.code === 1) {
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

// 处理生成并发送数据胶囊给使用方
// 轮询相关函数
const startPollingForRequests = () => {
  if (pollingTimer.value) {
    clearInterval(pollingTimer.value)
  }
  
  pollingTimer.value = setInterval(async () => {
    try {
      const response = await axios.get('http://localhost:8082/api/last-capsule-request')
      
      if (response.data && response.data.code === 1 && response.data.data) {
        // 使用ids字段作为唯一标识符
        const currentRequestIds = response.data.data.ids
        
        // 检查是否已经处理过该ID组合，防止重复弹窗
        if (processedRequestIds.value.has(currentRequestIds)) {
          return // 已经处理过该ID组合，跳过弹窗但继续轮询
        }
        
        // 不停止轮询，继续监听新的请求
        // stopPollingForRequests() // 注释掉这行，保持轮询继续
        
        // 记录当前请求ID组合
        processedRequestIds.value.add(currentRequestIds)
        saveProcessedRequestIds() // 同步保存到localStorage
        
        // 获取实体名
        try {
          const objectsResponse = await axios.get('http://localhost:8082/api/objects')
          let entityNames = []
          
          let objects = []
          if (Array.isArray(objectsResponse.data)) {
            objects = objectsResponse.data
          } else if (objectsResponse.data.data && Array.isArray(objectsResponse.data.data)) {
            objects = objectsResponse.data.data
          } else if (objectsResponse.data.list && Array.isArray(objectsResponse.data.list)) {
            objects = objectsResponse.data.list
          }
          
          if (objects.length > 0) {
            const requestIds = response.data.data.ids
            
            // 处理多个ID的情况
            let idsArray = []
            if (Array.isArray(requestIds)) {
              idsArray = requestIds
            } else if (typeof requestIds === 'string' && requestIds.includes(',')) {
              idsArray = requestIds.split(',').map(id => id.trim())
            } else {
              idsArray = [requestIds]
            }
            
            // 为每个ID获取对应的实体名
             entityNames = idsArray.map(id => {
               const foundObject = objects.find(obj => obj.id === id)
               if (foundObject) {
                 // 根据治理方页面的数据结构获取实体名
                 let entityName = '未知实体'
                 if (foundObject.dataEntity && foundObject.dataEntity.entity) {
                   entityName = foundObject.dataEntity.entity
                 } else if (foundObject.entity) {
                   entityName = foundObject.entity
                 } else if (foundObject.name) {
                   entityName = foundObject.name
                 } else if (foundObject.dataContent) {
                   try {
                     const dataContent = typeof foundObject.dataContent === 'string' 
                       ? JSON.parse(foundObject.dataContent) 
                       : foundObject.dataContent
                     if (dataContent && dataContent.entity) {
                       entityName = dataContent.entity
                     } else if (dataContent && dataContent.dataEntity && dataContent.dataEntity.entity) {
                       entityName = dataContent.dataEntity.entity
                     }
                   } catch (e) {
                     console.warn('从dataContent解析实体名称失败:', e)
                   }
                 }
                 return entityName
               }
               return '未知实体'
             })
          }
          
          pendingRequest.value = {
            ...response.data.data,
            entityName: entityNames.length === 1 ? entityNames[0] : entityNames
          }
        } catch (error) {
          console.log('获取实体名失败:', error.message)
          pendingRequest.value = {
            ...response.data.data,
            entityName: '未知实体'
          }
        }
        
        requestNotificationVisible.value = true
      }
    } catch (error) {
      console.log('轮询请求失败:', error.message)
    }
  }, pollingInterval.value) 
}

const stopPollingForRequests = () => {
  if (pollingTimer.value) {
    clearInterval(pollingTimer.value)
    pollingTimer.value = null
  }
}

const handleRequestNotification = async (action) => {
  requestNotificationVisible.value = false
  
  if (action === 'generate') {
    // 自动触发生成并发送数据胶囊的逻辑
    await handleGenerateAndSendCapsule()
    // 生成后继续轮询，监听新的请求
    console.log('数据胶囊生成完成，继续轮询监听新请求')
    // 保持已处理的请求ID集合，不重置
  } else if (action === 'later') {
    // 稍后处理，从已处理集合中移除该ID，允许稍后重新提醒
    const currentIds = pendingRequest.value?.ids
    if (currentIds) {
      processedRequestIds.value.delete(currentIds)
      saveProcessedRequestIds() // 同步保存到localStorage
    }
    // 继续轮询
    console.log('选择稍后处理，继续轮询')
  }
}

const handleGenerateAndSendCapsule = async () => {
  const loading = ElLoading.service({ 
    fullscreen: true, 
    text: '正在生成并发送数据胶囊...' 
  });
  
  try {
    const response = await axios.post('http://localhost:8082/api/generate-and-send-capsule');
    
    loading.close();
    
    if (response.data && response.data.code === 0) {
      ElMessage.error(`操作失败：${response.data?.msg || response.data?.message || '未知错误'}`);
    } else if (response.data && response.data.code === 1) {
      ElMessage.success('数据胶囊已成功生成并发送给使用方');
    } else {
      ElMessage.error(`操作失败：${response.data?.msg || response.data?.message || '未知错误'}`);
    }
  } catch (error) {
    loading.close();
    
    console.error('生成并发送数据胶囊失败:', error);
    
    if (error.response) {
      if (error.response.status === 404) {
        ElMessage.error('服务未启动或接口不存在');
      } else if (error.response.status === 500) {
        ElMessage.error(`服务错误: ${error.response.data?.message || '内部服务器错误'}`);
      } else {
        ElMessage.error(`操作失败 (${error.response.status}): ${error.response.data?.message || error.message}`);
      }
    } else if (error.request) {
      ElMessage.error('无法连接到服务，请确保服务已启动');
    } else {
      ElMessage.error(`操作失败: ${error.message || '未知错误'}`);
    }
  }
};

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
  setWatermark('治  理  方')
  window.addEventListener('resize', () => setWatermark('治  理  方'))
  startPollingForRequests()
})
onBeforeUnmount(() => {
  removeWatermark()
  window.removeEventListener('resize', () => setWatermark('治  理  方'))
  stopPollingForRequests()
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
  background-color: #e0e2e6;
  color: #8b8e8f;
}

.feedback-text.status-pending {
  color: #b3b3b3;
  background-color: #e0e2e6;
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
  width: 700px !important;
  max-width: 90vw !important;
}

:deep(.feedback-dialog .el-message-box__input) {
  padding-top: 8px;
}

:deep(.feedback-dialog .el-textarea__inner) {
  min-height: 200px !important;
  font-size: 14px;
  resize: vertical;
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
  min-height: 0;
  min-width: 0;
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

.feedback-text:empty {
  display: none;
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

.status-to-generate {
  background-color: #fff7e6;
  color: #fa8c16;
  border: 1px solid #ffd591;
}
/* 请求通知弹窗样式 */
.request-notification-content {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

.notification-info {
  text-align: left;
  max-width: 400px;
}

.notification-info p {
  text-align: left;
  margin: 8px 0;
  font-size: 14px;
  line-height: 1.5;
}

.notification-info strong {
  color: #303133;
  font-weight: 600;
}

.dialog-footer {
  text-align: right;
}

.dialog-footer .el-button {
  margin-left: 10px;
}

/* 弹窗标题左对齐样式 */
.centered-dialog :deep(.el-dialog__header) {
  text-align: left;
}

.centered-dialog :deep(.el-dialog__title) {
  text-align: left;
}

/* 实体列换行样式 */
.entity-link {
  white-space: normal;
  word-wrap: break-word;
  word-break: break-all;
  line-height: 1.4;
  display: inline-block;
  max-width: 100%;
}
</style>