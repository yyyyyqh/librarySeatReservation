package com.yqh.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@TableName("sys_seat")
public class Seat {
    @TableId(type = IdType.AUTO)
    private Long seatId;

    private Long roomId;
    private String seatNum;

    // SVG 坐标
    // 🛠️ 核心修复：同时加两个注解
    // 1. @TableField("x_axis") -> 告诉 MyBatis 存数据库时找 "x_axis" 列
    // 2. @JsonProperty("xAxis") -> 告诉 Jackson 收发 JSON 时用 "xAxis" 键
    @TableField("x_axis")
    @JsonProperty("xAxis")
    private Integer xAxis;
    
    @TableField("y_axis")
    @JsonProperty("yAxis")
    private Integer yAxis;

    private Integer hasSocket; // 1:有, 0:无
    private Integer isWindow;  // 1:是, 0:否
    private Integer status;    // 1:正常, 0:停用
}