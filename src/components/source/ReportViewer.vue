<template>
  <!-- 审查报告查看器 -->
  <el-dialog
    :model-value="visible"
    @update:model-value="emit('update:visible', $event)"
    title="审查报告"
    width="70%"
    :close-on-click-modal="false"
    :show-close="true"
    draggable
    class="report-dialog"
    top="5vh"
  >
    <template #header>
      <div class="dialog-header">
        <h3>审查报告</h3>
        <p class="upload-prompt">{{ objectId ? `对象ID: ${objectId}` : '系统审查报告' }}</p>
      </div>
    </template>
    
    <div class="report-container">
      <div v-if="!reportContent" class="loading-area">
        <el-skeleton :rows="10" animated />
      </div>
      
      <div v-else class="report-content">
        <el-scrollbar class="report-scrollbar">
          <div class="txt-content" v-html="formattedReportContent"></div>
        </el-scrollbar>
      </div>
    </div>
    
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="closeDialog">关闭</el-button>
        <el-button type="primary" @click="exportReport">导出报告</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  objectId: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:visible'])

const reportContent = ref('')
const reportLoading = ref(false)

// 处理对话框关闭
const closeDialog = () => {
  emit('update:visible', false)
  
  setTimeout(() => {
    reportContent.value = ''
  }, 300)
}

// 导出报告
const exportReport = () => {
  try {
    // 创建一个纯文本版本的报告内容
    const plainTextReport = reportContent.value || '审查报告内容为空';
    
    // 创建Blob对象
    const blob = new Blob([plainTextReport], { type: 'text/plain;charset=utf-8' });
    
    // 创建下载链接
    const downloadLink = document.createElement('a');
    downloadLink.href = URL.createObjectURL(blob);
    
    // 设置文件名
    const fileName = props.objectId 
      ? `审查报告_对象ID_${props.objectId}_${new Date().toISOString().slice(0, 10)}.txt`
      : `审查报告_${new Date().toISOString().slice(0, 10)}.txt`;
    
    downloadLink.download = fileName;
    
    // 添加到文档并触发点击
    document.body.appendChild(downloadLink);
    downloadLink.click();
    
    // 清理
    document.body.removeChild(downloadLink);
    URL.revokeObjectURL(downloadLink.href);
    
    ElMessage.success(`报告已导出为 ${fileName}`);
  } catch (error) {
    console.error('导出报告失败:', error);
    ElMessage.error(`导出报告失败: ${error.message || '未知错误'}`);
  }
}

const formattedReportContent = computed(() => {
  if (!reportContent.value) return ''
  
  let formatted = reportContent.value
    .replace(/\n/g, '<br>')
    .replace(/\r/g, '')
  
  // 高亮关键词
  formatted = formatted
    .replace(/错误/g, '<span class="highlight-error">错误</span>')
    .replace(/失败/g, '<span class="highlight-error">失败</span>')
    .replace(/不合格/g, '<span class="highlight-error">不合格</span>')
    .replace(/问题/g, '<span class="highlight-warning">问题</span>')
  
  formatted = formatted
    .replace(/成功/g, '<span class="highlight-success">成功</span>')
    .replace(/通过/g, '<span class="highlight-success">通过</span>')
    .replace(/合格/g, '<span class="highlight-success">合格</span>')
  
  // 替换分隔符
  formatted = formatted
    .replace(/-{3,}/g, '<hr class="separator">')
    .replace(/={3,}/g, '<div class="strong-separator"></div>')
  
  // 格式化标题和子标题
  formatted = formatted
    .replace(/^(.+?)(?=<br>)/g, '<h3 class="report-title">$1</h3>')
    .replace(/<br>(.+?)(?=<br>)/g, '<br><h4 class="report-subtitle">$1</h4>')
  
  return formatted
})

// 加载报告数据
const loadReport = async () => {
  try {
    reportLoading.value = true
    
    // 从数源方API获取对象信息，包含审计报告
    if (props.objectId) {
      try {
        // 从数源方API获取审计报告
        const response = await axios.get(`http://localhost:8081/api/objects/${props.objectId}`);
        
        let auditReport = '';
        
        // 尝试从响应中提取审计报告
        if (response.data) {
          if (response.data.auditReport) {
            auditReport = response.data.auditReport;
          } else if (response.data.data && response.data.data.auditReport) {
            auditReport = response.data.data.auditReport;
          }
        }
        
        // 如果找到审计报告，则使用它
        if (auditReport && auditReport.trim() !== '') {
          reportContent.value = auditReport;
          reportLoading.value = false;
          return;
        }
      } catch (apiError) {
        console.warn('从数源方API获取审计报告失败:', apiError);
      }
    }
    
    // 如果没有获取到报告内容，显示默认信息
    reportContent.value = props.objectId 
      ? `等待治理方审查\n======================\n\n该数据对象 (ID: ${props.objectId}) 尚未生成审查报告，请等待治理方进行审查。`
      : '暂无审查报告信息';
    
  } catch (error) {
    console.error('加载报告失败:', error);
    ElMessage.error('加载报告失败: ' + error.message);
    reportContent.value = `
加载报告失败
======================

错误信息: ${error.message || '未知错误'}

请尝试刷新页面或联系管理员。
`;
  } finally {
    reportLoading.value = false;
  }
}

watch(() => props.visible, (newValue) => {
  if (newValue) {
    loadReport();
  } else {
    // 关闭对话框时清空内容，但添加延迟以便有淡出动画
    setTimeout(() => {
      reportContent.value = '';
    }, 300);
  }
});

// 监听objectId变化，重新加载报告
watch(() => props.objectId, (newValue) => {
  if (props.visible && newValue) {
    loadReport();
  }
});

onMounted(() => {
  if (props.visible) {
    loadReport();
  }
});
</script>

<style scoped>
.report-dialog :deep(.el-dialog__header) {
  padding: 15px 20px;
  border-bottom: 1px solid #ebeef5;
  margin-right: 0;
}

.report-dialog :deep(.el-dialog__body) {
  padding: 20px;
  max-height: 70vh; /* 增加弹窗高度 */
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

.loading-area {
  margin: 20px 0;
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

.report-scrollbar {
  height: 750px; /* 增加高度 */
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