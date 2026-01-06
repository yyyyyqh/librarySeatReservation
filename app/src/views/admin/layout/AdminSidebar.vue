<template>
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
</template>

<script setup>
import { computed } from "vue";
import { useRoute } from "vue-router";
// 引入需要的图标
import { DataBoard, House, Monitor, User } from "@element-plus/icons-vue";

const route = useRoute();
const activeMenu = computed(() => route.path);
</script>

<style scoped>
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
</style>
