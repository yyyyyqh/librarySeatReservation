package com.yqh.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yqh.entity.ReadingRoom;
import com.yqh.entity.Seat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminResourceTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testRoomAndSeatManagement() throws Exception {
        // 1. 管理员添加阅览室
        ReadingRoom room = new ReadingRoom();
        room.setRoomName("测试阅览室");
        room.setLocation("测试位置");
        room.setTotalSeats(50);
        room.setOpenTime(LocalTime.of(8, 0));
        room.setCloseTime(LocalTime.of(22, 0));
        room.setStatus(1);

        mockMvc.perform(post("/api/admin/room/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(room)))
                .andExpect(jsonPath("$.code").value(200));

        // 2. 普通用户查询阅览室列表
        mockMvc.perform(get("/api/room/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.roomName == '测试阅览室')]").exists());

        // 3. 管理员添加座位
        Seat seat = new Seat();
        seat.setRoomId(1L); // 假设往 ID=1 的阅览室加
        seat.setSeatNum("TEST-999");
        seat.setXAxis(50);
        seat.setYAxis(50);

        mockMvc.perform(post("/api/admin/seat/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(seat)))
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testResetCredit() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", 1);

        mockMvc.perform(post("/api/admin/user/reset-credit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(params)))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("信用分已重置为100"));
    }
}