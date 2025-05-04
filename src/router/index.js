import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/login/Login.vue')
  },
  {
    path: '/datasource',
    name: 'DataSource',
    component: () => import('../views/datasource/DataSource.vue')
  },
  {
    path: '/governor',
    name: 'Governor',
    component: () => import('../views/governor/Governor.vue')
  },
  {
    path: '/user',
    name: 'User',
    component: () => import('../views/user/User.vue')
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

export default router 