<template>
  <el-dialog v-model="dialogVisible" title="申请列表" width="1200px" :close-on-click-modal="false">
    <el-table 
      :data="currentGroupData" 
      border 
      stripe 
      style="width: 100%"
      :span-method="spanMethod"
      :row-style="{ height: '60px' }"
    >
      <el-table-column prop="groupId" label="组序号" width="120" align="center">
        <template #default="scope">
          <span style="font-weight: bold;">{{ scope.row.groupId }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="objectId" label="ID" min-width="240"  align="center"/>
      <el-table-column prop="entity" label="实体名" min-width="120" align="center" />
      <el-table-column prop="applicant" label="使用方用户名" min-width="120" align="center" />
      <el-table-column label="操作" width="160" align="center">
        <template #default="scope">
          <el-button type="primary" plain @click="handleGenerateDV" size="small">生成数据凭证</el-button>
        </template>
      </el-table-column>
      <el-table-column label="申请状态" min-width="160" align="center">
        <template #default="scope">
          <el-tag :type="getStatusTagType(getStatusText(scope.row))">
            {{ getStatusText(scope.row) }}
          </el-tag>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 分页组件 -->
    <div class="pagination-area" style="margin-top: 20px; display: flex; justify-content: flex-end;">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="1"
        layout="prev, pager, next, total"
        :total="totalGroups"
        @current-change="handlePageChange"
        background
      />
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, defineProps, watch, computed } from 'vue'
import { ElMessage, ElLoading, ElMessageBox } from 'element-plus'
import axios from 'axios'

const props = defineProps({
  visible: Boolean
})
const emit = defineEmits(['update:visible', 'close'])

const dialogVisible = ref(props.visible)
watch(() => props.visible, v => dialogVisible.value = v)

// 分页相关
const currentPage = ref(1)
const totalGroups = ref(0)

// 分组数据
const groupData = ref([
  {
    groupId: 'G001',
    applyTime: '2024-01-15 10:30:00',
    dataCredentialStatus: false, 
    applications: [
      {
        id: 1,
        objectId: '2e5f5221-e3b2-46de-ae66-53330b14e55a',
        applicant: '浙江省税务局',
        entity: 'CivilAffairs'
      },
      {
        id: 2,
        objectId: 'bc1e9474-6a35-4d5d-9e50-3c99a518a65d',
        applicant: '浙江省税务局',
        entity: 'EducationData'
      },
      {
        id: 3,
        objectId: '7ad2b71e-1804-4720-b20c-ea11cf18499a',
        applicant: '浙江省税务局',
        entity: 'HealthRecord'
      }
    ]
  },
  {
    groupId: 'G002',
    applyTime: '2024-01-16 14:20:00',
    dataCredentialStatus: true, // 数据凭证状态
    applications: [
      {
        id: 4,
        objectId: 'OBJ004_FinancialData',
        applicant: '浙江省教育厅',
        entity: 'FinancialData'
      },
      {
        id: 5,
        objectId: 'OBJ005_TrafficInfo',
        applicant: '浙江省教育厅',
        entity: 'TrafficInfo'
      }
    ]
  },
  {
    groupId: 'G003',
    applyTime: '2024-01-18 16:30:00',
    dataCredentialStatus: false, // 数据凭证状态
    applications: [
      {
        id: 6,
        objectId: 'OBJ006_PolicyData',
        applicant: '浙江省民政厅',
        entity: 'PolicyData'
      }
    ]
  }
])

// 当前组数据
const currentGroupData = computed(() => {
  const groupIndex = currentPage.value - 1
  const group = groupData.value[groupIndex]
  if (!group) return []
  
  return group.applications.map(app => ({
    ...app,
    groupId: group.groupId,
    applyTime: group.applyTime,
    dataCredentialStatus: group.dataCredentialStatus
  }))
})

const tableData = ref([])

// 单元格合并方法
const spanMethod = ({ row, column, rowIndex, columnIndex }) => {
  // 组序号列(第0列)、用户名列(第3列)、操作列(第4列)、申请状态列(第5列)需要合并
  if (columnIndex === 0 || columnIndex === 3 || columnIndex === 4 || columnIndex === 5) {
    if (rowIndex === 0) {
      return {
        rowspan: currentGroupData.value.length,
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

// 分页处理
const handlePageChange = (page) => {
  currentPage.value = page
}

function fetchRecords() {
  // 使用模拟数据，设置总组数
  totalGroups.value = groupData.value.length
  currentPage.value = 1
  
  // 如果需要从API获取数据，可以取消注释下面的代码
  /*
  axios.get('http://localhost:8080/api/applications/records', { withCredentials: true })
    .then(res => {
      if (res.data && res.data.code === 1) {
        tableData.value = res.data.data || []
      } else {
        ElMessage.error(res.data.msg || '获取申请记录失败')
      }
    })
    .catch(err => {
      ElMessage.error('获取申请记录失败: ' + (err.response?.data?.message || err.message))
    })
  */
}

watch(dialogVisible, v => {
  emit('update:visible', v)
  if (v) fetchRecords()
  if (!v) emit('close')
})

// 生成数据凭证方法
const handleGenerateDV = async () => {
  try {
    // 显示加载状态
    const loadingInstance = ElLoading.service({
      fullscreen: true,
      text: '正在生成数据凭证...',
      background: 'rgba(0, 0, 0, 0.7)'
    });
    
    // 调用生成数据凭证接口
    const response = await axios.post('http://localhost:8081/api/generate-dv');
    
    loadingInstance.close();
    
    if (response.data && response.data.code === 0 && response.data.msg && response.data.msg.includes("生成DV失败")) {
      // 生成DV失败的情况，直接显示错误信息，不弹出加密确认框
      ElMessage.error(response.data.msg || '生成DV失败');
    } else if (response.data && (response.data.code === 1 || response.data.success === true)) {
      // 更新当前组的数据凭证状态
      const groupIndex = currentPage.value - 1
      const group = groupData.value[groupIndex]
      if (group) {
        group.dataCredentialStatus = true
      }
      
      // 生成凭证成功后询问是否加密并签名
      ElMessageBox.confirm('数据凭证生成成功，是否加密签名并发送?', '确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info',
      }).then(async () => {
        try {
          const encryptLoading = ElLoading.service({
            fullscreen: true,
            text: '正在加密并签名...',
            background: 'rgba(0, 0, 0, 0.7)'
          });
          
          // 调用加密并签名接口
          const encryptResponse = await axios.post('http://localhost:8081/api/encrypt-and-send');
          
          encryptLoading.close();
          
          if (encryptResponse.data && encryptResponse.data.code === 0 && encryptResponse.data.msg && encryptResponse.data.msg.includes("加密发送失败")) {
            // 加密发送失败的特殊错误处理
            ElMessage.error(encryptResponse.data.msg);
          } else if (encryptResponse.data && (encryptResponse.data.code === 1 || encryptResponse.data.success === true)) {
            ElMessage.success('加密并签名成功');
          } else {
            ElMessage.warning(`加密失败: ${encryptResponse.data?.message || encryptResponse.data?.msg || '未知错误'}`);
          }
        } catch (encryptError) {
          console.error('加密并签名失败:', encryptError);
          
          if (encryptError.response) {
            if (encryptError.response.status === 404) {
              ElMessage.error('加密服务未启动或接口不存在');
            } else if (encryptError.response.status === 500) {
              ElMessage.error(`加密服务错误: ${encryptError.response.data?.message || '内部服务器错误'}`);
            } else {
              ElMessage.error(`加密失败 (${encryptError.response.status}): ${encryptError.response.data?.message || encryptError.message}`);
            }
          } else if (encryptError.request) {
            ElMessage.error('无法连接到加密服务，请确保服务已启动');
          } else {
            ElMessage.error(`加密并签名失败: ${encryptError.message || '未知错误'}`);
          }
        }
      }).catch(() => {
        // 用户取消加密操作
        ElMessage.info('已取消加密操作');
      });
    } else {
      if (response.data && response.data.code === 0) {
        ElMessage.error('生成DV失败');
      } else {
        ElMessage.warning(`生成数据凭证失败: ${response.data?.message || response.data?.msg || '未知错误'}`);
      }
    }
  } catch (error) {
    console.error('生成数据凭证失败:', error);
    
    if (error.response) {
      if (error.response.status === 404) {
        ElMessage.error('凭证服务未启动或接口不存在');
      } else if (error.response.status === 500) {
        ElMessage.error(`凭证服务错误: ${error.response.data?.message || '内部服务器错误'}`);
      } else {
        ElMessage.error(`生成失败 (${error.response.status}): ${error.response.data?.message || error.message}`);
      }
    } else if (error.request) {
      ElMessage.error('无法连接到凭证服务，请确保服务已启动');
    } else {
      ElMessage.error(`生成数据凭证失败: ${error.message || '未知错误'}`);
    }
  }
}
function getStatusText(row) {
  if (row.dataCredentialStatus === true) return '数源方已生成数据凭证'
  return '数源方待生成数据凭证'
}
function getStatusTagType(status) {
  if (!status) return 'info'
  if (status.includes('已')) return 'success'
  if (status.includes('待')) return 'danger'
  return 'info'
}
</script>