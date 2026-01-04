package com.yqh.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalTime;

@Data
@TableName("sys_reading_room")
public class ReadingRoom {
    @TableId(type = IdType.AUTO)
    private Long roomId;

    private String roomName;
    private String location;
    private Integer totalSeats;

    // 对应 MySQL 的 TIME 类型
    private LocalTime openTime;
    private LocalTime closeTime;

    private Integer status; // 1:开放, 0:维护
}