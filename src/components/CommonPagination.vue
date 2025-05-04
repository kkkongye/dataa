<template>
  <div class="common-pagination-container" :class="{ 'border-top': showBorder }">
    <slot name="before-pagination"></slot>
    
    <div class="pagination-info" v-if="showInfo">
      <slot name="info">
        <span class="total-text">共 {{ totalCount }} 条</span>
        <span class="page-info" v-if="showPageInfo">
          {{ currentPageValue * pageSizeValue - pageSizeValue + 1 }}-{{ Math.min(currentPageValue * pageSizeValue, totalCount) }}/{{ totalCount }}
        </span>
      </slot>
    </div>
    
    <div class="pagination-controls">
      <el-pagination
        v-if="visible"
        v-model:current-page="currentPageValue"
        v-model:page-size="pageSizeValue"
        :page-sizes="pageSizes"
        :layout="layout"
        :total="totalCount"
        :background="background"
        :disabled="disabled"
        :small="small"
        :hide-on-single-page="hideOnSinglePage"
        :pager-count="pagerCount"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        @prev-click="handlePrevClick"
        @next-click="handleNextClick"
      />
    </div>
    
    <slot name="after-pagination"></slot>
  </div>
</template>

<script setup>
import { ref, watch, computed } from 'vue'

const props = defineProps({
  // 当前页码
  currentPage: {
    type: Number,
    default: 1
  },
  // 每页数据条数
  pageSize: {
    type: Number,
    default: 10
  },
  // 总数据量
  totalCount: {
    type: Number,
    default: 0
  },
  // 是否显示分页组件
  visible: {
    type: Boolean,
    default: true
  },
  // 可选的每页数据条数
  pageSizes: {
    type: Array,
    default: () => [5, 10, 20]
  },
  // 分页布局
  layout: {
    type: String,
    default: 'total, sizes, prev, pager, next, jumper'
  },
  // 是否为分页按钮添加背景色
  background: {
    type: Boolean,
    default: false
  },
  // 是否禁用
  disabled: {
    type: Boolean,
    default: false
  },
  // 是否使用小型分页样式
  small: {
    type: Boolean,
    default: false
  },
  // 只有一页时是否隐藏分页
  hideOnSinglePage: {
    type: Boolean,
    default: false
  },
  // 页码按钮的数量
  pagerCount: {
    type: Number,
    default: 7
  },
  // 是否显示数据汇总信息
  showInfo: {
    type: Boolean,
    default: true
  },
  // 是否显示当前页范围信息
  showPageInfo: {
    type: Boolean,
    default: true
  },
  // 是否显示上边框
  showBorder: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits([
  'update:currentPage', 
  'update:pageSize', 
  'size-change', 
  'current-change',
  'prev-click',
  'next-click'
])

// 使用计算属性和watch来处理双向绑定
const currentPageValue = computed({
  get: () => props.currentPage,
  set: (val) => emit('update:currentPage', val)
})

const pageSizeValue = computed({
  get: () => props.pageSize,
  set: (val) => emit('update:pageSize', val)
})

// 处理每页显示数量变化
const handleSizeChange = (val) => {
  emit('size-change', val)
}

// 处理当前页变化
const handleCurrentChange = (val) => {
  emit('current-change', val)
}

// 处理上一页点击
const handlePrevClick = (val) => {
  emit('prev-click', val)
}

// 处理下一页点击
const handleNextClick = (val) => {
  emit('next-click', val)
}

// 监听总数变化，自动调整当前页
watch(() => props.totalCount, (newValue, oldValue) => {
  if (newValue > 0 && oldValue > 0) {
    const maxPage = Math.ceil(newValue / props.pageSize)
    if (props.currentPage > maxPage) {
      currentPageValue.value = maxPage
    }
  }
})
</script>

<style scoped>
.common-pagination-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
  padding: 8px 0;
  width: 100%;
  box-sizing: border-box;
}

.border-top {
  border-top: 1px solid #ebeef5;
  padding-top: 16px;
}

.pagination-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.total-text {
  font-size: 14px;
  color: #606266;
  white-space: nowrap;
}

.page-info {
  font-size: 14px;
  color: #909399;
  white-space: nowrap;
}

.pagination-controls {
  display: flex;
  justify-content: flex-end;
}

/* 响应式布局 */
@media screen and (max-width: 768px) {
  .common-pagination-container {
    flex-direction: column;
    gap: 12px;
  }
  
  .pagination-info, .pagination-controls {
    width: 100%;
    justify-content: center;
  }
}
</style> 