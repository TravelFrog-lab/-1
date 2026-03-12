<template>
  <div class="notification">
    <nav-header></nav-header>
    <div class="content-wrapper">
      <h2>通知中心</h2>
      <div class="notification-content">
        <p>功能开发中...</p>
      </div>
    </div>
    <page-footer></page-footer>
  </div>
</template>

<script>
import NavHeader from '@/components/front/NavHeader.vue'
import PageFooter from '@/components/front/PageFooter.vue'

export default {
  name: 'Notification',
  components: {
    NavHeader,
    PageFooter
  }
}
</script>

<style scoped>
.notification {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.content-wrapper {
  flex: 1;
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
}

h2 {
  margin-bottom: 20px;
  padding-left: 10px;
  border-left: 4px solid #409EFF;
}
</style> 