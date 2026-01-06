<template>
  <div class="admin-layout">
    <AdminSidebar />

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
import { useUserStore } from "@/stores/user";
import AdminSidebar from "@/views/admin/layout/AdminSidebar.vue";

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

// 侧边栏的高亮逻辑移走了，这里只需要面包屑名称
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

/* 侧边栏样式移入 AdminSidebar.vue */

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
