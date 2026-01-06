<template>
  <div class="user-manage">
    <el-card shadow="never" class="toolbar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索姓名或学号..."
        style="width: 200px; margin-right: 10px"
        clearable
        @clear="loadData"
      />
      <el-button type="primary" @click="loadData">
        <el-icon><Search /></el-icon> 查询
      </el-button>
    </el-card>

    <el-card shadow="never" style="margin-top: 20px">
      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="userId" label="ID" width="80" />
        <el-table-column prop="username" label="账号" width="120" />
        <el-table-column prop="realName" label="真实姓名" width="120" />
        <el-table-column prop="campusId" label="学号/工号" width="150" />
        <el-table-column prop="identityType" label="身份" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.identityType === 0" type="info">学生</el-tag>
            <el-tag v-else-if="row.identityType === 1" type="warning"
              >教师</el-tag
            >
            <el-tag v-else type="danger">管理员</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="creditScore" label="信用分" width="100">
          <template #default="{ row }">
            <el-tag :type="getCreditColor(row.creditScore)">
              {{ row.creditScore }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" />

        <el-table-column label="操作" fixed="right" width="150">
          <template #default="{ row }">
            <el-button
              v-if="row.identityType !== 2 && row.creditScore < 100"
              type="primary"
              link
              @click="handleResetCredit(row)"
            >
              重置信用
            </el-button>
            <span v-else style="color: #999; font-size: 12px">无操作</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from "vue";
import request from "@/utils/request";
import { ElMessage, ElMessageBox } from "element-plus";

const loading = ref(false);
const rawData = ref([]); // 原始数据
const searchKeyword = ref("");

// 前端实现简单搜索过滤（因为后端没写搜索接口，且大作业数据量不大）
const tableData = computed(() => {
  if (!searchKeyword.value) return rawData.value;
  const k = searchKeyword.value.toLowerCase();
  return rawData.value.filter(
    (u) =>
      (u.realName && u.realName.toLowerCase().includes(k)) ||
      (u.campusId && u.campusId.toLowerCase().includes(k))
  );
});

onMounted(() => {
  loadData();
});

const loadData = async () => {
  loading.value = true;
  try {
    // 复用之前的获取所有阅览室接口？不，我们需要一个新的获取所有用户的接口
    // ⚠️ 注意：之前后端 UserController 只有 getUserInfo(单个)，AdminController 也没写 listUser
    // 为了省事，我们可以临时用 "MyBatis-Plus" 的 selectList 特性
    // 但正规做法是：我们需要去后端 AdminController 补一个 listUser 接口
    // 这里假设您能去后端补，或者我们暂时用一个假数据/或者直接调 mapper 查（如果不严谨的话）

    // --- 方案 A：去后端补接口（推荐） ---
    // 请在 AdminController.java 添加：
    // @GetMapping("/user/list")
    // public Result<List<User>> listUsers() { return Result.success(userMapper.selectList(null)); }

    const res = await request.get("/api/admin/user/list");
    rawData.value = res;
  } catch (e) {
    // 如果您不想改后端，这里可以 mock 一下数据防止报错，方便您看界面
    // rawData.value = [
    //   { userId: 1, username: 'test', realName: '张三', campusId: '2021001', identityType: 0, creditScore: 80 },
    //   { userId: 2, username: 'admin', realName: '管理员', campusId: 'ADM001', identityType: 2, creditScore: 100 }
    // ]
  } finally {
    loading.value = false;
  }
};

const handleResetCredit = (row) => {
  ElMessageBox.confirm(
    `确定要将 ${row.realName} 的信用分重置为 100 吗？`,
    "信用恢复",
    {
      type: "warning",
    }
  ).then(async () => {
    await request.post("/api/admin/user/reset-credit", { userId: row.userId });
    ElMessage.success("信用分已重置");
    loadData(); // 刷新列表
  });
};

const getCreditColor = (score) => {
  if (score >= 90) return "success";
  if (score >= 60) return "warning";
  return "danger";
};
</script>

<style scoped>
.toolbar {
  display: flex;
}
</style>
