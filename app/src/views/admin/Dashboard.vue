<template>
  <div class="admin-dashboard">
    <el-row :gutter="20" class="data-cards">
      <el-col :span="6">
        <el-card shadow="hover" class="card-box">
          <template #header><span>今日预约</span></template>
          <div class="card-num">{{ summary.todayCount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="card-box">
          <template #header><span>当前在馆</span></template>
          <div class="card-num text-success">
            {{ summary.activeCount || 0 }}
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="card-box">
          <template #header><span>违约总数</span></template>
          <div class="card-num text-danger">
            {{ summary.violationCount || 0 }}
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="card-box">
          <template #header><span>系统状态</span></template>
          <div class="card-num text-primary">运行中</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="16">
        <el-card shadow="never" class="chart-card">
          <template #header>📅 近七日预约趋势</template>
          <div ref="trendChartRef" style="height: 300px"></div>
        </el-card>

        <el-card shadow="never" class="chart-card" style="margin-top: 20px">
          <template #header>⏰ 每日高峰时段分布</template>
          <div ref="peakChartRef" style="height: 300px"></div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card shadow="never" class="chart-card">
          <template #header>🏆 图书馆卷王榜 (Top 10)</template>
          <el-table :data="userRank" size="small" stripe style="width: 100%">
            <el-table-column type="index" label="排名" width="50" />
            <el-table-column prop="name" label="姓名" />
            <el-table-column
              prop="value"
              label="时长(h)"
              width="80"
              align="right"
            />
          </el-table>
        </el-card>

        <el-card shadow="never" class="chart-card" style="margin-top: 20px">
          <template #header>📊 阅览室热度占比</template>
          <div ref="roomPieRef" style="height: 250px"></div>
        </el-card>

        <el-card shadow="never" class="chart-card" style="margin-top: 20px">
          <template #header>❤️ 预约履约率</template>
          <div ref="statusPieRef" style="height: 200px"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from "vue";
import * as echarts from "echarts";
import request from "../../utils/request";

// 数据定义
const summary = ref({});
const userRank = ref([]);

// DOM 引用
const trendChartRef = ref(null);
const peakChartRef = ref(null);
const roomPieRef = ref(null);
const statusPieRef = ref(null);

// ECharts 实例
let charts = [];

onMounted(async () => {
  await loadData();
  window.addEventListener("resize", handleResize);
});

onBeforeUnmount(() => {
  window.removeEventListener("resize", handleResize);
  charts.forEach((c) => c.dispose());
});

const handleResize = () => {
  charts.forEach((c) => c.resize());
};

const loadData = async () => {
  // 调用后端聚合接口
  const res = await request.get("/api/stats/dashboard");
  const { trendStats, roomStats, userRank: rank, peakStats, ratioStats } = res;

  userRank.value = rank || [];
  // 模拟头部卡片数据（因为后端没写这个接口，这里先算一下或者模拟）
  summary.value = {
    todayCount:
      trendStats?.length > 0 ? trendStats[trendStats.length - 1].value : 0,
    activeCount: Math.floor(Math.random() * 50) + 10, // 演示用随机数
    violationCount: ratioStats?.find((i) => i.name === "违约")?.value || 0,
  };

  // 1. 渲染趋势图 (Line)
  initChart(trendChartRef, {
    tooltip: { trigger: "axis" },
    xAxis: { type: "category", data: trendStats.map((i) => i.name) },
    yAxis: { type: "value" },
    series: [
      {
        data: trendStats.map((i) => i.value),
        type: "line",
        smooth: true,
        areaStyle: {},
      },
    ],
  });

  // 2. 渲染高峰时段 (Bar)
  initChart(peakChartRef, {
    tooltip: { trigger: "axis" },
    xAxis: { type: "category", data: peakStats?.map((i) => i.name + "点") },
    yAxis: { type: "value" },
    series: [
      { data: peakStats?.map((i) => i.value), type: "bar", color: "#409EFF" },
    ],
  });

  // 3. 渲染阅览室热度 (Pie)
  initChart(roomPieRef, {
    tooltip: { trigger: "item" },
    series: [
      {
        type: "pie",
        radius: ["40%", "70%"],
        data: roomStats,
        itemStyle: { borderRadius: 10, borderColor: "#fff", borderWidth: 2 },
      },
    ],
  });

  // 4. 渲染履约率 (Pie)
  initChart(statusPieRef, {
    tooltip: { trigger: "item" },
    series: [
      {
        type: "pie",
        radius: "60%",
        data: ratioStats,
        color: ["#67C23A", "#E6A23C", "#F56C6C", "#909399"], // 对应颜色
      },
    ],
  });
};

const initChart = (domRef, option) => {
  if (domRef.value) {
    const chart = echarts.init(domRef.value);
    chart.setOption(option);
    charts.push(chart);
  }
};
</script>

<style scoped>
.admin-dashboard {
  padding: 20px;
  background: #f0f2f5;
  min-height: 100vh;
}
.card-box {
  text-align: center;
}
.card-num {
  font-size: 24px;
  font-weight: bold;
}
.text-success {
  color: #67c23a;
}
.text-danger {
  color: #f56c6c;
}
.text-primary {
  color: #409eff;
}
.chart-card {
  margin-bottom: 0;
}
</style>
