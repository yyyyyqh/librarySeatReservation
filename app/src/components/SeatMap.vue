<template>
  <div class="map-container">
    <div class="legend">
      <div class="item"><span class="dot available"></span>空闲</div>
      <div class="item"><span class="dot occupied"></span>占用</div>
      <div class="item"><span class="dot selected"></span>已选</div>
    </div>

    <svg viewBox="0 0 800 600" class="seat-svg">
      <rect
        x="10"
        y="10"
        width="780"
        height="580"
        fill="#ffffff"
        stroke="#e4e7ed"
        stroke-width="2"
      />

      <g
        v-for="seat in seatList"
        :key="seat.seatId"
        class="seat-group"
        @click="handleSeatClick(seat)"
      >
        <circle
          :cx="seat.xaxis"
          :cy="seat.yaxis"
          r="16"
          :class="['seat-shape', getSeatClass(seat)]"
        />

        <text
          :x="seat.xaxis"
          :y="seat.yaxis"
          dy="5"
          text-anchor="middle"
          class="seat-text"
        >
          {{ seat.seatNum }}
        </text>
      </g>
    </svg>
  </div>
</template>

<script setup>
import { defineProps, defineEmits } from "vue";

const props = defineProps({
  seatList: {
    type: Array,
    default: () => [],
  },
  // 当前选中的座位ID
  selectedId: {
    type: [Number, String],
    default: null,
  },
});

const emit = defineEmits(["select"]);

// 计算样式类
const getSeatClass = (seat) => {
  if (seat.status === 0) return "disabled"; // 停用
  // 注意：后端返回的 status 是座位物理状态。
  // 实际业务中，我们会动态给 seat 对象追加一个 _occupied 属性来表示当前时间段是否有人
  if (seat._occupied) return "occupied";
  if (props.selectedId === seat.seatId) return "selected";
  return "available";
};

const handleSeatClick = (seat) => {
  if (seat.status === 0 || seat._occupied) return;
  emit("select", seat);
};
</script>

<style scoped lang="scss">
.map-container {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.legend {
  display: flex;
  gap: 20px;
  margin-bottom: 10px;
  .item {
    display: flex;
    align-items: center;
    font-size: 14px;
    .dot {
      width: 12px;
      height: 12px;
      border-radius: 50%;
      margin-right: 5px;
      &.available {
        background: #67c23a;
      } // 绿色
      &.occupied {
        background: #f56c6c;
      } // 红色
      &.selected {
        background: #409eff;
      } // 蓝色
    }
  }
}

.seat-svg {
  width: 100%;
  max-width: 800px; // 限制最大宽度
  height: auto;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  border-radius: 4px;

  .seat-group {
    cursor: pointer;
    transition: all 0.3s;
    &:hover .seat-shape.available {
      stroke: #409eff;
      stroke-width: 2px;
    }
  }

  .seat-shape {
    stroke: none;
    &.available {
      fill: #67c23a;
    }
    &.occupied {
      fill: #f56c6c;
      cursor: not-allowed;
    }
    &.selected {
      fill: #409eff;
    }
    &.disabled {
      fill: #909399;
      cursor: not-allowed;
    }
  }

  .seat-text {
    font-size: 10px;
    fill: white;
    pointer-events: none; // 让文字不挡住点击事件
  }
}
</style>
