import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/dashboard',
  },
  {
    path: '/dashboard',
    component: () => import('../views/Dashboard.vue'),
    meta: { title: '仪表板' },
  },
  {
    path: '/tasks',
    component: () => import('../views/TaskList.vue'),
    meta: { title: '任务管理' },
  },
  {
    path: '/tasks/:id',
    component: () => import('../views/TaskDetail.vue'),
    meta: { title: '任务详情' },
  },
  {
    path: '/create',
    component: () => import('../views/CreateTask.vue'),
    meta: { title: '创建任务' },
  },
  {
    path: '/logs',
    component: () => import('../views/LogViewer.vue'),
    meta: { title: '日志查看' },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title || '任务队列管理系统'
  next()
})

export default router
