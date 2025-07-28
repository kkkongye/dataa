<template>
  <el-dialog
    :model-value="visible"
    @update:model-value="updateVisible"
    title="三维数据可视化"
    width="80%"
    fullscreen
    :before-close="handleClose"
    @opened="handleDialogOpened"
    class="visualization-dialog"
    append-to-body
    destroy-on-close
    :close-on-click-modal="false"
    :show-close="false"
  >
    <!-- 自定义标题栏，包含"我的数据对象列表"按钮 -->
    <template #header>
      <div class="custom-header">
        <div class="dialog-title">三维数据可视化</div>
        <el-button type="primary" class="list-button" @click="handleClose">
          <el-icon><Document /></el-icon>
          返回我的数据对象列表
        </el-button>
      </div>
    </template>
    
    <div class="visualization-content" ref="visualizationContent">
      <el-alert
        v-if="showManualRenderOption"
        type="warning"
        :closable="false"
        title="图表加载失败"
        description="自动初始化图表失败，请点击下方按钮手动渲染"
        show-icon
        style="margin-bottom: 15px;"
      >
        <template #default>
          <div class="manual-render-options">
            <el-button type="primary" @click="handleManualRender">手动渲染图表</el-button>
            <el-button @click="showManualRenderOption = false">隐藏此消息</el-button>
          </div>
        </template>
      </el-alert>
      
      <div class="chart-and-list-container">
        <!-- 图表区域 -->
        <div class="chart-section">
          <DataCube ref="dataCubeRef" @data-point-click="handleDataPointClick" />
        </div>
        
        <!-- 申请列表区域 -->
        <div class="application-list-section">
          <div class="list-header">
            <h3>构造共享证书申请列表</h3>
            <el-button type="danger" size="small" @click="clearAllApplications" :disabled="applicationList.length === 0">
              清空全部
            </el-button>
          </div>
          
          <div class="list-content">
            <div v-if="applicationList.length === 0" class="empty-list">
              <el-icon class="empty-icon"><Document /></el-icon>
              <p>暂无申请数据</p>
              <p class="empty-tip">点击图表中的数据点添加到申请列表</p>
            </div>
            
            <div v-else class="application-items">
              <div 
                v-for="(item, index) in applicationList" 
                :key="item.id"
                class="application-item"
              >
                <div class="item-info">
                  <div class="item-id">{{ item.id }}</div>
                  <div class="item-name">{{ item.name }}</div>
                  <div class="item-industry">{{ item.industry }}</div>
                </div>
                <el-button 
                  type="danger" 
                  size="small" 
                  circle 
                  @click="removeApplication(index)"
                  class="remove-btn"
                >
                  <el-icon><Close /></el-icon>
                </el-button>
              </div>
            </div>
            
            <div v-if="applicationList.length > 0" class="list-footer">
              <el-button type="primary" @click="submitApplications" :disabled="applicationList.length === 0">
                提交申请 ({{ applicationList.length }})
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue';
import DataCube from './DataCube.vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Document, Close } from '@element-plus/icons-vue';
import axios from 'axios';

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['update:visible']);
const dataCubeRef = ref(null);
const visualizationContent = ref(null);
let initializationTimer = null;
let initializationAttempts = 0;
const MAX_INIT_ATTEMPTS = 2;
const showManualRenderOption = ref(false);

// 申请列表相关状态
const applicationList = ref([]);

// 更新可见性状态的方法
const updateVisible = (val) => {
  emit('update:visible', val);
};

watch(() => props.visible, (val) => {
  console.log('VisualizationDialog visible prop changed:', val);
  
  if (!val) {

    if (initializationTimer) {
      clearTimeout(initializationTimer);
      initializationTimer = null;
    }

    showManualRenderOption.value = false;
    initializationAttempts = 0;
  }
});

const handleClose = () => {
  console.log('关闭对话框');
  emit('update:visible', false);
};

const handleDataPointClick = (dataPoint) => {
  console.log('数据点被点击:', dataPoint);
  
  const existingIndex = applicationList.value.findIndex(item => item.id === dataPoint.id);
  if (existingIndex !== -1) {
    ElMessage.warning('该数据已在申请列表中');
    return;
  }
  
  // 添加到申请列表
  applicationList.value.push({
    id: dataPoint.id || dataPoint.name,
    name: dataPoint.name || dataPoint.entity || '未知数据',
    industry: dataPoint.industry || '未分类',
    status: dataPoint.status || '未知状态',
    addTime: new Date().toLocaleTimeString()
  });
  
  ElMessage.success(`已添加 "${dataPoint.name || dataPoint.entity || '数据'}" 到申请列表`);
};

// 移除申请项
const removeApplication = (index) => {
  const item = applicationList.value[index];
  applicationList.value.splice(index, 1);
  ElMessage.success(`已移除 "${item.name}"`);
};

// 清空所有申请
const clearAllApplications = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要清空所有申请项吗？',
      '确认清空',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );
    applicationList.value = [];
    ElMessage.success('已清空申请列表');
  } catch {
    // 用户取消操作
  }
};

// 提交申请
const submitApplications = async () => {
  if (applicationList.value.length === 0) {
    ElMessage.warning('申请列表为空');
    return;
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要提交 ${applicationList.value.length} 个数据的申请吗？`,
      '确认提交',
      {
        confirmButtonText: '提交',
        cancelButtonText: '取消',
        type: 'info',
      }
    );
    
    // 提取所有申请项的ID，用逗号分隔
    const ids = applicationList.value.map(item => item.id).join(',');
    console.log('提交申请ID列表:', ids);
    
    // 调用后端接口
    const response = await axios.post('http://localhost:8083/api/generate-scr', null, {
      params: {
        ids: ids
      }
    });
    
    if (response.data && (response.data.code === 1 || response.data.msg === 'success' || response.data.data?.includes('成功'))) {
      ElMessage.success(`SCR生成并发送成功，已提交 ${applicationList.value.length} 个数据的申请`);
      applicationList.value = []; // 提交成功后清空列表
    } else {
      ElMessage.error(response.data?.msg || 'SCR生成失败');
    }
  } catch (error) {
    console.error('提交申请失败:', error);
    if (error.response) {
      ElMessage.error(error.response.data?.msg || '提交申请失败，请稍后再试');
    } else {
      ElMessage.error('提交申请失败，可能网络连接问题');
    }
  }
};


const handleManualRender = async () => {
  console.log('手动渲染请求');
  if (dataCubeRef.value) {
    try {

      await new Promise(resolve => setTimeout(resolve, 100));
      dataCubeRef.value.forceRender();
      ElMessage.success('正在尝试手动渲染图表...');
      showManualRenderOption.value = false;
    } catch (error) {
      console.error('手动渲染失败:', error);
      ElMessage.error('手动渲染失败，请刷新页面后再试');
    }
  } else {
    ElMessage.error('无法获取图表组件引用');
  }
};

// 处理对话框打开完成事件
const handleDialogOpened = async () => {
  console.log('对话框已完全打开，准备初始化图表');
  initializationAttempts++;

  await nextTick();
  
  if (visualizationContent.value) {
    console.log(`对话框内容区域尺寸: ${visualizationContent.value.clientWidth}x${visualizationContent.value.clientHeight}`);
  }

  initializationTimer = setTimeout(async () => {
    try {
      if (dataCubeRef.value) {
        console.log('开始调用DataCube的初始化方法');
        dataCubeRef.value.initializeChart();
        setTimeout(() => {
          if (dataCubeRef.value && 
              ((initializationAttempts >= MAX_INIT_ATTEMPTS) || 
              (dataCubeRef.value.hasError && dataCubeRef.value.hasError()))) {
            console.log('自动初始化失败，显示手动渲染选项');
            showManualRenderOption.value = true;
          }
        }, 5000); 
      } else {
        console.error('无法获取DataCube组件引用');
        showManualRenderOption.value = true;
      }
    } catch (error) {
      console.error('初始化过程中发生错误:', error);
      showManualRenderOption.value = true;
    }
  }, 300); 
};
</script>

<style scoped>
.visualization-dialog {
  display: flex;
  flex-direction: column;
}

/* 自定义标题栏样式 */
.custom-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  padding: 0;
}

.dialog-title {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.list-button {
  margin-left: auto;
}

.visualization-content {
  flex: 1;
  height: 100%;
  width: 100%;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  position: relative;
  padding: 0;
}

.chart-and-list-container {
  display: flex;
  height: 100%;
  width: 100%;
  gap: 10px;
  padding: 10px;
}

.chart-section {
  flex: 1;
  height: 100%;
  min-width: 0;
}

.application-list-section {
  width: 320px;
  height: 100%;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.list-header {
  padding: 15px;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
}

.list-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.list-content {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
}

.empty-list {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: #909399;
  text-align: center;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 10px;
  color: #c0c4cc;
}

.empty-tip {
  font-size: 12px;
  color: #c0c4cc;
  margin-top: 5px;
}

.application-items {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.application-item {
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  padding: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: all 0.3s ease;
}

.application-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
}

.item-info {
  flex: 1;
  min-width: 0;
}

.item-id {
  font-size: 12px;
  color: #409eff;
  font-weight: 600;
  margin-bottom: 4px;
  word-break: break-all;
}

.item-name {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
  margin-bottom: 2px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-industry {
  font-size: 12px;
  color: #909399;
}

.remove-btn {
  margin-left: 8px;
  flex-shrink: 0;
}

.list-footer {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #e4e7ed;
}

.list-footer .el-button {
  width: 100%;
}

.manual-render-options {
  margin-top: 10px;
  display: flex;
  justify-content: flex-start;
  gap: 10px;
}

:deep(.el-dialog__body) {
  height: calc(100% - 50px);
  padding: 0;
  overflow: hidden;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

:deep(.el-dialog__header) {
  background-color: #f5f7fa;
  padding: 10px 15px;
  margin: 0;
  border-bottom: 1px solid #e4e7ed;
  height: 50px;
  box-sizing: border-box;
}

:deep(.el-dialog) {
  display: flex;
  flex-direction: column;
  margin: 0 !important;
  position: absolute;
  top: 0;
  bottom: 0;
  left: 0;
  right: 0;
  height: 100%;
  max-height: 100%;
}

:deep(.el-alert) {
  width: 96%;
  margin: 5px auto;
}

:deep(.el-dialog__title) {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
}

:deep(.dialog-footer) {
  padding: 10px 20px;
  text-align: right;
  border-top: 1px solid #e4e7ed;
}
</style>