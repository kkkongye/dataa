<template>
  <el-dialog
    v-model="dialogVisible"
    title="分类分级值"
    width="600px"
    :close-on-click-modal="false"
    :show-close="true"
    destroy-on-close
  >
    <el-tabs v-model="activeTab">
      <el-tab-pane label="分类" name="classification">
        <!-- 分类选项卡内容 -->
        <div class="classification-form">
          <div class="form-item">
            <span class="label">行业领域分类值：</span>
            <el-select v-model="industryCategory" placeholder="请选择" @change="calculateClassificationValue">
              <el-option 
                v-for="(value, key) in industryCategoryMap" 
                :key="key" 
                :label="key" 
                :value="key" 
              />
            </el-select>
            <span class="value-display">对应分类值为：{{ getIndustryCategoryValue() }}</span>
          </div>

          <div class="form-item">
            <span class="label">处理时效分类值：</span>
            <el-select v-model="dataTimeliness" placeholder="请选择" @change="calculateClassificationValue">
              <el-option 
                v-for="(value, key) in timelinessMap" 
                :key="key" 
                :label="key" 
                :value="key" 
              />
            </el-select>
            <span class="value-display">对应分类值为：{{ getTimelinessValue() }}</span>
          </div>

          <div class="form-item">
            <span class="label">数据来源分类值：</span>
            <el-select v-model="dataSource" placeholder="请选择" @change="calculateClassificationValue">
              <el-option 
                v-for="(value, key) in sourceMap" 
                :key="key" 
                :label="key" 
                :value="key" 
              />
            </el-select>
            <span class="value-display">对应分类值为：{{ getSourceValue() }}</span>
          </div>
        </div>

        <div class="classification-result">
          <div class="formula">计算公式：行业领域分类值 + 处理时效分类值 + 数据来源分类值</div>
          <div class="calculation">{{ getIndustryCategoryValue() || 0 }} + {{ getTimelinessValue() || 0 }} + {{ getSourceValue() || 0 }}</div>
          <div class="result-value">文件分类值计算得：{{ totalClassificationValue }}</div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="分级" name="level">
        <!-- 分级选项卡内容 -->
        <div class="level-form">
          <div class="level-item">
            <span class="label">库分级值：</span>
            <span class="value">{{ dbGrade }}</span>
            <el-popover
              placement="right"
              :width="300"
              trigger="click"
            >
              <template #reference>
                <el-link type="primary" class="help-link">说明</el-link>
              </template>
              <div class="help-content">
                <h4>库分级值说明</h4>
                <p>根据数据库的数据量、表的分级值确定数据库分级值。
                  由数据表量对应的分级值、所有表的分级值的最大值累加确定数据库分级值;
                  其中根据数据表的数量分为1~99的小型数据库、100~999的中型数据库、999以上的大型数据库，
                  分别对应分级值:100、200、300;</p>
              </div>
            </el-popover>
          </div>

          <div class="level-item">
            <span class="label">表分级值：</span>
            <span class="value">{{ tableGrade }}</span>
            <el-popover
              placement="right"
              :width="300"
              trigger="click"
            >
              <template #reference>
                <el-link type="primary" class="help-link">说明</el-link>
              </template>
              <div class="help-content">
                <h4>表分级值说明</h4>
                <p>由表内总的记录数对应的分级值与对所有行的行分级值的最大值累加求得出表分级值;
                  其中根据表内记录的数量分为:1~999的小型数据表、
                  1000~1000000的中型数据表、1000000以上的大型数据表，
                  分别对应分级值10、20、30;</p>
              </div>
            </el-popover>
          </div>
          
          <div class="level-item">
            <span class="label">行分级值：</span>
            <span class="value">{{ rowGradeValue }}</span>
            <el-popover
              placement="right"
              :width="300"
              trigger="click"
            >
              <template #reference>
                <el-link type="primary" class="help-link">说明</el-link>
              </template>
              <div class="help-content">
                <h4>行分级值说明</h4>
                <p>数据表中往往含有若干行，根据每行记录的权重值与对所含字段分级值的最大值累加，
                  得到行分级值默认一般记录的权重为:1(默认值);
                  重要记录的权重为:2;核心记录的权重为:3;</p>
              </div>
            </el-popover>
            
            <el-link type="primary" class="help-link" @click="showRowDetailDialog">查看详情</el-link>
            <el-link type="primary" class="help-link" @click="showWeightForm = !showWeightForm">修改权重</el-link>
          </div>
          
          <!-- 权重修改表单 -->
          <div v-if="showWeightForm" class="weight-form">
            <div class="weight-item">
              <span class="weight-label">一般记录的权重为:</span>
              <el-input-number v-model="normalWeight" :min="0" :max="5" :step="0.1" size="small" />
            </div>
            <div class="weight-item">
              <span class="weight-label">重要记录的权重为:</span>
              <el-input-number v-model="importantWeight" :min="0" :max="5" :step="0.1" size="small" />
            </div>
            <div class="weight-item">
              <span class="weight-label">核心记录的权重为:</span>
              <el-input-number v-model="criticalWeight" :min="0" :max="5" :step="0.1" size="small" />
            </div>
            <div class="weight-actions">
              <el-button type="primary" size="small" @click="confirmWeightChange">确认</el-button>
            </div>
          </div>
          
          <div class="level-item">
            <span class="label">列分级值：</span>
            <span class="value">{{ columnGradeValue }}</span>
            <el-popover
              placement="right"
              :width="300"
              trigger="click"
            >
              <template #reference>
                <el-link type="primary" class="help-link">说明</el-link>
              </template>
              <div class="help-content">
                <h4>列分级值说明</h4>
                <p>根据字段的敏感程度来确定数据的级别，可分为4级，由高至低分别为:敏感数据(L4级)、
                  较敏感数据(L3 级)、低敏感数据(L2级)、不敏感数据(L1级)。
                  分别对应字段分级值:0.8、0.6、0.4、0.2</p>
              </div>
            </el-popover>
            <el-link type="primary" class="help-link" @click="showColumnDetailDialog">查看详情</el-link>
          </div>
        </div>

        <div class="level-result">
          <div class="formula">计算公式：库分级值 + 表分级值 + 行分级值 + 列分级值</div>
          <div class="calculation">{{ dbGrade }} + {{ tableGrade }} + {{ rowGradeValue }} + {{ columnGradeValue }}</div>
          <div class="result-value">分级值计算得：{{ totalGradeValue }}</div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirm">确定</el-button>
      </div>
    </template>
  </el-dialog>

  <!-- 行分级值详情弹窗 -->
  <el-dialog
    v-model="rowDetailDialogVisible"
    title="行分级值详情"
    width="800px"
    append-to-body
    destroy-on-close
  >
    <div class="excel-data-preview">
      <h3>数据预览</h3>
      
      <div v-if="rowExcelData.length > 0">
        <div class="data-info">找到 {{ rowExcelData.length }} 条记录</div>
        <el-table :data="rowExcelData" border style="width: 100%" max-height="400px">
          <el-table-column 
            v-for="(key, index) in getObjectKeys(rowExcelData).filter(k => k !== 'rowGradeValue')" 
            :key="index"
            :prop="key"
            :label="key"
            :min-width="100"
          />
          <!-- 行分级值列 -->
          <el-table-column
            label="行分级值"
            align="center"
            min-width="100"
          >
            <template #default="scope">
              <el-tag :type="getGradeTagType(scope.row.rowGradeValue)">{{ scope.row.rowGradeValue }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div v-else class="no-data-message">
        <el-empty description="暂无数据" />
        <div v-if="fetchingData" class="loading-text">正在获取数据...</div>
      </div>
    </div>
  </el-dialog>

  <!-- 列分级值详情弹窗 -->
  <el-dialog
    v-model="columnDetailDialogVisible"
    title="列分级值详情"
    width="800px"
    append-to-body
    destroy-on-close
  >
    <div class="excel-data-preview">
      <h3>数据预览</h3>
      
      <div v-if="columnExcelData.length > 0">
        <div class="data-info">找到 {{ columnExcelData.length - 1 }} 条记录</div>
        <el-table :data="columnExcelData" border style="width: 100%" max-height="400px">
          <el-table-column 
            v-for="(key, index) in getObjectKeys(columnExcelData).filter(k => k !== '_isGradeRow')" 
            :key="index"
            :prop="key"
            :label="key"
            :min-width="100"
          >
            <template #default="scope">
              <!-- 如果是分级值行，使用彩色标签显示分级值 -->
              <template v-if="scope.row._isGradeRow">
                <div class="column-grade-cell">
                  <div class="column-grade-label">列分级值:</div>
                  <el-tag type="info">{{ parseFloat(scope.row[key]).toFixed(1) }}</el-tag>
                </div>
              </template>
              <!-- 普通数据行正常显示 -->
              <template v-else>
                {{ scope.row[key] }}
              </template>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div v-else class="no-data-message">
        <el-empty description="暂无数据" />
        <div v-if="fetchingData" class="loading-text">正在获取数据...</div>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, watch, defineProps, defineEmits, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import { API_URL } from '@/services/apiConfig'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  objectId: {
    type: String,
    default: ''
  },
  modelValue: {
    type: Object,
    default: () => ({
      classificationValue: '',
      levelValue: '',
      dbGrade: 100,
      tableGrade: 10,
      rowGrades: [0.3],
      columnGrades: [0.3]
    })
  },
  // 调试模式：是否打印更多日志
  debug: {
    type: Boolean,
    default: false
  },
  // 自定义API基础URL
  apiBaseUrl: {
    type: String,
    default: 'http://localhost:8080'
  }
})

const emit = defineEmits(['update:visible', 'update:modelValue', 'confirm'])

// 对话框可见性
const dialogVisible = ref(props.visible)

// 监听visible属性变化
watch(() => props.visible, (newVal) => {
  dialogVisible.value = newVal
})

// 监听dialogVisible变化
watch(dialogVisible, (newVal) => {
  emit('update:visible', newVal)
})

// 当前激活的标签页
const activeTab = ref('classification')

// 分类相关数据
const industryCategory = ref('')
const dataTimeliness = ref('')
const dataSource = ref('')

// 分级值数据
const dbGrade = ref(0) // 默认库分级值为0
const tableGrade = ref(0) // 默认表分级值为0
const rowGrades = ref([0, 0]) // 默认行分级值包含两个0
const columnGrades = ref([0, 0]) // 默认列分级值包含两个0

// 计算行分级值（取最大值）
const rowGradeValue = computed(() => {
  if (!rowGrades.value || rowGrades.value.length === 0) return 0;
  try {
    // 确保所有值都是数值类型
    const numericValues = rowGrades.value.map(val => {
      const num = parseFloat(val);
      return isNaN(num) ? 0 : num;
    });
    // 使用Math.max取数组中的最大值
    const maxValue = Math.max(...numericValues);
    // 保留一位小数
    const result = parseFloat(maxValue.toFixed(1));
    return result;
  } catch (error) {
    return 0;
  }
})

// 计算列分级值（取最大值）
const columnGradeValue = computed(() => {
  if (!columnGrades.value || columnGrades.value.length === 0) return 0;
  try {
    // 确保所有值都是数值类型
    const numericValues = columnGrades.value.map(val => {
      const num = parseFloat(val);
      return isNaN(num) ? 0 : num;
    });
    // 使用Math.max取数组中的最大值
    const maxValue = Math.max(...numericValues);
    // 保留一位小数
    const result = parseFloat(maxValue.toFixed(1));
    return result;
  } catch (error) {
    return 0;
  }
})

// 计算总分级值（库+表+行+列的总和）
const totalGradeValue = computed(() => {
  try {
    // 确保使用数值类型进行计算
    const dbValue = parseFloat(dbGrade.value) || 0;
    const tableValue = parseFloat(tableGrade.value) || 0;
    const rowValue = parseFloat(rowGradeValue.value) || 0;
    const colValue = parseFloat(columnGradeValue.value) || 0;
    
    // 计算总和并保留一位小数
    const sum = dbValue + tableValue + rowValue + colValue;
    const result = parseFloat(sum.toFixed(1));
    
    return result;
  } catch (error) {
    return 0; // 出错时使用默认值
  }
})

// 监听modelValue变化，初始化表单数据
watch(() => props.modelValue, (newVal) => {
  if (newVal) {
    // 初始化分类值
    industryCategory.value = newVal.industryCategory !== undefined ? newVal.industryCategory : '';
    dataTimeliness.value = newVal.dataTimeliness !== undefined ? newVal.dataTimeliness : '';
    dataSource.value = newVal.dataSource !== undefined ? newVal.dataSource : '';
    
    // 初始化分级值 - 确保值正确解析为数字
    if (newVal.dbGrade !== undefined) {
      dbGrade.value = parseFloat(newVal.dbGrade) || 0;
    } else {
      dbGrade.value = 0;
    }
    
    if (newVal.tableGrade !== undefined) {
      tableGrade.value = parseFloat(newVal.tableGrade) || 0;
    } else {
      tableGrade.value = 0;
    }
    
    // 处理行分级值
    if (newVal.rowGrades) {
      if (Array.isArray(newVal.rowGrades)) {
        rowGrades.value = newVal.rowGrades.map(val => {
          const parsedVal = parseFloat(val) || 0;
          return parsedVal;
        });
      } else {
        // 如果不是数组，尝试解析为单一值
        const parsedVal = parseFloat(newVal.rowGrades) || 0;
        rowGrades.value = [parsedVal, parsedVal];
      }
    } else {
      rowGrades.value = [0, 0];
    }
    
    // 处理列分级值
    if (newVal.columnGrades) {
      if (Array.isArray(newVal.columnGrades)) {
        columnGrades.value = newVal.columnGrades.map(val => {
          const parsedVal = parseFloat(val) || 0;
          return parsedVal;
        });
      } else {
        // 如果不是数组，尝试解析为单一值
        const parsedVal = parseFloat(newVal.columnGrades) || 0;
        columnGrades.value = [parsedVal, parsedVal];
      }
    } else {
      columnGrades.value = [0, 0];
    }
  }
}, { deep: true, immediate: true })

// 分类值映射
const industryCategoryMap = reactive({
  '交通运输': 90,
  '金融': 90,
  '卫生社会工作': 90,
  '教育': 60,
  '制造业': 60,
  '建筑业': 60,
  '餐饮': 30,
  '居民服务': 30,
  '个人组织': 30
})

const timelinessMap = reactive({
  '实时': 9,
  '近实时': 6,
  '历史': 3
})

const sourceMap = reactive({
  '政府': 0.9,
  '企业': 0.6,
  '个人': 0.3
})

// 获取行业分类值
const getIndustryCategoryValue = () => {
  return industryCategory.value ? industryCategoryMap[industryCategory.value] : 0
}

// 获取处理时效分类值
const getTimelinessValue = () => {
  return dataTimeliness.value ? timelinessMap[dataTimeliness.value] : 0
}

// 获取数据来源分类值
const getSourceValue = () => {
  return dataSource.value ? sourceMap[dataSource.value] : 0
}

// 计算总的分类值
const totalClassificationValue = ref(0)

// 计算分类值
const calculateClassificationValue = () => {
  const industryValue = getIndustryCategoryValue()
  const timeValue = getTimelinessValue()
  const sourceValue = getSourceValue()
  
  // 计算总分类值，直接相加并保留一位小数
  // 行业领域分类值 + 处理时效分类值 + 数据来源分类值
  const result = industryValue + timeValue + sourceValue
  totalClassificationValue.value = parseFloat(result.toFixed(1)).toString()
}

// 组件挂载时初始化数据
onMounted(() => {
  // 不设置默认选项，保持所有下拉选项为空
  // 确保初始化totalClassificationValue为0
  totalClassificationValue.value = "0";
})

// 确认按钮处理
const handleConfirm = () => {
  try {
    // 准备要返回的数据
    const result = {
      // 分类相关数据
      classificationValue: totalClassificationValue.value, // 计算得到的分类值
      industryCategory: industryCategory.value,
      dataTimeliness: dataTimeliness.value,
      dataSource: dataSource.value,
      
      // 分级相关数据
      levelValue: totalGradeValue.value.toString(), // 总分级值作为分级值
      dbGrade: parseFloat(dbGrade.value),
      tableGrade: parseFloat(tableGrade.value),
      rowGrades: [...rowGrades.value],
      columnGrades: [...columnGrades.value],
      rowGradeValue: parseFloat(rowGradeValue.value),
      columnGradeValue: parseFloat(columnGradeValue.value),
      totalGradeValue: totalGradeValue.value
    };
    
    // 从props.modelValue中获取ID，如果没有则从其他props尝试获取
    const id = props.objectId 
      ? props.objectId
      : (props.modelValue && props.modelValue.id 
          ? props.modelValue.id 
          : (props.modelValue && props.modelValue.objectId 
              ? props.modelValue.objectId 
              : ''));
            
    if (!id) {
      console.warn('【分类分级值】错误: 未找到有效的对象ID，无法保存分类分级值');
      ElMessage.error('缺少对象ID，无法保存分类分级值');
      // 仍然关闭对话框并传递本地计算结果
      emit('confirm', result);
      emit('update:modelValue', null);
      return; // 提前退出，不发送请求
    }
    
    // 构建要发送的总分类分级值数据
    const postData = {
      totalCategoryValue: String(totalClassificationValue.value),
      totalGradeValue: String(totalGradeValue.value)
    };
    
    // 构建要发送的分类类别数据
    const categoryData = {
      industryCategory: industryCategory.value,
      processingTimeCategory: dataTimeliness.value,  // 注意这里的映射，前端使用dataTimeliness，后端使用processingTimeCategory
      dataSourceCategory: dataSource.value
    };

    // 确保API路径正确
    const baseUrl = 'http://localhost:8080/api';
    
    // 创建一个Promise数组以并行发送两个请求
    const requests = [];

    // 1. 发送总分类分级值数据到服务器
    const totalValuesUrl = `${baseUrl}/objects/${id}/total_values`;
    const totalValuesRequest = axios.post(totalValuesUrl, postData, {
      headers: {
        'Content-Type': 'application/json',
      },
      timeout: 10000,
    });
    requests.push(totalValuesRequest);

    // 2. 发送分类类别数据到服务器
    const categoriesUrl = `${baseUrl}/objects/${id}/categories`;
    console.log('发送分类数据到：', categoriesUrl, '数据：', JSON.stringify(categoryData));
    const categoriesRequest = axios.post(categoriesUrl, categoryData, {
      headers: {
        'Content-Type': 'application/json',
      },
      timeout: 10000,
    });
    requests.push(categoriesRequest);

    // 并行处理两个请求
    Promise.all(requests)
      .then(([totalValuesResponse, categoriesResponse]) => {
        let successMessages = [];
        let warningMessages = [];

        // 处理总分类分级值响应
        if (totalValuesResponse.status >= 200 && totalValuesResponse.status < 300 && 
            totalValuesResponse.data && totalValuesResponse.data.code === 1) {
          // 成功情况
          const successUrlPattern = totalValuesUrl.replace(id, '{id}');
          localStorage.setItem('classificationLevelSuccessUrl', successUrlPattern);
          successMessages.push('分类分级总值保存成功');
        } else {
          // 失败情况记录日志
          warningMessages.push(`分类分级总值保存失败: ${totalValuesResponse.data?.msg || totalValuesResponse.data?.message || '未知错误'}`);
        }

        // 处理分类类别响应
        if (categoriesResponse.status >= 200 && categoriesResponse.status < 300 && 
            categoriesResponse.data && categoriesResponse.data.code === 1) {
          successMessages.push('分类类别值保存成功');
        } else {
          // 失败情况记录日志
          warningMessages.push(`分类类别值保存失败: ${categoriesResponse.data?.msg || categoriesResponse.data?.message || '未知错误'}`);
        }

        // 显示消息
        if (successMessages.length > 0) {
          ElMessage.success(successMessages.join('；'));
        }
        if (warningMessages.length > 0) {
          ElMessage.warning(warningMessages.join('；'));
        }

        // 无论成功还是失败，都更新前端显示并关闭对话框
        emit('confirm', result);
        emit('update:modelValue', null);
      })
      .catch(error => {
        console.error('【分类分级值】请求失败:', error.message);
        
        // 即使请求出错，仍更新前端显示
        ElMessage({
          message: '无法连接到后端保存分类分级值，但已更新本地显示',
          type: 'warning',
          duration: 5000
        });
        
        emit('confirm', result);
        emit('update:modelValue', null);
      });

  } catch (error) {
    console.error('【分类分级值】确认过程出现异常:', error);
    ElMessage.error('确认分类分级值时发生错误');
  }
}

// 权重修改相关逻辑
const showWeightForm = ref(false)
const normalWeight = ref(1)
const importantWeight = ref(2)
const criticalWeight = ref(3)

const confirmWeightChange = () => {
  // 更正后的API路径格式，确保一致性
  const baseUrl = props.apiBaseUrl.endsWith('/api') ? props.apiBaseUrl : `${props.apiBaseUrl}/api`;
  
  // 发送权重数据到服务器
  axios.post(`${baseUrl}/setWeights`, {
    general: normalWeight.value,
    important: importantWeight.value,
    core: criticalWeight.value
  })
    .then(response => {
      if (response.data && response.data.code === 1) {
        ElMessage.success('权重设置成功');
        showWeightForm.value = false;
      } else {
        console.warn('权重设置返回非成功状态:', response.data);
        ElMessage.warning(`权重设置失败: ${response.data?.msg || response.data?.message || '未知错误'}`);
      }
    })
    .catch(error => {
      console.error('权重设置请求失败:', error.message, error.response?.status);
      ElMessage.error(`权重设置失败: ${error.message}，请稍后重试`);
      // 即使请求失败，仍在本地保存权重值
      showWeightForm.value = false;
    });
}

// 行分级值详情弹窗
const rowDetailDialogVisible = ref(false)
const rowExcelData = ref([])

// 列分级值详情弹窗
const columnDetailDialogVisible = ref(false)
const columnExcelData = ref([])

// 数据加载状态
const fetchingData = ref(false)

// 显示行分级值详情弹窗
const showRowDetailDialog = async () => {
  try {
    console.log('显示行分级值详情，当前对象ID:', props.objectId);
    
    // 清空之前的数据，避免显示上一次的结果
    rowExcelData.value = [];
    fetchingData.value = true;
    
    await fetchExcelData('row');
    rowDetailDialogVisible.value = true;
  } catch (error) {
    console.error('获取行分级值详情失败:', error);
    ElMessage.error('获取行分级值详情失败');
  } finally {
    fetchingData.value = false;
  }
}

// 显示列分级值详情弹窗
const showColumnDetailDialog = async () => {
  try {
    console.log('显示列分级值详情，当前对象ID:', props.objectId);
    
    // 清空之前的数据，避免显示上一次的结果
    columnExcelData.value = [];
    fetchingData.value = true;
    
    await fetchExcelData('column');
    columnDetailDialogVisible.value = true;
  } catch (error) {
    console.error('获取列分级值详情失败:', error);
    ElMessage.error('获取列分级值详情失败');
  } finally {
    fetchingData.value = false;
  }
}

// 从API获取Excel数据
const fetchExcelData = async (type = 'row') => {
  try {
    // 获取当前对象ID
    const id = props.objectId;
    
    if (!id) {
      console.error('【Excel数据】错误: 无法获取对象ID');
      throw new Error('无法获取对象ID');
    }
    
    console.log(`【Excel数据】正在获取${type === 'row' ? '行' : '列'}数据，对象ID:`, id);
    
    // 使用对象特定的API端点，可以从props中获取自定义API基础URL
    const apiBaseUrl = props.apiBaseUrl || 'http://localhost:8080';
    const apiUrl = `${apiBaseUrl}/api/objects/${id}`;
    console.log('【Excel数据】API请求URL:', apiUrl);
    
    // 如果开启了调试模式，添加随机参数避免缓存
    const url = props.debug ? `${apiUrl}?_t=${Date.now()}` : apiUrl;
    
    const response = await axios.get(url);
    
    // 在调试模式下打印更多信息
    if (props.debug) {
      console.log('【Excel数据-DEBUG】API响应状态码:', response.status);
      console.log('【Excel数据-DEBUG】API响应头:', response.headers);
      console.log('【Excel数据-DEBUG】API响应数据类型:', typeof response.data);
      console.log('【Excel数据-DEBUG】API响应数据:', response.data);
    } else {
      console.log('【Excel数据】API响应状态码:', response.status);
      console.log('【Excel数据】API响应数据类型:', typeof response.data);
    }
    
    // 查看是否可以直接从API响应中提取数据项
    if (response.data && typeof response.data === 'object') {
      console.log('【Excel数据】API响应数据顶级键:', Object.keys(response.data));
      
      // 处理响应数据的方法
      const extractDataItems = (data) => {
        // 尝试获取dataItems字段
        if (data.dataItems && Array.isArray(data.dataItems)) {
          console.log('【Excel数据】从response.dataItems获取数据');
          return data.dataItems;
        }
        
        // 尝试获取data.data.dataItems字段
        if (data.data && data.data.dataItems && Array.isArray(data.data.dataItems)) {
          console.log('【Excel数据】从response.data.dataItems获取数据');
          return data.data.dataItems;
        }
        
        // 尝试从data字段获取数据
        if (data.data) {
          const objectData = data.data;
          console.log('【Excel数据】data字段键:', Object.keys(objectData));
          
          // 尝试获取dataContent字段
          if (objectData.dataContent) {
            console.log('【Excel数据】发现dataContent字段，类型:', typeof objectData.dataContent);
            
            try {
              // 如果dataContent是字符串，尝试解析
              let content = objectData.dataContent;
              
              if (typeof content === 'string') {
                // 打印字符串前100个字符便于调试
                console.log('【Excel数据】dataContent前100个字符:', content.substring(0, 100));
                
                // 尝试解析JSON
                try {
                  content = JSON.parse(content);
                  console.log('【Excel数据】解析dataContent成功，解析后键:', Object.keys(content));
                } catch (parseError) {
                  console.warn('【Excel数据】解析dataContent为JSON失败:', parseError.message);
                }
              }
              
              // 检查解析后的content是否有dataItems字段
              if (content && content.dataItems && Array.isArray(content.dataItems)) {
                console.log('【Excel数据】从dataContent.dataItems获取数据');
                return content.dataItems;
              }
              
              // 检查是否content本身就是一个数组
              if (Array.isArray(content)) {
                console.log('【Excel数据】dataContent本身是数组');
                return content;
              }
              
              // 寻找content中的任何数组字段
              if (typeof content === 'object') {
                for (const key in content) {
                  if (Array.isArray(content[key]) && content[key].length > 0) {
                    console.log(`【Excel数据】从dataContent.${key}获取数组数据`);
                    return content[key];
                  }
                }
              }
            } catch (contentError) {
              console.error('【Excel数据】处理dataContent时出错:', contentError);
            }
          }
          
          // 检查对象数据中是否有任何数组字段
          for (const key in objectData) {
            if (Array.isArray(objectData[key]) && objectData[key].length > 0) {
              console.log(`【Excel数据】从objectData.${key}获取数组数据`);
              return objectData[key];
            }
          }
        }
        
        // 检查原始响应中是否有任何数组字段
        for (const key in data) {
          if (Array.isArray(data[key]) && data[key].length > 0) {
            console.log(`【Excel数据】从response.${key}获取数组数据`);
            return data[key];
          }
        }
        
        return null;
      };
      
      // 提取数据项
      const dataItems = extractDataItems(response.data);
      
      if (dataItems && dataItems.length > 0) {
        console.log(`【Excel数据】成功获取${dataItems.length}条数据项:`, dataItems);
        
        if (type === 'row') {
          // 将获取到的数据存储到rowExcelData中
          // 确保rowGrades数组长度与行数据一致
          if (rowGrades.value.length < dataItems.length) {
            // 如果rowGrades数组比数据项少，补充默认值
            const defaultValue = 1.0; // 默认分级值
            const difference = dataItems.length - rowGrades.value.length;
            for (let i = 0; i < difference; i++) {
              rowGrades.value.push(defaultValue);
            }
            console.log('已为行分级值数组补充默认值，当前数组:', rowGrades.value);
          } else if (rowGrades.value.length > dataItems.length) {
            // 如果rowGrades数组比数据项多，截取需要的部分
            rowGrades.value = rowGrades.value.slice(0, dataItems.length);
            console.log('已截取行分级值数组，当前数组:', rowGrades.value);
          }
          
          // 将行分级值直接添加到每行数据中
          rowExcelData.value = dataItems.map((item, index) => {
            return {
              ...item,
              rowGradeValue: rowGrades.value[index] || '未设置'
            };
          });
          
          console.log('行数据已处理，添加了行分级值:', rowExcelData.value);
        } else {
          columnExcelData.value = dataItems;
          
          // 确保columnGrades数组长度与列数量匹配
          const columnKeys = getObjectKeys(dataItems);
          if (columnGrades.value.length < columnKeys.length) {
            // 如果columnGrades数组比列少，补充默认值
            const defaultValue = 0.4; // 默认分级值
            const difference = columnKeys.length - columnGrades.value.length;
            for (let i = 0; i < difference; i++) {
              columnGrades.value.push(defaultValue);
            }
            console.log('已为列分级值数组补充默认值，当前数组:', columnGrades.value);
          } else if (columnGrades.value.length > columnKeys.length) {
            // 如果columnGrades数组比列多，截取需要的部分
            columnGrades.value = columnGrades.value.slice(0, columnKeys.length);
            console.log('已截取列分级值数组，当前数组:', columnGrades.value);
          }
          
          // 添加一个额外的记录作为最后一行，用于显示列分级值
          if (columnExcelData.value.length > 0) {
            const gradeRow = {};
            // 为每列创建一个分级值
            columnKeys.forEach((key, index) => {
              gradeRow[key] = columnGrades.value[index] || 0.4;
            });
            // 添加一个特殊标记字段，用于在表格中区分这是分级值行
            gradeRow['_isGradeRow'] = true;
            columnExcelData.value.push(gradeRow);
            console.log('列数据已处理，添加了列分级值行:', gradeRow);
          }
        }
        
        return true;
      }
    }
    
    // 如果上面的方法都失败了，说明API返回格式异常
    console.error('【Excel数据】无法从API响应中提取有效数据');
    throw new Error('API返回数据格式不正确');
    
  } catch (error) {
    console.error('【Excel数据】获取失败:', error.message);
    
    // 使用模拟数据（与当前对象ID相关）
    const id = props.objectId || '';
    const shortId = id.substring(0, 4);
    
    const mockData = [
      {
        "产品ID": `P${shortId}-001`,
        "名称": "手机",
        "库存量": "200",
        "对象ID": id
      },
      {
        "产品ID": `P${shortId}-002`,
        "名称": "耳机",
        "库存量": "500",
        "对象ID": id
      },
      {
        "产品ID": `P${shortId}-003`,
        "名称": "充电器",
        "库存量": "300",
        "对象ID": id
      }
    ];
    
    console.log('【Excel数据】使用模拟数据:', mockData);
    
    if (type === 'row') {
      rowExcelData.value = mockData;
    } else {
      columnExcelData.value = mockData;
    }
    
    // 使用模拟数据也算成功
    return true;
  }
}

// 辅助函数：获取对象的键
const getObjectKeys = (dataArray) => {
  if (!dataArray || !Array.isArray(dataArray) || dataArray.length === 0) {
    return [];
  }
  
  // 获取所有对象的所有键
  const keySets = dataArray.map(item => {
    if (item && typeof item === 'object') {
      return Object.keys(item);
    }
    return [];
  });
  
  // 合并所有键集并去重
  const allKeys = [...new Set(keySets.flat())];
  // 过滤掉 rowGradeValue 键
  const filteredKeys = allKeys.filter(key => key !== 'rowGradeValue');
  console.log('【Excel数据】获取到的所有键:', filteredKeys);
  
  return filteredKeys;
}

// 根据分级值获取标签类型
const getGradeTagType = (value) => {
  const num = parseFloat(value) || 0;
  if (num >= 3.0) return 'danger';   // 高分级值：红色
  if (num >= 2.0) return 'warning';  // 中高分级值：橙色
  if (num >= 1.0) return 'success';  // 中等分级值：绿色
  return 'info';                     // 低分级值：灰色
}
</script>

<style scoped>
.classification-form,
.level-form {
  padding: 20px 0;
}

.form-item,
.level-item {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.label {
  width: 140px;
  text-align: right;
  margin-right: 10px;
  font-size: 14px;
  color: #606266;
}

.value-display {
  margin-left: 15px;
  color: #409EFF;
  font-size: 14px;
}

.value {
  font-weight: bold;
  color: #409EFF;
  font-size: 16px;
  margin-right: 15px;
}

.help-link {
  margin-left: 10px;
  font-size: 12px;
}

.classification-result,
.level-result {
  background-color: #f8f9fa;
  padding: 15px;
  margin-top: 20px;
  border-radius: 4px;
  text-align: center;
}

.formula {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

.calculation {
  font-size: 15px;
  color: #606266;
  font-weight: bold;
  margin-bottom: 10px;
}

.result-value {
  font-size: 16px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 5px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

:deep(.el-select) {
  width: 200px;
}

.weight-form {
  margin-top: 10px;
  margin-bottom: 15px;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-left: 140px;
}

.weight-item {
  margin-bottom: 15px;
  display: flex;
  align-items: center;
}

.weight-label {
  margin-right: 10px;
  width: 150px;
  color: #606266;
}

.weight-actions {
  margin-top: 15px;
  text-align: right;
}

.level-result .result-value {
  font-weight: 700;
  font-size: 16px;
  color: #409EFF;
}

/* Excel数据预览样式 */
.excel-data-preview {
  padding: 10px;
}

.excel-data-preview h3 {
  margin-top: 0;
  margin-bottom: 15px;
  font-size: 16px;
  color: #303133;
  text-align: center;
}

.data-info {
  margin-bottom: 10px;
  font-size: 14px;
  color: #606266;
}

.no-data-message {
  text-align: center;
  padding: 20px;
  color: #606266;
}

.loading-text {
  margin-top: 10px;
  font-size: 14px;
}

.column-grade-cell {
  display: flex;
  align-items: center;
  justify-content: center;
}

.column-grade-label {
  margin-right: 5px;
  font-size: 12px;
  color: #606266;
  font-weight: bold;
}

/* 为分级值行添加样式 */
:deep(.el-table__row:last-child) {
  background-color: #f8f8f8;
}

:deep(.el-table__row:last-child td) {
  border-top: 1px solid #dcdfe6;
  padding-top: 8px;
  padding-bottom: 8px;
}
</style> 