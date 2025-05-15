<template>
  <div class="data-cube-container">
    <!-- 始终渲染图表容器，但通过CSS控制可见性 -->
    <div ref="chartContainer" class="chart-container" :class="{ 'hidden': loading || error }"></div>
    
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
      <div class="loading-text">正在加载图表数据...</div>
    </div>
    
    <div v-if="error" class="error-container">
      <el-result
        icon="error"
        title="图表加载失败"
        :sub-title="errorMessage"
      >
        <template #extra>
          <el-button type="primary" @click="retryInit">重试</el-button>
          <el-button @click="forceRender">手动渲染</el-button>
        </template>
      </el-result>
    </div>
    
    <div class="chart-controls">
      <el-divider>图表控制</el-divider>
      <div class="control-items">
        <div class="control-group">
          <el-switch
            v-model="autoRotate"
            active-text="自动旋转"
            inactive-text="停止旋转"
            @change="updateChartOption"
          />
        </div>
        
        <div class="control-group">
          <span>点大小:</span>
          <el-slider 
            v-model="pointSize" 
            :min="3" 
            :max="20"  
            :step="1" 
            show-stops 
            show-tooltip 
            @change="updatePointSize"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, defineExpose } from 'vue';
import * as echarts from 'echarts';
import 'echarts-gl';

const chartContainer = ref(null);
let chart = null;
const loading = ref(true);
const error = ref(false);
const errorMessage = ref('');
const autoRotate = ref(false);
const pointSize = ref(10);
let initAttempts = 0;
const MAX_INIT_ATTEMPTS = 10; // 增加尝试次数
const RETRY_DELAY = 500; // 减少重试间隔，增加频率

// 行业分类映射值
const industryValues = {
  '交通运输': 90,
  '金融': 90,
  '卫生社会工作': 90,
  '教育': 60,
  '制造业': 60,
  '建筑业': 60,
  '餐饮': 30,
  '居民服务': 30,
  '个人组织': 30
};

// 生成模拟数据
const generateMockData = () => {
  const data = [];
  const industries = Object.keys(industryValues);
  const securityLevels = ['低', '中', '高'];
  const securityColors = ['#91cc75', '#fac858', '#ee6666'];
  
  // 生成100个样本数据点
  for (let i = 0; i < 100; i++) {
    const industry = industries[Math.floor(Math.random() * industries.length)];
    const securityIndex = Math.floor(Math.random() * 3);
    const securityLevel = securityLevels[securityIndex];
    const value = industryValues[industry];
    
    // 生成过去30天内的随机日期
    const date = new Date();
    date.setDate(date.getDate() - Math.floor(Math.random() * 30));
    const timeValue = date.getTime();
    
    // 随机生成数据规模(散点大小)
    const dataSize = Math.random() * (pointSize.value - 3) + 3;
    
    // 随机生成数据完整度(透明度)
    const completeness = Math.random() * 0.5 + 0.5;
    
    data.push({
      name: `数据${i+1}`,
      value: [
        timeValue,           // X轴: 时间
        industries.indexOf(industry),  // Y轴: 行业索引
        value              // Z轴: 分类值
      ],
      industry: industry,
      securityLevel: securityLevel,
      securityColor: securityColors[securityIndex],
      completeness: completeness,
      symbolSize: dataSize,
      itemStyle: {
        color: securityColors[securityIndex],
        opacity: completeness
      }
    });
  }
  
  return data;
};

// 强制渲染方法
const forceRender = async () => {
  console.log('用户请求强制渲染');
  
  // 重置所有状态
  loading.value = true;
  error.value = false;
  
  // 等待下一个tick确保UI更新
  await nextTick();
  
  // 延迟一点时间确保DOM完成更新
  setTimeout(() => {
    if (!chartContainer.value) {
      console.error('即使强制渲染也找不到容器');
      error.value = true;
      errorMessage.value = '容器元素不可用，请联系开发人员';
      return;
    }
    
    console.log('强制渲染 - 容器尺寸:', chartContainer.value.offsetWidth, chartContainer.value.offsetHeight);
    
    try {
      // 如果已经有chart实例，销毁它
      if (chart) {
        chart.dispose();
        chart = null;
      }
      
      // 明确设置容器尺寸
      chartContainer.value.style.width = '96%';
      chartContainer.value.style.height = '75vh';
      chartContainer.value.style.visibility = 'visible';
      chartContainer.value.style.display = 'block';
      chartContainer.value.style.margin = '5px auto';
      
      // 确保容器在DOM中完全可见
      document.body.appendChild(chartContainer.value);
      document.body.removeChild(chartContainer.value);
      chartContainer.value.parentNode.appendChild(chartContainer.value);
      
      // 创建新的echarts实例
      chart = echarts.init(chartContainer.value);
      
      // 应用数据和选项
      const data = generateMockData();
      const industries = Object.keys(industryValues);
      
      // 设置图表选项
      const option = {
        title: {
          text: '数据立体化可视化看板',
          left: 'center',
          top: 10,
          textStyle: {
            fontSize: 18,
            fontWeight: 'bold'
          }
        },
        tooltip: {
          formatter: (params) => {
            const item = params.data;
            const date = new Date(item.value[0]);
            return `<div style="font-weight:bold;margin-bottom:5px;">${item.name}</div>
                    <div>编辑时间: ${date.toLocaleDateString()}</div>
                    <div>行业分类: ${item.industry}</div>
                    <div>分类值: ${item.value[2]}</div>
                    <div>安全级别: ${item.securityLevel}</div>
                    <div>数据完整度: ${Math.round(item.completeness * 100)}%</div>`;
          }
        },
        visualMap: {
          min: 0,
          max: 100,
          dimension: 2,
          inRange: {
            color: ['#313695', '#4575b4', '#74add1', '#abd9e9', '#e0f3f8', '#ffffbf', '#fee090', '#fdae61', '#f46d43', '#d73027', '#a50026']
          }
        },
        xAxis3D: {
          type: 'time',
          name: '编辑时间',
          nameTextStyle: {
            fontSize: 18,
            fontWeight: 'bold',
            color: '#333',
            backgroundColor: '#f5f5f5',
            padding: [5, 8],
            borderRadius: 3
          }
        },
        yAxis3D: {
          type: 'category',
          name: '行业分类',
          data: industries,
          nameTextStyle: {
            fontSize: 18,
            fontWeight: 'bold',
            color: '#333',
            backgroundColor: '#f5f5f5',
            padding: [5, 8],
            borderRadius: 3
          }
        },
        zAxis3D: {
          type: 'value',
          name: '分类值',
          nameTextStyle: {
            fontSize: 18,
            fontWeight: 'bold',
            color: '#333',
            backgroundColor: '#f5f5f5',
            padding: [5, 8],
            borderRadius: 3
          }
        },
        grid3D: {
          boxWidth: 150,
          boxHeight: 150,
          boxDepth: 150,
          axisLine: {
            lineStyle: { width: 2 }
          },
          axisLabel: {
            fontSize: 12,
            margin: 6
          },
          viewControl: {
            projection: 'perspective',
            autoRotate: autoRotate.value,
            autoRotateSpeed: 5,
            beta: 15,
            alpha: 25,
            distance: 350,
            minDistance: 250,
            maxDistance: 500,
            minAlpha: 0,
            maxAlpha: 90,
            minBeta: -90,
            maxBeta: 90,
            zoomSensitivity: 1.5,
            panSensitivity: 1
          },
          light: {
            main: {
              intensity: 1.2,
              shadow: true
            },
            ambient: {
              intensity: 0.3
            }
          },
          postEffect: {
            enable: true,
            bloom: {
              enable: true
            },
            SSAO: {
              enable: true,
              radius: 5
            }
          },
          temporalSuperSampling: {
            enable: true
          }
        },
        series: [{
          type: 'scatter3D',
          data: data.map(item => ({
            name: item.name,
            value: item.value,
            industry: item.industry,
            securityLevel: item.securityLevel,
            securityColor: item.securityColor,
            completeness: item.completeness,
            symbolSize: item.symbolSize,
            itemStyle: item.itemStyle
          })),
          emphasis: {
            itemStyle: {
              borderWidth: 1,
              borderColor: '#fff'
            }
          }
        }]
      };
      
      // 应用选项
      chart.setOption(option);
      
      // 添加resize事件
      window.addEventListener('resize', resizeChart);
      
      // 更新状态
      loading.value = false;
      error.value = false;
      console.log('强制渲染成功');
    } catch (err) {
      console.error('强制渲染失败:', err);
      error.value = true;
      errorMessage.value = `强制渲染失败: ${err.message}`;
    }
  }, 500);
};

// 初始化图表
const initChart = async () => {
  loading.value = true;
  error.value = false;
  initAttempts++;
  
  try {
    console.log(`开始第${initAttempts}次图表初始化尝试...`);
    
    // 确保DOM已经渲染
    await nextTick();
    
    if (!chartContainer.value) {
      console.error(`图表容器未找到，尝试次数: ${initAttempts}/${MAX_INIT_ATTEMPTS}`);
      throw new Error(`图表容器未找到，请确保对话框完全显示后再初始化图表`);
    }
    
    // 确保容器有正确的样式和可见性
    chartContainer.value.style.visibility = 'visible';
    chartContainer.value.style.display = 'block';
    
    // 检查容器尺寸并打印更详细的信息
    const containerWidth = chartContainer.value.clientWidth;
    const containerHeight = chartContainer.value.clientHeight;
    const containerDisplay = window.getComputedStyle(chartContainer.value).display;
    const containerVisibility = window.getComputedStyle(chartContainer.value).visibility;
    const containerPosition = window.getComputedStyle(chartContainer.value).position;
    
    console.log(`图表容器详细信息:
      - 尺寸: ${containerWidth}x${containerHeight}
      - 显示: ${containerDisplay}
      - 可见性: ${containerVisibility}
      - 定位: ${containerPosition}
      - offsetWidth: ${chartContainer.value.offsetWidth}
      - offsetHeight: ${chartContainer.value.offsetHeight}
    `);
    
    if (containerWidth <= 0 || containerHeight <= 0) {
      console.error(`图表容器尺寸无效: ${containerWidth}x${containerHeight}`);
      
      // 强制设置容器尺寸
      chartContainer.value.style.width = '96%';
      chartContainer.value.style.height = '75vh';
      chartContainer.value.style.display = 'block';
      chartContainer.value.style.position = 'relative';
      chartContainer.value.style.visibility = 'visible';
      chartContainer.value.style.margin = '5px auto';
      
      // 再次测量尺寸
      await nextTick();
      const newWidth = chartContainer.value.clientWidth;
      const newHeight = chartContainer.value.clientHeight;
      
      console.log(`强制设置尺寸后: ${newWidth}x${newHeight}`);
      
      // 如果尺寸仍然为0，抛出错误
      if (newWidth <= 0 || newHeight <= 0) {
        throw new Error(`无法设置图表容器的有效尺寸，对话框可能尚未完全显示`);
      }
    }
    
    // 创建ECharts实例
    if (chart) {
      chart.dispose(); // 如果已存在，先销毁
      chart = null;
    }
    
    console.log('准备创建ECharts实例...');
    chart = echarts.init(chartContainer.value);
    console.log('ECharts实例创建成功');
    
    // 生成模拟数据
    const data = generateMockData();
    
    // 准备Y轴标签(行业类别)
    const industries = Object.keys(industryValues);
    
    // 设置图表选项
    const option = {
      title: {
        text: '数据立体化可视化看板',
        left: 'center',
        top: 10,
        textStyle: {
          fontSize: 18,
          fontWeight: 'bold'
        }
      },
      tooltip: {
        formatter: (params) => {
          const item = params.data;
          const date = new Date(item.value[0]);
          return `<div style="font-weight:bold;margin-bottom:5px;">${item.name}</div>
                  <div>编辑时间: ${date.toLocaleDateString()}</div>
                  <div>行业分类: ${item.industry}</div>
                  <div>分类值: ${item.value[2]}</div>
                  <div>安全级别: ${item.securityLevel}</div>
                  <div>数据完整度: ${Math.round(item.completeness * 100)}%</div>`;
        }
      },
      visualMap: {
        min: 0,
        max: 100,
        dimension: 2,
        inRange: {
          color: ['#313695', '#4575b4', '#74add1', '#abd9e9', '#e0f3f8', '#ffffbf', '#fee090', '#fdae61', '#f46d43', '#d73027', '#a50026']
        }
      },
      xAxis3D: {
        type: 'time',
        name: '编辑时间',
        nameTextStyle: {
          fontSize: 18,
          fontWeight: 'bold',
          color: '#333',
          backgroundColor: '#f5f5f5',
          padding: [5, 8],
          borderRadius: 3
        }
      },
      yAxis3D: {
        type: 'category',
        name: '行业分类',
        data: industries,
        nameTextStyle: {
          fontSize: 18,
          fontWeight: 'bold',
          color: '#333',
          backgroundColor: '#f5f5f5',
          padding: [5, 8],
          borderRadius: 3
        }
      },
      zAxis3D: {
        type: 'value',
        name: '分类值',
        nameTextStyle: {
          fontSize: 18,
          fontWeight: 'bold',
          color: '#333',
          backgroundColor: '#f5f5f5',
          padding: [5, 8],
          borderRadius: 3
        }
      },
      grid3D: {
        boxWidth: 150,
        boxHeight: 150,
        boxDepth: 150,
        axisLine: {
          lineStyle: { width: 2 }
        },
        axisLabel: {
          fontSize: 12,
          margin: 6
        },
        viewControl: {
          projection: 'perspective',
          autoRotate: autoRotate.value,
          autoRotateSpeed: 5,
          beta: 15,
          alpha: 25,
          distance: 350,
          minDistance: 250,
          maxDistance: 500,
          minAlpha: 0,
          maxAlpha: 90,
          minBeta: -90,
          maxBeta: 90,
          zoomSensitivity: 1.5,
          panSensitivity: 1
        },
        light: {
          main: {
            intensity: 1.2,
            shadow: true
          },
          ambient: {
            intensity: 0.3
          }
        },
        postEffect: {
          enable: true,
          bloom: {
            enable: true
          },
          SSAO: {
            enable: true,
            radius: 5
          }
        },
        temporalSuperSampling: {
          enable: true
        }
      },
      series: [{
        type: 'scatter3D',
        data: data.map(item => ({
          name: item.name,
          value: item.value,
          industry: item.industry,
          securityLevel: item.securityLevel,
          securityColor: item.securityColor,
          completeness: item.completeness,
          symbolSize: item.symbolSize,
          itemStyle: item.itemStyle
        })),
        emphasis: {
          itemStyle: {
            borderWidth: 1,
            borderColor: '#fff'
          }
        }
      }]
    };
    
    // 设置图表选项
    console.log('设置图表选项...');
    chart.setOption(option);
    console.log('图表选项设置成功');
    
    // 添加窗口大小改变时自动调整图表大小的事件监听
    window.addEventListener('resize', resizeChart);
    
    console.log('图表初始化完全成功');
    loading.value = false;
    error.value = false;
    initAttempts = 0; // 重置尝试次数
  } catch (err) {
    console.error('初始化图表失败:', err);
    
    // 如果尝试次数未达到最大值，则延迟重试
    if (initAttempts < MAX_INIT_ATTEMPTS) {
      console.log(`将在${RETRY_DELAY}毫秒后进行第 ${initAttempts + 1} 次尝试`);
      
      setTimeout(() => {
        initChart();
      }, RETRY_DELAY);
      
      return;
    }
    
    error.value = true;
    errorMessage.value = err.message || '未知错误';
    loading.value = false;
  }
};

// 外部调用的初始化方法
const initializeChart = () => {
  console.log('收到初始化图表请求');
  // 重置尝试次数
  initAttempts = 0;
  error.value = false;
  
  // 延迟初始化，确保DOM已经完全渲染
  // 增加延迟时间，给对话框更多时间完全打开
  setTimeout(() => {
    console.log('延迟结束，开始初始化图表');
    initChart();
  }, 300); // 减少延迟时间
};

// 重试初始化
const retryInit = () => {
  console.log('用户手动重试初始化');
  initAttempts = 0;
  initChart();
};

// 更新图表选项
const updateChartOption = () => {
  if (!chart) return;
  
  chart.setOption({
    grid3D: {
      viewControl: {
        autoRotate: autoRotate.value
      }
    }
  });
};

// 更新点大小
const updatePointSize = () => {
  if (!chart) return;
  
  // 重新生成数据并更新图表
  const data = generateMockData();
  
  chart.setOption({
    series: [{
      data: data.map(item => ({
        name: item.name,
        value: item.value,
        industry: item.industry,
        securityLevel: item.securityLevel,
        securityColor: item.securityColor,
        completeness: item.completeness,
        symbolSize: item.symbolSize,
        itemStyle: item.itemStyle
      }))
    }]
  });
};

// 调整图表大小
const resizeChart = () => {
  if (chart) {
    chart.resize();
  }
};

// 组件挂载时确保容器可见
onMounted(() => {
  console.log('DataCube组件已挂载');
  
  // 延迟一点点检查容器
  setTimeout(async () => {
    if (chartContainer.value) {
      console.log('容器已找到，可在需要时初始化图表');
      console.log('容器尺寸:', chartContainer.value.offsetWidth, chartContainer.value.offsetHeight);
    } else {
      console.warn('组件已挂载但容器引用未找到');
    }
  }, 100);
});

// 组件卸载时释放资源
onUnmounted(() => {
  if (chart) {
    chart.dispose();
    chart = null;
  }
  window.removeEventListener('resize', resizeChart);
});

// 暴露方法给父组件调用
defineExpose({
  initializeChart,
  forceRender,
  hasError: () => error.value
});
</script>

<style scoped>
.data-cube-container {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  position: relative;
  align-items: center;
  justify-content: center;
}

.chart-container {
  width: 96%;
  height: 75vh;
  max-height: 750px;
  position: relative !important;
  display: block !important;
  overflow: hidden;
  border: 1px solid #f0f0f0;
  background-color: #fff;
  box-sizing: border-box;
  z-index: 1;
  margin: 5px auto;
}

.chart-container.hidden {
  visibility: hidden;
  position: absolute;
  z-index: -1;
}

.loading-container {
  width: 96%;
  height: 75vh;
  max-height: 750px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 20px;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background-color: #fff;
  z-index: 2;
  margin: 5px auto;
}

.loading-text {
  margin-top: 20px;
  color: #909399;
  font-size: 14px;
}

.error-container {
  width: 96%;
  height: 75vh;
  max-height: 750px;
  display: flex;
  justify-content: center;
  align-items: center;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background-color: #fff;
  z-index: 3;
  margin: 5px auto;
}

.chart-controls {
  width: 96%;
  padding: 10px;
  background-color: #fff;
  border-top: 1px solid #f0f0f0;
  border-bottom: 1px solid #f0f0f0;
  margin: 0 auto 5px auto;
  z-index: 4;
}

.control-items {
  display: flex;
  flex-wrap: wrap; /* 允许控制项在小屏幕上换行 */
  justify-content: space-between;
  align-items: center;
  gap: 15px;
  padding: 0 10px;
}

.control-items .el-slider {
  width: 180px;
  margin-left: 10px;
}

.control-group {
  display: flex;
  align-items: center;
  gap: 10px;
}
</style> 