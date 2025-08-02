<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-title">
        <span class="system-icon">🔄</span>
        政务可信数据空间
      </div>
      <el-form :model="loginForm" class="login-form" ref="loginFormRef">
        <el-form-item>
          <div class="username-selector">
            <el-icon class="input-icon"><User /></el-icon>
            <el-select v-model="loginForm.province" placeholder="请选择省份" class="province-select">
              <el-option label="浙江省" value="浙江省" />
              <el-option label="北京市" value="北京市" />
              <el-option label="天津市" value="天津市" />
              <el-option label="河北省" value="河北省" />
              <el-option label="山西省" value="山西省" />
              <el-option label="内蒙古自治区" value="内蒙古自治区" />
              <el-option label="辽宁省" value="辽宁省" />
              <el-option label="吉林省" value="吉林省" />
              <el-option label="黑龙江省" value="黑龙江省" />
              <el-option label="上海市" value="上海市" />
              <el-option label="江苏省" value="江苏省" />
              <el-option label="安徽省" value="安徽省" />
              <el-option label="福建省" value="福建省" />
              <el-option label="江西省" value="江西省" />
              <el-option label="山东省" value="山东省" />
              <el-option label="河南省" value="河南省" />
              <el-option label="湖北省" value="湖北省" />
              <el-option label="湖南省" value="湖南省" />
              <el-option label="广东省" value="广东省" />
              <el-option label="广西壮族自治区" value="广西壮族自治区" />
              <el-option label="海南省" value="海南省" />
              <el-option label="重庆市" value="重庆市" />
              <el-option label="四川省" value="四川省" />
              <el-option label="贵州省" value="贵州省" />
              <el-option label="云南省" value="云南省" />
              <el-option label="西藏自治区" value="西藏自治区" />
              <el-option label="陕西省" value="陕西省" />
              <el-option label="甘肃省" value="甘肃省" />
              <el-option label="青海省" value="青海省" />
              <el-option label="宁夏回族自治区" value="宁夏回族自治区" />
              <el-option label="新疆维吾尔自治区" value="新疆维吾尔自治区" />
            </el-select>
            <el-select v-if="loginForm.role !== 'governor'" v-model="loginForm.bureau" placeholder="请选择局" class="bureau-select">
              <el-option label="税务局" value="税务局" />
              <el-option label="审计局" value="审计局" />
              <el-option label="财政局" value="财政局" />
              <el-option label="统计局" value="统计局" />
              <el-option label="民政局" value="民政局" />
              <el-option label="教育局" value="教育局" />
              <el-option label="医保局" value="医保局" />
              <el-option label="科技局" value="科技局" />
              <el-option label="旅游局" value="旅游局" />
              <el-option label="体育局" value="体育局" />
              <el-option label="自定义" value="自定义" />
            </el-select>
            <span v-if="loginForm.role === 'governor'" class="bureau-text">大数据局</span>
          </div>
          <el-input v-if="loginForm.bureau === '自定义'" v-model="loginForm.customBureau" placeholder="请输入局名" class="custom-bureau-input" />
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
          <el-button type="primary" class="login-btn" @click="handleLogin" :loading="loading">登录系统</el-button>
        </el-form-item>
        <div class="login-footer">
          <span class="register-link" @click="goToRegister">注册账号</span>
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
const loginFormRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: '',
  role: 'datasource', // 默认选中数源方
  province: '',
  bureau: '',
  customBureau: ''
})

const handleLogin = async () => {
  // 组合用户名
  if (!loginForm.province) {
    ElMessage.warning('请选择省份')
    return
  }
  
  // 治理方默认为大数据局
  if (loginForm.role === 'governor') {
    loginForm.bureau = '大数据局'
  } else {
    if (!loginForm.bureau) {
      ElMessage.warning('请选择局')
      return
    }
    if (loginForm.bureau === '自定义' && !loginForm.customBureau) {
      ElMessage.warning('请输入自定义局名')
      return
    }
  }
  
  if (!loginForm.password) {
    ElMessage.warning('请输入密码')
    return
  }
  
  const bureauName = loginForm.bureau === '自定义' ? loginForm.customBureau : loginForm.bureau
  loginForm.username = `${loginForm.province}${bureauName}`
  loading.value = true
  try {

    const response = await axios.post(
      'http://localhost:8080/api/login',
      { username: loginForm.username, password: loginForm.password },
      { withCredentials: true }
    )
    
    if (response.data && (response.data.token || response.data.message === '登录成功')) {
      localStorage.setItem('role', loginForm.role)
      localStorage.setItem('username', loginForm.username)
      localStorage.setItem('userId', response.data.id?.toString?.() || '')
      if (response.data.token) {
        localStorage.setItem('token', response.data.token)
      }
      ElMessage.success('登录成功')

      switch (loginForm.role) {
        case 'datasource':
          router.push('/datasource')
          break
        case 'governor':
          router.push('/governor')
          break
        case 'user':
          router.push('/user-main')
          break
        default:
          router.push('/datasource')
      }
    } else {
      ElMessage.error('登录失败，用户名或密码错误')
    }
  } catch (error) {
    ElMessage.error('登录失败，请重试')
  } finally {
    loading.value = false
  }
}


const goToRegister = () => {
  router.push('/register')
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  width: 100vw;
  background-image: url('/loginbg.png');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  background-attachment: fixed;
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

.username-selector {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
}

.username-icon {
  color: #909399;
  font-size: 16px;
  flex-shrink: 0;
}

.bureau-text {
  color: #606266;
  font-size: 14px;
  padding: 8px 15px;
  background-color: #f5f7fa;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
  transition: all 0.3s ease;
}

.province-select,
.bureau-select {
  flex: 1;
  min-width: 0;
}

.province-select :deep(.el-input__wrapper),
.bureau-select :deep(.el-input__wrapper) {
  padding: 8px 15px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.province-select :deep(.el-input__wrapper:hover),
.bureau-select :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #409EFF;
}

.custom-bureau-input {
  margin-top: 10px;
  width: 100%;
}

.login-form {
  margin-top: 25px;
}

.input-icon {
  color: #409EFF;
}

.login-input :deep(.el-input__wrapper) {
  padding: 8px 15px;
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

.register-link {
  cursor: pointer;
  color: #409EFF;
  transition: all 0.3s ease;
}

.register-link:hover {
  text-decoration: underline;
}
</style>