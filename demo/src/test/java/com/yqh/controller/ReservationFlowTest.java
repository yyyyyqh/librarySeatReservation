package com.yqh.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // 自动回滚，测试产生的数据不会污染数据库
class ReservationFlowTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testFullReservationCycle() throws Exception {
        // 1. 发起预约
        Map<String, Object> bookParams = new HashMap<>();
        bookParams.put("userId", 1);
        bookParams.put("seatId", 1);
        bookParams.put("startTime", "2026-02-01 09:00:00");
        bookParams.put("endTime", "2026-02-01 11:00:00");

        mockMvc.perform(post("/api/reservation/book")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookParams)))
                .andExpect(jsonPath("$.code").value(200));

        // 2. 查询预约列表，获取刚生成的 resId
        String responseString = mockMvc.perform(get("/api/reservation/list").param("userId", "1"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // 这里只是为了演示，实际应该解析 JSON 拿到最新的 resId。
        // 为了简化测试，我们假设用户只有一条预约，或者手动去数据库看。
        System.out.println("预约列表响应: " + responseString);

        // 3. 测试冲突预约 (同一个人同一时间再约一次)
        mockMvc.perform(post("/api/reservation/book")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookParams)))
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("该时间段座位已被占用"));
    }
}