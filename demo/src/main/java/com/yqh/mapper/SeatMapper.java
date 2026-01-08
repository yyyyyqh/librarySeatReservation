package com.yqh.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yqh.entity.Seat;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Map;

public interface SeatMapper extends BaseMapper<Seat> {
    /**
     * 悲观锁核心方法
     * 作用：根据 seatId 查询座位信息，并强制加上 MySQL 行级排他锁 (X锁)
     * 注意：必须在 @Transactional 事务中使用才能生效，事务结束自动释放锁
     */
    @Select("SELECT * FROM sys_seat WHERE seat_id = #{seatId} FOR UPDATE")
    Seat selectSeatForUpdate(@Param("seatId") Long seatId);


    @Select("<script>" +
            "SELECT s.*, " +
            "(CASE WHEN r.res_id IS NOT NULL THEN 1 ELSE 0 END) as is_occupied " +
            "FROM sys_seat s " +
            "LEFT JOIN sys_reservation r ON s.seat_id = r.seat_id " +
            "AND r.status IN (0, 1) " +
            "AND ( " +
            "  <choose>" +
            "    <when test='startTime != null and endTime != null'>" +
            "      (r.start_time &lt; #{endTime} AND r.end_time &gt; #{startTime})" +
            "    </when>" +
            "    <otherwise>" +
            "      NOW() BETWEEN r.start_time AND r.end_time" + // 💡 管理员端默认走这里
            "    </otherwise>" +
            "  </choose>" +
            ") " +
            "WHERE s.room_id = #{roomId}" +
            "</script>")
    List<Seat> getSeatStatusByRoom(
            @Param("roomId") Long roomId,
            @Param("startTime") String startTime,
            @Param("endTime") String endTime);
}