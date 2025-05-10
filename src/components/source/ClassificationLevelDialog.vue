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
                <p>数据表中往往含有若干行，根据每行记录的权重值与对所含字段分级值的平均值累加，
                  得到行分级值默认一般记录的权重为:1(默认值);
                  重要记录的权重为:2;核心记录的权重为:3;</p>
              </div>
            </el-popover>
            
            <el-link type="primary" class="help-link">查看详情</el-link>
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
            <el-link type="primary" class="help-link">查看详情</el-link>
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

// 计算行分级值（取平均值）
const rowGradeValue = computed(() => {
  if (!rowGrades.value || rowGrades.value.length === 0) return 0;
  try {
    // 确保所有值都是数值类型
    const numericValues = rowGrades.value.map(val => {
      const num = parseFloat(val);
      return isNaN(num) ? 0 : num;
    });
    const sum = numericValues.reduce((acc, val) => acc + val, 0);
    const avg = sum / numericValues.length;
    const result = parseFloat(avg.toFixed(1));
    return result;
  } catch (error) {
    return 0;
  }
})

// 计算列分级值（取平均值）
const columnGradeValue = computed(() => {
  if (!columnGrades.value || columnGrades.value.length === 0) return 0;
  try {
    // 确保所有值都是数值类型
    const numericValues = columnGrades.value.map(val => {
      const num = parseFloat(val);
      return isNaN(num) ? 0 : num;
    });
    const sum = numericValues.reduce((acc, val) => acc + val, 0);
    const avg = sum / numericValues.length;
    const result = parseFloat(avg.toFixed(1));
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
    
    // 发送确认事件
    emit('confirm', result);
    // 关闭对话框
    emit('update:modelValue', null);
  } catch (error) {
    ElMessage.error('确认分类分级值时发生错误');
  }
}

// 权重修改相关逻辑
const showWeightForm = ref(false)
const normalWeight = ref(1)
const importantWeight = ref(2)
const criticalWeight = ref(3)

const confirmWeightChange = () => {
  // 发送权重数据到服务器
  axios.post(`${API_URL}/setWeights`, {
    general: normalWeight.value,
    important: importantWeight.value,
    core: criticalWeight.value
  })
    .then(response => {
      if (response.data && response.data.code === 1) {
        ElMessage.success('权重设置成功');
        showWeightForm.value = false;
      } else {
        ElMessage.warning('权重设置失败');
      }
    })
    .catch(error => {
      ElMessage.error('权重设置失败，请稍后重试');
      // 即使请求失败，仍在本地保存权重值
      showWeightForm.value = false;
    });
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
</style> 