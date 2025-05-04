import { API_URL } from './apiConfig';
import axios from 'axios';

/**
 * 报告服务 - 处理与报告文件相关的操作
 */
const reportService = {
  /**
   * 获取数据问题报告内容 - 调用/api/baogao1生成报告
   * @returns {Promise<string>} 提示信息
   */
  getDataIssuesReport: async () => {
    try {
      console.log('开始获取数据问题报告，调用/api/baogao1接口');
      
      // 调用baogao1接口触发报告生成
      await axios.get(`${API_URL}/baogao1`);
      console.log('成功调用baogao1接口');
      
      // 简单返回提示信息
      return `# 库存管理数据问题报告
===================================

此处应显示从 D:\\datasystem\\test\\data_issues.txt 读取的内容。

文件已生成，但前端无法直接访问服务器文件系统。

可查看服务器上的 D:\\datasystem\\test\\data_issues.txt 文件获取实际内容。

===================================`;
    } catch (error) {
      console.error('获取数据问题报告失败:', error);
      // 出错时返回友好的错误信息
      return `# 数据问题报告获取失败
===================================

调用 /api/baogao1 接口时出错。

错误信息: ${error.message || '未知错误'}

请确认后端服务是否正常运行。
===================================`;
    }
  }
};

export default reportService; 