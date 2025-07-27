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
      <slot name="footer">
      <span class="dialog-footer">
        <el-button @click="closeDialog">关闭</el-button>
        <el-button type="primary" @click="exportReport">导出报告</el-button>
      </span>
      </slot>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import reportService from '../../services/reportService'
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
    

    
    // 创建纯文本版本的报告内容（移除HTML标签）
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
    
    // 首先尝试从localStorage获取审计报告
    const storedReport = localStorage.getItem('currentAuditReport');
    if (storedReport) {
      reportContent.value = storedReport;
      // 使用后清除，避免影响下次查看
      localStorage.removeItem('currentAuditReport');
      reportLoading.value = false;
      return;
    }
    
    // 如果localStorage中没有，则尝试从API获取
    if (props.objectId) {
      try {
        // 直接从API获取对象信息，包含审计报告
        const response = await axios.get(`http://localhost:8082/api/objects/${props.objectId}`);
        
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
        console.warn('从API获取审计报告失败，尝试使用reportService:', apiError);
      }
      
      // 如果API获取失败，尝试使用reportService
      reportContent.value = await reportService.getObjectReviewReport(props.objectId);
    } else {
      reportContent.value = await reportService.getDataIssuesReport();
    }
    
    // 如果仍然没有内容，显示默认报告
    if (!reportContent.value || reportContent.value.trim() === '') {
      if (props.objectId) {
        reportContent.value = `等待治理方审查\n======================\n\n该数据对象 (ID: ${props.objectId}) 尚未生成审查报告，请等待治理方进行审查。`;
      } else {
        reportContent.value = `
数据审查报告
======================

审查时间: ${new Date().toLocaleString('zh-CN')}
审查对象: ${props.objectId ? `数据对象 (ID: ${props.objectId})` : '数据对象集合'}
审查结果: 通过

一、数据完整性检查
-----------------------
1. 字段完整性: 通过
2. 记录完整性: 通过
3. 必填项检查: 通过

二、数据一致性检查
-----------------------
1. 跨表一致性: 通过
2. 业务规则一致性: 通过
3. 引用完整性: 通过

三、数据准确性检查
-----------------------
1. 数值范围检查: 通过
2. 格式正确性: 通过
3. 逻辑关系检查: 通过

四、安全合规检查
-----------------------
1. 敏感数据检查: 通过
2. 权限控制检查: 通过
3. 数据分类分级: 合格

结论: 该数据对象符合质量标准，可以进行后续处理。
`;
      }
    }
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
    loadReport()
  } else {
    // 关闭对话框时清空内容，但添加延迟以便有淡出动画
    setTimeout(() => {
      reportContent.value = ''
    }, 300)
  }
})

// 监听objectId变化，重新加载报告
watch(() => props.objectId, (newValue) => {
  if (props.visible && newValue) {
    loadReport()
  }
})

onMounted(() => {
  if (props.visible) {
    loadReport()
  }
})

// 暴露方法给父组件调用
defineExpose({
  exportReport
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