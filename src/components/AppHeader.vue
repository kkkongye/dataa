<template>
  <div class="header">
    <div class="title">政务可信数据空间</div>
    <div class="user-info">
      <el-icon class="setting-icon"><Setting /></el-icon>
      <el-dropdown trigger="click">
        <div class="user-dropdown">
          <el-avatar :size="32" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png"></el-avatar>
          <span class="user-role">{{ displayName }}</span>
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
import { ref, onMounted } from 'vue'
import { Setting, ArrowDown } from '@element-plus/icons-vue'

const emit = defineEmits(['logout'])
const displayName = ref('')

// 角色映射
const getRoleDisplayName = (role) => {
  const roleMap = {
    'datasource': '数源方',
    'governor': '治理方',
    'user': '使用方'
  }
  return roleMap[role] || '未知角色'
}

// 获取当前用户信息
const getCurrentUser = () => {
  const username = localStorage.getItem('username')
  const role = localStorage.getItem('role')
  
  if (username && role) {
    const roleDisplayName = getRoleDisplayName(role)
    displayName.value = `${username}（${roleDisplayName}）`
  } else if (username) {
    displayName.value = username
  } else {
    displayName.value = '未知用户'
  }
}

// 处理退出登录
const handleLogout = () => {
  emit('logout')
}

// 组件挂载时获取用户信息
onMounted(() => {
  getCurrentUser()
})
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
  font-size: 25px;
  font-weight: 600;
  color: #1677c7;
  background: rgba(24, 144, 255, 0.08);
  padding: 6px 16px;
  border-radius: 10px;
  margin-left: 6px;
  transition: background 0.2s;
}
</style>