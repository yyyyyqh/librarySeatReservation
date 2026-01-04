<template>
  <div class="student-layout">
    <div class="student-header">
      <div class="brand" @click="$router.push('/student/home')">
        📚 图书馆预约系统
      </div>
      <div class="user-actions">
        <span class="welcome-text"
          >你好，{{ userStore.userInfo?.realName }}</span
        >
        <el-button link type="primary" @click="$router.push('/student/home')"
          >首页</el-button
        >
        <el-button link type="primary" @click="$router.push('/student/profile')"
          >个人中心</el-button
        >
        <el-divider direction="vertical" />
        <el-button link type="danger" @click="handleLogout">退出</el-button>
      </div>
    </div>

    <div class="student-content">
      <router-view />
    </div>
  </div>
</template>

<script setup>
import { useRouter } from "vue-router";
import { useUserStore } from "../stores/user";

const router = useRouter();
const userStore = useUserStore();

const handleLogout = () => {
  userStore.logout();
  router.push("/login");
};
</script>

<style scoped>
.student-layout {
  min-height: 100vh;
  background-color: #f5f7fa;
  display: flex;
  flex-direction: column;
}

.student-header {
  height: 60px;
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 40px;
  z-index: 10;
}

.brand {
  font-size: 18px;
  font-weight: bold;
  color: #409eff;
  cursor: pointer;
}

.user-actions {
  display: flex;
  align-items: center;
  gap: 15px;
}

.welcome-text {
  font-size: 14px;
  color: #606266;
  margin-right: 10px;
}

.student-content {
  flex: 1; /* 撑满剩余高度 */
  width: 100%;
  max-width: 1200px; /* 限制最大宽度，让学生端看起来不像后台那么宽 */
  margin: 0 auto;
  padding: 20px;
}
</style>
