// 需要 JS 逻辑来判断（例如：手机端不加载复杂的 ECharts 图表，或者手机端点击座位弹出 Drawer 而不是 Dialog）。
// src/hooks/useViewport.js
import { ref, onMounted, onUnmounted } from "vue";

export function useViewport() {
  const isMobile = ref(false);
  const width = ref(window.innerWidth);

  const checkDevice = () => {
    width.value = window.innerWidth;
    isMobile.value = window.innerWidth <= 768;
  };

  onMounted(() => {
    checkDevice();
    window.addEventListener("resize", checkDevice);
  });

  onUnmounted(() => {
    window.removeEventListener("resize", checkDevice);
  });

  return {
    isMobile,
    width,
  };
}
