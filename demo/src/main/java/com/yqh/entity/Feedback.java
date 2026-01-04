package com.yqh.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_feedback")
public class Feedback {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long seatId;
    private String content;
    private Integer status; // 0:待处理 1:已修复
    private LocalDateTime createTime;
}