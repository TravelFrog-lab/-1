import Vue from 'vue'
import VueRouter, { RouteConfig } from 'vue-router'
import home from '../layout/index.vue';
import { transformAdminMenuList } from '@/utils/adminMenuTransform';

import ShowView from '@/views/back/showView.vue';


import Login from '@/views/Login.vue';
import LRLayout from '@/layout/LRLayout.vue';
import RegisterVue from '@/views/RegisterVue.vue';


import Forget from '@/views/Forget.vue';
import Home from '@/views/front/Home.vue'
import FrontUserLayout from '@/layout/FrontUserLayout.vue'
import store from '../store'

Vue.use(VueRouter)

const frontMeta = { requiresAuth: true, userType: 'front' }

const routes = [
  // 业主端：左侧导航 + 右侧内容区
  {
    path: '/',
    component: FrontUserLayout,
    meta: frontMeta,
    children: [
      { path: '', redirect: 'home' },
      {
        path: 'home',
        name: 'Home',
        component: Home,
        meta: frontMeta
      },
      {
        path: 'news/:id',
        name: 'NewsDetail',
        component: () => import('@/views/front/NewsDetail.vue'),
        meta: frontMeta
      },
      {
        path: 'property-fee',
        name: 'PropertyFee',
        component: () => import('@/views/front/PropertyFee.vue'),
        meta: frontMeta
      },
      {
        path: 'repair',
        name: 'Repair',
        component: () => import('@/views/front/Repair.vue'),
        meta: frontMeta
      },
      {
        path: 'complaint',
        name: 'Complaint',
        component: () => import('@/views/front/Complaint.vue'),
        meta: frontMeta
      },
      {
        path: 'volunteer',
        redirect: 'community-activity'
      },
      {
        path: 'community-activity',
        name: 'CommunityActivity',
        component: () => import('@/views/front/Volunteer.vue'),
        meta: frontMeta
      },
      {
        path: 'housekeeping',
        name: 'Housekeeping',
        component: () => import('@/views/front/HousekeepingService.vue'),
        meta: frontMeta
      },
      {
        path: 'vote',
        name: 'Vote',
        component: () => import('@/views/front/Vote.vue'),
        meta: frontMeta
      },
      {
        path: 'vote/:id',
        name: 'VoteDetail',
        component: () => import('@/views/front/VoteDetail.vue'),
        meta: frontMeta
      },
      {
        path: 'ai-assistant',
        name: 'AIAssistant',
        component: () => import('@/views/front/AIAssistant.vue'),
        meta: frontMeta
      },
      {
        path: 'alipay-payment',
        name: 'AlipayPayment',
        component: () => import('@/views/front/AlipayPayment.vue'),
        meta: frontMeta
      },
      {
        path: 'notification',
        name: 'Notification',
        component: () => import('@/views/front/Notification.vue'),
        meta: frontMeta
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/front/Profile.vue'),
        meta: { requiresAuth: true }
      },
      /* AI 客服白名单路径别名（与后端 normalize 一致） */
      { path: 'payment', redirect: 'property-fee' },
      /* 未实现独立停车业务；车牌等信息在个人中心维护 */
      { path: 'parking', redirect: 'profile' },
      { path: 'notice', redirect: 'home' },
      { path: 'contact', redirect: 'complaint' }
    ]
  },
  {
    path: '/orders',
    redirect: '/property-fee'
  },
  {
    path: '/login',
    component: LRLayout,
    children: [
      {
        path: '',
        name: 'Login',
        component: Login
      }
    ]
  },
  {
    path: '/register',
    component: LRLayout,
    children: [
      {
        path: '',
        name: 'Register',
        component: RegisterVue
      }
    ]
  },
  {
    path: '/forget',
    component: LRLayout,
    children: [
      {
        path: '',
        name: 'Forget',
        component: Forget
      }
    ]
  },
  {
    path: '/owner/info',
    component: LRLayout,
    children: [
      {
        path: '',
        name: 'OwnerInfo',
        component: () => import('@/views/owner/OwnerInfo.vue')
      }
    ],
    beforeEnter: (to, from, next) => {
      if (!to.query.userId) {
        next('/login')
      } else {
        next()
      }
    }
  },
  {
    path: '/404',
    name: '404',
    component: () => import('../views/404.vue')
  },
  {
    path: '/403',
    name: '403',
    component: () => import('../views/403.vue')
  },

];



const router = new VueRouter({
  mode: "history",
  base: process.env.BASE_URL,
  routes,
});
export const setRoutes = async () => {
  const userMenuListStr = sessionStorage.getItem("userMenuList")
  const currentUser = JSON.parse(sessionStorage.getItem('currentUser') || 'null')
  
  // 创建一个新的路由实例
  const newRouter = new VueRouter({
    mode: "history",
    base: process.env.BASE_URL,
    routes: routes.map(route => ({ ...route })), // 复制原始路由配置
  });
  
  // 替换当前路由器的匹配器
  router.matcher = newRouter.matcher;
  
  if (userMenuListStr) {
    const menus = transformAdminMenuList(JSON.parse(userMenuListStr));
    const flattenLeafMenus = []
    menus.forEach((item) => {
      if (item.path) flattenLeafMenus.push(item)
      if (item.children?.length) {
        item.children.forEach((sub) => {
          if (sub.path) flattenLeafMenus.push(sub)
        })
      }
    })
    let defaultBackPath = '/showView'
    if (currentUser && currentUser.role === 'KEEPER') {
      const dashMenu = flattenLeafMenus.find((m) => m.pagePath === 'HousekeeperDashboard')
      const housekeepingMenu = flattenLeafMenus.find((m) => m.pagePath === 'HousekeepingOrderManager')
      const firstLeaf = flattenLeafMenus[0]
      defaultBackPath = (dashMenu && dashMenu.path) ||
        (housekeepingMenu && housekeepingMenu.path) ||
        (firstLeaf && firstLeaf.path) ||
        '/housekeeperDashboard'
    }
    
    const currRouter = {
      path: '/',
      name: 'Layout',
      component: home,
      redirect: defaultBackPath,
      meta: {
        requiresAuth: true,
        userType: 'back'
      },
      children: [
        {
          path: "/showView",
          name: "homeView",
          component: ShowView,
          meta: {
            title: "首页",
            requiresAuth: true,
            userType: 'back'
          },
        },
      ]
    }

    menus.forEach((item) => {
      if (item.path) {
        const itemMenu = {
          path: item.path.replace("/", ""),
          name: item.name,
          component: () => import(`../views/back/${item.pagePath}.vue`),
          meta: {
            title: item.name,
            requiresAuth: true,
            userType: 'back'
          }
        };
        currRouter.children.push(itemMenu);
      } else if (item.children?.length) {
        item.children.forEach((item) => {
          if (item.path) {
            const itemMenu = {
              path: item.path.replace("/", ""),
              name: item.name,
              component: () => import(`../views/back/${item.pagePath}.vue`),
              meta: {
                title: item.name,
                requiresAuth: true,
                userType: 'back'
              }
            };
            currRouter.children.push(itemMenu);
          }
        });
      }
    });

    // 添加后台管理路由
    router.addRoute(currRouter);
  }
  
  // 返回 Promise 以确保路由加载完成
  return new Promise((resolve) => {
    router.onReady(() => {
      resolve();
    });
  });
}

// 修改路由守卫，优化判断逻辑
router.beforeEach((to, from, next) => {
  const isLoggedIn = store.getters.isLoggedIn
  const isVerifiedOwner = store.getters.isVerifiedOwner
  const currentUser = store.getters.currentUser
  
  // 如果路由不存在，跳转到404
  if (!to.matched.length) {
    next('/404');
    return;
  }
  
  if (to.matched.some(record => record.meta.requiresAuth)) {
    if (!isLoggedIn) {
      next({
        path: '/login',
        query: { redirect: to.fullPath }
      });
    } else {
      // 检查用户类型和路由要求
      if (to.meta.userType === 'front') {
        // 检查是否是业主角色
        if (currentUser.role !== 'OWNER') {
          next('/403');
        } else if (!isVerifiedOwner) {
          // 如果是业主但未验证
          next({
            path: '/owner/info',
            query: { userId: currentUser.id }
          });
        } else {
          next();
        }
      } else if (to.meta.userType === 'back' && currentUser.role !== 'ADMIN' &&currentUser.role!=='MAINTENANCE' && currentUser.role!=='KEEPER') {
        next('/403');
      } else {
        next();
      }
    }
  } else {
    next();
  }
});

// 确保在应用启动时初始化路由
setRoutes();

const originalPush = VueRouter.prototype.push
   VueRouter.prototype.push = function push(location) {
   return originalPush.call(this, location).catch(err => err)
}

export default router;
