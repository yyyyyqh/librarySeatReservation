<template>
  <div class="room-manage">
    <el-card shadow="never" class="toolbar">
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon> 新增阅览室
      </el-button>
      <el-button @click="loadData">
        <el-icon><Refresh /></el-icon> 刷新
      </el-button>
    </el-card>

    <el-card shadow="never" style="margin-top: 20px">
      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="roomId" label="ID" width="80" />
        <el-table-column prop="roomName" label="名称" width="180" />
        <el-table-column prop="location" label="位置" />
        <el-table-column prop="totalSeats" label="座位数" width="100" />
        <el-table-column label="开放时间" width="200">
          <template #default="{ row }">
            {{ formatTime(row.openTime) }} - {{ formatTime(row.closeTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? "开放" : "维护" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)"
              >编辑</el-button
            >
            <el-button type="danger" link @click="handleDelete(row)"
              >删除</el-button
            >
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="form.roomId ? '编辑阅览室' : '新增阅览室'"
      width="500px"
    >
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="form.roomName" placeholder="例如：一楼自习室" />
        </el-form-item>
        <el-form-item label="位置">
          <el-input
            v-model="form.location"
            placeholder="例如：图书馆一楼东侧"
          />
        </el-form-item>
        <el-form-item label="座位数">
          <el-input-number v-model="form.totalSeats" :min="0" />
        </el-form-item>
        <el-form-item label="开放时间">
          <el-time-select
            v-model="form.openTime"
            start="06:00"
            step="01:00"
            end="23:00"
            placeholder="开馆"
            style="width: 140px; margin-right: 10px"
          />
          -
          <el-time-select
            v-model="form.closeTime"
            start="06:00"
            step="01:00"
            end="23:00"
            placeholder="闭馆"
            style="width: 140px"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">开放</el-radio>
            <el-radio :label="0">维护</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import request from "../../../utils/request";
import { ElMessage, ElMessageBox } from "element-plus";

const loading = ref(false);
const tableData = ref([]);
const dialogVisible = ref(false);

const form = reactive({
  roomId: null,
  roomName: "",
  location: "",
  totalSeats: 0,
  openTime: "08:00",
  closeTime: "22:00",
  status: 1,
});

onMounted(() => {
  loadData();
});

// 加载列表
const loadData = async () => {
  loading.value = true;
  try {
    const res = await request.get("/api/room/list");
    tableData.value = res;
  } finally {
    loading.value = false;
  }
};

// 打开新增
const handleAdd = () => {
  form.roomId = null;
  form.roomName = "";
  form.location = "";
  form.totalSeats = 50;
  form.openTime = "08:00";
  form.closeTime = "22:00";
  form.status = 1;
  dialogVisible.value = true;
};

// 打开编辑
const handleEdit = (row) => {
  Object.assign(form, row);
  // 注意：后端返回的时间可能是 "08:00:00"，TimeSelect 组件可能只需要 "08:00"
  // 这里做一个简单截取，具体看组件兼容性
  if (form.openTime && form.openTime.length > 5)
    form.openTime = form.openTime.substring(0, 5);
  if (form.closeTime && form.closeTime.length > 5)
    form.closeTime = form.closeTime.substring(0, 5);
  dialogVisible.value = true;
};

// 提交表单
const submitForm = async () => {
  // 简单的必填校验
  if (!form.roomName || !form.location) {
    ElMessage.warning("请填写完整信息");
    return;
  }

  // 补全时间格式 HH:mm:ss (后端 LocalTime 需要)
  const submitData = { ...form };
  if (submitData.openTime.length === 5) submitData.openTime += ":00";
  if (submitData.closeTime.length === 5) submitData.closeTime += ":00";

  await request.post("/api/admin/room/save", submitData);
  ElMessage.success("操作成功");
  dialogVisible.value = false;
  loadData();
};

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm("确定删除该阅览室吗？其下所有座位也会被删除！", "警告", {
    confirmButtonText: "删除",
    cancelButtonText: "取消",
    type: "warning",
  }).then(async () => {
    await request.delete(`/api/admin/room/${row.roomId}`);
    ElMessage.success("删除成功");
    loadData();
  });
};

// 格式化时间显示
const formatTime = (t) => {
  return t ? t.substring(0, 5) : "";
};
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
}
</style>
