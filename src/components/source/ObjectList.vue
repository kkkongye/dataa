<template>
  <div class="object-list-container">
    <!-- 状态筛选按钮 -->
    <div class="status-filter">
      <el-button 
        :class="['status-btn', { active: currentStatus === '' }]" 
        @click="setStatus('')"
      >全部数据对象</el-button>
      <el-button 
        :class="['status-btn', { active: currentStatus === '待校验' }]" 
        @click="setStatus('待校验')"
      >待校验</el-button>
      <el-button 
        :class="['status-btn', { active: currentStatus === '已合格' }]" 
        @click="setStatus('已合格')"
      >已合格</el-button>
      <el-button 
        :class="['status-btn', { active: currentStatus === '不合格' }]" 
        @click="setStatus('不合格')"
      >不合格</el-button>
      <el-button 
        :class="['status-btn', { active: currentStatus === '待生成分类分级值' }]" 
        @click="setStatus('待生成分类分级值')"
      >待生成分类分级值</el-button>
    </div>
    
    <!-- 搜索和操作区 -->
    <div class="action-bar">
      <div class="search-area">
        <el-input
          v-model="searchValue"
          placeholder="搜索实体名、约束条件、传输控制操作"
          class="search-input"
          @input="handleSearchInput"
        >
          <template #suffix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>
      <div class="action-buttons">
        <!-- <el-button type="primary" plain @click="$emit('visualization')" class="visualization-btn">
          <el-icon><DataAnalysis /></el-icon>
          三维数据可视化
        </el-button> -->
        <el-button type="primary" plain @click="$emit('create')">新建数据对象</el-button>
        <el-button type="primary" plain @click="handlePushToGovernance">发送数字对象至治理方</el-button>
        <!-- <el-button type="primary" plain @click="handleGenerateDV">生成数据凭证</el-button> -->
        <el-button type="info" plain  @click="$emit('show-application-list')" style="margin-left: 8px;">申请列表</el-button>

      </div>
    </div>
    
    <!-- 数据表格 -->
    <div class="table-container">
      <el-table
        :data="tableData"
        style="width: 100%"
        @selection-change="handleSelectionChange"
        @sort-change="handleSortChange"
        :cell-style="cellStyle"
        :header-cell-style="headerCellStyle"
        border
        height="100%"
        fit
        empty-text="暂无数据"
      >
        <el-table-column 
          prop="id" 
          label="ID" 
          width="240" 
          align="center"
          sortable
        >
          <template #default="scope">
            <div class="id-cell highlight-blue">{{ scope.row.id }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="entity" label="实体" width="120" align="center">
          <template #default="scope">
            <el-link type="primary" @click="handlePreview(scope.row)">{{ scope.row.entity }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="locationInfo" label="定位信息" min-width="140" align="center">
          <template #default="scope">
            <!-- 增加调试输出 -->
            <!-- <span style="display: none;">{{ console.log('渲染定位信息:', scope.row.id, scope.row.locationInfo) }}</span> -->
            <span v-if="scope.row.locationInfo">
              <!-- 对象格式的locationInfo -->
              <template v-if="typeof scope.row.locationInfo === 'object' && scope.row.locationInfo !== null">
                ({{ scope.row.locationInfo.databaseName || scope.row.locationInfo.database || '-' }},
                {{ scope.row.locationInfo.tableName || scope.row.locationInfo.table || '-' }},
                <el-popover placement="top" trigger="click">
                  <template #reference>
                    <span class="select-fields-link" style="color:#409EFF;cursor:pointer;">"select字段"</span>
                  </template>
                  <div style="max-width:400px;word-break:break-all;">{{ scope.row.locationInfo.selectFields || scope.row.locationInfo.fields || '-' }}</div>
                </el-popover>
                )
              </template>
              <!-- 字符串格式的locationInfo，且已经格式化 -->
              <template v-else-if="typeof scope.row.locationInfo === 'string' && scope.row.locationInfo.startsWith('(')">
                {{ scope.row.locationInfo }}
              </template>
              <!-- 其他情况，尝试使用解析函数 -->
              <template v-else>
                <span v-if="getLocationInfoObj(scope.row.locationInfo, scope.row.locationInfoJson)">
                  ({{ getLocationInfoObj(scope.row.locationInfo, scope.row.locationInfoJson).databaseName || '-' }},
                  {{ getLocationInfoObj(scope.row.locationInfo, scope.row.locationInfoJson).tableName || '-' }},
                  <el-popover placement="top" trigger="click">
                    <template #reference>
                      <span class="select-fields-link" style="color:#409EFF;cursor:pointer;">"select字段"</span>
                    </template>
                    <div style="max-width:400px;word-break:break-all;">{{ getLocationInfoObj(scope.row.locationInfo, scope.row.locationInfoJson).selectFields || '-' }}</div>
                  </el-popover>
                  )
                </span>
                <span v-else>-</span>
              </template>
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
              <template v-else>-</template>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="transferControl" label="传输控制操作" min-width="250" align="center">
          <template #default="scope">
            <div class="control-container">
              <!-- 优先使用transferControl数组 -->
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
              <!-- 如果没有transferControl，尝试使用propagationControl对象 -->
              <template v-else-if="scope.row.propagationControl">
                <el-tag
                  v-if="scope.row.propagationControl.canRead"
                  size="small"
                  type="primary"
                  effect="plain"
                  class="control-tag"
                >
                  可读
                </el-tag>
                <el-tag
                  v-if="scope.row.propagationControl.canModify"
                  size="small"
                  type="primary"
                  effect="plain"
                  class="control-tag"
                >
                  可修改
                </el-tag>
                <el-tag
                  v-if="scope.row.propagationControl.canShare"
                  size="small"
                  type="primary"
                  effect="plain"
                  class="control-tag"
                >
                  可共享
                </el-tag>
                <el-tag
                  v-if="scope.row.propagationControl.canDelegate"
                  size="small"
                  type="primary"
                  effect="plain"
                  class="control-tag"
                >
                  可委托
                </el-tag>
                <el-tag
                  v-if="scope.row.propagationControl.canDestroy"
                  size="small"
                  type="primary"
                  effect="plain"
                  class="control-tag"
                >
                  可销毁
                </el-tag>
              </template>
              <template v-else>-</template>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="auditInfo" label="审计控制信息" width="140" align="center">
          <template #default="scope">
            <el-link type="primary" @click="showAuditLogDialog(scope.row)">查看日志</el-link>
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
              <el-button
                type="primary"
                size="small"
                class="generate-btn"
                @click.stop="generateClassificationLevel(scope.row)"
                :disabled="isClassificationGenerated(scope.row)"
              >
                {{ isClassificationGenerated(scope.row) ? '已生成分类分级值' : '生成分类分级值' }}
              </el-button>
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
        <el-table-column prop="feedback" label="反馈意见" min-width="150" align="center">
          <template #default="scope">
            <div style="display: flex; flex-direction: column; align-items: center;">
              <span v-if="scope.row.feedback" :class="['feedback-text', getFeedbackClass(scope.row.status)]" style="margin-bottom: 10px;">
              {{ scope.row.feedback }}
            </span>
              <span v-else-if="scope.row.dataContent" :class="['feedback-text', getFeedbackClass(scope.row.status)]" style="margin-bottom: 10px;">
              {{ extractFeedback(scope.row.dataContent) }}
            </span>
              <span v-else style="margin-bottom: 10px;"></span>
              <el-button v-if="hasReportContent(scope.row)" link type="info" size="small" style="margin-top: 0;" @click="handleViewReport(scope.row)">查看审查报告</el-button>
              <!-- <el-button v-if="scope.row.auditReport && scope.row.status !== '已合格'" link type="info" size="small" style="margin-top: 0;" @click="handleViewReport(scope.row)">查看审查报告</el-button> -->
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="operation" label="操作" width="150" align="center">
          <template #default="scope">
            <el-button link type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    
    <!-- 分页 -->
    <div class="pagination-area">
      <CommonPagination
        v-model:current-page="currentPageValue"
        v-model:page-size="pageSizeValue"
        :total-count="totalCount"
        :page-sizes="[5, 10, 20, 30, 50]"
        background
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
    
    <!-- 分类分级值对话框 -->
    <ClassificationLevelDialog
      v-model:visible="classificationLevelDialogVisible"
      v-model:modelValue="classificationLevelData"
      :objectId="currentRow?.id || ''"
      :debug="true"
      :apiBaseUrl="'http://localhost:8081'"
      @confirm="handleClassificationLevelConfirm"
    />

    <AuditLogDialog 
      :visible="auditLogVisible" 
      :object-id="currentRow?.id"
      @close="auditLogVisible = false" 
    />
  </div>
</template>

<script setup>
import { ref, computed, watch, defineEmits, defineProps, onMounted } from 'vue'
import { Search, InfoFilled, DataAnalysis } from '@element-plus/icons-vue'
import CommonPagination from '@/components/CommonPagination.vue'
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus'
import ClassificationLevelDialog from './ClassificationLevelDialog.vue'
import AuditLogDialog from './AuditLogDialog.vue'
import axios from 'axios'

const props = defineProps({
  // 表格数据
  data: {
    type: Array,
    default: () => []
  },
  currentStatus: {
    type: String,
    default: ''
  },
  // 搜索关键词
  searchKeyword: {
    type: String,
    default: ''
  },
  currentPage: {
    type: Number,
    default: 1
  },
  pageSize: {
    type: Number,
    default: 5
  },
  totalCount: {
    type: Number,
    default: 0
  },
  isQualifiedStatus: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits([
  'update:current-status',
  'update:search-keyword',
  'update:current-page',
  'update:page-size',
  'update:data',
  'selection-change',
  'sort-change',
  'edit',
  'delete',
  'preview',
  'create',
  'export',
  'visualization',
  'show-application-list',
  'view-report'
])

// 内部状态
const searchValue = ref(props.searchKeyword)
const currentPageValue = ref(props.currentPage)
const pageSizeValue = ref(props.pageSize || 5)
const selectedRows = ref([])

const classificationLevelDialogVisible = ref(false)
const currentRow = ref(null)
const classificationLevelData = ref({
  classificationValue: '',
  levelValue: '',
  dbGrade: 100,
  tableGrade: 10,
  rowGrades: [0.3],
  columnGrades: [0.3],
  rowGradeValue: 0.3,
  columnGradeValue: 0.3,
  totalGradeValue: 0
})

const auditLogVisible = ref(false)
const reportViewerVisible = ref(false)
const currentReportObjectId = ref('')

// 判断审查报告内容是否为空
const hasReportContent = (row) => {
  // 检查auditReport字段是否存在且不为空
  if (row.auditReport && row.auditReport.trim() !== '') {
    return true
  }
  
  // 检查其他可能存储报告的字段
  if (row.reviewReport && row.reviewReport.trim() !== '') {
    return true
  }
  
  // 如果都没有内容，返回false
  return false
}

// 处理查看审查报告
const handleViewReport = (row) => {
  currentReportObjectId.value = row.id
  emit('view-report', row)
}

const tableData = computed(() => {
  if (!props.data) return []
  let filtered = props.data
  if (props.currentStatus === '待校验') {
    filtered = filtered.filter(item => item.status === '待校验' || item.status === '待检验')
  } else if (props.currentStatus === '待生成分类分级值') {
    filtered = filtered.filter(item => item.status === '待生成分类分级值')
  } else if (props.currentStatus) {
    filtered = filtered.filter(item => item.status === props.currentStatus)
  }
  const startIndex = (props.currentPage - 1) * props.pageSize
  const endIndex = startIndex + props.pageSize
  return filtered.slice(startIndex, Math.min(endIndex, filtered.length))
})

watch(() => props.searchKeyword, (newVal) => {
  searchValue.value = newVal
})

watch(() => props.currentPage, (newVal) => {
  currentPageValue.value = newVal
})

watch(() => props.pageSize, (newVal) => {
  pageSizeValue.value = newVal
})

watch(searchValue, (newVal) => {
  emit('update:search-keyword', newVal)
})

watch(currentPageValue, (newVal) => {
  emit('update:current-page', newVal)
})

watch(pageSizeValue, (newVal) => {
  emit('update:page-size', newVal)
})

const setStatus = (status) => {
  emit('update:current-status', status)
}

const handleSearchInput = (value) => {
  emit('update:search-keyword', value)
}

const handleSelectionChange = (rows) => {
  selectedRows.value = rows
  emit('selection-change', rows)
}

const handleEdit = (row) => {
  emit('edit', row)
}

const handleDelete = (row) => {
  emit('delete', row)
}

const handlePreview = (row) => {
  emit('preview', row)
}

const handleSizeChange = (size) => {
  pageSizeValue.value = size
  emit('update:page-size', size)
}

const handleCurrentChange = (page) => {
  currentPageValue.value = page
  emit('update:current-page', page)
}

const handleSortChange = (column) => {
  emit('sort-change', column)
}

const getStatusClass = (status) => {
  if (status === '待校验' || status === '待检验') return 'status-pending'
  if (status === '已合格') return 'status-success'
  if (status === '不合格') return 'status-error'
  if (status === '待生成分类分级值') return 'status-to-generate'
  return ''
}

const formatConstraintText = (text) => {
  if (!text) return text
  
  if (text.includes(':')) {
    const parts = text.split(':')
    return `<span class="constraint-prefix">${parts[0]}:</span>${parts[1]}`
  }
  
  return text
}

const parseMetadataJson = (jsonString) => {
  try {
    if (!jsonString) {
      return {}
    }
    
    let metadata = {}
    
    if (typeof jsonString === 'string') {
      try {
        metadata = JSON.parse(jsonString)
      } catch (parseError) {
        let processedString = jsonString
        
        if (jsonString.includes('\\')) {
          try {

            processedString = jsonString.replace(/\\"/g, '"')
            metadata = JSON.parse(processedString)
          } catch (error) {
            try {

              if (jsonString.startsWith('"') && jsonString.endsWith('"')) {
                processedString = jsonString.substring(1, jsonString.length - 1).replace(/\\"/g, '"')
                metadata = JSON.parse(processedString)
              }
            } catch (error2) {
              try {
   
                processedString = jsonString.replace(/\\\\/g, '\\')
                metadata = JSON.parse(processedString)
              } catch (error3) {

              }
            }
          }
        }
        

        if (Object.keys(metadata).length === 0) {

          const patterns = [
            /resourceSummary[\\]*"*:[\\]*"*([^"\\,}]+)/,
            /resourceSummary=([^,}]+)/,
            /resourceSummary[\\]*":([^",}]+)/
          ]
          
          const fieldPatterns = [
            /fieldClassification[\\]*"*:[\\]*"*([^"\\,}]+)/,
            /fieldClassification=([^,}]+)/,
            /fieldClassification[\\]*":([^",}]+)/
          ]
          
          for (const pattern of patterns) {
            const match = jsonString.match(pattern)
            if (match && match[1]) {
              metadata.resourceSummary = match[1].trim()
              break
            }
          }
          
          for (const pattern of fieldPatterns) {
            const match = jsonString.match(pattern)
            if (match && match[1]) {
              metadata.fieldClassification = match[1].trim()
              break
            }
          }
        }
      }
    } else if (typeof jsonString === 'object') {
      metadata = jsonString
    }
    
    const result = {
      dataName: metadata.dataName || '',
      sourceUnit: metadata.sourceUnit || '',
      contactPerson: metadata.contactPerson || '',
      contactPhone: metadata.contactPhone || '',
      resourceSummary: metadata.resourceSummary || '',
      fieldClassification: metadata.fieldClassification || ''
    }
    
    return result
  } catch (error) {
    console.error('解析元数据JSON失败:', error)
    return {}
  }
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
    return '提取失败';
  }
}

// 生成分类分级值
const generateClassificationLevel = (row) => {
  try {
    currentRow.value = row;
    classificationLevelData.value = {
      classificationValue: row.classificationValue || row.totalCategoryValue || '',
      industryCategory: row.industryCategory || '',
      dataTimeliness: row.dataTimeliness || '',
      dataSource: row.dataSource || '',
      levelValue: row.levelValue || row.totalGradeValue || '',
      dbGrade: row.dbGrade !== undefined ? row.dbGrade : 0,
      tableGrade: row.tableGrade !== undefined ? row.tableGrade : 0,
      rowGrades: row.rowGrades || [0, 0],
      columnGrades: row.columnGrades || [0, 0]
    };

    classificationLevelDialogVisible.value = true;
  } catch (error) {
    ElMessage.error('生成分类分级值时出错');
  }
};

// 处理分类分级确认
const handleClassificationLevelConfirm = async (data) => {
  try {
    if (currentRow.value) {
      currentRow.value.classificationValue = data.classificationValue;
      currentRow.value.totalCategoryValue = data.classificationValue;
      
      currentRow.value.industryCategory = data.industryCategory;
      currentRow.value.dataTimeliness = data.dataTimeliness;
      currentRow.value.dataSource = data.dataSource;
      
      currentRow.value.levelValue = data.levelValue;
      currentRow.value.totalGradeValue = data.levelValue;

      currentRow.value.dbGrade = data.dbGrade;
      currentRow.value.tableGrade = data.tableGrade;
      currentRow.value.rowGrades = data.rowGrades;
      currentRow.value.columnGrades = data.columnGrades;
      
      try {
        const savedValues = JSON.parse(localStorage.getItem('classificationLevelValues') || '{}');
        savedValues[currentRow.value.id] = {
          ...data,
          timestamp: new Date().toISOString()
        };
        localStorage.setItem('classificationLevelValues', JSON.stringify(savedValues));
      } catch (localStorageError) {
      }
      
      const categoryData = {
        industryCategory: data.industryCategory || '',
        processingTimeCategory: data.dataTimeliness || '', 
        dataSourceCategory: data.dataSource || ''
      };
      

      try {
        const objectId = currentRow.value.id;
        const categoriesResponse = await fetch(`http://localhost:8081/api/objects/${objectId}/categories`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(categoryData)
        });
        
        if (!categoriesResponse.ok) {
          console.warn(`分类数据提交状态: ${categoriesResponse.status} ${categoriesResponse.statusText}`);
        } else {
          console.log('分类数据提交成功:', await categoriesResponse.text());
        }
      } catch (apiError) {
        console.error('分类数据API提交失败:', apiError);

      }
      
      const index = props.data.findIndex(item => item.id === currentRow.value.id);
      if (index !== -1) {
        const updatedItem = { ...props.data[index], ...currentRow.value };
        props.data[index] = updatedItem;
        console.log('已更新数据源中的数据项:', updatedItem);
      }
      
      ElMessage.success('分类分级值已更新');
      
      emit('update:data', [...props.data]);
    } else {
      ElMessage.error('更新分类分级值失败：当前行数据为空');
    }
    
    classificationLevelDialogVisible.value = false;
  } catch (error) {
    console.error('更新分类分级值时出错:', error);
    ElMessage.error(`更新分类分级值失败：${error.message}`);
  }
};

const getFeedbackClass = (status) => {
  if (status === '待校验' || status === '待检验') return 'feedback-pending'
  if (status === '已合格') return 'feedback-success'
  if (status === '不合格') return 'feedback-error'
  return ''
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
      color: '#444',
      fontWeight: 'bold',
      fontSize: '15px',
      textAlign: 'center',
      padding: '10px 0'
    };
  }
  return {
    background: '#f5f7fa',
    color: '#606266',
    fontWeight: 'bold',
    fontSize: '15px',
    textAlign: 'center',
    padding: '10px 0'
  };
};

const cellStyle = ({ column }) => {
  const grayProps = ['status', 'feedback', 'operation'];
  if (grayProps.includes(column.property) || column.label === 'operation') {
    return {
      background: '#fafafa' 
    };
  }
  return {};
};

const showAuditLogDialog = (row) => {
  currentRow.value = row
  auditLogVisible.value = true
}

function getLocationInfoObj(locationInfo, locationInfoJson) {
  if (!locationInfo) {
    if (!locationInfoJson) {
      return {
        databaseName: '-',
        tableName: '-',
        selectFields: '-'
      };
    }
  }
  if (typeof locationInfo === 'string' && locationInfo.startsWith('(') && locationInfo.endsWith(')')) {
    return null;
  }
  if (typeof locationInfo === 'object' && locationInfo !== null) {
    if (locationInfo.databaseName !== undefined || 
        locationInfo.tableName !== undefined || 
        locationInfo.selectFields !== undefined) {
      console.log('返回直接对象:', locationInfo);
      return locationInfo;
    }
    
    // 检查data属性中的locationInfo
    if (locationInfo.data && typeof locationInfo.data === 'object') {
      // 可能在data.locationInfo中
      if (locationInfo.data.locationInfo) {
        if (typeof locationInfo.data.locationInfo === 'object') {
          console.log('返回data.locationInfo对象:', locationInfo.data.locationInfo);
          return locationInfo.data.locationInfo;
        } else if (typeof locationInfo.data.locationInfo === 'string') {
          try {
            const parsed = JSON.parse(locationInfo.data.locationInfo);
            if (parsed && typeof parsed === 'object') {
              return parsed;
            }
          } catch (e) {
          }
        }
      }
      
      // 检查data本身是否包含需要的字段
      const dataObj = locationInfo.data;
      if (dataObj.databaseName !== undefined || dataObj.tableName !== undefined || 
          dataObj.selectFields !== undefined || dataObj.database !== undefined || 
          dataObj.table !== undefined || dataObj.fields !== undefined) {
        const result = {
          databaseName: dataObj.databaseName || dataObj.database || '-',
          tableName: dataObj.tableName || dataObj.table || '-',
          selectFields: dataObj.selectFields || dataObj.fields || '-'
        };
        console.log('从data提取字段:', result);
        return result;
      }
    }
  }

  // 2. 尝试从字符串解析JSON
  if (typeof locationInfo === 'string' && !locationInfo.startsWith('(')) {
    try {
      const parsed = JSON.parse(locationInfo);
      if (parsed && typeof parsed === 'object') {
        console.log('从字符串解析JSON:', parsed);
        return parsed;
      }
    } catch (e) {
      // 解析失败，继续尝试其他方式
    }
  }
  
  // 3. 尝试从locationInfoJson解析
  if (locationInfoJson) {
    if (typeof locationInfoJson === 'object' && locationInfoJson !== null) {
      console.log('返回locationInfoJson对象:', locationInfoJson);
      return locationInfoJson;
    }
    
    if (typeof locationInfoJson === 'string') {
      try {
        const parsed = JSON.parse(locationInfoJson);
        if (parsed && typeof parsed === 'object') {
          console.log('返回解析的locationInfoJson字符串:', parsed);
          return parsed;
        }
      } catch (e) {
        // 解析失败，继续尝试其他方式
      }
    }
  }
  
  // 4. 嵌套的locationInfo
  if (typeof locationInfo === 'object' && locationInfo !== null && locationInfo.locationInfo) {
    if (typeof locationInfo.locationInfo === 'object') {
      console.log('返回嵌套locationInfo对象:', locationInfo.locationInfo);
      return locationInfo.locationInfo;
    } else if (typeof locationInfo.locationInfo === 'string') {
      if (!locationInfo.locationInfo.startsWith('(')) {
        try {
          const parsed = JSON.parse(locationInfo.locationInfo);
          if (parsed && typeof parsed === 'object') {
            console.log('返回解析的嵌套locationInfo字符串:', parsed);
            return parsed;
          }
        } catch (e) {
          // 解析失败，继续尝试其他方式
        }
      }
    }
  }
  
  // 5. 尝试从各种可能的位置查找字段
  if (typeof locationInfo === 'object' && locationInfo !== null) {
    const result = {
      databaseName: '-',
      tableName: '-',
      selectFields: '-'
    };
    
    // 检查各种可能的字段位置
    if (locationInfo.databaseName !== undefined) result.databaseName = locationInfo.databaseName;
    else if (locationInfo.database !== undefined) result.databaseName = locationInfo.database;
    else if (locationInfo.dbName !== undefined) result.databaseName = locationInfo.dbName;
    
    if (locationInfo.tableName !== undefined) result.tableName = locationInfo.tableName;
    else if (locationInfo.table !== undefined) result.tableName = locationInfo.table;
    else if (locationInfo.tblName !== undefined) result.tableName = locationInfo.tblName;
    
    if (locationInfo.selectFields !== undefined) result.selectFields = locationInfo.selectFields;
    else if (locationInfo.fields !== undefined) result.selectFields = locationInfo.fields;
    else if (locationInfo.columns !== undefined) result.selectFields = locationInfo.columns;
    else if (locationInfo.select !== undefined) result.selectFields = locationInfo.select;
    
    // 如果至少有一个字段不是默认值，则认为找到了有效数据
    if (result.databaseName !== '-' || result.tableName !== '-' || result.selectFields !== '-') {
      console.log('返回组装的对象:', result);
      return result;
    }
  }
  
  console.log('所有方法都失败，返回默认对象');
  return {
    databaseName: '-',
    tableName: '-',
    selectFields: '-'
  };
}

function isSelectFieldsLong(selectFields) {
  return typeof selectFields === 'string' && selectFields.length > 30
}

function isClassificationGenerated(row) {
  const isEmpty = v =>
    v === undefined ||
    v === null ||
    v === '' ||
    v === 0 ||
    v === '0' ||
    v === '未分类' ||
    v === '未分级';
  const cat = row.totalCategoryValue;
  const grade = row.totalGradeValue;
  return !isEmpty(cat) || !isEmpty(grade);
}

// 添加发送数字对象到治理方的方法
const handlePushToGovernance = () => {
  ElMessageBox.confirm('确定要将所有数字对象发送到治理方吗?', '确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      const loadingInstance = ElLoading.service({
        fullscreen: true,
        text: '正在发送数据到治理方...',
        background: 'rgba(0, 0, 0, 0.7)'
      });
      
      const response = await axios.post('http://localhost:8081/api/push-objects-to-governance');
      
      loadingInstance.close();
      
      if (response.data && (response.data.code === 1 || response.data.success === true)) {
        const objectCount = response.data.data ? response.data.data.length || 0 : '所有';
        ElMessage.success(`成功发送到治理方`);
      } else {
        ElMessage.warning(`发送失败: ${response.data?.message || response.data?.msg || '未知错误'}`);
      }
    } catch (error) {
      console.error('发送数据到治理方失败:', error);
      
      // 更详细的错误信息
      if (error.response) {
        if (error.response.status === 404) {
          ElMessage.error('治理方服务未启动或接口不存在');
        } else if (error.response.status === 500) {
          ElMessage.error(`治理方服务错误: ${error.response.data?.message || '内部服务器错误'}`);
        } else {
          ElMessage.error(`发送失败 (${error.response.status}): ${error.response.data?.message || error.message}`);
        }
      } else if (error.request) {
        ElMessage.error('无法连接到治理方服务，请确保服务已启动');
      } else {
        ElMessage.error(`发送数据到治理方失败: ${error.message || '未知错误'}`);
      }
    }
  }).catch(() => {
    // 用户取消操作
    ElMessage.info('已取消发送操作');
  });
};

// 添加生成数据凭证方法
const handleGenerateDV = async () => {
  try {
    // 显示加载状态
    const loadingInstance = ElLoading.service({
      fullscreen: true,
      text: '正在生成数据凭证...',
      background: 'rgba(0, 0, 0, 0.7)'
    });
    
    // 调用生成数据凭证接口
    const response = await axios.post('http://localhost:8081/api/generate-dv');
    
    loadingInstance.close();
    
    if (response.data && response.data.code === 0 && response.data.msg && response.data.msg.includes("生成DV失败")) {
      // 生成DV失败的情况，直接显示错误信息，不弹出加密确认框
      ElMessage.error(response.data.msg || '生成DV失败');
    } else if (response.data && (response.data.code === 1 || response.data.success === true)) {
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
};
</script>

<style scoped>
.object-list-container {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
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
  flex: 1;
}

/* 状态标签样式 */
.status-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
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
  background-color: #e0e2e6;
  color: #8b8e8f;
}

/* 待生成分类分级值状态样式 */
.status-to-generate {
  background-color: #fff7e6;
  color: #fa8c16;
  border: 1px solid #ffd591;
}

/* 分页区域 */
.pagination-area {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
}

.total-text {
  font-size: 14px;
  color: #8c8c8c;
}

/* 纯文本样式 */
.plain-text-container {
  color: #333;
  text-align: center;
  line-height: 1.5;
  padding: 2px 0;
}

/* 约束条件列样式 */
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

/* 反馈意见样式 */
.feedback-text {
  font-weight: 500;
  font-size: 13px;
  display: inline-block;
  padding: 2px 6px;
  border-radius: 4px;
}

/* 反馈状态样式类 */
.feedback-text.feedback-success {
  color: #67c23a;
  background-color: #f0f9eb;
}

.feedback-text.feedback-error {
  color: #f56c6c;
  background-color: #fef0f0;
}

.feedback-text.feedback-pending {
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

.generate-btn {
  margin-top: 5px;
  font-size: 12px;
}

.visualization-btn {
  display: flex;
  align-items: center;
  gap: 5px;
}

.visualization-btn .el-icon {
  font-size: 16px;
}

.select-fields-link {
  text-decoration: underline;
}
</style>