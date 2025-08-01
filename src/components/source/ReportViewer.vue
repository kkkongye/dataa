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
    // 检查报告内容是否存在
    if (!reportContent.value || reportContent.value.trim() === '') {
      ElMessage.warning('没有可导出的报告内容');
      return;
    }
    
    console.log('开始导出报告...');
    
    let plainTextReport = reportContent.value;
    
    if (plainTextReport.includes('<')) {
      const tempDiv = document.createElement('div');
      tempDiv.innerHTML = plainTextReport;
      plainTextReport = tempDiv.textContent || tempDiv.innerText || '';
    }
    
    if (!plainTextReport.trim()) {
      plainTextReport = '审查报告内容为空';
    }
    
    const BOM = '\uFEFF';
    const finalContent = BOM + plainTextReport;
    
    // 创建Blob对象
    const blob = new Blob([finalContent], { 
      type: 'text/plain;charset=utf-8' 
    });
    
    // 设置文件名
    const timestamp = new Date().toISOString().slice(0, 19).replace(/:/g, '-');
    const fileName = props.objectId 
      ? `审查报告_对象ID_${props.objectId}_${timestamp}.txt`
      : `审查报告_${timestamp}.txt`;
    
    // 创建下载链接
    const downloadLink = document.createElement('a');
    downloadLink.href = URL.createObjectURL(blob);
    downloadLink.download = fileName;
    
    // 设置链接样式为隐藏
    downloadLink.style.display = 'none';
    
    // 添加到文档并触发点击
    document.body.appendChild(downloadLink);
    downloadLink.click();
    
    
    // 延迟清理以确保下载完成
    setTimeout(() => {
      document.body.removeChild(downloadLink);
      URL.revokeObjectURL(downloadLink.href);
    }, 100);
    
    ElMessage.success(`报告已导出为 ${fileName}`);
  } catch (error) {
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
  
  // 处理标题格式 ======== 标题 ========
  formatted = formatted
    .replace(/={8,}\s*([^=<>\n\r]+?)\s*={8,}/g, '<div class="section-title">$1</div>')
  
  // 处理剩余的等号分隔符（不是标题的）
  formatted = formatted
    .replace(/={8,}/g, '<div class="strong-separator">========</div>')
    .replace(/-{3,}/g, '<hr class="separator">')
  
  // 格式化行号和字段名
  formatted = formatted
    .replace(/第 (\d+) 行/g, '<span class="line-number">第 $1 行</span>')
    .replace(/字段 \[([^\]]+)\]/g, '<span class="field-name">字段 [$1]</span>')
    .replace(/原因：([^，。\n<]+)/g, '<span class="reason">原因：$1</span>')
  
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
  padding: 16px 16px 16px 40px;
  white-space: pre-wrap;
  font-family: 'Courier New', Courier, monospace;
  font-size: 14px;
  line-height: 1.8;
  color: #303133;
  text-align: left;
  font-weight: bold;
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
  text-align: center;
  font-weight: bold;
  color: #409eff;
  margin: 15px 0;
  padding: 8px 0;
  border-top: 1px solid #409eff;
  border-bottom: 1px solid #409eff;
  background-color: #f0f8ff;
  font-size: 16px;
}

.section-title {
  text-align: center;
  font-weight: 900;
  color: #303133;
  margin: 20px 0;
  padding: 12px 0;
  font-size: 18px;
  background-color: #f5f7fa;
  border-radius: 4px;
  letter-spacing: 1px;
}

.line-number {
  color: #409eff;
  font-weight: bold;
}

.field-name {
  color: #67c23a;
  font-weight: bold;
}

.reason {
  color: #e6a23c;
  font-style: italic;
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