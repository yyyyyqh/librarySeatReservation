<template>
  <div class="student-home">
    <div class="main-content">
      <div class="filter-panel">
        <el-card>
          <template #header>预约条件</template>
          <el-form label-position="top">
            <el-form-item label="选择阅览室">
              <el-select
                v-model="currentRoomId"
                placeholder="请选择"
                @change="loadSeats"
              >
                <el-option
                  v-for="room in roomList"
                  :key="room.roomId"
                  :label="room.roomName"
                  :value="room.roomId"
                />
              </el-select>
            </el-form-item>

            <el-form-item label="预约日期">
              <el-date-picker
                v-model="searchDate"
                type="date"
                placeholder="选择日期"
                :disabled-date="disabledDate"
                style="width: 100%"
              />
            </el-form-item>

            <el-form-item label="开始时间">
              <el-time-select
                v-model="startTime"
                start="08:00"
                step="01:00"
                end="22:00"
                placeholder="开始时间"
                style="width: 100%"
              />
            </el-form-item>

            <el-form-item label="结束时间">
              <el-time-select
                v-model="endTime"
                start="08:00"
                step="01:00"
                end="22:00"
                placeholder="结束时间"
                style="width: 100%"
              />
            </el-form-item>

            <el-button type="primary" class="w-100" @click="refreshStatus"
              >刷新状态</el-button
            >
          </el-form>
        </el-card>

        <el-card style="margin-top: 20px">
          <template #header>我的当前预约</template>
          <div v-if="myReservations.length === 0" class="empty-text">
            暂无记录
          </div>
          <div v-else class="res-list">
            <div
              v-for="res in myReservations"
              :key="res.resId"
              class="res-item"
            >
              <p>
                {{ formatTime(res.startTime) }} - {{ formatTime(res.endTime) }}
              </p>
              <el-tag size="small" :type="getStatusType(res.status)">{{
                getStatusText(res.status)
              }}</el-tag>
              <el-button
                v-if="res.status === 0"
                type="primary"
                link
                size="small"
                @click="handleCheckIn(res.resId)"
                >签到</el-button
              >
              <el-button
                v-if="res.status === 0"
                type="danger"
                link
                size="small"
                @click="handleCancel(res.resId)"
                >取消</el-button
              >
            </div>
          </div>
        </el-card>
      </div>

      <div class="map-panel">
        <el-card shadow="never">
          <SeatMap
            :seat-list="seatList"
            :selected-id="selectedSeatId"
            @select="handleSeatSelect"
          />
        </el-card>
      </div>
    </div>

    <el-dialog v-model="dialogVisible" title="确认预约" width="30%">
      <div class="confirm-info">
        <p><strong>阅览室：</strong> {{ currentRoomName }}</p>
        <p><strong>座位号：</strong> {{ selectedSeatNum }}</p>
        <p>
          <strong>时间段：</strong> {{ fullStartTime }} 至 {{ fullEndTime }}
        </p>
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
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { useUserStore } from "../../stores/user";
import request from "../../utils/request";
import { ElMessage, ElMessageBox } from "element-plus";
import SeatMap from "../../components/SeatMap.vue";

const router = useRouter();
const userStore = useUserStore();

// 数据定义
const roomList = ref([]);
const currentRoomId = ref(null);
const seatList = ref([]);
const myReservations = ref([]);

// 搜索条件
const searchDate = ref(new Date()); // 默认今天
const startTime = ref("09:00");
const endTime = ref("12:00");

// 选座状态
const selectedSeatId = ref(null);
const selectedSeatNum = ref("");
const dialogVisible = ref(false);
const submitting = ref(false);

// 计算属性
const currentRoomName = computed(() => {
  const room = roomList.value.find((r) => r.roomId === currentRoomId.value);
  return room ? room.roomName : "";
});

const fullStartTime = computed(() => {
  return formatDateStr(searchDate.value) + " " + startTime.value + ":00";
});

const fullEndTime = computed(() => {
  return formatDateStr(searchDate.value) + " " + endTime.value + ":00";
});

// 工具：日期不可选以前的
const disabledDate = (time) => {
  return time.getTime() < Date.now() - 8.64e7;
};

// 工具：格式化 yyyy-MM-dd
const formatDateStr = (date) => {
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, "0");
  const d = String(date.getDate()).padStart(2, "0");
  return `${y}-${m}-${d}`;
};

// 格式化展示时间
const formatTime = (isoStr) => {
  return isoStr.substring(5, 16).replace("T", " ");
};

// 1. 初始化加载
onMounted(() => {
  loadRooms();
  loadMyReservations();
});

// 加载阅览室
const loadRooms = async () => {
  const res = await request.get("/api/room/list");
  roomList.value = res;
  if (roomList.value.length > 0) {
    currentRoomId.value = roomList.value[0].roomId;
    loadSeats();
  }
};

// 加载座位
const loadSeats = async () => {
  if (!currentRoomId.value) return;
  const res = await request.get("/api/room/seats", {
    params: { roomId: currentRoomId.value },
  });

  // 这里可以做一些简单的处理，比如根据时间判断是否被占用
  // 由于我们是演示系统，这里简化：所有座位默认空闲
  // 真正严谨的做法是：再调一个后端接口，查该时间段哪些座位被占了，然后标记 _occupied
  seatList.value = res.map((s) => ({
    ...s,
    // 注意：这里需要后端 Seat 实体类返回 x_axis 转成 xaxis (或者前端做映射)
    // 根据您后端的 Entity：private Integer xAxis; -> JSON 默认转为 xAxis
    // 请确认后端返回的字段名，如果是 xAxis，下面不用改，如果是 x_axis，需要改
    xaxis: s.xaxis || s.xAxis || s.x_axis || 0,
    yaxis: s.yaxis || s.yAxis || s.y_axis || 0,
    _occupied: false, // 暂定空闲，您可以在这里扩展查询占用逻辑
  }));
};

// 刷新（模拟）
const refreshStatus = () => {
  loadSeats();
  loadMyReservations();
  ElMessage.success("状态已更新");
};

// 加载我的预约
const loadMyReservations = async () => {
  const res = await request.get("/api/reservation/list", {
    params: { userId: userStore.userInfo.userId },
  });
  myReservations.value = res;
};

// 处理点击座位
const handleSeatSelect = (seat) => {
  if (!startTime.value || !endTime.value) {
    ElMessage.warning("请先选择时间段");
    return;
  }
  selectedSeatId.value = seat.seatId;
  selectedSeatNum.value = seat.seatNum;
  dialogVisible.value = true;
};

// 提交预约
const submitReservation = async () => {
  submitting.value = true;
  try {
    await request.post("/api/reservation/book", {
      userId: userStore.userInfo.userId,
      seatId: selectedSeatId.value,
      startTime: fullStartTime.value,
      endTime: fullEndTime.value,
    });
    ElMessage.success("预约成功！");
    dialogVisible.value = false;
    selectedSeatId.value = null; // 清除选中
    loadMyReservations(); // 刷新列表
  } catch (error) {
    // 错误在 request.js 已处理，这里不需要额外操作
  } finally {
    submitting.value = false;
  }
};

// 签到
const handleCheckIn = async (resId) => {
  await request.post("/api/reservation/checkin", { resId });
  ElMessage.success("签到成功");
  loadMyReservations();
};

// 取消
const handleCancel = async (resId) => {
  await request.post("/api/reservation/cancel", { resId });
  ElMessage.warning("预约已取消");
  loadMyReservations();
};

const handleLogout = () => {
  userStore.logout();
  router.push("/login");
};

// 状态字典
const getStatusText = (status) => {
  const map = { 0: "待签到", 1: "已签到", 2: "已结束", 3: "已取消", 4: "违约" };
  return map[status] || "未知";
};
const getStatusType = (status) => {
  const map = { 0: "warning", 1: "success", 2: "info", 3: "info", 4: "danger" };
  return map[status] || "info";
};
</script>

<style scoped>
.student-home {
  height: 100vh;
  display: flex;
  flex-direction: column;
}
.header {
  height: 60px;
  background: #fff;
  border-bottom: 1px solid #dcdfe6;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}
.main-content {
  flex: 1;
  display: flex;
  gap: 20px;
  background: #f5f7fa;
}
.filter-panel {
  width: 300px;
  display: flex;
  flex-direction: column;
}
.map-panel {
  flex: 1;
  background: #fff;
  border-radius: 4px;
}
.w-100 {
  width: 100%;
}
.res-item {
  border-bottom: 1px solid #eee;
  padding: 10px 0;
}
.res-item p {
  margin: 5px 0;
  font-size: 13px;
  color: #666;
}
.empty-text {
  color: #999;
  text-align: center;
  padding: 20px;
}
</style>
