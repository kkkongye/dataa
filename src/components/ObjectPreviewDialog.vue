<template>
  <el-dialog
    v-model="dialogVisible"
    :title="`预览 - ${displayObject.entity || ''}`"
    width="90%"
    :close-on-click-modal="false"
    draggable
    class="custom-dialog"
    top="5vh"
  >
    <div class="preview-header">
      <div class="preview-info">
        <!-- 基本信息表格 -->
        <div class="basic-info-table two-rows">
          <div v-if="isLoadingObjectData" class="loading-info">
            <el-icon class="is-loading" :size="20"><Loading /></el-icon>
            <span>正在加载对象信息...</span>
          </div>
          <template v-else>
            <div class="info-row">
              <span class="info-item"><strong>ID：</strong>{{ displayObject.id }}</span>
              <span class="info-item"><strong>实体：</strong>{{ displayObject.entity }}</span>
              <span class="info-item"><strong>定位信息：</strong>
                <template v-if="getLocationInfoObj(displayObject.locationInfo, displayObject.locationInfoJson)">
                  ({{ getLocationInfoObj(displayObject.locationInfo, displayObject.locationInfoJson).databaseName || '-' }},
                   {{ getLocationInfoObj(displayObject.locationInfo, displayObject.locationInfoJson).tableName || '-' }},
                   <el-popover placement="top" trigger="click">
                     <template #reference>
                       <span class="select-fields-link" style="color:#409EFF;cursor:pointer;">select字段</span>
                     </template>
                     <div style="max-width:400px;word-break:break-all;">
                       {{ getLocationInfoObj(displayObject.locationInfo, displayObject.locationInfoJson).selectFields }}
                     </div>
                   </el-popover>
                  )
                </template>
                <template v-else>-</template>
              </span>
            </div>
            <div class="info-row">
              <span class="info-item constraint-info" :title="Array.isArray(displayObject.constraint) ? displayObject.constraint.join(', ') : displayObject.constraint"><strong>约束条件：</strong>{{ Array.isArray(displayObject.constraint) ? displayObject.constraint.join(', ') : displayObject.constraint }}</span>
              <span class="info-item"><strong>传输控制操作：</strong>{{ Array.isArray(displayObject.transferControl) ? displayObject.transferControl.join(', ') : displayObject.transferControl }}</span>
              <span class="info-item"><strong>分类分级值：</strong>{{ 
                        (() => {
                          const sum = (parseFloat(displayObject.totalCategoryValue) || 0) + (parseFloat(displayObject.totalGradeValue) || 0);
                          return sum === 0 ? '未生成分类分级值' : sum.toFixed(4);
                        })()
                      }}</span>
            </div>
          </template>
        </div>
        <!-- 元数据信息显示 -->
        <div v-if="displayObject.metadata" class="metadata-section">
          <span class="info-item"><strong>状态：</strong>{{ displayObject.status }}</span>
          <div class="info-item">更新时间: <strong>{{ getCurrentDateTime() }}</strong></div>
          <div class="metadata-items">
            <div class="metadata-item">数据名称: <strong>{{ displayObject.metadata.dataName || displayObject.entity }}</strong></div>
            <div class="metadata-item">来源单位: <strong>{{ displayObject.metadata.sourceUnit || '数据部' }}</strong></div>
            <div class="metadata-item">联系人: <strong>{{ displayObject.metadata.contactPerson || '未指定' }}</strong></div>
            <div class="metadata-item">联系电话: <strong>{{ displayObject.metadata.contactPhone || '未提供' }}</strong></div>
            <div class="metadata-item">资源摘要: <strong>{{ displayObject.metadata.resourceSummary|| '无' }}</strong></div>
            <div class="metadata-item">领域分类: <strong>{{ displayObject.metadata.fieldClassification || '未分类' }}</strong></div>
          </div>
        </div>
      </div>
    </div>
    <div class="excel-data-section">
      <h3 class="section-title">数据预览</h3>
      <div v-if="isExcelLoading" class="loading-container">
        <!-- 替换el-loading为Element Plus的加载指示器 -->
        <el-icon class="is-loading" :size="30"><Loading /></el-icon>
        <span class="loading-text">正在加载Excel数据...</span>
      </div>
      <div v-else-if="totalCount > 0" class="excel-table-container">
        <div class="data-info">找到 {{ totalCount }} 条记录，当前显示第 {{ (currentPage - 1) * pageSize + 1 }} - {{ Math.min(currentPage * pageSize, totalCount) }} 条</div>
        <el-table :data="excelTableData" border stripe style="width: 100%">
          <!-- 序号列 -->
          <el-table-column 
            label="序号" 
            type="index" 
            width="80" 
            align="center"
            :index="(index) => (currentPage - 1) * pageSize + index + 1"
          />
          <!-- 数据列，过滤掉rowNumber字段 -->
          <el-table-column 
            v-for="(key, index) in getFilteredObjectKeys(excelTableData)" 
            :key="index"
            :prop="key"
            :label="key"
            :align="typeof excelTableData[0] && typeof excelTableData[0][key] === 'number' ? 'center' : 'left'"
            :min-width="100"
          />
        </el-table>
        
        <!-- 分页组件 -->
        <div class="pagination-container">
          <CommonPagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total-count="totalCount"
            :page-sizes="[10, 20, 50, 100]"
            background
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
      <div v-else class="no-data-message">
        <el-empty description="暂无数据" />
      </div>
    </div>
    <template v-slot:footer>
      <span class="dialog-footer">
        <slot name="footer"></slot>
        <el-button type="primary" v-if="totalCount > 0" @click="handleExportExcel">导出Excel</el-button>
        <el-button @click="closeDialog">关闭</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch, defineProps, defineEmits, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import * as XLSX from 'xlsx'
import CommonPagination from './CommonPagination.vue'
import dataObjectService from '../services/dataObjectService.js'

const props = defineProps({
  visible: Boolean,
  object: {
    type: Object,
    default: () => ({})
  },
  excelData: {
    type: Array,
    default: () => []
  }
})
const emit = defineEmits(['update:visible'])
const dialogVisible = ref(props.visible)
const excelTableData = ref([])
const isExcelLoading = ref(false)

// 分页相关变量
const currentPage = ref(1)
const pageSize = ref(10)
const totalCount = ref(0)

// 原始数据源（不直接存储所有数据）
const originalDataSource = ref(null)
const loadedPages = ref(new Map()) // 缓存已加载的页面数据

// 完整的对象数据（从API获取）
const fullObjectData = ref(null)
const isLoadingObjectData = ref(false)

// 计算属性：决定显示哪个对象的数据
const displayObject = computed(() => {
  // 优先使用从API获取的完整数据
  if (fullObjectData.value) {
    return fullObjectData.value
  }
  // 回退到props传入的对象数据
  return props.object || {}
})

// 根据ID从API获取完整的对象数据
const fetchObjectDataById = async (id) => {
  if (!id) return null
  
  try {
    isLoadingObjectData.value = true
    
    // 使用dataObjectService中的fetchDataObjectById方法
    const result = await dataObjectService.fetchDataObjectById(id)
    
    if (result) {
      console.log('API返回的数据:', result)
      return result
    }
    
    return null
  } catch (error) {
    console.error('获取对象数据失败:', error)
    ElMessage.error('获取数据失败，请稍后重试')
    return null
  } finally {
    isLoadingObjectData.value = false
  }
}

// 初始化时和依赖变化时更新数据源
const updateTableData = async () => {
  // 优先使用传入的excelData
  if (props.excelData && props.excelData.length > 0) {
    originalDataSource.value = props.excelData
  } 
  // 如果excelData为空但有object.id，则从API获取完整数据
  else if (props.object && props.object.id) {
    const fetchedData = await fetchObjectDataById(props.object.id)
    
    if (fetchedData) {
      fullObjectData.value = fetchedData
      // 使用API返回的dataItems数据
      if (fetchedData.dataItems && fetchedData.dataItems.length > 0) {
        originalDataSource.value = fetchedData.dataItems
      } else {
        originalDataSource.value = []
      }
    } else {
      // API获取失败，回退到使用props.object.dataItems
      originalDataSource.value = props.object.dataItems || []
    }
  }
  // 如果excelData为空但object.dataItems存在，则使用object.dataItems
  else if (props.object && props.object.dataItems && props.object.dataItems.length > 0) {
    originalDataSource.value = props.object.dataItems
  } 
  // 如果都为空则清空数据
  else {
    originalDataSource.value = []
  }
  
  // 更新总数和重置页码
  totalCount.value = originalDataSource.value ? originalDataSource.value.length : 0
  currentPage.value = 1
  loadedPages.value.clear() // 清空缓存
  
  // 加载第一页数据
  loadPageData(1)
}

// 懒加载指定页面的数据
const loadPageData = (page) => {
  if (!originalDataSource.value || originalDataSource.value.length === 0) {
    excelTableData.value = []
    return
  }
  
  // 检查是否已缓存该页数据
  if (loadedPages.value.has(page)) {
    excelTableData.value = loadedPages.value.get(page)
    return
  }
  
  // 计算页面数据范围
  const startIndex = (page - 1) * pageSize.value
  const endIndex = startIndex + pageSize.value
  const pageData = originalDataSource.value.slice(startIndex, endIndex)
  
  // 缓存页面数据
  loadedPages.value.set(page, pageData)
  excelTableData.value = pageData
}

// 监听当前页变化，懒加载数据
watch(currentPage, (newPage) => {
  loadPageData(newPage)
}, { immediate: true })

// 添加一个标志来防止重复调用
const isUpdating = ref(false)

// 优化的updateTableData调用函数
const safeUpdateTableData = async () => {
  if (isUpdating.value) {
    return
  }
  isUpdating.value = true
  try {
    await updateTableData()
  } finally {
    isUpdating.value = false
  }
}

// 监听props变化 - 只在弹窗打开时调用一次
watch(() => props.visible, (val) => {
  dialogVisible.value = val
  if (val) {
    safeUpdateTableData()
  }
})

// 监听dialogVisible变化并通知父组件
watch(dialogVisible, (val) => {
  emit('update:visible', val)
})

// 监听excelData变化 - 只在弹窗可见且数据真正变化时调用
watch(() => props.excelData, (newVal, oldVal) => {
  if (props.visible && JSON.stringify(newVal) !== JSON.stringify(oldVal)) {
    safeUpdateTableData()
  }
}, { deep: true })

// 监听object.dataItems变化 - 只在没有excelData且弹窗可见时调用
watch(() => props.object?.dataItems, (newVal, oldVal) => {
  if (props.visible && (!props.excelData || props.excelData.length === 0) && JSON.stringify(newVal) !== JSON.stringify(oldVal)) {
    safeUpdateTableData()
  }
}, { deep: true })

// 分页处理函数
const handleCurrentChange = (page) => {
  currentPage.value = page
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadedPages.value.clear() // 清空缓存，因为页面大小改变了
  loadPageData(1) // 重新加载第一页
}

// 组件挂载时初始化
onMounted(() => {
  updateTableData()
})

function closeDialog() {
  dialogVisible.value = false
}

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
function isSelectFieldsLong(selectFields) {
  if (!selectFields) return false;
  return selectFields.length > 30;
}
function getObjectKeys(dataArray) {
  if (!dataArray || !Array.isArray(dataArray) || dataArray.length === 0) return []
  const keySets = dataArray.map(item => (item && typeof item === 'object') ? Object.keys(item) : [])
  return [...new Set(keySets.flat())]
}
function getFilteredObjectKeys(dataArray) {
  if (!dataArray || !Array.isArray(dataArray) || dataArray.length === 0) return []
  const keySets = dataArray.map(item => (item && typeof item === 'object') ? Object.keys(item) : [])
  const allKeys = [...new Set(keySets.flat())]
  // 过滤掉rowNumber字段
  return allKeys.filter(key => key !== 'rowNumber')
}
function getCurrentDateTime() {
  const now = new Date()
  return now.toLocaleString('zh-CN', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit', second: '2-digit', hour12: false
  })
}
function handleExportExcel() {
  if (!originalDataSource.value || !originalDataSource.value.length) {
    ElMessage.warning('没有数据可导出')
    return
  }
  try {
    const wb = XLSX.utils.book_new()
    const ws = XLSX.utils.json_to_sheet(originalDataSource.value)
    XLSX.utils.book_append_sheet(wb, ws, 'Sheet1')
    const fileName = `${displayObject.value.entity || 'excel_data'}.xlsx`
    XLSX.writeFile(wb, fileName)
    ElMessage.success(`已成功导出 ${fileName}`)
  } catch (error) {
    ElMessage.error(`导出Excel失败: ${error.message}`)
  }
}
</script>

<style scoped>
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
  flex-wrap: wrap;
  gap: 20px;
  justify-content: center;
  width: 100%;
  max-width: 100%;
  overflow-x: auto;
  white-space: normal;
}
.info-item {
  display: inline-block;
  padding: 0 10px;
  color: #333;
  font-size: 14px;
  max-width: 600px;
  overflow: hidden;
  text-overflow: ellipsis;
  text-align: center;
  white-space: normal;
}
.select-fields-link {
  text-decoration: underline;
}
.metadata-section {
  margin: 10px auto 5px;
  padding: 8px 10px;
  background-color: #f9f9f9;
  border-radius: 4px;
  width: 100%;
  max-width: 1800px;
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
  -ms-overflow-style: none;
}
.metadata-item {
  padding: 4px 8px;
  background-color: transparent;
  border-radius: 0;
  box-shadow: none;
  border: none;
  margin: 0 8px;
  white-space: nowrap;
  flex-shrink: 0;
  font-size: 13px;
}
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
.no-data-message {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  color: #909399;
}
.section-title {
  font-size: 18px;
  color: #333;
  margin: 10px 0 15px;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
  text-align: center;
}
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 12px;
  margin-top: 15px;
}

.dialog-footer .el-button {
  margin: 0;
}

.pagination-container {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  margin-top: 15px;
  padding: 10px 0;
}
.constraint-info {
  max-width: 800px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  display: inline-block;
  vertical-align: middle;
}
.loading-container {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 150px;
}
.loading-info {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 20px;
  color: #909399;
  font-size: 14px;
}
.is-loading {
  animation: rotating 2s linear infinite;
}
.loading-text {
  margin-top: 10px;
  color: #909399;
  font-size: 14px;
}
@keyframes rotating {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 表头样式 - 浅灰色背景 */
:deep(.el-table__header th.el-table__cell) {
  background-color: #f5f7fa !important;
  color: #606266 !important;
  font-weight: bold !important;
  font-size: 14px !important;
  text-align: center !important;
  padding: 12px 8px !important;
}
</style>