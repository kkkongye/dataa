<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-title">
        <span class="system-icon">🔄</span>
        个人可信数据空间
      </div>
      <el-form :model="loginForm" class="login-form" ref="loginFormRef">
        <el-form-item>
          <el-input v-model="loginForm.username" placeholder="请输入用户名" class="login-input">
            <template #prefix>
              <el-icon class="input-icon"><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" class="login-input">
            <template #prefix>
              <el-icon class="input-icon"><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <div class="role-section">
          <div class="role-label">请选择您的角色</div>
          <el-radio-group v-model="loginForm.role" class="login-role-select">
            <el-radio label="datasource" class="role-option">
              <div class="role-content">📊 数源方</div>
            </el-radio>
            <el-radio label="governor" class="role-option">
              <div class="role-content">⚖️ 治理方</div>
            </el-radio>
            <el-radio label="user" class="role-option">
              <div class="role-content">👤 使用方</div>
            </el-radio>
          </el-radio-group>
        </div>
        <el-form-item>
          <el-button type="primary" class="login-btn" @click="handleLogin">登录系统</el-button>
        </el-form-item>
        <div class="login-footer">
          个人可信数据空间
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'

const router = useRouter()
const loginFormRef = ref(null)

const loginForm = reactive({
  username: '',
  password: '',
  role: 'datasource' // 默认选中数源方
})

const handleLogin = () => {
  if (!loginForm.username || !loginForm.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  
  // 模拟登录成功，根据角色跳转到不同页面
  localStorage.setItem('role', loginForm.role)
  
  console.log('登录成功，角色:', loginForm.role)
  
  try {
    switch (loginForm.role) {
      case 'datasource':
        console.log('跳转到数源方页面')
        router.push('/datasource')
        break
      case 'governor':
        console.log('跳转到治理方页面')
        router.push('/governor')
        break
      case 'user':
        console.log('跳转到使用方页面')
        router.push('/user')
        break
      default:
        console.log('默认跳转到数源方页面')
        router.push('/datasource')
    }
  } catch (error) {
    console.error('路由跳转错误:', error)
    ElMessage.error('页面跳转失败，请查看控制台错误信息')
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  width: 100vw;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  overflow: hidden;
  margin: 0;
  padding: 0;
}

.login-container::before {
  content: "";
  position: absolute;
  width: 200%;
  height: 200%;
  top: -50%;
  left: -50%;
  background: radial-gradient(circle, rgba(255,255,255,0.2) 0%, rgba(255,255,255,0) 70%);
  animation: pulse 15s infinite ease-in-out;
  pointer-events: none;
}

@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.05); }
  100% { transform: scale(1); }
}

.login-box {
  width: 420px;
  padding: 40px;
  background-color: rgba(255, 255, 255, 0.95);
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(5px);
  transform: translateY(0);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  animation: fadeIn 0.8s ease-out;
}

.login-box:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.15);
}

@keyframes fadeIn {
  0% { opacity: 0; transform: translateY(20px); }
  100% { opacity: 1; transform: translateY(0); }
}

.login-title {
  margin-bottom: 35px;
  text-align: center;
  font-size: 28px;
  font-weight: bold;
  color: #2c3e50;
  position: relative;
  padding-bottom: 15px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.system-icon {
  font-size: 32px;
  margin-right: 10px;
}

.login-title::after {
  content: "";
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  height: 3px;
  width: 80px;
  background: linear-gradient(90deg, #409EFF, #53a8ff);
  border-radius: 3px;
}

.login-form {
  margin-top: 25px;
}

.input-icon {
  color: #409EFF;
}

.login-input :deep(.el-input__wrapper) {
  padding: 12px 15px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.login-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #409EFF;
}

.role-section {
  margin: 15px 0;
}

.role-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 10px;
}

.login-role-select {
  width: 100%;
  display: flex;
  justify-content: space-around;
  margin: 10px 0;
}

.role-option {
  transition: all 0.3s ease;
}

.role-option:hover .role-content {
  transform: translateY(-3px);
}

.role-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  transition: all 0.3s ease;
}

.login-btn {
  width: 100%;
  padding: 12px 0;
  font-size: 16px;
  font-weight: 500;
  border-radius: 6px;
  background: linear-gradient(90deg, #409EFF, #53a8ff);
  border: none;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
  margin-top: 10px;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 15px rgba(64, 158, 255, 0.4);
}

.login-btn:active {
  transform: translateY(0);
}

.login-footer {
  margin-top: 20px;
  text-align: center;
  color: #909399;
  font-size: 12px;
}
</style> 