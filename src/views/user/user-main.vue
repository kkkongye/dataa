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
              placeholder="搜索实体名、约束条件、传输控制操作、状态"
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
            <el-button type="primary" plain @click="handleVerifySC"> 验证组织机构凭证</el-button>
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
            :row-style="{ height: '45px' }"
            :header-cell-style="headerCellStyle"
          >
            <el-table-column prop="entity" label="实体" width="200" align="center">
              <template #default="scope">
                <el-link type="primary" @click="previewEntity(scope.row)">{{ scope.row.entity }}</el-link>
              </template>
            </el-table-column>
            <el-table-column prop="constraint" label="约束条件" width="280" align="center">
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
            <el-table-column prop="status" label="状态" width="150" align="center">
              <template #default="scope">
                <el-tag
                  :type="getStatusType(scope.row.status)"
                  effect="plain"
                  size="small"
                >
                  {{ scope.row.status }}
                </el-tag>
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
            :page-sizes="[5, 10, 20]"
            background
            @size-change="handleSizeChange"
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
const pageSize = ref(10)
const selectedRows = ref([])

// 模拟数据
const tableData = ref([
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
    transferControl: ['可读取', '可修改', '可共享'],
    status: '已合格'
  },
  {
    id: '2',
    entity: 'EducationData',
    constraint: [
      '格式约束: json',
      '访问约束: 只允许管理方获取',
      '路径约束: 点对点',
      '区域约束: 内网'
    ],
    transferControl: ['可读取', '可委托'],
    status: '待校验'
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
    transferControl: ['可读取', '可修改', '可销毁'],
    status: '不合格'
  },
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
    transferControl: ['可读取'],
    status: '已合格'
  },
  {
    id: '5',
    entity: 'TrafficInfo',
    constraint: [
      '格式约束: txt',
      '访问约束: 全部允许',
      '路径约束: 多跳',
      '区域约束: 内网'
    ],
    transferControl: ['可读取', '可修改', '可共享', '可委托'],
    status: '待校验'
  },
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
    transferControl: ['可读取', '可共享'],
    status: '已合格'
  }
])

// 计算总数据量
const totalCount = computed(() => {
  let result = tableData.value
  
  if (searchKeyword.value) {
    result = advancedSearch(result, searchKeyword.value)
  }
  
  return result.length
})

// 根据搜索条件过滤数据
const filteredTableData = computed(() => {
  let result = tableData.value

  if (searchKeyword.value) {
    result = advancedSearch(result, searchKeyword.value)
  }

  const startIndex = (currentPage.value - 1) * pageSize.value
  const endIndex = startIndex + pageSize.value
  return result.slice(startIndex, endIndex)
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

// 处理每页显示数量变化
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
}

// 表头样式
const headerCellStyle = ({ column }) => {
  const blueProps = [
    'entity',
    'constraint',
    'transferControl',
    'status'
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

// 获取状态类型
const getStatusType = (status) => {
  switch (status) {
    case '已合格':
      return 'success'
    case '不合格':
      return 'danger'
    case '待校验':
      return 'warning'
    default:
      return 'info'
  }
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
  ctx.fillStyle = 'rgba(150,150,150,0.22)'
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

onMounted(() => {
  console.log('使用方数据对象列表页面已加载')
  setWatermark('使  用  方')
  window.addEventListener('resize', () => setWatermark('使  用  方'))
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
  padding: 8px 0;
  box-sizing: border-box;
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
  height: 45px !important;
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
</style>