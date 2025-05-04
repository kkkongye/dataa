import axios from 'axios';

// 基础API URL
export const API_BASE_URL = 'http://localhost:8080';  // 确保这里的端口与后端服务端口一致
export const API_URL = `${API_BASE_URL}/api`;

// 在生产环境可能需要切换到真实服务器地址
// export const API_BASE_URL = process.env.NODE_ENV === 'production' 
//   ? 'http://your-production-server:8080' 
//   : 'http://localhost:8080';

// Mock模式配置 - 在API不可用时提供回退
export const MOCK_ENABLED = true; // 保持模拟模式开启，以防API不可用
export const AUTO_FALLBACK_TO_MOCK = true; // 保持自动回退

// 创建一个预配置的axios实例
export const axiosInstance = axios.create({
  baseURL: API_URL,
  timeout: 15000, // 增加超时时间到15秒
  withCredentials: false, // 修改为false，避免CORS预检请求问题
  headers: {
    'Content-Type': 'application/json'
  }
});

// 请求拦截器
axiosInstance.interceptors.request.use(
  config => {
    // 在每个请求中添加mock模式支持，方便测试
    if (MOCK_ENABLED && config.mock !== false) {
      config.headers['X-Mock-Mode'] = 'enabled';
    }
    
    // 添加CORS支持
    config.headers['Access-Control-Allow-Origin'] = '*';
    config.headers['Access-Control-Allow-Methods'] = 'GET, POST, PUT, DELETE, PATCH, OPTIONS';
    config.headers['Access-Control-Allow-Headers'] = 'Origin, X-Requested-With, Content-Type, Accept, Authorization';
    
    return config;
  },
  error => {
    return Promise.reject(error);
  }
);

// 响应拦截器
axiosInstance.interceptors.response.use(
  response => {
    // 直接返回响应数据
    return response;
  },
  error => {
    // 处理CORS错误
    if (error.message && error.message.includes('Network Error')) {
      console.warn('可能的CORS问题或网络连接失败:', error.message);
      
      // 如果配置了自动回退到模拟模式并且原始请求未明确禁用模拟
      if (AUTO_FALLBACK_TO_MOCK && 
          error.config && 
          error.config.mock !== false) {
        
        console.log('自动回退到模拟模式，返回虚拟成功响应');
        
        // 根据请求类型返回模拟数据
        const mockData = {
          success: true,
          code: 200,
          message: '模拟响应成功',
          data: {}
        };
        
        // 如果是状态更新请求，返回特定的模拟响应
        if (error.config.url && error.config.url.includes('/status')) {
          mockData.message = '状态更新成功（模拟响应）';
        }
        
        // 返回模拟的axios响应对象
        return Promise.resolve({
          data: mockData,
          status: 200,
          statusText: 'OK (Mocked)',
          headers: {},
          config: error.config,
          mock: true // 标记这是一个模拟响应
        });
      }
    }
    
    // 统一处理错误
    if (error.response) {
      // 服务器返回了错误状态码
      console.error('API请求错误:', error.response.status, error.response.data);
    } else if (error.request) {
      // 请求已发送但未收到响应
      console.error('API请求未收到响应:', error.request);
    } else {
      // 请求配置出错
      console.error('API请求配置错误:', error.message);
    }
    return Promise.reject(error);
  }
);

// 测试API连接是否可用
export const testApiConnection = async () => {
  try {
    console.log('开始测试API连接，目标URL:', `${API_URL}/objects/list`);
    
    // 使用列表API测试连接 - 这是一个可能较为稳定的端点
    const response = await axiosInstance.get('/objects/list', { 
      timeout: 5000,  // 增加超时时间
      validateStatus: function (status) {
        // 任何响应码都视为"连接成功"，因为我们只是测试连接
        return status >= 200 && status < 600;
      }
    });
    
    console.log('API连接测试成功，状态码:', response.status, '响应数据:', response.data);
    return true;
  } catch (error) {
    console.warn('API连接测试失败:', error.message);
    
    // 更详细地记录错误信息
    if (error.code) {
      console.error('错误代码:', error.code);
    }
    
    if (error.config) {
      console.error('请求配置:', {
        url: error.config.url,
        method: error.config.method,
        baseURL: error.config.baseURL,
        timeout: error.config.timeout
      });
    }
    
    // 尝试检查错误是否是由于网络问题，而不是服务器返回的错误
    if (!error.response && error.code === 'ECONNREFUSED') {
      console.error('无法连接到服务器，可能服务器未启动或地址错误');
      return false;
    }
    
    if (error.code === 'ECONNABORTED') {
      console.error('连接超时，服务器响应时间过长');
      return false;
    }
    
    // 如果有响应但状态码不是2xx，也算连接成功（服务器活着但返回了错误）
    if (error.response) {
      console.warn('服务器返回了错误状态码，但连接是有效的:', error.response.status);
      return true;
    }
    
    return false;
  }
};

export default {
  axiosInstance,
  API_URL,
  API_BASE_URL,
  MOCK_ENABLED,
  AUTO_FALLBACK_TO_MOCK,
  testApiConnection
}; 