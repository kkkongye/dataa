<template>
  <div class="api-test-container">
    <h1>后端API连接测试工具</h1>
    
    <div class="test-card">
      <h2>1. 检查API连接</h2>
      <div class="action-row">
        <el-button type="primary" @click="testApiConnection" :loading="testingConnection">
          测试连接
        </el-button>
        <span class="result" :class="{ success: connectionStatus.success, error: !connectionStatus.success }">
          {{ connectionStatus.message }}
        </span>
      </div>
      <div v-if="connectionStatus.details" class="details-panel">
        <pre>{{ JSON.stringify(connectionStatus.details, null, 2) }}</pre>
      </div>
    </div>
    
    <div class="test-card">
      <h2>2. 测试文件上传</h2>
      <div class="upload-box">
        <el-upload
          action="#"
          :auto-upload="false"
          :show-file-list="true"
          :on-change="handleFileSelected"
          accept=".xlsx,.xls"
        >
          <el-button type="primary">
            选择Excel文件
          </el-button>
        </el-upload>
      </div>
      
      <div class="action-row" v-if="selectedFile">
        <div>
          已选择: {{ selectedFile.name }} ({{ formatFileSize(selectedFile.size) }})
        </div>
        <el-button type="success" @click="testFileUpload" :loading="uploadingFile" :disabled="!selectedFile">
          测试上传
        </el-button>
      </div>
      
      <div v-if="uploadStatus.message" class="result-panel" :class="{ success: uploadStatus.success, error: !uploadStatus.success }">
        <h3>上传结果:</h3>
        <p>{{ uploadStatus.message }}</p>
        
        <div v-if="uploadStatus.details" class="details-panel">
          <h4>详细信息:</h4>
          <pre>{{ JSON.stringify(uploadStatus.details, null, 2) }}</pre>
        </div>
      </div>
    </div>
    
    <div class="test-card">
      <h2>3. API配置信息</h2>
      <div class="config-info">
        <p><strong>Base URL:</strong> {{ API_BASE_URL }}</p>
        <p><strong>API URL:</strong> {{ API_URL }}</p>
        <p><strong>上传文件接口:</strong> {{ API_URL }}/objects/excel</p>
        <p><strong>特定对象上传文件接口:</strong> {{ API_URL }}/objects/{id}/excel</p>
      </div>
    </div>
    
    <div class="test-card">
      <h2>4. 故障排除</h2>
      <div class="troubleshoot-info">
        <h3>无法连接到后端?</h3>
        <ul>
          <li>确认后端服务器是否正在运行</li>
          <li>检查API基础URL是否正确</li>
          <li>验证后端是否已实现Excel上传接口</li>
          <li>检查网络连接（例如防火墙设置）</li>
          <li>可能存在跨域(CORS)问题，需要后端配置允许跨域请求</li>
        </ul>
        
        <h3>上传失败?</h3>
        <ul>
          <li>确认上传文件大小不超过后端限制</li>
          <li>检查后端是否正确处理multipart/form-data请求</li>
          <li>确认文件是否为有效的Excel格式</li>
          <li>检查表单中"file"字段名称是否与后端期望的一致</li>
        </ul>
        
        <h3>要检查的Console错误:</h3>
        <ul>
          <li>网络错误: 通常是连接问题</li>
          <li>CORS错误: 表示跨域权限问题</li>
          <li>4xx错误: 客户端错误，例如格式不正确或权限不足</li>
          <li>5xx错误: 服务器错误，通常是后端实现问题</li>
        </ul>
      </div>
    </div>
    
    <div class="footer-actions">
      <el-button @click="goBack">返回主页</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import excelUploadService from '@/services/excelUploadService'
import { API_URL, API_BASE_URL } from '@/services/apiConfig'

const router = useRouter()

// 连接测试状态
const testingConnection = ref(false)
const connectionStatus = reactive({
  success: false,
  message: '未测试',
  details: null
})

// 文件上传测试状态
const uploadingFile = ref(false)
const selectedFile = ref(null)
const uploadStatus = reactive({
  success: false,
  message: '',
  details: null
})

// 测试API连接
const testApiConnection = async () => {
  testingConnection.value = true
  connectionStatus.message = '正在测试连接...'
  connectionStatus.details = null
  
  try {
    const result = await excelUploadService.testApiConnection()
    connectionStatus.success = result.success
    connectionStatus.message = result.message
    connectionStatus.details = result.details
  } catch (error) {
    connectionStatus.success = false
    connectionStatus.message = `连接测试出错: ${error.message}`
    connectionStatus.details = { error: error.toString() }
  } finally {
    testingConnection.value = false
  }
}

// 处理文件选择
const handleFileSelected = (file) => {
  selectedFile.value = file.raw
  // 重置上传状态
  uploadStatus.message = ''
  uploadStatus.details = null
}

// 测试文件上传
const testFileUpload = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择Excel文件')
    return
  }
  
  uploadingFile.value = true
  uploadStatus.message = '正在上传文件...'
  uploadStatus.details = null
  
  try {
    const result = await excelUploadService.uploadExcelFile(selectedFile.value)
    uploadStatus.success = result.success
    uploadStatus.message = result.message
    uploadStatus.details = result.details || result.data
  } catch (error) {
    uploadStatus.success = false
    uploadStatus.message = `上传测试出错: ${error.message}`
    uploadStatus.details = { error: error.toString() }
  } finally {
    uploadingFile.value = false
  }
}

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (bytes < 1024) return bytes + ' bytes'
  else if (bytes < 1048576) return (bytes / 1024).toFixed(2) + ' KB'
  else return (bytes / 1048576).toFixed(2) + ' MB'
}

// 返回主页
const goBack = () => {
  router.push('/source')
}
</script>

<style scoped>
.api-test-container {
  max-width: 1000px;
  margin: 20px auto;
  padding: 20px;
  font-family: 'Arial', sans-serif;
}

h1 {
  color: #333;
  margin-bottom: 25px;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
}

.test-card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
  padding: 20px;
  margin-bottom: 20px;
}

h2 {
  color: #409EFF;
  font-size: 18px;
  margin-top: 0;
  margin-bottom: 15px;
}

.action-row {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.result {
  margin-left: 15px;
  font-weight: bold;
}

.success {
  color: #67C23A;
}

.error {
  color: #F56C6C;
}

.details-panel {
  background: #f8f8f8;
  border-radius: 4px;
  padding: 10px;
  margin-top: 10px;
  font-family: 'Courier New', monospace;
  font-size: 13px;
  overflow-x: auto;
}

pre {
  margin: 0;
  white-space: pre-wrap;
}

.upload-box {
  padding: 20px;
  border: 1px dashed #d9d9d9;
  border-radius: 4px;
  margin-bottom: 15px;
  text-align: center;
}

.result-panel {
  padding: 15px;
  border-radius: 4px;
  margin-top: 15px;
}

.result-panel.success {
  background: rgba(103, 194, 58, 0.1);
  border: 1px solid rgba(103, 194, 58, 0.2);
}

.result-panel.error {
  background: rgba(245, 108, 108, 0.1);
  border: 1px solid rgba(245, 108, 108, 0.2);
}

.config-info, .troubleshoot-info {
  line-height: 1.6;
}

.troubleshoot-info h3 {
  margin-top: 20px;
  color: #606266;
  font-size: 16px;
}

.troubleshoot-info ul {
  padding-left: 20px;
}

.footer-actions {
  margin-top: 30px;
  text-align: center;
}
</style> 