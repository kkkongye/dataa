import { reactive, ref } from 'vue'
import axios from 'axios'
import { API_URL, axiosInstance } from './apiConfig'
import Cookies from 'js-cookie'

// 存储CSRF令牌的变量
let csrfToken = null;

// 共享的数字对象数据
const sharedTableData = reactive([
  { id: 1, entity: '表01', locationInfo: '(表01, -, -)', constraint: ['格式约束:xlsx', '访问权限:全部允许', '传输路径约束:点对点', '地域性约束:内网', '共享约束:允许共享'], formatConstraint: 'xlsx', accessConstraint: '全部允许', pathConstraint: '点对点', regionConstraint: '内网', shareConstraint: '允许共享', transferControl: ['可修改'], propagationControl: { canRead: false, canModify: true, canDestroy: false, canShare: false, canDelegate: false }, auditInfo: '查看日志', status: '已合格', feedback: '', excelData: null },
  { id: 2, entity: '表02', locationInfo: '(表02, 1-2, 3-6)', constraint: ['格式约束:jpg', '访问权限:只允许管理方获取', '传输路径约束:面对面', '地域性约束:外网', '共享约束:不允许共享'], formatConstraint: 'jpg', accessConstraint: '只允许管理方获取', pathConstraint: '面对面', regionConstraint: '外网', shareConstraint: '不允许共享', transferControl: ['可修改'], propagationControl: { canRead: false, canModify: true, canDestroy: false, canShare: false, canDelegate: false }, auditInfo: '查看日志', status: '不合格', feedback: '缺少约束条件', excelData: null },
  { id: 3, entity: '表03', locationInfo: '(表03, 1-6, 12-50)', constraint: ['格式约束:xlsx', '访问权限:全部允许', '传输路径约束:点对点', '地域性约束:内网', '共享约束:允许共享'], formatConstraint: 'xlsx', accessConstraint: '全部允许', pathConstraint: '点对点', regionConstraint: '内网', shareConstraint: '允许共享', transferControl: ['可读'], propagationControl: { canRead: true, canModify: false, canDestroy: false, canShare: false, canDelegate: false }, auditInfo: '查看日志', status: '已合格', feedback: '', excelData: null },
  { id: 4, entity: '表04', locationInfo: '(表04, 3-7, 1-10)', constraint: ['格式约束:xlsx', '访问权限:全部允许', '传输路径约束:点对点', '地域性约束:内网', '共享约束:允许共享'], formatConstraint: 'xlsx', accessConstraint: '全部允许', pathConstraint: '点对点', regionConstraint: '内网', shareConstraint: '允许共享', transferControl: ['可读', '可修改', '可销毁'], propagationControl: { canRead: true, canModify: true, canDestroy: true, canShare: false, canDelegate: false }, auditInfo: '查看日志', status: '待检验', feedback: '', excelData: null },
  { id: 5, entity: '表05', locationInfo: '(表05, 5-10, 8-15)', constraint: ['格式约束:jpg', '访问权限:只允许管理方获取', '传输路径约束:面对面', '地域性约束:外网', '共享约束:不允许共享'], formatConstraint: 'jpg', accessConstraint: '只允许管理方获取', pathConstraint: '面对面', regionConstraint: '外网', shareConstraint: '不允许共享', transferControl: ['可读', '可修改'], propagationControl: { canRead: true, canModify: true, canDestroy: false, canShare: false, canDelegate: false }, auditInfo: '查看日志', status: '不合格', feedback: '违反传输路径约束', excelData: null },
  { id: 6, entity: '表06', locationInfo: '(表06, 2-8, 3-10)', constraint: ['格式约束:xlsx', '访问权限:全部允许', '传输路径约束:点对点', '地域性约束:内网', '共享约束:允许共享'], formatConstraint: 'xlsx', accessConstraint: '全部允许', pathConstraint: '点对点', regionConstraint: '内网', shareConstraint: '允许共享', transferControl: ['可读', '可销毁'], propagationControl: { canRead: true, canModify: false, canDestroy: true, canShare: false, canDelegate: false }, auditInfo: '查看日志', status: '已合格', feedback: '', excelData: null },
  { id: 7, entity: '表07', locationInfo: '(表07, 1-5, 1-7)', constraint: ['格式约束:xlsx', '访问权限:全部允许', '传输路径约束:点对点', '地域性约束:内网', '共享约束:允许共享'], formatConstraint: 'xlsx', accessConstraint: '全部允许', pathConstraint: '点对点', regionConstraint: '内网', shareConstraint: '允许共享', transferControl: ['可读'], propagationControl: { canRead: true, canModify: false, canDestroy: false, canShare: false, canDelegate: false }, auditInfo: '查看日志', status: '待检验', feedback: '', excelData: null },
  { id: 8, entity: '表08', locationInfo: '(表08, 0-0, 0-0)', constraint: ['格式约束:jpg', '访问权限:只允许管理方获取', '传输路径约束:面对面', '地域性约束:外网', '共享约束:不允许共享'], formatConstraint: 'jpg', accessConstraint: '只允许管理方获取', pathConstraint: '面对面', regionConstraint: '外网', shareConstraint: '不允许共享', transferControl: ['可修改', '可销毁'], propagationControl: { canRead: false, canModify: true, canDestroy: true, canShare: false, canDelegate: false }, auditInfo: '查看日志', status: '不合格', feedback: '访问权限不符合要求', excelData: null },
  { id: 9, entity: '表09', locationInfo: '(表09, 4-15, 2-12)', constraint: ['格式约束:xlsx', '访问权限:全部允许', '传输路径约束:点对点', '地域性约束:内网', '共享约束:允许共享'], formatConstraint: 'xlsx', accessConstraint: '全部允许', pathConstraint: '点对点', regionConstraint: '内网', shareConstraint: '允许共享', transferControl: ['可读', '可修改'], propagationControl: { canRead: true, canModify: true, canDestroy: false, canShare: false, canDelegate: false }, auditInfo: '查看日志', status: '已合格', feedback: '', excelData: null },
  { id: 10, entity: '表10', locationInfo: '(表10, 7-10, 5-9)', constraint: ['格式约束:xlsx', '访问权限:全部允许', '传输路径约束:点对点', '地域性约束:内网', '共享约束:允许共享'], formatConstraint: 'xlsx', accessConstraint: '全部允许', pathConstraint: '点对点', regionConstraint: '内网', shareConstraint: '允许共享', transferControl: ['可读', '可销毁'], propagationControl: { canRead: true, canModify: false, canDestroy: true, canShare: false, canDelegate: false }, auditInfo: '查看日志', status: '待检验', feedback: '', excelData: null },
  { id: 11, entity: '表11', locationInfo: '(表11, 0-1, 0-1)', constraint: ['格式约束:jpg', '访问权限:只允许管理方获取', '传输路径约束:面对面', '地域性约束:外网', '共享约束:不允许共享'], formatConstraint: 'jpg', accessConstraint: '只允许管理方获取', pathConstraint: '面对面', regionConstraint: '外网', shareConstraint: '不允许共享', transferControl: ['可修改'], propagationControl: { canRead: false, canModify: true, canDestroy: false, canShare: false, canDelegate: false }, auditInfo: '查看日志', status: '不合格', feedback: '格式约束不符', excelData: null },
  { id: 12, entity: '表12', locationInfo: '(表12, 3-12, 4-8)', constraint: ['格式约束:xlsx', '访问权限:全部允许', '传输路径约束:点对点', '地域性约束:内网', '共享约束:允许共享'], formatConstraint: 'xlsx', accessConstraint: '全部允许', pathConstraint: '点对点', regionConstraint: '内网', shareConstraint: '允许共享', transferControl: ['可读', '可修改', '可销毁'], propagationControl: { canRead: true, canModify: true, canDestroy: true, canShare: false, canDelegate: false }, auditInfo: '查看日志', status: '已合格', feedback: '', excelData: null },
  { id: 13, entity: '表13', locationInfo: '(表13, 5-15, 3-10)', constraint: ['格式约束:xlsx', '访问权限:全部允许', '传输路径约束:点对点', '地域性约束:内网', '共享约束:允许共享'], formatConstraint: 'xlsx', accessConstraint: '全部允许', pathConstraint: '点对点', regionConstraint: '内网', shareConstraint: '允许共享', transferControl: ['可读'], propagationControl: { canRead: true, canModify: false, canDestroy: false, canShare: false, canDelegate: false }, auditInfo: '查看日志', status: '待检验', feedback: '', excelData: null },
  { id: 14, entity: '表14', locationInfo: '(表14, 2-5, 6-10)', constraint: ['格式约束:jpg', '访问权限:只允许管理方获取', '传输路径约束:面对面', '地域性约束:外网', '共享约束:不允许共享'], formatConstraint: 'jpg', accessConstraint: '只允许管理方获取', pathConstraint: '面对面', regionConstraint: '外网', shareConstraint: '不允许共享', transferControl: ['可修改', '可销毁'], propagationControl: { canRead: false, canModify: true, canDestroy: true, canShare: false, canDelegate: false }, auditInfo: '查看日志', status: '不合格', feedback: '共享约束违规', excelData: null },
  { id: 15, entity: '表15', locationInfo: '(表15, 1-10, 1-20)', constraint: ['格式约束:xlsx', '访问权限:全部允许', '传输路径约束:点对点', '地域性约束:内网', '共享约束:允许共享'], formatConstraint: 'xlsx', accessConstraint: '全部允许', pathConstraint: '点对点', regionConstraint: '内网', shareConstraint: '允许共享', transferControl: ['可读', '可修改'], propagationControl: { canRead: true, canModify: true, canDestroy: false, canShare: false, canDelegate: false }, auditInfo: '查看日志', status: '已合格', feedback: '', excelData: null },
])

// 监听数据变化的回调函数
const changeListeners = []

// 添加数据变化监听器
const addChangeListener = (callback) => {
  changeListeners.push(callback)
}

// 移除数据变化监听器
const removeChangeListener = (callback) => {
  const index = changeListeners.indexOf(callback)
  if (index !== -1) {
    changeListeners.splice(index, 1)
  }
}

// 通知所有监听器数据已变化
const notifyListeners = () => {
  changeListeners.forEach(callback => callback(sharedTableData))
}

// 保存最后接收的API原始数据
let lastReceivedApiData = null

// 根据ID获取数字对象详情
const fetchDataObjectById = async (id) => {
  try {
    // 首先尝试从API获取
    let dataObject = null
    try {
      const response = await axiosInstance.get(`/objects/${id}`)
      
      // 检查响应状态
      if (response && response.data) {
        // 判断返回格式
        
        // 情况1: 标准格式 {code: 200, message: '', data: {...}}
        if (response.data.code === 200 && response.data.data) {
          dataObject = response.data.data
        }
        // 情况2: 直接返回带code和data结构的对象，但code不是200 {code: 1, data: {...}}
        else if (response.data.code !== undefined && response.data.data) {
          dataObject = response.data
        }
        // 情况3: 直接返回对象 {...}
        else if (response.data && !Array.isArray(response.data)) {
          dataObject = response.data
        }
      }
    } catch (apiError) {
      // 删除日志
      // console.error('API获取对象详情失败:', apiError)
      // API失败，继续尝试从本地数据获取
    }
    
    // 如果API获取失败，尝试从本地数据查找
    if (!dataObject) {
      dataObject = sharedTableData.find(item => compareIds(item.id, id))
      
      if (dataObject) {
        // 本地找到的数据已经是前端格式，直接返回
        return dataObject
      } else {
        return null
      }
    }
    
    return dataObject
  } catch (error) {
    return null
  }
}

// 从后端获取数据对象列表
const fetchDataObjectsFromBackend = async () => {
  try {
    const response = await axiosInstance.get(`/objects/list`)
    
    // 保存原始响应数据
    lastReceivedApiData = response.data
    
    // 检查是否有响应数据
    if (response && response.data) {
      // 判断返回格式，兼容多种响应结构
      let dataArray = []
      
      // 情况1: 标准格式 {code: 200, message: '', data: [...]}
      if (response.data.code === 200 && Array.isArray(response.data.data)) {
        dataArray = response.data.data
      } 
      // 情况2: 直接返回数组 [...]
      else if (Array.isArray(response.data)) {
        dataArray = response.data
      }
      // 情况3: 包含在data字段中 {data: [...]}
      else if (response.data.data && Array.isArray(response.data.data)) {
        dataArray = response.data.data
      }
      // 情况4: 其他未知格式，尝试寻找数组
      else {
        console.warn('无法识别API响应格式，尝试查找数组数据')
        for (const key in response.data) {
          if (Array.isArray(response.data[key])) {
            dataArray = response.data[key]
            break
          }
        }
      }
      
      // 处理获取到的数据
      if (dataArray.length > 0) {
        // 清空当前数据
        sharedTableData.splice(0, sharedTableData.length)
        
        // 添加获取到的数据（经过适配处理）
        dataArray.forEach(item => {
          // 适配后端数据到前端格式
          const adaptedItem = adaptBackendData(item)
          sharedTableData.push(adaptedItem)
        })
        
        // 通知监听器
        notifyListeners()
        return sharedTableData
      } else {
        console.warn('从API获取的数据为空数组或未找到数组数据')
        return sharedTableData
      }
    } else {
      console.error('获取数据对象列表失败: API返回了空响应')
      return sharedTableData
    }
  } catch (error) {
    console.error('API请求错误:', error)
    // 提供更详细的错误信息
    const errorMessage = error.response 
      ? `状态码: ${error.response.status}, 消息: ${error.response.statusText || '无详细消息'}`
      : error.message || '网络错误'
    console.error(`详细错误信息: ${errorMessage}`)
    return sharedTableData
  }
}

// 获取最后接收的API数据
const getLastReceivedApiData = () => {
  return lastReceivedApiData
}

// 适配后端数据到前端格式
const adaptBackendData = (backendItem) => {
  if (!backendItem) {
    console.warn('收到空数据项，将创建默认对象')
    return createDefaultDataObject()
  }
  
  // 如果有 locationInfoJson 字段，尝试解析
  let parsedLocation = null
  if (backendItem.locationInfoJson) {
    try {
      parsedLocation = JSON.parse(backendItem.locationInfoJson)
    } catch (error) {
      console.error('位置信息JSON解析失败:', error)
    }
  }
  
  // 处理约束条件 - 从嵌套对象转换为字符串数组
  const constraintArray = extractConstraintArray(backendItem)
  
  // 处理传播控制操作 - 从布尔值属性转换为字符串数组
  const transferControlArray = extractTransferControlArray(backendItem)
  
  // 处理审计信息
  const auditInfo = extractAuditInfo(backendItem)
  
  // 处理位置信息
  const locationInfo = extractLocationInfo(backendItem)
  
  // 提取反馈信息 - 使用专门的提取函数
  const feedback = extractFeedback(backendItem)
  
  // 处理元数据 - 优先保留原始元数据，不执行元数据提取逻辑
  let metadata = null
  
  // 先检查是否有直接的metadata字段
  if (backendItem.metadata && typeof backendItem.metadata === 'object') {
    metadata = { ...backendItem.metadata };
  }
  
  // 检查是否保留了原始元数据(防止覆盖用户输入的元数据)
  if (!metadata && backendItem.originalMetadata && typeof backendItem.originalMetadata === 'object') {
    metadata = { ...backendItem.originalMetadata }
  }
  
  // 尝试从metadataJson解析
  if (!metadata && backendItem.metadataJson) {
    try {
      let parsedMetadata = null;
      
      // 处理字符串类型的metadataJson
      if (typeof backendItem.metadataJson === 'string') {
        // 替换可能导致解析问题的字符
        let jsonStr = backendItem.metadataJson
          .replace(/，/g, ',')  // 中文逗号替换为英文逗号
          .replace(/：/g, ':')  // 中文冒号替换为英文冒号
          .replace(/【/g, '[')  // 中文方括号替换为英文方括号
          .replace(/】/g, ']')
          .replace(/"/g, '"')   // 中文引号替换为英文引号
          .replace(/"/g, '"')
          .replace(/'/g, "'")   // 中文单引号替换为英文单引号
          .replace(/'/g, "'")
          .replace(/；/g, ';')  // 中文分号替换为英文分号
          .trim();
        
        // 尝试修复常见的JSON格式问题
        if (!jsonStr.startsWith('{')) jsonStr = '{' + jsonStr;
        if (!jsonStr.endsWith('}')) jsonStr = jsonStr + '}';
        
        // 解析JSON
        parsedMetadata = JSON.parse(jsonStr);
      } else if (typeof backendItem.metadataJson === 'object') {
        // 对象类型直接使用
        parsedMetadata = backendItem.metadataJson;
      }
      
      if (parsedMetadata) {
        metadata = {
          dataName: parsedMetadata.dataName || extractEntityName(backendItem),
          sourceUnit: parsedMetadata.sourceUnit || '',
          contactPerson: parsedMetadata.contactPerson || '',
          contactPhone: parsedMetadata.contactPhone || '',
          resourceSummary: parsedMetadata.resourceSummary || '',
          fieldClassification: parsedMetadata.fieldClassification || '',
          headers: parsedMetadata.headers || []
        };
      }
    } catch (error) {
      console.error('解析metadataJson失败:', error);
    }
  }
  
  // 尝试从dataEntity中提取元数据
  if (!metadata && backendItem.dataEntity) {
    if (backendItem.dataEntity.metadata && typeof backendItem.dataEntity.metadata === 'object') {
      metadata = { ...backendItem.dataEntity.metadata };
    } else if (backendItem.dataEntity.metadataJson) {
      try {
        const parsedMetadata = typeof backendItem.dataEntity.metadataJson === 'string' 
          ? JSON.parse(backendItem.dataEntity.metadataJson)
          : backendItem.dataEntity.metadataJson;
        
        metadata = {
          dataName: parsedMetadata.dataName || extractEntityName(backendItem),
          sourceUnit: parsedMetadata.sourceUnit || '',
          contactPerson: parsedMetadata.contactPerson || '',
          contactPhone: parsedMetadata.contactPhone || '',
          resourceSummary: parsedMetadata.resourceSummary || '',
          fieldClassification: parsedMetadata.fieldClassification || '',
          headers: parsedMetadata.headers || []
        };
      } catch (error) {
        console.error('解析dataEntity.metadataJson失败:', error);
      }
    }
  }
  
  // 如果以上方法都无法获取元数据，创建默认元数据
  if (!metadata) {
    metadata = {
      dataName: extractEntityName(backendItem),
      sourceUnit: '',
      contactPerson: '',
      contactPhone: '',
      resourceSummary: '',
      fieldClassification: '',
      headers: []
    };
  }
  
  // 构建适配后的数据对象
  const result = {
    id: backendItem.id !== undefined ? backendItem.id : null,
    entity: extractEntityName(backendItem),
    locationInfo: locationInfo,
    constraint: constraintArray,
    metadata: metadata,
    // 保存原始元数据
    originalMetadata: backendItem.originalMetadata || metadata,
    transferControl: transferControlArray,
    propagationControl: extractConstraintData(backendItem).propagationControl || {},
    auditInfo: auditInfo,
    status: extractStatus(backendItem),
    feedback: feedback,
    // 保存其他可能需要的字段
    formatConstraint: extractConstraintData(backendItem).formatConstraint || '',
    accessConstraint: extractConstraintData(backendItem).accessConstraint || '',
    pathConstraint: extractConstraintData(backendItem).pathConstraint || '',
    regionConstraint: extractConstraintData(backendItem).regionConstraint || '',
    shareConstraint: extractConstraintData(backendItem).shareConstraint || '',
    // 保存Excel数据
    excelData: backendItem.excelData || null,
    dataItems: backendItem.dataItems || [],
    
    // 提取分类分级值相关字段
    classificationValue: backendItem.classificationValue || '',
    industryCategory: backendItem.industryCategory || '',
    dataTimeliness: backendItem.dataTimeliness || '',
    dataSource: backendItem.dataSource || '',
    levelValue: backendItem.levelValue || '',
    
    // 从后端获取totalCategoryValue和totalGradeValue字段
    totalCategoryValue: backendItem.totalCategoryValue || '',
    totalGradeValue: backendItem.totalGradeValue || '',
    
    // 提取分级值字段
    dbGrade: backendItem.dbGrade !== undefined ? backendItem.dbGrade : 0,
    tableGrade: backendItem.tableGrade !== undefined ? backendItem.tableGrade : 0,
    rowGrades: backendItem.rowGrades || [0, 0],
    columnGrades: backendItem.columnGrades || [0, 0]
  };
  
  return result;
}

// 提取实体名称
const extractEntityName = (backendItem) => {
  // 从dataEntity中提取实体名称
  if (backendItem.dataEntity && backendItem.dataEntity.entity) {
    return backendItem.dataEntity.entity
  }
  
  // 尝试从 dataContent 字段解析
  if (backendItem.dataContent) {
    try {
      const dataEntity = JSON.parse(backendItem.dataContent)
      if (dataEntity && dataEntity.entity) {
        return dataEntity.entity
      }
    } catch (error) {
      console.warn('解析 dataContent 失败:', error)
    }
  }
  
  // 如果包含 id 字段，使用"表xx"格式
  if (backendItem.id || backendItem.numericId) {
    const id = backendItem.id || backendItem.numericId
    // 如果id是数字且小于100，使用2位数格式（例如：表01、表02）
    if (typeof id === 'number' || !isNaN(Number(id))) {
      const numId = Number(id)
      if (numId < 100) {
        return `表${numId < 10 ? '0' + numId : numId}`
      }
    }
    return `表${id}`
  }
  
  // 如果没有dataEntity，尝试其他可能的字段
  return getValidValue(backendItem.entity, '未命名对象')
}

// 提取位置信息
const extractLocationInfo = (backendItem) => {
  // 如果已经是格式化的字符串，直接返回
  if (typeof backendItem.locationInfo === 'string') {
    return backendItem.locationInfo
  }
  
  // 如果有 locationInfoJson 字段（JSON 字符串格式）
  if (backendItem.locationInfoJson) {
    try {
      // 解析 JSON 字符串
      const locationInfoObj = JSON.parse(backendItem.locationInfoJson)
      const entity = extractEntityName(backendItem)
      
      // 如果有 locations 数组且不为空
      if (Array.isArray(locationInfoObj.locations) && locationInfoObj.locations.length > 0) {
        const location = locationInfoObj.locations[0]
        // 格式化行列信息
        const rowRange = location.startRow && location.endRow 
          ? `${location.startRow}-${location.endRow}`
          : '-'
        const colRange = location.startColumn && location.endColumn 
          ? `${location.startColumn}-${location.endColumn}`
          : '-'
        return `(${entity}, ${rowRange}, ${colRange})`
      }
    } catch (error) {
      console.error('解析 locationInfoJson 失败:', error, backendItem.locationInfoJson)
    }
  }
  
  // 如果是对象，尝试格式化
  if (backendItem.locationInfo && typeof backendItem.locationInfo === 'object') {
    const entity = extractEntityName(backendItem)
    
    // 如果有locations数组，使用第一个location
    if (Array.isArray(backendItem.locationInfo.locations) && backendItem.locationInfo.locations.length > 0) {
      const location = backendItem.locationInfo.locations[0]
      const rowRange = location.startRow && location.endRow 
        ? `${location.startRow}-${location.endRow}`
        : '-'
      const colRange = location.startColumn && location.endColumn 
        ? `${location.startColumn}-${location.endColumn}`
        : '-'
      return `(${entity}, ${rowRange}, ${colRange})`
    }
  }
  
  // 如果有dataEntity对象，使用其中的信息
  if (backendItem.dataEntity) {
    const entity = backendItem.dataEntity.entity || '默认表'
    return `(${entity}, -, -)`
  }
  
  return '(-, -, -)'
}

// 从后端数据中提取约束条件数组
const extractConstraintArray = (backendItem) => {
  const constraintArray = []
  
  // 处理1: 如果已经是字符串数组，直接使用
  if (Array.isArray(backendItem.constraint)) {
    return backendItem.constraint
  }
  
  // 处理2: 从constraintSet.constraints或constraintSet.selectedConstraint中提取
  if (backendItem.constraintSet) {
    // 优先使用selectedConstraint
    let constraint = backendItem.constraintSet.selectedConstraint
    
    // 如果没有selectedConstraint但有constraints数组，使用第一个
    if (!constraint && Array.isArray(backendItem.constraintSet.constraints) && 
        backendItem.constraintSet.constraints.length > 0) {
      constraint = backendItem.constraintSet.constraints[0]
    }
    
    // 从constraint对象中提取各项约束并格式化
    if (constraint) {
      if (constraint.formatConstraint) {
        constraintArray.push(`格式约束:${constraint.formatConstraint}`)
      }
      if (constraint.accessConstraint) {
        constraintArray.push(`访问权限:${constraint.accessConstraint}`)
      }
      if (constraint.pathConstraint) {
        constraintArray.push(`传输路径约束:${constraint.pathConstraint}`)
      }
      if (constraint.regionConstraint) {
        constraintArray.push(`地域性约束:${constraint.regionConstraint}`)
      }
      if (constraint.shareConstraint) {
        constraintArray.push(`共享约束:${constraint.shareConstraint}`)
      }
    }
  }
  
  return constraintArray
}

// 提取约束条件数据对象
const extractConstraintData = (backendItem) => {
  const result = {
    formatConstraint: '',
    accessConstraint: '',
    pathConstraint: '',
    regionConstraint: '',
    shareConstraint: ''
  }
  
  // 情况1: 如果前端已经有这些字段，优先使用
  if (backendItem.formatConstraint) result.formatConstraint = backendItem.formatConstraint
  if (backendItem.accessConstraint) result.accessConstraint = backendItem.accessConstraint
  if (backendItem.pathConstraint) result.pathConstraint = backendItem.pathConstraint
  if (backendItem.regionConstraint) result.regionConstraint = backendItem.regionConstraint
  if (backendItem.shareConstraint) result.shareConstraint = backendItem.shareConstraint
  
  // 情况2: 从constraintSet中提取
  if (backendItem.constraintSet) {
    // 检查是否有constraints数组并且有值
    if (Array.isArray(backendItem.constraintSet.constraints) && 
        backendItem.constraintSet.constraints.length > 0) {
      const constraint = backendItem.constraintSet.constraints[0]
      if (constraint) {
        if (constraint.formatConstraint) result.formatConstraint = constraint.formatConstraint
        if (constraint.accessConstraint) result.accessConstraint = constraint.accessConstraint
        if (constraint.pathConstraint) result.pathConstraint = constraint.pathConstraint
        if (constraint.regionConstraint) result.regionConstraint = constraint.regionConstraint
        if (constraint.shareConstraint) result.shareConstraint = constraint.shareConstraint
      }
    }
    // 向后兼容：检查selectedConstraint
    else if (backendItem.constraintSet.selectedConstraint) {
      const constraint = backendItem.constraintSet.selectedConstraint
      if (constraint.formatConstraint) result.formatConstraint = constraint.formatConstraint
      if (constraint.accessConstraint) result.accessConstraint = constraint.accessConstraint
      if (constraint.pathConstraint) result.pathConstraint = constraint.pathConstraint
      if (constraint.regionConstraint) result.regionConstraint = constraint.regionConstraint
      if (constraint.shareConstraint) result.shareConstraint = constraint.shareConstraint
    }
  }
  
  // 情况3: 尝试从constraint数组中提取
  if (Array.isArray(backendItem.constraint) && backendItem.constraint.length > 0) {
    backendItem.constraint.forEach(item => {
      if (typeof item === 'string') {
        const parts = item.split(':')
        if (parts.length === 2) {
          const key = parts[0].trim()
          const value = parts[1].trim()
          
          if (key === '格式约束') result.formatConstraint = value
          else if (key === '访问权限') result.accessConstraint = value
          else if (key === '传输路径约束') result.pathConstraint = value
          else if (key === '地域性约束') result.regionConstraint = value
          else if (key === '共享约束') result.shareConstraint = value
        }
      }
    })
  }
  
  return result
}

// 从后端数据中提取传播控制操作数组
const extractTransferControlArray = (backendItem) => {
  const transferControlArray = []
  
  // 处理1: 如果已经是字符串数组，直接使用
  if (Array.isArray(backendItem.transferControl)) {
    return backendItem.transferControl
  }
  
  // 处理2: 处理propagationControlJson字段（JSON字符串格式）
  if (backendItem.propagationControlJson) {
    try {
      // 解析JSON字符串
      const propagationControl = typeof backendItem.propagationControlJson === 'string' 
        ? JSON.parse(backendItem.propagationControlJson) 
        : backendItem.propagationControlJson
      
      // 从operations对象提取操作
      if (propagationControl && propagationControl.operations) {
        const ops = propagationControl.operations
        // 值为1表示对应操作可用
        if (ops.read === 1) transferControlArray.push('可读')
        if (ops.modify === 1) transferControlArray.push('可修改') 
        if (ops.share === 1) transferControlArray.push('可共享')
        if (ops.delegate === 1) transferControlArray.push('可委托')
        if (ops.destroy === 1) transferControlArray.push('可销毁')
      }
      
      // 如果解析出了传输控制操作，直接返回
      if (transferControlArray.length > 0) {
        return transferControlArray
      }
    } catch (error) {
      // 解析失败，继续下一步处理
    }
  }
  
  // 处理3: 如果有propagationControl对象，尝试提取operations
  if (backendItem.propagationControl) {
    const control = backendItem.propagationControl;
    
    // 优先从operations中提取
    if (backendItem.propagationControl.operations) {
      const ops = backendItem.propagationControl.operations
      if (ops.read === 1) transferControlArray.push('可读')
      if (ops.modify === 1) transferControlArray.push('可修改')
      if (ops.share === 1) transferControlArray.push('可共享')
      if (ops.delegate === 1) transferControlArray.push('可委托')
      if (ops.destroy === 1) transferControlArray.push('可销毁')
    }
    // 向后兼容：从布尔字段中提取
    else {
      if (control.canRead === true) transferControlArray.push('可读')
      if (control.canModify === true) transferControlArray.push('可修改')
      if (control.canShare === true) transferControlArray.push('可共享')
      if (control.canDelegate === true) transferControlArray.push('可委托')
      if (control.canDestroy === true) transferControlArray.push('可销毁')
    }
    
    // 如果有selectedOperations对象，也检查它
    if (control.selectedOperations) {
      if (control.selectedOperations.read === true) 
        !transferControlArray.includes('可读') && transferControlArray.push('可读')
      if (control.selectedOperations.modify === true) 
        !transferControlArray.includes('可修改') && transferControlArray.push('可修改')
      if (control.selectedOperations.share === true) 
        !transferControlArray.includes('可共享') && transferControlArray.push('可共享')
      if (control.selectedOperations.delegate === true) 
        !transferControlArray.includes('可委托') && transferControlArray.push('可委托')
      if (control.selectedOperations.destroy === true) 
        !transferControlArray.includes('可销毁') && transferControlArray.push('可销毁')
    }
  }
  
  return transferControlArray
}

// 提取审计信息
const extractAuditInfo = (backendItem) => {
  // 统一返回"查看日志"，无论输入的数据格式如何
  return '查看日志'
}

// 提取状态信息
const extractStatus = (backendItem) => {
  // 优先从dataEntity中获取
  if (backendItem.dataEntity && backendItem.dataEntity.status) {
    return backendItem.dataEntity.status
  }
  
  // 其次尝试直接获取status字段
  return getValidValue(backendItem.status, '待检验')
}

// 获取有效值，如果第一个值无效则使用后备值
const getValidValue = (value, ...fallbacks) => {
  if (value !== undefined && value !== null) {
    return value
  }
  
  // 遍历所有后备值，返回第一个有效的
  for (const fallback of fallbacks) {
    if (fallback !== undefined && fallback !== null) {
      return fallback
    }
  }
  
  // 如果都没有有效值，返回空字符串
  return ''
}

// 确保数组格式
const ensureArray = (value) => {
  if (Array.isArray(value)) {
    return [...value]
  }
  return value ? [value] : []
}

// 创建默认数据对象
const createDefaultDataObject = () => {
  return {
    id: 0,
    entity: '未命名对象',
    locationInfo: '(-, -, -)',
    constraint: [],
    transferControl: ['可读', '可修改', '可销毁', '可共享', '可委托'],
    propagationControl: {
      canRead: true,
      canModify: true,
      canDestroy: true,
      canShare: true,
      canDelegate: true
    },
    auditInfo: '查看日志',
    status: '待检验',
    feedback: '',
    excelData: null,
    formatConstraint: '',
    accessConstraint: '',
    pathConstraint: '',
    regionConstraint: '',
    shareConstraint: ''
  }
}

// 将前端数据转换为后端所需的格式
const transformToBackendFormat = (frontendData) => {
  // 优先使用originalMetadata(如果存在)，否则使用metadata，确保原始用户输入被保留
  const userMetadata = frontendData.originalMetadata || frontendData.metadata || {};
  
  // 构建数据实体对象 - 确保所有字段都在dataEntity内
  const dataEntity = {
    entity: frontendData.entity || '',
    status: frontendData.status || '待审核', // 注意这里使用"待审核"作为默认值
    feedback: frontendData.feedback || '',
    // 确保使用用户提供的元数据，不使用默认值覆盖用户数据
    metadata: {
      dataName: userMetadata.dataName || frontendData.entity || '',
      sourceUnit: userMetadata.sourceUnit || '',
      contactPerson: userMetadata.contactPerson || '',
      contactPhone: userMetadata.contactPhone || '',
      resourceSummary: userMetadata.resourceSummary || '',
      fieldClassification: userMetadata.fieldClassification || '',
      headers: userMetadata.headers || []
    },
    // 添加一个标记，表示这是用户输入的元数据
    _userMetadataProcessed: true,
    // 保留原始元数据，确保后续处理不会丢失
    originalMetadata: { ...userMetadata },
    dataItems: frontendData.dataItems || []
  }
  
  // 添加metadataJson字段，确保后端能正确识别元数据
  const metadataJsonObj = {
    dataName: userMetadata.dataName || frontendData.entity || '',
    sourceUnit: userMetadata.sourceUnit || '',
    contactPerson: userMetadata.contactPerson || '',
    contactPhone: userMetadata.contactPhone || '',
    resourceSummary: userMetadata.resourceSummary || '',
    fieldClassification: userMetadata.fieldClassification || '',
    headers: userMetadata.headers || []
  };
  
  // 将元数据对象转换为JSON字符串
  dataEntity.metadataJson = JSON.stringify(metadataJsonObj);

  // 如果有Excel文件ID，也添加到dataEntity中
  if (frontendData.excelFileId) {
    dataEntity.excelFileId = frontendData.excelFileId;
  }

  // 构建位置信息对象
  const locationInfo = {
    locations: [
      {
        sheet: frontendData.sheet || "Sheet1",
        startRow: frontendData.locationInfo && frontendData.locationInfo.row ? frontendData.locationInfo.row.split('-')[0] : "1",
        endRow: frontendData.locationInfo && frontendData.locationInfo.row ? frontendData.locationInfo.row.split('-')[1] || "100" : "100",
        startColumn: frontendData.locationInfo && frontendData.locationInfo.col ? frontendData.locationInfo.col.split('-')[0] : "A",
        endColumn: frontendData.locationInfo && frontendData.locationInfo.col ? frontendData.locationInfo.col.split('-')[1] || "Z" : "Z"
      }
    ]
  }

  // 创建约束条件字符串 - 使用完全符合要求的格式
  const constraintSet = {
    constraints: [
      {
        formatConstraint: frontendData.formatConstraint || "xlsx",
        accessConstraint: frontendData.accessConstraint || "全部允许",
        pathConstraint: frontendData.pathConstraint || "点对点",
        regionConstraint: frontendData.regionConstraint || "内网",
        shareConstraint: frontendData.shareConstraint || "允许共享"
      }
    ]
  };

  // 创建传播控制对象 - 使用完全符合要求的格式
  const hasRead = frontendData.transferControl && frontendData.transferControl.includes("可读");
  const hasModify = frontendData.transferControl && frontendData.transferControl.includes("可修改");
  const hasShare = frontendData.transferControl && frontendData.transferControl.includes("可共享");
  const hasDelegate = frontendData.transferControl && frontendData.transferControl.includes("可委托");
  const hasDestroy = frontendData.transferControl && frontendData.transferControl.includes("可销毁");

  const propagationControl = {
    selectedOperations: {
      read: hasRead,
      modify: hasModify,
      share: hasShare,
      delegate: hasDelegate,
      destroy: hasDestroy
    },
    canRead: hasRead,
    canModify: hasModify,
    canShare: hasShare, 
    canDelegate: hasDelegate,
    canDestroy: hasDestroy,
    operations: {
      read: hasRead ? 1 : 0,
      modify: hasModify ? 1 : 0,
      share: hasShare ? 1 : 0,
      delegate: hasDelegate ? 1 : 0,
      destroy: hasDestroy ? 1 : 0
    }
  };

  // 构建审计信息对象
  const auditInfo = {
    auditRecords: [
      {
        subject: "system",
        object: frontendData.entity || "数据对象",
        operationType: frontendData.id ? "更新" : "创建",
        timestamp: new Date().toISOString().replace('T', ' ').substring(0, 19),
        blockHash: "0x" + Math.random().toString(16).substring(2, 10),
        previousHash: "0x" + Math.random().toString(16).substring(2, 10)
      }
    ]
  }

  // 返回后端所需的完整格式
  const result = {
    id: frontendData.id || "",
    dataEntity: dataEntity,
    locationInfo: locationInfo,
    constraintSet: constraintSet,
    propagationControl: propagationControl,
    auditInfo: auditInfo,
    // 在顶级添加元数据字段，确保后端可以直接访问
    metadata: dataEntity.metadata,
    metadataJson: dataEntity.metadataJson,
    
    // 添加分类分级值字段
    totalCategoryValue: frontendData.totalCategoryValue || frontendData.classificationValue || '',
    totalGradeValue: frontendData.totalGradeValue || frontendData.levelValue || '',
    
    // 添加分级值字段
    dbGrade: frontendData.dbGrade,
    tableGrade: frontendData.tableGrade,
    rowGrades: frontendData.rowGrades,
    columnGrades: frontendData.columnGrades,
    
    // 确保dataItems也被添加到顶级对象中，保持编辑时不丢失数据
    dataItems: frontendData.dataItems || []
  }

  return result;
}

// 获取CSRF令牌的函数
const getCsrfToken = () => {
  // 首先检查内存中是否已有令牌
  if (csrfToken) {
    return csrfToken;
  }
  
  // 尝试从Cookie中获取
  const tokenFromCookie = Cookies.get('XSRF-TOKEN') || Cookies.get('csrf_token');
  if (tokenFromCookie) {
    csrfToken = tokenFromCookie;
    return tokenFromCookie;
  }
  
  // 如果都没有，返回空值
  return '';
};

// 更新prepareCsrfToken函数
const prepareCsrfToken = async () => {
  try {
    // 首先检查是否已经有令牌
    const existingToken = getCsrfToken();
    if (existingToken) {
      console.log('使用现有的CSRF令牌');
      return existingToken;
    }
    
    // 尝试从不同端点获取CSRF token
    try {
      // 首先尝试常规的/csrf-token端点
      const response = await axiosInstance.get('/csrf-token');
      
      if (response.data && response.data.token) {
        cookieService.setCookie('XSRF-TOKEN', response.data.token);
        csrfToken = response.data.token;
        return response.data.token;
      } else if (response.data && typeof response.data === 'string') {
        cookieService.setCookie('XSRF-TOKEN', response.data);
        csrfToken = response.data;
        return response.data;
      }
    } catch (firstError) {
      console.warn('第一个CSRF端点失败，尝试备用端点:', firstError.message);
      
      // 如果第一个端点失败，尝试备用端点
      try {
        const backupResponse = await axiosInstance.get('/api/security/csrf');
        if (backupResponse.data && (backupResponse.data.token || typeof backupResponse.data === 'string')) {
          const token = backupResponse.data.token || backupResponse.data;
          cookieService.setCookie('XSRF-TOKEN', token);
          csrfToken = token;
          return token;
        }
      } catch (backupError) {
        console.warn('备用CSRF端点也失败:', backupError.message);
        // 继续尝试从cookie获取
      }
    }
    
    // 尝试从HTTP头中获取
    try {
      const headResponse = await axiosInstance.head('/');
      const tokenHeader = headResponse.headers['x-csrf-token'] || 
                         headResponse.headers['X-CSRF-TOKEN'] || 
                         headResponse.headers['csrf-token'];
      if (tokenHeader) {
        cookieService.setCookie('XSRF-TOKEN', tokenHeader);
        csrfToken = tokenHeader;
        return tokenHeader;
      }
    } catch (headError) {
      console.warn('通过HEAD请求获取CSRF令牌失败:', headError.message);
    }
    
    // 如果上述方法都失败，尝试生成一个随机令牌
    // 这是一个回退方案，可能不是所有后端都接受
    const randomToken = Math.random().toString(36).substring(2, 15);
    console.warn('无法从服务器获取CSRF令牌，使用随机生成的令牌:', randomToken);
    cookieService.setCookie('XSRF-TOKEN', randomToken);
    csrfToken = randomToken;
    return randomToken;
  } catch (error) {
    console.error('获取CSRF token失败:', error);
    // 失败时仍尝试从cookie获取
    const fallbackToken = Cookies.get('XSRF-TOKEN') || Cookies.get('csrf_token') || '';
    
    // 如果有回退令牌，使用它
    if (fallbackToken) {
      csrfToken = fallbackToken;
      console.log('使用回退的CSRF令牌');
    }
    
    return fallbackToken;
  }
};

// 更新addDataObjectViaApi和updateDataObjectViaApi函数，确保使用正确的字段名和格式
const addDataObjectViaApi = async (dataObject, extraParams = {}) => {
  try {
    // 准备csrfToken
    const token = await prepareCsrfToken();
    
    // 如果没有originalMetadata，但有metadata，则创建一个副本
    if (!dataObject.originalMetadata && dataObject.metadata) {
      dataObject.originalMetadata = { ...dataObject.metadata };
    }
    
    // 检查元数据完整性，如果metadata为null但有表单信息，则创建元数据
    if (!dataObject.metadata && !dataObject.originalMetadata) {
      // 尝试从其他属性中收集元数据信息
      const createdMetadata = {
        dataName: dataObject.entity || '未命名数据',
        sourceUnit: '',
        contactPerson: '',
        contactPhone: '',
        resourceSummary: '',
        fieldClassification: '',
        headers: dataObject.dataItems && dataObject.dataItems.length > 0 ? 
                 Object.keys(dataObject.dataItems[0] || {}) : []
      };
      
      dataObject.metadata = createdMetadata;
      dataObject.originalMetadata = { ...createdMetadata };
    }
    
    // 创建请求数据对象
    const requestData = transformToBackendFormat(dataObject);
    
    // 确保原始元数据被保留
    if (dataObject.originalMetadata) {
      if (!requestData.dataEntity.originalMetadata) {
        requestData.dataEntity.originalMetadata = { ...dataObject.originalMetadata };
      }
    }
    
    // 确保顶级请求对象也包含metadataJson
    if (requestData.dataEntity && requestData.dataEntity.metadataJson) {
      // 将metadataJson字段提升到顶级对象
      requestData.metadataJson = requestData.dataEntity.metadataJson;
    } else if (dataObject.metadata) {
      // 如果dataEntity中没有metadataJson但有元数据，创建它
      const metadataJsonObj = {
        dataName: dataObject.metadata.dataName || dataObject.entity || '',
        sourceUnit: dataObject.metadata.sourceUnit || '',
        contactPerson: dataObject.metadata.contactPerson || '',
        contactPhone: dataObject.metadata.contactPhone || '',
        resourceSummary: dataObject.metadata.resourceSummary || '',
        fieldClassification: dataObject.metadata.fieldClassification || '',
        headers: dataObject.metadata.headers || []
      };
      requestData.metadataJson = JSON.stringify(metadataJsonObj);
    }
    
    // 确保顶级请求对象直接包含metadata字段
    if (!requestData.metadata && requestData.dataEntity && requestData.dataEntity.metadata) {
      requestData.metadata = requestData.dataEntity.metadata;
    } else if (!requestData.metadata && dataObject.metadata) {
      requestData.metadata = { ...dataObject.metadata };
    }
    
    // 添加额外的请求参数
    if (extraParams && Object.keys(extraParams).length > 0) {
      Object.assign(requestData, extraParams);
      
      // 如果有preserveUserMetadata参数，确保将它也添加到dataEntity
      if (extraParams.preserveUserMetadata) {
        requestData.dataEntity._preserveUserMetadata = true;
      }
    }
    
    // 确保excelFileId存在于请求中
    if (dataObject.excelFileId && !requestData.dataEntity.excelFileId) {
      requestData.dataEntity.excelFileId = dataObject.excelFileId;
    }
    
    // 确保请求路径包含查询参数
    let url = `${API_URL}/objects`;
    if (dataObject.excelFileId) {
      url += `?excelFileId=${encodeURIComponent(dataObject.excelFileId)}`;
    }
    
    // 准备请求头
    const headers = {
      'Content-Type': 'application/json'
    };
    
    // 如果有令牌，添加到请求头
    if (token) {
      headers['X-CSRF-TOKEN'] = token;
    }
    
    // 发送请求
    const response = await axiosInstance.post(url, requestData, {
      headers,
      withCredentials: true // 确保发送cookie
    });
    
    // 检查响应
    if (response.status === 200 || response.status === 201) {
      // 处理响应数据
      let responseData = response.data;
      
      // 处理不同格式的响应数据
      if (responseData && typeof responseData === 'string') {
        try {
          responseData = JSON.parse(responseData);
        } catch (e) {
          // 创建一个包含基本信息的响应对象
          responseData = {
            message: responseData,
            success: true,
            dataEntity: {
              entity: dataObject.entity
            }
          };
        }
      }
      
      // 如果响应只是简单成功消息，创建完整的响应对象
      if (responseData && 
          (responseData.code === 1 || responseData.code === 200) && 
          responseData.msg === 'success' && 
          !responseData.metadata && 
          !responseData.dataEntity) {
        
        // 将原始信息保存在响应中
        responseData.originalMetadata = dataObject.originalMetadata || dataObject.metadata;
        
        // 创建dataEntity
        if (!responseData.dataEntity) {
          responseData.dataEntity = {
            entity: dataObject.entity,
            metadata: dataObject.metadata
          };
        }
      }
      
      if (responseData) {
        // 确保响应中有元数据字段
        if (dataObject.originalMetadata) {
          responseData.originalMetadata = { ...dataObject.originalMetadata };
        }
      }
      
      // 更新本地数据
      const newObject = adaptBackendData(responseData);
      
      // 确保新对象也保留原始元数据
      if (dataObject.originalMetadata && newObject) {
        if (!newObject.metadata || 
            (newObject.metadata.dataName !== dataObject.originalMetadata.dataName) ||
            (newObject.metadata.sourceUnit !== dataObject.originalMetadata.sourceUnit) ||
            (newObject.metadata.contactPerson !== dataObject.originalMetadata.contactPerson)) {
          // 使用原始元数据
          newObject.metadata = { ...dataObject.originalMetadata };
        }
        
        // 无论如何都保留originalMetadata
        newObject.originalMetadata = { ...dataObject.originalMetadata };
      }
      
      // 添加到数据列表
      sharedTableData.push(newObject);
      
      // 触发监听器
      notifyListeners();
      
      return {
        success: true,
        data: newObject
      };
    } else {
      console.warn('添加数据对象失败:', response);
      return {
        success: false,
        message: '无法添加数据对象',
        error: response
      };
    }
  } catch (error) {
    console.error('添加数据对象异常:', error);
    
    // 准备错误信息
    let errorMessage = '添加数据对象失败';
    if (error.response) {
      // 服务器返回错误
      if (error.response.data && error.response.data.message) {
        errorMessage = error.response.data.message;
      } else if (typeof error.response.data === 'string') {
        errorMessage = error.response.data.substring(0, 100);
      } else {
        errorMessage = `服务器错误: ${error.response.status}`;
      }
    } else if (error.request) {
      // 请求发送但没有收到响应
      errorMessage = '服务器未响应';
    } else {
      // 请求设置出错
      errorMessage = error.message;
    }
    
    return {
      success: false,
      message: errorMessage,
      error: error
    };
  }
};

// 更新updateDataObjectViaApi函数，确保使用相同的格式
const updateDataObjectViaApi = async (id, dataObject) => {
  try {
    if (!id || !dataObject) {
      console.error('更新数据对象失败: ID或数据对象为空', id, dataObject)
      return false
    }
    
    // 获取当前对象，确保保留已有的dataItems
    let originalDataItems = [];
    try {
      const currentIndex = sharedTableData.findIndex(item => compareIds(item.id, id));
      if (currentIndex !== -1) {
        const currentObject = sharedTableData[currentIndex];
        if (currentObject.dataItems && Array.isArray(currentObject.dataItems) && currentObject.dataItems.length > 0) {
          originalDataItems = [...currentObject.dataItems];
        }
      }
    } catch (e) {
      console.warn('获取本地dataItems失败:', e);
    }
    
    // 检查传入对象是否有dataItems
    if (!dataObject.dataItems || dataObject.dataItems.length === 0) {
      if (originalDataItems.length > 0) {
        dataObject.dataItems = originalDataItems;
      }
    }
    
    // 获取CSRF令牌
    const token = await prepareCsrfToken();
    
    // 将前端数据转换为后端格式
    const backendData = transformToBackendFormat(dataObject)
    
    // 再次确认backendData包含dataItems
    if (!backendData.dataItems || backendData.dataItems.length === 0) {
      if (dataObject.dataItems && dataObject.dataItems.length > 0) {
        backendData.dataItems = dataObject.dataItems;
      } else if (originalDataItems.length > 0) {
        backendData.dataItems = originalDataItems;
      }
    }
    
    // 确保dataEntity中也包含dataItems
    if (backendData.dataEntity && (!backendData.dataEntity.dataItems || backendData.dataEntity.dataItems.length === 0)) {
      if (backendData.dataItems && backendData.dataItems.length > 0) {
        backendData.dataEntity.dataItems = backendData.dataItems;
      } else if (dataObject.dataItems && dataObject.dataItems.length > 0) {
        backendData.dataEntity.dataItems = dataObject.dataItems;
      } else if (originalDataItems.length > 0) {
        backendData.dataEntity.dataItems = originalDataItems;
      }
    }
    
    // 设置请求头
    const headers = {
      'Content-Type': 'application/json'
    };
    
    // 如果有令牌，添加到请求头
    if (token) {
      headers['X-CSRF-TOKEN'] = token;
    }
    
    // 发送PUT请求到后端
    const response = await axiosInstance.put(`/objects/${id}`, backendData, {
      headers,
      withCredentials: true // 确保发送cookie
    });
    
    // 检查响应状态
    if (response.status === 200 || response.status === 204) {
      console.log('数字对象更新成功')
      
      // 同时更新本地数据
      updateDataObject(dataObject)
      
      return true
    }
    
    // 处理返回的数据格式
    if (response && response.data) {
      if (response.data.code === 200) {
        console.log('数字对象更新成功')
        
        // 同时更新本地数据
        updateDataObject(dataObject)
        
        return true
      }
    }
    
    console.warn('API返回了非预期的响应格式:', response)
    return false
  } catch (error) {
    console.error('通过API更新数字对象失败:', error)
    
    // 提供更详细的错误信息
    const errorDetails = error.response 
      ? `错误状态: ${error.response.status}, 消息: ${error.response.statusText || '未知错误'}`
      : error.message || '网络错误';
    console.error(`详细错误信息: ${errorDetails}`);
    
    // 尽管API调用失败，我们仍然更新本地数据
    try {
      updateDataObject(dataObject);
      console.log('API更新失败，但已更新本地数据');
    } catch (localError) {
      console.error('本地数据更新也失败:', localError);
    }
    
    return false
  }
}

// 添加新的数字对象
const addDataObject = (newObject) => {
  // 确保对象有唯一ID
  const newId = sharedTableData.length > 0 
    ? Math.max(...sharedTableData.map(item => item.id)) + 1 
    : 1
  
  const objectToAdd = {
    id: newId,
    ...newObject,
    auditInfo: newObject.auditInfo || '查看日志',
    status: newObject.status || '待检验',
    feedback: newObject.feedback || ''
  }
  
  // 添加到数组
  sharedTableData.unshift(objectToAdd)
  
  // 通知监听器
  notifyListeners()
  
  return objectToAdd
}

// 更新数字对象
const updateDataObject = (updatedObject) => {
  const index = sharedTableData.findIndex(item => compareIds(item.id, updatedObject.id))
  
  if (index !== -1) {
    // 保留原始对象中没被更新的字段
    const originalObject = sharedTableData[index]
    
    // 特别处理dataItems字段，确保不会丢失
    if (!updatedObject.dataItems && originalObject.dataItems) {
      updatedObject.dataItems = originalObject.dataItems
    }
    
    sharedTableData[index] = {
      ...originalObject,
      ...updatedObject
    }
    
    // 通知监听器
    notifyListeners()
    
    return true
  }
  
  return false
}

// 通过API删除数字对象
const deleteDataObjectViaApi = async (id) => {
  try {
    if (!id) {
      console.error('删除数据对象失败: ID为空')
      return false
    }
    
    console.log('准备通过API删除数据对象, ID:', id)
    let apiResponse = null
    try {
      // 使用原始API路径
      apiResponse = await axiosInstance.delete(`/${id}`)
      console.log('删除数据对象API响应:', apiResponse)
      
      // 检查HTTP状态码
      if (apiResponse.status === 200 || apiResponse.status === 204) {
        console.log('通过状态码确认数字对象删除成功')
        return true
      }
      
      // 检查响应数据
      if (apiResponse && apiResponse.data) {
        // 支持多种成功响应格式
        if (apiResponse.data.code === 200 || 
            apiResponse.data.code === 0 || 
            apiResponse.data.success === true || 
            apiResponse.data.status === 'success') {
          console.log('通过响应数据确认数字对象删除成功')
          return true
        }
      }
      
      return false
    } catch (apiError) {
      console.error('API调用失败:', apiError)
      return false
    }
  } catch (error) {
    console.error('通过API删除数字对象失败:', error)
    return false
  }
}

// 删除数字对象
const deleteDataObject = (id) => {
  const index = sharedTableData.findIndex(item => compareIds(item.id, id))
  
  if (index !== -1) {
    sharedTableData.splice(index, 1)
    
    // 通知监听器
    notifyListeners()
    
    return true
  }
  
  return false
}

// 更新对象状态
const updateObjectStatus = (id, status, feedback = '') => {
  const index = sharedTableData.findIndex(item => compareIds(item.id, id))
  
  if (index !== -1) {
    sharedTableData[index].status = status
    
    // 如果是已合格或待检验状态，清空反馈意见
    if (status === '已合格' || status === '待检验') {
      sharedTableData[index].feedback = ''
    } else {
      sharedTableData[index].feedback = feedback
    }
    
    // 通知监听器
    notifyListeners()
    
    return true
  }
  
  return false
}

// 辅助函数，用于比较ID，处理字符串和数字类型ID
const compareIds = (id1, id2) => {
  // 如果有任一ID为null或undefined，无法比较
  if (id1 === null || id1 === undefined || id2 === null || id2 === undefined) {
    return false;
  }
  
  // 转换为字符串进行比较
  const str1 = String(id1).trim();
  const str2 = String(id2).trim();
  
  // 完全匹配
  if (str1 === str2) {
    return true;
  }
  
  // 处理UUID格式 (忽略大小写和连字符)
  if (str1.includes('-') || str2.includes('-')) {
    // 去除所有连字符并转为小写比较
    const clean1 = str1.replace(/-/g, '').toLowerCase();
    const clean2 = str2.replace(/-/g, '').toLowerCase();
    
    return clean1 === clean2;
  }
  
  return false;
}

// 获取所有数字对象
const getAllDataObjects = () => {
  // 不输出任何日志
  return sharedTableData
}

// Cookie管理函数
const cookieService = {
  // 设置认证Token
  setAuthToken: (token, days = 7) => {
    Cookies.set('auth_token', token, { expires: days, path: '/' })
    console.log('已设置认证Token Cookie:', token)
    return token
  },
  
  // 获取认证Token
  getAuthToken: () => {
    const token = Cookies.get('auth_token')
    return token
  },
  
  // 清除认证Token
  clearAuthToken: () => {
    Cookies.remove('auth_token')
    console.log('已清除认证Token Cookie')
    return true
  },
  
  // 设置其他Cookie
  setCookie: (name, value, options = {}) => {
    Cookies.set(name, value, { path: '/', ...options })
    console.log(`已设置Cookie: ${name}=${value}`)
    return value
  },
  
  // 获取指定Cookie
  getCookie: (name) => {
    return Cookies.get(name)
  },
  
  // 清除指定Cookie
  removeCookie: (name) => {
    Cookies.remove(name, { path: '/' })
    console.log(`已删除Cookie: ${name}`)
    return true
  }
}

// 添加新方法：获取会话中的临时数据对象
const fetchTempDataObject = async (uploadData = null, retryCount = 3, retryDelay = 500) => {
  try {
    // 用于存储每次尝试的响应，便于调试
    const attempts = [];
    
    // 重试逻辑
    for (let attempt = 0; attempt < retryCount; attempt++) {
      try {
        // 如果不是第一次尝试，添加延迟
        if (attempt > 0) {
          await new Promise(resolve => setTimeout(resolve, retryDelay));
        }
        
        // 准备请求参数
        let url = `${API_URL}/objects/temp`;
        const config = {};
        
        // 如果有上传数据，尝试添加到请求参数中
        if (uploadData) {
          // 尝试不同方式传递识别信息
          if (typeof uploadData === 'string') {
            // 如果是字符串，可能是ID
            url += `?id=${encodeURIComponent(uploadData)}`;
          } else if (typeof uploadData === 'object') {
            // 如果是对象，检查是否有ID字段
            if (uploadData.id) {
              url += `?id=${encodeURIComponent(uploadData.id)}`;
            }
            
            // 添加可能需要的额外参数
            if (uploadData.timestamp) {
              url += (url.includes('?') ? '&' : '?') + `timestamp=${uploadData.timestamp}`;
            }
          }
          
          // 添加withCredentials确保发送cookie
          config.withCredentials = true;
        }
        
        // 发送请求
        const response = await axiosInstance.get(url, config);
        
        // 记录尝试结果
        attempts.push({
          attempt: attempt + 1,
          status: response.status,
          hasData: !!response.data
        });
        
        // 检查响应
        if (response.data && response.status === 200) {
          return {
            success: true,
            data: response.data,
            attempts
          };
        }
      } catch (attemptError) {
        console.warn(`第 ${attempt + 1} 次获取临时对象失败:`, attemptError.message);
        attempts.push({
          attempt: attempt + 1,
          error: attemptError.message
        });
      }
    }
    
    // 所有尝试都失败
    console.warn('获取临时数据对象失败，已尝试多次:', attempts);
    return {
      success: false,
      message: '无法获取临时数据对象，多次尝试后失败',
      attempts
    };
  } catch (error) {
    console.error('获取临时数据对象时出错:', error);
    return {
      success: false,
      message: error.message || '获取临时数据对象时发生异常',
      error: error
    };
  }
};

// 添加上传Excel文件的方法
const uploadExcelFile = async (file) => {
  try {
    console.log('开始上传Excel文件...');
    
    // 创建FormData对象
    const formData = new FormData();
    formData.append('file', file);
    
    // 发送请求
    const response = await axiosInstance.post(`${API_URL}/objects/excel`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      withCredentials: true // 确保发送cookie
    });
    
    // 处理响应
    if (response.status === 200) {
      console.log('Excel上传成功，正在获取临时对象...');
      
      try {
        // 添加时间戳，用于标识会话和调试
        const timestamp = Date.now();
        
        // 构建用于识别的数据
        let identifierData = {
          timestamp,
          originalResponse: response.data
        };
        
        // 尝试从响应中提取ID
        if (response.data && typeof response.data === 'object' && response.data.id) {
          identifierData.id = response.data.id;
        } else if (response.data && typeof response.data === 'string') {
          // 尝试从字符串响应中提取ID
          const idMatch = response.data.match(/id[":=\s]+([^",:}\s]+)/i);
          if (idMatch && idMatch[1]) {
            identifierData.id = idMatch[1];
          }
        }
        
        // 获取会话中的临时对象，传递标识数据
        const tempResult = await fetchTempDataObject(identifierData);
        
        if (tempResult.success) {
          // 将临时对象与上传响应合并
          const mergedData = {
            ...tempResult.data,
            uploadResponse: response.data,
            timestamp
          };
          
          // 确保有excelFileId
          if (!mergedData.excelFileId && tempResult.data.id) {
            mergedData.excelFileId = tempResult.data.id;
          }
          
          return {
            success: true,
            data: mergedData,
            originalUploadResponse: response.data
          };
        } else {
          // 如果获取临时对象失败，但上传成功，也返回成功状态和上传响应
          console.warn('Excel上传成功但获取临时对象失败，返回上传响应');
          
          // 尝试从上传响应中提取ID
          let excelFileId = `upload-${timestamp}`;
          if (response.data && typeof response.data === 'object' && response.data.id) {
            excelFileId = response.data.id;
          }
          
          return {
            success: true,
            message: '上传成功但无法获取临时对象，请检查会话状态',
            data: {
              excelFileId,
              originalResponse: response.data,
              timestamp,
              attempts: tempResult.attempts
            },
            originalUploadResponse: response.data
          };
        }
      } catch (tempError) {
        console.error('获取临时对象时出错:', tempError);
        // 上传成功但获取临时对象异常，也返回成功状态
        return {
          success: true,
          message: '上传成功但获取临时对象时出错',
          error: tempError,
          data: {
            excelFileId: `upload-${Date.now()}`,
            uploadResponse: response.data
          },
          originalUploadResponse: response.data
        };
      }
    } else {
      console.warn('Excel上传失败:', response);
      return {
        success: false,
        message: '上传Excel文件失败',
        error: response,
        originalUploadResponse: response.data
      };
    }
  } catch (error) {
    console.error('上传Excel文件时出错:', error);
    return {
      success: false,
      message: error.message || '上传Excel文件时发生异常',
      error: error
    };
  }
};

// 通过API更新对象状态
const updateObjectStatusViaApi = async (id, status, feedback = '', localModeOnly = false) => {
  try {
    if (!id) {
      console.error('更新对象状态失败: ID为空')
      return false
    }
    
    // 如果启用本地模式，只更新本地数据
    if (localModeOnly) {
      console.log(`本地模式：只在本地更新对象状态, ID: ${id}, 状态: ${status}, 反馈: ${feedback}`)
      const result = updateObjectStatus(id, status, feedback)
      return result
    }
    
    // 获取CSRF令牌
    let token = ''
    try {
      token = await prepareCsrfToken();
      console.log('获取到CSRF令牌:', token ? '成功' : '失败');
    } catch (csrfError) {
      console.warn('获取CSRF令牌失败，将继续尝试不带令牌的请求:', csrfError)
    }
    
    console.log(`准备通过API更新对象状态, ID: ${id}, 状态: ${status}, 反馈: ${feedback}`)
    
    // 首先获取当前对象信息
    const currentObject = sharedTableData.find(item => compareIds(item.id, id));
    
    if (!currentObject) {
      console.error(`找不到ID为${id}的对象，无法更新状态`);
      return false;
    }

    // 更新当前对象的状态和反馈
    const updatedObject = { ...currentObject, status, feedback };
    
    // 使用完整的数据格式
    const backendData = transformToBackendFormat(updatedObject);
    
    // 确保状态和反馈在正确的位置
    if (backendData.dataEntity) {
      backendData.dataEntity.status = status;
      backendData.dataEntity.feedback = feedback || '';
    }
    
    // 记录发送的完整数据以便调试
    console.log('发送到后端的完整数据:', JSON.stringify(backendData));
    
    // 设置请求头
    const headers = {
      'Content-Type': 'application/json'
    };
    
    // 如果有令牌，添加到请求头（尝试多种常见的CSRF头名称）
    if (token) {
      headers['X-CSRF-TOKEN'] = token;
      headers['CSRF-Token'] = token;
      headers['X-XSRF-TOKEN'] = token;
      headers['X-CSRF'] = token;
    }
    
    try {
      // 确保使用正确的API完整路径
      const apiUrl = `${API_URL}/objects/${id}`;
      console.log('使用API端点:', apiUrl);
      
      // 发送PUT请求
      const response = await axiosInstance.put(apiUrl, backendData, {
        headers,
        withCredentials: true // 确保发送cookie
      });
      
      console.log('更新对象状态API响应:', response)
      
      // 检查响应状态
      if (response.status === 200 || response.status === 204) {
        console.log('对象状态更新成功')
        
        // 同时更新本地数据
        updateObjectStatus(id, status, feedback)
        
        return true
      }
      
      // 处理返回的数据格式
      if (response && response.data) {
        if (response.data.code === 200 || response.data.success) {
          console.log('对象状态更新成功')
          
          // 同时更新本地数据
          updateObjectStatus(id, status, feedback)
          
          return true
        }
      }
      
      console.warn('API返回了非预期的响应格式:', response)
    } catch (apiError) {
      console.error('API调用失败:', apiError)
      
      // 尝试备用端点 - 不同的API路径格式
      try {
        console.log('尝试备用API端点...');
        // 尝试不同的API路径格式
        const backupUrls = [
          `/api/objects/${id}`,         // 带api前缀
          `/api/object/${id}`,          // 单数形式
          `/objects/status/${id}`,      // 专用状态更新端点
          `/object/status/${id}`        // 另一种可能的专用端点
        ];
        
        for (const backupUrl of backupUrls) {
          try {
            console.log(`尝试备用端点: ${backupUrl}`);
            const backupResponse = await axiosInstance.put(backupUrl, backendData, {
              headers,
              withCredentials: true
            });
            
            if (backupResponse.status === 200 || backupResponse.status === 204 || 
                (backupResponse.data && (backupResponse.data.code === 200 || backupResponse.data.success))) {
              console.log(`备用端点 ${backupUrl} 成功更新了对象状态`);
              updateObjectStatus(id, status, feedback);
              return true;
            }
          } catch (urlError) {
            console.warn(`备用端点 ${backupUrl} 失败:`, urlError.message);
          }
        }
        
        // 最后尝试使用简化数据结构
        const simpleData = { 
          id: id,
          status: status, 
          feedback: feedback || '' 
        };
        
        console.log('尝试使用简化数据结构:', simpleData);
        const simpleResponse = await axiosInstance.put(`${API_URL}/objects/${id}`, simpleData, {
          headers,
          withCredentials: true
        });
        
        if (simpleResponse.status === 200 || simpleResponse.status === 204 || 
            (simpleResponse.data && (simpleResponse.data.code === 200 || simpleResponse.data.success))) {
          console.log('使用简化数据结构成功更新了对象状态');
          updateObjectStatus(id, status, feedback);
          return true;
        }
      } catch (backupError) {
        console.error('所有备用请求都失败:', backupError.message);
      }
      
      // 所有API请求失败，仍继续更新本地数据
    }
    
    // 即使API调用失败，我们仍然更新本地数据以保持UI一致性
    console.log('API调用可能失败，回退到本地更新')
    updateObjectStatus(id, status, feedback)
    
    // 在本地模式回退情况下，返回true以表示本地更新成功
    return true
  } catch (error) {
    console.error('通过API更新对象状态失败:', error)
    
    // 提供更详细的错误信息
    const errorDetails = error.response 
      ? `错误状态: ${error.response.status}, 消息: ${error.response.statusText || '未知错误'}`
      : error.message || '网络错误';
    console.error(`详细错误信息: ${errorDetails}`);
    
    // 虽然API调用失败，我们仍然更新本地数据以保持UI一致性
    updateObjectStatus(id, status, feedback)
    
    // 在本地模式回退情况下，返回true以表示本地更新成功
    return true
  }
}

// 提取反馈信息
const extractFeedback = (backendItem) => {
  try {
    // 1. 首先检查直接的feedback字段
    if (backendItem.feedback) {
      return backendItem.feedback;
    }
    
    // 2. 检查dataEntity中的feedback
    if (backendItem.dataEntity && backendItem.dataEntity.feedback) {
      return backendItem.dataEntity.feedback;
    }
    
    // 3. 检查statusInfo中的feedback
    if (backendItem.statusInfo && backendItem.statusInfo.feedback) {
      return backendItem.statusInfo.feedback;
    }
    
    // 4. 解析dataContent字段（如果它是JSON字符串）
    if (backendItem.dataContent && typeof backendItem.dataContent === 'string') {
      try {
        const dataContentObj = JSON.parse(backendItem.dataContent);
        if (dataContentObj && dataContentObj.feedback) {
          return dataContentObj.feedback;
        }
        
        // 检查dataContentObj.data中是否有feedback
        if (dataContentObj.data && dataContentObj.data.feedback) {
          return dataContentObj.data.feedback;
        }
      } catch (jsonError) {
        // JSON解析失败，尝试使用正则表达式提取
        const feedbackMatch = backendItem.dataContent.match(/"feedback"\s*:\s*"([^"]*)"/);
        if (feedbackMatch && feedbackMatch[1]) {
          return feedbackMatch[1];
        }
      }
    }
    
    // 5. 如果是不合格状态且没有反馈，设置默认反馈
    if (extractStatus(backendItem) === '不合格') {
      return '数据格式不符合要求';
    }
    
    return '';
  } catch (error) {
    return '';
  }
}

// 同步数据对象
const syncDataObjects = (dataObjects) => {
  try {
    // 更新内部数据
    _dataObjects = [...dataObjects];
    
    // 通知监听器数据已更改
    notifyListeners();
    
    // 尝试保存到本地存储
    localStorage.setItem('dataObjects', JSON.stringify(_dataObjects));
    
    // 遍历所有数据对象，确保分类分级值和库表行列分级值已正确保存
    for (let i = 0; i < _dataObjects.length; i++) {
      const obj = _dataObjects[i];
      
      // 如果有API接口，可以尝试调用API更新数据
      // 此处只是示例，实际实现可能需要根据后端API调整
      if (axiosInstance) {
        try {
          // 构造要发送的数据对象
          const updateData = {
            id: obj.id,
            classificationValue: obj.classificationValue,
            levelValue: obj.levelValue,
            dbGrade: obj.dbGrade,
            tableGrade: obj.tableGrade,
            rowGrades: obj.rowGrades,
            columnGrades: obj.columnGrades
          };
          
          // 异步发送到后端，不等待响应
          axiosInstance.post(`${API_URL}/objects/sync`, updateData)
            .then(response => {
              console.log(`同步对象 ${obj.id} 成功:`, response.data);
            })
            .catch(error => {
              console.error(`同步对象 ${obj.id} 出错:`, error);
            });
        } catch (apiError) {
          console.error('调用API同步数据失败:', apiError);
        }
      }
    }
    
    return true;
  } catch (error) {
    console.error('同步数据对象失败:', error);
    return false;
  }
}

// 更新所有数据对象
const updateDataObjects = (newDataObjects) => {
  if (!newDataObjects || !Array.isArray(newDataObjects)) {
    console.error('更新数据对象失败：提供的数据不是数组')
    return false
  }
  
  try {
    // 清空当前数据
    sharedTableData.splice(0, sharedTableData.length)
    
    // 添加新数据（经过适配处理）
    newDataObjects.forEach(item => {
      // 适配后端数据到前端格式
      const adaptedItem = adaptBackendData(item)
      sharedTableData.push(adaptedItem)
    })
    
    // 通知所有监听器数据已变化
    notifyListeners()
    return true
  } catch (error) {
    console.error('更新数据对象时出错:', error)
    return false
  }
}

// 导出模块
export default {
  getAllDataObjects,
  fetchDataObjectsFromBackend,
  fetchDataObjectById,
  getLastReceivedApiData,
  addDataObject,
  updateDataObject,
  deleteDataObject,
  updateObjectStatus,
  addChangeListener,
  removeChangeListener,
  fetchTempDataObject,
  uploadExcelFile,
  updateObjectStatusViaApi,
  syncDataObjects,
  updateDataObjects, // 添加新方法到导出对象
  updateDataObjectViaApi, // 添加API更新方法
  addDataObjectViaApi,
  prepareCsrfToken,
  getCsrfToken,
  syncDataObjects,  fetchDataObjectsFromBackend,
  getLastReceivedApiData,
  fetchDataObjectById,
  updateDataObjectViaApi,
  addDataObjectViaApi,
  deleteDataObjectViaApi,
  compareIds,
  cookieService,  
  updateObjectStatusViaApi,
  getAllDataObjects

} 
