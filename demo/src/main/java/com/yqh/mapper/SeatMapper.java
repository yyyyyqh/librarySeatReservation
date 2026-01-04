package com.yqh.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yqh.entity.Seat;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Map;

public interface SeatMapper extends BaseMapper<Seat> {

    /**
     * 自定义查询：获取某个阅览室的所有座位，并简单联查当前是否有正在进行的预约（可选优化）
     * 这里演示如何用注解写复杂联表
     */
    @Select("SELECT s.*, " +
            "(CASE WHEN r.res_id IS NOT NULL THEN 1 ELSE 0 END) as is_occupied " +
            "FROM sys_seat s " +
            "LEFT JOIN sys_reservation r ON s.seat_id = r.seat_id " +
            "AND r.status = 1 AND NOW() BETWEEN r.start_time AND r.end_time " +
            "WHERE s.room_id = #{roomId}")
    List<Map<String, Object>> getSeatStatusByRoom(@Param("roomId") Long roomId);
}