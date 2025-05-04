<!-- 反馈列 -->
<el-table-column
  prop="feedback"
  label="反馈"
  min-width="100"
  :show-overflow-tooltip="true">
  <template #default="{ row }">
    <div class="feedback-cell" :title="row.feedback || extractFeedback(row.dataContent)">
      <template v-if="row.feedback">
        <!-- 优先显示已提取的feedback字段 -->
        {{ row.feedback }}
      </template>
      <template v-else-if="row.entity === '客户反馈' && row.status === '不合格'">
        <!-- 客户反馈实体且不合格状态时强制显示反馈内容 -->
        <span class="error-text">数据格式错误</span>
      </template>
      <template v-else-if="extractFeedback(row.dataContent)">
        <!-- 尝试从dataContent提取反馈 -->
        {{ extractFeedback(row.dataContent) }}
      </template>
      <template v-else>
        <!-- 没有反馈内容时显示短横线 -->
        <span class="no-feedback">-</span>
      </template>
    </div>
  </template>
</el-table-column>

// 提取反馈信息的辅助函数
function extractFeedback(dataContent) {
  try {
    // 记录数据类型便于调试
    console.log(`extractFeedback: dataContent类型=${typeof dataContent}`);
    
    // 处理字符串类型
    if (typeof dataContent === 'string') {
      // 先尝试解析为JSON
      try {
        const jsonObj = JSON.parse(dataContent);
        if (jsonObj && jsonObj.feedback) {
          console.log(`从JSON解析成功: feedback="${jsonObj.feedback}"`);
          return jsonObj.feedback;
        }
        
        // 检查data子对象
        if (jsonObj && jsonObj.data && jsonObj.data.feedback) {
          console.log(`从data子对象解析成功: feedback="${jsonObj.data.feedback}"`);
          return jsonObj.data.feedback;
        }
      } catch (jsonError) {
        console.log('JSON解析失败，尝试正则匹配');
      }
      
      // 正则匹配标准格式
      const feedbackMatch = dataContent.match(/"feedback"\s*:\s*"([^"]*)"/);
      if (feedbackMatch && feedbackMatch[1]) {
        console.log(`正则匹配成功: feedback="${feedbackMatch[1]}"`);
        return feedbackMatch[1];
      }
      
      // 正则匹配转义格式
      const escapedMatch = dataContent.match(/\\"feedback\\"\\s*:\\s*\\"([^\\"]*)\\"/);
      if (escapedMatch && escapedMatch[1]) {
        console.log(`转义格式匹配成功: feedback="${escapedMatch[1]}"`);
        return escapedMatch[1];
      }
      
      // 检查是否直接包含关键词
      if (dataContent.includes('数据格式错误')) {
        console.log('直接包含关键词"数据格式错误"');
        return '数据格式错误';
      }
    }
    // 处理对象类型
    else if (dataContent && typeof dataContent === 'object') {
      // 直接访问对象属性
      if (dataContent.feedback) {
        console.log(`对象属性访问成功: feedback="${dataContent.feedback}"`);
        return dataContent.feedback;
      }
      
      // 检查data子对象
      if (dataContent.data && dataContent.data.feedback) {
        console.log(`对象data子属性访问成功: feedback="${dataContent.data.feedback}"`);
        return dataContent.data.feedback;
      }
    }
    
    console.log('未能提取到反馈信息');
    return null;
  } catch (error) {
    console.error('提取反馈信息时出错:', error);
    return null;
  }
} 