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
          <div class="result-value">该数据对象分类值计算得：{{ totalClassificationValue }}</div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="分级" name="level">
        <!-- 分级选项卡内容 -->
        <div class="level-form">

          
          <div class="level-item">
            <span class="label">列分级值：</span>
            <span class="value">{{ columnGradeValue*0.001.toFixed(4) }}</span>

            <el-link type="primary" class="help-link" @click="showColumnDetailDialog">查看详情</el-link>
          </div>
          
          <div class="grading-rule-card">①根据字段的敏感程度来确定数据的级别;</div>


          <div class="level-item">
            <span class="label">行分级值：</span>
            <span class="value">{{ (rowGradeValue*0.001).toFixed(4) }}</span>
            <el-link type="primary" class="help-link" @click="showRowDetailDialog">查看详情</el-link>
            <el-link type="primary" class="help-link" @click="showWeightForm = !showWeightForm">修改权重</el-link>
          </div>
          
          <div class="grading-rule-card">②数据表中往往含有若干行，根据每行记录的权重值与对所含字段分级值的平均值累加，得到行分级值;</div>
          
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
            <span class="label">表分级值：</span>
            <span class="value">{{ (tableGradeValue*0.001).toFixed(4) }}</span>
          </div>
          
          <div class="grading-rule-card">③由表内总的记录数对应的分级值与对所有行的行分级值的最大值累加求得出表分级值;</div>
          

          <div class="level-item">
              <span class="label">库分级值：</span>
              <span class="value">{{ ((dbGrade+tableGradeValue)*0.001).toFixed(4) }}</span>
          </div>

          <div class="grading-rule-card">④根据数据库的数据量、表分级值确定;</div>
        </div>


        <div class="level-result">
          <div class="result-value">该数据对象分级值计算得：{{ (totalGradeValue).toFixed(4) }}</div>
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
    width="1600px"
    append-to-body
    destroy-on-close
  >
    <div class="excel-data-preview">
      <h3>数据预览</h3>
      
      <div v-if="rowTotalCount > 0">
        <div class="data-info">找到 {{ rowTotalCount }} 条记录，当前显示第 {{ (rowCurrentPage - 1) * rowPageSize + 1 }} - {{ Math.min(rowCurrentPage * rowPageSize, rowTotalCount) }} 条</div>
        <el-table :data="rowExcelData" border style="width: 100%" max-height="600px">
          <!-- 序号列 -->
          <el-table-column 
            label="序号"
            type="index"
            width="70"
            align="center"
            :index="index => (rowCurrentPage - 1) * rowPageSize + index + 1"
          />
          <!-- 其余字段列，排除"rowNumber" -->
          <el-table-column 
            v-for="(key, index) in getObjectKeys(rowExcelData).filter(k => k !== 'rowNumber')" 
            :key="index"
            :prop="key"
            :label="key"
            :min-width="100"
          />
          <!-- 行分级值列 -->
          <el-table-column
            label="行分级值"
            min-width="100"
            align="center"
          >
            <template #default="scope">
              <el-tag type="success">{{ calcRowGradeValue((rowCurrentPage - 1) * rowPageSize + scope.$index) }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
        
        <!-- 行分级值分页组件 -->
        <div class="pagination-container">
          <CommonPagination
            v-model:current-page="rowCurrentPage"
            v-model:page-size="rowPageSize"
            :total-count="rowTotalCount"
            :page-sizes="[10, 15, 20, 50]"
            background
            @size-change="handleRowSizeChange"
            @current-change="handleRowCurrentChange"
          />
        </div>
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
    width="1600px"
    append-to-body
    destroy-on-close
  >
    <div class="excel-data-preview">
      <h3>数据预览</h3>
      
      <div v-if="columnTotalCount > 0">
        <div class="data-info">找到 {{ columnTotalCount }} 条记录，当前显示第 {{ (columnCurrentPage - 1) * columnPageSize + 1 }} - {{ Math.min(columnCurrentPage * columnPageSize, columnTotalCount) }} 条</div>
        <el-table :data="getColumnTableDataWithoutGradeRow()" border style="width: 100%" max-height="600px">
          <!-- 序号列 -->
          <el-table-column 
            label="序号"
            type="index"
            width="70"
            align="center"
            :index="index => (columnCurrentPage - 1) * columnPageSize + index + 1"
          />
          <!-- 其余字段列，排除"重要性"和"rowNumber"，表头右侧显示分级值 -->
          <el-table-column 
            v-for="(key, index) in getObjectKeys(columnOriginalData).filter(k => k !== '_isGradeRow' && k !== '重要性' && k !== 'rowNumber')" 
            :key="index"
            :prop="key"
            :min-width="100"
          >
            <template #header>
              <span>{{ key }}</span>
              <el-tag
                v-if="getGradeValue(key) !== null"
                :type="getGradeTagType(getGradeValue(key))"
                size="small"
                style="margin-left:4px;vertical-align:middle;"
              >
                {{ (parseFloat(getGradeValue(key)) * 0.001).toFixed(4) }}
              </el-tag>
              </template>
            <template #default="scope">
                {{ scope.row[key] }}
            </template>
          </el-table-column>
        </el-table>
        
        <!-- 列分级值分页组件 -->
        <div class="pagination-container">
          <CommonPagination
            v-model:current-page="columnCurrentPage"
            v-model:page-size="columnPageSize"
            :total-count="columnTotalCount"
            :page-sizes="[10, 15, 20, 50]"
            background
            @size-change="handleColumnSizeChange"
            @current-change="handleColumnCurrentChange"
          />
        </div>
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
import CommonPagination from '../CommonPagination.vue'

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
    default: 'http://localhost:8081'
  }
})

const emit = defineEmits(['update:visible', 'update:modelValue', 'confirm'])

const dialogVisible = ref(props.visible)

watch(() => props.visible, (newVal) => {
  dialogVisible.value = newVal
})

watch(dialogVisible, (newVal) => {
  emit('update:visible', newVal)
})

const activeTab = ref('classification')

const industryCategory = ref('')
const dataTimeliness = ref('')
const dataSource = ref('')


const dbGrade = ref(0) 
const tableGrade = ref(0)
const rowGrades = ref([0, 0]) 
const columnGrades = ref([0, 0]) 


// 计算列权重的平均值
const columnAverageValue = computed(() => {
  if (!columnGrades.value || columnGrades.value.length === 0) return 0;
  try {
    const numericValues = columnGrades.value.map(val => {
      const num = parseFloat(val);
      return isNaN(num) ? 0 : num;
    });

    const sum = numericValues.reduce((acc, val) => acc + val, 0);
    const average = sum / numericValues.length;
    const result = parseFloat(average.toFixed(1));
    return result;
  } catch (error) {
    return 0;
  }
})

const rowGradeValue = computed(() => {
  if (!rowGrades.value || rowGrades.value.length === 0) return 0;
  try {
    // 获取所有行权重的最大值
    const numericValues = rowGrades.value.map(val => {
      const num = parseFloat(val);
      return isNaN(num) ? 0 : num;
    });

    const maxValue = Math.max(...numericValues);
    
    // 行分级值 = 行权重最大值 + 列权重平均值
    // const result = maxValue + columnAverageValue.value;
    const result = maxValue;

    return parseFloat(result.toFixed(3));
  } catch (error) {
    return 0;
  }
})


const columnGradeValue = computed(() => {
  if (!columnGrades.value || columnGrades.value.length === 0) return 0;
  try {
    const numericValues = columnGrades.value.map(val => {
      const num = parseFloat(val);
      return isNaN(num) ? 0 : num;
    });

    const maxValue = Math.max(...numericValues);
    const result = parseFloat(maxValue.toFixed(1));
    return result;
  } catch (error) {
    return 0;
  }
})


const tableGradeValue = computed(() => {
  try {
    const tableValue = parseFloat(tableGrade.value) || 0;
    return tableValue;
  } catch (error) {
    return 0;
  }
})

const totalGradeValue = computed(() => {
  try {
    const dbValue = parseFloat(dbGrade.value) || 0;
    const tableValue = parseFloat(tableGrade.value) || 0;
    const rowValue = parseFloat(rowGradeValue.value) || 0;
    const colValue = parseFloat(columnGradeValue.value) || 0;
    
    // 表分级值 = 原始表分级值 + 列权重平均值
    // const adjustedTableValue = tableValue + columnAverageValue.value;
    const adjustedTableValue = (tableValue+dbValue)*0.001 ;
    const result = parseFloat(adjustedTableValue.toFixed(4));
    
    return result;
  } catch (error) {
    return 0; 
  }
})


watch(() => props.modelValue, (newVal) => {
  if (newVal) {


    industryCategory.value = newVal.industryCategory !== undefined ? newVal.industryCategory : '';
    dataTimeliness.value = newVal.dataTimeliness !== undefined ? newVal.dataTimeliness : '';
    dataSource.value = newVal.dataSource !== undefined ? newVal.dataSource : '';
 
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

    if (newVal.rowGrades) {
      if (Array.isArray(newVal.rowGrades)) {
        rowGrades.value = newVal.rowGrades.map(val => {
          const parsedVal = parseFloat(val) || 0;
          return parsedVal;
        });
      } else {

        const parsedVal = parseFloat(newVal.rowGrades) || 0;
        rowGrades.value = [parsedVal, parsedVal];
      }
    } else {
      rowGrades.value = [0, 0];
    }

    if (newVal.columnGrades) {
      if (Array.isArray(newVal.columnGrades)) {
        columnGrades.value = newVal.columnGrades.map(val => {
          const parsedVal = parseFloat(val) || 0;
          return parsedVal;
        });
      } else {

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
  '交通运输': 900,
  '金融': 900,
  '卫生社会工作': 900,
  '教育': 600,
  '制造业': 600,
  '建筑业': 600,
  '餐饮': 300,
  '居民服务': 300,
  '个人组织': 300
})

const timelinessMap = reactive({
  '实时': 90,
  '近实时': 60,
  '历史': 30
})

const sourceMap = reactive({
  '政府': 9,
  '企业': 6,
  '个人': 3
})


const getIndustryCategoryValue = () => {
  return industryCategory.value ? industryCategoryMap[industryCategory.value] : 0
}


const getTimelinessValue = () => {
  return dataTimeliness.value ? timelinessMap[dataTimeliness.value] : 0
}


const getSourceValue = () => {
  return dataSource.value ? sourceMap[dataSource.value] : 0
}


const totalClassificationValue = ref(0)

const calculateClassificationValue = () => {
  const industryValue = getIndustryCategoryValue()
  const timeValue = getTimelinessValue()
  const sourceValue = getSourceValue()
  
  const result = industryValue + timeValue + sourceValue
  totalClassificationValue.value = parseFloat(result.toFixed(1)).toString()
}


onMounted(() => {

  totalClassificationValue.value = "0";

  if (props.objectId) {
    fetchCategoryData(props.objectId);
  }
})


const fetchCategoryData = async (objectId) => {
  try {
    if (!objectId) {
      return;
    }
  
    const baseUrl = 'http://localhost:8081/api';
    const url = `${baseUrl}/objects/${objectId}`;

    const response = await axios.get(url);
    
    if (response.data) {
      let categoryData = null;

      if (response.data.industryCategory !== undefined || 
          response.data.processingTimeCategory !== undefined || 
          response.data.dataSourceCategory !== undefined) {
        categoryData = response.data;
      } 

      else if (response.data.data) {
        if (response.data.data.industryCategory !== undefined || 
            response.data.data.processingTimeCategory !== undefined || 
            response.data.data.dataSourceCategory !== undefined) {
          categoryData = response.data.data;
        }
      }
      
      if (categoryData) {

        if (categoryData.industryCategory) {
          industryCategory.value = categoryData.industryCategory;
        }
        
        if (categoryData.processingTimeCategory) {

          dataTimeliness.value = categoryData.processingTimeCategory;
        }
        
        if (categoryData.dataSourceCategory) {
          dataSource.value = categoryData.dataSourceCategory;
        }

        calculateClassificationValue();
      } else {
        console.warn('在对象数据中未找到分类数据');
      }
    } 
  } catch (error) {

    industryCategory.value = '';
    dataTimeliness.value = '';
    dataSource.value = '';
  }
};


watch(dialogVisible, (newVal) => {
  emit('update:visible', newVal);
  

  if (newVal && props.objectId) {
    fetchCategoryData(props.objectId);
  }
});

const handleConfirm = async () => {
  try {
    const result = {
      classificationValue: totalClassificationValue.value, 
      industryCategory: industryCategory.value,
      dataTimeliness: dataTimeliness.value,
      dataSource: dataSource.value,
      
              levelValue: totalGradeValue.value.toString(), 
        dbGrade: parseFloat(dbGrade.value),
        tableGrade: totalGradeValue.value, 
        rowGrades: [...rowGrades.value],
        columnGrades: [...columnGrades.value],
        rowGradeValue: parseFloat(rowGradeValue.value),
        columnGradeValue: parseFloat(columnGradeValue.value),
        totalGradeValue: totalGradeValue.value
    };
    
    const id = props.objectId 
      ? props.objectId
      : (props.modelValue && props.modelValue.id 
          ? props.modelValue.id 
          : (props.modelValue && props.modelValue.objectId 
              ? props.modelValue.objectId 
              : ''));
            
    if (!id) {
      ElMessage.error('缺少对象ID，无法保存分类分级值');
      emit('confirm', result);
      emit('update:modelValue', null);
      window.location.reload(); 
      return; 
    }
    
    const baseUrl = 'http://localhost:8081/api';
    
    try {
      const checkResp = await axios.get(`${baseUrl}/objects/${id}`);
      
      if (checkResp.data) {
        const objectData = checkResp.data && checkResp.data.data ? checkResp.data.data : checkResp.data;
        
        const updateData = {
          id: id,
          entity: objectData.entity || '',
          industryCategory: industryCategory.value || "",
          processingTimeCategory: dataTimeliness.value || "",
          dataSourceCategory: dataSource.value || "",
          totalCategoryValue: totalClassificationValue.value || "0",
          totalGradeValue: totalGradeValue.value || "0",
          tableGrade: totalGradeValue.value || "0", 
          status: '待校验'
        };
        if (Array.isArray(rowGrades.value)) {
          updateData.rowGrades = rowGrades.value.map(val => {
            const num = parseFloat(val);
            return isNaN(num) ? 1 : num;
          });
        } else {
          updateData.rowGrades = [];
        }
        
        if (Array.isArray(columnGrades.value)) {
          updateData.columnGrades = columnGrades.value.map(val => {
            const num = parseFloat(val);
            return isNaN(num) ? 0.4 : num;
          });
        } else {
          updateData.columnGrades = [];
        }
        
        if (objectData.locationInfo) updateData.locationInfo = objectData.locationInfo;
        if (objectData.metadata) updateData.metadata = objectData.metadata;
        if (objectData.constraint) updateData.constraint = objectData.constraint;
        if (objectData.transferControl) updateData.transferControl = objectData.transferControl;
        if (objectData.propagationControl) updateData.propagationControl = objectData.propagationControl;
        if (objectData.dataItems) updateData.dataItems = objectData.dataItems;
        if (objectData.excelData) updateData.excelData = objectData.excelData;
        
        updateData.dataEntity = {
          ...(objectData.dataEntity || {}),
          status: '待校验'
        };
    
        const validStates = ['待生成分类分级值', '不合格', '待校验', '已合格'];
        if (updateData.status && !validStates.includes(updateData.status)) {
          console.warn(`[分类分级] 状态值 "${updateData.status}" 不在有效范围内: ${validStates.join(', ')}`);
          updateData.status = '待校验';
        }
        if (updateData.dataEntity && updateData.dataEntity.status && !validStates.includes(updateData.dataEntity.status)) {
          console.warn(`[分类分级] dataEntity.status "${updateData.dataEntity.status}" 不在有效范围内: ${validStates.join(', ')}`);
          updateData.dataEntity.status = '待校验';
        }
        
        const updateUrl = `${baseUrl}/objects/${id}`;
        console.log('[分类分级] 使用URL:', updateUrl);
        
        try {
            const updateResp = await axios.put(updateUrl, updateData, {
              headers: { 'Content-Type': 'application/json' }
            });
          
          if (updateResp.status >= 200 && updateResp.status < 300) {
            ElMessage.success('分类分级值更新成功');
            emit('confirm', result);
            emit('update:modelValue', null);
            window.location.reload();
            return;
          } else {
            console.warn('[分类分级] 更新响应非成功:', updateResp);
            throw new Error('更新请求返回非成功状态');
          }
        } catch (updateError) {
          console.error('[分类分级] 更新请求错误:', updateError);
          
          // 详细记录错误信息
          if (updateError.response) {
            console.error('错误状态码:', updateError.response.status);
            console.error('错误响应数据:', updateError.response.data);
            
            if (updateError.response.status === 400) {
              try {
                const simpleData = {
                  id: id,
                  industryCategory: industryCategory.value || "",
                  processingTimeCategory: dataTimeliness.value || "",
                  dataSourceCategory: dataSource.value || "",
                  totalCategoryValue: totalClassificationValue.value || "0",
                  totalGradeValue: totalGradeValue.value || "0",
                  tableGrade: totalGradeValue.value || "0", 
                  status: '待校验'
                };
                
                console.log('[分类分级] 尝试简化请求:', JSON.stringify(simpleData));
                
                const simpleResp = await axios.patch(`${baseUrl}/objects/${id}`, simpleData);
                if (simpleResp.status >= 200 && simpleResp.status < 300) {
                  ElMessage.success('分类分级基本值更新成功');
                  emit('confirm', result);
                  emit('update:modelValue', null);
                  window.location.reload();
                  return;
                }
              } catch (simpleError) {
                console.error('[分类分级] 简化请求失败:', simpleError);
              }
            }
          }
          
          // 尝试使用独立的API端点
          try {
            const totalValuesResp = await axios.post(`${baseUrl}/objects/${id}/total_values`, {
              totalCategoryValue: totalClassificationValue.value || "0",
              totalGradeValue: totalGradeValue.value || "0"
            });
            
            const categoriesResp = await axios.post(`${baseUrl}/objects/${id}/categories`, {
              industryCategory: industryCategory.value || "",
              processingTimeCategory: dataTimeliness.value || "",
              dataSourceCategory: dataSource.value || ""
            });
            
            if (totalValuesResp.status >= 200 && totalValuesResp.status < 300 && 
                categoriesResp.status >= 200 && categoriesResp.status < 300) {
              ElMessage.success('分类分级值通过独立API保存成功');
              emit('confirm', result);
              emit('update:modelValue', null);
              window.location.reload();
              return;
            }
          } catch (apiError) {
            console.error('[分类分级] 独立API请求失败:', apiError);
            ElMessage.error('保存分类分级值失败，请检查控制台日志');
          }
        }
      } else {
        throw new Error('获取对象详情失败');
      }
    } catch (fetchError) {
      console.error('[分类分级] 获取或处理对象失败:', fetchError);
      ElMessage.error('获取对象信息失败，无法更新分类分级值');
    }
    
    // 最后尝试单独发送分类分级总值
    try {
      const basicResp = await axios.post(`${baseUrl}/objects/${id}/total_values`, {
        totalCategoryValue: totalClassificationValue.value || "0",
        totalGradeValue: totalGradeValue.value || "0"
      });
      
      if (basicResp.status >= 200 && basicResp.status < 300) {
        ElMessage.success('保存基本分类分级值成功');
        emit('confirm', result);
        emit('update:modelValue', null);
        return;
      }
    } catch (basicError) {
      console.error('[分类分级] 基本值保存失败:', basicError);
      ElMessage.error('保存失败，请稍后重试');
    }

  } catch (error) {
    console.error('[分类分级] 确认操作错误:', error);
    ElMessage.error('确认分类分级值时发生错误');
  }
}

// 权重修改相关逻辑
const showWeightForm = ref(false)
const normalWeight = ref(1)
const importantWeight = ref(2)
const criticalWeight = ref(3)

const confirmWeightChange = () => {

  const baseUrl = props.apiBaseUrl.endsWith('/api') ? props.apiBaseUrl : `${props.apiBaseUrl}/api`;
  

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

      showWeightForm.value = false;
    });
}


const rowDetailDialogVisible = ref(false)
const rowExcelData = ref([])

// 行分级值详情分页相关
const rowCurrentPage = ref(1)
const rowPageSize = ref(15)
const rowTotalCount = ref(0)
const rowOriginalData = ref([])
const rowLoadedPages = ref(new Map())

const columnDetailDialogVisible = ref(false)
const columnExcelData = ref([])

// 列分级值详情分页相关
const columnCurrentPage = ref(1)
const columnPageSize = ref(15)
const columnTotalCount = ref(0)
const columnOriginalData = ref([])
const columnLoadedPages = ref(new Map())

const fetchingData = ref(false)


const showRowDetailDialog = async () => {
  try {
    rowExcelData.value = [];
    fetchingData.value = true;
    rowCurrentPage.value = 1;
    rowLoadedPages.value.clear();
    
    if (props.modelValue && props.modelValue.dataItems && props.modelValue.dataItems.length > 0) {
      console.log('[行分级值] 使用现有数据, rowGrades:', rowGrades.value);
      
      if (rowGrades.value.length < props.modelValue.dataItems.length) {
        const defaultValue = 1.0;
        const difference = props.modelValue.dataItems.length - rowGrades.value.length;
        for (let i = 0; i < difference; i++) {
          rowGrades.value.push(defaultValue);
        }
      }
      
      const processedData = props.modelValue.dataItems.map((item, index) => {
        const gradeValue = index < rowGrades.value.length ? rowGrades.value[index] : 1;
        return {
          ...item,
          rowGradeValue: gradeValue
        };
      });
      
      // 存储原始数据并设置分页
      rowOriginalData.value = processedData;
      rowTotalCount.value = processedData.length;
      rowCurrentPage.value = 1;
      rowLoadedPages.value.clear();
      loadRowPageData(1);
      
      rowDetailDialogVisible.value = true;
      fetchingData.value = false;
      return;
    }
    
    await fetchExcelData('row');
    rowDetailDialogVisible.value = true;
  } catch (error) {
    ElMessage.error('获取行分级值详情失败');
  } finally {
    fetchingData.value = false;
  }
}

// 显示列分级值详情弹窗
const showColumnDetailDialog = async () => {
  try {
    columnExcelData.value = [];
    fetchingData.value = true;
    columnCurrentPage.value = 1;
    columnLoadedPages.value.clear();
    
    if (props.modelValue && props.modelValue.dataItems && props.modelValue.dataItems.length > 0) {
      let processedData = [...props.modelValue.dataItems];
      
      const columnKeys = getObjectKeys(processedData);
      if (processedData.length > 0) {
        const gradeRow = {};
        columnKeys.forEach((key, index) => {
          gradeRow[key] = columnGrades.value[index] || 0.4;
        });
        gradeRow['_isGradeRow'] = true;
        processedData.push(gradeRow);
      }
      
      // 存储原始数据并设置分页
      columnOriginalData.value = processedData;
      columnTotalCount.value = processedData.length - 1; // 减去分级行
      columnCurrentPage.value = 1;
      columnLoadedPages.value.clear();
      loadColumnPageData(1);
      
      columnDetailDialogVisible.value = true;
      fetchingData.value = false;
      return;
    }
    
    await fetchExcelData('column');
    columnDetailDialogVisible.value = true;
  } catch (error) {
    ElMessage.error('获取列分级值详情失败');
  } finally {
    fetchingData.value = false;
  }
}

// 从API获取Excel数据
const fetchExcelData = async (type = 'row') => {
  try {
    const id = props.objectId;
    
    if (!id) {
      throw new Error('无法获取对象ID');
    }

    const apiBaseUrl = props.apiBaseUrl || 'http://localhost:8081';
    const apiUrl = `${apiBaseUrl}/api/objects/${id}`;

    const url = props.debug ? `${apiUrl}?_t=${Date.now()}` : apiUrl;
    
    console.log(`[${type}分级值] 请求数据: ${url}`);
    const response = await axios.get(url);
    console.log(`[${type}分级值] 响应数据:`, response.data);

    if (response.data && typeof response.data === 'object') {
      // 提取数据项
      const extractDataItems = (data) => {
        
        // 首先检查dataEntity.dataItems
        if (data.dataEntity && data.dataEntity.dataItems && Array.isArray(data.dataEntity.dataItems)) {
          return data.dataEntity.dataItems;
        }
        
        // 然后检查顶层dataItems
        if (data.dataItems && Array.isArray(data.dataItems)) {
          return data.dataItems;
        }

        // 检查data.dataEntity.dataItems
        if (data.data && data.data.dataEntity && Array.isArray(data.data.dataEntity.dataItems)) {
          return data.data.dataEntity.dataItems;
        }
        
        // 检查data.dataItems
        if (data.data && data.data.dataItems && Array.isArray(data.data.dataItems)) {
          return data.data.dataItems;
        }
        
        // 尝试从data字段获取数据
        if (data.data) {
          const objectData = data.data;

          if (objectData.dataContent) {
            try {
              let content = objectData.dataContent;
              
              if (typeof content === 'string') {
                try {
                  content = JSON.parse(content);
                  console.log(`[${type}分级值] 解析dataContent成功`);
                } catch (parseError) {
                  console.warn(`[${type}分级值] 解析dataContent为JSON失败:`, parseError.message);
                }
              }

              if (content && content.dataItems && Array.isArray(content.dataItems)) {
                return content.dataItems;
              }
              
              if (content && content.dataEntity && content.dataEntity.dataItems && Array.isArray(content.dataEntity.dataItems)) {
                return content.dataEntity.dataItems;
              }
      
              if (Array.isArray(content)) {
                return content;
              }
              
              if (typeof content === 'object') {
                for (const key in content) {
                  if (Array.isArray(content[key]) && content[key].length > 0) {
                    return content[key];
                  }
                }
              }
            } catch (contentError) {
              console.error(`[${type}分级值] 处理dataContent时出错:`, contentError);
            }
          }
          
          for (const key in objectData) {
            if (Array.isArray(objectData[key]) && objectData[key].length > 0) {
              return objectData[key];
            }
          }
        }
        
        for (const key in data) {
          if (Array.isArray(data[key]) && data[key].length > 0) {
            return data[key];
          }
        }

        return null;
      };
      
      const dataItems = extractDataItems(response.data);
      
      if (dataItems && dataItems.length > 0) {
        
        if (type === 'row') {
          // 处理行分级值数据
          // 尝试从响应中提取rowGrades数组
          let extractedRowGrades = null;
          
          if (response.data.rowGrades && Array.isArray(response.data.rowGrades)) {
            extractedRowGrades = response.data.rowGrades;
          } else if (response.data.data && response.data.data.rowGrades && Array.isArray(response.data.data.rowGrades)) {
            extractedRowGrades = response.data.data.rowGrades;
          }
          
          console.log(`[行分级值] 提取到的rowGrades:`, extractedRowGrades);
          
          // 如果成功提取到rowGrades，则使用它
          if (extractedRowGrades && extractedRowGrades.length > 0) {
            rowGrades.value = extractedRowGrades.map(val => {
              // 确保每个值都是数字
              const numVal = parseFloat(val);
              return isNaN(numVal) ? 1 : numVal;
            });
          }
          
          if (rowGrades.value.length < dataItems.length) {
            const defaultValue = 1.0; 
            const difference = dataItems.length - rowGrades.value.length;
            for (let i = 0; i < difference; i++) {
              rowGrades.value.push(defaultValue);
            }
          } else if (rowGrades.value.length > dataItems.length) {
            rowGrades.value = rowGrades.value.slice(0, dataItems.length);
          }

          const processedData = dataItems.map((item, index) => {
            return {
              ...item,
              rowGradeValue: rowGrades.value[index]
            };
          });
          
          // 存储原始数据并设置分页
          rowOriginalData.value = processedData;
          rowTotalCount.value = processedData.length;
          rowCurrentPage.value = 1;
          rowLoadedPages.value.clear();
          loadRowPageData(1);
          
          console.log(`[行分级值] 数据处理完成，行数:${processedData.length}，权重:`, rowGrades.value);
        } else {
          // 处理列分级值数据
          const columnKeys = getObjectKeys(dataItems);
          console.log(`[分类分级详情] 提取到列名: ${columnKeys.join(', ')}`);
          
          if (columnGrades.value.length < columnKeys.length) {
            const defaultValue = 0.4;
            const difference = columnKeys.length - columnGrades.value.length;
            for (let i = 0; i < difference; i++) {
              columnGrades.value.push(defaultValue);
            }
          } else if (columnGrades.value.length > columnKeys.length) {
            columnGrades.value = columnGrades.value.slice(0, columnKeys.length);
          }
          
          let processedData = [...dataItems];
          if (processedData.length > 0) {
            const gradeRow = {};
            columnKeys.forEach((key, index) => {
              gradeRow[key] = columnGrades.value[index] || 0.4;
            });
            gradeRow['_isGradeRow'] = true;
            processedData.push(gradeRow);
          }
          
          // 存储原始数据并设置分页
          columnOriginalData.value = processedData;
          columnTotalCount.value = processedData.length - 1; // 减去分级行
          columnCurrentPage.value = 1;
          columnLoadedPages.value.clear();
          loadColumnPageData(1);
        
        }
        
        return true;
      } else {
        console.warn('[分类分级详情] 未找到有效的数据项');
      }
    }
    
    throw new Error('API返回数据格式不正确');
    
  } catch (error) {
    
    // 不使用模拟数据，直接设置为空
    if (type === 'row') {
      rowExcelData.value = [];
    } else {
      columnExcelData.value = [];
    }

    return false;
  }
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

  const filteredKeys = allKeys.filter(key => key !== 'rowGradeValue');
  
  return filteredKeys;
}



// 获取行权重最大值
const getMaxRowGrade = () => {
  if (!rowGrades.value || rowGrades.value.length === 0) return 0;
  try {
    const numericValues = rowGrades.value.map(val => {
      const num = parseFloat(val);
      return isNaN(num) ? 0 : num;
    });
    return Math.max(...numericValues);
  } catch (error) {
    return 0;
  }
}

const getRowWeightTagType = (value) => {

  const num = parseFloat(value);
  if (!isNaN(num)) {
    if (num >= 3.0) return 'danger';   
    if (num >= 2.0) return 'warning';  
    if (num >= 1.0) return 'success'; 
  } else {

    if (value === '核心') return 'danger';
    if (value === '重要') return 'warning';
  }
  return 'success'; 
}

// 获取分级值（不再依赖_isGradeRow，直接从columnGrades或props/modelValue获取）
function getGradeValue(key) {
  // 优先从columnGrades
  if (Array.isArray(columnGrades.value)) {
    const keys = getObjectKeys(columnOriginalData.value).filter(k => k !== '_isGradeRow' && k !== '重要性' && k !== 'rowNumber');
    const idx = keys.indexOf(key);
    if (idx > -1 && columnGrades.value[idx] !== undefined) {
      return columnGrades.value[idx];
    }
  }
  return null;
}
// 根据分级值返回el-tag类型
function getGradeTagType(val) {
  const num = parseFloat(val);
  if (num >= 0.8) return 'danger'; // 红
  if (num >= 0.6) return 'warning'; // 橙
  if (num >= 0.4) return 'info'; // 蓝
  return 'default'; // 灰
}
function getColumnTableDataWithoutGradeRow() {
  if (!columnExcelData.value) return [];
  // 过滤掉所有字段均为数字且数量与字段数一致的分级值行
  const keys = getObjectKeys(columnExcelData.value).filter(k => k !== '_isGradeRow' && k !== '重要性' && k !== 'rowNumber');
  return columnExcelData.value.filter(row => {
    // 只要有一个字段不是数字或为空，就不是分级值行
    let isGradeRow = true;
    for (const key of keys) {
      if (typeof row[key] === 'undefined' || row[key] === null || row[key] === '') {
        isGradeRow = false;
        break;
      }
      if (typeof row[key] === 'string' && isNaN(Number(row[key]))) {
        isGradeRow = false;
        break;
      }
      if (typeof row[key] === 'number' && (row[key] < 0 || row[key] > 1)) {
        isGradeRow = false;
        break;
      }
    }
    // 只要不是所有字段都是0~1的数字，就保留
    return !isGradeRow;
  });
}

// 行分级值=行权重+列分级值平均值
function calcRowGradeValue(idx) {
  const rowWeight = Array.isArray(rowGrades.value) && rowGrades.value[idx] !== undefined ? parseFloat(rowGrades.value[idx]) : 0;
  const colAvg = columnAverageValue.value || 0;
  return (rowWeight * 0.001).toFixed(4);
}

// 懒加载行分级值页面数据
const loadRowPageData = (page) => {
  if (!rowOriginalData.value || rowOriginalData.value.length === 0) {
    rowExcelData.value = [];
    return;
  }
  
  // 检查是否已缓存该页数据
  if (rowLoadedPages.value.has(page)) {
    rowExcelData.value = rowLoadedPages.value.get(page);
    return;
  }
  
  // 计算页面数据范围
  const startIndex = (page - 1) * rowPageSize.value;
  const endIndex = startIndex + rowPageSize.value;
  const pageData = rowOriginalData.value.slice(startIndex, endIndex);
  
  // 缓存页面数据
  rowLoadedPages.value.set(page, pageData);
  rowExcelData.value = pageData;
};

// 懒加载列分级值页面数据
const loadColumnPageData = (page) => {
  if (!columnOriginalData.value || columnOriginalData.value.length === 0) {
    columnExcelData.value = [];
    return;
  }
  
  // 检查是否已缓存该页数据
  if (columnLoadedPages.value.has(page)) {
    columnExcelData.value = columnLoadedPages.value.get(page);
    return;
  }
  
  // 获取不包含分级行的数据
  const dataWithoutGradeRow = columnOriginalData.value.filter(row => !row._isGradeRow);
  
  // 计算页面数据范围
  const startIndex = (page - 1) * columnPageSize.value;
  const endIndex = startIndex + columnPageSize.value;
  const pageData = dataWithoutGradeRow.slice(startIndex, endIndex);
  
  // 缓存页面数据
  columnLoadedPages.value.set(page, pageData);
  columnExcelData.value = pageData;
};

// 行分级值分页处理函数
const handleRowCurrentChange = (page) => {
  rowCurrentPage.value = page;
  loadRowPageData(page);
};

const handleRowSizeChange = (size) => {
  rowPageSize.value = size;
  rowCurrentPage.value = 1;
  rowLoadedPages.value.clear();
  loadRowPageData(1);
};

// 列分级值分页处理函数
const handleColumnCurrentChange = (page) => {
  columnCurrentPage.value = page;
  loadColumnPageData(page);
};

const handleColumnSizeChange = (size) => {
  columnPageSize.value = size;
  columnCurrentPage.value = 1;
  columnLoadedPages.value.clear();
  loadColumnPageData(1);
};


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

.grading-rule-card {
  background: #f8fafd;
  border-radius: 8px;
  padding: 10px 18px;
  margin-bottom: 12px;
  margin-left: 140px;
  margin-right: 20px;
  font-size: 15px;
  color: #333;
  display: flex;
  align-items: center;
  box-shadow: 0 1px 4px rgba(64,158,255,0.06);
}

.pagination-container {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  margin-top: 15px;
  padding: 10px 0;
}
</style>