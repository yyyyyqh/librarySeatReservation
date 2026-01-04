import { createRouter, createWebHistory } from "vue-router";
import { useUserStore } from "../stores/user";

const routes = [
  {
    path: "/login",
    name: "Login",
    component: () => import("../views/Login.vue"),
  },
  {
    path: "/",
    redirect: "/login",
  },
  // 预留：学生端路由
  {
    path: "/student",
    component: () => import("../views/StudentLayout.vue"), // 稍后创建
    children: [
      { path: "home", component: () => import("../views/student/Home.vue") },
    ],
  },
  // 预留：管理端路由
  {
    path: "/admin",
    component: () => import("../views/AdminLayout.vue"), // 稍后创建
    children: [
      {
        path: "dashboard",
        component: () => import("../views/admin/Dashboard.vue"),
      },
    ],
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

// 路由守卫：防止未登录直接访问
router.beforeEach((to, from, next) => {
  const userStore = useUserStore();
  userStore.initUser(); // 尝试从本地恢复用户信息

  if (to.path !== "/login" && !userStore.userInfo) {
    next("/login");
  } else {
    next();
  }
});

export default router;
