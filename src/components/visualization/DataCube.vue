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
import axios from 'axios';

// 定义emit事件
const emit = defineEmits(['data-point-click']);

const chartContainer = ref(null);
let chart = null;
const loading = ref(true);
const error = ref(false);
const errorMessage = ref('');
const autoRotate = ref(false);
const pointSize = ref(10);
let initAttempts = 0;
const MAX_INIT_ATTEMPTS = 10;
const RETRY_DELAY = 500;
const apiUrl = 'http://localhost:8081/api/objects/list';

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

// 修改状态颜色映射
const statusColors = {
  '已合格': '#91cc75',  // 绿色
  '不合格': '#ee6666',  // 红色
  '待校验': '#909399'   // 灰色
};

// 从后端获取数据
const fetchDataFromBackend = async () => {
  try {
    const response = await axios.get(apiUrl);
    return response.data;
  } catch (err) {
    console.error('获取后端数据失败:', err);
    error.value = true;
    errorMessage.value = `无法从API获取数据: ${err.message}`;
    return null;
  }
};

// 处理后端数据并生成图表数据
const processBackendData = async () => {
  try {
    const backendData = await fetchDataFromBackend();
    if (!backendData) {
      return { data: [], industries: Object.keys(industryValues) };
    }
    
    let dataArray = backendData;
    
    // 检查数据结构
    if (!Array.isArray(dataArray)) {
      if (backendData.data && Array.isArray(backendData.data)) {
        dataArray = backendData.data;
      } else if (backendData.results && Array.isArray(backendData.results)) {
        dataArray = backendData.results;
      } else if (backendData.items && Array.isArray(backendData.items)) {
        dataArray = backendData.items;
      } else if (backendData.list && Array.isArray(backendData.list)) {
        dataArray = backendData.list;
      } else if (backendData.content && Array.isArray(backendData.content)) {
        dataArray = backendData.content;
      } else {
        console.error('无法识别的数据结构:', backendData);
        return { data: [], industries: Object.keys(industryValues) };
      }
    }
    
    const industries = [...new Set(dataArray.map(item => item.industryCategory || '未分类'))];
    
    const data = dataArray.map((item, index) => {
      try {
        const timeValue = item.updatedAt ? new Date(item.updatedAt).getTime() : new Date().getTime();
        const categoryValue = parseFloat(item.totalCategoryValue || 0);
        const gradeValue = parseFloat(item.totalGradeValue || 0);
        const industry = item.industryCategory || '未分类';
        const industryIndex = industries.indexOf(industry);
        
        // 从dataEntity中获取状态
        let status = '待校验';
        if (item.dataEntity && item.dataEntity.status) {
          status = item.dataEntity.status === '待检验' ? '待校验' : item.dataEntity.status;
        } else if (item.dataContent) {
          try {
            const dataContent = JSON.parse(item.dataContent);
            if (dataContent.status) {
              status = dataContent.status === '待检验' ? '待校验' : dataContent.status;
            }
          } catch (e) {
            console.error('解析dataContent失败:', e);
          }
        }
        
        const statusColor = statusColors[status] || statusColors['待校验'];
        
        const dataSize = pointSize.value;
        const completeness = 0.8;
        
        let entityName = `数据${index+1}`;
        let metadata = {
          dataName: '未知',
          sourceUnit: '未知',
          contactPerson: '未知',
          contactPhone: '未知',
          resourceSummary: '未知',
          fieldClassification: '未知'
        };
        
        // 从dataEntity中获取实体名称和元数据
        if (item.dataEntity) {
          entityName = item.dataEntity.entity || entityName;
          
          if (item.dataEntity.metadata) {
            metadata = {
              dataName: item.dataEntity.metadata.dataName || '未知',
              sourceUnit: item.dataEntity.metadata.sourceUnit || '未知',
              contactPerson: item.dataEntity.metadata.contactPerson || '未知',
              contactPhone: item.dataEntity.metadata.contactPhone || '未知',
              resourceSummary: item.dataEntity.metadata.resourceSummary || '未知',
              fieldClassification: item.dataEntity.metadata.fieldClassification || '未知'
            };
          }
        }
        
        return {
          name: entityName,
          value: [
            timeValue,
            industryIndex,
            categoryValue
          ],
          industry: industry,
          status: status,
          statusColor: statusColor,
          completeness: completeness,
          symbolSize: dataSize,
          itemStyle: {
            color: statusColor,
            opacity: completeness
          },
          entity: entityName,
          statusInfo: status,
          metadata: metadata,
          gradeValue: gradeValue
        };
      } catch (err) {
        console.error('处理数据项时出错:', err);
        return {
          name: `错误数据${index+1}`,
          value: [
            new Date().getTime(),
            0,
            0
          ],
          industry: '未知',
          status: '待校验',
          statusColor: statusColors['待校验'],
          completeness: 0.8,
          symbolSize: pointSize.value,
          itemStyle: {
            color: statusColors['待校验'],
            opacity: 0.8
          },
          entity: `错误数据${index+1}`,
          statusInfo: '待校验',
          metadata: {
            dataName: '未知',
            sourceUnit: '未知',
            contactPerson: '未知',
            contactPhone: '未知',
            resourceSummary: '未知',
            fieldClassification: '未知'
          },
          gradeValue: 0
        };
      }
    });
    
    return { data, industries };
  } catch (err) {
    console.error('处理数据时发生错误:', err);
    return { data: [], industries: Object.keys(industryValues) };
  }
};

// tooltip格式
const tooltipFormatter = (params) => {
  const item = params.data;
  const date = new Date(item.value[0]);
  return `<div style="font-weight:bold;margin-bottom:5px;">${item.entity || '未命名'}</div>
          <div>编辑时间: ${date.toLocaleDateString()}</div>
          <div>行业分类: ${item.industry || '未分类'}</div>
          <div>分类值: ${item.value[2] || 0}</div>
          <div>分级值: ${item.gradeValue || 0}</div>
          <div>状态: <span style="color:${item.statusColor}">${item.status || '未知'}</span></div>
          <div style="margin-top:5px;border-top:1px solid #eee;padding-top:5px;"><b>元数据信息:</b></div>
          <div>数据名称: ${item.metadata?.dataName || '未知'}</div>
          <div>来源单位: ${item.metadata?.sourceUnit || '未知'}</div>
          <div>联系人: ${item.metadata?.contactPerson || '未知'}</div>
          <div>联系电话: ${item.metadata?.contactPhone || '未知'}</div>
          <div>资源摘要: ${item.metadata?.resourceSummary || '未知'}</div>
          <div>字段分类: ${item.metadata?.fieldClassification || '未知'}</div>`;
};

// 原始模拟数据生成函数保留为备用方案
const generateMockData = () => {
  const data = [];
  const industries = Object.keys(industryValues);
  const securityLevels = ['低', '中', '高'];
  const securityColors = ['#91cc75', '#fac858', '#ee6666'];

  for (let i = 0; i < 100; i++) {
    const industry = industries[Math.floor(Math.random() * industries.length)];
    const securityIndex = Math.floor(Math.random() * 3);
    const securityLevel = securityLevels[securityIndex];
    const value = industryValues[industry];

    const date = new Date();
    date.setDate(date.getDate() - Math.floor(Math.random() * 30));
    const timeValue = date.getTime();
    const dataSize = pointSize.value;

    const completeness = 0.8;
    

    const gradeValue = Math.random() * 20;
    
    data.push({
      name: `数据${i+1}`,
      value: [
        timeValue,          
        industries.indexOf(industry),  
        value             
      ],
      industry: industry,
      securityLevel: securityLevel,
      securityColor: securityColors[securityIndex],
      completeness: completeness,
      symbolSize: dataSize,
      itemStyle: {
        color: securityColors[securityIndex],
        opacity: completeness
      },
      entity: `数据${i+1}`,
      status: '未知',
      metadata: {
        dataName: '未知',
        sourceUnit: '未知',
        contactPerson: '未知',
        contactPhone: '未知',
        resourceSummary: '未知',
        fieldClassification: '未知'
      },
      gradeValue: gradeValue  
    });
  }
  
  return data;
};

// 强制渲染方法
const forceRender = async () => {

  loading.value = true;
  error.value = false;
  errorMessage.value = '';

  await nextTick();

  setTimeout(async () => {
    if (!chartContainer.value) {
      console.error('即使强制渲染也找不到容器');
      error.value = true;
      errorMessage.value = '容器元素不可用，请联系开发人员';
      return;
    }
    
    try {

      if (chart) {
        chart.dispose();
        chart = null;
      }

      chartContainer.value.style.width = '96%';
      chartContainer.value.style.height = '75vh';
      chartContainer.value.style.visibility = 'visible';
      chartContainer.value.style.display = 'block';
      chartContainer.value.style.margin = '5px auto';

      document.body.appendChild(chartContainer.value);
      document.body.removeChild(chartContainer.value);
      chartContainer.value.parentNode.appendChild(chartContainer.value);

      chart = echarts.init(chartContainer.value);
      
      const { data, industries } = await processBackendData();
      
      let chartData = data;
      if (data.length === 0) {

        const mockData = generateMockData();
        chartData = mockData;
      }
      





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
          formatter: tooltipFormatter
        },
        visualMap: {
          show: true,
          type: 'piecewise',
          dimension: 2,
          pieces: [
            { value: 0, label: '待校验', color: statusColors['待校验'] },
            { value: 1, label: '已合格', color: statusColors['已合格'] },
            { value: 2, label: '不合格', color: statusColors['不合格'] }
          ],
          left: 'left',
          top: 'middle',
          orient: 'vertical',
          textStyle: {
            color: '#333'
          }
        },
        xAxis3D: {
          type: 'time',
          name: '最近编辑时间',
          nameGap: 60,
          nameTextStyle: {
            fontSize: 15,
            fontWeight: 'bold',
            color: '#333',
            backgroundColor: 'rgba(245,245,245,0.7)',
            padding: [12, 25],
            borderRadius: 3,
            distance: 180,
            align: 'left',
            position: 'end',
            margin: 80
          },
          axisLine: {
            lineStyle: { 
              width: 3,
              color: '#666666'
            }
          },
          axisLabel: {
            formatter: function (value) {
              const date = new Date(value);
              const month = date.getMonth() + 1;
              const day = date.getDate();
              const hours = date.getHours().toString().padStart(2, '0');
              const minutes = date.getMinutes().toString().padStart(2, '0');
              return `${month}月${day}日 ${hours}:${minutes}`;
            },
            margin: 8,
            fontSize: 12,
            interval: 'auto'
          },
          minInterval: 1000 * 60 * 60, 
          maxInterval: 24 * 3600 * 1000 * 7,
          splitNumber: 3 
        },
        yAxis3D: {
          type: 'category',
          name: '行业分类',
          nameGap: 80,
          data: industries,
          nameTextStyle: {
            fontSize: 15,
            fontWeight: 'bold',
            color: '#333',
            backgroundColor: 'rgba(245,245,245,0.7)',
            padding: [12, 25],
            borderRadius: 3,
            distance: 200,
            align: 'left',
            position: 'end',
            margin: 100
          },
          axisLine: {
            lineStyle: { 
              width: 3,
              color: '#666666' 
            }
          },
          axisLabel: {
            formatter: function (value) {
              return value;
            },
            margin: 16,
            fontSize: 13,
            show: true,
            lineHeight: 23,
            interval: 0,
            textStyle: {
              color: '#333',
              fontWeight: 'bold'
            }
          },
          axisTick: {
            show: true,
            interval: 0,
            inside: false,
            length: 6,
            lineStyle: {
              color: '#666666',  
              width: 2
            }
          }
        },
        zAxis3D: {
          type: 'value',
          name: '分级值',
          nameGap: 60,
          nameTextStyle: {
            fontSize: 15,
            fontWeight: 'bold',
            color: '#333',
            backgroundColor: 'rgba(245,245,245,0.7)',
            padding: [12, 25],
            borderRadius: 3,
            distance: 180,
            align: 'left',
            position: 'end',
            margin: 80
          },
          axisLine: {
            lineStyle: { 
              width: 3,
              color: '#666666' 
            }
          },
          min: 0,
          max: 100,
          splitNumber: 5,
          axisLabel: {
            formatter: '{value}',
            margin: 10,
            fontSize: 12
          }
        },
        grid3D: {
          boxWidth: 210,
          boxHeight: 210,
          boxDepth: 210,
          axisLine: {
            show: true,
            lineStyle: { 
              width: 3,
              color: '#666666' 
            }
          },
          axisLabel: {
            fontSize: 12,
            margin: 6,
            show: true
          },
          axisTick: {
            show: true,
            lineStyle: {
              color: '#666666', 
              width: 2
            },
            length: 6
          },
          splitLine: {
            show: true,
            lineStyle: {
              color: '#666666', 
              width: 1.8,
              opacity: 0.5 
            }
          },
          splitArea: {
            show: true,
            areaStyle: {
              color: ['rgba(255,255,255,0.02)', 'rgba(250,250,250,0.05)'] 
            }
          },
          environment: '#ffffff',
          viewControl: {
            projection: 'perspective',
            autoRotate: autoRotate.value,
            autoRotateSpeed: 5,
            alpha: 15,
            beta: 25,
            distance: 550,
            minDistance: 300,
            maxDistance: 800,
            minAlpha: 0,
            maxAlpha: 60,
            minBeta: -90,
            maxBeta: 90,
            zoomSensitivity: 1.5,
            panSensitivity: 1,
            damping: 0.8
          },
          light: {
            main: {
              intensity: 1.5,
              shadow: true,
              shadowQuality: 'high'
            },
            ambient: {
              intensity: 0.5
            },
            ambientCubemap: {
              texture: null,
              exposure: 1,
              diffuseIntensity: 0.5,
              specularIntensity: 0.5
            }
          },
          postEffect: {
            enable: false 
          },
          temporalSuperSampling: {
            enable: true
          }
        },
        series: [{
          type: 'scatter3D',
          data: chartData.map(item => ({
            name: item.name,
            value: item.value,
            industry: item.industry,
            status: item.status,
            statusColor: item.statusColor,
            completeness: item.completeness,
            symbolSize: item.symbolSize,
            itemStyle: item.itemStyle,
            entity: item.entity,
            statusInfo: item.statusInfo,
            metadata: item.metadata,
            gradeValue: item.gradeValue
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
      
      // 添加数据点点击事件监听器
      chart.on('click', (params) => {
        if (params.componentType === 'series' && params.seriesType === 'scatter3D') {
          console.log('数据点被点击:', params.data);
          // 发送点击事件到父组件
          emit('data-point-click', {
            id: params.data.entity || params.data.name,
            name: params.data.name,
            entity: params.data.entity,
            industry: params.data.industry,
            status: params.data.status,
            metadata: params.data.metadata
          });
        }
      });
      
      // 添加resize事件
      window.addEventListener('resize', resizeChart);
      
      // 更新状态
      loading.value = false;
      error.value = false;
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
  errorMessage.value = '';
  initAttempts++;
  
  try {
    // 确保DOM已经渲染
    await nextTick();
    
    if (!chartContainer.value) {
      console.error(`图表容器未找到，尝试次数: ${initAttempts}/${MAX_INIT_ATTEMPTS}`);
      throw new Error(`图表容器未找到，请确保对话框完全显示后再初始化图表`);
    }
    
    // 确保容器有正确的样式和可见性
    chartContainer.value.style.visibility = 'visible';
    chartContainer.value.style.display = 'block';
    
    // 检查容器尺寸出现问题时强制设置尺寸
    const containerWidth = chartContainer.value.clientWidth;
    const containerHeight = chartContainer.value.clientHeight;
    
    if (containerWidth <= 0 || containerHeight <= 0) {
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
    
    chart = echarts.init(chartContainer.value);
    
    // 获取后端数据
    const { data, industries } = await processBackendData();
    
    // 如果没有数据，使用模拟数据作为备选方案
    let chartData = data;
    if (data.length === 0) {
      const mockData = generateMockData();
      chartData = mockData;
    }
    





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
        formatter: tooltipFormatter
      },
      visualMap: {
        show: true,
        type: 'piecewise',
        dimension: 2,
        pieces: [
          { value: 0, label: '待校验', color: statusColors['待校验'] },
          { value: 1, label: '已合格', color: statusColors['已合格'] },
          { value: 2, label: '不合格', color: statusColors['不合格'] }
        ],
        left: 'left',
        top: 'middle',
        orient: 'vertical',
        textStyle: {
          color: '#333'
        }
      },
      xAxis3D: {
        type: 'time',
        name: '最近编辑时间',
        nameGap: 60,
        nameTextStyle: {
          fontSize: 15,
          fontWeight: 'bold',
          color: '#333',
          backgroundColor: 'rgba(245,245,245,0.7)',
          padding: [12, 25],
          borderRadius: 3,
          distance: 180,
          align: 'left',
          position: 'end',
          margin: 80
        },
        axisLine: {
          lineStyle: { 
            width: 3,
            color: '#666666'
          }
        },
        axisLabel: {
          formatter: function (value) {
            const date = new Date(value);
            const month = date.getMonth() + 1;
            const day = date.getDate();
            const hours = date.getHours().toString().padStart(2, '0');
            const minutes = date.getMinutes().toString().padStart(2, '0');
            return `${month}月${day}日 ${hours}:${minutes}`;
          },
          margin: 8,
          fontSize: 12,
          interval: 'auto'
        },
        minInterval: 1000 * 60 * 60, // 最小间隔设为1小时
        maxInterval: 24 * 3600 * 1000 * 7, // 最大间隔设为7天
        splitNumber: 3 // 设置分割段数，减少为3条
      },
      yAxis3D: {
        type: 'category',
        name: '行业分类',
        nameGap: 80,
        data: industries,
        nameTextStyle: {
          fontSize: 15,
          fontWeight: 'bold',
          color: '#333',
          backgroundColor: 'rgba(245,245,245,0.7)',
          padding: [12, 25],
          borderRadius: 3,
          distance: 200,
          align: 'left',
          position: 'end',
          margin: 100
        },
        axisLine: {
          lineStyle: { 
            width: 3,
            color: '#666666' // 将轴线颜色改为深灰色
          }
        },
        axisLabel: {
          formatter: function (value) {
            return value;
          },
          margin: 16,
          fontSize: 13,
          show: true,
          lineHeight: 23,
          interval: 0,
          textStyle: {
            color: '#333',
            fontWeight: 'bold'
          }
        },
        axisTick: {
          show: true,
          interval: 0,
          inside: false,
          length: 6,
          lineStyle: {
            color: '#666666', // 将刻度线颜色改为深灰色
            width: 2
          }
        }
      },
      zAxis3D: {
        type: 'value',
        name: '分级值',
        nameGap: 60,
        nameTextStyle: {
          fontSize: 15,
          fontWeight: 'bold',
          color: '#333',
          backgroundColor: 'rgba(245,245,245,0.7)',
          padding: [12, 25],
          borderRadius: 3,
          distance: 180,
          align: 'left',
          position: 'end',
          margin: 80
        },
        axisLine: {
          lineStyle: { 
            width: 3,
            color: '#666666' // 将轴线颜色改为深灰色
          }
        },
        min: 0,
        max: 100,
        splitNumber: 5,
        axisLabel: {
          formatter: '{value}',
          margin: 10,
          fontSize: 12
        }
      },
      grid3D: {
        boxWidth: 210,
        boxHeight: 210,
        boxDepth: 210,
        axisLine: {
          show: true,
          lineStyle: { 
            width: 3,
            color: '#666666' // 将轴线颜色改为深灰色
          }
        },
        axisLabel: {
          fontSize: 12,
          margin: 6,
          show: true
        },
        axisTick: {
          show: true,
          lineStyle: {
            color: '#666666', // 将刻度线颜色改为深灰色
            width: 2
          },
          length: 6
        },
        splitLine: {
          show: true,
          lineStyle: {
            color: '#666666', // 将网格线颜色改为深灰色
            width: 1.8,
            opacity: 0.5 // 降低一点不透明度，使网格线不那么突兀
          }
        },
        splitArea: {
          show: true,
          areaStyle: {
            color: ['rgba(255,255,255,0.02)', 'rgba(250,250,250,0.05)'] // 更轻微的区域分隔，保持白色背景
          }
        },
        environment: '#ffffff', // 确保背景为纯白色
        viewControl: {
          projection: 'perspective',
          autoRotate: autoRotate.value,
          autoRotateSpeed: 5,
          alpha: 15,
          beta: 25,
          distance: 550,
          minDistance: 300,
          maxDistance: 800,
          minAlpha: 0,
          maxAlpha: 60,
          minBeta: -90,
          maxBeta: 90,
          zoomSensitivity: 1.5,
          panSensitivity: 1,
          damping: 0.8
        },
        light: {
          main: {
            intensity: 1.5,
            shadow: true,
            shadowQuality: 'high'
          },
          ambient: {
            intensity: 0.5
          },
          ambientCubemap: {
            texture: null,
            exposure: 1,
            diffuseIntensity: 0.5,
            specularIntensity: 0.5
          }
        },
        postEffect: {
          enable: false 
        },
        temporalSuperSampling: {
          enable: true
        }
      },
      series: [{
        type: 'scatter3D',
        data: chartData.map(item => ({
          name: item.name,
          value: item.value,
          industry: item.industry,
          status: item.status,
          statusColor: item.statusColor,
          completeness: item.completeness,
          symbolSize: item.symbolSize,
          itemStyle: item.itemStyle,
          entity: item.entity,
          statusInfo: item.statusInfo,
          metadata: item.metadata,
          gradeValue: item.gradeValue
        })),
        emphasis: {
          itemStyle: {
            borderWidth: 1,
            borderColor: '#fff'

          }
        }
      }]
    };

    chart.setOption(option);
    
    // 添加数据点点击事件监听器
    chart.on('click', (params) => {
      if (params.componentType === 'series' && params.seriesType === 'scatter3D') {
        console.log('数据点被点击:', params.data);
        // 发送点击事件到父组件
        emit('data-point-click', {
          id: params.data.entity || params.data.name,
          name: params.data.name,
          entity: params.data.entity,
          industry: params.data.industry,
          status: params.data.status,
          metadata: params.data.metadata
        });
      }
    });

    window.addEventListener('resize', resizeChart);
    
    loading.value = false;
    error.value = false;
    initAttempts = 0; 
  } catch (err) {
    console.error('初始化图表失败:', err);

    if (initAttempts < MAX_INIT_ATTEMPTS) {
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

  initAttempts = 0;
  error.value = false;

  setTimeout(() => {
    initChart();
  }, 300); 
};


const retryInit = () => {
  initAttempts = 0;
  initChart();
};


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
const updatePointSize = async () => {
  if (!chart) return;
  

  const option = chart.getOption();
  const seriesData = option.series[0].data;
  
  const updatedData = seriesData.map(item => {
    item.symbolSize = pointSize.value;
    return item;


  });
  
  chart.setOption({
    series: [{
      data: updatedData
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
  
  setTimeout(async () => {
    if (chartContainer.value) {
      console.log('容器已找到，可在需要时初始化图表');
      console.log('容器尺寸:', chartContainer.value.offsetWidth, chartContainer.value.offsetHeight);
    } else {
      console.warn('组件已挂载但容器引用未找到');
    }
  }, 100);
});

onUnmounted(() => {
  if (chart) {
    chart.dispose();
    chart = null;
  }
  window.removeEventListener('resize', resizeChart);
});

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
  background-color: #ffffff; /* 确保容器背景为白色 */
}

.chart-container {
  width: 96%;
  height: 75vh;
  max-height: 750px;
  position: relative !important;
  display: block !important;
  overflow: hidden;
  border: 1px solid #f0f0f0;
  background-color: #ffffff; /* 确保图表容器背景为白色 */
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
  background-color: #ffffff; /* 确保加载容器背景为白色 */
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