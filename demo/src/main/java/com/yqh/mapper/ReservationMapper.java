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
}