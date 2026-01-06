package com.yqh.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yqh.entity.Reservation;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

public interface ReservationMapper extends BaseMapper<Reservation> {

    /**
     * 核心冲突检测 SQL
     * 逻辑：查询指定座位(seat_id)在有效状态(status 0,1)下，
     * 是否存在时间段重叠 (ExistingStart < NewEnd AND ExistingEnd > NewStart)
     *
     * @param seatId 座位ID
     * @param startTime 新预约开始时间
     * @param endTime 新预约结束时间
     * @return 冲突记录数 (大于0则表示有冲突)
     */
    @Select("SELECT COUNT(*) FROM sys_reservation " +
            "WHERE seat_id = #{seatId} " +
            "AND status IN (0, 1) " +
            "AND start_time < #{endTime} " +
            "AND end_time > #{startTime}")
    int countOverlappingReservations(@Param("seatId") Long seatId,
                                     @Param("startTime") LocalDateTime startTime,
                                     @Param("endTime") LocalDateTime endTime);

    /**
     * 统计指定阅览室当前时刻的占用数
     * 逻辑：关联座位表，筛选出属于该阅览室、状态有效(0或1)、且当前时间在预约时段内的记录
     */
    @Select("SELECT COUNT(r.res_id) " +
            "FROM sys_reservation r " +
            "LEFT JOIN sys_seat s ON r.seat_id = s.seat_id " +
            "WHERE s.room_id = #{roomId} " +
            "AND r.status IN (0, 1) " + // 0:待签到, 1:已签到 (视为占用)
            "AND NOW() BETWEEN r.start_time AND r.end_time")
    Integer countOccupiedSeatsByRoom(@Param("roomId") Long roomId);
}