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
          <!-- <div class="level-item">
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
          </div> -->
          
          <div class="level-item">
            <span class="label">列分级值：</span>
            <span class="value">{{ columnGradeValue }}</span> --
            <!-- <el-popover
              placement="right"
              :width="300"
              trigger="click"
            >
              <template #reference>
                <el-link type="primary" class="help-link">说明</el-link>
              </template>
              <div class="help-content">
                <h4>列分级值说明</h4>
                <p>根据字段的敏感程度来确定数据的级别;</p>
              </div>
            </el-popover> -->
            <el-link type="primary" class="help-link" @click="showColumnDetailDialog">查看详情</el-link>
          </div>
          
          <div class="grading-rule-card">① 根据字段的敏感程度来确定数据的级别;</div>


          <div class="level-item">
            <span class="label">行分级值：</span>
            <span class="value">{{ rowGradeValue }}</span>
            <!-- <el-popover
              placement="right"
              :width="300"
              trigger="click"
            >
              <template #reference>
                <el-link type="primary" class="help-link">说明</el-link>
              </template>
              <div class="help-content">
                <h4>行分级值说明</h4>
                <p>数据表中往往含有若干行，根据每行记录的权重值与对所含字段分级值的平均值累加，得到行分级值;</p>
              </div>
            </el-popover> -->
            
            <el-link type="primary" class="help-link" @click="showRowDetailDialog">查看详情</el-link>
            <el-link type="primary" class="help-link" @click="showWeightForm = !showWeightForm">修改权重</el-link>
          </div>
          
          <div class="grading-rule-card">② 数据表中往往含有若干行，根据每行记录的权重值与对所含字段分级值的平均值累加，得到行分级值;</div>
          
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
            <span class="value">{{ tableGrade }}</span>
            <!-- <el-popover
              placement="right"
              :width="300"
              trigger="click"
            >
              <template #reference>
                <el-link type="primary" class="help-link">说明</el-link>
              </template>
              <div class="help-content">
                <h4>表分级值说明</h4>
                <p>由表内总的记录数对应的分级值与对所有行的行分级值的最大值累加求得出表分级值;</p>
              </div>
            </el-popover> -->
          </div>
          
          <div class="grading-rule-card">③ 由表内总的记录数对应的分级值与对所有行的行分级值的最大值累加求得出表分级值;</div>
          
        </div>

        <div class="level-result">
          <div class="result-value">最终该<b>表分级值</b>计算得：{{ tableGrade }}</div>
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
      
      <div v-if="rowExcelData.length > 0">
        <div class="data-info">找到 {{ rowExcelData.length }} 条记录</div>
        <el-table :data="rowExcelData" border style="width: 100%" max-height="600px">
          <el-table-column 
            v-for="(key, index) in getObjectKeys(rowExcelData).filter(k => k !== 'rowGradeValue')" 
            :key="index"
            :prop="key"
            :label="key"
            :min-width="100"
          />
          <!-- 行分级值列 -->
          <el-table-column
            label="行权重"
            align="center"
            min-width="100"
          >
            <template #default="scope">
              <el-tag :type="getRowWeightTagType(scope.row.rowGradeValue || scope.row.重要程度)">
                {{ scope.row.rowGradeValue || 
                   (scope.row.重要程度 === '核心' ? 3 :
                    scope.row.重要程度 === '重要' ? 2 : 1) }}
              </el-tag>
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
    width="1600px"
    append-to-body
    destroy-on-close
  >
    <div class="excel-data-preview">
      <h3>数据预览</h3>
      
      <div v-if="columnExcelData.length > 0">
        <div class="data-info">找到 {{ columnExcelData.length - 1 }} 条记录</div>
        <el-table :data="columnExcelData" border style="width: 100%" max-height="600px">
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


const rowGradeValue = computed(() => {
  if (!rowGrades.value || rowGrades.value.length === 0) return 0;
  try {

    const numericValues = rowGrades.value.map(val => {
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


const totalGradeValue = computed(() => {
  try {

    const dbValue = parseFloat(dbGrade.value) || 0;
    const tableValue = parseFloat(tableGrade.value) || 0;
    const rowValue = parseFloat(rowGradeValue.value) || 0;
    const colValue = parseFloat(columnGradeValue.value) || 0;
    
    const sum =tableValue;
    const result = parseFloat(sum.toFixed(1));
    
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
      tableGrade: parseFloat(tableGrade.value),
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
      // 仍然关闭对话框并传递本地计算结果
      emit('confirm', result);
      emit('update:modelValue', null);
      window.location.reload(); 
      return; 
    }
    


    const categoryData = {
      id: id,  
      objectId: id, 
      industryCategory: industryCategory.value || "",
      processingTimeCategory: dataTimeliness.value || "", 
      dataSourceCategory: dataSource.value || ""
    };

    const baseUrl = 'http://localhost:8081/api';
    
    try {
      const checkResp = await axios.get(`${baseUrl}/objects/${id}`);
      
   
      if (checkResp.data) {
        const objectData = checkResp.data && checkResp.data.data ? checkResp.data.data : checkResp.data;



        const updateData = { ...objectData };
        if (updateData.dataEntity) {
          updateData.dataEntity = { ...updateData.dataEntity };
        } else {
          updateData.dataEntity = {};
        }
        
        // 更新分类和分级值
        updateData.industryCategory = industryCategory.value || "";
        updateData.processingTimeCategory = dataTimeliness.value || "";
        updateData.dataSourceCategory = dataSource.value || "";
        updateData.totalCategoryValue = totalClassificationValue.value || "0";
        updateData.totalGradeValue = totalGradeValue.value || "0";
        updateData.rowGrades = [...rowGrades.value]; // 确保包含行分级值数组
        updateData.status = '待校验';
        
        if (updateData.dataEntity) {
          updateData.dataEntity.status = '待校验';
        }
        
        // 确保所有关键字段都存在
        if (!updateData.id && id) {
          updateData.id = id;
        }
        
        // 移除可能导致问题的undefined值
        Object.keys(updateData).forEach(key => {
          if (updateData[key] === undefined) {
            delete updateData[key];
          } else if (typeof updateData[key] === 'object' && updateData[key] !== null) {
            Object.keys(updateData[key]).forEach(subKey => {
              if (updateData[key][subKey] === undefined) {
                delete updateData[key][subKey];
              }
            });
          }
        });
        
        
        try {
          const updateResp = await axios.put(`${baseUrl}/objects/${id}`, updateData, {
            headers: { 'Content-Type': 'application/json' }
          });
          
          if (updateResp.status === 200 || 
              updateResp.status === 204 || 
              (updateResp.data && (updateResp.data.code === 1 || updateResp.data.code === 200))) {
            ElMessage.success('分类分级值更新成功');
            emit('confirm', result);
            emit('update:modelValue', null);
            window.location.reload();
            return;
          }
        } catch (updateError) {
        }
      }
    } catch(e) {
    }
    
    // 如果直接更新失败，继续使用原有的方式尝试
    const requests = [];

    // 尝试使用PUT方法更新总值
    const totalValuesUrl = `${baseUrl}/objects/${id}/total_values`;
    const totalValuesData = {
      id: id,
      objectId: id,
      totalCategoryValue: totalClassificationValue.value || "0",
      totalGradeValue: totalGradeValue.value || "0"
    };
    
    let useFallbackMethod = false;  // 标记是否需要使用备用方法
    
    // 尝试PUT方法
    try {
      const totalValuesPutResponse = await axios.put(totalValuesUrl, totalValuesData, {
        headers: { 'Content-Type': 'application/json' },
        timeout: 10000
      });

      
      if (totalValuesPutResponse.status >= 200 && totalValuesPutResponse.status < 300) {

      } else {
        // 如果PUT失败，标记使用备用方法
        useFallbackMethod = true;
      }
    } catch (putError) {
      console.error('[分类分级] 总值PUT失败:', putError);
      useFallbackMethod = true;
    }
    
    // 如果需要使用备用方法
    if (useFallbackMethod) {
      
      // 只更新必要的字段
      try {
        // 第三种方式：直接通过PATCH更新特定字段
        const patchData = {
          id: id,
          totalCategoryValue: totalClassificationValue.value || "0",
          totalGradeValue: totalGradeValue.value || "0",
          industryCategory: industryCategory.value || "",
          processingTimeCategory: dataTimeliness.value || "",
          dataSourceCategory: dataSource.value || "",
          rowGrades: [...rowGrades.value] // 确保包含行分级值数组
        };
        

        
        const patchResponse = await axios.patch(`${baseUrl}/objects/${id}`, patchData, {
          headers: { 'Content-Type': 'application/json' }
        });
        
        
        if (patchResponse.status >= 200 && patchResponse.status < 300) {
          ElMessage.success('分类分级值更新成功');
          emit('confirm', result);
          emit('update:modelValue', null);
          window.location.reload();
          return;
        }
      } catch (patchError) {
        console.error('[分类分级] PATCH更新失败:', patchError);
      }
      
      // 如果所有尝试都失败了，使用原始的POST方法
      const totalValuesRequest = axios.post(totalValuesUrl, totalValuesData, {
        headers: { 'Content-Type': 'application/json' },
        timeout: 10000
      });
      requests.push(totalValuesRequest);
      
      const categoriesUrl = `${baseUrl}/objects/${id}/categories`;
      const categoriesRequest = axios.post(categoriesUrl, categoryData, {
        headers: { 'Content-Type': 'application/json' },
        timeout: 10000
      });
      requests.push(categoriesRequest);
    }
    
    // 移除旧的分类PUT请求尝试，因为已经在上面处理过了

    // 如果有POST请求需要执行
    if (requests.length > 0) {
      Promise.all(requests)
        .then(async ([totalValuesResponse, categoriesResponse]) => {
          let successMessages = [];
          let warningMessages = [];

          if (totalValuesResponse && totalValuesResponse.status >= 200 && totalValuesResponse.status < 300 && 
              totalValuesResponse.data && totalValuesResponse.data.code === 1) {
            const successUrlPattern = totalValuesUrl.replace(id, '{id}');
            localStorage.setItem('classificationLevelSuccessUrl', successUrlPattern);
            successMessages.push('分类分级总值保存成功');
          } else if (totalValuesResponse) {
            warningMessages.push(`分类分级总值保存失败: ${totalValuesResponse.data?.msg || totalValuesResponse.data?.message || '未知错误'}`);
          }

          if (categoriesResponse && categoriesResponse.status >= 200 && categoriesResponse.status < 300 && 
              categoriesResponse.data && categoriesResponse.data.code === 1) {
            successMessages.push('分类类别值保存成功');
          } else if (categoriesResponse) {
            warningMessages.push(`分类类别值保存失败: ${categoriesResponse.data?.msg || categoriesResponse.data?.message || '未知错误'}`);
          }

          // 其余的处理逻辑不变
          if (successMessages.length > 0) {
            ElMessage.success(successMessages.join('；'));
          }
          if (warningMessages.length > 0) {
            ElMessage.warning(warningMessages.join('；'));
          }

          emit('confirm', result);
          emit('update:modelValue', null);
          window.location.reload(); 
        })
        .catch(error => {
          console.error('[分类分级] 保存请求错误:', error);
          ElMessage({
            message: '无法连接到后端保存分类分级值，但已更新本地显示',
            type: 'warning',
            duration: 5000
          });
          
          emit('confirm', result);
          emit('update:modelValue', null);
        });
    } else {
      // 如果没有请求需要执行(PUT方法都成功了)，直接返回成功
      ElMessage.success('分类分级值保存成功');
      emit('confirm', result);
      emit('update:modelValue', null);
      window.location.reload();
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


const columnDetailDialogVisible = ref(false)
const columnExcelData = ref([])


const fetchingData = ref(false)


const showRowDetailDialog = async () => {
  try {
    rowExcelData.value = [];
    fetchingData.value = true;
    
    // 直接使用props.modelValue中的dataItems，如果存在
    if (props.modelValue && props.modelValue.dataItems && props.modelValue.dataItems.length > 0) {
      console.log('[行分级值] 使用现有数据, rowGrades:', rowGrades.value);
      
      // 确保rowGrades长度与dataItems匹配
      if (rowGrades.value.length < props.modelValue.dataItems.length) {
        const defaultValue = 1.0;
        const difference = props.modelValue.dataItems.length - rowGrades.value.length;
        for (let i = 0; i < difference; i++) {
          rowGrades.value.push(defaultValue);
        }
      }
      
      rowExcelData.value = props.modelValue.dataItems.map((item, index) => {
        // 使用rowGrades中对应位置的值作为rowGradeValue
        const gradeValue = index < rowGrades.value.length ? rowGrades.value[index] : 1;
        return {
          ...item,
          rowGradeValue: gradeValue
        };
      });
      
      console.log('[行分级值] 处理后的行数据:', rowExcelData.value);
      rowDetailDialogVisible.value = true;
      fetchingData.value = false;
      return;
    }
    
    await fetchExcelData('row');
    rowDetailDialogVisible.value = true;
  } catch (error) {
    console.error('[行分级值] 获取详情失败:', error);
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
    
    // 直接使用props.modelValue中的dataItems，如果存在
    if (props.modelValue && props.modelValue.dataItems && props.modelValue.dataItems.length > 0) {
      columnExcelData.value = [...props.modelValue.dataItems];
      
      // 添加分级值行
      const columnKeys = getObjectKeys(columnExcelData.value);
      if (columnExcelData.value.length > 0) {
        const gradeRow = {};
        columnKeys.forEach((key, index) => {
          gradeRow[key] = columnGrades.value[index] || 0.4;
        });
        gradeRow['_isGradeRow'] = true;
        columnExcelData.value.push(gradeRow);
      }
      
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

          rowExcelData.value = dataItems.map((item, index) => {
            return {
              ...item,
              rowGradeValue: rowGrades.value[index]
            };
          });
          
          console.log(`[行分级值] 数据处理完成，行数:${rowExcelData.value.length}，权重:`, rowGrades.value);
        } else {
          // 处理列分级值数据
          columnExcelData.value = dataItems;
          
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
          
          if (columnExcelData.value.length > 0) {
            const gradeRow = {};
            columnKeys.forEach((key, index) => {
              gradeRow[key] = columnGrades.value[index] || 0.4;
            });
            gradeRow['_isGradeRow'] = true;
            columnExcelData.value.push(gradeRow);
          }
          
          console.log(`[分类分级详情] 列分级值数据处理完成，数量: ${columnExcelData.value.length}`);
        }
        
        return true;
      } else {
        console.warn('[分类分级详情] 未找到有效的数据项');
      }
    }
    
    // 如果上面的方法都失败了，说明API返回格式异常
    console.error('[分类分级详情] 无法从API响应中提取有效数据');
    throw new Error('API返回数据格式不正确');
    
  } catch (error) {
    console.error('[分类分级详情] 获取数据失败:', error.message);
    
    // 生成模拟数据
    const id = props.objectId || '';
    const shortId = id.substring(0, 4);
    
    const mockData = [
      {
        "产品ID": `P${shortId}-001`,
        "名称": "手机",
        "库存量": "200",
        "重要性": "重要",
        "对象ID": id
      },
      {
        "产品ID": `P${shortId}-002`,
        "名称": "耳机",
        "库存量": "500",
        "重要性": "重要",
        "对象ID": id
      },
      {
        "产品ID": `P${shortId}-003`,
        "名称": "充电器",
        "库存量": "300",
        "重要性": "核心",
        "对象ID": id
      }
    ];

    
    if (type === 'row') {
      rowExcelData.value = mockData;
    } else {
      columnExcelData.value = mockData;
    }

    return true;
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

const getGradeTagType = (value) => {
  const num = parseFloat(value) || 0;
  if (num >= 3.0) return 'danger';   
  if (num >= 2.0) return 'warning';  
  if (num >= 1.0) return 'success';  
  return 'info';                     
}

const getRowWeightTagType = (value) => {
  // 如果值是字符串形式的数字，转换为数字
  const num = parseFloat(value);
  if (!isNaN(num)) {
    if (num >= 3.0) return 'danger';   // 高权重（对应"核心"）
    if (num >= 2.0) return 'warning';  // 中等权重（对应"重要"）
    if (num >= 1.0) return 'success';  // 低权重（对应"一般"）
  } else {
    // 如果值不是数字，按字符串处理
    if (value === '核心') return 'danger';
    if (value === '重要') return 'warning';
  }
  return 'success'; // 默认低权重
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
</style> 