/**
 * 管理端侧边栏菜单整理：
 * - 系统配置：轮播图管理提升为顶级；隐藏菜单/图标配置等
 * - 个人工作台：合并为「个人中心」单菜单，并置于列表最末；隐藏「安全设置」
 *
 * 仅影响展示与 sessionStorage 中的菜单数据；路由仍由 setRoutes 根据转换后的列表注册。
 * - 维修工：不展示「投诉建议管理」（由管理员处理）
 */

const HIDDEN_MENU_NAMES = new Set([
  '菜单管理',
  '菜单配置',
  '图标库',
  '图标管理',
  '安全设置',
  '公告管理',
  '消息通知',
  '社区动态',
  '新闻管理'
])

/** 未实现的业务页面（与菜单 pagePath 一致，侧边栏与动态路由均不注册）；停车服务未做，仅业主端保留车牌等信息 */
const HIDDEN_ADMIN_PAGE_PATHS = new Set([
  'ParkingSpaceManager',
  'ParkingFeeManager',
  'PrivateParkingManager',
  'AnnouncementManager', // 公告管理
  'NotificationManager' // 消息通知
])

function isSystemConfigGroup (item) {
  if (!item || !item.name) return false
  const n = String(item.name).trim()
  return n === '系统配置' || n.includes('系统配置')
}

function isPersonalWorkbenchGroup (item) {
  if (!item || !item.name) return false
  const n = String(item.name).trim()
  return n.includes('个人工作台')
}

function isCarouselMenu (item) {
  if (!item) return false
  if (item.pagePath === 'CarouselManager') return true
  if (item.name && String(item.name).includes('轮播')) return true
  return false
}

function isBasicInfoMenu (item) {
  if (!item) return false
  if (item.pagePath === 'PersonInfo') return true
  const name = item.name && String(item.name)
  return name && (name.includes('基本信息') || name.includes('个人信息'))
}

function isPersonalProfilePage (item) {
  return item && item.pagePath === 'PersonInfo'
}

/** 信息发布分组（业主端投票由管理员在此维护） */
function isInfoPublishGroup (item) {
  if (!item || !item.name) return false
  const n = String(item.name).trim()
  return n.includes('信息发布')
}

/** 社区服务分组：子菜单在侧边栏与「信息发布」一致，提升为顶级项 */
function isCommunityServiceGroup (item) {
  if (!item || !item.name) return false
  const n = String(item.name).trim()
  return n.includes('社区服务')
}

function pushChildrenAsTopLevel (out, children) {
  if (!Array.isArray(children) || !children.length) return
  for (const c of children) {
    out.push({ ...c, pid: null, children: [] })
  }
}

function hasVoteManagerInChildren (children) {
  if (!Array.isArray(children)) return false
  return children.some((c) => c && c.pagePath === 'VoteManager')
}

function isVoteManagerMenu (item) {
  return item && item.pagePath === 'VoteManager'
}

function hasHomeMenu (menus) {
  if (!Array.isArray(menus)) return false
  return menus.some((m) => m && m.pagePath === 'ShowView')
}

function hasHousekeeperDashboardMenu (menus) {
  if (!Array.isArray(menus)) return false
  return menus.some((m) => m && m.pagePath === 'HousekeeperDashboard')
}

/** 家政人员端：数据看板（独立页，非管理员 ShowView） */
function syntheticHousekeeperDashboardMenu () {
  return {
    id: -987654322,
    name: '数据看板',
    path: '/housekeeperDashboard',
    icon: 'el-icon-data-board',
    description: '家政人员数据看板',
    pid: null,
    pagePath: 'HousekeeperDashboard',
    sortNum: 0,
    role: 'KEEPER',
    children: []
  }
}

function syntheticHomeMenu () {
  const currentUser = JSON.parse(sessionStorage.getItem('currentUser') || 'null')
  const isKeeper = currentUser && currentUser.role === 'KEEPER'
  return {
    id: -987654320,
    name: '首页',
    path: '/showView',
    icon: 'el-icon-data-board',
    description: '系统首页',
    pid: null,
    pagePath: 'ShowView',
    sortNum: 0,
    role: isKeeper ? 'ADMIN,MAINTENANCE' : 'ADMIN,MAINTENANCE,KEEPER',
    children: []
  }
}

/** 与动态路由、VoteManager.vue 一致；id 为负数避免与库内主键冲突 */
function syntheticVoteManagerMenu (parentId) {
  return {
    id: -987654321,
    name: '投票表决管理',
    path: '/voteManager',
    icon: 'el-icon-s-data',
    description: '业主端投票主题、选项与结果管理',
    pid: parentId,
    pagePath: 'VoteManager',
    sortNum: 888,
    role: 'ADMIN',
    children: []
  }
}

function shouldHideLeaf (item, isMaintenance = false) {
  if (!item) return false
  if (isMaintenance && item.pagePath === 'ComplaintManager') return true
  if (item.pagePath && HIDDEN_ADMIN_PAGE_PATHS.has(item.pagePath)) return true
  if (item.pagePath === 'NewsManager') return true
  if (!item.name) return false
  return HIDDEN_MENU_NAMES.has(String(item.name).trim())
}

/**
 * @param {Array|null|undefined} menuList
 * @returns {Array|null|undefined}
 */
export function transformAdminMenuList (menuList) {
  if (!Array.isArray(menuList) || menuList.length === 0) {
    return menuList
  }
  const currentUser = JSON.parse(sessionStorage.getItem('currentUser') || 'null')
  const isMaintenance = currentUser && currentUser.role === 'MAINTENANCE'

  const out = []
  /** @type {object|null} */
  let personalCenterFromGroup = null
  /** @type {object|null} */
  let personalCenterFromLeaf = null

  for (const item of menuList) {
    const hasChildren = item.children && item.children.length > 0
    const isLeafTop = !!item.path && !hasChildren

    // 顶级带子菜单
    if (!item.path && hasChildren) {
      if (isPersonalWorkbenchGroup(item)) {
        const basic = item.children.find(isBasicInfoMenu)
        if (basic) {
          personalCenterFromGroup = {
            ...basic,
            name: '个人中心',
            pid: null,
            children: []
          }
        }
        continue
      }

      if (isSystemConfigGroup(item)) {
        const carousel = item.children.find(isCarouselMenu)
        const rest = item.children.filter(
          (c) => !isCarouselMenu(c) && !shouldHideLeaf(c, isMaintenance)
        )

        if (carousel) {
          out.push({
            ...carousel,
            pid: null,
            children: []
          })
        }
        if (rest.length) {
          out.push({ ...item, children: rest })
        }
        continue
      }

      if (isInfoPublishGroup(item)) {
        let filteredChildren = item.children.filter((c) => !shouldHideLeaf(c, isMaintenance))
        if (isMaintenance) {
          filteredChildren = filteredChildren.filter((c) => !isVoteManagerMenu(c))
        }
        if (!hasVoteManagerInChildren(filteredChildren)) {
          if (!isMaintenance) {
            filteredChildren = [...filteredChildren, syntheticVoteManagerMenu(item.id)]
          }
        }
        pushChildrenAsTopLevel(out, filteredChildren)
        continue
      }

      if (isCommunityServiceGroup(item)) {
        const filteredChildren = item.children.filter((c) => !shouldHideLeaf(c, isMaintenance))
        pushChildrenAsTopLevel(out, filteredChildren)
        continue
      }

      const filteredChildren = item.children.filter((c) => !shouldHideLeaf(c, isMaintenance))
      if (filteredChildren.length) {
        out.push({ ...item, children: filteredChildren })
      }
      continue
    }

    // 顶级单菜单项
    if (isLeafTop || item.path) {
      if (shouldHideLeaf(item, isMaintenance)) continue
      if (isPersonalProfilePage(item)) {
        personalCenterFromLeaf = {
          ...item,
          name: '个人中心',
          pid: null
        }
        continue
      }
      out.push(item)
      continue
    }

    out.push(item)
  }

  if (personalCenterFromGroup) {
    out.push(personalCenterFromGroup)
  } else if (personalCenterFromLeaf) {
    out.push(personalCenterFromLeaf)
  }

  const shouldInjectHome = !currentUser || currentUser.role !== 'KEEPER'

  // 兜底：首页在管理员/后勤侧边栏首位（家政端先隐藏 ShowView）
  if (shouldInjectHome && !hasHomeMenu(out)) {
    out.unshift(syntheticHomeMenu())
  }

  // 家政人员端：侧边栏首位「数据看板」
  if (currentUser && currentUser.role === 'KEEPER' && !hasHousekeeperDashboardMenu(out)) {
    out.unshift(syntheticHousekeeperDashboardMenu())
  }

  return out
}
