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
  >
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
      
      <DataCube ref="dataCubeRef" />
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue';
import DataCube from './DataCube.vue';
import { ElMessage } from 'element-plus';

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

// 更新可见性状态的方法
const updateVisible = (val) => {
  emit('update:visible', val);
};

watch(() => props.visible, (val) => {
  console.log('VisualizationDialog visible prop changed:', val);
  
  if (!val) {
    // 如果对话框关闭，清除任何待执行的初始化定时器
    if (initializationTimer) {
      clearTimeout(initializationTimer);
      initializationTimer = null;
    }
    // 重置状态
    showManualRenderOption.value = false;
    initializationAttempts = 0;
  }
});

// 处理对话框关闭
const handleClose = () => {
  console.log('关闭对话框');
  emit('update:visible', false);
};

// 处理手动渲染
const handleManualRender = async () => {
  console.log('手动渲染请求');
  if (dataCubeRef.value) {
    try {
      // 首先等待一个小延迟
      await new Promise(resolve => setTimeout(resolve, 100));
      
      // 调用DataCube组件的强制渲染方法
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
  
  // 先等待一个UI刷新周期
  await nextTick();
  
  if (visualizationContent.value) {
    console.log(`对话框内容区域尺寸: ${visualizationContent.value.clientWidth}x${visualizationContent.value.clientHeight}`);
  }
  
  // 使用setTimeout在宏任务中初始化，确保DOM已完全渲染
  initializationTimer = setTimeout(async () => {
    try {
      if (dataCubeRef.value) {
        console.log('开始调用DataCube的初始化方法');
        dataCubeRef.value.initializeChart();
        
        // 设置超时检查，判断是否需要显示手动渲染选项
        setTimeout(() => {
          if (dataCubeRef.value && 
              ((initializationAttempts >= MAX_INIT_ATTEMPTS) || 
              (dataCubeRef.value.hasError && dataCubeRef.value.hasError()))) {
            console.log('自动初始化失败，显示手动渲染选项');
            showManualRenderOption.value = true;
          }
        }, 5000); // 5秒后检查
      } else {
        console.error('无法获取DataCube组件引用');
        showManualRenderOption.value = true;
      }
    } catch (error) {
      console.error('初始化过程中发生错误:', error);
      showManualRenderOption.value = true;
    }
  }, 300); // 给对话框动画完成一些额外时间
};
</script>

<style scoped>
.visualization-dialog {
  display: flex;
  flex-direction: column;
}

.visualization-content {
  flex: 1;
  height: 100%;
  width: 100%;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  position: relative;
  justify-content: center;
  align-items: center;
  padding: 0;
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