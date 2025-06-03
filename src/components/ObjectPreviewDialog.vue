<template>
  <el-dialog
    v-model="dialogVisible"
    :title="`预览 - ${object.entity || ''}`"
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
          <div class="info-row">
            <span class="info-item"><strong>实体：</strong>{{ object.entity }}</span>
            <span class="info-item"><strong>定位信息：</strong>
              <template v-if="getLocationInfoObj(object.locationInfo, object.locationInfoJson)">
                <template v-if="isSelectFieldsLong(getLocationInfoObj(object.locationInfo, object.locationInfoJson).selectFields)">
                  ({{ getLocationInfoObj(object.locationInfo, object.locationInfoJson).databaseName || '-' }},
                   {{ getLocationInfoObj(object.locationInfo, object.locationInfoJson).tableName || '-' }},
                   <el-popover placement="top" trigger="click">
                     <template #reference>
                       <span class="select-fields-link" style="color:#409EFF;cursor:pointer;">"select字段"</span>
                     </template>
                     <div style="max-width:400px;word-break:break-all;">{{ getLocationInfoObj(object.locationInfo, object.locationInfoJson).selectFields }}</div>
                   </el-popover>
                  )
                </template>
                <template v-else>
                  ({{ getLocationInfoObj(object.locationInfo, object.locationInfoJson).databaseName || '-' }},
                   {{ getLocationInfoObj(object.locationInfo, object.locationInfoJson).tableName || '-' }},
                   {{ getLocationInfoObj(object.locationInfo, object.locationInfoJson).selectFields || '-' }})
                </template>
              </template>
              <template v-else>{{ object.locationInfo }}</template>
            </span>
            <span class="info-item constraint-info" :title="Array.isArray(object.constraint) ? object.constraint.join(', ') : object.constraint"><strong>约束条件：</strong>{{ Array.isArray(object.constraint) ? object.constraint.join(', ') : object.constraint }}</span>
          </div>
          <div class="info-row">
            <span class="info-item"><strong>传输控制操作：</strong>{{ Array.isArray(object.transferControl) ? object.transferControl.join(', ') : object.transferControl }}</span>
            <span class="info-item"><strong>分类值：</strong>{{ object.totalCategoryValue || object.classificationValue || '未分类' }}</span>
            <span class="info-item"><strong>分级值：</strong>{{ object.totalGradeValue || object.levelValue || '未分级' }}</span>
          </div>
        </div>
        <!-- 元数据信息显示 -->
        <div v-if="object.metadata" class="metadata-section">
          <span class="info-item"><strong>状态：</strong>{{ object.status }}</span>
          <div class="metadata-items">
            <div class="metadata-item">数据名称: <strong>{{ object.metadata.dataName || object.entity }}</strong></div>
            <div class="metadata-item">来源单位: <strong>{{ object.metadata.sourceUnit || '数据部' }}</strong></div>
            <div class="metadata-item">联系人: <strong>{{ object.metadata.contactPerson || '未指定' }}</strong></div>
            <div class="metadata-item">联系电话: <strong>{{ object.metadata.contactPhone || '未提供' }}</strong></div>
            <div class="metadata-item">资源摘要: <strong>{{ object.metadata.resourceSummary|| '无' }}</strong></div>
            <div class="metadata-item">领域分类: <strong>{{ object.metadata.fieldClassification || '未分类' }}</strong></div>
            <div class="metadata-item">更新时间: <strong>{{ getCurrentDateTime() }}</strong></div>
          </div>
        </div>
      </div>
    </div>
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
            :align="typeof excelTableData[0][key] === 'number' ? 'center' : 'left'"
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
        <el-button @click="closeDialog">关闭</el-button>
        <el-button type="primary" v-if="excelTableData.length > 0" @click="handleExportExcel">导出Excel</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch, defineProps, defineEmits } from 'vue'
import { ElMessage } from 'element-plus'
import * as XLSX from 'xlsx'

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
const excelTableData = ref(props.excelData)
const isExcelLoading = ref(false)

watch(() => props.visible, (val) => {
  dialogVisible.value = val
})
watch(dialogVisible, (val) => {
  emit('update:visible', val)
})
watch(() => props.excelData, (val) => {
  excelTableData.value = val
})

function closeDialog() {
  dialogVisible.value = false
}

function getLocationInfoObj(locationInfo, locationInfoJson) {
  if (typeof locationInfo === 'string') {
    try { locationInfo = JSON.parse(locationInfo) } catch { locationInfo = null }
  }
  if ((!locationInfo || typeof locationInfo !== 'object') && locationInfoJson) {
    try { locationInfo = JSON.parse(locationInfoJson) } catch { locationInfo = null }
  }
  if (!locationInfo || typeof locationInfo !== 'object') return null
  return locationInfo
}
function isSelectFieldsLong(selectFields) {
  return typeof selectFields === 'string' && selectFields.length > 30
}
function getObjectKeys(dataArray) {
  if (!dataArray || !Array.isArray(dataArray) || dataArray.length === 0) return []
  const keySets = dataArray.map(item => (item && typeof item === 'object') ? Object.keys(item) : [])
  return [...new Set(keySets.flat())]
}
function getCurrentDateTime() {
  const now = new Date()
  return now.toLocaleString('zh-CN', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit', second: '2-digit', hour12: false
  })
}
function handleExportExcel() {
  if (!excelTableData.value.length) {
    ElMessage.warning('没有数据可导出')
    return
  }
  try {
    const wb = XLSX.utils.book_new()
    const ws = XLSX.utils.json_to_sheet(excelTableData.value)
    XLSX.utils.book_append_sheet(wb, ws, 'Sheet1')
    const fileName = `${props.object.entity || 'excel_data'}.xlsx`
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
.select-fields-link {
  text-decoration: underline;
}
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
  gap: 10px;
  margin-top: 15px;
}
</style> 