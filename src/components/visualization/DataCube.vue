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
const apiUrl = 'http://localhost:8080/api/objects/list';

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
      // 如果获取数据失败，返回空对象
      return { data: [], industries: Object.keys(industryValues) };
    }
    
    // 检查后端数据结构
    let dataArray = backendData;
    
    // 检查数据是否包含在特定字段中（常见的API响应格式）
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
    
    // 提取所有唯一的行业分类
    const industries = [...new Set(dataArray.map(item => {
      // 行业分类位于顶层的industryCategory字段
      return item.industryCategory || '未分类';
    }))];
    
    // 添加调试日志，查看第一个数据项中的totalGradeValue
    if (dataArray.length > 0) {
      console.log('第一个数据项的分级值:', {
        原始值: dataArray[0].totalGradeValue,
        解析后: parseFloat(dataArray[0].totalGradeValue || 0)
      });
    }
    
    // 处理数据
    const data = dataArray.map((item, index) => {
      try {
        // 从数据结构中直接提取必要信息
        // 解析时间戳
        const timeValue = item.updatedAt ? new Date(item.updatedAt).getTime() : new Date().getTime();
        
        // 解析分类值
        const categoryValue = parseFloat(item.totalCategoryValue || 0);
        
        // 解析分级值（新增）- 确保正确解析字符串值
        const gradeValue = parseFloat(item.totalGradeValue || 0);
        
        // 获取行业索引
        const industry = item.industryCategory || '未分类';
        const industryIndex = industries.indexOf(industry);
        
        // 确定安全级别（基于分类值）
        const securityLevels = ['低', '中', '高'];
        const securityColors = ['#91cc75', '#fac858', '#ee6666'];
        
        // 基于分类值决定安全等级
        let securityIndex = 0;
        const safeValue = isNaN(categoryValue) ? 0 : categoryValue;
        if (safeValue > 30 && safeValue <= 70) {
          securityIndex = 1;
        } else if (safeValue > 70) {
          securityIndex = 2;
        }
        
        // 使用统一大小，基于滑块值
        const dataSize = pointSize.value;
        
        // 使用固定的透明度
        const completeness = 0.8;
        
        // 从dataEntity嵌套对象中提取实体、状态和元数据
        // 检查dataEntity是否存在
        let entityName = `数据${index+1}`;
        let statusInfo = '未知';
        let metadata = {
          dataName: '未知',
          sourceUnit: '未知',
          contactPerson: '未知',
          contactPhone: '未知',
          resourceSummary: '未知',
          fieldClassification: '未知'
        };
        
        if (item.dataEntity) {
          // 提取实体名称
          entityName = item.dataEntity.entity || entityName;
          
          // 提取状态
          statusInfo = item.dataEntity.status || statusInfo;
          
          // 提取元数据
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
        
        // 在返回数据前验证分级值
        if (index === 0) {
          console.log('处理后的第一个数据点分级值:', gradeValue);
        }
        
        return {
          name: entityName,
          value: [
            timeValue,              // X轴: 时间
            industryIndex,          // Y轴: 行业索引
            safeValue               // Z轴: 分类值
          ],
          industry: industry,
          securityLevel: securityLevels[securityIndex],
          securityColor: securityColors[securityIndex],
          completeness: completeness,
          symbolSize: dataSize,
          itemStyle: {
            color: securityColors[securityIndex],
            opacity: completeness
          },
          // 添加额外字段，用于悬浮提示
          entity: entityName,
          status: statusInfo,
          metadata: metadata,
          gradeValue: gradeValue   // 添加分级值字段
        };
      } catch (err) {
        console.error('处理数据项时出错:', err);
        // 返回一个默认数据点
        return {
          name: `错误数据${index+1}`,
          value: [
            new Date().getTime(),  // X轴: 当前时间
            0,                    // Y轴: 默认行业索引
            0                     // Z轴: 默认分类值
          ],
          industry: '未知',
          securityLevel: '低',
          securityColor: '#91cc75',
          completeness: 0.8,
          symbolSize: pointSize.value,
          itemStyle: {
            color: '#91cc75',
            opacity: 0.8
          },
          entity: `错误数据${index+1}`,
          status: '未知',
          metadata: {
            dataName: '未知',
            sourceUnit: '未知',
            contactPerson: '未知',
            contactPhone: '未知',
            resourceSummary: '未知',
            fieldClassification: '未知'
          },
          gradeValue: 0   // 默认分级值
        };
      }
    });
    
    return { data, industries };
  } catch (err) {
    console.error('处理数据时发生错误:', err);
    return { data: [], industries: Object.keys(industryValues) };
  }
};

// 修改tooltip格式，添加分级值
const tooltipFormatter = (params) => {
  const item = params.data;
  const date = new Date(item.value[0]);
  return `<div style="font-weight:bold;margin-bottom:5px;">${item.entity || '未命名'}</div>
          <div>编辑时间: ${date.toLocaleDateString()}</div>
          <div>行业分类: ${item.industry || '未分类'}</div>
          <div>分类值: ${item.value[2] || 0}</div>
          <div>分级值: ${item.gradeValue || 0}</div>
          <div>状态: ${item.status || '未知'}</div>
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
    
    // 使用统一大小，基于滑块值
    const dataSize = pointSize.value;
    
    // 使用固定的透明度
    const completeness = 0.8;
    
    // 生成随机分级值（新增）
    const gradeValue = Math.random() * 20;
    
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
      gradeValue: gradeValue  // 添加分级值
    });
  }
  
  return data;
};

// 强制渲染方法
const forceRender = async () => {
  // 重置所有状态
  loading.value = true;
  error.value = false;
  errorMessage.value = '';
  
  // 等待下一个tick确保UI更新
  await nextTick();
  
  // 延迟一点时间确保DOM完成更新
  setTimeout(async () => {
    if (!chartContainer.value) {
      console.error('即使强制渲染也找不到容器');
      error.value = true;
      errorMessage.value = '容器元素不可用，请联系开发人员';
      return;
    }
    
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
      
      // 获取后端数据
      const { data, industries } = await processBackendData();
      
      let chartData = data;
      if (data.length === 0) {
        // 如果没有数据，使用模拟数据作为备选方案
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
          axisLabel: {
            formatter: function (value) {
              const date = new Date(value);
              if (date.getDate() === 1) {
                return (date.getMonth() + 1) + '月';
              }
              return '';
            },
            margin: 8,
            fontSize: 12
          },
          minInterval: 24 * 3600 * 1000,
          maxInterval: 30 * 24 * 3600 * 1000
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
          name: '分类值',
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
            enable: false // 禁用后期效果，可能导致背景变灰
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
            securityLevel: item.securityLevel,
            securityColor: item.securityColor,
            completeness: item.completeness,
            symbolSize: item.symbolSize,
            itemStyle: item.itemStyle,
            entity: item.entity,
            status: item.status,
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
        axisLabel: {
          formatter: function (value) {
            const date = new Date(value);
            if (date.getDate() === 1) {
              return (date.getMonth() + 1) + '月';
            }
            return '';
          },
          margin: 8,
          fontSize: 12
        },
        minInterval: 24 * 3600 * 1000,
        maxInterval: 30 * 24 * 3600 * 1000
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
        name: '分类值',
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
          enable: false // 禁用后期效果，可能导致背景变灰
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
          securityLevel: item.securityLevel,
          securityColor: item.securityColor,
          completeness: item.completeness,
          symbolSize: item.symbolSize,
          itemStyle: item.itemStyle,
          entity: item.entity,
          status: item.status,
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
    
    // 设置图表选项
    chart.setOption(option);
    
    // 添加窗口大小改变时自动调整图表大小的事件监听
    window.addEventListener('resize', resizeChart);
    
    loading.value = false;
    error.value = false;
    initAttempts = 0; // 重置尝试次数
  } catch (err) {
    console.error('初始化图表失败:', err);
    
    // 如果尝试次数未达到最大值，则延迟重试
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
  // 重置尝试次数
  initAttempts = 0;
  error.value = false;
  
  // 延迟初始化，确保DOM已经完全渲染
  setTimeout(() => {
    initChart();
  }, 300); // 减少延迟时间
};

// 重试初始化
const retryInit = () => {
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
const updatePointSize = async () => {
  if (!chart) return;
  
  // 更新图表中所有点的大小
  const option = chart.getOption();
  const seriesData = option.series[0].data;
  
  // 更新每个数据点的大小为当前滑块值
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