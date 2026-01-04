package com.yqh.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yqh.common.Result;
import com.yqh.entity.ReadingRoom;
import com.yqh.entity.Seat;
import com.yqh.mapper.ReadingRoomMapper;
import com.yqh.mapper.SeatMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/room")
@CrossOrigin
public class RoomController {

    @Autowired
    private ReadingRoomMapper roomMapper;
    @Autowired
    private SeatMapper seatMapper;

    // 获取所有阅览室
    @GetMapping("/list")
    public Result<List<ReadingRoom>> getRoomList() {
        return Result.success(roomMapper.selectList(null));
    }

    // 获取某个阅览室的所有座位（包含坐标）
    @GetMapping("/seats")
    public Result<List<Seat>> getSeatsByRoom(@RequestParam Long roomId) {
        return Result.success(seatMapper.selectList(
                new LambdaQueryWrapper<Seat>().eq(Seat::getRoomId, roomId)
        ));
    }
}