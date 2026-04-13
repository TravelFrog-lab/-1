<template>
  <div class="app-wrapper">
    <div class="side-container">
      <SideMenu ref="sideMenu" class="side-menu-body" />
    </div>
    <div class="main-container">
      <div class="main-header">
        <HeaderBar />
      </div>
      <div class="main-content">
        <router-view @update:user="updateUser" />
      </div>
    </div>
  </div>
</template>

<script>
import HeaderBar from '../components/Header.vue'
import SideMenu from '../components/Aside.vue'

function parseBackUserFromStorage () {
  try {
    const s = sessionStorage.getItem('backUser')
    return s ? JSON.parse(s) : {}
  } catch (e) {
    return {}
  }
}

export default {
  name: 'Layout',
  data() {
    return {
      userInfo: parseBackUserFromStorage()
    };
  },
  created() {


  },
  provide() {
    return {
      userInfo: this.userInfo,
    
    };
  },
  components: { HeaderBar, SideMenu },
  computed: {

  },
  methods: {
    updateUser(user) {
      this.userInfo = user;
      this.$refs.sideMenu.refreshMenu();
    }

  }
}
</script>

<style lang="less">
@import "../assets/less/scroller-bar";

.app-wrapper {
  width: 100%;
  height: 100%;
  display: flex;
  min-width: 1200px;

  .side-container {
    width: 210px;
    height: 100vh;
    background-color: #ffffff;
    border-right: 1px solid #e6e6e6;
    flex-shrink: 0;
    display: flex;
    flex-direction: column;
    
    .side-menu-body {
      flex: 1;
      min-height: 0;
      overflow: hidden;
      display: flex;
      flex-direction: column;
    }
  }

  .main-container {
    flex: 1;
    height: 100vh;
    display: flex;
    flex-direction: column;
    background-color: #f4f6f8;
    overflow-x: hidden;
    min-width: 0;
    
    .main-header {
      height: 60px;
      width: 100%;
      background-color: #ffffff;
      border-bottom: 1px solid #e6e6e6;
      flex-shrink: 0;
      display: flex;
      align-items: stretch;
      min-width: 0;
    }

    .main-content {
      flex: 1;
      padding: 20px;
      overflow-x: hidden;
      min-width: 0;
      
      > * {
        max-width: 100%;
      }
    }
  }
}

::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-thumb {
  background: #ccc;
  border-radius: 3px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
}
</style>