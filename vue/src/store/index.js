import Vue from 'vue'
import Vuex from 'vuex'
import request from '@/utils/request'
import { transformAdminMenuList } from '@/utils/adminMenuTransform'

Vue.use(Vuex)

function readStoredMenuList () {
  try {
    const raw = sessionStorage.getItem('userMenuList')
    if (!raw) return null
    const parsed = JSON.parse(raw)
    const transformed = transformAdminMenuList(parsed)
    if (JSON.stringify(parsed) !== JSON.stringify(transformed)) {
      sessionStorage.setItem('userMenuList', JSON.stringify(transformed))
    }
    return transformed
  } catch {
    return null
  }
}

export default new Vuex.Store({
  state: {
    currentUser: JSON.parse(sessionStorage.getItem('currentUser')) || null,
    userMenuList: readStoredMenuList(),
    userType: sessionStorage.getItem('userType') || null, // 'front' or 'back'
    ownerInfo: JSON.parse(sessionStorage.getItem('ownerInfo')) || null,
    ownerStatus: sessionStorage.getItem('ownerStatus') || 'unknown' // 'verified', 'unverified', 'unknown'
  },
  getters: {
    isLoggedIn: state => !!state.currentUser,
    currentUser: state => state.currentUser,
    userMenuList: state => state.userMenuList,
    userType: state => state.userType,
    ownerInfo: state => state.ownerInfo,
    isVerifiedOwner: state => state.ownerStatus === 'verified',
    ownerStatus: state => state.ownerStatus
  },
  mutations: {
    SET_CURRENT_USER(state, user) {
      state.currentUser = user
      if (user) {
        sessionStorage.setItem('currentUser', JSON.stringify(user))
      } else {
        sessionStorage.removeItem('currentUser')
      }
    },
    SET_MENU_LIST(state, menuList) {
      const next = menuList ? transformAdminMenuList(menuList) : null
      state.userMenuList = next
      if (next) {
        sessionStorage.setItem('userMenuList', JSON.stringify(next))
      } else {
        sessionStorage.removeItem('userMenuList')
      }
    },
    SET_USER_TYPE(state, type) {
      state.userType = type
      if (type) {
        sessionStorage.setItem('userType', type)
      } else {
        sessionStorage.removeItem('userType')
      }
    },
    LOGOUT(state) {
      state.currentUser = null
      state.userMenuList = null
      state.userType = null
      state.ownerInfo = null
      state.ownerStatus = 'unknown'
      sessionStorage.clear()
    },
    SET_OWNER_INFO(state, info) {
      state.ownerInfo = info
      if (info) {
        sessionStorage.setItem('ownerInfo', JSON.stringify(info))
        state.ownerStatus = 'verified'
        sessionStorage.setItem('ownerStatus', 'verified')
      } else {
        sessionStorage.removeItem('ownerInfo')
        state.ownerStatus = 'unverified'
        sessionStorage.setItem('ownerStatus', 'unverified')
      }
    }
  },
  actions: {
    async login({ commit }, userData) {
      if (userData.role === 'OWNER') {
        commit('SET_CURRENT_USER', userData)
        commit('SET_USER_TYPE', 'front')
        
        try {
          const response = await request.get(`/owner/user/${userData.id}`)
          
          if (response.code === '0' && response.data && Object.keys(response.data).length > 0) {
            if (response.data.status === 'DISABLED') {
              commit('SET_OWNER_INFO', null)
              return {
                error: true,
                message: '该账号审核暂未通过，请耐心等待'
              }
            }
            commit('SET_OWNER_INFO', response.data)
            return { success: true }
          } else {
            commit('SET_OWNER_INFO', null)
            return { 
              needVerification: true,
              userId: userData.id,
              message: '您还不是已认证业主，请先完善业主信息'
            }
          }
        } catch (error) {
          if (error.response && error.response.status === 404) {
            commit('SET_OWNER_INFO', null)
            return { 
              needVerification: true,
              userId: userData.id,
              message: '您还不是已认证业主，请先完善业主信息'
            }
          } else {
            console.error('获取业主信息失败:', error)
            commit('SET_OWNER_INFO', null)
            return { 
              error: true,
              message: '获取业主信息失败，请刷新页面重试'
            }
          }
        }
      } else {
        commit('SET_CURRENT_USER', userData)
        commit('SET_MENU_LIST', userData.menuList)
        commit('SET_USER_TYPE', 'back')
        return { success: true }
      }
    },
    logout({ commit }) {
      commit('LOGOUT')
    }
  },
  modules: {
  }
})
