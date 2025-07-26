<template>
  <div class="datasource-container watermark-bg">
    <!-- 头部导航 -->
    <AppHeader role-name="某街道居委会(使用方)" @logout="logout" />
    
    <!-- 主内容区域 -->
    <div class="main-content">
      <!-- 标签页 -->
      <div class="content-card">
        <div class="table-title">我的数据对象列表</div>
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
            <el-button type="primary" plain @click="handleInitUser"> 使用方初始化</el-button>
            <el-button type="primary" plain @click="handleVerifySC"> 验证组织机构凭证</el-button>
            <el-button type="info" plain @click="showDirectoryDialog">目录</el-button>
            <el-button v-if="isDecrypted" type="warning" plain @click="resetDecryption">重新解密</el-button>
          </div>
        </div>
        
        <!-- 数据表格 -->
        <div class="table-container">
          <div v-if="!isDecrypted" class="data-locked-placeholder">
            <el-icon class="locked-icon"><Lock /></el-icon>
            <p>数据已加密，请点击右上角"目录"按钮并选择数据对象ID发送解密申请</p>
            <p>发送解密申请后请等待治理方生成并发送数字胶囊进行解密</p>
            <p class="locked-subtitle">解密后将显示所有匹配ID的数据对象数据</p>
          </div>
          <el-table
            v-else
            :data="filteredTableData"
            style="width: 100%"
            @selection-change="handleSelectionChange"
            border
            height="100%"
            fit
            :row-style="{ height: '45px' }"
            :header-cell-style="headerCellStyle"
          >
            <el-table-column prop="id" label="ID" width="400" align="center" fixed>
              <template #default="scope">
                <div class="id-cell">{{ scope.row.id }}</div>
              </template>
            </el-table-column>
            <el-table-column prop="entity" label="实体" width="120" align="center">
              <template #default="scope">
                <el-link type="primary" @click="previewEntity(scope.row)">{{ scope.row.entity }}</el-link>
              </template>
            </el-table-column>
            <el-table-column prop="locationInfo" label="定位信息" min-width="200" align="center">
              <template #default="scope">
                <span v-if="getLocationInfoObj(scope.row.locationInfo, scope.row.locationInfoJson)">
                  ({{ getLocationInfoObj(scope.row.locationInfo, scope.row.locationInfoJson).databaseName || '-' }},
                  {{ getLocationInfoObj(scope.row.locationInfo, scope.row.locationInfoJson).tableName || '-' }},
                  <el-popover placement="top" trigger="click">
                    <template #reference>
                      <span class="select-fields-link" style="color:#409EFF;cursor:pointer;">"select字段"</span>
                    </template>
                    <div style="max-width:400px;word-break:break-all;">{{ getLocationInfoObj(scope.row.locationInfo, scope.row.locationInfoJson).selectFields }}</div>
                  </el-popover>
                  )
                </span>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column prop="constraint" label="约束条件" min-width="250" align="center">
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
            <el-table-column prop="transferControl" label="传输控制操作" min-width="180" align="center">
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
            
            <!-- 添加分类分级值列 -->
            <el-table-column prop="classificationLevelValue" label="分类分级值" width="180" align="center">
              <template #default="scope">
                <div class="classification-level-container">
                  <div class="classification-level-item">
                    <span class="label">分类值：</span>
                    <span class="value">{{ scope.row.totalCategoryValue || scope.row.classificationValue || '未分类' }}</span>
                  </div>
                  <div class="classification-level-item">
                    <span class="label">分级值：</span>
                    <span class="value">{{ scope.row.totalGradeValue || scope.row.levelValue || '未分级' }}</span>
                  </div>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
        
        <!-- 分页 -->
        <div class="pagination-area">
          <CommonPagination
            v-if="isDecrypted"
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total-count="totalCount"
            :page-sizes="[5, 10, 20]"
            :disabled="!isDecrypted"
            background
            @size-change="handleSizeChange"
          />
        </div>
      </div>
    </div>
  </div>
  

  <!-- 解密对话框 -->
  <el-dialog
    v-model="decryptDialogVisible"
    title="解密"
    width="500px"
    append-to-body
    destroy-on-close
    :close-on-click-modal="false"
    :show-close="true"
    draggable
    class="decrypt-dialog"
  >
    <div style="margin-bottom: 16px; font-size: 16px;">
      <span style="font-weight: bold;">数据对象ID：</span>
      <span style="word-break: break-all;">{{ decryptForm.objectId }}</span>
    </div>
    <template #footer>
      <el-button type="primary" plain @click="handleGenerateCapsule">获取封装的数据对象</el-button>
      <el-button type="primary" @click="handleDecrypt">确定</el-button>
      <el-button @click="decryptDialogVisible = false">取消</el-button>
    </template>
  </el-dialog>

  <!-- Excel预览对话框 -->
  <ObjectPreviewDialog
    v-model:visible="previewDialogVisible"
    :object="previewForm"
    :excelData="excelTableData"
  />

  <!-- 添加目录对话框 -->
  <el-dialog
    v-model="directoryDialogVisible"
    title="目录"
    width="80%"
    :close-on-click-modal="false"
    :show-close="true"
    draggable
    class="directory-dialog"
    destroy-on-close
  >
    <DirectoryTable 
      :visible="directoryDialogVisible"
      @close="directoryDialogVisible = false"
      @view-detail="handleViewDirectoryItem"
      @show-decrypt="showDecryptDialog"
    />
  </el-dialog>

  <!-- 添加三维可视化对话框组件 -->
  <VisualizationDialog v-model:visible="visualizationVisible" />

  <!-- 构造共享证书申请对话框 -->
  <el-dialog v-model="scrDialogVisible" title="构造共享证书申请" width="500px">
    <el-form :model="scrForm" label-width="80px">
      <el-form-item label="metaData">
        <el-input v-model="scrForm.metaData" />
      </el-form-item>
      <el-form-item label="fno">
        <el-input v-model="scrForm.fno" />
      </el-form-item>
      <el-form-item label="sfno">
        <el-input v-model="scrForm.sfno" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="scrDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="submitScrForm">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, reactive, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus'
import { Search, Lock, Document, DataAnalysis } from '@element-plus/icons-vue'
import * as XLSX from 'xlsx'
import ExcelPreview from '@/components/ExcelPreview.vue'
import AppHeader from '@/components/AppHeader.vue'
import CommonPagination from '@/components/CommonPagination.vue'
import DirectoryTable from '@/components/user/DirectoryTable.vue'
import dataObjectService from '@/services/dataObjectService'
import { ensureArray, advancedSearch } from '@/utils/searchUtils';
import axios from 'axios'
import VisualizationDialog from '@/components/visualization/VisualizationDialog.vue'
import ObjectPreviewDialog from '@/components/ObjectPreviewDialog.vue'

const router = useRouter()
const activeTab = ref('objectList')
const currentStatus = ref('') // 默认显示全部数字对象
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(5) // 改为默认显示5条
const isDecrypted = ref(false)
const selectedRows = ref([])
const decryptedObjectId = ref('')


const tableData = ref(dataObjectService.getAllDataObjects())

// 监听共享服务数据变化
onMounted(() => {
  dataObjectService.addChangeListener((newData) => {
  })

  showVisualization()
})

// 计算实际数据量
const totalCount = computed(() => {
  let result = tableData.value;
  
  if (isDecrypted.value && decryptedObjectIds.value.length > 0) {
    result = result.filter(item => decryptedObjectIds.value.includes(item.id));
    return result.length;
  } else if (isDecrypted.value && decryptedObjectId.value) {
    result = result.filter(item => item.id === decryptedObjectId.value);
    return result.length;
  }
  
  if (currentStatus.value) {
    result = result.filter(item => item.status === currentStatus.value);
  }
  
  if (searchKeyword.value) {
    result = advancedSearch(result, searchKeyword.value);
  }
  
  return result.length;
});

// 根据状态和搜索条件过滤数据
const filteredTableData = computed(() => {
  let result = tableData.value;

  if (isDecrypted.value && decryptedObjectIds.value.length > 0) {
    result = result.filter(item => decryptedObjectIds.value.includes(item.id));
  } else if (isDecrypted.value && decryptedObjectId.value) {
    result = result.filter(item => item.id === decryptedObjectId.value);
  } else if (currentStatus.value) {
    result = result.filter(item => item.status === currentStatus.value);
  }

  if (searchKeyword.value) {
    result = advancedSearch(result, searchKeyword.value);
  }

  const startIndex = (currentPage.value - 1) * pageSize.value;
  const endIndex = startIndex + pageSize.value;
  return result.slice(startIndex, endIndex);
});

// 设置当前状态
const setStatus = (status) => {
  currentStatus.value = status
}

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

// 解密状态和表单
const decryptDialogVisible = ref(false)
const decryptFormRef = ref(null)
const decryptForm = reactive({
  objectId: '',
  dataCapsule: ''
})
const decryptFormRules = {
  objectId: [{ required: true, message: '请输入数据对象ID', trigger: 'blur' }],
}
const decryptedObjectIds = ref([])

const isRequestingToken = ref(false)
const isGeneratingCapsule = ref(false)

// 显示解密对话框
const showDecryptDialog = (ids) => {
  decryptForm.objectId = ids.join(', ')
  decryptedObjectIds.value = ids
  
  // 直接调用解密函数，不显示弹窗
  handleDecrypt()
}

// 处理解密操作
const handleDecrypt = async () => {
  try {
    isDecrypted.value = true
    const idList = decryptForm.objectId.split(',').map(id => id.trim()).filter(id => id)
    decryptedObjectIds.value = idList
    decryptedObjectId.value = idList.length === 1 ? idList[0] : ''
    
    // 关闭目录弹窗
    directoryDialogVisible.value = false
    
    ElMessage.success('解密成功')
  } catch (error) {
    console.error('解密失败:', error)
    ElMessage.error(`解密失败: ${error.message}`)
  }
}

// 修改申请token的处理函数
const handleRequestToken = async () => {
  if (!decryptForm.objectId) {
    ElMessage.warning('请先填写数据对象ID')
    return
  }
  
  isRequestingToken.value = true
  
  try {
    const apiUrl = 'http://localhost:8083/api/getToken'
    const response = await fetch(apiUrl, {
      method: 'GET',
      headers: {
        'Accept': 'application/json'
      }
    })
    
    if (!response.ok) {
      throw new Error(`请求失败: ${response.status}`)
    }
    
    const data = await response.json()
    
    if (data && data.code === 1 && data.msg === 'success' && data.data) {
      const token = data.data
      decryptForm.token = token
      localStorage.setItem('receivedToken', token)
      ElMessage.success('成功获取token')
    } else {
      throw new Error('返回数据格式不符合预期')
    }
  } catch (error) {
    console.error('获取token失败:', error)
    ElMessage.error(`获取token失败: ${error.message}`)
  } finally {
    isRequestingToken.value = false
  }
}

const handleGenerateCapsule = async () => {
  if (!decryptForm.objectId) {
    ElMessage.warning('请选择数据对象ID')
    return
  }
  isGeneratingCapsule.value = true
  try {
    const ids = decryptForm.objectId.split(',').map(id => id.trim()).filter(id => id).join(',')
    const apiUrl = `http://localhost:8083/api/selectIds?ids=${encodeURIComponent(ids)}`
    const response = await fetch(apiUrl, {
      method: 'GET',
      headers: { 'Accept': 'application/json' }
    })
    if (!response.ok) {
      throw new Error(`请求失败: ${response.status}`)
    }
    const data = await response.json()
    if (data && data.code === 1 && data.msg === 'success' && data.data) {
      decryptForm.dataCapsule = data.data
      ElMessage.success('成功生成数据胶囊')
    } else {
      throw new Error('返回数据格式不符合预期')
    }
  } catch (error) {
    console.error('生成数据胶囊失败:', error)
    ElMessage.error(`生成数据胶囊失败: ${error.message}`)
  } finally {
    isGeneratingCapsule.value = false
  }
}

// Excel预览相关
const previewDialogVisible = ref(false)
const previewForm = reactive({
  id: '',
  entity: '',
  locationInfo: '',
  locationInfoJson: '',
  constraint: '',
  transferControl: '',
  status: '',
  totalCategoryValue: '',
  totalGradeValue: '',
  classificationValue: '',
  levelValue: '',
  metadata: null
})

const excelBinaryData = ref(null)
const excelTableData = ref([])
const isExcelLoading = ref(false)

const handleExcelDataLoaded = (data) => {
  console.log('Excel数据加载完成:', data)
}

const handleExcelError = (error) => {
  console.error('Excel加载错误:', error)
  ElMessage.error('加载Excel数据时出错: ' + error)
}

const getObjectKeys = (dataArray) => {
  if (!dataArray || !Array.isArray(dataArray) || dataArray.length === 0) {
    return [];
  }
  

  const keySets = dataArray.map(item => {
    if (item && typeof item === 'object') {
      return Object.keys(item);
    }
    return [];
  });

  const allKeys = [...new Set(keySets.flat())];
  
  return allKeys;
}


const handleExportExcel = () => {
  if (excelTableData.value.length === 0) {
    ElMessage.warning('没有数据可导出');
    return;
  }
  
  try {

    const wb = XLSX.utils.book_new();
    const ws = XLSX.utils.json_to_sheet(excelTableData.value);
    XLSX.utils.book_append_sheet(wb, ws, 'Sheet1');

    const fileName = `${previewForm.entity || 'excel_data'}.xlsx`;

    XLSX.writeFile(wb, fileName);
    
    ElMessage.success(`已成功导出 ${fileName}`);
  } catch (error) {
    console.error('导出Excel失败:', error);
    ElMessage.error(`导出Excel失败: ${error.message}`);
  }
}

const fetchExcelDataFromApi = async (objectId) => {
  if (!objectId) {
    ElMessage.warning('无法获取对象ID，无法显示Excel数据')
    return
  }
  isExcelLoading.value = true
  const apiUrl = 'http://localhost:8081/api/objects/list'

  
  try {
    const response = await axios.get(apiUrl)
    

    let targetObject = null
    let dataItems = null
    
    if (response.data && Array.isArray(response.data)) {
      targetObject = response.data.find(item => item.id === objectId)
    } else if (response.data && response.data.list && Array.isArray(response.data.list)) {
      targetObject = response.data.list.find(item => item.id === objectId)
    } else if (response.data && response.data.data && Array.isArray(response.data.data)) {
      targetObject = response.data.data.find(item => item.id === objectId)
    }
    
    if (targetObject) {
      console.log(`【Excel数据】找到ID为${objectId}的对象:`, targetObject)
      

      extractClassificationValues(targetObject)
      
      // 检查 dataEntity.dataItems
      if (targetObject.dataEntity && targetObject.dataEntity.dataItems && Array.isArray(targetObject.dataEntity.dataItems)) {
        dataItems = targetObject.dataEntity.dataItems
        console.log(`【Excel数据】从对象的dataEntity中提取到${dataItems.length}条dataItems`)
      } else if (targetObject.dataItems && Array.isArray(targetObject.dataItems)) {
        dataItems = targetObject.dataItems
        console.log(`【Excel数据】从对象中直接提取到${dataItems.length}条dataItems`)
      } else if (targetObject.dataContent) {
        try {
          const dataContent = typeof targetObject.dataContent === 'string' 
            ? JSON.parse(targetObject.dataContent) 
            : targetObject.dataContent
            
          if (dataContent && dataContent.dataItems && Array.isArray(dataContent.dataItems)) {
            dataItems = dataContent.dataItems
            console.log(`【Excel数据】从dataContent中提取到${dataItems.length}条dataItems`)
          } else if (dataContent && dataContent.dataEntity && dataContent.dataEntity.dataItems && Array.isArray(dataContent.dataEntity.dataItems)) {
            dataItems = dataContent.dataEntity.dataItems
            console.log(`【Excel数据】从dataContent.dataEntity中提取到${dataItems.length}条dataItems`)
          }
        } catch (e) {
          console.error('解析dataContent失败:', e)
        }
      }
    } 

    else if (response.data && response.data.dataItems && Array.isArray(response.data.dataItems)) {
      dataItems = response.data.dataItems.filter(item => 
        item.objectId === objectId || 
        item.id === objectId ||
        (item.对象ID && item.对象ID === objectId)
      )
      
      if (dataItems.length > 0) {
        console.log(`【Excel数据】从全局dataItems中过滤出${dataItems.length}条与ID ${objectId}相关的数据`)
      } else {
        console.log('未找到与对象ID相关的数据，显示所有dataItems')
        dataItems = response.data.dataItems
      }
    }

    if (!dataItems || dataItems.length === 0) {
      console.log(`【Excel数据】未找到ID为${objectId}的对象数据，使用模拟数据`)
      ElMessage.info(`未找到ID为${objectId}的Excel数据，显示示例数据`)

      dataItems = generateMockDataForObject(objectId)
    }
    
    // 创建Excel数据
    createExcelFromDataItems(dataItems)
  } catch (error) {
    console.error('【Excel数据】API请求失败:', error.message)
    ElMessage.error(`获取Excel数据失败: ${error.message}`)
    
    // 使用带有对象ID的模拟数据
    const mockData = generateMockDataForObject(objectId)
    createExcelFromDataItems(mockData)
  }
}

// 提取对象中的分类分级值
const extractClassificationValues = (obj) => {
  if (!obj) return
  
  // 直接提取顶层字段
  if (obj.totalCategoryValue !== undefined) {
    previewForm.totalCategoryValue = obj.totalCategoryValue
  } else if (obj.classificationValue !== undefined) {
    previewForm.classificationValue = obj.classificationValue
  }
  
  if (obj.totalGradeValue !== undefined) {
    previewForm.totalGradeValue = obj.totalGradeValue
  } else if (obj.levelValue !== undefined) {
    previewForm.levelValue = obj.levelValue
  }
  
  // 尝试从dataContent中获取
  if (obj.dataContent) {
    let dataContent = obj.dataContent
    if (typeof dataContent === 'string') {
      try {
        dataContent = JSON.parse(dataContent)
      } catch (e) {
        console.warn('解析dataContent失败:', e)
      }
    }
    
    if (dataContent && typeof dataContent === 'object') {
      if (dataContent.totalCategoryValue !== undefined) {
        previewForm.totalCategoryValue = dataContent.totalCategoryValue
      } else if (dataContent.classificationValue !== undefined) {
        previewForm.classificationValue = dataContent.classificationValue
      }
      
      if (dataContent.totalGradeValue !== undefined) {
        previewForm.totalGradeValue = dataContent.totalGradeValue
      } else if (dataContent.levelValue !== undefined) {
        previewForm.levelValue = dataContent.levelValue
      }
    }
  }
}

// 根据对象ID生成不同的模拟数据
const generateMockDataForObject = (objectId) => {
  // 获取ID的最后两位作为数字（用于生成不同的数据）
  const idNum = parseInt(objectId.slice(-2), 10) || 1
  
  // 根据ID生成不同类型的数据
  if (objectId.includes('user') || objectId.includes('用户')) {
    return [
      { "用户ID": "U10001", "用户名": "张三", "年龄": "28", "性别": "男", "注册日期": "2023-01-15" },
      { "用户ID": "U10002", "用户名": "李四", "年龄": "34", "性别": "男", "注册日期": "2023-02-22" },
      { "用户ID": "U10003", "用户名": "王五", "年龄": "26", "性别": "女", "注册日期": "2023-03-08" },
      { "用户ID": "U10004", "用户名": "赵六", "年龄": "31", "性别": "男", "注册日期": "2023-04-19" },
      { "用户ID": "U10005", "用户名": "钱七", "年龄": "29", "性别": "女", "注册日期": "2023-05-25" }
    ]
  } else if (objectId.includes('order') || objectId.includes('订单')) {
    return [
      { "订单ID": "O20001", "用户ID": "U10001", "商品": "笔记本电脑", "金额": "6999", "下单日期": "2023-06-12" },
      { "订单ID": "O20002", "用户ID": "U10002", "商品": "手机", "金额": "4299", "下单日期": "2023-06-18" },
      { "订单ID": "O20003", "用户ID": "U10003", "商品": "耳机", "金额": "799", "下单日期": "2023-06-25" },
      { "订单ID": "O20004", "用户ID": "U10004", "商品": "平板电脑", "金额": "3599", "下单日期": "2023-07-03" },
      { "订单ID": "O20005", "用户ID": "U10005", "商品": "智能手表", "金额": "1599", "下单日期": "2023-07-10" }
    ]
  } else if (objectId.includes('product') || objectId.includes('产品')) {
    return [
      { "产品ID": "P30001", "产品名称": "华为MateBook", "类别": "笔记本电脑", "价格": "6999", "库存": "120" },
      { "产品ID": "P30002", "产品名称": "iPhone 14", "类别": "手机", "价格": "5999", "库存": "350" },
      { "产品ID": "P30003", "产品名称": "AirPods Pro", "类别": "耳机", "价格": "1999", "库存": "500" },
      { "产品ID": "P30004", "产品名称": "iPad Air", "类别": "平板电脑", "价格": "4599", "库存": "230" },
      { "产品ID": "P30005", "产品名称": "Apple Watch", "类别": "智能手表", "价格": "2999", "库存": "180" }
    ]
  } else if (objectId.includes('inventory') || objectId.includes('库存')) {
    return [
      { "仓库编号": "WH001", "产品ID": "P30001", "产品名称": "华为MateBook", "库存数量": "120", "更新日期": "2023-07-01" },
      { "仓库编号": "WH001", "产品ID": "P30002", "产品名称": "iPhone 14", "库存数量": "350", "更新日期": "2023-07-01" },
      { "仓库编号": "WH001", "产品ID": "P30003", "产品名称": "AirPods Pro", "库存数量": "500", "更新日期": "2023-07-01" },
      { "仓库编号": "WH002", "产品ID": "P30004", "产品名称": "iPad Air", "库存数量": "230", "更新日期": "2023-07-01" },
      { "仓库编号": "WH002", "产品ID": "P30005", "产品名称": "Apple Watch", "库存数量": "180", "更新日期": "2023-07-01" }
    ]
  } else {
    // 通用数据
    return [
      { "姓名": `${idNum}-张三`, "rowNumber": "1", "性别": "男", "对象ID": objectId },
      { "姓名": `${idNum}-李四`, "rowNumber": "2", "性别": "男", "对象ID": objectId },
      { "姓名": `${idNum}-王五`, "rowNumber": "3", "性别": "女", "对象ID": objectId },
      { "姓名": `${idNum}-赵六`, "rowNumber": "4", "性别": "男", "对象ID": objectId },
      { "姓名": `${idNum}-钱七`, "rowNumber": "5", "性别": "女", "对象ID": objectId }
    ]
  }
}

// 创建Excel数据
const createExcelFromDataItems = (dataItems) => {
  try {
    // 处理dataItems中的字段值，移除前缀（字段名称:）
    const processedItems = dataItems.map(item => {
      const processedItem = {};
      
      for (const key in item) {
        // 如果值是字符串，并且包含"key名:"格式的前缀，则去掉前缀
        if (typeof item[key] === 'string' && item[key].startsWith(`${key}：`)) {
          // 使用中文冒号分割，取冒号后面的部分
          processedItem[key] = item[key].substring(item[key].indexOf('：') + 1).trim();
        } else if (typeof item[key] === 'string' && item[key].startsWith(`${key}:`)) {
          // 使用英文冒号分割，取冒号后面的部分
          processedItem[key] = item[key].substring(item[key].indexOf(':') + 1).trim();
        } else {
          // 其他情况保持不变
          processedItem[key] = item[key];
        }
      }
      
      return processedItem;
    });

    // 使用处理后的数据创建Excel
    const wb = XLSX.utils.book_new();
    const ws = XLSX.utils.json_to_sheet(processedItems);
    XLSX.utils.book_append_sheet(wb, ws, "数据");
    
    const excelBuffer = XLSX.write(wb, { bookType: 'xlsx', type: 'array' });
    const blob = new Blob([excelBuffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });

    excelBinaryData.value = blob;
    excelTableData.value = processedItems;
    
    isExcelLoading.value = false;
    ElMessage.success(`成功获取${processedItems.length}条数据记录`);
  } catch (error) {
    console.error('【Excel数据】创建Excel数据失败:', error);
    ElMessage.error(`创建Excel数据失败: ${error.message}`);
    isExcelLoading.value = false;
  }
}

// 预览实体
const previewEntity = (row) => {
  previewForm.id = row.id
  previewForm.entity = row.entity
  previewForm.locationInfo = row.locationInfo
  previewForm.locationInfoJson = row.locationInfoJson
  previewForm.constraint = row.constraint
  previewForm.transferControl = row.transferControl
  previewForm.status = row.status || ''
  

  previewForm.totalCategoryValue = ''
  previewForm.totalGradeValue = ''
  previewForm.classificationValue = ''
  previewForm.levelValue = ''
  previewForm.metadata = extractMetadata(row)
  
  excelBinaryData.value = null
  excelTableData.value = []
  
  previewDialogVisible.value = true

  fetchExcelDataFromApi(row.id)
}

// 处理元数据字符串的函数
const processMetadataString = (metadataString) => {
  if (!metadataString) {
    return { 
      dataName: '未知数据',
      sourceUnit: '未知来源',
      contactPerson: '未指定',
      contactPhone: '未提供',
      resourceSummary: '无描述',
      fieldClassification: '未分类'
    }
  }

  if (typeof metadataString === 'object') {
    return metadataString
  }

  let cleanString = metadataString.toString()
  
  try {
    if (cleanString.startsWith('"') && cleanString.endsWith('"')) {
      const unquoted = cleanString.slice(1, -1).replace(/\\"/g, '"')
      cleanString = unquoted
    }

    if (cleanString.includes('\\\"') || cleanString.includes('\\\\')) {
      cleanString = cleanString.replace(/\\\\/g, '\\').replace(/\\"/g, '"')
    }

    if (cleanString.includes('"]}"') && !cleanString.endsWith('"}')) {
      cleanString = cleanString.replace('"]}"', '"}')
    }
    if (cleanString.includes('"]}",')) {
      cleanString = cleanString.replace('"]}",', '"}')
    }

    if (!cleanString.startsWith('{') && cleanString.includes('":"')) {
      cleanString = '{' + cleanString
    }

    if (!cleanString.endsWith('}') && cleanString.includes('":"')) {
      cleanString = cleanString + '}'
    }

    try {
      const parsed = JSON.parse(cleanString)

      return {
        dataName: parsed.dataName || '未知数据',
        sourceUnit: parsed.sourceUnit || '未知来源',
        contactPerson: parsed.contactPerson || '未指定',
        contactPhone: parsed.contactPhone || '未提供',
        resourceSummary: parsed.resourceSummary || '无描述',
        fieldClassification: parsed.fieldClassification || '未分类',
        headers: parsed.headers || []
      }
    } catch (parseError) {
      const keyValuePairs = {}
      const regex = /"([^"]+)"\s*:\s*"([^"]*)"/g
      let match
      
      while ((match = regex.exec(cleanString)) !== null) {
        keyValuePairs[match[1]] = match[2]
      }
      
      if (Object.keys(keyValuePairs).length > 0) {
        return {
          dataName: keyValuePairs.dataName || '未知数据',
          sourceUnit: keyValuePairs.sourceUnit || '未知来源',
          contactPerson: keyValuePairs.contactPerson || '未指定',
          contactPhone: keyValuePairs.contactPhone || '未提供',
          resourceSummary: keyValuePairs.resourceSummary || '无描述',
          fieldClassification: keyValuePairs.fieldClassification || '未分类'
        }
      }

      return {
        dataName: '解析错误',
        sourceUnit: '数据部',
        contactPerson: '未知',
        contactPhone: '未知',
        resourceSummary: '元数据解析失败: ' + cleanString.substring(0, 50) + '...',
        fieldClassification: '未分类'
      }
    }
  } catch (e) {
    return {
      dataName: '解析错误',
      sourceUnit: '数据部',
      contactPerson: '未知',
      contactPhone: '未知',
      resourceSummary: '元数据解析失败: ' + e.message,
      fieldClassification: '未分类'
    }
  }
}

const extractMetadata = (row) => {
  if (!row) {
    return createDefaultMetadata('未知实体')
  }

  if (row.metadata && typeof row.metadata === 'object') {

    return {
      dataName: row.metadata.dataName || row.entity || '未知数据',
      sourceUnit: row.metadata.sourceUnit || '数据部',
      contactPerson: row.metadata.contactPerson || '未指定',
      contactPhone: row.metadata.contactPhone || '未提供',
      resourceSummary: row.metadata.resourceSummary || '无',
      fieldClassification: row.metadata.fieldClassification || '未分类',
      headers: Array.isArray(row.metadata.headers) ? row.metadata.headers : []
    }
  }

  if (row.metadataJson) {
    try {
      const parsedMetadata = processMetadataString(row.metadataJson)
      return parsedMetadata
    } catch (e) {
      console.warn('解析row.metadataJson失败:', e)
    }
  }
  

  if (row.dataContent) {
    try {
      const contentObj = typeof row.dataContent === 'string' ? 
        JSON.parse(row.dataContent) : row.dataContent
      
      if (contentObj && contentObj.metadataJson) {
        const parsedMetadata = processMetadataString(contentObj.metadataJson)
        return parsedMetadata
      }

      if (contentObj && (contentObj.metadata || contentObj.dataName || contentObj.sourceUnit || 
          contentObj.contactPerson || contentObj.contactPhone)) {

        if (contentObj.metadata && typeof contentObj.metadata === 'object') {
          return {
            dataName: contentObj.metadata.dataName || row.entity || '未知数据',
            sourceUnit: contentObj.metadata.sourceUnit || '数据部',
            contactPerson: contentObj.metadata.contactPerson || '未指定',
            contactPhone: contentObj.metadata.contactPhone || '未提供',
            resourceSummary: contentObj.metadata.resourceSummary || '无',
            fieldClassification: contentObj.metadata.fieldClassification || '未分类',
            headers: contentObj.metadata.headers || []
          };
        }
        
        return {
          dataName: contentObj.dataName || row.entity || '未知数据',
          sourceUnit: contentObj.sourceUnit || '数据部',
          contactPerson: contentObj.contactPerson || '未指定',
          contactPhone: contentObj.contactPhone || '未提供',
          resourceSummary: contentObj.resourceSummary || '无',
          fieldClassification: contentObj.fieldClassification || '未分类',
          headers: contentObj.headers || []
        }
      }
    } catch (e) {
      console.warn('解析dataContent失败:', e)
    }
  }
  
  // 解析位置信息
  if (row.locationInfoJson) {
    try {
      const locationInfo = JSON.parse(row.locationInfoJson)
      if (locationInfo && locationInfo.locations) {
        const locations = locationInfo.locations
        const locationStrings = locations.map(loc => 
          `${loc.sheet || '默认'}: ${loc.startRow || '1'}-${loc.endRow || '*'} 行, ${loc.startColumn || 'A'}-${loc.endColumn || '*'} 列`
        )
        previewForm.locationInfo = locationStrings.join('; ')
      }
    } catch (e) {
      console.warn('解析位置信息JSON失败:', e)
    }
  }

  return createDefaultMetadata(row.entity)
}

// 创建默认元数据的辅助函数
const createDefaultMetadata = (entityName) => {
  entityName = entityName || '未知实体'
  let sourceUnit = '数据部'
  let contactPerson = '王主任'
  

  if (entityName.includes('用户')) {
    sourceUnit = '用户管理部'
  } else if (entityName.includes('订单')) {
    sourceUnit = '订单管理部'
    contactPerson = '李经理'
  } else if (entityName.includes('产品')) {
    sourceUnit = '产品部'
    contactPerson = '张总监'
  } else if (entityName.includes('库存')) {
    sourceUnit = '仓储部'
    contactPerson = '张管理员'
  }
  
  return {
    dataName: entityName,
    sourceUnit: sourceUnit,
    contactPerson: contactPerson,
    contactPhone: "123-456789",
    resourceSummary: `${entityName}数据资源`,
    fieldClassification: entityName.includes('用户') ? '用户数据' : 
                        (entityName.includes('订单') ? '订单数据' : 
                         (entityName.includes('库存') ? '运营数据' : '基础数据')),
    headers: []
  }
}

// 获取元数据字段值的辅助函数
const getMetadataValue = (fieldName) => {

  if (!previewForm.metadata) return null
  

  if (previewForm.metadata[fieldName]) {
    return previewForm.metadata[fieldName]
  }
  

  const checkNestedObject = (obj, field) => {

    if (typeof obj !== 'object' || obj === null) return null
    if (obj[field] !== undefined) return obj[field]

    for (const key in obj) {
      if (typeof obj[key] === 'object' && obj[key] !== null) {
        const result = checkNestedObject(obj[key], field)
        if (result !== null) return result
      }
    }
    
    return null
  }
  

  return checkNestedObject(previewForm.metadata, fieldName)
}

// 获取当前日期时间的格式化字符串
const getCurrentDateTime = () => {
  const now = new Date()
  return now.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false
  })
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

// 处理下载数字对象
const handleDownload = () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要下载的数据对象');
    return;
  }
  selectedRows.value.forEach(row => {
    if (row.excelData) {
      downloadExcelFile(row);
    } else {
      ElMessage.info(`${row.entity} 没有可下载的数据，请先点击实体名进行预览`);
    }
  });
}

// 下载Excel文件
const downloadExcelFile = (row) => {
  try {

    const blob = new Blob([row.excelData], { 
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' 
    });

    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = `${row.entity}.xlsx`;

    document.body.appendChild(link);
    link.click();

    document.body.removeChild(link);
    URL.revokeObjectURL(link.href);
    
    ElMessage.success(`${row.entity} 已下载`);
  } catch (error) {
    console.error('下载文件时出错:', error);
    ElMessage.error(`下载 ${row.entity} 时出错: ${error.message}`);
  }
}

// 重置解密状态
const resetDecryption = () => {
  ElMessageBox.confirm('确定要重新解密？当前解密的数据将不再显示。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    isDecrypted.value = false;
    decryptedObjectId.value = '';
    decryptedObjectIds.value = [];
    decryptForm.objectId = '';
    decryptForm.dataCapsule = '';
    decryptDialogVisible.value = false;
  }).catch(() => {
  });
}

const fetchLatestDataFromApi = async () => {
  try {
    ElMessage.info('正在从API获取最新数据...')

    const apiUrl = 'http://localhost:8081/api/objects/list'
    const response = await axios.get(apiUrl)
    
    if (response.data) {

      let newData = []
      
      if (Array.isArray(response.data)) {
        newData = response.data
      } else if (response.data.list && Array.isArray(response.data.list)) {
        newData = response.data.list
      } else if (response.data.data && Array.isArray(response.data.data)) {
        newData = response.data.data
      }
      
      if (newData.length > 0) {

        dataObjectService.updateDataObjects(newData)
        ElMessage.success(`成功获取${newData.length}条最新数据`)
      } else {
        ElMessage.warning('API返回的数据为空')
      }
    }
  } catch (error) {
    console.error('获取最新数据失败:', error)
    ElMessage.error(`获取最新数据失败: ${error.message}`)
  }
}

const directoryDialogVisible = ref(false)

const showDirectoryDialog = () => {

  fetchLatestDataFromApi()
    .then(() => {
      console.log('成功获取最新数据，显示目录对话框')
      directoryDialogVisible.value = true
    })
    .catch(error => {
      console.error('获取数据失败，但仍然显示目录对话框:', error)
      directoryDialogVisible.value = true
    })
}

// 处理查看目录项目
const handleViewDirectoryItem = (item) => {

  directoryDialogVisible.value = false
  previewEntity(item)
}

// 添加三维可视化相关
const visualizationVisible = ref(false)
const showVisualization = () => {
  visualizationVisible.value = true
}

// 处理使用方初始化
const handleInitUser = async () => {
  try {
    const loadingInstance = ElLoading.service({
      fullscreen: true,
      text: '正在初始化使用方系统...',
      background: 'rgba(0, 0, 0, 0.7)'
    });
    
    const response = await axios.post('http://localhost:8083/api/send-du-info');
    
    loadingInstance.close();
    
    if (response.data && (response.data.code === 1 || response.data.success === true)) {
      ElMessage.success('使用方系统初始化成功');
      
      openScrDialog()
    } else {
      ElMessage.warning(`使用方系统初始化失败: ${response.data?.message || response.data?.msg || '未知错误'}`);
    }
  } catch (error) {
    console.error('使用方系统初始化失败:', error);
    
    if (error.response) {
      if (error.response.status === 404) {
        ElMessage.error('使用方服务未启动或接口不存在');
      } else if (error.response.status === 500) {
        ElMessage.error(`使用方服务错误: ${error.response.data?.message || '内部服务器错误'}`);
      } else {
        ElMessage.error(`初始化失败 (${error.response.status}): ${error.response.data?.message || error.message}`);
      }
    } else if (error.request) {
      ElMessage.error('无法连接到使用方服务，请确保服务已启动');
    } else {
      ElMessage.error(`使用方系统初始化失败: ${error.message || '未知错误'}`);
    }
  }
};

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

    if (response.data && (response.data.code === 1 || response.data.success === true) && 
        !(response.data.msg && response.data.msg.includes('验证失败'))) {
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
};


const headerCellStyle = ({ column }) => {
  const blueProps = [
    'id',
    'entity',
    'locationInfo',
    'constraint',
    'transferControl',
    'auditInfo',
    'classificationLevelValue'
  ];
  if (blueProps.includes(column.property)) {
    return {
      background: '#eaf6ff',
      color: '#1677c7',
      fontWeight: 'bold',
      fontSize: '16px',
      textAlign: 'center',
      padding: '10px 0'
    };
  }

  return {
    background: '#f5f7fa',
    color: '#606266',
    fontWeight: 'bold',
    fontSize: '15px',
    textAlign: 'center',
    padding: '10px 0'
  };
};

function getLocationInfoObj(locationInfo, locationInfoJson) {
  if (locationInfoJson) {
    try {
      return typeof locationInfoJson === 'object' ? locationInfoJson : JSON.parse(locationInfoJson);
    } catch {}
  }
  if (locationInfo) {
    if (typeof locationInfo === 'object') return locationInfo;
    try {
      return JSON.parse(locationInfo);
    } catch {}
  }
  return null;
}

const scrDialogVisible = ref(false)
const scrForm = ref({
  metaData: '',
  fno: '',
  sfno: ''
})
function openScrDialog() {
  scrForm.value = { metaData: '', fno: '', sfno: '' }
  scrDialogVisible.value = true
}
async function submitScrForm() {
  const scrLoading = ElLoading.service({
    fullscreen: true,
    text: '正在构造共享证书申请...',
    background: 'rgba(0, 0, 0, 0.7)'
  })
  try {
    const scrResponse = await axios.post('http://localhost:8083/api/generate-scr', {
      metaData: scrForm.value.metaData,
      fno: scrForm.value.fno,
      sfno: scrForm.value.sfno
    })
    scrLoading.close()
    scrDialogVisible.value = false
    if (scrResponse.data && (scrResponse.data.code === 1 || scrResponse.data.success === true)) {
      ElMessage.success('共享证书申请构造成功')
    } else {
      ElMessage.warning(`共享证书申请构造失败: ${scrResponse.data?.message || scrResponse.data?.msg || '未知错误'}`)
    }
  } catch (e) {
    scrLoading.close()
    ElMessage.error('请求失败: ' + e.message)
  }
}

function setWatermark(text) {
  const id = 'global-watermark-bg'
  let wm = document.getElementById(id)
  if (wm) {
    wm.parentNode.removeChild(wm)
  }
  const can = document.createElement('canvas')
  can.width = 300
  can.height = 200
  const ctx = can.getContext('2d')
  ctx.rotate(-20 * Math.PI / 180)
  ctx.font = '16px Microsoft YaHei'
  ctx.fillStyle = 'rgba(150,150,150,0.22)'
  ctx.textAlign = 'left'
  ctx.textBaseline = 'middle'
  ctx.fillText(text, 40, 100)
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
  setWatermark('使用方')
  window.addEventListener('resize', () => setWatermark('使用方'))
})
onBeforeUnmount(() => {
  removeWatermark()
  window.removeEventListener('resize', () => setWatermark('使用方'))
})
</script>

<style scoped>
.datasource-container {
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #ffffff;
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

/* 状态筛选按钮区域 */
.status-filter {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.status-btn {
  border: none;
}

.status-btn.active {
  background-color: #1890ff;
  color: #ffffff;
}

/* 搜索和操作区域 */
.action-bar {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
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

/* 确保ID单元格不被全局样式覆盖 */
:deep(.el-table .el-table__cell .id-cell) {
  white-space: normal;
  overflow: visible;
  text-overflow: clip;
  word-break: break-all;
}

:deep(.el-table .el-table__cell:last-child .cell) {
  padding-right: 5px;
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

/* 状态标签样式 */
.status-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
}

/* 移除按钮点击后的黑色边框 */
.el-button:focus {
  outline: none !important;
  box-shadow: none !important;
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
  background-color: #f4f4f5;
  color: #909399;
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

.total-text {
  font-size: 14px;
  color: #8c8c8c;
}

/* 对话框样式 */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 20px;
}

/* 数据锁定占位符样式 */
.data-locked-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  width: 100%;
  background-color: #f8f9fa;
  border-radius: 4px;
  border: 1px dashed #dcdfe6;
  padding: 40px 0;
}

.locked-icon {
  font-size: 60px;
  color: #909399;
  margin-bottom: 20px;
}

.data-locked-placeholder p {
  font-size: 16px;
  color: #606266;
  text-align: center;
  line-height: 1.6;
  max-width: 80%;
}

.locked-subtitle {
  font-size: 14px;
  color: #8c8c8c;
  text-align: center;
  line-height: 1.6;
  max-width: 80%;
}

/* 全局对话框样式 - 放在样式的最底部以确保最高优先级 */
:deep(.el-overlay) {
  overflow: hidden;
}

:deep(.decrypt-dialog) {
  display: flex;
  align-items: center;
  justify-content: center;
}

:deep(.decrypt-dialog .el-dialog) {
  margin: 0 auto !important;
  position: relative !important;
  top: 0 !important;
  transform: none !important;
  max-width: 90%;
}

:deep(.edit-dialog) {
  display: flex;
  align-items: center;
  justify-content: center;
}

:deep(.edit-dialog .el-dialog) {
  margin: 0 auto !important;
  position: relative !important;
  top: 0 !important;
  transform: none !important;
  max-width: 90%;
}

/* 确保对话框居中显示 */
:deep(.el-dialog) {
  margin: 0 auto !important;
  position: fixed !important;
  top: 50% !important;
  left: 50% !important;
  transform: translate(-50%, -50%) !important;
  max-width: 90%;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

:deep(.el-dialog__header) {
  padding: 20px;
  text-align: center;
  border-bottom: 1px solid #f0f0f0;
  cursor: move;
  font-weight: bold;
}

:deep(.el-dialog__body) {
  padding: 30px 20px;
}

:deep(.el-dialog__footer) {
  padding: 10px 20px 20px;
  text-align: center;
}

/* 添加预览相关的样式 */
.preview-header {
  margin-bottom: 15px;
}

.preview-info {
  display: flex;
  flex-direction: column;
  gap: 10px;
  background-color: #f8f9fa;
  border-radius: 4px;
  padding: 10px;
}

/* 新的基本信息表格样式 */
.basic-info-table {
  width: 100%;
  display: flex;
  flex-wrap: nowrap;
  gap: 20px;
  justify-content: center;
  margin-bottom: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
  padding: 12px 15px;
  overflow-x: auto;
  white-space: nowrap;
}

.info-item {
  display: inline-block;
  padding: 0 10px;
  color: #333;
  font-size: 14px;
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  text-align: center;
}

.constraint-info {
  max-width: 500px;
}

.info-item strong {
  font-weight: bold;
  color: #606266;
  margin-right: 5px;
}

/* 元数据部分样式 */
.metadata-section {
  margin: 10px auto 5px;
  padding: 8px 10px;
  background-color: #f9f9f9;
  border-radius: 4px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  border: 1px solid #eaeaea;
}

.metadata-items {
  display: flex;
  flex-wrap: nowrap;
  justify-content: center;
  overflow-x: auto;
  padding-bottom: 3px;
  scrollbar-width: thin;
}

.metadata-items::-webkit-scrollbar {
  height: 3px;
}

.metadata-items::-webkit-scrollbar-thumb {
  background-color: rgba(0, 0, 0, 0.1);
  border-radius: 3px;
}

.metadata-item {
  padding: 4px 8px;
  margin: 0 8px;
  white-space: nowrap;
  flex-shrink: 0;
  font-size: 13px;
}

/* 表头信息部分样式 */
.headers-section {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px dashed #e0e0e0;
}

.headers-title {
  font-size: 13px;
  color: #606266;
  margin-bottom: 8px;
  text-align: center;
  font-weight: bold;
}

.headers-list {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 5px;
  padding: 0 15px;
}

.header-tag {
  margin: 2px;
  font-size: 12px;
}

/* ID单元格样式 */
.id-cell {
  width: 100%;
  overflow: visible;
  white-space: normal;
  word-break: break-all;
  padding: 2px 5px;
  text-align: left;
  text-align: center;
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

/* 分类分级值样式 */
.classification-level-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5px;
}

.classification-level-item {
  display: flex;
  gap: 5px;
  font-size: 12px;
  line-height: 1.5;
}

.classification-level-item .label {
  font-weight: bold;
  color: #606266;
}

.classification-level-item .value {
  color: #409EFF;
}

/* 分类分级值样式 */
.classification-level-section {
  margin-top: 10px;
  padding-top: 5px;
  border-top: 1px dashed #eaeaea;
  text-align: center;
}

.classification-level-items {
  display: flex;
  justify-content: center;
  gap: 30px;
  padding: 5px 0;
}

.classification-level-item {
  display: flex;
  align-items: center;
  gap: 5px;
}

.classification-level-item .label {
  font-weight: bold;
  color: #606266;
}

.classification-level-item .value {
  color: #409EFF;
  font-weight: bold;
}

/* 表头信息部分样式 */
.table-title {
  font-size: 33px;
  font-weight: bold;
  text-align: center;
  margin-bottom: 12px;
  color: #222;
}

.select-fields-link {
  text-decoration: underline;
}
</style> 