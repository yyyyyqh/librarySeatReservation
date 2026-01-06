package com.yqh.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yqh.common.Result;
import com.yqh.entity.ReadingRoom;
import com.yqh.entity.Seat;
import com.yqh.mapper.ReadingRoomMapper;
import com.yqh.mapper.ReservationMapper;
import com.yqh.mapper.SeatMapper;
import com.yqh.service.IRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/room")
@CrossOrigin
public class RoomController {

    @Autowired
    private ReadingRoomMapper roomMapper;
    @Autowired
    private SeatMapper seatMapper;
    @Autowired
    private ReservationMapper reservationMapper;
    @Autowired
    private IRoomService roomService;

    // 获取所有阅览室
    @GetMapping("/list")
    public Result<List<ReadingRoom>> getRoomList() {
        return Result.success(roomMapper.selectList(null));
    }

    // 获取某个阅览室的所有座位（包含坐标）
    @GetMapping("/seats")
    public Result<List<Seat>> getSeatsByRoom(
            @RequestParam Long roomId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {

        // 如果前端传了时间，就按时间段查；没传，默认按 NOW() 查
        return Result.success(seatMapper.getSeatStatusByRoom(roomId, startTime, endTime));
    }

    /**
     * 获取阅览室列表（带实时统计数据）
     */
    @GetMapping("/list-with-stats")
    public Result<List<Map<String, Object>>> listWithStats() {
        // 1. 获取所有阅览室
        List<ReadingRoom> rooms = roomService.list();
        List<Map<String, Object>> result = new ArrayList<>();

        // 2. 遍历计算每个阅览室的实时数据
        for (ReadingRoom room : rooms) {
            Map<String, Object> map = new HashMap<>();
            map.put("roomId", room.getRoomId());
            map.put("roomName", room.getRoomName());

            // 获取总座位数 (防止 null，默认为 0)
            int total = room.getTotalSeats() == null ? 0 : room.getTotalSeats();
            map.put("totalSeats", total);

            // 【关键修改】查询数据库获取真实的“已占用”数量
            // 调用 Mapper 查询该阅览室当前有效的预约数
            int occupied = reservationMapper.countOccupiedSeatsByRoom(room.getRoomId());

            // 逻辑修正：已占数不能超过总数 (防止脏数据导致前端显示负数)
            if (occupied > total) occupied = total;

            map.put("occupied", occupied);

            // 计算饱和度百分比 (避免除以0错误)
            int percent = (total == 0) ? 0 : (occupied * 100 / total);
            map.put("percent", percent);

            result.add(map);
        }
        return Result.success(result);
    }
}