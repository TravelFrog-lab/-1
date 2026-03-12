import Vue from 'vue'
import VueRouter, { RouteConfig } from 'vue-router'
import home from '../layout/index.vue';


import ShowView from '@/views/back/showView.vue';


import Login from '@/views/Login.vue';
import LRLayout from '@/layout/LRLayout.vue';
import RegisterVue from '@/views/RegisterVue.vue';


import Menu from '@/views/back/Menu.vue';

import Forget from '@/views/Forget.vue';
import Home from '@/views/front/Home.vue'
import store from '../store'

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    redirect: '/home'
  },
  {
    path: '/home',
    name: 'Home',
    component: Home,
    meta: {
      requiresAuth: true,
      userType: 'front'
    }
  },
  // 新闻详情页路由
  {
    path: '/news/:id',
    name: 'NewsDetail',
    component: () => import('@/views/front/NewsDetail.vue'),
    meta: {
      requiresAuth: true,
      userType: 'front'
    }
  },
  // 业务中心相关路由
  {
    path: '/property-fee',
    name: 'PropertyFee',
    component: () => import('@/views/front/PropertyFee.vue'),
    meta: {
      requiresAuth: true,
      userType: 'front'
    }
  },
  {
    path: '/repair',
    name: 'Repair',
    component: () => import('@/views/front/Repair.vue'),
    meta: {
      requiresAuth: true,
      userType: 'front'
    }
  },
  {
    path: '/complaint',
    name: 'Complaint',
    component: () => import('@/views/front/Complaint.vue'),
    meta: {
      requiresAuth: true,
      userType: 'front'
    }
  },
  {
    path: '/volunteer',
    name: 'Volunteer',
    component: () => import('@/views/front/Volunteer.vue'),
    meta: {
      requiresAuth: true,
      userType: 'front'
    }
  },
  {
    path: '/pet-sitting',
    name: 'PetSitting',
    component: () => import('@/views/front/PetSitting.vue'),
    meta: {
      requiresAuth: true,
      userType: 'front'
    }
  },
  {
    path: '/housekeeping',
    name: 'Housekeeping',
    component: () => import('@/views/front/HousekeepingService.vue'),
    meta: {
      requiresAuth: true,
      userType: 'front'
    }
  },
  {
    path: '/parking',
    name: 'Parking',
    component: () => import('@/views/front/Parking.vue'),
    meta: {
      requiresAuth: true,
      userType: 'front'
    }
  },
  {
    path: '/vote',
    name: 'Vote',
    component: () => import('@/views/front/Vote.vue'),
    meta: {
      requiresAuth: true,
      userType: 'front'
    }
  },
  {
    path: '/vote/:id',
    name: 'VoteDetail',
    component: () => import('@/views/front/VoteDetail.vue'),
    meta: {
      requiresAuth: true,
      userType: 'front'
    }
  },
  {
    path: '/ai-assistant',
    name: 'AIAssistant',
    component: () => import('@/views/front/AIAssistant.vue'),
    meta: {
      requiresAuth: true,
      userType: 'front'
    }
  },
  // 支付宝支付页面路由
  {
    path: '/alipay-payment',
    name: 'AlipayPayment',
    component: () => import('@/views/front/AlipayPayment.vue'),
    meta: {
      requiresAuth: true,
      userType: 'front'
    }
  },
  // 我的订单（支付结果页「查看我的订单」跳转，重定向到物业费列表）
  {
    path: '/orders',
    redirect: '/property-fee'
  },
  // 通知页面路由
  {
    path: '/notification',
    name: 'Notification',
    component: () => import('@/views/front/Notification.vue'),
    meta: {
      requiresAuth: true,
      userType: 'front'
    }
  },
  // 个人中心路由
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/front/Profile.vue'),
    meta: {
      requiresAuth: true
    }
  },
  {
    path: "/",
    name: "LRLayout",
    redirect: '/login',  
    component:LRLayout,
    children: [
      {
        path: "/login",
        name: "Login",
        component: Login,
      },
      {
        path: "/register",
        name: "Register",
        component: RegisterVue
      },

      {
        path:'/forget',
        name: 'Forget',
        component: Forget
      },
      {
        path: '/owner/info',
        name: 'OwnerInfo',
        component: () => import('@/views/owner/OwnerInfo.vue'),
        beforeEnter: (to, from, next) => {
          // 检查是否有 userId 参数
          if (!to.query.userId) {
            next('/login');
          } else {
            next();
          }
        }
      }
 
    ]
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
  
  // 创建一个新的路由实例
  const newRouter = new VueRouter({
    mode: "history",
    base: process.env.BASE_URL,
    routes: routes.map(route => ({ ...route })), // 复制原始路由配置
  });
  
  // 替换当前路由器的匹配器
  router.matcher = newRouter.matcher;
  
  if (userMenuListStr) {
    const menus = JSON.parse(userMenuListStr);
    
    const currRouter = {
      path: '/',
      name: 'Layout',
      component: home,
      redirect: '/showView',
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
        {
          path: '/menu',
          name: 'Menu',
          component: Menu,
          meta: {
            title: "菜单管理",
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
