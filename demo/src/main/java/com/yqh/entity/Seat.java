package com.yqh.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_seat")
public class Seat {
    @TableId(type = IdType.AUTO)
    private Long seatId;

    private Long roomId;
    private String seatNum;

    // SVG 坐标
    private Integer xAxis;
    private Integer yAxis;

    private Integer hasSocket; // 1:有, 0:无
    private Integer isWindow;  // 1:是, 0:否
    private Integer status;    // 1:正常, 0:停用
}