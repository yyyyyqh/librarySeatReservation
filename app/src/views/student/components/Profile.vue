<template>
  <div class="student-profile">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card class="box-card profile-card" shadow="hover">
          <div class="avatar-area">
            <el-avatar
              :size="100"
              src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png"
            />
            <h2 class="username">
              {{ userInfo.realName || userInfo.username }}
            </h2>
            <p class="role-tag">学生用户</p>
          </div>

          <el-divider />

          <div class="credit-area">
            <p class="label">当前信用积分</p>
            <el-progress
              type="dashboard"
              :percentage="userInfo.creditScore || 100"
              :color="creditColors"
              :width="150"
            >
              <template #default="{ percentage }">
                <span class="score-text">{{ percentage }}</span>
                <div class="score-label">分</div>
              </template>
            </el-progress>
            <div class="credit-tip" v-if="(userInfo.creditScore || 100) < 60">
              <el-tag type="danger">信用过低，已禁止预约</el-tag>
            </div>
            <div class="credit-tip" v-else>
              <el-tag type="success">信用良好</el-tag>
            </div>
          </div>

          <el-descriptions :column="1" border class="info-desc">
            <el-descriptions-item label="学号">{{
              userInfo.campusId || "-"
            }}</el-descriptions-item>
            <el-descriptions-item label="账号">{{
              userInfo.username
            }}</el-descriptions-item>
            <el-descriptions-item label="手机">{{
              userInfo.phone || "未绑定"
            }}</el-descriptions-item>
            <el-descriptions-item label="邮箱">{{
              userInfo.email || "未绑定"
            }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>

      <el-col :span="16">
        <el-card
          shadow="hover"
          class="current-res-card"
          style="margin-bottom: 20px"
        >
          <template #header>
            <div class="card-header">
              <span>🔥 当前状态</span>
              <el-tag v-if="!currentRes" type="info">空闲中</el-tag>
              <el-tag
                v-else-if="currentRes.status === 0"
                type="warning"
                effect="dark"
                >待签到</el-tag
              >
              <el-tag
                v-else-if="currentRes.status === 1"
                type="success"
                effect="dark"
                >学习中</el-tag
              >
            </div>
          </template>

          <div v-if="currentRes" class="res-detail-wrapper">
            <div class="res-info">
              <h3>
                📍 {{ currentRes.roomName }}
                <span style="margin-left: 10px; color: #409eff">{{
                  currentRes.seatNum
                }}</span>
              </h3>
              <p>
                📅 时间：{{ formatFullTime(currentRes.startTime) }} ~
                {{ formatTime(currentRes.endTime) }}
              </p>
            </div>

            <div class="action-btns">
              <template v-if="currentRes.status === 0">
                <el-button
                  type="primary"
                  size="large"
                  @click="handleCheckIn(currentRes.resId)"
                >
                  <el-icon style="margin-right: 5px"><Location /></el-icon>
                  立即签到
                </el-button>
                <el-button
                  type="danger"
                  plain
                  @click="handleCancel(currentRes.resId)"
                  >取消预约</el-button
                >
              </template>

              <template v-if="currentRes.status === 1">
                <el-button type="success" disabled>学习中...</el-button>
                <el-button
                  type="warning"
                  plain
                  @click="handleLeave(currentRes.resId)"
                >
                  <el-icon style="margin-right: 5px"><SwitchButton /></el-icon>
                  签退离座
                </el-button>
              </template>
            </div>
          </div>

          <el-empty
            v-else
            description="当前没有正在进行的预约"
            :image-size="80"
          >
            <el-button type="primary" @click="$router.push('/student/home')"
              >去选座大厅</el-button
            >
          </el-empty>
        </el-card>

        <el-card shadow="hover">
          <el-tabs v-model="activeTab">
            <el-tab-pane label="全部记录" name="all"></el-tab-pane>
            <el-tab-pane label="违约记录" name="violation">
              <template #label>
                <span style="color: #f56c6c">违约记录</span>
              </template>
            </el-tab-pane>
          </el-tabs>

          <el-table
            :data="filteredList"
            stripe
            style="width: 100%"
            height="500"
          >
            <el-table-column label="日期" width="120" sortable prop="startTime">
              <template #default="{ row }">
                {{ formatDate(row.startTime) }}
              </template>
            </el-table-column>
            <el-table-column label="时间段" width="160">
              <template #default="{ row }">
                {{ formatTime(row.startTime) }} - {{ formatTime(row.endTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="roomName" label="阅览室" />
            <el-table-column prop="seatNum" label="座位" width="100">
              <template #default="{ row }">
                <el-tag size="small" effect="plain">{{ row.seatNum }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)">{{
                  getStatusText(row.status)
                }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="签到时间">
              <template #default="{ row }">
                {{ row.checkInTime ? formatFullTime(row.checkInTime) : "-" }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from "vue";
import request from "../../../utils/request";
import { useUserStore } from "../../../stores/user";
import { ElMessage, ElMessageBox } from "element-plus";

const userStore = useUserStore();
const userInfo = ref({});
const reservationList = ref([]);
const activeTab = ref("all");
const currentRes = ref(null); // 当前正在进行的预约

// 信用分颜色
const creditColors = [
  { color: "#f56c6c", percentage: 60 },
  { color: "#e6a23c", percentage: 80 },
  { color: "#5cb87a", percentage: 100 },
];

// 历史列表过滤 (排除掉当前的，避免重复显示)
const filteredList = computed(() => {
  let list = reservationList.value;
  // 如果 activeTab 是违约，只看 status=4
  if (activeTab.value === "violation") {
    return list.filter((item) => item.status === 4);
  }
  return list;
});

onMounted(() => {
  loadUserInfo();
  loadData();
});

const loadUserInfo = async () => {
  try {
    const res = await request.get("/api/user/info", {
      params: { userId: userStore.userInfo.userId },
    });
    userInfo.value = res || {};
    userStore.userInfo = res; // 更新 store
  } catch (e) {
    // 如果接口报错，回退使用 store 里的缓存
    userInfo.value = userStore.userInfo || {};
  }
};

const loadData = async () => {
  const res = await request.get("/api/reservation/list", {
    params: { userId: userStore.userInfo.userId },
  });

  // 1. 找出当前状态是 0(待签到) 或 1(使用中) 的记录
  // 假设一个人同一时间只能有一个有效预约
  const active = res.find((r) => r.status === 0 || r.status === 1);
  currentRes.value = active || null;

  // 2. 剩下的放入列表
  reservationList.value = res;
};

// --- 交互操作 ---

// 签到
const handleCheckIn = async (resId) => {
  // 模拟定位过程
  //   const loading = ElMessage.loading({
  //     message: "正在校验位置...",
  //     duration: 1000,
  //   });
  setTimeout(async () => {
    // loading.close();
    try {
      await request.post("/api/reservation/checkin", { resId });
      ElMessage.success("签到成功！");
      loadData(); // 刷新
    } catch (e) {}
  }, 1000);
};

// 签退
const handleLeave = (resId) => {
  ElMessageBox.confirm("确定要结束学习并释放座位吗？", "签退", {
    type: "warning",
  }).then(async () => {
    await request.post("/api/reservation/leave", { resId });
    ElMessage.success("签退成功");
    loadData();
  });
};

// 取消
const handleCancel = (resId) => {
  ElMessageBox.confirm("确定取消预约？", "提示", { type: "warning" }).then(
    async () => {
      await request.post("/api/reservation/cancel", { resId });
      ElMessage.success("已取消");
      loadData();
    }
  );
};

// --- 工具函数 ---
const getStatusText = (status) => {
  const map = { 0: "待签到", 1: "已签到", 2: "已结束", 3: "已取消", 4: "违约" };
  return map[status] || "未知";
};
const getStatusType = (status) => {
  const map = { 0: "warning", 1: "success", 2: "info", 3: "info", 4: "danger" };
  return map[status] || "info";
};
const formatDate = (val) => (val ? val.substring(0, 10) : "");
const formatTime = (val) => (val ? val.substring(11, 16) : "");
const formatFullTime = (val) =>
  val ? val.replace("T", " ").substring(0, 16) : "";
</script>

<style scoped>
.student-profile {
  padding: 20px;
}
.profile-card {
  text-align: center;
}
.avatar-area {
  margin-bottom: 20px;
}
.username {
  margin: 10px 0 5px;
  font-size: 20px;
}
.role-tag {
  color: #999;
  font-size: 14px;
}
.credit-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin: 20px 0;
}
.score-text {
  font-size: 28px;
  font-weight: bold;
}
.score-label {
  font-size: 12px;
  color: #999;
}
.info-desc {
  margin-top: 20px;
  text-align: left;
}

/* 预约卡片样式 */
.current-res-card {
  border: 1px solid #dcdfe6;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}
.res-detail-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px;
}
.res-info h3 {
  margin: 0 0 10px 0;
  font-size: 18px;
}
.res-info p {
  margin: 0;
  color: #666;
}
.action-btns {
  display: flex;
  gap: 10px;
}
</style>
