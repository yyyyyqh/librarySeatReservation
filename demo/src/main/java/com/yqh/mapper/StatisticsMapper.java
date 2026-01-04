package com.yqh.mapper;

import com.yqh.entity.vo.ChartDataVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface StatisticsMapper {

    /**
     * 1. 阅览室热度统计
     * 逻辑：关联 预约表->座位表->阅览室表，按阅览室分组统计预约总数
     */
    @Select("SELECT r.room_name AS name, COUNT(res.res_id) AS value " +
            "FROM sys_reservation res " +
            "LEFT JOIN sys_seat s ON res.seat_id = s.seat_id " +
            "LEFT JOIN sys_reading_room r ON s.room_id = r.room_id " +
            "GROUP BY r.room_id, r.room_name")
    List<ChartDataVO> getRoomPopularity();

    /**
     * 2. 学霸排行榜 (Top 10)
     * 逻辑：统计每位用户已签到(status=1)或已完成(status=2)的总时长(小时)
     * timestampdiff(MINUTE, start, end) 计算分钟差，除以60转小时
     */
    @Select("SELECT u.real_name AS name, " +
            "ROUND(SUM(TIMESTAMPDIFF(MINUTE, res.start_time, res.end_time))/60.0, 1) AS value " +
            "FROM sys_reservation res " +
            "LEFT JOIN sys_user u ON res.user_id = u.user_id " +
            "WHERE res.status IN (1, 2) " + // 只统计有效学习时间
            "GROUP BY u.user_id, u.real_name " +
            "ORDER BY value DESC " +
            "LIMIT 10")
    List<ChartDataVO> getStudyLeaderboard();

    /**
     * 3. 最近 7 天预约趋势
     * 逻辑：按预约开始日期的 'YYYY-MM-DD' 格式分组统计
     * 注意：这里用 CURDATE() - INTERVAL 7 DAY 取最近一周
     */
    @Select("SELECT DATE_FORMAT(start_time, '%Y-%m-%d') AS name, COUNT(*) AS value " +
            "FROM sys_reservation " +
            "WHERE start_time >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
            "GROUP BY DATE_FORMAT(start_time, '%Y-%m-%d') " +
            "ORDER BY name ASC")
    List<ChartDataVO> getDailyTrend();

    /**
     * 4. 时段高峰分析
     * 逻辑：提取预约开始时间的小时部分 (HOUR函数)，统计每个小时的预约量
     */
    @Select("SELECT HOUR(start_time) AS name, COUNT(*) AS value " +
            "FROM sys_reservation " +
            "GROUP BY HOUR(start_time) " +
            "ORDER BY name ASC")
    List<ChartDataVO> getPeakHourStats();

    /**
     * 5. 预约状态占比 (系统健康度)
     * 逻辑：统计不同状态(已完成、违约、取消)的数量
     * status: 2-已结束, 3-已取消, 4-违约
     */
    @Select("SELECT CASE status " +
            "  WHEN 2 THEN '正常完成' " +
            "  WHEN 3 THEN '主动取消' " +
            "  WHEN 4 THEN '违约' " +
            "  ELSE '进行中' END AS name, " +
            "COUNT(*) AS value " +
            "FROM sys_reservation " +
            "WHERE status IN (2, 3, 4) " +
            "GROUP BY status")
    List<ChartDataVO> getStatusRatio();
}