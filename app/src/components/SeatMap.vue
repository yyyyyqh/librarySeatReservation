<template>
  <div class="map-container">
    <div class="legend">
      <div class="item"><span class="dot available"></span>空闲</div>
      <div class="item"><span class="dot occupied"></span>占用</div>
      <div class="item"><span class="dot selected"></span>已选</div>
      <div class="separator">|</div>
      <div class="item"><span class="badge-icon socket">⚡</span>有插座</div>
      <div class="item"><span class="badge-icon window">🪟</span>靠窗</div>
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

        <g
          v-if="seat.hasSocket"
          :transform="`translate(${seat.xaxis + 12}, ${seat.yaxis - 12})`"
        >
          <circle r="7" fill="#E6A23C" stroke="white" stroke-width="1" />
          <path d="M-1 -4 L3 -4 L0 0 L2 0 L-2 5 L-1 1 L-3 1 Z" fill="white" />
        </g>

        <g
          v-if="seat.isWindow"
          :transform="`translate(${seat.xaxis - 12}, ${seat.yaxis - 12})`"
        >
          <circle r="7" fill="#409EFF" stroke="white" stroke-width="1" />
          <rect
            x="-3.5"
            y="-3.5"
            width="7"
            height="7"
            rx="1"
            fill="none"
            stroke="white"
            stroke-width="1.2"
          />
          <line
            x1="0"
            y1="-3.5"
            x2="0"
            y2="3.5"
            stroke="white"
            stroke-width="1.2"
          />
          <line
            x1="-3.5"
            y1="0"
            x2="3.5"
            y2="0"
            stroke="white"
            stroke-width="1.2"
          />
        </g>

        <title>{{ getSeatTooltip(seat) }}</title>
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
  selectedId: {
    type: [Number, String],
    default: null,
  },
});

const emit = defineEmits(["select"]);

// 计算主体样式 (状态颜色)
const getSeatClass = (seat) => {
  if (seat.status === 0) return "disabled";
  if (seat._occupied) return "occupied";
  if (props.selectedId === seat.seatId) return "selected";
  return "available";
};

// 构造 Title 提示
const getSeatTooltip = (seat) => {
  let tips = seat.seatNum;
  if (seat.hasSocket) tips += " [插座]";
  if (seat.isWindow) tips += " [靠窗]";
  return tips;
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
  gap: 15px;
  margin-bottom: 10px;
  background: #fdfdfd;
  padding: 8px 15px;
  border-radius: 20px;
  border: 1px solid #eee;

  .separator {
    color: #ddd;
    font-size: 12px;
  }

  .item {
    display: flex;
    align-items: center;
    font-size: 12px;
    color: #606266;

    .dot {
      width: 10px;
      height: 10px;
      border-radius: 50%;
      margin-right: 5px;
      &.available {
        background: #67c23a;
      }
      &.occupied {
        background: #f56c6c;
      }
      &.selected {
        background: #409eff;
      }
    }

    .badge-icon {
      font-size: 14px;
      margin-right: 3px;
      &.socket {
        color: #e6a23c;
      }
      &.window {
        color: #409eff;
      }
    }
  }
}

.seat-svg {
  width: 100%;
  max-width: 800px;
  height: auto;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  border-radius: 8px;
  background: #fff;

  .seat-group {
    cursor: pointer;
    transition: opacity 0.2s;
    &:hover {
      opacity: 0.8;
    }
  }

  .seat-shape {
    stroke: none;
    transition: fill 0.3s;
    &.available {
      fill: #67c23a;
    } /* 默认绿色 */
    &.occupied {
      fill: #f56c6c;
      cursor: not-allowed;
    } /* 占用红色 */
    &.selected {
      fill: #409eff;
      stroke: #b3d8ff;
      stroke-width: 3px;
    } /* 选中蓝色 */
    &.disabled {
      fill: #dcdfe6;
      cursor: not-allowed;
    } /* 停用灰色 */
  }

  .seat-text {
    font-size: 10px;
    fill: white;
    font-weight: bold;
    pointer-events: none;
    user-select: none;
  }
}
</style>
