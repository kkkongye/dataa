<template>
  <el-dialog
    v-model="dialogVisible"
    title="Excel预览"
    width="80%"
    :close-on-click-modal="false"
    class="excel-preview-dialog"
  >
    <div class="excel-preview-container">
      <!-- 元数据信息部分 -->
      <div class="metadata-section" v-if="object && object.metadata">
        <h3>元数据信息</h3>
        <div class="metadata-items">
          <div class="metadata-item">
            <span class="label">数据名称：</span>
            <span class="value">{{ object.metadata.dataName || '--' }}</span>
          </div>
          <div class="metadata-item">
            <span class="label">来源单位：</span>
            <span class="value">{{ object.metadata.sourceUnit || '--' }}</span>
          </div>
          <div class="metadata-item">
            <span class="label">联系人：</span>
            <span class="value">{{ object.metadata.contactPerson || '--' }}</span>
          </div>
          <div class="metadata-item">
            <span class="label">联系电话：</span>
            <span class="value">{{ object.metadata.contactPhone || '--' }}</span>
          </div>
          <div class="metadata-item">
            <span class="label">资源摘要：</span>
            <span class="value">{{ object.metadata.resourceSummary || '--' }}</span>
          </div>
          <div class="metadata-item">
            <span class="label">领域分类：</span>
            <span class="value">{{ object.metadata.fieldClassification || '--' }}</span>
          </div>
        </div>
      </div>

      <!-- 对象信息部分 -->
      <div class="object-info-section" v-if="object">
        <h3>对象信息</h3>
        <div class="info-items">
          <div class="info-item">
            <span class="label">实体：</span>
            <span class="value">{{ object.entity || '--' }}</span>
          </div>
          <div class="info-item">
            <span class="label">ID：</span>
            <span class="value">{{ object.id || '--' }}</span>
          </div>
          <div class="info-item">
            <span class="label">位置：</span>
            <span class="value" v-if="object.locationInfo">
              ({{ object.locationInfo.row || '--' }}, {{ object.locationInfo.col || '--' }})
            </span>
            <span class="value" v-else>--</span>
          </div>
        </div>
      </div>

      <!-- Excel数据表格 -->
      <div class="excel-data-section">
        <h3>Excel数据</h3>
        <div class="excel-table-container" v-if="tableData.length > 0">
          <el-table :data="tableData" border style="width: 100%" height="400">
            <el-table-column 
              v-for="(col, index) in tableColumns" 
              :key="index"
              :prop="col.prop"
              :label="col.label"
              :width="col.width || 150"
            />
          </el-table>
        </div>
        <div v-else class="no-data">
          <span>暂无数据</span>
        </div>
      </div>
    </div>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="dialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleExport">导出Excel</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch, defineProps, defineEmits, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import * as XLSX from 'xlsx'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  object: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['update:visible'])

// 对话框可见性
const dialogVisible = ref(false)

// 表格数据和列
const tableData = ref([])
const tableColumns = ref([])

// 监听visible属性变化
watch(() => props.visible, (newVal) => {
  console.log('ExcelPreview组件 - visible变化:', newVal)
  dialogVisible.value = newVal
  if (newVal && props.object) {
    console.log('ExcelPreview组件 - visible为true且有object，解析对象数据')
    parseObjectData()
  }
})

// 监听dialogVisible变化
watch(dialogVisible, (newVal) => {
  console.log('ExcelPreview组件 - dialogVisible变化:', newVal)
  emit('update:visible', newVal)
})

// 监听object变化
watch(() => props.object, (newVal) => {
  console.log('ExcelPreview组件 - object变化:', newVal)
  if (newVal && dialogVisible.value) {
    console.log('ExcelPreview组件 - object变化且dialogVisible为true，解析对象数据')
    parseObjectData()
  }
}, { deep: true })

// 解析元数据JSON
const parseMetadataJson = (jsonString) => {
  console.log('开始解析metadataJson:', jsonString)
  try {
    if (!jsonString) {
      console.log('metadataJson为空')
      return {}
    }
    
    let metadata = {}
    
    // 处理不同格式的metadataJson
    if (typeof jsonString === 'string') {
      console.log('metadataJson是字符串类型')
      
      try {
        // 1. 尝试直接解析标准JSON
        metadata = JSON.parse(jsonString)
        console.log('成功解析标准JSON字符串')
      } catch (parseError) {
        console.warn('标准JSON解析失败，尝试处理转义字符:', parseError)
        
        // 2. 处理各种转义的情况
        let processedString = jsonString
        
        // 处理可能的反斜杠转义
        if (jsonString.includes('\\')) {
          try {
            // 尝试处理双重转义的JSON字符串 
            // 例如: "{\\\"key\\\":\\\"value\\\"}"
            processedString = jsonString.replace(/\\"/g, '"')
            metadata = JSON.parse(processedString)
            console.log('成功解析处理转义后的JSON字符串 (步骤1)')
          } catch (error) {
            console.warn('处理转义后解析失败 (步骤1):', error)
            
            try {
              // 尝试删除开头和结尾的引号，并处理转义
              // 例如: "{\\"key\\":\\"value\\"}" -> {"key":"value"}
              if (jsonString.startsWith('"') && jsonString.endsWith('"')) {
                processedString = jsonString.substring(1, jsonString.length - 1).replace(/\\"/g, '"')
                metadata = JSON.parse(processedString)
                console.log('成功解析处理转义后的JSON字符串 (步骤2)')
              }
            } catch (error2) {
              console.warn('处理转义后解析失败 (步骤2):', error2)
              
              try {
                // 尝试将双反斜杠替换为单反斜杠
                // 例如: {\\\"key\\\":\\\"value\\\"} -> {\"key\":\"value\"}
                processedString = jsonString.replace(/\\\\/g, '\\')
                metadata = JSON.parse(processedString)
                console.log('成功解析处理转义后的JSON字符串 (步骤3)')
              } catch (error3) {
                console.warn('处理转义后解析失败 (步骤3):', error3)
              }
            }
          }
        }
        
        // 3. 如果以上都失败，尝试正则表达式提取关键字段
        if (Object.keys(metadata).length === 0) {
          console.log('尝试使用正则表达式提取关键字段')
          
          // 匹配各种可能的格式，应对各种转义情况
          const patterns = [
            // 匹配: "resourceSummary":"值"
            /resourceSummary[\\]*"*:[\\]*"*([^"\\,}]+)/,
            // 匹配: resourceSummary=值
            /resourceSummary=([^,}]+)/,
            // 匹配: "resourceSummary":值
            /resourceSummary[\\]*":([^",}]+)/
          ]
          
          const fieldPatterns = [
            // 匹配: "fieldClassification":"值"
            /fieldClassification[\\]*"*:[\\]*"*([^"\\,}]+)/,
            // 匹配: fieldClassification=值
            /fieldClassification=([^,}]+)/,
            // 匹配: "fieldClassification":值
            /fieldClassification[\\]*":([^",}]+)/
          ]
          
          // 尝试每种模式
          for (const pattern of patterns) {
            const match = jsonString.match(pattern)
            if (match && match[1]) {
              metadata.resourceSummary = match[1].trim()
              console.log('通过正则提取到resourceSummary:', metadata.resourceSummary)
              break
            }
          }
          
          for (const pattern of fieldPatterns) {
            const match = jsonString.match(pattern)
            if (match && match[1]) {
              metadata.fieldClassification = match[1].trim()
              console.log('通过正则提取到fieldClassification:', metadata.fieldClassification)
              break
            }
          }
        }
      }
    } else if (typeof jsonString === 'object') {
      console.log('metadataJson是对象类型')
      // 已经是对象，直接使用
      metadata = jsonString
    }
    
    // 构建结果对象，确保所有字段都有默认值
    const result = {
      dataName: metadata.dataName || '',
      sourceUnit: metadata.sourceUnit || '',
      contactPerson: metadata.contactPerson || '',
      contactPhone: metadata.contactPhone || '',
      resourceSummary: metadata.resourceSummary || '',
      fieldClassification: metadata.fieldClassification || ''
    }
    
    console.log('最终解析的元数据:', result)
    return result
  } catch (error) {
    console.error('解析元数据JSON失败:', error)
    return {}
  }
}

// 解析对象数据
const parseObjectData = () => {
  if (!props.object) return

  console.log('开始解析对象数据:', props.object)
  
  // 创建元数据对象（如果不存在）
  if (!props.object.metadata) {
    props.object.metadata = {}
  }
  
  // 处理元数据 - 分多种情况
  // 1. 直接使用对象中的resourceSummary和fieldClassification字段
  if (props.object.resourceSummary || props.object.fieldClassification) {
    console.log('对象顶层包含resourceSummary或fieldClassification字段')
    props.object.metadata.resourceSummary = props.object.resourceSummary || props.object.metadata.resourceSummary || ''
    props.object.metadata.fieldClassification = props.object.fieldClassification || props.object.metadata.fieldClassification || ''
  }
  
  // 2. 从metadataJson字段中解析
  if (props.object.metadataJson) {
    console.log('发现metadataJson字段:', props.object.metadataJson)
    // 解析元数据JSON，并合并到现有metadata对象
    const parsedMetadata = parseMetadataJson(props.object.metadataJson)
    // 只有在解析得到的字段有值时才覆盖现有值
    Object.keys(parsedMetadata).forEach(key => {
      if (parsedMetadata[key]) {
        props.object.metadata[key] = parsedMetadata[key]
      }
    })
  }
  
  // 最后确保所有元数据字段都有默认值，避免模板中出现undefined
  const defaultMetadata = {
    dataName: props.object.entity || '',
    sourceUnit: '',
    contactPerson: '',
    contactPhone: '',
    resourceSummary: '',
    fieldClassification: ''
  }
  
  // 将默认值合并到metadata对象中
  props.object.metadata = { ...defaultMetadata, ...props.object.metadata }
  
  console.log('最终使用的metadata对象:', props.object.metadata)

  // 解析Excel数据
  if (props.object.excelData) {
    console.log('开始解析Excel数据')
    parseExcelData(props.object.excelData)
  } else {
    console.log('没有Excel数据')
    tableData.value = []
    tableColumns.value = []
  }
}

// 解析Excel数据
const parseExcelData = (excelData) => {
  try {
    // 从二进制数据中解析Excel
    const workbook = XLSX.read(excelData, { type: 'binary' })
    
    // 获取第一个工作表
    const firstSheetName = workbook.SheetNames[0]
    const worksheet = workbook.Sheets[firstSheetName]
    
    // 转换为JSON
    const data = XLSX.utils.sheet_to_json(worksheet)
    
    if (data.length === 0) {
      tableData.value = []
      tableColumns.value = []
      return
    }
    
    // 设置表格数据
    tableData.value = data
    
    // 设置表格列
    const firstRow = data[0]
    tableColumns.value = Object.keys(firstRow).map(key => ({
      prop: key,
      label: key
    }))
  } catch (error) {
    console.error('解析Excel数据失败:', error)
    ElMessage.error('Excel数据解析失败')
    tableData.value = []
    tableColumns.value = []
  }
}

// 导出Excel
const handleExport = () => {
  if (tableData.value.length === 0) {
    ElMessage.warning('没有数据可导出')
    return
  }
  
  try {
    // 创建工作簿
    const wb = XLSX.utils.book_new()
    
    // 创建工作表
    const ws = XLSX.utils.json_to_sheet(tableData.value)
    
    // 添加工作表到工作簿
    XLSX.utils.book_append_sheet(wb, ws, 'Sheet1')
    
    // 导出文件名
    const fileName = `${props.object.entity || 'excel_data'}.xlsx`
    
    // 保存文件
    XLSX.writeFile(wb, fileName)
    
    ElMessage.success(`已成功导出 ${fileName}`)
  } catch (error) {
    console.error('导出Excel失败:', error)
    ElMessage.error('导出Excel失败')
  }
}

// 组件挂载时
onMounted(() => {
  console.log('ExcelPreview组件 - 挂载完成')
  console.log('ExcelPreview组件 - dialogVisible初始值:', dialogVisible.value)
  console.log('ExcelPreview组件 - props.object初始值:', props.object)
  
  if (dialogVisible.value && props.object) {
    console.log('ExcelPreview组件 - 挂载时就需要解析对象数据')
    parseObjectData()
  }
})
</script>

<style scoped>
.excel-preview-dialog {
  display: flex;
  flex-direction: column;
}

.excel-preview-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.metadata-section,
.object-info-section,
.excel-data-section {
  border: 1px solid #e6e6e6;
  border-radius: 4px;
  padding: 15px;
  background-color: #f9f9f9;
}

h3 {
  margin-top: 0;
  margin-bottom: 15px;
  font-size: 16px;
  color: #409eff;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
}

.metadata-items,
.info-items {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
}

.metadata-item,
.info-item {
  min-width: 200px;
  flex: 1;
}

.label {
  font-weight: bold;
  color: #606266;
  margin-right: 5px;
}

.value {
  color: #333;
}

.excel-table-container {
  margin-top: 15px;
}

.no-data {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100px;
  color: #999;
  font-size: 14px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 15px;
}
</style> 