<template>
  <div class="nav-header">
    <div class="container nav-container">
      <div class="brand" @click="$router.push('/student/home')">
        <el-icon :size="24" class="logo-icon"><Reading /></el-icon>
        <span class="brand-text">智图·预约</span>
      </div>

      <el-menu
        :default-active="activePath"
        mode="horizontal"
        class="nav-menu"
        :ellipsis="false"
        router
      >
        <el-menu-item index="/student/home">选座大厅</el-menu-item>
        <el-menu-item index="/student/profile">个人中心</el-menu-item>
      </el-menu>

      <div class="user-actions">
        <el-dropdown trigger="click" @command="handleCommand">
          <div class="avatar-wrapper">
            <el-avatar :size="32" :src="avatarUrl" class="user-avatar" />
            <span class="username">{{
              userStore.userInfo?.realName || "同学"
            }}</span>
            <el-icon><CaretBottom /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">我的信用</el-dropdown-item>
              <el-dropdown-item command="logout" divided style="color: #f56c6c"
                >退出登录</el-dropdown-item
              >
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from "vue";
import { useRouter, useRoute } from "vue-router";
import { useUserStore } from "@/stores/user";
import { Reading, CaretBottom } from "@element-plus/icons-vue"; // 记得引入图标

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();

const activePath = computed(() => route.path);
const avatarUrl =
  "https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png";

const handleCommand = (cmd) => {
  if (cmd === "logout") {
    userStore.logout();
    router.push("/login");
  } else if (cmd === "profile") {
    router.push("/student/profile");
  }
};
</script>

<style scoped>
/* 复用 container 样式以保持对齐 */
.container {
  width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.nav-header {
  background: white;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  height: 60px;
  position: sticky;
  top: 0;
  z-index: 100;
}

.nav-container {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  color: #409eff;
}
.brand-text {
  font-size: 20px;
  font-weight: bold;
  letter-spacing: 1px;
}

.nav-menu {
  border-bottom: none !important;
  flex: 1;
  justify-content: center;
  background: transparent;
}

.user-actions {
  display: flex;
  align-items: center;
}

.avatar-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 5px 10px;
  border-radius: 20px;
  transition: background 0.3s;
}
.avatar-wrapper:hover {
  background: #f0f2f5;
}
.username {
  font-size: 14px;
  color: #606266;
}
</style>
