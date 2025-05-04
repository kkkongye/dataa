import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import { createPinia } from 'pinia'
import router from './router'
import App from './App.vue'
import './style.css'

// 创建Vue应用
const app = createApp(App)

// 将插件应用到Vue实例
app.use(ElementPlus)
app.use(createPinia())
app.use(router)

// 挂载应用
app.mount('#app')
