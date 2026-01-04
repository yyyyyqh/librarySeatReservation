package com.yqh.entity.vo;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 通用图表数据对象
 * 前端 ECharts 通常只需要 name (X轴/标签) 和 value (Y轴/数值)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChartDataVO {
    private String name;  // 比如：阅览室名称、学生姓名、日期
    private Object value; // 比如：预约次数、学习时长
}