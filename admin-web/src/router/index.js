import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/LoginView.vue'),
    meta: { public: true, title: '登录' }
  },
  {
    path: '/',
    component: () => import('../layout/AdminLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'dashboard',
        component: () => import('../views/DashboardView.vue'),
        meta: { title: '数据概览', roles: ['ADMIN'] }
      },
      {
        path: 'orders',
        name: 'orders',
        component: () => import('../views/OrderView.vue'),
        meta: { title: '订单管理' }
      },
      {
        path: 'services',
        name: 'services',
        component: () => import('../views/ServiceTypeView.vue'),
        meta: { title: '服务项目' }
      },
      {
        path: 'points/goods',
        name: 'points-goods',
        component: () => import('../views/PointsGoodsView.vue'),
        meta: { title: '积分商品', roles: ['ADMIN'] }
      },
      {
        path: 'points/exchanges',
        name: 'points-exchanges',
        component: () => import('../views/ExchangeView.vue'),
        meta: { title: '兑换核销', roles: ['ADMIN', 'STAFF'] }
      },
      {
        path: 'notices',
        name: 'notices',
        component: () => import('../views/NoticeView.vue'),
        meta: { title: '公告管理', roles: ['ADMIN'] }
      },
      {
        path: 'activities',
        name: 'activities',
        component: () => import('../views/ActivityView.vue'),
        meta: { title: '活动管理', roles: ['ADMIN'] }
      },
      {
        path: 'messages',
        name: 'messages',
        component: () => import('../views/MessageView.vue'),
        meta: { title: '留言管理', roles: ['ADMIN'] }
      },
      {
        path: 'users',
        name: 'users',
        component: () => import('../views/UserView.vue'),
        meta: { title: '用户管理', roles: ['ADMIN'] }
      },
      {
        path: 'system/cache',
        name: 'system-cache',
        component: () => import('../views/CacheView.vue'),
        meta: { title: '缓存管理', roles: ['ADMIN'] }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/orders'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(to => {
  const auth = useAuthStore()
  if (to.meta.public) {
    return auth.token ? { path: '/' } : true
  }
  if (!auth.token) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if (to.meta.roles && !to.meta.roles.includes(auth.role)) {
    return { path: '/orders' }
  }
  return true
})

router.afterEach(to => {
  document.title = (to.meta.title ? to.meta.title + ' · ' : '') + '星辰汽车美容管理端'
})

export default router
