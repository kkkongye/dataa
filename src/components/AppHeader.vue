<template>
  <div class="header">
    <div class="title">个人可信数据空间</div>
    <div class="user-info">
      <el-icon class="setting-icon"><Setting /></el-icon>
      <el-dropdown trigger="click">
        <div class="user-dropdown">
          <el-avatar :size="32" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png"></el-avatar>
          <span class="user-role">{{ roleName }}</span>
          <el-icon><ArrowDown /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue'
import { Setting, ArrowDown } from '@element-plus/icons-vue'

const props = defineProps({
  // 角色名称：数源方、治理方或使用方
  roleName: {
    type: String,
    required: true
  }
})

const emit = defineEmits(['logout'])

// 处理退出登录
const handleLogout = () => {
  emit('logout')
}
</script>

<style scoped>
/* 头部样式 */
.header {
  height: 60px;
  width: 100%;
  background-color: #ffffff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  border-bottom: 1px solid #e8e8e8;
  flex-shrink: 0;
  box-sizing: border-box;
  z-index: 10;
}

.title {
  font-size: 25px;
  font-weight: bold;
  color: #1890ff;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 20px;
}

.setting-icon {
  font-size: 18px;
  cursor: pointer;
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.user-role {
  font-size: 14px;
  color: #333;
}
</style> 