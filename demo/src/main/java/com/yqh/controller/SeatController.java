package com.yqh.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yqh.common.Result;
import com.yqh.entity.Seat;
import com.yqh.service.ISeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/seat")
@CrossOrigin // 允许跨域，方便Vue前端调试
public class SeatController {
    @Autowired
    private ISeatService seatService;

    /**
     * 批量插入座位，解决插入重复座位导致500。
     */
    @PostMapping("/batch-add")
    public Result<String> batchAdd(@RequestBody List<Seat> seatList) {
        if (seatList == null || seatList.isEmpty()) {
            return Result.error("座位列表为空");
        }

        // 1. 获取当前操作的 roomId (假设这一批都是同一个房间的)
        Long roomId = seatList.get(0).getRoomId();

        // 2. 查出该房间目前已有的所有座位号，放入一个 Set 中以便比对
        // 注意：这里使用了 MyBatis-Plus 的查询条件构造器
        List<Seat> existSeats = seatService.list(new LambdaQueryWrapper<Seat>()
                .eq(Seat::getRoomId, roomId));

        Set<String> existSeatNums = existSeats.stream()
                .map(Seat::getSeatNum)
                .collect(Collectors.toSet());

        // 3. 过滤：只保留那些【不包含】在 existSeatNums 里的新座位
        List<Seat> validSeats = seatList.stream()
                .filter(s -> !existSeatNums.contains(s.getSeatNum()))
                .collect(Collectors.toList());

        if (validSeats.isEmpty()) {
            return Result.error("这些座位号在系统中都已存在，无需重复添加！");
        }

        // 4. 批量保存剩余的有效座位
        try {
            seatService.saveBatch(validSeats);
            return Result.success("成功添加 " + validSeats.size() + " 个新座位 (跳过 " + (seatList.size() - validSeats.size()) + " 个重复)");
        } catch (Exception e) {
            return Result.error("保存失败：" + e.getMessage());
        }
    }
}
