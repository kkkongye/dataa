<!-- 编辑对象对话框组件 -->
<template>
  <el-dialog
    v-model="dialogVisible"
    :title="title"
    width="40%"
    :close-on-click-modal="false"
    draggable
    class="custom-dialog"
    @closed="handleDialogClosed"
  >
    <el-form :model="form" label-width="120px" ref="formRef" :rules="formRules">
      <el-form-item label="ID：" v-if="form.id !== undefined && form.id !== null">
        <el-input v-model="form.id" disabled placeholder="自动生成" style="width: 300px;"></el-input>
      </el-form-item>
      <el-form-item label="实体：" prop="entity">
        <div style="display: flex; align-items: center; gap: 10px;">
          <el-upload
            action="#"
            :auto-upload="false"
            :show-file-list="false"
            :on-change="handleFileChange"
            accept=".xlsx,.xls"
          >
            <el-button type="primary" plain>
              <el-icon style="margin-right: 4px;"><Document /></el-icon>上传Excel表格
            </el-button>
          </el-upload>
          <span v-if="form.entity">已选择"{{ form.entity }}"</span>
          <span v-else class="upload-tip">请上传Excel表格文件</span>
        </div>
      </el-form-item>
      
      <!-- 元数据区域 -->
      <el-form-item label="数据名称：" prop="metadata.dataName" class="metadata-form-item">
        <el-input v-model="form.metadata.dataName" placeholder="请输入数据名称" style="width: 300px;"></el-input>
      </el-form-item>
      <el-form-item label="来源单位：" prop="metadata.sourceUnit" class="metadata-form-item">
        <el-input v-model="form.metadata.sourceUnit" placeholder="请输入来源单位" style="width: 300px;"></el-input>
      </el-form-item>
      <el-form-item label="联系人：" prop="metadata.contactPerson" class="metadata-form-item">
        <el-input v-model="form.metadata.contactPerson" placeholder="请输入联系人" style="width: 300px;"></el-input>
      </el-form-item>
      <el-form-item label="联系电话：" prop="metadata.contactPhone" class="metadata-form-item">
        <el-input v-model="form.metadata.contactPhone" placeholder="请输入联系电话" style="width: 300px;"></el-input>
      </el-form-item>
      <el-form-item label="资源摘要：" prop="metadata.resourceSummary" class="metadata-form-item">
        <el-input v-model="form.metadata.resourceSummary" placeholder="请输入资源摘要" style="width: 300px;"></el-input>
      </el-form-item>
      <el-form-item label="领域分类：" prop="metadata.fieldClassification" class="metadata-form-item">
        <el-input v-model="form.metadata.fieldClassification" placeholder="请输入领域分类" style="width: 300px;"></el-input>
      </el-form-item>
      
      <el-form-item label="定位信息：" prop="locationInfo" style="margin-bottom: 22px;">
        <div style="display: flex; align-items: center; gap: 10px;">
          <el-input v-model="form.locationInfo.row" placeholder="例：0-4" style="width: 150px;"></el-input>
          <span>行</span>
          <el-input v-model="form.locationInfo.col" placeholder="例：A-D" style="width: 150px;"></el-input>
          <span>列</span>
        </div>
      </el-form-item>
      <el-form-item label="约束条件：" prop="constraint">
        <div class="constraint-section">
          <div class="constraint-item">
            <label>格式约束：</label>
            <div class="custom-select-wrapper">
              <el-select v-model="form.formatConstraint" placeholder="请选择格式" class="custom-select-component">
                <el-option label="jpg" value="jpg"></el-option>
                <el-option label="xlsx" value="xlsx"></el-option>
              </el-select>
              <div v-if="form.formatConstraint" class="selected-value">{{ form.formatConstraint }}</div>
            </div>
          </div>
          
          <div class="constraint-item">
            <label>访问权限：</label>
            <div class="custom-select-wrapper">
              <el-select v-model="form.accessConstraint" placeholder="请选择访问权限" class="custom-select-component">
                <el-option label="只允许管理方获取" value="只允许管理方获取"></el-option>
                <el-option label="全部允许" value="全部允许"></el-option>
              </el-select>
              <div v-if="form.accessConstraint" class="selected-value">{{ form.accessConstraint }}</div>
            </div>
          </div>
          
          <div class="constraint-item">
            <label>传输路径约束：</label>
            <div class="custom-select-wrapper">
              <el-select v-model="form.pathConstraint" placeholder="请选择传输路径" class="custom-select-component">
                <el-option label="点对点" value="点对点"></el-option>
                <el-option label="广播" value="广播"></el-option>
              </el-select>
              <div v-if="form.pathConstraint" class="selected-value">{{ form.pathConstraint }}</div>
            </div>
          </div>
          
          <div class="constraint-item">
            <label>地域性约束：</label>
            <div class="custom-select-wrapper">
              <el-select v-model="form.regionConstraint" placeholder="请选择地域性约束" class="custom-select-component">
                <el-option label="内网" value="内网"></el-option>
                <el-option label="外网" value="外网"></el-option>
              </el-select>
              <div v-if="form.regionConstraint" class="selected-value">{{ form.regionConstraint }}</div>
            </div>
          </div>
          
          <div class="constraint-item">
            <label>共享约束：</label>
            <div class="custom-select-wrapper">
              <el-select v-model="form.shareConstraint" placeholder="请选择共享约束" class="custom-select-component">
                <el-option label="不允许共享" value="不允许共享"></el-option>
                <el-option label="允许共享" value="允许共享"></el-option>
              </el-select>
              <div v-if="form.shareConstraint" class="selected-value">{{ form.shareConstraint }}</div>
            </div>
          </div>
        </div>
      </el-form-item>
      <el-form-item label="传输控制操作：" prop="transferControl">
        <div class="custom-multi-select-wrapper">
          <el-select 
            v-model="form.transferControl" 
            placeholder="请选择传输控制操作" 
            multiple
            class="custom-multi-select"
          >
            <el-option label="可读" value="可读"></el-option>
            <el-option label="可修改" value="可修改"></el-option>
            <el-option label="可销毁" value="可销毁"></el-option>
            <el-option label="可共享" value="可共享"></el-option>
            <el-option label="可委托" value="可委托"></el-option>
          </el-select>
        </div>
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" @click="handleSave">确定</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch, defineProps, defineEmits } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document } from '@element-plus/icons-vue'
import excelUploadService from '@/services/excelUploadService'
import { API_URL, MOCK_ENABLED, AUTO_FALLBACK_TO_MOCK } from '@/services/apiConfig'
import * as XLSX from 'xlsx' // 确保引入XLSX库用于处理Excel数据

const props = defineProps({
  // 是否显示对话框
  visible: {
    type: Boolean,
    default: false
  },
  // 对话框标题
  title: {
    type: String,
    default: '编辑数字对象'
  },
  // 表单数据
  modelValue: {
    type: Object,
    default: () => ({
      id: '',
      entity: '',
      locationInfo: {
        row: '',
        col: ''
      },
      constraint: [],
      transferControl: [],
      auditInfo: '',
      status: '',
      feedback: '',
      excelData: null
    })
  }
})

// 添加离线模式状态
const offlineMode = ref(false)
const apiError = ref(null)

const emit = defineEmits(['update:visible', 'update:modelValue', 'save', 'cancel', 'navigate-home'])

// 表单引用
const formRef = ref(null)

// 对话框可见性状态
const dialogVisible = ref(false)

// 表单数据
const form = reactive({
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

// 表单校验规则
const formRules = {
  entity: [
    { required: false, message: '请输入实体名称', trigger: 'blur' }
  ],
  'metadata.dataName': [
    { required: false, message: '请输入数据名称', trigger: 'blur' }
  ],
  'metadata.sourceUnit': [
    { required: false, message: '请输入来源单位', trigger: 'blur' }
  ],
  'metadata.contactPerson': [
    { required: false, message: '请输入联系人', trigger: 'blur' }
  ],
  'metadata.contactPhone': [
    { required: false, message: '请输入联系电话', trigger: 'blur' }
  ],
  'metadata.resourceSummary': [
    { required: false, message: '请输入资源摘要', trigger: 'blur' }
  ],
  'metadata.fieldClassification': [
    { required: false, message: '请输入领域分类', trigger: 'blur' }
  ],
  locationInfo: [
    { 
      validator: (rule, value, callback) => {
        if (form.locationInfo.row && form.locationInfo.col) {
          callback()
        } else {
          callback(new Error('请输入行和列'))
        }
      },
      trigger: 'blur'
    }
  ],
  constraint: [
    { type: 'array', trigger: 'change' }
  ],
  formatConstraint: [
    { required: true, message: '请选择格式约束', trigger: 'change' }
  ],
  accessConstraint: [
    { required: true, message: '请选择访问权限', trigger: 'change' }
  ],
  pathConstraint: [
    { required: true, message: '请选择传输路径约束', trigger: 'change' }
  ],
  regionConstraint: [
    { required: true, message: '请选择地域性约束', trigger: 'change' }
  ],
  shareConstraint: [
    { required: true, message: '请选择共享约束', trigger: 'change' }
  ],
  transferControl: [
    { type: 'array', trigger: 'change' }
  ]
}

// 监听可见性变化
watch(() => props.visible, (newVal) => {
  // console.log('EditObjectDialogNew组件可见性变化:', newVal)
  
  // 设置本地可见性状态
  dialogVisible.value = newVal
  
  if (newVal) {
    // 对话框打开时立即更新表单数据
    // console.log('对话框打开，应用modelValue:', props.modelValue)
    updateFormFromModelValue(props.modelValue)
  }
}, { immediate: true })

// 监听dialogVisible变化
watch(dialogVisible, (newVal) => {
  emit('update:visible', newVal)
})

// 监听modelValue变化
watch(() => props.modelValue, (newVal) => {
  // console.log('EditObjectDialogNew组件接收到新的modelValue:', newVal ? newVal.id : 'undefined')
  
  if (dialogVisible.value && newVal) {
    // console.log('应用新的modelValue到表单')
    updateFormFromModelValue(newVal)
  }
}, { immediate: true })

// 从modelValue更新表单数据的函数
const updateFormFromModelValue = (modelValue) => {
  // console.log('从modelValue更新表单数据:', modelValue)
  
  // 处理ID - 确保即使ID为0也能正确显示
  if (modelValue.id !== undefined && modelValue.id !== null) {
    form.id = String(modelValue.id)
    // console.log('设置ID值:', form.id)
  } else {
    form.id = ''
  }
  
  // 设置其他字段
  form.entity = modelValue.entity || ''
  
  // 设置定位信息
  if (modelValue.locationInfo) {
    if (typeof modelValue.locationInfo === 'string') {
      // 尝试从字符串解析
      const matches = modelValue.locationInfo.match(/\((.*?),\s*(.*?),\s*(.*?)\)/)
      if (matches && matches.length > 3) {
        form.locationInfo = {
          row: matches[2].trim(),
          col: matches[3].trim()
        }
      } else {
        form.locationInfo = { row: '', col: '' }
      }
    } else if (typeof modelValue.locationInfo === 'object') {
      // 直接使用对象格式
      form.locationInfo = {
        row: modelValue.locationInfo.row || '',
        col: modelValue.locationInfo.col || ''
      }
    } else {
      form.locationInfo = { row: '', col: '' }
    }
  } else {
    form.locationInfo = { row: '', col: '' }
  }
  
  // 设置元数据
  form.metadata = {
    dataName: '',
    sourceUnit: '',
    contactPerson: '',
    contactPhone: '',
    resourceSummary: '',
    fieldClassification: '',
    headers: []
  }
  
  if (modelValue.metadata && typeof modelValue.metadata === 'object') {
    form.metadata = { ...form.metadata, ...modelValue.metadata }
  }
  
  // 如果没有元数据，则使用实体名称和默认值
  if (!form.metadata.dataName) {
    form.metadata.dataName = modelValue.entity || ''
  }
  
  // 设置约束条件数组
  form.constraint = Array.isArray(modelValue.constraint) ? [...modelValue.constraint] : (modelValue.constraint ? [modelValue.constraint] : [])
  
  // 设置各个约束条件字段
  form.formatConstraint = modelValue.formatConstraint || ''
  form.accessConstraint = modelValue.accessConstraint || ''
  form.pathConstraint = modelValue.pathConstraint || ''
  form.regionConstraint = modelValue.regionConstraint || ''
  form.shareConstraint = modelValue.shareConstraint || ''
  
  // 如果没有明确的各约束字段值，尝试从约束数组中解析
  if ((!form.formatConstraint || !form.accessConstraint || !form.pathConstraint || 
      !form.regionConstraint || !form.shareConstraint) && form.constraint.length > 0) {
    form.constraint.forEach(item => {
      if (typeof item === 'string') {
        const parts = item.split(':')
        if (parts.length === 2) {
          const type = parts[0].trim()
          const value = parts[1].trim()
          
          if (type === '格式约束') form.formatConstraint = value
          else if (type === '访问权限') form.accessConstraint = value
          else if (type === '传输路径约束') form.pathConstraint = value
          else if (type === '地域性约束') form.regionConstraint = value
          else if (type === '共享约束') form.shareConstraint = value
        }
      }
    })
  }
  
  // 传输控制
  form.transferControl = Array.isArray(modelValue.transferControl) ? [...modelValue.transferControl] : (modelValue.transferControl ? [modelValue.transferControl] : [])
  
  // 其他字段
  form.auditInfo = modelValue.auditInfo || ''
  form.status = modelValue.status || ''
  form.feedback = modelValue.feedback || ''
  form.excelData = modelValue.excelData
  form.dataItems = modelValue.dataItems || []
  
  // console.log('表单数据更新完成:', JSON.stringify(form))
}

// 监听form变化，更新v-model
watch(form, (newVal) => {
  // 构建约束条件数组
  const constraintArray = []
  if (newVal.formatConstraint) constraintArray.push(`格式约束:${newVal.formatConstraint}`)
  if (newVal.accessConstraint) constraintArray.push(`访问权限:${newVal.accessConstraint}`)
  if (newVal.pathConstraint) constraintArray.push(`传输路径约束:${newVal.pathConstraint}`)
  if (newVal.regionConstraint) constraintArray.push(`地域性约束:${newVal.regionConstraint}`)
  if (newVal.shareConstraint) constraintArray.push(`共享约束:${newVal.shareConstraint}`)
  
  emit('update:modelValue', {
    id: newVal.id,
    entity: newVal.entity,
    locationInfo: {
      row: newVal.locationInfo.row,
      col: newVal.locationInfo.col
    },
    metadata: {
      dataName: newVal.metadata.dataName,
      sourceUnit: newVal.metadata.sourceUnit,
      contactPerson: newVal.metadata.contactPerson,
      contactPhone: newVal.metadata.contactPhone,
      resourceSummary: newVal.metadata.resourceSummary,
      fieldClassification: newVal.metadata.fieldClassification,
      headers: newVal.metadata.headers || []
    },
    constraint: constraintArray,
    formatConstraint: newVal.formatConstraint,
    accessConstraint: newVal.accessConstraint,
    pathConstraint: newVal.pathConstraint,
    regionConstraint: newVal.regionConstraint,
    shareConstraint: newVal.shareConstraint,
    transferControl: newVal.transferControl,
    auditInfo: newVal.auditInfo,
    status: newVal.status,
    feedback: newVal.feedback,
    excelData: newVal.excelData,
    dataItems: newVal.dataItems || [],
    offlineMode: offlineMode.value
  })
}, { deep: true })

// 直接重置表单，不触发任何事件
const resetFormDirectly = () => {
  form.id = ''
  form.entity = ''
  form.locationInfo = { row: '', col: '' }
  form.metadata = {
    dataName: '',
    sourceUnit: '',
    contactPerson: '',
    contactPhone: '',
    resourceSummary: '',
    fieldClassification: '',
    headers: []
  }
  form.constraint = []
  form.formatConstraint = ''
  form.accessConstraint = ''
  form.pathConstraint = ''
  form.regionConstraint = ''
  form.shareConstraint = ''
  form.transferControl = []
  form.auditInfo = ''
  form.status = ''
  form.feedback = ''
  form.excelData = null
  form.dataItems = []
}

// 重置表单
const resetForm = () => {
  resetFormDirectly()
  
  // 重置API错误状态
  apiError.value = null
  
  // 重置离线模式
  offlineMode.value = false
  
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

// 添加Excel数据处理函数
const processExcelData = (binaryString) => {
  try {
    // 将二进制字符串转换为工作簿对象
    const workbook = XLSX.read(binaryString, { type: 'binary' })
    
    // 获取第一个工作表的名称
    const firstSheetName = workbook.SheetNames[0]
    
    // 获取工作表
    const worksheet = workbook.Sheets[firstSheetName]
    
    // 将工作表转换为JSON对象数组
    const jsonData = XLSX.utils.sheet_to_json(worksheet, { header: 1 })
    
    // 提取表头和数据
    if (jsonData.length > 0) {
      const headers = jsonData[0]
      const dataRows = jsonData.slice(1)
      
      // 将数据行转换为对象数组
      const dataItems = dataRows.map(row => {
        const item = {}
        headers.forEach((header, index) => {
          if (header && index < row.length) {
            item[header] = row[index] !== undefined ? String(row[index]) : ''
          }
        })
        return item
      })
      
      // 保存表头到元数据
      form.metadata.headers = headers
      
      // 返回处理后的数据
      return {
        headers,
        dataItems
      }
    }
    
    return { headers: [], dataItems: [] }
  } catch (error) {
    console.error('处理Excel数据失败:', error)
    return { headers: [], dataItems: [] }
  }
}

// 处理文件变更
const handleFileChange = async (file) => {
  // 验证文件类型
  const isExcel = file.raw.type === 'application/vnd.ms-excel' || 
                 file.raw.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
  if (!isExcel) {
    ElMessage.warning('请上传Excel格式的文件（.xls或.xlsx）')
    return false
  }
  
  // 设置实体名称为文件名（不带扩展名）
  const fileName = file.name
  const fileNameWithoutExt = fileName.substring(0, fileName.lastIndexOf('.')) || fileName
  form.entity = fileNameWithoutExt
  
  // 如果数据名称为空，也用文件名填充
  if (!form.metadata.dataName) {
    form.metadata.dataName = fileNameWithoutExt
  }
  
  // 显示上传中提示
  ElMessage.info('正在上传Excel文件，请稍候...')
  
  try {
    // 1. 上传文件到后端
    const uploadResult = await excelUploadService.uploadExcelFile(file.raw)
    
    if (uploadResult.success) {
      ElMessage.success(`已成功上传Excel表格"${fileName}"`)
      
      // 2. 同时保存本地副本用于前端预览
      const reader = new FileReader()
      reader.onload = (e) => {
        try {
          // 保存文件的二进制数据用于前端预览
          form.excelData = e.target.result
          
          // 处理Excel数据并提取表头和数据项
          const { headers, dataItems } = processExcelData(e.target.result)
          
          // 保存提取的数据项到表单中
          form.dataItems = dataItems
        } catch (error) {
          console.error('读取Excel文件失败:', error)
        }
      }
      reader.onerror = () => {
        console.error('读取文件失败')
      }
      reader.readAsBinaryString(file.raw)
      
      // 3. 如果后端返回了文件URL或base64数据，也可以保存
      if (uploadResult.data && uploadResult.data.excelData) {
        // 如果后端返回的是URL或base64，可以在这里处理
        // console.log('后端返回的Excel数据:', uploadResult.data.excelData)
      }
    } else {
      ElMessage.error(`上传失败: ${uploadResult.message}`)
    }
  } catch (error) {
    console.error('Excel文件上传过程出错:', error)
    ElMessage.error('上传Excel文件过程中出错')
    
    // 上传失败时，仍然尝试本地读取用于预览
    const reader = new FileReader()
    reader.onload = (e) => {
      form.excelData = e.target.result
      
      // 处理Excel数据并提取表头和数据项
      const { headers, dataItems } = processExcelData(e.target.result)
      
      // 保存提取的数据项到表单中
      form.dataItems = dataItems
    }
    reader.readAsBinaryString(file.raw)
  }
}

// 保存按钮处理
const handleSave = () => {
  // 简单验证
  if (!form.entity) {
    ElMessage.warning('请输入实体名称或上传Excel表格文件')
    return
  }
  
  if (!form.locationInfo.row || !form.locationInfo.col) {
    ElMessage.warning('请输入定位信息（行和列）')
    return
  }
  
  // 验证约束条件
  if (!form.formatConstraint) {
    ElMessage.warning('请选择格式约束')
    return
  }
  
  if (!form.accessConstraint) {
    ElMessage.warning('请选择访问权限')
    return
  }
  
  if (!form.pathConstraint) {
    ElMessage.warning('请选择传输路径约束')
    return
  }
  
  if (!form.regionConstraint) {
    ElMessage.warning('请选择地域性约束')
    return
  }
  
  if (!form.shareConstraint) {
    ElMessage.warning('请选择共享约束')
    return
  }
  
  // 验证表单
  formRef.value.validate(async (valid) => {
    if (valid) {
      // 如果处于离线模式，显示警告
      if (offlineMode.value) {
        try {
          await ElMessageBox.confirm(
            '当前处于离线模式，修改只会保存到本地，不会同步到服务器。确定要继续吗？',
            '离线保存警告',
            {
              confirmButtonText: '继续保存',
              cancelButtonText: '取消',
              type: 'warning'
            }
          )
        } catch (e) {
          // 用户取消了操作
          return
        }
      }
      
      // 构建约束条件数组
      const constraintArray = []
      if (form.formatConstraint) constraintArray.push(`格式约束:${form.formatConstraint}`)
      if (form.accessConstraint) constraintArray.push(`访问权限:${form.accessConstraint}`)
      if (form.pathConstraint) constraintArray.push(`传输路径约束:${form.pathConstraint}`)
      if (form.regionConstraint) constraintArray.push(`地域性约束:${form.regionConstraint}`)
      if (form.shareConstraint) constraintArray.push(`共享约束:${form.shareConstraint}`)
      
      // 构建传播控制对象，与transferControl数组对应
      const propagationControl = {
        canRead: form.transferControl.includes('可读'),
        canModify: form.transferControl.includes('可修改'),
        canDestroy: form.transferControl.includes('可销毁'),
        canShare: form.transferControl.includes('可共享'),
        canDelegate: form.transferControl.includes('可委托')
      }
      
      // 如果没有处理Excel数据，再次处理
      if (!form.dataItems && form.excelData) {
        const { dataItems } = processExcelData(form.excelData)
        form.dataItems = dataItems
      }
      
      // 构建更新后的对象
      const updatedObject = {
        id: form.id,
        entity: form.entity,
        locationInfo: {
          row: form.locationInfo.row,
          col: form.locationInfo.col
        },
        metadata: {
          dataName: form.metadata.dataName,
          sourceUnit: form.metadata.sourceUnit,
          contactPerson: form.metadata.contactPerson,
          contactPhone: form.metadata.contactPhone,
          resourceSummary: form.metadata.resourceSummary,
          fieldClassification: form.metadata.fieldClassification,
          headers: form.metadata.headers || []
        },
        constraint: constraintArray,
        formatConstraint: form.formatConstraint,
        accessConstraint: form.accessConstraint,
        pathConstraint: form.pathConstraint,
        regionConstraint: form.regionConstraint,
        shareConstraint: form.shareConstraint,
        transferControl: form.transferControl,
        propagationControl: propagationControl,
        auditInfo: form.auditInfo,
        status: form.status,
        feedback: form.feedback,
        excelData: form.excelData,
        dataItems: form.dataItems || [], // 添加数据项
        offlineMode: offlineMode.value // 标记是否为离线模式
      }
      
      // 发送保存事件
      emit('save', updatedObject)
      
      // 关闭对话框
      dialogVisible.value = false
      
      // 发送导航回主页的事件
      emit('navigate-home')
    } else {
      // 显示验证错误
      ElMessage.warning('请填写必填字段')
      return false
    }
  })
}

// 取消按钮处理
const handleCancel = () => {
  dialogVisible.value = false
  emit('cancel')
}

// 对话框关闭处理
const handleDialogClosed = () => {
  // 重置表单验证
  if (formRef.value) {
    formRef.value.resetFields()
  }
  
  // 重置离线模式状态
  offlineMode.value = false
  apiError.value = null
}
</script>

<style scoped>
/* 对话框样式 */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

:deep(.custom-dialog) {
  margin-top: 15vh !important;
}

/* 自定义选择控件样式 */
.custom-select-wrapper {
  position: relative;
  width: 100%;
  min-height: 40px;
  display: flex;
  align-items: center;
}

.custom-select-component {
  width: 100%;
  min-width: 180px; /* 确保选择框足够宽 */
}

/* 选中值显示 */
.selected-value {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 38px;
  display: flex;
  align-items: center;
  padding-left: 12px;
  color: #409EFF;
  font-weight: bold;
  background-color: #fff;
  border-radius: 4px;
  border: 1px solid #dcdfe6;
  z-index: 1;
  pointer-events: none; /* 允许点击穿透到下面的select */
}

/* 覆盖Element Plus的下拉箭头样式 */
:deep(.custom-select-component .el-input__wrapper) {
  z-index: 2;
  opacity: 0.01; /* 几乎透明但仍可交互 */
}

:deep(.custom-select-component .el-input__inner) {
  opacity: 0;
}

/* 添加自定义下拉箭头 */
.custom-select-wrapper::after {
  content: '▼';
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  color: #409EFF;
  font-size: 14px;
  z-index: 3;
  pointer-events: none;
}

/* 上传提示样式 */
.upload-tip {
  color: #909399;
  font-size: 14px;
}

/* 约束条件样式 */
.constraint-section {
  display: flex;
  flex-direction: column;
  gap: 15px;
  margin-bottom: 10px;
}

.constraint-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.constraint-item label {
  min-width: 110px;
  text-align: right;
  font-size: 14px;
  color: #606266;
}

.constraint-item .el-select {
  flex: 1;
}

/* 传输控制操作样式 */
.custom-multi-select-wrapper {
  position: relative;
  width: 100%;
  min-height: 40px;
  display: flex;
  flex-direction: column;
}

.custom-multi-select {
  width: 100%;
  min-width: 180px; /* 确保选择框足够宽 */
}

/* 添加元数据相关样式 */
.metadata-form-title {
  margin-bottom: 10px;
}

.metadata-form-item {
  margin-left: 20px;
  margin-bottom: 15px;
}

.metadata-form-item :deep(.el-form-item__label) {
  font-weight: normal;
  color: #606266;
}

:deep(.el-divider__text) {
  font-size: 14px;
  color: #409eff;
  background-color: #f5f7fa;
}
</style> 