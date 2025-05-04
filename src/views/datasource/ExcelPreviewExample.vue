<template>
  <div class="excel-preview-example">
    <el-button type="primary" @click="openExcelPreview">打开Excel预览</el-button>
    
    <!-- Excel预览对话框 -->
    <ExcelPreview
      v-model:visible="excelPreviewVisible"
      :object="previewObject"
    />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import ExcelPreview from '@/components/ExcelPreview.vue'

// 预览对话框可见性
const excelPreviewVisible = ref(false)

// 要预览的对象
const previewObject = ref({
  id: 123,
  entity: '用户数据',
  locationInfo: {
    row: '0-4',
    col: '0-4'
  },
  // 这里是关键：metadataJson可以是字符串形式的JSON
  metadataJson: JSON.stringify({
    dataName: '用户数据',
    sourceUnit: '数据部',
    contactPerson: '王主任',
    contactPhone: '123-456789',
    resourceSummary: '用户基本信息',
    fieldClassification: '个人信息'
  }),
  // 可选：直接提供元数据对象（如果已经解析过metadataJson）
  metadata: {
    dataName: '用户数据',
    sourceUnit: '数据部',
    contactPerson: '王主任',
    contactPhone: '123-456789',
    resourceSummary: '用户基本信息', 
    fieldClassification: '个人信息'
  },
  excelData: null // 二进制Excel数据
})

// 打开预览对话框
const openExcelPreview = () => {
  excelPreviewVisible.value = true
}
</script>

<style scoped>
.excel-preview-example {
  padding: 20px;
}
</style> 