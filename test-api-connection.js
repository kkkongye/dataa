import fetch from 'node-fetch';

// 延迟函数
const delay = ms => new Promise(resolve => setTimeout(resolve, ms));

// 测试基本端点
async function testBasicEndpoint() {
  console.log('\n-------------------------------');
  console.log('测试基本API端点: http://localhost:8080/');
  console.log('-------------------------------');
  
  try {
    const response = await fetch('http://localhost:8080/');
    console.log(`状态码: ${response.status}`);
    
    // 输出响应头
    console.log('响应头:');
    for (const [key, value] of response.headers.entries()) {
      console.log(`  ${key}: ${value}`);
    }
    
    // 输出响应体
    const text = await response.text();
    if (text && text.length > 0) {
      console.log(`响应体: ${text.length > 100 ? text.substring(0, 100) + '...' : text}`);
    } else {
      console.log('响应体为空');
    }
    
    return true;
  } catch (error) {
    console.error(`请求出错: ${error.message}`);
    return false;
  }
}

// 测试API端点
async function testApiEndpoint() {
  console.log('\n-------------------------------');
  console.log('测试API端点: http://localhost:8080/api');
  console.log('-------------------------------');
  
  try {
    const response = await fetch('http://localhost:8080/api');
    console.log(`状态码: ${response.status}`);
    
    // 输出响应头
    console.log('响应头:');
    for (const [key, value] of response.headers.entries()) {
      console.log(`  ${key}: ${value}`);
    }
    
    // 输出响应体
    const text = await response.text();
    if (text && text.length > 0) {
      console.log(`响应体: ${text.length > 100 ? text.substring(0, 100) + '...' : text}`);
    } else {
      console.log('响应体为空');
    }
    
    return true;
  } catch (error) {
    console.error(`请求出错: ${error.message}`);
    return false;
  }
}

// 测试Excel上传端点
async function testExcelEndpoint() {
  console.log('\n-------------------------------');
  console.log('测试Excel上传端点: http://localhost:8080/api/objects/excel');
  console.log('-------------------------------');
  
  try {
    const response = await fetch('http://localhost:8080/api/objects/excel', {
      method: 'OPTIONS'
    });
    console.log(`状态码: ${response.status}`);
    
    // 输出响应头
    console.log('响应头:');
    for (const [key, value] of response.headers.entries()) {
      console.log(`  ${key}: ${value}`);
    }
    
    // 输出响应体
    const text = await response.text();
    if (text && text.length > 0) {
      console.log(`响应体: ${text.length > 100 ? text.substring(0, 100) + '...' : text}`);
    } else {
      console.log('响应体为空');
    }
    
    return true;
  } catch (error) {
    console.error(`请求出错: ${error.message}`);
    return false;
  }
}

// 运行所有测试
async function runTests() {
  console.log('=================================');
  console.log('开始API连接测试...');
  console.log('=================================');
  
  let results = {
    basic: false,
    api: false,
    excel: false
  };
  
  try {
    results.basic = await testBasicEndpoint();
    await delay(1000); // 延迟1秒
    
    results.api = await testApiEndpoint();
    await delay(1000); // 延迟1秒
    
    results.excel = await testExcelEndpoint();
  } catch (error) {
    console.error('测试过程中发生错误:', error);
  }
  
  console.log('\n=================================');
  console.log('测试结果汇总:');
  console.log('=================================');
  console.log(`基本端点  (http://localhost:8080/):           ${results.basic ? '✅ 可访问' : '❌ 不可访问'}`);
  console.log(`API端点   (http://localhost:8080/api):        ${results.api ? '✅ 可访问' : '❌ 不可访问'}`);
  console.log(`Excel端点 (http://localhost:8080/api/objects/excel): ${results.excel ? '✅ 可访问' : '❌ 不可访问'}`);
  
  if (!results.basic && !results.api && !results.excel) {
    console.log('\n🔴 结论: 后端服务器不可访问或未运行');
  } else if (!results.excel) {
    console.log('\n🟠 结论: 后端服务器运行中，但Excel上传接口未实现或不可访问');
  } else {
    console.log('\n🟢 结论: 后端服务器运行正常，所有接口可访问');
  }
  
  console.log('\n测试完成');
}

runTests(); 