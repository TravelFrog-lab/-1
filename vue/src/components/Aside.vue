<script>
import '../assets/iconfont.css';
import Request from '@/utils/request.js'
import { mapGetters } from 'vuex'
import {setRoutes} from "@/router";

export default {
    name: 'Aside',
    computed: {
        ...mapGetters(['currentUser', 'userMenuList']),
        displayName() {
            const u = this.currentUser
            if (!u) return '用户'
            return u.name || u.username || '用户'
        },
        userInitial() {
            const n = this.displayName
            return n ? String(n).charAt(0).toUpperCase() : 'U'
        },
        avatarUrl() {
            const u = this.currentUser
            if (!u || !u.avatar) return ''
            const a = u.avatar
            if (a.startsWith('http')) return a
            return '/api' + (a.startsWith('/') ? a : '/' + a)
        }
    },
    data() {
        return {
            path: this.$route.path
        }
    },
    methods: {
        refreshMenu(){
            Request.get("/menu/getMenuTree/"+ this.currentUser.id).then(response => {
                if (response.code === '0') {
                    this.$store.commit('SET_MENU_LIST', response.data);
                    setRoutes();
                } else {
                    this.$message({
                        type: 'error',
                        message: response.msg
                    })
                }
            })
        }
    }
}
</script>

<template>
    <div class="aside-container">
        <div v-if="currentUser" class="aside-user-block">
            <el-avatar :size="52" :src="avatarUrl" class="aside-user-avatar">
                {{ userInitial }}
            </el-avatar>
            <div class="aside-user-name" :title="displayName">{{ displayName }}</div>
        </div>
        <div class="aside-menu-wrap">
        <el-menu style="width: 100%;" 
            default-active="dashboard" 
            class="el-menu-vertical-demo" 
            router
            text-color="#5C5C5C">

            <div v-for="item in userMenuList" :key="item.id">
                <div v-if="item.path">
                    <div v-if="!item.pid">
                        <el-menu-item :index="item.path">
                            <span :class="item.icon" style="margin-right: 5px"></span>
                            <span slot="title">{{ item.name }}</span>
                        </el-menu-item>
                    </div>
                </div>
                <div v-else>
                    <el-submenu :index="item.id + ''" text-color="#fff">
                        <template slot="title">
                            <span :class="item.icon" style=""></span>
                            <span slot="title">{{ item.name }}</span>
                        </template>
                        <div v-for="subItem in item.children" :key="subItem.id">
                            <el-menu-item :index="subItem.path" style="display: flex;align-items: center;">
                                <template slot="title">
                                    <span :class="subItem.icon"></span>
                                    <span slot="title">{{ subItem.name }}</span>
                                </template>
                            </el-menu-item>
                        </div>
                    </el-submenu>
                </div>
            </div>
        </el-menu>
        </div>
    </div>
</template>

<style scoped>
.aside-user-block {
    flex-shrink: 0;
    padding: 16px 12px 14px;
    text-align: center;
    border-bottom: 1px solid #ebeef5;
    background: #fafbfc;
}

.aside-user-avatar {
    border: 2px solid rgba(26, 35, 126, 0.12);
    background: rgba(26, 35, 126, 0.06);
    color: #1a237e;
    font-weight: 600;
}

.aside-user-name {
    margin-top: 10px;
    font-size: 14px;
    font-weight: 600;
    color: #303133;
    line-height: 1.3;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    max-width: 100%;
}

.aside-menu-wrap {
    flex: 1;
    min-height: 0;
    overflow-y: auto;
    overflow-x: hidden;
}

.aside-container {
    flex: 1;
    min-height: 0;
    height: auto;
    overflow: hidden;
    display: flex;
    flex-direction: column;
}

.aside-menu-wrap::-webkit-scrollbar {
    width: 6px;
}

.aside-menu-wrap::-webkit-scrollbar-thumb {
    background: rgba(26, 35, 126, 0.2);
    border-radius: 3px;
}

.aside-menu-wrap::-webkit-scrollbar-track {
    background: transparent;
}

.el-menu {
    border-right: none;
    background-color: #ffffff;
    text-align: left;
    padding: 8px 0;
}

.el-menu-item {
    background-color: #FFFFFF !important;
    margin-bottom: 5px;
    height: 45px;
    line-height: 45px;
    font-size: 15px;
    min-width: 95% !important;
    border-radius: 4px;
    margin: 4px 8px;
}

.el-menu-item.is-active {
    color: #1a237e;
    background: rgba(26, 35, 126, 0.1) !important;
    border-right: 4px solid #1a237e;
}

.el-menu-item:hover {
    background: rgba(26, 35, 126, 0.05) !important;
}

.el-submenu ::v-deep .el-submenu__title {
    font-size: 15px !important;
    line-height: 45px !important;
    background-color: #ffffff !important;
    height: 45px !important;
    margin: 4px 8px;
    border-radius: 4px;
}

.el-submenu ::v-deep .el-submenu__title:hover {
    background: rgba(26, 35, 126, 0.05) !important;
}

.el-submenu /deep/ .el-menu--inline {
    background: #ffffff !important;
    padding: 0 8px;
}

.el-submenu .el-menu--inline .el-menu-item {
    background-color: #ffffff !important;
    height: 40px !important;
    line-height: 40px !important;
    margin: 4px 0 !important;
    padding-left: 45px !important;
}

.el-submenu .el-menu--inline .el-menu-item:hover {
    background: rgba(26, 35, 126, 0.05) !important;
}

::v-deep .el-submenu .el-menu--inline .el-menu-item.is-active {
    background: rgba(26, 35, 126, 0.1) !important;
    color: #1a237e;
    border-right: 4px solid #1a237e;
}

.icon-hover {
    transition: transform 0.1s ease;
    /* 平滑过渡效果 */
}

/* 悬浮时的样式 */
.el-menu-item:hover .icon-hover /deep/ {
    transform: scale(2) !important;
    /* 放大到1.2倍 */
}
</style>