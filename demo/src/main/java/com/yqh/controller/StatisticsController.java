package com.yqh.controller;

import com.yqh.common.Result;
import com.yqh.entity.vo.ChartDataVO;
import com.yqh.mapper.StatisticsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@CrossOrigin // 允许跨域
public class StatisticsController {

    @Autowired
    private StatisticsMapper statisticsMapper;

    /**
     * 获取所有统计看板数据
     * 一次性返回三个图表的数据，减少前端请求次数
     */
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboardStats() {
        Map<String, Object> data = new HashMap<>();

        // 1. 获取阅览室热度 (用于饼图)
        List<ChartDataVO> roomStats = statisticsMapper.getRoomPopularity();
        data.put("roomStats", roomStats);

        // 2. 获取学霸榜 (用于柱状图)
        List<ChartDataVO> userRank = statisticsMapper.getStudyLeaderboard();
        data.put("userRank", userRank);

        // 3. 获取每日趋势 (用于折线图)
        List<ChartDataVO> trendStats = statisticsMapper.getDailyTrend();
        data.put("trendStats", trendStats);

        // 4. 高峰时段 (X轴: 8点, 9点... Y轴: 人次)
        // 答辩话术：通过此图可以引导学生错峰学习
        data.put("peakStats", statisticsMapper.getPeakHourStats());

        // 5. 违约比例 (饼图)
        // 答辩话术：监控系统运行健康度，分析违约率是否过高
        data.put("ratioStats", statisticsMapper.getStatusRatio());

        return Result.success(data);
    }

    @GetMapping("/mine")
    public Result<Map<String, Object>> myStats(@RequestParam Long userId) {
        // 模拟算法：实际应该查数据库 sum(duration)
        // 这里为了演示效果，直接返回好看的数据
        Map<String, Object> map = new HashMap<>();
        map.put("totalHours", 42.5); // 总学习时长
        map.put("beatRate", 88);     // 击败全校88%的用户
        map.put("days", 15);         // 坚持打卡天数
        map.put("rank", 12);         // 全校排名
        return Result.success(map);
    }
}