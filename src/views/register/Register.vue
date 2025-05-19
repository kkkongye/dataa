<template>
  <div class="register-container">
    <div class="register-box">
      <div class="register-title">
        <span class="system-icon">🔄</span>
        账号注册
      </div>
      <el-form :model="registerForm" class="register-form" ref="registerFormRef">
        <el-form-item>
          <el-input v-model="registerForm.username" placeholder="请输入用户名" class="register-input">
            <template #prefix>
              <el-icon class="input-icon"><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-input v-model="registerForm.password" type="password" placeholder="请输入密码" class="register-input">
            <template #prefix>
              <el-icon class="input-icon"><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <div class="role-section">
          <div class="role-label">请选择您的角色</div>
          <el-radio-group v-model="registerForm.roll" class="register-role-select">
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
          <el-button type="primary" class="register-btn" @click="handleRegister">注册账号</el-button>
        </el-form-item>
        <div class="register-footer">
          <span class="login-link" @click="goToLogin">已有账号？返回登录</span>
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
import axios from 'axios'

const router = useRouter()
const registerFormRef = ref(null)

const registerForm = reactive({
  username: '',
  password: '',
  roll: 'datasource' // 默认选中数源方
})

const handleRegister = async () => {
  if (!registerForm.username || !registerForm.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  
  try {
    const response = await axios.post('http://localhost:8080/api/register', {
      username: registerForm.username,
      password: registerForm.password,
      roll: registerForm.roll
    })
    
    // 打印完整的响应信息，用于调试
    console.log('注册响应数据:', response.data)
    
    // 后端成功状态码是1，不是200
    if (response.data.code === 1 || response.data.msg === 'success' || response.data.data?.includes('成功')) {
      // 使用type参数明确指定为success类型
      ElMessage({
        message: '注册成功，即将跳转到登录页面',
        type: 'success',
        duration: 2000
      })
      
      // 延迟跳转，让用户看到成功提示
      setTimeout(() => {
        router.push('/login')
      }, 2000)
    } else {
      ElMessage.error(response.data.msg || '注册失败')
    }
  } catch (error) {
    console.error('注册错误:', error)
    if (error.response) {
      console.log('错误响应数据:', error.response.data)
      ElMessage.error(error.response.data.msg || '注册失败，请稍后再试')
    } else {
      ElMessage.error('注册失败，可能网络连接问题')
    }
  }
}

const goToLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  width: 100vw;
  background: linear-gradient(135deg, #f0f7ff 0%, #b5d0e7 100%);
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  overflow: hidden;
  margin: 0;
  padding: 0;
}

.register-container::before {
  content: "";
  position: absolute;
  width: 200%;
  height: 200%;
  top: -50%;
  left: -50%;
  background: radial-gradient(circle, rgba(235,245,255,0.2) 0%, rgba(235,245,255,0) 70%);
  animation: pulse 15s infinite ease-in-out;
  pointer-events: none;
}

@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.05); }
  100% { transform: scale(1); }
}

.register-box {
  width: 420px;
  padding: 40px;
  background-color: rgba(255, 255, 255, 0.95);
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.12);
  backdrop-filter: blur(8px);
  transform: translateY(0);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  animation: fadeIn 0.8s ease-out;
  border: 1px solid rgba(235, 245, 255, 0.3);
}

.register-box:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.15);
}

@keyframes fadeIn {
  0% { opacity: 0; transform: translateY(20px); }
  100% { opacity: 1; transform: translateY(0); }
}

.register-title {
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

.register-title::after {
  content: "";
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  height: 3px;
  width: 80px;
  background: linear-gradient(90deg, #3d8cdd, #6aa9ef);
  border-radius: 3px;
}

.register-form {
  margin-top: 25px;
}

.input-icon {
  color: #409EFF;
}

.register-input :deep(.el-input__wrapper) {
  padding: 12px 15px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.register-input :deep(.el-input__wrapper:hover) {
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

.register-role-select {
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

.register-btn {
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

.register-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 15px rgba(64, 158, 255, 0.4);
}

.register-btn:active {
  transform: translateY(0);
}

.register-footer {
  margin-top: 20px;
  text-align: center;
  color: #909399;
  font-size: 12px;
}

.login-link {
  cursor: pointer;
  color: #409EFF;
  transition: all 0.3s ease;
}

.login-link:hover {
  text-decoration: underline;
}
</style> 