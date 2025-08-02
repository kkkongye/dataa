<template>
  <div class="register-container">
    <div class="register-box">
      <div class="register-title">
        <img src="/logo.jpg" alt="系统图标" class="system-icon" />
        政务可信数据空间
      </div>
      <div class="register-subtitle">账号注册</div>
      <el-form :model="registerForm" class="register-form" ref="registerFormRef">
        <el-form-item>
          <div class="username-selector">
            <el-icon class="input-icon"><User /></el-icon>
            <el-select v-model="registerForm.province" placeholder="请选择省份" class="province-select">
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
            <el-select v-if="registerForm.role !== 'governor'" v-model="registerForm.bureau" placeholder="请选择局" class="bureau-select">
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
            <span v-if="registerForm.role === 'governor'" class="bureau-text">大数据局</span>
          </div>
          <el-input v-if="registerForm.bureau === '自定义'" v-model="registerForm.customBureau" placeholder="请输入局名" class="custom-bureau-input" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="registerForm.password" type="password" placeholder="请输入密码" class="register-input">
            <template #prefix>
              <el-icon class="input-icon"><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item v-if="registerForm.role === 'user'">
          <el-input v-model="registerForm.organization" placeholder="请输入上级机构" class="register-input">
            <template #prefix>
              <el-icon class="input-icon"><OfficeBuilding /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <div class="role-section">
          <div class="role-label">请选择您的角色</div>
          <el-radio-group v-model="registerForm.role" class="register-role-select">
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
import { User, Lock, OfficeBuilding } from '@element-plus/icons-vue'
import axios from 'axios'

const router = useRouter()
const registerFormRef = ref(null)

const registerForm = reactive({
  username: '',
  password: '',
  role: 'datasource', 
  organization: '',
  province: '',
  bureau: '',
  customBureau: ''
})

const handleRegister = async () => {
  // 组合用户名
  if (!registerForm.province) {
    ElMessage.warning('请选择省份')
    return
  }
  
  // 治理方默认为大数据局
  if (registerForm.role === 'governor') {
    registerForm.bureau = '大数据局'
  } else {
    if (!registerForm.bureau) {
      ElMessage.warning('请选择局')
      return
    }
    if (registerForm.bureau === '自定义' && !registerForm.customBureau) {
      ElMessage.warning('请输入自定义局名')
      return
    }
  }
  
  if (!registerForm.password) {
    ElMessage.warning('请输入密码')
    return
  }
  
  const bureauName = registerForm.bureau === '自定义' ? registerForm.customBureau : registerForm.bureau
  registerForm.username = `${registerForm.province}${bureauName}`
  
  if (registerForm.role === 'user' && !registerForm.organization) {
    ElMessage.warning('使用方用户请填写上级机构')
    return
  }
  
  try {
    let requestData = {
      username: registerForm.username,
      password: registerForm.password,
      role: registerForm.role
    }
    
    if (registerForm.role === 'user') {
      requestData.superior = registerForm.organization
    }
    
    const response = await axios.post('http://localhost:8080/api/register', requestData)

    console.log('注册响应数据:', response.data)
    

    if (response.data.code === 1 || response.data.msg === 'success' || response.data.data?.includes('成功')) {

      ElMessage({
        message: '注册成功，即将跳转到登录页面',
        type: 'success',
        duration: 2000
      })

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
  width: 24px;
  height: 24px;
  margin-right: 8px;
  object-fit: contain;
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

.register-subtitle {
  text-align: center;
  font-size: 20px;
  font-weight: bold;
  color: #606266;
  margin-bottom: 20px;
  margin-top: 10px;
}

.register-form {
  margin-top: 25px;
}

.input-icon {
  color: #409EFF;
}

.register-input :deep(.el-input__wrapper) {
  padding: 8px 15px;
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