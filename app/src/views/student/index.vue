<template>
  <div class="student-layout">
    <StudentHeader />

    <div class="container main-content">
      <router-view v-slot="{ Component }">
        <transition name="fade-transform" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </div>

    <StudentFooter />
  </div>
</template>

<script setup>
import StudentHeader from "@/views/student/layout/StudentHeader.vue";
import StudentFooter from "@/views/student/layout/StudentFooter.vue";
</script>

<style scoped>
/* 全局布局背景 */
.student-layout {
  min-height: 100vh;
  background-color: #f5f7fa;
  display: flex;
  flex-direction: column;
}

/* 限制内容宽度的容器 (保持与 Header 内部的 container 宽度一致) */
.container {
  width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 内容区域自动撑开 */
.main-content {
  flex: 1;
  padding-top: 20px;
  padding-bottom: 20px;
}

/* 简单的淡入动画 */
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.3s;
}
.fade-transform-enter-from {
  opacity: 0;
  transform: translateY(-10px);
}
.fade-transform-leave-to {
  opacity: 0;
  transform: translateY(10px);
}
</style>
