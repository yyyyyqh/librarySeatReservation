<template>
  <div class="student-home">
    <div class="welcome-banner">
      <div class="banner-content">
        <h2>👋 早安，{{ userStore.userInfo?.realName }}！</h2>
        <p>今天想去哪个阅览室学习呢？保持专注，未来可期。</p>
      </div>
      <div class="banner-icon">🎓</div>
    </div>

    <div class="stats-panel">
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="stat-card blue">
            <div class="label">累计学习 (小时)</div>
            <div class="num">{{ myStats.totalHours || 0 }}</div>
            <el-icon class="bg-icon"><Timer /></el-icon>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card green">
            <div class="label">坚持打卡 (天)</div>
            <div class="num">{{ myStats.days || 0 }}</div>
            <el-icon class="bg-icon"><Calendar /></el-icon>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card orange">
            <div class="label">全校排名</div>
            <div class="num">No.{{ myStats.rank || "-" }}</div>
            <el-icon class="bg-icon"><Trophy /></el-icon>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card purple">
            <div class="label">击败用户</div>
            <div class="num">{{ myStats.beatRate || 0 }}%</div>
            <el-icon class="bg-icon"><TrendCharts /></el-icon>
          </div>
        </el-col>
      </el-row>
    </div>

    <div class="main-body">
      <div class="room-list-panel">
        <el-card shadow="never" class="room-card">
          <template #header>
            <div class="card-header">
              <span>🏫 阅览室状态</span>
              <el-tag size="small" type="success" effect="dark">实时</el-tag>
            </div>
          </template>
          <div class="room-scroll">
            <div
              v-for="room in roomStatsList"
              :key="room.roomId"
              :class="['room-item', { active: currentRoomId === room.roomId }]"
              @click="handleSwitchRoom(room)"
            >
              <div class="r-top">
                <span class="r-name">{{ room.roomName }}</span>
                <span :class="['r-status', getOccupancyColor(room.percent)]">
                  {{ room.percent }}% 饱和
                </span>
              </div>
              <el-progress
                :percentage="room.percent"
                :status="getProgressStatus(room.percent)"
                :stroke-width="8"
                :show-text="false"
              />
              <div class="r-info">
                剩余 {{ room.totalSeats - room.occupied }} / 总座
                {{ room.totalSeats }}
              </div>
            </div>
          </div>
        </el-card>
      </div>

      <div class="map-panel">
        <el-card shadow="never" class="map-card-container">
          <template #header>
            <div class="map-header">
              <span class="map-title"
                >📍 当前区域：{{ currentRoomName || "请选择阅览室" }}</span
              >
              <div class="map-ctrl">
                <el-date-picker
                  v-model="searchDate"
                  type="date"
                  size="small"
                  placeholder="日期"
                  :disabled-date="disabledDate"
                  style="width: 130px"
                />
                <el-time-select
                  v-model="startTime"
                  start="08:00"
                  step="01:00"
                  end="22:00"
                  size="small"
                  placeholder="开始"
                  style="width: 100px"
                  @change="loadSeats"
                />
                <el-time-select
                  v-model="endTime"
                  :min-time="startTime"
                  start="08:00"
                  step="01:00"
                  end="22:00"
                  size="small"
                  placeholder="结束"
                  style="width: 100px"
                  @change="loadSeats"
                />
                <el-button type="primary" size="small" @click="loadSeats"
                  >刷新</el-button
                >
              </div>
            </div>
          </template>

          <div class="map-wrapper">
            <SeatMap
              :seat-list="seatList"
              :selected-id="selectedSeatId"
              @select="handleSeatSelect"
            />
          </div>
        </el-card>
      </div>
    </div>

    <el-tooltip content="故障报修" placement="left">
      <div class="fix-btn" @click="feedbackDialogVisible = true">
        <el-icon><Tools /></el-icon>
      </div>
    </el-tooltip>

    <el-dialog
      v-model="feedbackDialogVisible"
      title="🛠️ 故障报修"
      width="400px"
    >
      <el-form :model="feedbackForm">
        <el-form-item label="座位号(可选)">
          <el-input v-model="feedbackForm.seatNum" placeholder="例如 A-001" />
        </el-form-item>
        <el-form-item label="问题描述">
          <el-input
            v-model="feedbackForm.content"
            type="textarea"
            :rows="4"
            placeholder="请描述您遇到的问题，如：插座没电、椅子损坏..."
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="feedbackDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitFeedback">提交反馈</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="dialogVisible" title="确认预约" width="30%">
      <div class="confirm-info">
        <p><strong>阅览室：</strong> {{ currentRoomName }}</p>
        <p><strong>座位号：</strong> {{ selectedSeatNum }}</p>
        <p><strong>日期：</strong> {{ formatDateStr(searchDate) }}</p>
        <p><strong>时间段：</strong> {{ startTime }} 至 {{ endTime }}</p>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button
            type="primary"
            @click="submitReservation"
            :loading="submitting"
          >
            确认提交
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from "vue";
import request from "../../../utils/request";
import { useUserStore } from "../../../stores/user";
import { ElMessage } from "element-plus";
import SeatMap from "@/components/SeatMap.vue";

const userStore = useUserStore();

// --- 状态定义 ---
const myStats = ref({});
const roomStatsList = ref([]);
const seatList = ref([]);

const currentRoomId = ref(null);
const currentRoomName = ref("");

// 搜索条件
const searchDate = ref(new Date());
const startTime = ref("09:00");
const endTime = ref("12:00");

// 选座交互
const selectedSeatId = ref(null);
const selectedSeatNum = ref("");
const dialogVisible = ref(false);
const submitting = ref(false);

// 报修交互
const feedbackDialogVisible = ref(false);
const feedbackForm = ref({ seatNum: "", content: "" });

// --- 初始化 ---
onMounted(() => {
  loadMyStats();
  loadRoomStats();
  // ✨ 新增：初始化默认预约时间为未来 1-2 小时
  initDefaultTime();
});

// --- 业务逻辑 ---

// 1. 获取个人学习统计
const loadMyStats = async () => {
  try {
    const res = await request.get("/api/stats/mine", {
      params: { userId: userStore.userInfo.userId },
    });
    myStats.value = res || {};
  } catch (e) {
    console.error("统计接口可能未就绪，使用默认显示");
  }
};

// 2. 获取阅览室列表（带拥挤度）
const loadRoomStats = async () => {
  try {
    const res = await request.get("/api/room/list-with-stats");
    roomStatsList.value = res;
    // 默认选中第一个
    if (res && res.length > 0) {
      handleSwitchRoom(res[0]);
    }
  } catch (e) {
    ElMessage.error("无法加载阅览室列表，请检查后端");
  }
};

const initDefaultTime = () => {
  const now = new Date();
  let currentHour = now.getHours();

  // 计算开始小时（当前小时 + 1）
  // 注意：如果超过 22 点（闭馆时间），可以根据业务逻辑处理，这里简单处理
  let startH = currentHour + 1;
  let endH = currentHour + 2;

  // 边界处理：如果超过 22 点，默认选第二天早上的时间或维持现状
  if (startH >= 22) {
    startTime.value = "08:00";
    endTime.value = "10:00";
  } else {
    // 格式化为 "HH:00" 格式
    startTime.value = `${String(startH).padStart(2, "0")}:00`;
    endTime.value = `${String(endH).padStart(2, "0")}:00`;
  }
};

// 3. 切换阅览室
const handleSwitchRoom = (room) => {
  currentRoomId.value = room.roomId;
  currentRoomName.value = room.roomName;
  loadSeats();
};

// 4. 加载座位
const loadSeats = async () => {
  if (!currentRoomId.value) return;

  // 拼接完整的时间字符串，发给后端进行范围查询
  const fullStart =
    formatDateStr(searchDate.value) + " " + startTime.value + ":00";
  const fullEnd = formatDateStr(searchDate.value) + " " + endTime.value + ":00";

  const res = await request.get("/api/room/seats", {
    params: { roomId: currentRoomId.value },
    startTime: fullStart, // 💡 新增：发送开始时间
    endTime: fullEnd, // 💡 新增：发送结束时间
  });

  // 映射字段并处理大小写兼容
  seatList.value = res.map((s) => ({
    ...s,
    // 兼容后端可能返回 xaxis/xAxis/x_axis
    xaxis: s.xaxis || s.xAxis || s.x_axis || 0,
    yaxis: s.yaxis || s.yAxis || s.y_axis || 0,
    // 简单模拟占用状态（实际应结合时间段查询后端）
    _occupied: s.isOccupied === 1,
  }));
  // 清除选中状态
  selectedSeatId.value = null;
};

// 5. 点击座位
const handleSeatSelect = (seat) => {
  if (!startTime.value || !endTime.value) {
    ElMessage.warning("请先选择开始和结束时间");
    return;
  }
  selectedSeatId.value = seat.seatId;
  selectedSeatNum.value = seat.seatNum;
  dialogVisible.value = true;
};

// 6. 提交预约
const submitReservation = async () => {
  submitting.value = true;
  try {
    const fullStart =
      formatDateStr(searchDate.value) + " " + startTime.value + ":00";
    const fullEnd =
      formatDateStr(searchDate.value) + " " + endTime.value + ":00";

    await request.post("/api/reservation/book", {
      userId: userStore.userInfo.userId,
      seatId: selectedSeatId.value,
      startTime: fullStart,
      endTime: fullEnd,
    });
    ElMessage.success("预约成功！");
    dialogVisible.value = false;
    selectedSeatId.value = null;
    // 这里可以后续加刷新逻辑
  } finally {
    submitting.value = false;
  }
};

// 7. 提交报修
const submitFeedback = async () => {
  if (!feedbackForm.value.content) return ElMessage.warning("请填写内容");

  await request.post("/api/feedback/add", {
    userId: userStore.userInfo.userId,
    // 如果填了座位号，就把座位号拼接到内容里
    content: feedbackForm.value.seatNum
      ? `[座位:${feedbackForm.value.seatNum}] ${feedbackForm.value.content}`
      : feedbackForm.value.content,
  });

  ElMessage.success("反馈已提交");
  feedbackDialogVisible.value = false;
  feedbackForm.value.content = "";
  feedbackForm.value.seatNum = "";
};

// --- 工具函数 ---
const disabledDate = (time) => {
  return time.getTime() < Date.now() - 8.64e7;
};
const formatDateStr = (date) => {
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, "0");
  const d = String(date.getDate()).padStart(2, "0");
  return `${y}-${m}-${d}`;
};
// 颜色辅助
const getOccupancyColor = (p) => {
  if (p > 80) return "text-danger";
  if (p > 50) return "text-warning";
  return "text-success";
};
const getProgressStatus = (p) => {
  if (p > 80) return "exception";
  if (p > 50) return "warning";
  return "success";
};
</script>

<style scoped>
/* 页面整体容器 */
.student-home {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 1. 欢迎横幅 */
.welcome-banner {
  background: linear-gradient(135deg, #e6f7ff 0%, #ffffff 100%);
  border: 1px solid #bae7ff;
  border-radius: 12px;
  padding: 20px 30px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.banner-content h2 {
  margin: 0 0 5px 0;
  color: #1f1f1f;
}
.banner-content p {
  margin: 0;
  color: #697b8c;
  font-size: 14px;
}
.banner-icon {
  font-size: 40px;
}

/* 2. 统计卡片 */
.stat-card {
  background: white;
  padding: 20px;
  border-radius: 8px;
  position: relative;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: transform 0.2s;
  height: 100px;
  color: white;
}
.stat-card:hover {
  transform: translateY(-3px);
}
.stat-card .label {
  font-size: 13px;
  opacity: 0.9;
}
.stat-card .num {
  font-size: 26px;
  font-weight: bold;
  margin-top: 5px;
}
.bg-icon {
  position: absolute;
  right: -5px;
  bottom: -5px;
  font-size: 70px;
  opacity: 0.2;
  transform: rotate(-15deg);
}
/* 卡片配色 */
.blue {
  background: linear-gradient(135deg, #36cfc9, #1890ff);
}
.green {
  background: linear-gradient(135deg, #95de64, #5cdbd3);
}
.orange {
  background: linear-gradient(135deg, #ffd666, #ffc53d);
}
.purple {
  background: linear-gradient(135deg, #b37feb, #85a5ff);
}

/* 3. 主体布局 (Flex) */
.main-body {
  display: flex;
  gap: 20px;
  align-items: flex-start; /* 顶部对齐 */
}

/* 左侧列表 */
.room-list-panel {
  width: 320px;
  flex-shrink: 0;
}
.room-card {
  height: 600px; /* 固定高度 */
  display: flex;
  flex-direction: column;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.room-scroll {
  height: 520px;
  overflow-y: auto;
  padding-right: 5px;
}
.room-item {
  padding: 15px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  border-radius: 4px;
  margin-bottom: 5px;
  transition: all 0.2s;
}
.room-item:hover {
  background: #f5f7fa;
}
.room-item.active {
  background: #e6f7ff;
  border-left: 4px solid #1890ff;
}

.r-top {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 15px;
}
.r-name {
  font-weight: bold;
  color: #333;
}
.r-info {
  font-size: 12px;
  color: #999;
  margin-top: 5px;
}
.text-danger {
  color: #f56c6c;
}
.text-warning {
  color: #e6a23c;
}
.text-success {
  color: #67c23a;
}

/* 右侧地图 */
.map-panel {
  flex: 1;
  /* 防止 Flex 子元素被内容撑开导致宽度溢出 */
  min-width: 0;
}
.map-card-container {
  height: 600px;
}
.map-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}
.map-title {
  font-weight: bold;
  font-size: 16px;
}
.map-ctrl {
  display: flex;
  gap: 8px;
  align-items: center;
}
.map-wrapper {
  height: 500px;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #f9f9f9;
  border-radius: 4px;
  overflow: hidden;
}

/* 悬浮按钮 */
.fix-btn {
  position: fixed;
  right: 40px;
  bottom: 80px;
  width: 50px;
  height: 50px;
  background: #f56c6c;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  box-shadow: 0 4px 12px rgba(245, 108, 108, 0.4);
  cursor: pointer;
  z-index: 999;
  transition: transform 0.3s;
}
.fix-btn:hover {
  transform: scale(1.1) rotate(90deg);
}

/* 预约信息弹窗文字 */
.confirm-info p {
  margin: 10px 0;
  font-size: 15px;
}
</style>
