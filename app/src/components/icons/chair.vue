<template>
  <g :class="['modern-chair', status]">
    <g class="chair-animation-wrapper">
      <path
        d="M-12,-10 
           C-12,-18 12,-18 12,-10 
           L12,2 
           C12,10 -12,10 -12,2 
           Z"
        fill="currentColor"
        class="chair-body"
      />

      <path
        d="M-15,-2 C-15,4 -13,6 -12,6 M15,-2 C15,4 13,6 12,6"
        fill="none"
        stroke="currentColor"
        stroke-width="1.8"
        stroke-linecap="round"
      />

      <ellipse cx="0" cy="14" rx="10" ry="3" fill="black" fill-opacity="0.05" />

      <path
        d="M-8,-8 C-8,-12 8,-12 8,-8"
        fill="none"
        stroke="white"
        stroke-opacity="0.2"
        stroke-width="1"
      />
    </g>
  </g>
</template>

<script setup>
defineProps({
  status: { type: String, default: "available" },
});
</script>

<style scoped>
/* 外层样式：
   处理颜色变量，确保 currentColor 能传递给子元素 
*/
.modern-chair {
  cursor: pointer; /* 鼠标手势移到外层，扩大感应面积 */
  transition: color 0.3s ease;
}

/* 状态配色 */
.available {
  color: #67c23a;
}
.selected {
  color: #63b3ed;
}
.sold {
  color: #fc8181;
}

.chair-body {
  stroke: rgba(0, 0, 0, 0.05);
  stroke-width: 0.5;
}

/* 内层动画包装器：
   专门处理缩放，不会影响外层的 translate 定位
*/
.chair-animation-wrapper {
  transform-origin: center;
  transform-box: fill-box;
  transition: transform 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

/* 关键逻辑：
   当鼠标悬停在“外层”(.modern-chair)时，
   触发“内层”(.chair-animation-wrapper)的缩放
*/
.modern-chair:hover .chair-animation-wrapper {
  transform: scale(1.15);
}
</style>
