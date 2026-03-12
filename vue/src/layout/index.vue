<template>
  <div class="app-wrapper">
    <div class="side-container">
      <div class="logo">
        <div class="logo-content">
          <div class="logo-left">
            <div class="logo-icon">
              <svg class="icon" viewBox="0 0 1024 1024" width="32" height="32">
                <path d="M512 128l384 298v470H128V426l384-298zm0 75L213 459v352h598V459L512 203zm-128 256h256v85H384v-85zm0 171h256v85H384v-85z" fill="#ffffff"/>
              </svg>
            </div>
          </div>
          <div class="logo-right">
            <span class="logo-text">邻聚里</span>
            <span class="logo-text-en">NEIGHBORHOOD CONNECT</span>
          </div>
        </div>
      </div>
      <SideMenu ref="sideMenu" />
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
import Logo from '../assets/logo.png'

export default {
  name: 'Layout',
  data() {
    return {
      userInfo: JSON.parse(sessionStorage.getItem("backUser") || {}),
      logoUrl: Logo
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
    
    .logo {
      height: 60px;
      background: linear-gradient(135deg, #f5f7fa 0%, #e4ecf7 100%);
      display: flex;
      align-items: center;
      padding: 0 15px;
      border-bottom: 1px solid #e6e6e6;
      flex-shrink: 0;
      
      .logo-content {
        display: flex;
        flex-direction: row;
        align-items: center;
        width: 100%;
        
        .logo-left {
          margin-right: 12px;
          
          .logo-icon {
            background: #1a237e;
            width: 40px;
            height: 40px;
            border-radius: 8px;
            display: flex;
            align-items: center;
            justify-content: center;
            box-shadow: 0 2px 8px rgba(26, 35, 126, 0.15);
            
            svg {
              width: 24px;
              height: 24px;
              fill: #ffffff;
            }
          }
        }
        
        .logo-right {
          display: flex;
          flex-direction: column;
          
          .logo-text {
            color: #1a237e;
            font-size: 18px;
            font-weight: 600;
            letter-spacing: 1px;
            line-height: 1;
            margin-bottom: 4px;
          }

          .logo-text-en {
            color: #666666;
            font-size: 10px;
            letter-spacing: 1px;
            line-height: 1;
          }
        }
      }
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
      background-color: #ffffff;
      border-bottom: 1px solid #e6e6e6;
      flex-shrink: 0;
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