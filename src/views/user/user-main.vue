<template>
  <div class="datasource-container watermark-bg">
    <!-- 头部导航 -->
    <AppHeader @logout="logout" />
    
    <!-- 主内容区域 -->
    <div class="main-content">
      <!-- 标签页 -->
      <div class="content-card">
        <div class="table-title">使用方请求的数据对象列表</div>
        <!-- 搜索和操作区 -->
        <div class="action-bar">
          <div class="search-area">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索实体名、约束条件、传输控制操作"
              class="search-input"
            >
              <template #suffix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </div>
          <div class="action-buttons">
            <el-button type="primary" plain @click="showVisualization" class="visualization-btn">
              <el-icon><DataAnalysis /></el-icon>
              三维数据可视化
            </el-button>
            <!-- <el-button type="primary" plain @click="handleVerifySC"> 验证组织机构凭证</el-button> -->
            <el-button type="warning" plain @click="goToDecrypt">
              <el-icon><Lock /></el-icon>
              去解密
            </el-button>
          </div>
        </div>
        
        <!-- 数据表格 -->
        <div class="table-container">
          <el-table
            :data="filteredTableData"
            style="width: 100%"
            @selection-change="handleSelectionChange"
            border
            height="100%"
            fit
            :row-style="{ height: '70px' }"
            :header-cell-style="headerCellStyle"
            :span-method="spanMethod"
          >
            <el-table-column prop="groupId" label="组序号" width="120" align="center">
              <template #default="scope">
                <span style="font-weight: bold; ">
                  {{ scope.row.groupId }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="entity" label="实体" width="150" align="center">
              <template #default="scope">
                {{ scope.row.entity }}
              </template>
            </el-table-column>
            <el-table-column prop="constraint" label="约束条件" width="330" align="center">
              <template #default="scope">
                <div class="constraint-container">
                  <template v-if="scope.row.constraint && scope.row.constraint.length">
                    <div 
                      v-for="(_, rowIndex) in Math.ceil((Array.isArray(scope.row.constraint) ? scope.row.constraint : [scope.row.constraint]).length / 2)" 
                      :key="rowIndex"
                      class="constraint-row"
                    >
                      <!-- 第一项 -->
                      <div class="constraint-item-pair">
                        <span v-if="(Array.isArray(scope.row.constraint) ? scope.row.constraint : [scope.row.constraint])[rowIndex * 2]" 
                              v-html="formatConstraintText((Array.isArray(scope.row.constraint) ? scope.row.constraint : [scope.row.constraint])[rowIndex * 2])"></span>
                      </div>
                      
                      <!-- 第二项 -->
                      <div class="constraint-item-pair">
                        <span v-if="(Array.isArray(scope.row.constraint) ? scope.row.constraint : [scope.row.constraint])[rowIndex * 2 + 1]" 
                              v-html="formatConstraintText((Array.isArray(scope.row.constraint) ? scope.row.constraint : [scope.row.constraint])[rowIndex * 2 + 1])"></span>
                      </div>
                    </div>
                  </template>
                  <template v-else>-</template>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="transferControl" label="传输控制操作" width="200" align="center">
              <template #default="scope">
                <div class="control-container">
                  <template v-if="scope.row.transferControl && scope.row.transferControl.length">
                    <el-tag
                      v-for="(item, index) in (Array.isArray(scope.row.transferControl) ? scope.row.transferControl : [scope.row.transferControl])"
                      :key="index"
                      size="small"
                      type="primary"
                      effect="plain"
                      class="control-tag"
                    >
                      {{ item }}
                    </el-tag>
                  </template>
                  <template v-else>-</template>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="申请状态" width="350" align="center">
                 <template #default="scope">
                   <div style="display: flex; justify-content: center; align-items: center; min-height: 70px; padding: 10px;">
                      <el-timeline style="max-width: 320px; text-align: left;">
                        <el-timeline-item
                          v-for="(step, index) in getTimelineSteps(currentGroup)"
                          :key="index"
                          :color="step.color"
                          size="large"
                          :hollow="step.hollow"
                        >
                          <div style="display: flex; align-items: center; justify-content: space-between; width: 100%;">
                            <div>
                              <span :style="{ fontWeight: 'bold', color: step.color }">{{ step.content }}</span>
                              <span v-if="step.extraInfo" style="font-weight: normal;">{{ step.extraInfo }}</span>
                            </div>
                            <el-button 
                              v-if="step.content === '待获得共享证书' && currentGroup && currentGroup.dataCredentialStatus && currentGroup.orgCredentialStatus && !currentGroup.sharedCertificateStatus"
                              type="primary" 
                              plain 
                              @click="handleVerifySC"
                              size="medium"
                              style="margin-left: 20px;"
                            >
                              验证组织机构凭证
                            </el-button>
                          </div>
                        </el-timeline-item>
                      </el-timeline>
                    </div>
                 </template>
               </el-table-column>
              <el-table-column prop="applyTime" label="申请时间" width="180" align="center">
                <template #default="scope">
                  {{ scope.row.applyTime }}
                </template>
              </el-table-column>
          </el-table>
        </div>
        
        <!-- 分页 -->
        <div class="pagination-area">
          <CommonPagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total-count="totalCount"
            background
            :show-sizes="false"
          />
        </div>
      </div>
    </div>
  </div>

  <!-- 添加三维可视化对话框组件 -->
  <VisualizationDialog v-model:visible="visualizationVisible" :source-page="'user-main'" />
</template>

<script setup>
import { ref, computed, reactive, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElLoading } from 'element-plus'
import axios from 'axios'
import { Search, Refresh, Download, Lock, DataAnalysis } from '@element-plus/icons-vue'
import AppHeader from '@/components/AppHeader.vue'
import CommonPagination from '@/components/CommonPagination.vue'
import VisualizationDialog from '@/components/visualization/VisualizationDialog.vue'
import { advancedSearch } from '@/utils/searchUtils'

const router = useRouter()
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(1) // 每页显示一组
const selectedRows = ref([])

// 模拟分组数据
const groupData = ref([
  {
    groupId: 'G001',
    applyTime: '2024-01-15 10:30:00',
    dataSourceUser: '浙江省税务局',
    governorUser: '浙江省大数据局',
    dataCredentialStatus: false,
    orgCredentialStatus: false,
    sharedCertificateStatus: false,
    entities: [
      {
        id: '1',
        entity: 'CivilAffairs',
        constraint: [
          '格式约束: xlsx',
          '访问约束: 全部允许',
          '路径约束: 点对点',
          '区域约束: 内网',
          '共享约束: 允许共享'
        ],
        transferControl: ['可读取', '可修改', '可共享']
      },
      {
        id: '2',
        entity: 'EducationData',
        constraint: [
          '格式约束: json',
          '访问约束: 只允许管理方获取',
          '路径约束: 点对点',
          '区域约束: 内网',
          '共享约束: 允许共享'          
        ],
        transferControl: ['可读取', '可委托']
      },
      {
        id: '3',
        entity: 'HealthRecord',
        constraint: [
          '格式约束: csv',
          '访问约束: 全部允许',
          '路径约束: 多跳',
          '区域约束: 外网',
          '共享约束: 禁止共享'
        ],
        transferControl: ['可读取', '可修改', '可销毁']
      }
    ]
  },
  {
    groupId: 'G002',
    applyTime: '2024-01-16 14:20:00',
    dataSourceUser: '浙江省税务局',
    governorUser: '浙江省大数据局',
    dataCredentialStatus: true,
    orgCredentialStatus: false,
    sharedCertificateStatus: false,
    entities: [
      {
        id: '4',
        entity: 'FinancialData',
        constraint: [
          '格式约束: pdf',
          '访问约束: 只允许管理方获取',
          '路径约束: 点对点',
          '区域约束: 内网',
          '共享约束: 允许共享'
        ],
        transferControl: ['可读取']
      },
      {
        id: '5',
        entity: 'TrafficInfo',
        constraint: [
          '格式约束: txt',
          '访问约束: 全部允许',
          '路径约束: 多跳',
          '区域约束: 内网',
          '共享约束: 允许共享'
        ],
        transferControl: ['可读取', '可修改', '可共享', '可委托']
      }
    ]
  },
  {
    groupId: 'G003',
    applyTime: '2024-01-18 16:30:00',
    dataSourceUser: '浙江省税务局',
    governorUser: '浙江省大数据局',
    dataCredentialStatus: true,
    orgCredentialStatus: true,
    sharedCertificateStatus: false,
    entities: [
      {
        id: '7',
        entity: 'PolicyData',
        constraint: [
          '格式约束: xml',
          '访问约束: 只允许管理方获取',
          '路径约束: 点对点',
          '区域约束: 内网',
          '共享约束: 允许共享'
        ],
        transferControl: ['可读取', '可共享']
      }
    ]
  },
  {
    groupId: 'G004',
    applyTime: '2024-01-17 09:15:00',
    dataSourceUser: '浙江省税务局',
    governorUser: '浙江省大数据局',
    dataCredentialStatus: true,
    orgCredentialStatus: true,
    sharedCertificateStatus: true,
    entities: [
      {
        id: '6',
        entity: 'WeatherData',
        constraint: [
          '格式约束: json',
          '访问约束: 全部允许',
          '路径约束: 点对点',
          '区域约束: 外网',
          '共享约束: 允许共享'
        ],
        transferControl: ['可读取', '可共享']
      },
      {
        id: '7',
        entity: 'SensorData',
        constraint: [
          '格式约束: xml',
          '访问约束: 全部允许',
          '路径约束: 点对点',
          '区域约束: 内网',
          '共享约束: 允许共享'
        ],
        transferControl: ['可读取', '可修改']
      }
    ]
  }
])

// 当前显示的组
const currentGroup = computed(() => {
  const groupIndex = currentPage.value - 1
  return groupData.value[groupIndex] || null
})

// 当前组的实体数据（用于表格显示）
const tableData = computed(() => {
  if (!currentGroup.value) return []
  return currentGroup.value.entities.map((entity, index) => ({
    ...entity,
    groupId: currentGroup.value.groupId,
    status: currentGroup.value.status,
    applyTime: currentGroup.value.applyTime,
    entityIndex: index + 1
  }))
})

// 计算总组数
const totalCount = computed(() => {
  return groupData.value.length
})

// 当前组的实体数据（已在上面定义为tableData）
const filteredTableData = computed(() => {
  return tableData.value
})

// 处理表格选择变更
const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

// 退出登录
const logout = () => {
  localStorage.removeItem('role')
  router.push('/login')
}

// 表格单元格合并方法
const spanMethod = ({ row, column, rowIndex, columnIndex }) => {
  // 组序号列(第0列)、状态列(第4列)、申请时间列(第5列)需要合并
  if (columnIndex === 0 || columnIndex === 4 || columnIndex === 5) {
    if (rowIndex === 0) {
      return {
        rowspan: tableData.value.length,
        colspan: 1
      }
    } else {
      return {
        rowspan: 0,
        colspan: 0
      }
    }
  }
  return {
    rowspan: 1,
    colspan: 1
  }
}

// 表头样式
const headerCellStyle = ({ column }) => {
  const blueProps = [
    'groupId',
    'entity',
    'constraint',
    'transferControl'
  ]
  if (blueProps.includes(column.property)) {
    return {
      background: '#eaf6ff',
      color: '#1677c7',
      fontWeight: 'bold',
      fontSize: '16px',
      textAlign: 'center',
      padding: '10px 0'
    }
  }

  return {
    background: '#f5f7fa',
    color: '#606266',
    fontWeight: 'bold',
    fontSize: '15px',
    textAlign: 'center',
    padding: '10px 0'
  }
}

// 格式化约束条件文本
const formatConstraintText = (text) => {
  if (!text) return text
  
  if (text.includes(':')) {
    const parts = text.split(':')
    return `<span class="constraint-prefix">${parts[0]}:</span>${parts[1]}`
  }
  
  return text
}

// 获取时间线步骤数据
const getTimelineSteps = (groupData) => {
  if (!groupData) return []
  
  const allSteps = [
    '待数源方生成数据凭证',
    '待治理方生成组织机构凭证', 
    '待获得共享证书',
    '已获得共享证书,可解密'
  ]
  
  // 根据布尔状态计算当前进行的步骤
  let currentStepIndex = 0
  if (!groupData.dataCredentialStatus) {
    currentStepIndex = 0 // 待数源方生成数据凭证
  } else if (!groupData.orgCredentialStatus) {
    currentStepIndex = 1 // 待治理方生成组织机构凭证
  } else if (!groupData.sharedCertificateStatus) {
    currentStepIndex = 2 // 待获得共享证书
  } else {
    currentStepIndex = 3 // 已获得共享证书
  }
  
  const steps = allSteps.map((stepContent, index) => {
    let stepConfig = {
      content: stepContent,
      color: '#C0C4CC', 
      hollow: true,
      extraInfo: ''
    }
    
    // 添加括号内容（只有当对应状态为true时才显示）
    if (stepContent === '待数源方生成数据凭证' && groupData.dataCredentialStatus) {
      stepConfig.extraInfo = `（数源方：${groupData.dataSourceUser}）`
    } else if (stepContent === '待治理方生成组织机构凭证' && groupData.orgCredentialStatus) {
      stepConfig.extraInfo = `（治理方：${groupData.governorUser}）`
    }
    
    // 根据状态设置颜色
    if (index < currentStepIndex) {
      // 已完成的步骤 - 绿色圆点
      stepConfig.color = '#67C23A'
      stepConfig.hollow = false
    } else if (index === currentStepIndex) {
      // 当前进行的步骤 - 蓝色圆点
      stepConfig.color = '#409EFF'
      stepConfig.hollow = false
    }
    // 其他保持灰色空心
    
    return stepConfig
  })
  
  return steps
}

// 预览实体
const previewEntity = (row) => {
  ElMessage.info(`预览实体: ${row.entity}`)
}

// 刷新数据
const handleRefresh = () => {
  ElMessage.success('数据已刷新')
}

// 导出数据
const handleExport = () => {
  ElMessage.success('数据导出功能开发中')
}

// 跳转到解密页面
const goToDecrypt = () => {
  router.push('/user')
}

// 添加三维可视化相关
const visualizationVisible = ref(false)
const showVisualization = () => {
  visualizationVisible.value = true
}

// 处理验证组织机构凭证
const handleVerifySC = async () => {
  try {
    const loadingInstance = ElLoading.service({
      fullscreen: true,
      text: '正在验证组织机构凭证...',
      background: 'rgba(0, 0, 0, 0.7)'
    });

    const response = await axios.post('http://localhost:8083/api/verify-sc');

    loadingInstance.close();

    if (response.data && response.data.code === 0 && response.data.msg === '尚未接收到共享证书') {
      ElMessage.error('验证失败：尚未接收到共享证书，请联系治理方发送共享证书');
    } else if (response.data && response.data.code === 1) {
      ElMessage.success('组织机构凭证验证成功！');
    } else {
      ElMessage.error(`组织机构凭证验证失败: ${response.data?.msg || response.data?.message || '未知错误'}`);
    }
  } catch (error) {
    console.error('组织机构凭证验证失败:', error);
    
    if (error.response) {
      if (error.response.status === 404) {
        ElMessage.error('使用方服务未启动或接口不存在');
      } else if (error.response.status === 500) {
        ElMessage.error(`使用方服务错误: ${error.response.data?.message || '内部服务器错误'}`);
      } else {
        ElMessage.error(`验证失败 (${error.response.status}): ${error.response.data?.message || error.message}`);
      }
    } else if (error.request) {
      ElMessage.error('无法连接到使用方服务，请确保服务已启动');
    } else {
      ElMessage.error(`组织机构凭证验证失败: ${error.message || '未知错误'}`);
    }
  }
}

function setWatermark(text) {
  const id = 'global-watermark-bg'
  let wm = document.getElementById(id)
  if (wm) {
    wm.parentNode.removeChild(wm)
  }
  const can = document.createElement('canvas')
  can.width = 600
  can.height = 400
  const ctx = can.getContext('2d')
  ctx.rotate(-30 * Math.PI / 180)
  ctx.font = '50px Microsoft YaHei'
  ctx.fillStyle = 'rgba(150,150,150,0.15)'
  ctx.textAlign = 'left'
  ctx.textBaseline = 'middle'
  ctx.fillText(text, 80, 200)
  const base64Url = can.toDataURL()
  const div = document.createElement('div')
  div.id = id
  div.style.pointerEvents = 'none'
  div.style.position = 'fixed'
  div.style.top = '0'
  div.style.left = '0'
  div.style.width = '100vw'
  div.style.height = '100vh'
  div.style.zIndex = '9999'
  div.style.background = `url(${base64Url}) left top repeat`
  document.body.appendChild(div)
}

function removeWatermark() {
  const wm = document.getElementById('global-watermark-bg')
  if (wm) wm.parentNode.removeChild(wm)
}

onMounted(async () => {
  console.log('使用方数据对象列表页面已加载')
  setWatermark('使  用  方')
  window.addEventListener('resize', () => setWatermark('使  用  方'))
  showVisualization()
  
  // 使用方系统自动初始化
  try {
    const response = await axios.post('http://localhost:8083/api/send-du-info');
    
    if (response.data && (response.data.code === 1 || response.data.success === true)) {
      console.log('使用方系统自动初始化成功');
    } else {
      console.warn('使用方系统自动初始化失败:', response.data?.message || response.data?.msg || '未知错误');
    }
  } catch (error) {
    console.error('使用方系统自动初始化失败:', error);
  }
})

onBeforeUnmount(() => {
  removeWatermark()
  window.removeEventListener('resize', () => setWatermark('使  用  方'))
})
</script>

<style scoped>
/* 容器样式 */
.datasource-container {
  height: 100vh;
  width: 100vw;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
}

/* 主内容区域样式 */
.main-content {
  flex: 1;
  padding: 16px;
  background-color: #ffffff;
  overflow: auto;
  box-sizing: border-box;
}

.content-card {
  background-color: #ffffff;
  border-radius: 4px;
  padding: 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
  height: 100%;
  width: 100%;
}

/* 搜索和操作区域 */
.action-bar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 12px;
  gap: 12px;
}

.search-area {
  margin-right: auto;
}

.search-input {
  width: 300px;
}

.action-buttons {
  display: flex;
  gap: 12px;
}

/* 表格容器区域 */
.table-container {
  margin-bottom: 16px;
  height: calc(100vh - 340px);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

/* 表格样式优化 */
:deep(.el-table) {
  width: 100% !important;
  height: 100% !important;
}

:deep(.el-table__header),
:deep(.el-table__body),
:deep(.el-table__footer) {
  width: 100% !important;
  table-layout: fixed !important;
  display: table !important;
}

:deep(.el-table__header-wrapper),
:deep(.el-table__body-wrapper),
:deep(.el-table__footer-wrapper) {
  width: 100% !important;
}

:deep(.el-table__cell) {
  text-align: center;
  box-sizing: border-box;
}

:deep(.el-table__body .el-table__cell) {
  padding: 15px 0 !important;
  height: 70px !important;
  line-height: 40px !important;
}

:deep(.el-table .el-table__cell .cell) {
  padding: 0 5px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  box-sizing: border-box;
  width: 100%;
  display: inline-block;
}

:deep(.el-table__row) {
  height: 70px !important;
}

:deep(.el-table__header tr) {
  height: 45px !important;
}

:deep(.el-table__header th.el-table__cell) {
  background-color: #f5f7fa;
  color: #606266;
  font-weight: bold;
  padding: 8px 0;
  text-align: center;
  height: 45px !important;
}

/* 分页区域 */
.pagination-area {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
  margin-bottom: 20px;
  height: 32px;
  position: relative;
  z-index: 1;
}

/* 约束条件相关样式 */
.constraint-container {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 4px;
}

.constraint-row {
  display: flex;
  justify-content: space-between;
  gap: 16px;
}

.constraint-item-pair {
  flex: 1;
  text-align: left;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  padding-left: 15px;
}

:deep(.constraint-prefix) {
  font-weight: bold !important;
  color: #303133 !important;
  margin-right: 4px !important;
}

/* 传输控制操作样式 */
.control-container {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 6px;
  padding: 4px;
}

.control-tag {
  margin: 2px;
}

/* 表头信息部分样式 */
.table-title {
  font-size: 33px;
  font-weight: bold;
  text-align: center;
  margin-bottom: 12px;
  color: #222;
}

/* 状态标签样式 */
.status-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
}

.status-success {
  background-color: #f6ffed;
  color: #52c41a;
}

.status-error {
  background-color: #fff2f0;
  color: #ff4d4f;
}

.status-pending {
  background-color: #e0e2e6;
  color: #8b8e8f;
}

/* 已获得共享证书状态样式 */
.status-certificate {
  background-color: #f6ffed;
  color: #52c41a;
}

/* 时间线组件文本对齐样式 */
.el-timeline-item__content {
  text-align: left !important;
}
</style>