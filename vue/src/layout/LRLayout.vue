<template>
    <div id="login-container">
        <div class="system-info">
            <div class="logo-area">
                <i class="el-icon-house"></i>
                <h1>小区物业管理系统</h1>
            </div>
        </div>
        <div class="content-area">
            <router-view v-slot="{ Component }">
                <transition name="fade" mode="out-in">
                    <component :is="Component" />
                </transition>
            </router-view>
        </div>
        <div class="decoration-area">
            <div class="circle circle-1"></div>
            <div class="circle circle-2"></div>
            <div class="circle circle-3"></div>
        </div>
    </div>
</template>

<style scoped>
#login-container {
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    min-height: 100vh;
    background: linear-gradient(135deg, #0c2340 0%, #1e4976 100%);
    position: relative;
    overflow: hidden;
}

.system-info {
    text-align: center;
    color: white;
    margin-bottom: 2rem;
    z-index: 1;
}

.logo-area {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 1rem;
}

.logo-area i {
    font-size: 3rem;
    background: linear-gradient(45deg, #64b5f6, #2196f3);
    -webkit-background-clip: text;
    color: transparent;
    padding: 0.5rem;
}

.logo-area h1 {
    font-size: 2.5rem;
    font-weight: 600;
    margin: 0;
    background: linear-gradient(45deg, #ffffff, #e3f2fd);
    -webkit-background-clip: text;
    color: transparent;
}

.content-area {
    position: relative;
    z-index: 1;
    width: 100%;
    display: flex;
    justify-content: center;
}

.decoration-area {
    position: absolute;
    width: 100%;
    height: 100%;
    overflow: hidden;
}

.circle {
    position: absolute;
    border-radius: 50%;
    background: linear-gradient(45deg, rgba(33, 150, 243, 0.1), rgba(33, 150, 243, 0.05));
}

.circle-1 {
    width: 300px;
    height: 300px;
    top: -150px;
    right: -150px;
}

.circle-2 {
    width: 500px;
    height: 500px;
    bottom: -250px;
    left: -250px;
}

.circle-3 {
    width: 200px;
    height: 200px;
    top: 40%;
    right: 10%;
}

/* 过渡动画 */
.fade-enter-active,
.fade-leave-active {
    transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
    opacity: 0;
}

@media screen and (max-width: 768px) {
    .logo-area h1 {
        font-size: 2rem;
    }
    
    .circle-1 {
        width: 200px;
        height: 200px;
        top: -100px;
        right: -100px;
    }
    
    .circle-2 {
        width: 300px;
        height: 300px;
        bottom: -150px;
        left: -150px;
    }
    
    .circle-3 {
        width: 150px;
        height: 150px;
    }
}
</style>