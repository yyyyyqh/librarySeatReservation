package com.yqh.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long userId;

    private String username;

    // 查询时自动排除密码字段，防止泄露
    @TableField(select = false)
    private String password;

    private String realName;
    private String campusId;
    private Integer identityType; // 0:学生, 1:教师, 2:管理员
    private String phone;
    private String email;
    private Integer creditScore;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}