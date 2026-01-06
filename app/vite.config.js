import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
import path from "path";
import VueDevTools from "vite-plugin-vue-devtools";

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue(), VueDevTools()],
  resolve: {
    alias: {
      // 设置 @ 指向 src 目录
      "@": path.resolve(__dirname, "./src"),
    },
  },
  server: {
    port: 3000, // 前端运行在 3000 端口
    proxy: {
      // 关键配置：遇到 /api 开头的请求，转发给后端 8080
      "/api": {
        target: "http://localhost:8080",
        changeOrigin: true,
        // rewrite: (path) => path.replace(/^\/api/, '') // 根据您的后端 Controller 是否包含 /api，您的代码里已经包含了 /api，所以这里不需要 rewrite
      },
    },
  },
  css: {
    preprocessorOptions: {
      scss: {
        // 自动给每个 scss 代码块头部加上这两行
        additionalData: `
          @use "@/assets/styles/variables.scss" as *;
          @use "@/assets/styles/mixins.scss" as *;
        `,
      },
    },
  },
});
