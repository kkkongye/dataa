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
    path: '/register',
    name: 'Register',
    component: () => import('../views/register/Register.vue')
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
  },
  {
    path: '/user-main',
    name: 'UserMain',
    component: () => import('../views/user/user-main.vue')
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

export default router