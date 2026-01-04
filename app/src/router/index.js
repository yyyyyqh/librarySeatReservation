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
  // 预留：学生端路由
  {
    path: "/student",
    component: () => import("../views/StudentLayout.vue"), // 稍后创建
    children: [
      { path: "home", component: () => import("../views/student/Home.vue") },

      {
        path: "profile",
        component: () => import("../views/student/Profile.vue"),
      },
    ],
  },
  // 预留：管理端路由
  {
    path: "/admin",
    component: () => import("../views/AdminLayout.vue"),
    redirect: "/admin/dashboard", // 默认跳到 dashboard
    children: [
      {
        path: "dashboard",
        name: "数据大屏",
        component: () => import("../views/admin/Dashboard.vue"),
      },
      // 暂时用 Dashboard 占位，之后我们一个个实现
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

  if (to.path !== "/login" && !userStore.userInfo) {
    next("/login");
  } else {
    next();
  }
});

export default router;
