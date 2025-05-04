import fetch from 'node-fetch';
import fs from 'fs';
import { FormData, File, Blob } from 'formdata-node';
import { fileFromPath } from 'formdata-node/file-from-path';

// 配置信息
const API_URL = 'http://localhost:8080/api';
const SAMPLE_EXCEL_PATH = './sample.xlsx'; // 如果没有可用的Excel文件，脚本会创建一个

// 创建一个简单的示例Excel文件（如果不存在）
async function createSampleExcelFile() {
  if (fs.existsSync(SAMPLE_EXCEL_PATH)) {
    console.log(`使用现有的示例文件: ${SAMPLE_EXCEL_PATH}`);
    return;
  }
  
  console.log('创建示例Excel文件...');
  // 创建一个简单的Excel文件内容（实际上是空文件）
  const buffer = Buffer.from('PK\x03\x04\x14\x00\x06\x00\x08\x00\x00\x00!\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00', 'binary');
  fs.writeFileSync(SAMPLE_EXCEL_PATH, buffer);
  console.log(`示例文件已创建: ${SAMPLE_EXCEL_PATH}`);
}

// 测试文件上传
async function testFileUpload() {
  console.log('\n=================================');
  console.log('开始测试Excel文件上传...');
  console.log('=================================');
  
  try {
    await createSampleExcelFile();
    
    // 准备上传的文件
    const file = await fileFromPath(SAMPLE_EXCEL_PATH, {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    });
    
    // 创建FormData对象
    const formData = new FormData();
    formData.append('file', file);
    
    console.log('正在上传文件...');
    console.log(`文件大小: ${file.size} 字节`);
    console.log(`文件类型: ${file.type}`);
    console.log(`上传URL: ${API_URL}/objects/excel`);
    
    // 发送上传请求
    const response = await fetch(`${API_URL}/objects/excel`, {
      method: 'POST',
      body: formData,
      headers: {
        // FormData会自动设置正确的Content-Type，包含boundary
      }
    });
    
    console.log(`\n状态码: ${response.status}`);
    
    // 输出响应头
    console.log('响应头:');
    for (const [key, value] of response.headers.entries()) {
      console.log(`  ${key}: ${value}`);
    }
    
    // 输出响应体
    let responseData;
    const contentType = response.headers.get('content-type');
    if (contentType && contentType.includes('application/json')) {
      responseData = await response.json();
      console.log('响应体 (JSON):', JSON.stringify(responseData, null, 2));
    } else {
      const text = await response.text();
      console.log(`响应体: ${text.length > 0 ? text : '(空)'}`);
    }
    
    // 分析结果
    if (response.ok) {
      console.log('\n✅ 文件上传成功!');
    } else {
      console.log('\n❌ 文件上传失败!');
    }
    
    return response.ok;
  } catch (error) {
    console.error(`\n文件上传出错: ${error.message}`);
    if (error.code === 'ECONNREFUSED') {
      console.error('无法连接到服务器，请确认服务器是否正在运行');
    }
    return false;
  }
}

// 测试上传文件到特定对象ID
async function testFileUploadWithId() {
  console.log('\n=================================');
  console.log('开始测试Excel文件上传到特定对象...');
  console.log('=================================');
  
  const objectId = 1; // 示例ID
  
  try {
    await createSampleExcelFile();
    
    // 准备上传的文件
    const file = await fileFromPath(SAMPLE_EXCEL_PATH, {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    });
    
    // 创建FormData对象
    const formData = new FormData();
    formData.append('file', file);
    
    console.log('正在上传文件...');
    console.log(`文件大小: ${file.size} 字节`);
    console.log(`文件类型: ${file.type}`);
    console.log(`上传URL: ${API_URL}/objects/${objectId}/excel`);
    
    // 发送上传请求
    const response = await fetch(`${API_URL}/objects/${objectId}/excel`, {
      method: 'POST',
      body: formData,
      headers: {
        // FormData会自动设置正确的Content-Type，包含boundary
      }
    });
    
    console.log(`\n状态码: ${response.status}`);
    
    // 输出响应头
    console.log('响应头:');
    for (const [key, value] of response.headers.entries()) {
      console.log(`  ${key}: ${value}`);
    }
    
    // 输出响应体
    let responseData;
    const contentType = response.headers.get('content-type');
    if (contentType && contentType.includes('application/json')) {
      responseData = await response.json();
      console.log('响应体 (JSON):', JSON.stringify(responseData, null, 2));
    } else {
      const text = await response.text();
      console.log(`响应体: ${text.length > 0 ? text : '(空)'}`);
    }
    
    // 分析结果
    if (response.ok) {
      console.log('\n✅ 文件上传成功!');
    } else {
      console.log('\n❌ 文件上传失败!');
    }
    
    return response.ok;
  } catch (error) {
    console.error(`\n文件上传出错: ${error.message}`);
    if (error.code === 'ECONNREFUSED') {
      console.error('无法连接到服务器，请确认服务器是否正在运行');
    }
    return false;
  }
}

// 运行测试
async function runTests() {
  let results = {
    upload: false,
    uploadWithId: false
  };
  
  results.upload = await testFileUpload();
  await new Promise(resolve => setTimeout(resolve, 1000)); // 延迟1秒
  
  results.uploadWithId = await testFileUploadWithId();
  
  console.log('\n=================================');
  console.log('测试结果汇总:');
  console.log('=================================');
  console.log(`普通上传:      ${results.upload ? '✅ 成功' : '❌ 失败'}`);
  console.log(`ID关联上传:    ${results.uploadWithId ? '✅ 成功' : '❌ 失败'}`);
  
  if (results.upload || results.uploadWithId) {
    console.log('\n🟢 结论: 文件上传接口工作正常');
    
    console.log('\n问题排查建议:');
    console.log('1. 前端可能存在跨域(CORS)问题，检查浏览器控制台');
    console.log('2. 前端FormData格式可能不符合后端期望，检查是否使用了正确的字段名(file)');
    console.log('3. 检查前端发送的Content-Type是否正确(应为multipart/form-data)');
  } else {
    console.log('\n🔴 结论: 文件上传接口不工作');
    
    console.log('\n问题排查建议:');
    console.log('1. 检查后端是否正确实现了文件上传接口');
    console.log('2. 检查接口参数要求，特别是文件字段名称');
    console.log('3. 检查服务器日志获取更多信息');
  }
}

// 添加命令行参数支持
const args = process.argv.slice(2);
if (args.includes('--upload-only')) {
  testFileUpload().then(() => console.log('测试完成'));
} else if (args.includes('--upload-with-id-only')) {
  testFileUploadWithId().then(() => console.log('测试完成'));
} else {
  runTests().then(() => console.log('所有测试完成'));
} 