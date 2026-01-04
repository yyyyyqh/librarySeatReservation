import axios from "axios";
import { ElMessage } from "element-plus";

// 创建 axios 实例
const service = axios.create({
  baseURL: "", // 已经在 vite.config.js 配置了代理，这里留空即可
  timeout: 5000, // 请求超时时间
});

// 响应拦截器
service.interceptors.response.use(
  (response) => {
    const res = response.data;

    // 如果是文件流，直接返回
    if (response.config.responseType === "blob") {
      return res;
    }

    // 您的后端约定：code === 200 为成功
    if (res.code === 200) {
      return res.data; // 直接返回 data 里的数据，剥离外层
    } else {
      // 业务错误（如密码错误、座位被抢），弹出错误提示
      ElMessage.error(res.message || "系统错误");
      return Promise.reject(new Error(res.message || "Error"));
    }
  },
  (error) => {
    console.error("请求错误:", error);
    ElMessage.error(error.message || "网络异常");
    return Promise.reject(error);
  }
);

export default service;
