<template>
  <div class="admin-layout">
    <div class="sidebar">
      <div class="logo">管理后台</div>
      <el-menu
        :default-active="activeMenu"
        class="el-menu-vertical"
        background-color="#001529"
        text-color="#fff"
        active-text-color="#409EFF"
        router
      >
        <el-menu-item index="/admin/dashboard">
          <el-icon><DataBoard /></el-icon>
          <span>数据大屏</span>
        </el-menu-item>
        <el-menu-item index="/admin/room">
          <el-icon><House /></el-icon>
          <span>阅览室管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/seat">
          <el-icon><Monitor /></el-icon>
          <span>座位管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/user">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
      </el-menu>
    </div>

    <div class="main-container">
      <div class="header">
        <div class="breadcrumb">首页 / {{ currentRouteName }}</div>
        <el-button type="info" link @click="logout">退出登录</el-button>
      </div>
      <div class="content">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useUserStore } from "../stores/user";

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const activeMenu = computed(() => route.path);
const currentRouteName = computed(() => route.name || "Dashboard");

const logout = () => {
  userStore.logout();
  router.push("/login");
};
</script>

<style scoped>
.admin-layout {
  display: flex;
  height: 100vh;
}
.sidebar {
  width: 220px;
  background: #001529;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
}
.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-size: 20px;
  font-weight: bold;
  background: #002140;
}
.el-menu-vertical {
  border-right: none;
  flex: 1;
}
.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #f0f2f5;
  overflow: hidden;
}
.header {
  height: 60px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}
.content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}
</style>
