<template>
  <div class="object-list-container">
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
        <el-button type="primary" plain @click="$emit('visualization')" class="visualization-btn">
          <el-icon><DataAnalysis /></el-icon>
          三维数据可视化
        </el-button>
        <el-button type="primary" plain @click="$emit('export')">导出检验</el-button>
        <el-button type="primary" @click="$emit('create')">新建数字对象</el-button>
      </div>
    </div>
    
    <!-- 数据表格 -->
    <div class="table-container">
      <el-table
        :data="tableData"
        style="width: 100%"
        @selection-change="handleSelectionChange"
        @sort-change="handleSortChange"
        :cell-style="{ padding: '8px 4px', textAlign: 'center' }"
        :header-cell-style="{ padding: '10px 0', background: '#f5f7fa', color: '#606266', fontWeight: 'bold', textAlign: 'center' }"
        border
        height="100%"
        fit
      >
        <el-table-column 
          type="selection" 
          width="50" 
          align="center"
          :selectable="(row) => row.status !== '已合格'"
        />
        <el-table-column 
          prop="id" 
          label="ID" 
          width="240" 
          align="center"
          sortable
        >
          <template #default="scope">
            <div class="id-cell">{{ scope.row.id }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="entity" label="实体" width="120" align="center">
          <template #default="scope">
            <el-link type="primary" @click="handlePreview(scope.row)">{{ scope.row.entity }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="locationInfo" label="定位信息" min-width="120" align="center" />
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
              <el-button type="primary" size="small" class="generate-btn" @click.stop="generateClassificationLevel(scope.row)">生成分类分级值</el-button>
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
        <el-table-column prop="feedback" label="反馈意见" min-width="150" align="center">
          <template #default="scope">
            <!-- 优先使用row.feedback -->
            <span v-if="scope.row.feedback" class="feedback-text">
              {{ scope.row.feedback }}
            </span>
            
            <!-- 其次尝试从dataContent中提取 -->
            <span v-else-if="scope.row.dataContent" class="feedback-text">
              {{ extractFeedback(scope.row.dataContent) }}
            </span>
            
            <!-- 如果是客户反馈实体且状态为不合格，强制显示 -->
            <span v-else-if="scope.row.entity === '客户反馈' && scope.row.status === '不合格'" class="feedback-text">
              数据格式错误
            </span>
            
            <!-- 没有反馈信息 -->
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center">
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
      :apiBaseUrl="'http://localhost:8080'"
      @confirm="handleClassificationLevelConfirm"
    />
  </div>
</template>

<script setup>
import { ref, computed, watch, defineEmits, defineProps, onMounted } from 'vue'
import { Search, InfoFilled, DataAnalysis } from '@element-plus/icons-vue'
import CommonPagination from '@/components/CommonPagination.vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import ClassificationLevelDialog from './ClassificationLevelDialog.vue'

const props = defineProps({
  // 表格数据
  data: {
    type: Array,
    default: () => []
  },
  // 当前状态筛选
  currentStatus: {
    type: String,
    default: ''
  },
  // 搜索关键词
  searchKeyword: {
    type: String,
    default: ''
  },
  // 当前页
  currentPage: {
    type: Number,
    default: 1
  },
  // 每页大小
  pageSize: {
    type: Number,
    default: 5
  },
  // 总数据量
  totalCount: {
    type: Number,
    default: 0
  },
  // 是否为已合格状态
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
  'visualization'
])

// 内部状态
const searchValue = ref(props.searchKeyword)
const currentPageValue = ref(props.currentPage)
const pageSizeValue = ref(props.pageSize || 5)
const selectedRows = ref([])

// 分类分级值对话框可见性
const classificationLevelDialogVisible = ref(false)
// 当前操作的行数据
const currentRow = ref(null)
// 分类分级值数据
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

// 组件内计算属性和方法
const tableData = computed(() => {
  if (!props.data) return []
  
  // 在组件内部进行分页
  const startIndex = (props.currentPage - 1) * props.pageSize
  const endIndex = startIndex + props.pageSize
  
  // 确保不超出数组范围
  return props.data.slice(startIndex, Math.min(endIndex, props.data.length))
})

// 监听props变化
watch(() => props.searchKeyword, (newVal) => {
  searchValue.value = newVal
})

watch(() => props.currentPage, (newVal) => {
  currentPageValue.value = newVal
})

watch(() => props.pageSize, (newVal) => {
  pageSizeValue.value = newVal
})

// 监听内部状态变化，向外发出事件
watch(searchValue, (newVal) => {
  emit('update:search-keyword', newVal)
})

watch(currentPageValue, (newVal) => {
  emit('update:current-page', newVal)
})

watch(pageSizeValue, (newVal) => {
  emit('update:page-size', newVal)
})

// 处理状态筛选
const setStatus = (status) => {
  emit('update:current-status', status)
}

// 处理搜索输入
const handleSearchInput = (value) => {
  emit('update:search-keyword', value)
}

// 处理表格选择变更
const handleSelectionChange = (rows) => {
  selectedRows.value = rows
  emit('selection-change', rows)
}

// 处理编辑
const handleEdit = (row) => {
  emit('edit', row)
}

// 处理删除
const handleDelete = (row) => {
  emit('delete', row)
}

// 处理预览
const handlePreview = (row) => {
  emit('preview', row)
}

// 处理分页大小变化
const handleSizeChange = (size) => {
  pageSizeValue.value = size
  emit('update:page-size', size)
}

// 处理当前页变化
const handleCurrentChange = (page) => {
  currentPageValue.value = page
  emit('update:current-page', page)
}

// 处理排序变化
const handleSortChange = (column) => {
  emit('sort-change', column)
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

// 解析元数据JSON
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
    // 如果是字符串类型
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
    
    return '-';
  } catch (e) {
    return '提取失败';
  }
}

// 生成分类分级值
const generateClassificationLevel = (row) => {
  try {
    // 设置当前行数据
    currentRow.value = row;
    
    // 为分类分级对话框初始化数据
    classificationLevelData.value = {
      // 优先使用classificationValue，如果不存在则尝试使用totalCategoryValue
      classificationValue: row.classificationValue || row.totalCategoryValue || '',
      industryCategory: row.industryCategory || '',
      dataTimeliness: row.dataTimeliness || '',
      dataSource: row.dataSource || '',
      // 优先使用levelValue，如果不存在则尝试使用totalGradeValue
      levelValue: row.levelValue || row.totalGradeValue || '',
      // 硬编码默认分级值 - 未来可以从服务获取
      dbGrade: row.dbGrade !== undefined ? row.dbGrade : 0,
      tableGrade: row.tableGrade !== undefined ? row.tableGrade : 0,
      rowGrades: row.rowGrades || [0, 0],
      columnGrades: row.columnGrades || [0, 0]
    };
    
    // 显示分类分级对话框
    classificationLevelDialogVisible.value = true;
  } catch (error) {
    ElMessage.error('生成分类分级值时出错');
  }
};

// 处理分类分级确认
const handleClassificationLevelConfirm = async (data) => {
  try {
    if (currentRow.value) {
      // 更新当前行的分类值和分级值
      currentRow.value.classificationValue = data.classificationValue;
      currentRow.value.totalCategoryValue = data.classificationValue;
      
      currentRow.value.industryCategory = data.industryCategory;
      currentRow.value.dataTimeliness = data.dataTimeliness;
      currentRow.value.dataSource = data.dataSource;
      
      currentRow.value.levelValue = data.levelValue;
      currentRow.value.totalGradeValue = data.levelValue;
      
      // 更新分级值
      currentRow.value.dbGrade = data.dbGrade;
      currentRow.value.tableGrade = data.tableGrade;
      currentRow.value.rowGrades = data.rowGrades;
      currentRow.value.columnGrades = data.columnGrades;
      
      // 保存到本地存储，作为临时备份，以防API保存失败
      try {
        const savedValues = JSON.parse(localStorage.getItem('classificationLevelValues') || '{}');
        savedValues[currentRow.value.id] = {
          ...data,
          timestamp: new Date().toISOString()
        };
        localStorage.setItem('classificationLevelValues', JSON.stringify(savedValues));
      } catch (localStorageError) {
        console.warn('【分类分级值】本地存储保存失败:', localStorageError);
      }
      
      // 构建分类值数据
      const categoryData = {
        industryCategory: data.industryCategory || '',
        processingTimeCategory: data.dataTimeliness || '', // 后端使用processingTimeCategory，前端使用dataTimeliness
        dataSourceCategory: data.dataSource || ''
      };
      
      // 直接向API提交分类数据，而不是通过数据服务同步
      try {
        const objectId = currentRow.value.id;
        
        // 使用fetch API发送分类数据到特定端点
        const categoriesResponse = await fetch(`http://localhost:8080/api/objects/${objectId}/categories`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(categoryData)
        });
        
        // 检查响应状态
        if (!categoriesResponse.ok) {
          console.warn(`分类数据提交状态: ${categoriesResponse.status} ${categoriesResponse.statusText}`);
        } else {
          console.log('分类数据提交成功:', await categoriesResponse.text());
        }
      } catch (apiError) {
        console.error('分类数据API提交失败:', apiError);
        // API错误不阻止本地更新
      }
      
      // 确保表格数据也得到更新
      const index = props.data.findIndex(item => item.id === currentRow.value.id);
      if (index !== -1) {
        // 创建对象的副本进行更新而不是直接修改
        const updatedItem = { ...props.data[index], ...currentRow.value };
        // 替换原对象
        props.data[index] = updatedItem;
        console.log('已更新数据源中的数据项:', updatedItem);
      }
      
      // 通知用户更新成功
      ElMessage.success('分类分级值已更新');
      
      // 触发数据更新事件 - 使用新数组触发Vue的响应性系统
      emit('update:data', [...props.data]);
    } else {
      ElMessage.error('更新分类分级值失败：当前行数据为空');
    }
    
    // 关闭对话框
    classificationLevelDialogVisible.value = false;
  } catch (error) {
    console.error('更新分类分级值时出错:', error);
    ElMessage.error(`更新分类分级值失败：${error.message}`);
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
  background-color: #f5f5f5;
  color: #8c8c8c;
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
  color: #f56c6c;
  font-weight: 500;
  font-size: 13px;
  display: inline-block;
  padding: 2px 6px;
  background-color: #fef0f0;
  border-radius: 4px;
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
</style> 