package com.yqh.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yqh.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc // 开启虚拟MVC调用
class UserInterfaceTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testLoginSuccess() throws Exception {
        // 准备登录数据 (假设数据库已有 admin/123456)
        User loginUser = new User();
        loginUser.setUsername("admin");
        loginUser.setPassword("123456");

        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginUser)))
                .andDo(print()) // 打印请求详情
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.realName").value("系统管理员"));
    }

    @Test
    void testLoginFail() throws Exception {
        User loginUser = new User();
        loginUser.setUsername("admin");
        loginUser.setPassword("wrong_password");

        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500)) // 预期登录失败
                .andExpect(jsonPath("$.message").value("密码错误"));
    }

    @Test
    void testGetUserInfo() throws Exception {
        mockMvc.perform(get("/api/user/info").param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(1));
    }
}