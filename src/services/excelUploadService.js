import { ElMessage } from 'element-plus';
import { API_URL, axiosInstance, MOCK_ENABLED, AUTO_FALLBACK_TO_MOCK, testApiConnection } from './apiConfig';
import axios from 'axios';

/**
 * 详细测试与后端API的连接并返回更多信息
 * @returns {Promise<Object>} 连接测试结果
 */
const checkApiConnection = async () => {
  try {
    // 使用导入的testApiConnection进行基础连接测试
    const isConnected = await testApiConnection();
    
    if (isConnected) {
      return {
        success: true,
        message: '与后端API连接正常',
        details: { isConnected }
      };
    } else {
      return {
        success: false,
        message: '与后端API连接失败',
        details: { isConnected }
      };
    }
  } catch (error) {
    console.error('API连接测试失败:', error);
    
    // 详细记录错误信息
    let errorDetails = {
      message: error.message || '未知错误'
    };
    
    if (error.response) {
      errorDetails.status = error.response.status;
      errorDetails.statusText = error.response.statusText;
      errorDetails.data = error.response.data;
    } else if (error.request) {
      errorDetails.request = '已发送请求但未收到响应';
      errorDetails.requestInfo = error.request;
    }
    
    return {
      success: false,
      message: '与后端API连接失败',
      details: errorDetails
    };
  }
};

/**
 * 创建模拟成功响应
 * @param {File} file - 上传的文件
 * @returns {Object} 模拟的成功响应
 */
const createMockSuccessResponse = (file) => {
  console.log('[Mock模式] 创建模拟上传成功响应');
  
  return {
    success: true,
    message: '[模拟] 文件上传成功',
    data: {
      id: Math.floor(Math.random() * 1000),
      filename: file.name,
      size: file.size,
      mimetype: file.type,
      uploadTime: new Date().toISOString(),
      isMock: true
    }
  };
};

/**
 * 上传Excel文件到服务器
 * @param {File} file - 要上传的Excel文件
 * @returns {Promise<Object>} - 包含上传结果的Promise
 */
const uploadExcelFile = async (file) => {
  try {
    // 开始前先测试API连接
    console.log('开始上传Excel文件', file.name, '大小:', file.size);
    console.log('目标URL:', `${API_URL}/objects/excel`);
    
    const apiConnectionTest = await checkApiConnection();
    console.log('API连接测试结果:', apiConnectionTest);
    
    if (!apiConnectionTest.success) {
      console.warn('API连接测试失败，但仍将尝试上传');
    }
    
    const formData = new FormData();
    formData.append('file', file);
    // 添加显式的文件名
    formData.append('filename', file.name);
    // 添加时间戳，避免缓存问题
    formData.append('timestamp', new Date().getTime());
    
    // 显示URL配置
    const apiUrl = `${API_URL}/objects/excel`;
    console.log('正在上传到URL:', apiUrl);
    
    // 使用直接的axios调用而不是封装的实例，以便更精确控制参数
    const response = await axios({
      method: 'POST',
      url: apiUrl,
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data',
        'Accept': 'application/json',
        'X-Requested-With': 'XMLHttpRequest'
      },
      timeout: 60000, // 增加超时时间到60秒
      withCredentials: false, // 确保跨域请求不携带凭证
      maxContentLength: 10 * 1024 * 1024, // 10MB 限制
      maxBodyLength: 10 * 1024 * 1024 // 10MB 限制
    });
    
    console.log('上传响应:', response);
    
    // 检查响应数据，提取有用信息
    let extractedData = {};
    
    if (response.data) {
      // 提取文件ID - 检查多种可能的响应结构
      if (response.data.data && response.data.data.id) {
        extractedData.id = response.data.data.id;
      } else if (response.data.id) {
        extractedData.id = response.data.id;
      } else if (response.data.fileId) {
        extractedData.id = response.data.fileId;
      } else if (response.data.data && response.data.data.fileId) {
        extractedData.id = response.data.data.fileId;
      }
      
      // 提取其他可能有用的信息
      if (response.data.data) {
        if (response.data.data.url) extractedData.url = response.data.data.url;
        if (response.data.data.headers) extractedData.headers = response.data.data.headers;
        if (response.data.data.dataItems) extractedData.dataItems = response.data.data.dataItems;
      }
    }
    
    console.log('从响应中提取的数据:', extractedData);
    
    return {
      success: true,
      data: {
        original: response.data,
        data: extractedData
      },
      message: '上传成功'
    };
  } catch (error) {
    console.error('Excel上传失败，详细错误:', error);
    
    let errorMessage = '上传Excel文件失败';
    let errorDetails = null;
    
    // 检查API_URL是否正确配置
    if (!API_URL || API_URL === 'undefined' || API_URL === 'null') {
      console.error('API_URL配置错误:', API_URL);
      errorMessage = '上传Excel文件失败: API地址未正确配置';
      
      return {
        success: false,
        error: error,
        message: errorMessage,
        details: {
          type: 'CONFIG_ERROR',
          message: 'API_URL未正确配置，请检查apiConfig.js文件'
        },
        shouldTryLocalFallback: true
      };
    }
    
    // 检查并启用MOCK_ENABLED模式进行本地处理
    if (MOCK_ENABLED && AUTO_FALLBACK_TO_MOCK) {
      console.log('[Mock模式] 上传失败，使用模拟数据');
      const mockResponse = createMockSuccessResponse(file);
      
      return {
        success: true,
        message: '[模拟] 文件上传成功(本地处理)',
        data: {
          data: {
            id: mockResponse.data.id,
            isMock: true
          },
          original: mockResponse
        }
      };
    }
    
    if (error.response) {
      // 服务器返回了错误响应
      console.error('服务器错误响应:');
      console.error('- 状态码:', error.response.status);
      
      if (error.response.status === 500) {
        errorMessage = '服务器内部错误';
        
        // 尝试从错误响应中获取更详细的错误信息
        if (error.response.data) {
          if (typeof error.response.data === 'string') {
            // 响应可能是HTML页面或纯文本
            const errorText = error.response.data.substring(0, 200) + '...';
            errorMessage += `: ${errorText}`;
            
            // 尝试从HTML错误页面中提取有用信息
            const match = error.response.data.match(/<h1>(.*?)<\/h1>/i);
            if (match && match[1]) {
              errorMessage = `服务器错误: ${match[1]}`;
            }
          } else if (error.response.data.message) {
            errorMessage += `: ${error.response.data.message}`;
          } else if (error.response.data.error) {
            errorMessage += `: ${error.response.data.error}`;
          }
        }
        
        errorDetails = {
          type: 'SERVER_ERROR',
          status: 500,
          message: '服务器内部错误，请联系管理员检查后端日志'
        };
      } else if (error.response.status === 413) {
        errorMessage = '文件太大，服务器拒绝处理';
        errorDetails = {
          type: 'FILE_TOO_LARGE',
          status: 413,
          message: '请使用小于8MB的文件'
        };
      } else if (error.response.status === 415) {
        errorMessage = '不支持的文件类型';
        errorDetails = {
          type: 'UNSUPPORTED_MEDIA_TYPE',
          status: 415,
          message: '请使用.xlsx或.xls格式文件'
        };
      } else if (error.response.status === 400) {
        errorMessage = '请求参数错误';
        if (error.response.data && error.response.data.message) {
          errorMessage += `: ${error.response.data.message}`;
        }
        errorDetails = {
          type: 'BAD_REQUEST',
          status: 400,
          message: errorMessage
        };
      } else {
        errorMessage = `服务器返回错误(${error.response.status})`;
        if (error.response.data && error.response.data.message) {
          errorMessage += `: ${error.response.data.message}`;
        }
      }
    } else if (error.request) {
      // 请求已发送但没有收到响应
      console.error('无响应错误:');
      console.error('- 请求详情:', error.request);
      
      errorMessage = '上传Excel文件失败，请检查网络连接';
      errorDetails = {
        type: 'NO_RESPONSE',
        message: '未收到服务器响应，请确认后端服务已启动'
      };
      
      // 检查后端URL是否配置正确
      console.log('检查后端URL配置:', API_URL);
      if (API_URL.includes('localhost') || API_URL.includes('127.0.0.1')) {
        console.warn('使用的是本地开发URL:', API_URL);
        errorDetails.message += '。您正在使用本地开发环境地址，请确保后端服务正在本地运行。';
      }
    } else {
      // 请求设置错误
      console.error('请求配置错误:', error.message);
      
      errorMessage = `请求错误: ${error.message}`;
      errorDetails = {
        type: 'REQUEST_ERROR',
        message: error.message
      };
    }
    
    // 尝试检查是否是CORS问题
    if (error.message && error.message.includes('Network Error')) {
      console.warn('可能存在跨域(CORS)问题');
      errorMessage = '网络错误，可能是跨域(CORS)限制导致';
      errorDetails = {
        type: 'CORS_ERROR',
        message: '请确认后端已正确配置CORS'
      };
    }
    
    // 用于尝试本地解析的标记
    const shouldTryLocalFallback = true;
    
    return {
      success: false,
      error: error,
      message: errorMessage,
      details: errorDetails,
      shouldTryLocalFallback
    };
  }
};

/**
 * 测试后端Excel上传接口是否可用
 * @returns {Promise<boolean>} - 接口是否可用
 */
const testExcelUploadEndpoint = async () => {
  try {
    // 创建一个最小的Excel文件模拟
    const smallFile = new File(
      [new Uint8Array([80, 75, 3, 4, 20, 0, 6, 0])], // 简单的Excel文件魔数
      'test.xlsx',
      { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' }
    );
    
    // 发送OPTIONS请求检查接口是否可用
    const response = await axios({
      method: 'OPTIONS',
      url: `${API_URL}/objects/excel`,
      timeout: 5000
    });
    
    console.log('Excel上传接口测试结果:', response.status);
    return response.status >= 200 && response.status < 300;
  } catch (error) {
    console.error('Excel上传接口不可用:', error.message);
    return false;
  }
};

/**
 * 上传Excel文件并关联到特定数字对象ID
 * @param {Number} objectId - 数字对象ID
 * @param {File} file - 要上传的Excel文件
 * @returns {Promise<Object>} - 包含上传结果的Promise
 */
const uploadExcelFileWithObjectId = async (objectId, file) => {
  // 验证参数
  if (!objectId) {
    return {
      success: false,
      message: '未提供对象ID',
      data: null
    };
  }
  
  if (!file) {
    return {
      success: false,
      message: '未提供文件',
      data: null
    };
  }
  
  // 如果启用了mock模式，直接返回模拟数据
  if (MOCK_ENABLED) {
    try {
      // 使用导入的testApiConnection函数测试API连接
      const isApiAvailable = await testApiConnection();
      
      // 如果API不可用或设置了自动回退，返回模拟数据
      if (!isApiAvailable || AUTO_FALLBACK_TO_MOCK) {
        console.log('[Mock模式] 后端API不可用，使用模拟数据');
        const mockResponse = createMockSuccessResponse(file);
        mockResponse.data.objectId = objectId; // 添加对象ID信息
        return mockResponse;
      }
    } catch (error) {
      console.warn('测试API连接时出错:', error);
      if (AUTO_FALLBACK_TO_MOCK) {
        const mockResponse = createMockSuccessResponse(file);
        mockResponse.data.objectId = objectId;
        return mockResponse;
      }
    }
  }

  // 正常上传流程
  try {
    // 创建FormData对象
    const formData = new FormData();
    formData.append('file', file);
    
    console.log('准备上传文件到对象ID:', objectId, '文件名:', file.name, '大小:', file.size);
    
    // 发送上传请求
    const response = await axiosInstance.post(`/objects/${objectId}/excel`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      withCredentials: false, // 跨域请求不携带凭证
      // 添加上传进度处理
      onUploadProgress: progressEvent => {
        const percentCompleted = Math.round((progressEvent.loaded * 100) / progressEvent.total);
        console.log(`上传进度: ${percentCompleted}%`);
      }
    });
    
    console.log('上传请求响应:', response);
    
    // 检查响应状态
    if (response.data && response.data.code === 200) {
      return {
        success: true,
        message: '文件上传成功',
        data: response.data.data
      };
    } else {
      return {
        success: false,
        message: response.data?.message || '上传失败',
        data: null,
        details: response.data
      };
    }
  } catch (error) {
    console.error('Excel文件上传错误详情:', error);
    
    // 如果启用了自动回退到模拟模式，在错误时返回模拟成功响应
    if (MOCK_ENABLED && AUTO_FALLBACK_TO_MOCK) {
      console.log('[Mock模式] 上传失败，回退到模拟数据');
      const mockResponse = createMockSuccessResponse(file);
      mockResponse.data.objectId = objectId;
      return mockResponse;
    }
    
    // 详细记录错误信息
    let errorMessage = '上传过程中发生错误';
    let errorDetails = {
      message: error.message || '未知错误'
    };
    
    if (error.response) {
      // 服务器返回了错误状态码
      errorMessage = `服务器返回错误(${error.response.status}): ${error.response.data?.message || error.response.statusText}`;
      errorDetails.status = error.response.status;
      errorDetails.statusText = error.response.statusText;
      errorDetails.data = error.response.data;
    } else if (error.request) {
      // 请求已发送但未收到响应
      errorMessage = '服务器未响应请求，请检查后端服务是否运行';
      errorDetails.request = '已发送请求但未收到响应';
    } else {
      // 请求配置出错
      errorMessage = `请求配置错误: ${error.message}`;
    }
    
    return {
      success: false,
      message: errorMessage,
      data: null,
      details: errorDetails
    };
  }
};

export default {
  checkApiConnection,
  testApiConnection,
  uploadExcelFile,
  uploadExcelFileWithObjectId,
  testExcelUploadEndpoint
}; 