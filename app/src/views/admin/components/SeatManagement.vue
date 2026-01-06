<template>
  <div class="seat-manage">
    <el-card shadow="never" class="toolbar">
      <el-form :inline="true">
        <el-form-item label="当前阅览室">
          <el-select
            v-model="currentRoomId"
            placeholder="请选择阅览室"
            @change="handleRoomChange"
            style="width: 200px"
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
            <el-icon><Plus /></el-icon> 单个添加
          </el-button>

          <el-button
            type="success"
            :disabled="!currentRoomId"
            @click="openBatchDialog"
          >
            <el-icon><Grid /></el-icon> 批量生成器
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
            <el-table-column label="属性" width="180">
              <template #default="{ row }">
                <el-tag
                  v-if="row.hasSocket"
                  size="small"
                  type="warning"
                  effect="dark"
                  >⚡ 插座</el-tag
                >
                <el-tag
                  v-if="row.isWindow"
                  size="small"
                  type="success"
                  effect="dark"
                  style="margin-left: 5px"
                  >🪟 靠窗</el-tag
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

    <el-dialog v-model="dialogVisible" title="添加单个座位" width="400px">
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
        </el-form-item>
        <el-form-item label="Y 坐标">
          <el-input-number
            v-model="form.yAxis"
            :min="0"
            :max="600"
            :step="10"
          />
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

    <el-dialog
      v-model="batchDialogVisible"
      title="🎨 可视化座位设计器"
      width="680px"
    >
      <div class="batch-header">
        <el-form inline>
          <el-form-item label="行数">
            <el-input-number
              v-model="batchRows"
              :min="1"
              :max="12"
              @change="initGrid"
            />
          </el-form-item>
          <el-form-item label="列数">
            <el-input-number
              v-model="batchCols"
              :min="1"
              :max="12"
              @change="initGrid"
            />
          </el-form-item>
        </el-form>

        <div class="brush-selector">
          <span class="brush-label">当前画笔：</span>
          <el-radio-group v-model="currentBrush" size="large">
            <el-radio-button label="std">🟢 普通座</el-radio-button>
            <el-radio-button label="socket">⚡ 带插座</el-radio-button>
            <el-radio-button label="window">🪟 靠窗</el-radio-button>
            <el-radio-button label="vip">👑 VIP全配</el-radio-button>
          </el-radio-group>
        </div>

        <div class="tips">
          💡
          操作提示：选中上方一种画笔，然后在下方网格点击绘制。再次点击已绘制的格子可取消。
        </div>
      </div>

      <div class="grid-wrapper">
        <div class="grid-container" :style="gridStyle">
          <div
            v-for="(cell, index) in flatGrid"
            :key="index"
            :class="['grid-cell', cell.type]"
            @click="toggleCell(index)"
          >
            <span v-if="cell.type">{{ getSeatLabel(cell.row, cell.col) }}</span>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="batch-footer">
          <span style="margin-right: auto; color: #999">
            已绘制: {{ selectedCount }} 个座位
          </span>
          <el-button @click="batchDialogVisible = false">取消</el-button>
          <el-button
            type="primary"
            @click="submitBatch"
            :loading="batchSubmitting"
          >
            🚀 立即生成
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from "vue";
import request from "@/utils/request";
import { ElMessage, ElMessageBox } from "element-plus";
import SeatMap from "@/components/SeatMap.vue";

// --- 基础数据 ---
const roomList = ref([]);
const currentRoomId = ref(null);
const seatList = ref([]);
const dialogVisible = ref(false);

// 单个添加表单
const form = reactive({
  seatNum: "",
  xAxis: 50,
  yAxis: 50,
  hasSocket: 0,
  isWindow: 0,
  status: 1,
});

// --- 批量生成 & 画笔 相关变量 ---
const batchDialogVisible = ref(false);
const batchSubmitting = ref(false);
const batchRows = ref(5);
const batchCols = ref(6);
const flatGrid = ref([]);
const currentBrush = ref("std"); // 默认画笔类型

// --- 初始化 ---
onMounted(() => {
  loadRooms();
});

// 1. 加载阅览室
const loadRooms = async () => {
  const res = await request.get("/api/room/list");
  roomList.value = res;
  if (roomList.value.length > 0) {
    currentRoomId.value = roomList.value[0].roomId;
    handleRoomChange();
  }
};

// 2. 切换阅览室 -> 加载座位
const handleRoomChange = async () => {
  if (!currentRoomId.value) return;
  const res = await request.get("/api/room/seats", {
    params: { roomId: currentRoomId.value },
  });

  // 映射字段 (处理后端可能的大小写差异)
  seatList.value = res.map((s) => ({
    ...s,
    xaxis: s.xaxis || s.xAxis || s.x_axis || 0,
    yaxis: s.yaxis || s.yAxis || s.y_axis || 0,
    _occupied: false,
  }));
};

const refreshData = () => {
  handleRoomChange();
};

// --- 单个添加逻辑 ---
const handleAdd = () => {
  form.seatNum = "";
  form.xAxis = 100;
  form.yAxis = 100;
  form.hasSocket = 0;
  form.isWindow = 0;
  dialogVisible.value = true;
};

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

// --- 删除/清退逻辑 ---
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除座位 ${row.seatNum} 吗？`, "警告", {
    type: "warning",
  }).then(async () => {
    await request.delete(`/api/admin/seat/${row.seatId}`);
    ElMessage.success("已删除");
    refreshData();
  });
};

const handleForceClear = (row) => {
  ElMessageBox.confirm(
    `确定要强制结束座位 ${row.seatNum} 的当前使用吗？`,
    "高危操作",
    { type: "warning" }
  ).then(async () => {
    await request.post("/api/admin/seat/clear", { seatId: row.seatId });
    ElMessage.success("操作成功，座位已释放");
  });
};

// ==========================================
// ✨ 批量生成逻辑 (画笔模式) START
// ==========================================

const openBatchDialog = () => {
  if (!currentRoomId.value) {
    return ElMessage.warning("请先在左上角选择一个阅览室！");
  }
  batchDialogVisible.value = true;
  initGrid();
};

// 初始化网格 (type为空字符串表示未选中)
const initGrid = () => {
  const arr = [];
  for (let r = 0; r < batchRows.value; r++) {
    for (let c = 0; c < batchCols.value; c++) {
      arr.push({
        row: r,
        col: c,
        type: "",
      });
    }
  }
  flatGrid.value = arr;
};

// Grid 样式
const gridStyle = computed(() => ({
  display: "grid",
  gridTemplateColumns: `repeat(${batchCols.value}, 45px)`,
  gap: "10px",
  justifyContent: "center",
}));

// 统计已绘个数
const selectedCount = computed(
  () => flatGrid.value.filter((i) => i.type).length
);

// 点击格子 -> 上色或取消
const toggleCell = (index) => {
  const cell = flatGrid.value[index];
  if (cell.type === currentBrush.value) {
    cell.type = ""; // 如果颜色一样，点击取消
  } else {
    cell.type = currentBrush.value; // 否则涂上当前画笔颜色
  }
};

// 辅助：生成座位号
const getSeatLabel = (r, c) => {
  const rowChar = String.fromCharCode(65 + r);
  const colNum = String(c + 1).padStart(3, "0");
  return `${rowChar}-${colNum}`;
};

// 提交批量
const submitBatch = async () => {
  const selectedCells = flatGrid.value.filter((i) => i.type);
  if (selectedCells.length === 0) {
    return ElMessage.warning("请至少绘制一个座位");
  }

  batchSubmitting.value = true;
  try {
    const seatsToAdd = selectedCells.map((cell) => {
      // 📐 坐标算法
      const startX = 50;
      const startY = 50;
      const gap = 60;

      // 根据画笔类型设置属性
      let hasSocket = 0;
      let isWindow = 0;
      if (cell.type === "socket") {
        hasSocket = 1;
      }
      if (cell.type === "window") {
        isWindow = 1;
      }
      if (cell.type === "vip") {
        hasSocket = 1;
        isWindow = 1;
      }

      return {
        roomId: currentRoomId.value,
        seatNum: getSeatLabel(cell.row, cell.col),
        // 注意：这里使用驼峰命名 xAxis/yAxis 以匹配后端 Entity
        xAxis: startX + cell.col * gap,
        yAxis: startY + cell.row * gap,
        status: 1,
        type: 0,
        hasSocket: hasSocket,
        isWindow: isWindow,
      };
    });

    await request.post("/api/seat/batch-add", seatsToAdd);

    ElMessage.success(`成功生成 ${seatsToAdd.length} 个座位！`);
    batchDialogVisible.value = false;
    // 增加 300ms 延迟，等待后端事务完全提交
    refreshData();
  } catch (e) {
    console.error(e);
  } finally {
    batchSubmitting.value = false;
  }
};
// ==========================================
// ✨ 批量生成逻辑 END
// ==========================================
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
}

/* 批量生成器样式 */
.batch-header {
  text-align: center;
  margin-bottom: 15px;
}
.brush-selector {
  margin: 15px 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}
.brush-label {
  font-weight: bold;
  color: #606266;
}
.tips {
  font-size: 13px;
  color: #909399;
  margin-top: 10px;
}

.grid-wrapper {
  background: #f5f7fa;
  border: 1px dashed #dcdfe6;
  border-radius: 6px;
  padding: 20px;
  max-height: 400px;
  overflow: auto;
  display: flex;
  justify-content: center;
}

.batch-footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
}

/* 格子基础样式 */
.grid-cell {
  width: 45px;
  height: 45px;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  color: #fff;
  transition: all 0.2s;
  user-select: none;
}
.grid-cell:hover {
  transform: scale(1.1);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  z-index: 1;
}

/* ✨ 不同类型的画笔颜色 ✨ */
.grid-cell.std {
  background: #67c23a; /* 绿色 */
  border-color: #529b2e;
}
.grid-cell.socket {
  background: #e6a23c; /* 橙色 */
  border-color: #b88230;
}
.grid-cell.window {
  background: #409eff; /* 蓝色 */
  border-color: #337ecc;
}
.grid-cell.vip {
  background: #8e44ad; /* 紫色 */
  border-color: #6c3483;
}
</style>
