package com.yqh.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class StatsDashboardTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testDashboardStructure() throws Exception {
        mockMvc.perform(get("/api/stats/dashboard"))
                .andDo(print())
                .andExpect(status().isOk())
                // 验证返回的数据结构是否包含三大图表所需的 Key
                .andExpect(jsonPath("$.data.roomStats").isArray())
                .andExpect(jsonPath("$.data.userRank").isArray())
                .andExpect(jsonPath("$.data.trendStats").isArray());
    }
}