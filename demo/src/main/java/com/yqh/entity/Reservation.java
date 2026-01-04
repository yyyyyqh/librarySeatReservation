package com.yqh.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_reservation")
public class Reservation {
    @TableId(type = IdType.AUTO)
    private Long resId;

    private Long userId;
    private Long seatId;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // 0:待签到, 1:已签到, 2:已结束, 3:已取消, 4:违约
    private Integer status;

    private LocalDateTime checkInTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}