import { defineStore } from "pinia";
import { ref } from "vue";
import request from "../utils/request";

export const useUserStore = defineStore("user", () => {
  const userInfo = ref(null); // 存储用户对象

  // 登录动作
  const login = async (loginForm) => {
    // 调用后端接口 /api/user/login
    const data = await request.post("/api/user/login", loginForm);
    userInfo.value = data; // 保存用户信息
    // 这里可以将用户信息存入 localStorage 防止刷新丢失
    localStorage.setItem("user", JSON.stringify(data));
    return data;
  };

  // 退出登录
  const logout = () => {
    userInfo.value = null;
    localStorage.removeItem("user");
  };

  // 初始化（防止刷新后数据丢失）
  const initUser = () => {
    const stored = localStorage.getItem("user");
    if (stored) {
      userInfo.value = JSON.parse(stored);
    }
  };

  return { userInfo, login, logout, initUser };
});
