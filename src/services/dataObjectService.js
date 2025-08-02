import { reactive, ref } from 'vue'
import axios from 'axios'
import { API_URL, axiosInstance } from './apiConfig'
import Cookies from 'js-cookie'


let csrfToken = null;

const sharedTableData = reactive([])

const changeListeners = []


const addChangeListener = (callback) => {
  changeListeners.push(callback)
}

const removeChangeListener = (callback) => {
  const index = changeListeners.indexOf(callback)
  if (index !== -1) {
    changeListeners.splice(index, 1)
  }
}

const notifyListeners = () => {
  changeListeners.forEach(callback => callback(sharedTableData))
}

let lastReceivedApiData = null

const fetchDataObjectById = async (id) => {
  try {
    let dataObject = null
    try {
      const response = await axiosInstance.get(`/objects/${id}`)
      

      if (response && response.data) {

        if (response.data.code === 200 && response.data.data) {
          dataObject = response.data.data
        }
 
        else if (response.data.code !== undefined && response.data.data) {
          dataObject = response.data
        }

        else if (response.data && !Array.isArray(response.data)) {
          dataObject = response.data
        }
      }
    } catch (apiError) {

    }
    

    if (!dataObject) {
      dataObject = sharedTableData.find(item => compareIds(item.id, id))
      
      if (dataObject) {
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

const fetchDataObjectsFromBackend = async () => {
  try {
    const response = await axiosInstance.get(`/objects/list1`)
    
 
    lastReceivedApiData = response.data

    if (response && response.data) {

      let dataArray = []
      
      if (response.data.code === 200 && Array.isArray(response.data.data)) {
        dataArray = response.data.data
      } 

      else if (Array.isArray(response.data)) {
        dataArray = response.data
      }

      else if (response.data.data && Array.isArray(response.data.data)) {
        dataArray = response.data.data
      }

      else {
        for (const key in response.data) {
          if (Array.isArray(response.data[key])) {
            dataArray = response.data[key]
            break
          }
        }
      }
      
      // 获取当前登录用户名，进行数据过滤
      const currentUsername = localStorage.getItem('username')
      if (currentUsername && dataArray.length > 0) {
        // 根据creatorName字段过滤数据，只显示当前用户创建的数据
        dataArray = dataArray.filter(item => {
          return item.creatorName === currentUsername
        })
        console.log(`数据过滤完成，当前用户: ${currentUsername}，过滤后数据数量: ${dataArray.length}`)
      }

      if (dataArray.length > 0) {
        sharedTableData.splice(0, sharedTableData.length)
        dataArray.forEach(item => {

          const adaptedItem = adaptBackendData(item)
          sharedTableData.push(adaptedItem)
        })
        
        // 通知监听器
        notifyListeners()
        return sharedTableData
      } else {
        // 没有数据时清空表格，显示NoData
        sharedTableData.splice(0, sharedTableData.length)
        notifyListeners()
        return []
      }
    } else {
      // 响应无效时清空表格，显示NoData
      sharedTableData.splice(0, sharedTableData.length)
      notifyListeners()
      return []
    }
  } catch (error) {
    console.error('API请求错误:', error.message || '未知错误')
    // API请求失败时清空表格，显示NoData，不使用模拟数据
    sharedTableData.splice(0, sharedTableData.length)
    notifyListeners()
    return []
  }
}

const getLastReceivedApiData = () => {
  return lastReceivedApiData
}

const adaptBackendData = (backendItem) => {
  if (!backendItem) {

    console.warn('adaptBackendData: 输入为空，返回默认对象')
    return createDefaultDataObject()
  }
  
  let parsedLocation = null
  if (backendItem.locationInfoJson) {
    try {
      parsedLocation = JSON.parse(backendItem.locationInfoJson)
    } catch (error) {

    }
  }

  const constraintArray = extractConstraintArray(backendItem)
  const transferControlArray = extractTransferControlArray(backendItem)
  const auditInfo = extractAuditInfo(backendItem)  
  // 保留原始locationInfo对象
  let locationInfo = backendItem.locationInfo;
  
  // 如果locationInfo不是对象，尝试从其他属性获取
  if (typeof locationInfo !== 'object' || locationInfo === null) {
    if (parsedLocation) {
      locationInfo = parsedLocation;
    } else if (backendItem.locationInfoJson) {
      try {
        locationInfo = JSON.parse(backendItem.locationInfoJson);
      } catch (e) {
        // 解析失败
      }
    }
    // 如果还是没有locationInfo对象，则使用extractLocationInfo生成格式化字符串
    if (typeof locationInfo !== 'object' || locationInfo === null) {
      locationInfo = extractLocationInfo(backendItem);
    }
  }

  const feedback = extractFeedback(backendItem)

  let metadata = null

  if (backendItem.metadata && typeof backendItem.metadata === 'object') {
    metadata = { ...backendItem.metadata };
  }

  if (!metadata && backendItem.originalMetadata && typeof backendItem.originalMetadata === 'object') {
    metadata = { ...backendItem.originalMetadata }
  }

  if (!metadata && backendItem.metadataJson) {
    try {
      let parsedMetadata = null;
      
      if (typeof backendItem.metadataJson === 'string') {
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
        
        if (!jsonStr.startsWith('{')) jsonStr = '{' + jsonStr;
        if (!jsonStr.endsWith('}')) jsonStr = jsonStr + '}';
        

        parsedMetadata = JSON.parse(jsonStr);
      } else if (typeof backendItem.metadataJson === 'object') {

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
    }
  }

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
    originalMetadata: backendItem.originalMetadata || metadata,
    transferControl: transferControlArray,
    propagationControl: extractConstraintData(backendItem).propagationControl || {},
    auditInfo: auditInfo,
    status: extractStatus(backendItem),
    feedback: feedback,
    formatConstraint: extractConstraintData(backendItem).formatConstraint || '',
    accessConstraint: extractConstraintData(backendItem).accessConstraint || '',
    pathConstraint: extractConstraintData(backendItem).pathConstraint || '',
    regionConstraint: extractConstraintData(backendItem).regionConstraint || '',
    shareConstraint: extractConstraintData(backendItem).shareConstraint || '',
    excelData: backendItem.excelData || null,
    dataItems: backendItem.dataItems || [],
    classificationValue: backendItem.classificationValue || '',
    industryCategory: backendItem.industryCategory || '',
    dataTimeliness: backendItem.dataTimeliness || '',
    dataSource: backendItem.dataSource || '',
    levelValue: backendItem.levelValue || '',
    totalCategoryValue: backendItem.totalCategoryValue || '',
    totalGradeValue: backendItem.totalGradeValue || '',
    dbGrade: backendItem.dbGrade !== undefined ? backendItem.dbGrade : 0,
    tableGrade: backendItem.tableGrade !== undefined ? backendItem.tableGrade : 0,
    rowGrades: backendItem.rowGrades || [0, 0],
    columnGrades: backendItem.columnGrades || [0, 0]
  };
  Object.keys(backendItem).forEach(key => {
    if (!(key in result)) {
      result[key] = backendItem[key];
    }
  });

  return result;
}

// 提取实体名称
const extractEntityName = (backendItem) => {

  if (backendItem.dataEntity && backendItem.dataEntity.entity) {
    return backendItem.dataEntity.entity
  }

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
  if (backendItem.id || backendItem.numericId) {
    const id = backendItem.id || backendItem.numericId
    if (typeof id === 'number' || !isNaN(Number(id))) {
      const numId = Number(id)
      if (numId < 100) {
        return `表${numId < 10 ? '0' + numId : numId}`
      }
    }
    return `表${id}`
  }

  return getValidValue(backendItem.entity, '未命名对象')
}

// 提取位置信息
const extractLocationInfo = (backendItem) => {
  if (typeof backendItem.locationInfo === 'string') {
    return backendItem.locationInfo
  }
  
  if (backendItem.locationInfoJson) {
    try {
      const locationInfoObj = JSON.parse(backendItem.locationInfoJson)
      const entity = extractEntityName(backendItem)
  
      if (Array.isArray(locationInfoObj.locations) && locationInfoObj.locations.length > 0) {
        const location = locationInfoObj.locations[0]

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
  
  if (backendItem.locationInfo && typeof backendItem.locationInfo === 'object') {
    const entity = extractEntityName(backendItem)
    

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

  if (backendItem.dataEntity) {
    const entity = backendItem.dataEntity.entity || '默认表'
    return `(${entity}, -, -)`
  }
  
  return '(-, -, -)'
}

// 从后端数据中提取约束条件数组
const extractConstraintArray = (backendItem) => {
  const constraintArray = []
  if (Array.isArray(backendItem.constraint)) {
    return backendItem.constraint
  }
  
  if (backendItem.constraintSet) {
    let constraint = backendItem.constraintSet.selectedConstraint
    if (!constraint && Array.isArray(backendItem.constraintSet.constraints) && 
        backendItem.constraintSet.constraints.length > 0) {
      constraint = backendItem.constraintSet.constraints[0]
    }

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

  if (backendItem.formatConstraint) result.formatConstraint = backendItem.formatConstraint
  if (backendItem.accessConstraint) result.accessConstraint = backendItem.accessConstraint
  if (backendItem.pathConstraint) result.pathConstraint = backendItem.pathConstraint
  if (backendItem.regionConstraint) result.regionConstraint = backendItem.regionConstraint
  if (backendItem.shareConstraint) result.shareConstraint = backendItem.shareConstraint

  if (backendItem.constraintSet) {

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

    else if (backendItem.constraintSet.selectedConstraint) {
      const constraint = backendItem.constraintSet.selectedConstraint
      if (constraint.formatConstraint) result.formatConstraint = constraint.formatConstraint
      if (constraint.accessConstraint) result.accessConstraint = constraint.accessConstraint
      if (constraint.pathConstraint) result.pathConstraint = constraint.pathConstraint
      if (constraint.regionConstraint) result.regionConstraint = constraint.regionConstraint
      if (constraint.shareConstraint) result.shareConstraint = constraint.shareConstraint
    }
  }
  
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
  

  if (Array.isArray(backendItem.transferControl)) {
    return backendItem.transferControl
  }

  if (backendItem.propagationControlJson) {
    try {

      const propagationControl = typeof backendItem.propagationControlJson === 'string' 
        ? JSON.parse(backendItem.propagationControlJson) 
        : backendItem.propagationControlJson
      

      if (propagationControl && propagationControl.operations) {
        const ops = propagationControl.operations

        if (ops.read === 1) transferControlArray.push('可读')
        if (ops.modify === 1) transferControlArray.push('可修改') 
        if (ops.share === 1) transferControlArray.push('可共享')
        if (ops.delegate === 1) transferControlArray.push('可委托')
        if (ops.destroy === 1) transferControlArray.push('可销毁')
      }
      

      if (transferControlArray.length > 0) {
        return transferControlArray
      }
    } catch (error) {

    }
  }

  if (backendItem.propagationControl) {
    const control = backendItem.propagationControl;

    if (backendItem.propagationControl.operations) {
      const ops = backendItem.propagationControl.operations
      if (ops.read === 1) transferControlArray.push('可读')
      if (ops.modify === 1) transferControlArray.push('可修改')
      if (ops.share === 1) transferControlArray.push('可共享')
      if (ops.delegate === 1) transferControlArray.push('可委托')
      if (ops.destroy === 1) transferControlArray.push('可销毁')
    }

    else {
      if (control.canRead === true) transferControlArray.push('可读')
      if (control.canModify === true) transferControlArray.push('可修改')
      if (control.canShare === true) transferControlArray.push('可共享')
      if (control.canDelegate === true) transferControlArray.push('可委托')
      if (control.canDestroy === true) transferControlArray.push('可销毁')
    }

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

  return '查看日志'
}

// 提取状态信息
const extractStatus = (backendItem) => {
  if (backendItem.dataEntity && backendItem.dataEntity.status) {
    return backendItem.dataEntity.status
  }

  return getValidValue(backendItem.status, '待生成分类分级值')
}

// 获取有效值，如果第一个值无效则使用后备值
const getValidValue = (value, ...fallbacks) => {
  if (value !== undefined && value !== null) {
    return value
  }
  for (const fallback of fallbacks) {
    if (fallback !== undefined && fallback !== null) {
      return fallback
    }
  }
  
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
    status: '待生成分类分级值',
    feedback: '',
    excelData: null,
    formatConstraint: '',
    accessConstraint: '',
    pathConstraint: '',
    regionConstraint: '',
    shareConstraint: ''
  }
}


const transformToBackendFormat = (frontendData) => {
  const userMetadata = frontendData.originalMetadata || frontendData.metadata || {};

  const dataEntity = {
    entity: frontendData.entity || '',
    status: frontendData.status || '待生成分类分级值', 
    feedback: frontendData.feedback || '',

    metadata: {
      dataName: userMetadata.dataName || frontendData.entity || '',
      sourceUnit: userMetadata.sourceUnit || '',
      contactPerson: userMetadata.contactPerson || '',
      contactPhone: userMetadata.contactPhone || '',
      resourceSummary: userMetadata.resourceSummary || '',
      fieldClassification: userMetadata.fieldClassification || '',
      headers: userMetadata.headers || []
    },
    _userMetadataProcessed: true,
    originalMetadata: { ...userMetadata },
    dataItems: frontendData.dataItems || []
  }
  

  const metadataJsonObj = {
    dataName: userMetadata.dataName || frontendData.entity || '',
    sourceUnit: userMetadata.sourceUnit || '',
    contactPerson: userMetadata.contactPerson || '',
    contactPhone: userMetadata.contactPhone || '',
    resourceSummary: userMetadata.resourceSummary || '',
    fieldClassification: userMetadata.fieldClassification || '',
    headers: userMetadata.headers || []
  };
  

  dataEntity.metadataJson = JSON.stringify(metadataJsonObj);


  if (frontendData.excelFileId) {
    dataEntity.excelFileId = frontendData.excelFileId;
  }


  let locationInfo = frontendData.locationInfo;

  if (
    locationInfo &&
    (locationInfo.databaseName || locationInfo.tableName || locationInfo.selectFields)
  ) {

  } else {

    locationInfo = {
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
  }


  const constraintSet = {
    constraints: [
      {
        formatConstraint: frontendData.formatConstraint !== undefined ? frontendData.formatConstraint : "xlsx",
        accessConstraint: frontendData.accessConstraint !== undefined ? frontendData.accessConstraint : "全部允许",
        pathConstraint: frontendData.pathConstraint !== undefined ? frontendData.pathConstraint : "点对点",
        regionConstraint: frontendData.regionConstraint !== undefined ? frontendData.regionConstraint : "内网",
        shareConstraint: frontendData.shareConstraint !== undefined ? frontendData.shareConstraint : "允许共享"
      }
    ]
  };


  let propagationControl;
  if (frontendData.propagationControl) {
    propagationControl = JSON.parse(JSON.stringify(frontendData.propagationControl));
  } else {

    const hasRead = frontendData.transferControl && frontendData.transferControl.includes("可读");
    const hasModify = frontendData.transferControl && frontendData.transferControl.includes("可修改");
    const hasShare = frontendData.transferControl && frontendData.transferControl.includes("可共享");
    const hasDelegate = frontendData.transferControl && frontendData.transferControl.includes("可委托");
    const hasDestroy = frontendData.transferControl && frontendData.transferControl.includes("可销毁");
    propagationControl = {
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
  }

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
    metadata: dataEntity.metadata,
    metadataJson: dataEntity.metadataJson,
    
    totalCategoryValue: frontendData.totalCategoryValue || frontendData.classificationValue || '',
    totalGradeValue: frontendData.totalGradeValue || frontendData.levelValue || '',
    dbGrade: frontendData.dbGrade,
    tableGrade: frontendData.tableGrade,
    rowGrades: frontendData.rowGrades,
    columnGrades: frontendData.columnGrades,

    dataItems: frontendData.dataItems || [],
    
    // 添加creatorName字段
    creatorName: frontendData.creatorName
  }

  return result;
}

// 获取CSRF令牌的函数
const getCsrfToken = () => {

  if (csrfToken) {
    return csrfToken;
  }

  const tokenFromCookie = Cookies.get('XSRF-TOKEN') || Cookies.get('csrf_token');
  if (tokenFromCookie) {
    csrfToken = tokenFromCookie;
    return tokenFromCookie;
  }

  return '';
};


const prepareCsrfToken = async () => {
  try {

    const existingToken = getCsrfToken();
    if (existingToken) {
      return existingToken;
    }
    

    try {

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

      try {
        const backupResponse = await axiosInstance.get('/security/csrf');
        if (backupResponse.data && (backupResponse.data.token || typeof backupResponse.data === 'string')) {
          const token = backupResponse.data.token || backupResponse.data;
          cookieService.setCookie('XSRF-TOKEN', token);
          csrfToken = token;
          return token;
        }
      } catch (backupError) {

      }
    }
    
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

    }

    const randomToken = Math.random().toString(36).substring(2, 15);
    cookieService.setCookie('XSRF-TOKEN', randomToken);
    csrfToken = randomToken;
    return randomToken;
  } catch (error) {

    const fallbackToken = Cookies.get('XSRF-TOKEN') || Cookies.get('csrf_token') || '';
    

    if (fallbackToken) {
      csrfToken = fallbackToken;
    }
    
    return fallbackToken;
  }
};


const addDataObjectViaApi = async (dataObject, extraParams = {}) => {
  try {

    const token = await prepareCsrfToken();

    // 添加creatorName字段，设置为当前登录用户
    const currentUsername = localStorage.getItem('username')
    if (currentUsername && !dataObject.creatorName) {
      dataObject.creatorName = currentUsername
      console.log(`为新建数据对象设置创建者: ${currentUsername}`)
    }

    if (!dataObject.originalMetadata && dataObject.metadata) {
      dataObject.originalMetadata = { ...dataObject.metadata };
    }

    if (!dataObject.metadata && !dataObject.originalMetadata) {

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
    

    const requestData = transformToBackendFormat(dataObject);

    if (dataObject.originalMetadata) {
      if (!requestData.dataEntity.originalMetadata) {
        requestData.dataEntity.originalMetadata = { ...dataObject.originalMetadata };
      }
    }
    

    if (requestData.dataEntity && requestData.dataEntity.metadataJson) {

      requestData.metadataJson = requestData.dataEntity.metadataJson;
    } else if (dataObject.metadata) {

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

    if (!requestData.metadata && requestData.dataEntity && requestData.dataEntity.metadata) {
      requestData.metadata = requestData.dataEntity.metadata;
    } else if (!requestData.metadata && dataObject.metadata) {
      requestData.metadata = { ...dataObject.metadata };
    }
    
    // 添加额外的请求参数
    if (extraParams && Object.keys(extraParams).length > 0) {
      Object.assign(requestData, extraParams);

      if (extraParams.preserveUserMetadata) {
        requestData.dataEntity._preserveUserMetadata = true;
      }
    }

    if (dataObject.excelFileId && !requestData.dataEntity.excelFileId) {
      requestData.dataEntity.excelFileId = dataObject.excelFileId;
    }

    let url = `${API_URL}/objects`;
    if (dataObject.excelFileId) {
      url += `?excelFileId=${encodeURIComponent(dataObject.excelFileId)}`;
    }

    const headers = {
      'Content-Type': 'application/json'
    };

    if (token) {
      headers['X-CSRF-TOKEN'] = token;
    }

    const response = await axiosInstance.post(url, requestData, {
      headers,
      withCredentials: true 
    });
    

    if (response.status === 200 || response.status === 201) {

      let responseData = response.data;
      

      if (responseData && typeof responseData === 'string') {
        try {
          responseData = JSON.parse(responseData);
        } catch (e) {

          responseData = {
            message: responseData,
            success: true,
            dataEntity: {
              entity: dataObject.entity
            }
          };
        }
      }
      

      if (responseData && 
          (responseData.code === 1 || responseData.code === 200) && 
          responseData.msg === 'success' && 
          !responseData.metadata && 
          !responseData.dataEntity) {
        

        responseData.originalMetadata = dataObject.originalMetadata || dataObject.metadata;
        

        if (!responseData.dataEntity) {
          responseData.dataEntity = {
            entity: dataObject.entity,
            metadata: dataObject.metadata
          };
        }
      }
      
      if (responseData) {

        if (dataObject.originalMetadata) {
          responseData.originalMetadata = { ...dataObject.originalMetadata };
        }
      }
      

      const newObject = adaptBackendData(responseData);
      

      if (dataObject.originalMetadata && newObject) {
        if (!newObject.metadata || 
            (newObject.metadata.dataName !== dataObject.originalMetadata.dataName) ||
            (newObject.metadata.sourceUnit !== dataObject.originalMetadata.sourceUnit) ||
            (newObject.metadata.contactPerson !== dataObject.originalMetadata.contactPerson)) {

          newObject.metadata = { ...dataObject.originalMetadata };
        }
        

        newObject.originalMetadata = { ...dataObject.originalMetadata };
      }
      

      sharedTableData.push(newObject);

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
      console.error('更新数据对象失败: ID或数据对象为空')
      return false
    }
    

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
    }

    if (!dataObject.dataItems || dataObject.dataItems.length === 0) {
      if (originalDataItems.length > 0) {
        dataObject.dataItems = originalDataItems;
      }
    }

    const token = await prepareCsrfToken();

    if (dataObject.locationInfoInput && typeof dataObject.locationInfoInput === 'string') {
      const arr = dataObject.locationInfoInput.split(',').map(s => s.trim())
      dataObject.locationInfo = {
        databaseName: arr[0] || '',
        tableName: arr[1] || '',
        selectFields: arr.length > 2 ? arr.slice(2).join(',') : ''
      }
    }
    
    const backendData = transformToBackendFormat(dataObject)
    
  
    if (!backendData.dataItems || backendData.dataItems.length === 0) {
      if (dataObject.dataItems && dataObject.dataItems.length > 0) {
        backendData.dataItems = dataObject.dataItems;
      } else if (originalDataItems.length > 0) {
        backendData.dataItems = originalDataItems;
      }
    }

    if (backendData.dataEntity && (!backendData.dataEntity.dataItems || backendData.dataEntity.dataItems.length === 0)) {
      if (backendData.dataItems && backendData.dataItems.length > 0) {
        backendData.dataEntity.dataItems = backendData.dataItems;
      } else if (dataObject.dataItems && dataObject.dataItems.length > 0) {
        backendData.dataEntity.dataItems = dataObject.dataItems;
      } else if (originalDataItems.length > 0) {
        backendData.dataEntity.dataItems = originalDataItems;
      }
    }
    

    const headers = {
      'Content-Type': 'application/json'
    };

    if (token) {
      headers['X-CSRF-TOKEN'] = token;
    }
    
    const response = await axiosInstance.put(`/objects/${id}`, backendData, {
      headers,
      withCredentials: true 
    });
    

    if (response.status === 200 || response.status === 204) {

      updateDataObject(dataObject)
      
      return true
    }
    
    // 处理返回的数据格式
    if (response && response.data) {
      if (response.data.code === 200) {

        updateDataObject(dataObject)
        
        return true
      }
    }
    
    return false
  } catch (error) {

    try {
      updateDataObject(dataObject);
    } catch (localError) {
      console.error('本地数据更新也失败');
    }
    
    return false
  }
}

// 添加新的数字对象
const addDataObject = (newObject) => {

  const newId = sharedTableData.length > 0 
    ? Math.max(...sharedTableData.map(item => item.id)) + 1 
    : 1
  
  const objectToAdd = {
    id: newId,
    ...newObject,
    auditInfo: newObject.auditInfo || '查看日志',
    status: newObject.status || '待生成分类分级值',
    feedback: newObject.feedback || ''
  }
  
  sharedTableData.unshift(objectToAdd)

  notifyListeners()
  
  return objectToAdd
}

// 更新数字对象
const updateDataObject = (updatedObject) => {
  const index = sharedTableData.findIndex(item => compareIds(item.id, updatedObject.id))
  
  if (index !== -1) {

    const originalObject = sharedTableData[index]

    if (!updatedObject.dataItems && originalObject.dataItems) {
      updatedObject.dataItems = originalObject.dataItems
    }
    
    sharedTableData[index] = {
      ...originalObject,
      ...updatedObject
    }

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
      apiResponse = await axiosInstance.delete(`/${id}`)
      console.log('删除数据对象API响应:', apiResponse)
      
      if (apiResponse.status === 200 || apiResponse.status === 204) {
        console.log('通过状态码确认数字对象删除成功')
        return true
      }
      

      if (apiResponse && apiResponse.data) {
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
    

    if (status === '已合格' || status === '待检验' || status === '待生成分类分级值') {
      sharedTableData[index].feedback = ''
    } else {
      sharedTableData[index].feedback = feedback
    }

    notifyListeners()
    
    return true
  }
  
  return false
}


const compareIds = (id1, id2) => {

  if (id1 === null || id1 === undefined || id2 === null || id2 === undefined) {
    return false;
  }
  

  const str1 = String(id1).trim();
  const str2 = String(id2).trim();
  

  if (str1 === str2) {
    return true;
  }

  if (str1.includes('-') || str2.includes('-')) {

    const clean1 = str1.replace(/-/g, '').toLowerCase();
    const clean2 = str2.replace(/-/g, '').toLowerCase();
    
    return clean1 === clean2;
  }
  
  return false;
}


const getAllDataObjects = () => {
  return sharedTableData
}

// Cookie管理函数
const cookieService = {

  setAuthToken: (token, days = 7) => {
    Cookies.set('auth_token', token, { expires: days, path: '/' })
    return token
  },
  

  getAuthToken: () => {
    const token = Cookies.get('auth_token')
    return token
  },
  

  clearAuthToken: () => {
    Cookies.remove('auth_token')
    return true
  },
  
  // 设置其他Cookie
  setCookie: (name, value, options = {}) => {
    Cookies.set(name, value, { path: '/', ...options })
    return value
  },
  
 
  getCookie: (name) => {
    return Cookies.get(name)
  },
  

  removeCookie: (name) => {
    Cookies.remove(name, { path: '/' })
    return true
  }
}

// 已删除 fetchTempDataObject 方法，不再调用 /api/objects/temp 接口

// 添加上传Excel文件的方法
const uploadExcelFile = async (file) => {
  try {

    const formData = new FormData();
    formData.append('file', file);
    

    const response = await axiosInstance.post(`${API_URL}/objects/excel`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      withCredentials: true 
    });

    if (response.status === 200) {
      console.log('Excel上传成功，正在获取临时对象...');
      
      try {
        const timestamp = Date.now();
        let identifierData = {
          timestamp,
          originalResponse: response.data
        };
        

        if (response.data && typeof response.data === 'object' && response.data.id) {
          identifierData.id = response.data.id;
        } else if (response.data && typeof response.data === 'string') {

          const idMatch = response.data.match(/id[":=\s]+([^",:}\s]+)/i);
          if (idMatch && idMatch[1]) {
            identifierData.id = idMatch[1];
          }
        }
 
        // 直接返回上传结果，不再调用临时对象接口
        let excelFileId = `upload-${timestamp}`;
        if (response.data && typeof response.data === 'object' && response.data.id) {
          excelFileId = response.data.id;
        }
        
        return {
          success: true,
          data: {
            excelFileId,
            originalResponse: response.data,
            timestamp
          },
          originalUploadResponse: response.data
        };
      } catch (tempError) {
        console.error('获取临时对象时出错:', tempError);

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
    if (localModeOnly) {
      return updateObjectStatus(id, status, feedback)
    }

    let currentObject = sharedTableData.find(item => compareIds(item.id, id));
    if (!currentObject) {
      try {
        const resp = await axios.get(`http://localhost:8081/api/objects/${id}`);
        if (resp.data && resp.data.data) {
          currentObject = resp.data.data;
        } else if (resp.data) {
          currentObject = resp.data;
        }
      } catch (e) {
        console.error('后端也找不到该对象:', e);
        return false;
      }
      if (!currentObject) {
        console.error(`找不到ID为${id}的对象，无法更新状态`);
        return false;
      }
    }

    // 添加调试输出
    console.log('=== 8081端口状态更新调试信息 ===');
    console.log('原始对象数据:', currentObject);
    console.log('原始约束条件字段:', {
      formatConstraint: currentObject.formatConstraint,
      accessConstraint: currentObject.accessConstraint,
      pathConstraint: currentObject.pathConstraint,
      regionConstraint: currentObject.regionConstraint,
      shareConstraint: currentObject.shareConstraint
    });
    
    const updatedObject = JSON.parse(JSON.stringify(currentObject));
    
    // 从constraintSet中提取约束条件字段
    if (currentObject.constraintSet && currentObject.constraintSet.constraints && currentObject.constraintSet.constraints.length > 0) {
      const constraint = currentObject.constraintSet.constraints[0];
      updatedObject.formatConstraint = constraint.formatConstraint;
      updatedObject.accessConstraint = constraint.accessConstraint;
      updatedObject.pathConstraint = constraint.pathConstraint;
      updatedObject.regionConstraint = constraint.regionConstraint;
      updatedObject.shareConstraint = constraint.shareConstraint;
    }
    
    if (updatedObject.dataEntity) {
      updatedObject.entity = updatedObject.dataEntity.entity;
      updatedObject.status = updatedObject.dataEntity.status;
      updatedObject.feedback = updatedObject.dataEntity.feedback;
      updatedObject.metadata = updatedObject.dataEntity.metadata;
      updatedObject.dataItems = updatedObject.dataEntity.dataItems;

      if (currentObject.propagationControl) {
        updatedObject.propagationControl = JSON.parse(JSON.stringify(currentObject.propagationControl));
      }
      if (currentObject.transferControl) {
        updatedObject.transferControl = JSON.parse(JSON.stringify(currentObject.transferControl));
      }
    }
    updatedObject.status = status;
    updatedObject.feedback = feedback;
    
    console.log('更新后的对象数据:', updatedObject);
    console.log('更新后的约束条件字段:', {
      formatConstraint: updatedObject.formatConstraint,
      accessConstraint: updatedObject.accessConstraint,
      pathConstraint: updatedObject.pathConstraint,
      regionConstraint: updatedObject.regionConstraint,
      shareConstraint: updatedObject.shareConstraint
    });

    const backendData = transformToBackendFormat(updatedObject);
    
    console.log('转换为后端格式后的数据:', backendData);
    console.log('后端数据中的约束条件:', backendData.constraintSet);

    try {
      const response = await axios.put(
        `http://localhost:8081/api/objects/${id}`,
        backendData,
        { headers: { 'Content-Type': 'application/json' } }
      );
      if (response.status === 200 || response.status === 204 ||
          (response.data && (response.data.code === 200 || response.data.code === 1 || response.data.success === true))) {
        updateObjectStatus(id, status, feedback)
        return true
      }
      return false
    } catch (error) {
      console.error('通过API更新对象状态失败:', error)
      updateObjectStatus(id, status, feedback)
      return true
    }
  } catch (error) {
    console.error('updateObjectStatusViaApi异常:', error)
    return false
  }
}

// 提取反馈信息
const extractFeedback = (backendItem) => {
  try {
    if (backendItem.feedback) {
      return backendItem.feedback;
    }

    if (backendItem.dataEntity && backendItem.dataEntity.feedback) {
      return backendItem.dataEntity.feedback;
    }
    
    if (backendItem.statusInfo && backendItem.statusInfo.feedback) {
      return backendItem.statusInfo.feedback;
    }
    
    if (backendItem.dataContent && typeof backendItem.dataContent === 'string') {
      try {
        const dataContentObj = JSON.parse(backendItem.dataContent);
        if (dataContentObj && dataContentObj.feedback) {
          return dataContentObj.feedback;
        }
        
        if (dataContentObj.data && dataContentObj.data.feedback) {
          return dataContentObj.data.feedback;
        }
      } catch (jsonError) {
        const feedbackMatch = backendItem.dataContent.match(/"feedback"\s*:\s*"([^"]*)"/);
        if (feedbackMatch && feedbackMatch[1]) {
          return feedbackMatch[1];
        }
      }
    }

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
    sharedTableData.splice(0, sharedTableData.length);

    dataObjects.forEach(item => {
      sharedTableData.push(item);
    });
    
    notifyListeners();

    try {
      localStorage.setItem('dataObjects', JSON.stringify(sharedTableData));
    } catch (storageError) {
      console.warn('本地存储失败');
    }

    for (let i = 0; i < sharedTableData.length; i++) {
    }
    
    return true;
  } catch (error) {
    // 保留关键错误信息
    console.error('同步数据对象失败:', error);
    return false;
  }
}

const updateDataObjects = (newDataObjects) => {
  if (!newDataObjects || !Array.isArray(newDataObjects)) {
    console.error('更新数据对象失败：提供的数据不是数组')
    return false
  }
  
  try {

    sharedTableData.splice(0, sharedTableData.length)
    newDataObjects.forEach(item => {

      const adaptedItem = adaptBackendData(item)
      sharedTableData.push(adaptedItem)
    })

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
  uploadExcelFile,
  updateObjectStatusViaApi,
  syncDataObjects,
  updateDataObjects, 
  updateDataObjectViaApi, 
  addDataObjectViaApi,
  prepareCsrfToken,
  getCsrfToken,
  deleteDataObjectViaApi,
  compareIds,
  cookieService  
}

