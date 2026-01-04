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
  {
    path: "/register",
    name: "Register",
    component: () => import("../views/Register.vue"),
  },
  // 学生端路由
  {
    path: "/student",
    meta: { requiresAuth: true, role: 0 },
    component: () => import("../views/StudentLayout.vue"),
    children: [
      { path: "home", component: () => import("../views/student/Home.vue") },

      {
        path: "profile",
        component: () => import("../views/student/Profile.vue"),
      },
    ],
  },
  // 管理端路由
  {
    path: "/admin",
    meta: { requiresAuth: true, role: 2 }, // 标记权限
    component: () => import("../views/AdminLayout.vue"),
    redirect: "/admin/dashboard", // 默认跳到 dashboard
    children: [
      {
        path: "dashboard",
        name: "数据大屏",
        component: () => import("../views/admin/Dashboard.vue"),
      },
      {
        path: "room",
        name: "阅览室管理",
        component: () => import("../views/admin/RoomManagement.vue"),
      },
      {
        path: "seat",
        name: "座位管理",
        component: () => import("../views/admin/SeatManagement.vue"),
      },
      {
        path: "/student",
        component: () => import("../views/StudentLayout.vue"), // 假设您有这个布局文件
        children: [
          {
            path: "home",
            component: () => import("../views/student/Home.vue"),
          },
        ],
      },
      {
        path: "user",
        name: "用户管理",
        component: () => import("../views/admin/UserManagement.vue"),
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
  const user = userStore.userInfo;

  // 如果去登录/注册页，直接放行
  if (to.path === "/login" || to.path === "/register") {
    next();
    return;
  }

  // 未登录，踢回登录页
  if (!user) {
    next("/login");
    return;
  }
  // 简单的权限拦截 (防止学生手动输入 /admin/dashboard)
  if (to.path.startsWith("/admin") && user.identityType !== 2) {
    next("/student/home"); // 学生想进后台？踢回首页
    return;
  }
  next();
});

export default router;
