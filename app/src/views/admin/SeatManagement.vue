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

    <el-dialog
      v-model="batchDialogVisible"
      title="🧩 批量座位生成器"
      width="620px"
    >
      <div class="batch-header">
        <el-form inline>
          <el-form-item label="行数 (排)">
            <el-input-number
              v-model="batchRows"
              :min="1"
              :max="12"
              @change="initGrid"
            />
          </el-form-item>
          <el-form-item label="列数 (列)">
            <el-input-number
              v-model="batchCols"
              :min="1"
              :max="12"
              @change="initGrid"
            />
          </el-form-item>
        </el-form>
        <div class="tips">
          💡 操作提示：点击下方的方格，<span
            style="color: #67c23a; font-weight: bold"
            >绿色</span
          >
          代表生成座位，白色代表过道。
        </div>
      </div>

      <div class="grid-wrapper">
        <div class="grid-container" :style="gridStyle">
          <div
            v-for="(cell, index) in flatGrid"
            :key="index"
            :class="['grid-cell', { active: cell.selected }]"
            @click="toggleCell(index)"
          >
            <span v-if="cell.selected">{{
              getSeatLabel(cell.row, cell.col)
            }}</span>
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="batchDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          @click="submitBatch"
          :loading="batchSubmitting"
        >
          🚀 立即生成 {{ selectedCount }} 个座位
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from "vue";
import request from "../../utils/request";
import { ElMessage, ElMessageBox } from "element-plus";
import SeatMap from "../../components/SeatMap.vue";

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

// --- 批量生成相关变量 ---
const batchDialogVisible = ref(false);
const batchSubmitting = ref(false);
const batchRows = ref(5); // 默认5行
const batchCols = ref(6); // 默认6列
const flatGrid = ref([]); // 网格数据

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

  // 映射字段确保兼容性
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
  // 注意：这里的接口路径按您的后端调整，可能是 /api/seat/add 或 /api/admin/seat/add
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
    // 同样注意接口路径
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
// ✨ 批量生成逻辑 START
// ==========================================

// 打开批量弹窗
const openBatchDialog = () => {
  if (!currentRoomId.value) {
    return ElMessage.warning("请先在左上角选择一个阅览室！");
  }
  batchDialogVisible.value = true;
  initGrid();
};

// 初始化网格数据
const initGrid = () => {
  const arr = [];
  for (let r = 0; r < batchRows.value; r++) {
    for (let c = 0; c < batchCols.value; c++) {
      arr.push({
        row: r,
        col: c,
        selected: false, // 默认白色（不选）
      });
    }
  }
  flatGrid.value = arr;
};

// 计算 Grid 样式 (动态列数)
const gridStyle = computed(() => ({
  display: "grid",
  gridTemplateColumns: `repeat(${batchCols.value}, 45px)`, // 每列宽45px
  gap: "10px",
  justifyContent: "center",
}));

// 统计已选个数
const selectedCount = computed(
  () => flatGrid.value.filter((i) => i.selected).length
);

// 点击格子切换状态
const toggleCell = (index) => {
  flatGrid.value[index].selected = !flatGrid.value[index].selected;
};

// 辅助：生成座位号 (A-001)
const getSeatLabel = (r, c) => {
  const rowChar = String.fromCharCode(65 + r); // 0->A
  const colNum = String(c + 1).padStart(3, "0"); // 0->001
  return `${rowChar}-${colNum}`;
};

// 提交批量
const submitBatch = async () => {
  const selectedCells = flatGrid.value.filter((i) => i.selected);
  if (selectedCells.length === 0) {
    return ElMessage.warning("请至少点选一个格子");
  }

  batchSubmitting.value = true;
  try {
    const seatsToAdd = selectedCells.map((cell) => {
      // 📐 坐标算法：起始50，间距60
      const startX = 50;
      const startY = 50;
      const gap = 60;

      return {
        roomId: currentRoomId.value,
        seatNum: getSeatLabel(cell.row, cell.col),
        xAxis: startX + cell.col * gap,
        yAxis: startY + cell.row * gap,
        status: 1, // 启用
        type: 0, // 普通座
        hasSocket: 0, // 默认无插座，如需可让用户勾选
        isWindow: 0,
      };
    });

    // 调用后端批量接口
    await request.post("/api/seat/batch-add", seatsToAdd);

    ElMessage.success(`成功生成 ${seatsToAdd.length} 个座位！`);
    batchDialogVisible.value = false;
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
  line-height: 20px;
}

/* 批量生成器样式 */
.batch-header {
  text-align: center;
  margin-bottom: 20px;
}
.tips {
  font-size: 13px;
  color: #909399;
  margin-top: 5px;
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

.grid-container {
  /* 由 computed 动态生成 grid-template-columns */
}

/* 格子样式 */
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
  border-color: #409eff;
  transform: scale(1.05);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.grid-cell.active {
  background: #67c23a; /* 选中变绿 */
  border-color: #529b2e;
  font-weight: bold;
}
</style>
