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
            <h2 class="username">{{ userInfo.realName }}</h2>
            <p class="role-tag">学生用户</p>
          </div>

          <el-divider />

          <div class="credit-area">
            <p class="label">当前信用积分</p>
            <el-progress
              type="dashboard"
              :percentage="userInfo.creditScore"
              :color="creditColors"
              :width="150"
            >
              <template #default="{ percentage }">
                <span class="score-text">{{ percentage }}</span>
                <div class="score-label">分</div>
              </template>
            </el-progress>
            <div class="credit-tip" v-if="userInfo.creditScore < 60">
              <el-tag type="danger">信用过低，已禁止预约</el-tag>
            </div>
            <div class="credit-tip" v-else>
              <el-tag type="success">信用良好</el-tag>
            </div>
          </div>

          <el-descriptions :column="1" border class="info-desc">
            <el-descriptions-item label="学号">{{
              userInfo.campusId
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
            <el-table-column prop="seatId" label="座位" width="120">
              <template #default="{ row }">
                <el-tag size="small" type="info">ID: {{ row.seatId }}</el-tag>
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
import request from "../../utils/request";
import { useUserStore } from "../../stores/user";

const userStore = useUserStore();
const userInfo = ref({});
const reservationList = ref([]);
const activeTab = ref("all");

// 信用分颜色策略
const creditColors = [
  { color: "#f56c6c", percentage: 60 },
  { color: "#e6a23c", percentage: 80 },
  { color: "#5cb87a", percentage: 100 },
];

// 过滤列表
const filteredList = computed(() => {
  if (activeTab.value === "violation") {
    return reservationList.value.filter((item) => item.status === 4);
  }
  return reservationList.value;
});

onMounted(() => {
  loadUserInfo();
  loadHistory();
});

const loadUserInfo = async () => {
  // 重新获取最新的用户信息（因为信用分可能变了）
  const res = await request.get("/api/user/info", {
    params: { userId: userStore.userInfo.userId },
  });
  userInfo.value = res;
  // 更新 store
  userStore.userInfo = res;
  localStorage.setItem("user", JSON.stringify(res));
};

const loadHistory = async () => {
  const res = await request.get("/api/reservation/list", {
    params: { userId: userStore.userInfo.userId },
  });
  reservationList.value = res;
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

// 时间格式化工具
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
</style>
