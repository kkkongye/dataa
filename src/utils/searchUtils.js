

/**
 * 确保数据是数组格式
 * @param {any} value - 要转换为数组的值
 * @returns {Array} - 转换后的数组
 */
export const ensureArray = (value) => {
  if (Array.isArray(value)) {
    return [...value];
  }
  return value ? [value] : [];
};

/**
 * 高级搜索过滤函数
 * 根据关键词过滤数据，匹配实体名、约束条件和传播控制操作
 * @param {Array} data - 原始数据数组
 * @param {String} keyword - 搜索关键词
 * @returns {Array} - 过滤后的数据
 */
export const advancedSearch = (data, keyword) => {
  if (!keyword) {
    return data;
  }
  
  const searchTerm = keyword.toLowerCase();
  return data.filter(item => {
    if (
      (item.id?.toString().includes(searchTerm)) || 
      (item.entity?.toLowerCase().includes(searchTerm))
    ) {
      return true;
    }
 
    const constraints = ensureArray(item.constraint);
    if (constraints.some(c => c && typeof c === 'string' && c.toLowerCase().includes(searchTerm))) {
      return true;
    }

    const transferControls = ensureArray(item.transferControl);
    if (transferControls.some(t => t && typeof t === 'string' && t.toLowerCase().includes(searchTerm))) {
      return true;
    }
    
    return false;
  });
};

/**
 * 创建过滤后的数据
 * 组合状态过滤和搜索过滤，并应用分页
 * @param {Array} data - 原始数据
 * @param {String} status - 状态过滤值
 * @param {String} keyword - 搜索关键词
 * @param {Number} currentPage - 当前页码
 * @param {Number} pageSize - 每页显示条数
 * @returns {Object} - 包含过滤后数据和总数的对象
 */
export const getFilteredData = (data, status, keyword, currentPage, pageSize) => {
  let result = [...data];

  if (status) {
    result = result.filter(item => item.status === status);
  }

  if (keyword) {
    result = advancedSearch(result, keyword);
  }

  const totalCount = result.length;
  const startIndex = (currentPage - 1) * pageSize;
  const endIndex = startIndex + pageSize;
  const pagedData = result.slice(startIndex, endIndex);
  
  return {
    data: pagedData,
    total: totalCount
  };
};

export default {
  ensureArray,
  advancedSearch,
  getFilteredData
};