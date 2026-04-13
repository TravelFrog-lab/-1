<template>
  <div class="news-redirect-hint">正在跳转到「社区动态」管理页…</div>
</template>

<script>
/**
 * 社区动态与 CarouselManager 为同一页面，保留本组件以便旧菜单路由仍可注册并自动跳转。
 */
function findPathByPagePath (menus, pagePath) {
  if (!Array.isArray(menus)) return null
  for (const m of menus) {
    if (m.pagePath === pagePath && m.path) {
      return m.path.startsWith('/') ? m.path : '/' + m.path
    }
    if (m.children && m.children.length) {
      const p = findPathByPagePath(m.children, pagePath)
      if (p) return p
    }
  }
  return null
}

export default {
  name: 'NewsManager',
  created () {
    let menus = []
    try {
      menus = JSON.parse(sessionStorage.getItem('userMenuList') || '[]')
    } catch (e) {
      menus = []
    }
    const path = findPathByPagePath(menus, 'CarouselManager')
    if (path) {
      this.$router.replace({ path, query: { ...this.$route.query, tab: 'news' } })
    } else {
      this.$router.replace({ path: '/showView' })
    }
  }
}
</script>

<style scoped>
.news-redirect-hint {
  padding: 40px;
  text-align: center;
  color: #909399;
}
</style>
