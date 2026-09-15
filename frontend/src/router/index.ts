import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('../views/Dashboard.vue')
  },
  {
    path: '/bracket',
    name: 'Bracket',
    component: () => import('../views/BracketManage.vue')
  },
  {
    path: '/group',
    name: 'Group',
    component: () => import('../views/GroupManage.vue')
  },
  {
    path: '/binding',
    name: 'Binding',
    component: () => import('../views/BindingManage.vue')
  },
  {
    path: '/schedule',
    name: 'Schedule',
    component: () => import('../views/ScheduleManage.vue')
  },
  {
    path: '/logs',
    name: 'Logs',
    component: () => import('../views/ChangeLogs.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router