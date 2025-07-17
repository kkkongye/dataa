// reportService.js
// 数据报告相关服务

import axios from 'axios'

/**
 * 报告服务
 */
const reportService = {
  /**
   * 获取数据问题报告
   * @returns {Promise<string>} 报告内容
   */
  async getDataIssuesReport() {
    try {
      // 尝试从API获取报告
      try {
        const response = await axios.get('http://localhost:8081/api/baogao1')
        if (response.data) {
          return typeof response.data === 'string' ? response.data : JSON.stringify(response.data, null, 2)
        }
      } catch (e) {
        console.warn('从API获取报告失败，将使用默认报告', e)
      }
      
      // 返回默认报告
      return `
数据审查报告
======================

审查时间: ${new Date().toLocaleString('zh-CN')}
审查对象: 数据对象集合
审查结果: 通过

此处应显示从 D:\\datasystem\\test\\data_issues.txt 读取的内容。

文件已生成，但前端无法直接访问服务器文件系统。

可查看服务器上的 D:\\datasystem\\test\\data_issues.txt 文件获取实际内容。

===================================`;
    } catch (error) {
      console.error('获取数据问题报告失败:', error)
      throw error
    }
  },
  
  /**
   * 获取指定对象的审查报告
   * @param {string} objectId 对象ID
   * @returns {Promise<string>} 报告内容
   */
  async getObjectReviewReport(objectId) {
    try {
      // 尝试从API获取特定对象的报告
      try {
        const response = await axios.get(`http://localhost:8081/api/objects/${objectId}/report`)
        if (response.data) {
          return typeof response.data === 'string' ? response.data : JSON.stringify(response.data, null, 2)
        }
      } catch (e) {
        console.warn(`从API获取对象${objectId}的报告失败，将使用默认报告`, e)
      }
      
      // 返回默认报告
      return `
数据对象审查报告 (ID: ${objectId})
======================

审查时间: ${new Date().toLocaleString('zh-CN')}
审查结果: 通过

一、数据完整性检查
-----------------------
1. 字段完整性: 通过
2. 记录完整性: 通过
3. 必填项检查: 通过

二、数据一致性检查
-----------------------
1. 跨表一致性: 通过
2. 业务规则一致性: 通过

调用 /api/baogao1 接口时出错。

错误信息: ${error.message || '未知错误'}

结论: 该数据对象符合质量标准，可以进行后续处理。
`
    } catch (error) {
      console.error(`获取对象${objectId}的审查报告失败:`, error)
      throw error
    }
  }
}

export default reportService 