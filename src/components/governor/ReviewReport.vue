<template>
  <!-- 审核报告弹窗 -->
  <el-dialog
    :model-value="visible"
    @update:model-value="emit('update:visible', $event)"
    title="审核报告"
    width="70%"
    :close-on-click-modal="false"
    :show-close="true"
    draggable
    class="report-dialog"
    top="5vh"
  >
    <template #header>
      <div class="dialog-header">
        <h3>审核报告</h3>
        <p class="upload-prompt">上传TXT格式报告文件</p>
      </div>
    </template>
    
    <div class="report-container">
      <div v-if="!reportContent" class="upload-area">
        <el-upload
          class="upload-component"
          action="#"
          :auto-upload="false"
          :show-file-list="false"
          accept=".txt"
          @change="handleFileChange"
        >
          <div class="upload-box">
            <el-icon class="upload-icon"><UploadFilled /></el-icon>
            <div class="upload-text">点击或拖拽文件到此处上传</div>
            <div class="upload-tip">支持 .txt 格式文件</div>
          </div>
        </el-upload>
      </div>
      
      <div v-else class="report-content">
        <div class="report-header">
          <span class="file-name">{{ selectedFileName }}</span>
          <el-button size="small" type="primary" plain @click="triggerFileSelect">上传文件</el-button>
        </div>
        
        <el-scrollbar class="report-scrollbar">
          <div class="txt-content" v-html="formattedReportContent"></div>
        </el-scrollbar>
      </div>
      
      <!-- 隐藏的文件输入 -->
      <input
        type="file"
        ref="fileInput"
        style="display: none"
        accept=".txt"
        @change="handleFileChange"
      />
    </div>
    
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="closeDialog">关闭</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { UploadFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import reportService from '../../services/reportService'

// 使用v-model绑定对话框可见性
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:visible'])

// 内部状态
const reportContent = ref('')
const reportLoading = ref(false)
const fileInput = ref(null)
const selectedFileName = ref('')

// 处理对话框关闭
const closeDialog = () => {
  emit('update:visible', false)
  
  // 延迟清除报告内容，提供更好的用户体验
  setTimeout(() => {
    reportContent.value = ''
    selectedFileName.value = ''
  }, 300)
}

// 格式化TXT内容 - 处理换行符并高亮显示某些内容
const formattedReportContent = computed(() => {
  if (!reportContent.value) return ''
  
  // 替换换行符为HTML换行
  let formatted = reportContent.value
    .replace(/\n/g, '<br>')
    .replace(/\r/g, '')
  
  // 高亮错误相关的文本
  formatted = formatted
    .replace(/错误/g, '<span class="highlight-error">错误</span>')
    .replace(/失败/g, '<span class="highlight-error">失败</span>')
    .replace(/不合格/g, '<span class="highlight-error">不合格</span>')
    .replace(/问题/g, '<span class="highlight-warning">问题</span>')
  
  // 高亮成功相关的文本
  formatted = formatted
    .replace(/成功/g, '<span class="highlight-success">成功</span>')
    .replace(/通过/g, '<span class="highlight-success">通过</span>')
    .replace(/合格/g, '<span class="highlight-success">合格</span>')
  
  // 美化分隔线
  formatted = formatted
    .replace(/-{3,}/g, '<hr class="separator">')
    .replace(/={3,}/g, '<div class="strong-separator"></div>')
  
  // 高亮标题行
  formatted = formatted
    .replace(/^(.+?)(?=<br>)/g, '<h3 class="report-title">$1</h3>')
    .replace(/<br>(.+?)(?=<br>)/g, '<br><h4 class="report-subtitle">$1</h4>')
  
  return formatted
})

// 触发文件选择
const triggerFileSelect = () => {
  fileInput.value.click()
}

// 处理文件变更
const handleFileChange = (event) => {
  const file = event.target ? event.target.files[0] : event.file
  
  if (!file) {
    ElMessage.warning('未选择文件')
    return
  }
  
  // 验证文件类型
  if (file.type !== 'text/plain' && !file.name.endsWith('.txt')) {
    ElMessage.error('请选择.txt格式的文件')
    return
  }
  
  // 更新已选文件名
  selectedFileName.value = file.name
  
  // 显示加载状态
  reportLoading.value = true
  
  // 读取文件内容
  const reader = new FileReader()
  
  reader.onload = (e) => {
    reportContent.value = e.target.result
    reportLoading.value = false
  }
  
  reader.onerror = () => {
    ElMessage.error('读取文件失败')
    reportLoading.value = false
  }
  
  reader.readAsText(file)
}

// 加载初始报告数据
const loadInitialReport = async () => {
  try {
    reportLoading.value = true
    selectedFileName.value = '系统生成报告.txt'
    const data = await reportService.getDataIssuesReport()
    reportContent.value = data
  } catch (error) {
    console.error('加载初始报告失败:', error)
    ElMessage.error('加载系统报告失败')
  } finally {
    reportLoading.value = false
  }
}

// 监听对话框可见性，在打开时加载默认报告，在关闭时重置状态
watch(() => props.visible, (newValue) => {
  if (newValue) {
    // 如果是打开状态，且没有内容，则加载默认报告
    if (!reportContent.value) {
      loadInitialReport()
    }
  } else {
    // 如果是关闭状态，延迟清除内容，确保关闭动画完成
    setTimeout(() => {
      reportContent.value = ''
      selectedFileName.value = ''
    }, 300)
  }
})

// 组件挂载时尝试加载初始报告内容
onMounted(() => {
  if (props.visible && !reportContent.value) {
    loadInitialReport()
  }
})
</script>

<style scoped>
.report-dialog :deep(.el-dialog__header) {
  padding: 15px 20px;
  border-bottom: 1px solid #ebeef5;
  margin-right: 0;
}

.report-dialog :deep(.el-dialog__body) {
  padding: 20px;
  max-height: 60vh;
}

.report-dialog :deep(.el-dialog) {
  max-width: 900px;
  min-width: 600px;
}

.dialog-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
}

.dialog-header h3 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.upload-prompt {
  margin: 8px 0 0;
  font-size: 14px;
  color: #909399;
}

.report-container {
  min-height: 300px;
  display: flex;
  flex-direction: column;
}

.upload-area {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
}

.upload-component {
  width: 100%;
}

.upload-box {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  padding: 40px 0;
  text-align: center;
  background-color: #fafafa;
  transition: border-color 0.3s;
  cursor: pointer;
}

.upload-box:hover {
  border-color: #409eff;
}

.upload-icon {
  font-size: 48px;
  color: #c0c4cc;
  margin-bottom: 16px;
}

.upload-text {
  font-size: 16px;
  color: #606266;
  margin-bottom: 8px;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
}

.report-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.report-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 0 8px;
}

.file-name {
  font-size: 14px;
  color: #606266;
  font-weight: bold;
}

.report-scrollbar {
  height: 350px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  background-color: #fafafa;
}

.txt-content {
  padding: 16px;
  white-space: pre-wrap;
  font-family: 'Courier New', Courier, monospace;
  font-size: 14px;
  line-height: 1.8;
  color: #303133;
}

.highlight-error {
  color: #f56c6c;
  font-weight: bold;
  background-color: #fef0f0;
  padding: 0 4px;
  border-radius: 2px;
}

.highlight-warning {
  color: #e6a23c;
  font-weight: bold;
  background-color: #fdf6ec;
  padding: 0 4px;
  border-radius: 2px;
}

.highlight-success {
  color: #67c23a;
  font-weight: bold;
  background-color: #f0f9eb;
  padding: 0 4px;
  border-radius: 2px;
}

.report-title {
  color: #303133;
  font-size: 18px;
  margin: 8px 0;
  padding-bottom: 8px;
  border-bottom: 1px solid #dcdfe6;
}

.report-subtitle {
  color: #606266;
  font-size: 16px;
  margin: 16px 0 8px 0;
  padding-bottom: 4px;
  border-bottom: 1px dashed #ebeef5;
}

.separator {
  border: 0;
  height: 1px;
  background-color: #dcdfe6;
  margin: 10px 0;
}

.strong-separator {
  height: 2px;
  background-color: #409eff;
  margin: 15px 0;
  border-radius: 2px;
}

@media (max-width: 768px) {
  .report-dialog :deep(.el-dialog) {
    width: 95% !important;
    min-width: unset;
  }
  
  .report-scrollbar {
    height: 250px;
  }
}
</style> 