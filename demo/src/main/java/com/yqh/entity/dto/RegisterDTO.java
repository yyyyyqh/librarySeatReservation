package com.yqh.entity.dto;

import lombok.Data;

@Data
public class RegisterDTO {
    private String username;
    private String password;
    private String realName;
    private String campusId; // 学号或工号
    private String phone;
}