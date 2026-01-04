<template>
  <div class="seat-manage">
    <el-card shadow="never" class="toolbar">
      <el-form :inline="true">
        <el-form-item label="当前阅览室">
          <el-select
            v-model="currentRoomId"
            placeholder="请选择阅览室"
            @change="handleRoomChange"
          >
            <el-option
              v-for="room in roomList"
              :key="room.roomId"
              :label="room.roomName"
              :value="room.roomId"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            :disabled="!currentRoomId"
            @click="handleAdd"
          >
            <el-icon><Plus /></el-icon> 添加座位
          </el-button>
          <el-button @click="refreshData" :disabled="!currentRoomId">
            <el-icon><Refresh /></el-icon> 刷新
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="10">
        <el-card shadow="never" header="📷 实时布局预览">
          <div class="map-preview">
            <SeatMap v-if="currentRoomId" :seat-list="seatList" />
            <div v-else class="empty-tip">请先选择阅览室</div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="14">
        <el-card shadow="never" header="📋 座位列表">
          <el-table :data="seatList" height="500" stripe>
            <el-table-column prop="seatNum" label="编号" width="100" sortable />
            <el-table-column label="坐标 (X, Y)" width="120">
              <template #default="{ row }">
                {{ row.xaxis }}, {{ row.yaxis }}
              </template>
            </el-table-column>
            <el-table-column label="属性" width="150">
              <template #default="{ row }">
                <el-tag v-if="row.hasSocket" size="small" type="warning"
                  >插座</el-tag
                >
                <el-tag
                  v-if="row.isWindow"
                  size="small"
                  type="success"
                  style="margin-left: 5px"
                  >靠窗</el-tag
                >
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'info'">
                  {{ row.status === 1 ? "启用" : "停用" }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" fixed="right" width="150">
              <template #default="{ row }">
                <el-button link type="danger" @click="handleDelete(row)"
                  >删除</el-button
                >
                <el-button link type="warning" @click="handleForceClear(row)"
                  >强制清退</el-button
                >
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="dialogVisible" title="添加座位" width="400px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="座位编号">
          <el-input v-model="form.seatNum" placeholder="如 A-001" />
        </el-form-item>
        <el-form-item label="X 坐标">
          <el-input-number
            v-model="form.xAxis"
            :min="0"
            :max="800"
            :step="10"
          />
          <div class="tip-text">画布范围 0-800</div>
        </el-form-item>
        <el-form-item label="Y 坐标">
          <el-input-number
            v-model="form.yAxis"
            :min="0"
            :max="600"
            :step="10"
          />
          <div class="tip-text">画布范围 0-600</div>
        </el-form-item>
        <el-form-item label="设施">
          <el-checkbox v-model="form.hasSocket" :true-label="1" :false-label="0"
            >有插座</el-checkbox
          >
          <el-checkbox v-model="form.isWindow" :true-label="1" :false-label="0"
            >靠窗</el-checkbox
          >
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAdd">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import request from "../../utils/request";
import { ElMessage, ElMessageBox } from "element-plus";
import SeatMap from "../../components/SeatMap.vue"; // 复用之前的地图组件

const roomList = ref([]);
const currentRoomId = ref(null);
const seatList = ref([]);
const dialogVisible = ref(false);

const form = reactive({
  seatNum: "",
  xAxis: 50,
  yAxis: 50,
  hasSocket: 0,
  isWindow: 0,
  status: 1,
});

onMounted(() => {
  loadRooms();
});

// 加载阅览室列表
const loadRooms = async () => {
  const res = await request.get("/api/room/list");
  roomList.value = res;
  // 默认选中第一个
  if (roomList.value.length > 0) {
    currentRoomId.value = roomList.value[0].roomId;
    handleRoomChange();
  }
};

// 切换阅览室 -> 加载座位
const handleRoomChange = async () => {
  if (!currentRoomId.value) return;
  const res = await request.get("/api/room/seats", {
    params: { roomId: currentRoomId.value },
  });

  // 同样做一下小写映射，确保地图能显示
  seatList.value = res.map((s) => ({
    ...s,
    xaxis: s.xaxis || s.xAxis || s.x_axis || 0,
    yaxis: s.yaxis || s.yAxis || s.y_axis || 0,
    _occupied: false, // 管理端预览时不显示占用状态，只显示位置
  }));
};

const refreshData = () => {
  handleRoomChange();
};

// 打开新增
const handleAdd = () => {
  form.seatNum = "";
  form.xAxis = 100;
  form.yAxis = 100;
  form.hasSocket = 0;
  form.isWindow = 0;
  dialogVisible.value = true;
};

// 提交新增
const submitAdd = async () => {
  if (!form.seatNum) return ElMessage.warning("请输入座位号");

  await request.post("/api/admin/seat/add", {
    roomId: currentRoomId.value,
    ...form,
  });

  ElMessage.success("添加成功");
  dialogVisible.value = false;
  refreshData();
};

// 删除座位
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除座位 ${row.seatNum} 吗？`, "警告", {
    type: "warning",
  }).then(async () => {
    await request.delete(`/api/admin/seat/${row.seatId}`);
    ElMessage.success("已删除");
    refreshData();
  });
};

// 强制清退 (调用后端新加的接口)
const handleForceClear = (row) => {
  ElMessageBox.confirm(
    `确定要强制结束座位 ${row.seatNum} 的当前使用吗？`,
    "高危操作",
    {
      type: "warning",
    }
  ).then(async () => {
    await request.post("/api/admin/seat/clear", { seatId: row.seatId });
    ElMessage.success("操作成功，座位已释放");
  });
};
</script>

<style scoped>
.toolbar {
  margin-bottom: 20px;
}
.map-preview {
  height: 500px;
  background: #f5f7fa;
  display: flex;
  justify-content: center;
  align-items: center;
  border: 1px dashed #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
}
.empty-tip {
  color: #909399;
}
.tip-text {
  font-size: 12px;
  color: #999;
  line-height: 20px;
}
</style>
