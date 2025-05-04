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
            <el-link type="primary" class="help-link">说明</el-link>
          </div>

          <div class="level-item">
            <span class="label">表分级值：</span>
            <span class="value">{{ tableGrade }}</span>
            <el-link type="primary" class="help-link">说明</el-link>
          </div>
          
          <div class="level-item">
            <span class="label">行分级值：</span>
            <span class="value">{{ rowGradeValue }}</span>
            <el-link type="primary" class="help-link">说明</el-link>
            <el-link type="primary" class="help-link">查看详情</el-link>
          </div>
          
          <div class="level-item">
            <span class="label">列分级值：</span>
            <span class="value">{{ columnGradeValue }}</span>
            <el-link type="primary" class="help-link">说明</el-link>
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
const dbGrade = ref(props.modelValue.dbGrade || 100)
const tableGrade = ref(props.modelValue.tableGrade || 10)
const rowGrades = ref(props.modelValue.rowGrades || [0.3])
const columnGrades = ref(props.modelValue.columnGrades || [0.3])

// 计算行分级值（取平均值）
const rowGradeValue = computed(() => {
  if (!rowGrades.value || rowGrades.value.length === 0) return 0.3;
  try {
    // 确保所有值都是数值类型
    const numericValues = rowGrades.value.map(val => {
      const num = parseFloat(val);
      return isNaN(num) ? 0.3 : num;
    });
    const sum = numericValues.reduce((acc, val) => acc + val, 0);
    const avg = sum / numericValues.length;
    return parseFloat(avg.toFixed(1));
  } catch (error) {
    console.error('计算行分级值平均值出错:', error);
    return 0.3;
  }
})

// 计算列分级值（取平均值）
const columnGradeValue = computed(() => {
  if (!columnGrades.value || columnGrades.value.length === 0) return 0.3;
  try {
    // 确保所有值都是数值类型
    const numericValues = columnGrades.value.map(val => {
      const num = parseFloat(val);
      return isNaN(num) ? 0.3 : num;
    });
    const sum = numericValues.reduce((acc, val) => acc + val, 0);
    const avg = sum / numericValues.length;
    return parseFloat(avg.toFixed(1));
  } catch (error) {
    console.error('计算列分级值平均值出错:', error);
    return 0.3;
  }
})

// 计算总分级值（库+表+行+列的总和）
const totalGradeValue = computed(() => {
  try {
    // 确保使用数值类型进行计算
    const dbValue = parseFloat(dbGrade.value) || 100;
    const tableValue = parseFloat(tableGrade.value) || 10;
    const rowValue = parseFloat(rowGradeValue.value) || 0.3;
    const colValue = parseFloat(columnGradeValue.value) || 0.3;
    
    // 计算总和并保留一位小数
    const sum = dbValue + tableValue + rowValue + colValue;
    const result = parseFloat(sum.toFixed(1));
    
    console.log(`计算总分级值: ${dbValue} + ${tableValue} + ${rowValue} + ${colValue} = ${result}`);
    
    return result;
  } catch (error) {
    console.error('计算总分级值出错:', error);
    return 110.6; // 出错时使用默认值
  }
})

// 监听modelValue变化，初始化表单数据
watch(() => props.modelValue, (newVal) => {
  if (newVal) {
    console.log('ClassificationLevelDialog接收到的数据：', JSON.stringify(newVal, null, 2));
    // 初始化分类值
    industryCategory.value = newVal.industryCategory !== undefined ? newVal.industryCategory : '';
    dataTimeliness.value = newVal.dataTimeliness !== undefined ? newVal.dataTimeliness : '';
    dataSource.value = newVal.dataSource !== undefined ? newVal.dataSource : '';
    
    // 初始化分级值 - 确保值正确解析为数字
    dbGrade.value = newVal.dbGrade !== undefined ? parseFloat(newVal.dbGrade) : 100;
    tableGrade.value = newVal.tableGrade !== undefined ? parseFloat(newVal.tableGrade) : 10;
    
    // 处理行分级值
    if (newVal.rowGrades && Array.isArray(newVal.rowGrades)) {
      rowGrades.value = newVal.rowGrades.map(val => parseFloat(val) || 0.3);
    } else {
      rowGrades.value = [0.3];
    }
    
    // 处理列分级值
    if (newVal.columnGrades && Array.isArray(newVal.columnGrades)) {
      columnGrades.value = newVal.columnGrades.map(val => parseFloat(val) || 0.3);
    } else {
      columnGrades.value = [0.3];
    }
    
    console.log('初始化后的分级值：', {
      dbGrade: dbGrade.value,
      tableGrade: tableGrade.value,
      rowGrades: rowGrades.value,
      columnGrades: columnGrades.value
    });
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
  
  console.log(`计算分类值: ${industryValue} + ${timeValue} + ${sourceValue} = ${totalClassificationValue.value}`)
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
    
    console.log('对话框确认返回数据:', JSON.stringify(result, null, 2));
    
    // 发送确认事件
    emit('confirm', result);
    // 关闭对话框
    emit('update:modelValue', null);
  } catch (error) {
    console.error('确认时发生错误:', error);
    ElMessage.error('确认分类分级值时发生错误');
  }
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
</style> 