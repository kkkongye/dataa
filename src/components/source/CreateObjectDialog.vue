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
    <el-form :model="form" label-width="150px" ref="formRef" :rules="formRules">
      <el-form-item label="实体：" prop="entity" style="margin-bottom: 22px;">
        <div style="display: flex; align-items: center; gap: 10px;">
          <el-input v-model="form.entity" placeholder="请输入实体名称" style="width: 300px;"></el-input>
          <el-upload
            action="#"
            :auto-upload="false"
            :show-file-list="false"
            :on-change="handleFileChange"
            accept=".xlsx,.xls"
            style="margin-left: 10px;"
            :disabled="loading"
          >
            <el-button type="primary" :loading="loading">上传Excel</el-button>
          </el-upload>
          <el-tag v-if="uploadSuccess" type="success" size="small">已上传</el-tag>
        </div>
      </el-form-item>
      
      <!-- 元数据区域 -->
      <el-form-item label="数据名称：" prop="metadata.dataName" style="margin-bottom: 22px;">
        <el-input v-model="form.metadata.dataName" placeholder="请输入数据名称" style="width: 300px;"></el-input>
      </el-form-item>
      <el-form-item label="来源单位：" prop="metadata.sourceUnit" style="margin-bottom: 22px;">
        <el-input v-model="form.metadata.sourceUnit" placeholder="请输入来源单位" style="width: 300px;"></el-input>
      </el-form-item>
      <el-form-item label="联系人：" prop="metadata.contactPerson" style="margin-bottom: 22px;">
        <el-input v-model="form.metadata.contactPerson" placeholder="请输入联系人" style="width: 300px;"></el-input>
      </el-form-item>
      <el-form-item label="联系电话：" prop="metadata.contactPhone" style="margin-bottom: 22px;">
        <el-input v-model="form.metadata.contactPhone" placeholder="请输入联系电话" style="width: 300px;"></el-input>
      </el-form-item>
      <el-form-item label="资源摘要：" prop="metadata.resourceSummary" style="margin-bottom: 22px;">
        <el-input v-model="form.metadata.resourceSummary" placeholder="请输入资源摘要" style="width: 300px;"></el-input>
      </el-form-item>
      <el-form-item label="领域分类：" prop="metadata.fieldClassification" style="margin-bottom: 22px;">
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
                <el-option label="json" value="json"></el-option>
                <el-option label="csv" value="csv"></el-option>
                <el-option label="pdf" value="pdf"></el-option>
                <el-option label="txt" value="txt"></el-option>
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
        <el-button @click="handleCancel" :disabled="loading">取消</el-button>
        <el-button type="primary" @click="handleSave" :disabled="loading">确定</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch, defineProps, defineEmits } from 'vue'
import { ElMessage, ElLoading } from 'element-plus'
import { Document } from '@element-plus/icons-vue'
import excelUploadService from '@/services/excelUploadService'
import * as XLSX from 'xlsx' // 确保引入XLSX库用于处理Excel数据
import axios from 'axios'
import { API_URL } from '@/services/apiConfig'

const props = defineProps({
  // 是否显示对话框
  visible: {
    type: Boolean,
    default: false
  },
  // 对话框标题
  title: {
    type: String,
    default: '新建数字对象'
  },
  // 表单数据
  modelValue: {
    type: Object,
    default: () => ({
      entity: '',
      locationInfo: {
        row: '',
        col: ''
      },
      constraint: [],
      transferControl: [],
      excelData: null
    })
  }
})

const emit = defineEmits(['update:visible', 'update:modelValue', 'save', 'cancel'])

// 表单引用
const formRef = ref(null)

// 对话框可见性状态
const dialogVisible = ref(false)

// 加载状态
const loading = ref(false)
const loadingText = ref('')

// 上传成功标志
const uploadSuccess = ref(false)

// 表单数据
const form = reactive({
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
  excelData: null,
  dataItems: [],
  excelFileId: null
})

// 表单校验规则
const formRules = {
  entity: [
    { required: false, message: '请上传Excel文件', trigger: 'change' }
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
        // 只要有行或列信息就通过验证
        if (form.locationInfo.row || form.locationInfo.col) {
          callback()
        } else {
          // 如果都没有提供，设置默认值
          form.locationInfo.row = '1-100'
          form.locationInfo.col = 'A-Z'
          callback()
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

// 监听visible属性变化
watch(() => props.visible, (newVal) => {
  dialogVisible.value = newVal
  if (newVal) {
    // 当对话框打开时，重置表单数据
    resetForm()
  }
})

// 监听dialogVisible变化
watch(dialogVisible, (newVal) => {
  emit('update:visible', newVal)
})

// 监听modelValue变化
watch(() => props.modelValue, (newVal) => {
  if (!newVal) return
  
  // 深拷贝对象，避免直接修改props
  form.entity = newVal.entity || ''
  if (newVal.locationInfo && typeof newVal.locationInfo === 'object') {
    form.locationInfo.row = newVal.locationInfo.row || ''
    form.locationInfo.col = newVal.locationInfo.col || ''
  }
  
  // 设置元数据
  if (newVal.metadata && typeof newVal.metadata === 'object') {
    form.metadata.dataName = newVal.metadata.dataName || newVal.entity || ''
    form.metadata.sourceUnit = newVal.metadata.sourceUnit || ''
    form.metadata.contactPerson = newVal.metadata.contactPerson || ''
    form.metadata.contactPhone = newVal.metadata.contactPhone || ''
    form.metadata.resourceSummary = newVal.metadata.resourceSummary || ''
    form.metadata.fieldClassification = newVal.metadata.fieldClassification || ''
  } else {
    // 如果没有元数据，则使用实体名称和默认值
    form.metadata.dataName = newVal.entity || ''
    form.metadata.sourceUnit = ''
    form.metadata.contactPerson = ''
    form.metadata.contactPhone = ''
    form.metadata.resourceSummary = ''
    form.metadata.fieldClassification = ''
  }
  
  // 设置约束条件数组
  form.constraint = Array.isArray(newVal.constraint) ? [...newVal.constraint] : (newVal.constraint ? [newVal.constraint] : [])
  
  // 设置各个约束条件字段
  if (newVal.formatConstraint) form.formatConstraint = newVal.formatConstraint
  if (newVal.accessConstraint) form.accessConstraint = newVal.accessConstraint
  if (newVal.pathConstraint) form.pathConstraint = newVal.pathConstraint
  if (newVal.regionConstraint) form.regionConstraint = newVal.regionConstraint
  if (newVal.shareConstraint) form.shareConstraint = newVal.shareConstraint
  
  // 传输控制
  form.transferControl = Array.isArray(newVal.transferControl) ? [...newVal.transferControl] : (newVal.transferControl ? [newVal.transferControl] : [])
  
  form.excelData = newVal.excelData
}, { deep: true })

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
      fieldClassification: newVal.metadata.fieldClassification
    },
    constraint: constraintArray,
    formatConstraint: newVal.formatConstraint,
    accessConstraint: newVal.accessConstraint,
    pathConstraint: newVal.pathConstraint,
    regionConstraint: newVal.regionConstraint,
    shareConstraint: newVal.shareConstraint,
    transferControl: newVal.transferControl,
    excelData: newVal.excelData
  })
}, { deep: true })

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
const handleFileChange = (file) => {
  // 验证文件类型
  const isExcel = file.raw.type === 'application/vnd.ms-excel' || 
                 file.raw.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
  if (!isExcel) {
    ElMessage.warning('请上传Excel格式的文件（.xls或.xlsx）')
    return false
  }
  
  // 如果没有手动输入实体名称，则使用文件名作为实体名称
  const fileName = file.name
  const fileNameWithoutExt = fileName.substring(0, fileName.lastIndexOf('.')) || fileName
  
  if (!form.entity) {
    form.entity = fileNameWithoutExt
  }

  // 显示上传中状态
  loading.value = true
  loadingText.value = '正在处理Excel文件...'
  
  const loadingInstance = ElLoading.service({
    text: loadingText.value,
    background: 'rgba(0, 0, 0, 0.7)'
  })
  
  // 使用统一服务上传文件
  import('@/services/dataObjectService').then(async (module) => {
    const dataObjectService = module.default
    
    try {
      // 先在本地读取解析Excel，获取表头和数据
      const reader = new FileReader()
      reader.onload = async (e) => {
        try {
          // 本地解析Excel数据
          const excelData = e.target.result
          const excelResult = processExcelData(excelData)
          
          if (!excelResult || !excelResult.headers || excelResult.headers.length === 0) {
            ElMessage.error('Excel文件解析失败，请检查文件格式')
            loading.value = false
            loadingInstance.close()
            return
          }
          
          // 保存本地解析的数据到表单
          form.excelData = excelData
          form.metadata.headers = excelResult.headers
          form.dataItems = excelResult.dataItems
          
          // 上传Excel文件到服务器并获取临时对象
          const uploadResult = await dataObjectService.uploadExcelFile(file.raw)
          
          console.log('Excel上传结果:', uploadResult)
          
          if (uploadResult.success) {
            uploadSuccess.value = true
            
            // 处理临时对象数据
            if (uploadResult.data) {
              // 优先提取excelFileId
              if (uploadResult.data.excelFileId) {
                form.excelFileId = uploadResult.data.excelFileId
                console.log('获取到Excel文件ID:', form.excelFileId)
              } else if (uploadResult.data.id) {
                form.excelFileId = uploadResult.data.id
                console.log('从临时对象ID获取文件ID:', form.excelFileId)
              }
              
              // 只有在用户未输入任何元数据字段时，才考虑使用服务器元数据
              if (uploadResult.data.metadata && 
                  (!form.metadata.dataName && !form.metadata.sourceUnit && 
                   !form.metadata.contactPerson && !form.metadata.contactPhone && 
                   !form.metadata.resourceSummary && !form.metadata.fieldClassification)) {
                console.log('用户未输入元数据，使用服务器返回的默认元数据')
                // 保留表头信息，其他使用服务器返回的数据
                const headers = form.metadata.headers || []
                form.metadata = {
                  ...uploadResult.data.metadata,
                  headers: headers
                }
                console.log('从临时对象获取元数据')
              } else {
                console.log('保留用户输入的元数据信息')
              }
              
              // 提取实体名称，只在用户未输入时使用
              if (uploadResult.data.entity && !form.entity) {
                form.entity = uploadResult.data.entity
                console.log('从临时对象获取实体名称:', form.entity)
              }
            }
            
            ElMessage.success('Excel文件上传并处理成功')
          } else {
            // 上传可能失败，但我们仍有本地解析的数据
            console.warn('Excel上传失败，但已解析本地数据:', uploadResult.message)
            
            // 设置一个本地ID
            form.excelFileId = `local-${Date.now()}`
            
            // 虽然服务器上传失败，但本地已解析数据，标记为成功
            uploadSuccess.value = true
            
            ElMessage.warning(`Excel文件上传到服务器失败，将使用本地解析结果: ${uploadResult.message}`)
          }
        } catch (error) {
          console.error('Excel处理错误:', error)
          ElMessage.error('处理Excel文件时出错')
          uploadSuccess.value = false
        } finally {
          loading.value = false
          loadingInstance.close()
        }
      }
      
      reader.onerror = () => {
        ElMessage.error('读取文件失败')
        loading.value = false
        loadingInstance.close()
      }
      
      // 开始读取文件
      reader.readAsBinaryString(file.raw)
    } catch (error) {
      console.error('处理文件时出错:', error)
      ElMessage.error('处理Excel文件时出错')
      loading.value = false
      loadingInstance.close()
    }
  }).catch(error => {
    console.error('加载dataObjectService服务失败:', error)
    ElMessage.error('加载服务失败')
    loading.value = false
    loadingInstance.close()
  })
  
  return false // 阻止自动上传
}

// 修改保存按钮处理逻辑，确保Excel文件已上传
const handleSave = () => {
  console.log('【保存开始】处理保存按钮点击');
  
  // 简单验证
  if (!form.entity) {
    ElMessage.warning('请输入实体名称或上传Excel表格文件')
    return
  }
  
  // 确保locationInfo有值，如果没有则设置默认值
  if (!form.locationInfo.row) {
    form.locationInfo.row = '1-100'
  }
  if (!form.locationInfo.col) {
    form.locationInfo.col = 'A-Z'
  }
  
  if (!form.excelData) {
    ElMessage.warning('请上传Excel表格文件')
    return
  }
  
  if (!uploadSuccess.value) {
    ElMessage.warning('请确保Excel文件已成功上传到服务器')
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
  
  // 确保有excelFileId，如果没有则创建一个
  if (!form.excelFileId) {
    form.excelFileId = `autogen-${Date.now()}`
    console.log('自动生成excelFileId:', form.excelFileId)
  }
  
  // 【增加调试信息】记录表单中的当前元数据
  console.log('【元数据状态】验证前表单中的元数据:');
  console.log('- dataName:', form.metadata.dataName);
  console.log('- sourceUnit:', form.metadata.sourceUnit);
  console.log('- contactPerson:', form.metadata.contactPerson);
  console.log('- contactPhone:', form.metadata.contactPhone);
  console.log('- resourceSummary:', form.metadata.resourceSummary);
  console.log('- fieldClassification:', form.metadata.fieldClassification);
  
  formRef.value.validate((valid) => {
    if (valid) {
      console.log('【表单验证】表单验证通过，准备保存数据');
      
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
      
      // 【重要修改】创建用户元数据对象副本，并添加特殊标记
      const userInputMetadata = {
        dataName: form.metadata.dataName || form.entity || '',
        sourceUnit: form.metadata.sourceUnit || '',
        contactPerson: form.metadata.contactPerson || '',
        contactPhone: form.metadata.contactPhone || '',
        resourceSummary: form.metadata.resourceSummary || '',
        fieldClassification: form.metadata.fieldClassification || '',
        headers: Array.isArray(form.metadata.headers) ? [...form.metadata.headers] : [],
        _userInput: true, // 特殊标记，表示这是用户输入的元数据
        _inputTimestamp: Date.now() // 添加时间戳
      };
      
      // 记录元数据对象
      console.log('【元数据准备】已创建用户元数据对象:', JSON.stringify(userInputMetadata));
      
      // 将元数据转换为JSON字符串，确保格式正确
      const metadataJsonString = JSON.stringify(userInputMetadata);
      
      // 检查JSON格式是否正确
      try {
        // 验证JSON格式
        JSON.parse(metadataJsonString);
        console.log('【元数据验证】元数据JSON格式正确');
      } catch (e) {
        console.error('【元数据错误】元数据JSON格式错误:', e);
        ElMessage.error('元数据格式错误，请检查输入');
        return;
      }
      
      // 【新增】确保元数据格式符合后端预期
      // 检查headers是否为数组
      if (!Array.isArray(userInputMetadata.headers)) {
        if (typeof userInputMetadata.headers === 'string') {
          // 如果是字符串，尝试转换为数组
          userInputMetadata.headers = userInputMetadata.headers.split(/[,，]/);
          console.log('【元数据修复】将headers字符串转换为数组:', userInputMetadata.headers);
        } else {
          // 设置为空数组
          userInputMetadata.headers = [];
          console.log('【元数据修复】将invalid headers设为空数组');
        }
      }
      
      // 确保metadataJson字段独立存在
      const metadataForBackend = { ...userInputMetadata };
      
      // 【重要修改】构建新对象，确保元数据被保留
      const newObject = {
        entity: form.entity,
        locationInfo: {
          row: form.locationInfo.row,
          col: form.locationInfo.col
        },
        // 使用用户输入的元数据
        metadata: {...userInputMetadata},
        // 添加原始元数据字段，确保在处理过程中不会丢失
        originalMetadata: {...userInputMetadata},
        metadataJson: metadataJsonString,
        _preserveUserMetadata: true, // 特殊标记，指示应保留用户元数据
        constraint: constraintArray,
        formatConstraint: form.formatConstraint,
        accessConstraint: form.accessConstraint,
        pathConstraint: form.pathConstraint,
        regionConstraint: form.regionConstraint,
        shareConstraint: form.shareConstraint,
        transferControl: form.transferControl,
        propagationControl: propagationControl,
        status: '待检验',
        feedback: '',
        excelData: form.excelData,
        dataItems: form.dataItems || [],
        excelFileId: form.excelFileId
      };
      
      // 创建dataContent字段，确保包含完整元数据
      try {
        // 确保明确包含元数据字段
        const dataContent = {
          entity: newObject.entity,
          status: newObject.status,
          // 使用用户输入的元数据
          metadata: {...userInputMetadata},
          // 保留原始元数据
          originalMetadata: {...userInputMetadata},
          // 独立的metadataJson字段，确保后端可以正确识别
          metadataJson: metadataJsonString,
          _preserveUserMetadata: true,
          dataItems: newObject.dataItems,
          excelFileId: newObject.excelFileId
        };
        
        // 验证dataContent对象中的元数据是否完整
        console.log('【dataContent】dataContent中的元数据字段:', Object.keys(dataContent.metadata).join(', '));
        
        // 将整个dataContent转为字符串
        newObject.dataContent = JSON.stringify(dataContent);
        
        // 验证dataContent中是否包含元数据
        console.log('【验证】dataContent是否包含metadata字段:', 
          newObject.dataContent.includes('"metadata":'));
        
        // 检查具体元数据字段是否包含在dataContent中
        if (userInputMetadata.dataName) {
          console.log(`【验证】dataContent是否包含dataName(${userInputMetadata.dataName}):`, 
            newObject.dataContent.includes(userInputMetadata.dataName));
        }
        if (userInputMetadata.sourceUnit) {
          console.log(`【验证】dataContent是否包含sourceUnit(${userInputMetadata.sourceUnit}):`, 
            newObject.dataContent.includes(userInputMetadata.sourceUnit));
        }
      } catch (error) {
        console.error('【错误】创建dataContent失败:', error);
      }
      
      // 在发送前再次检查元数据完整性
      console.log('【最终检查】newObject.metadata:', JSON.stringify(newObject.metadata));
      console.log('【最终检查】newObject.originalMetadata:', JSON.stringify(newObject.originalMetadata));
      
      // 发送保存事件
      console.log('【保存流程】触发save事件，保存对象到后端');
      
      // 添加事件前后的标记，以便追踪元数据是否被修改
      emit('save', newObject);
      
      console.log('【保存流程】save事件已触发完成');
      
      // 关闭对话框
      dialogVisible.value = false;
      
      // 显示成功消息
      ElMessage.success('数字对象创建成功');
      
      // 重置表单
      resetForm();
    } else {
      ElMessage.warning('请填写必填字段')
      return false
    }
  })
}

// 重置表单时也重置上传状态
const resetForm = () => {
  form.entity = ''
  form.locationInfo.row = ''
  form.locationInfo.col = ''
  form.metadata.dataName = ''
  form.metadata.sourceUnit = ''
  form.metadata.contactPerson = ''
  form.metadata.contactPhone = ''
  form.metadata.resourceSummary = ''
  form.metadata.fieldClassification = ''
  form.constraint = []
  form.formatConstraint = ''
  form.accessConstraint = ''
  form.pathConstraint = ''
  form.regionConstraint = ''
  form.shareConstraint = ''
  form.transferControl = []
  form.excelData = null
  form.dataItems = []
  form.excelFileId = null
  
  // 重置上传状态
  uploadSuccess.value = false
  
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

// 取消按钮处理
const handleCancel = () => {
  dialogVisible.value = false
  emit('cancel')
  resetForm()
}

// 对话框关闭处理
const handleDialogClosed = () => {
  // 只有在非编辑模式下才重置表单
  if (!props.modelValue.id) {
    resetForm()
  }
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