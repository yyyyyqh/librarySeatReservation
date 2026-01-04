package com.yqh.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yqh.common.Result;
import com.yqh.entity.ReadingRoom;
import com.yqh.entity.Seat;
import com.yqh.mapper.ReadingRoomMapper;
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
    private IRoomService roomService;

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
    @GetMapping("/list-with-stats")
    public Result<List<Map<String, Object>>> listWithStats() {
        List<ReadingRoom> rooms = roomService.list();
        List<Map<String, Object>> result = new ArrayList<>();

        // 模拟拥挤度数据
        for (ReadingRoom room : rooms) {
            Map<String, Object> map = new HashMap<>();
            map.put("roomId", room.getRoomId());
            map.put("roomName", room.getRoomName());
            map.put("totalSeats", room.getTotalSeats());

            // 模拟：生成一个随机的空闲数
            int occupied = (int) (Math.random() * room.getTotalSeats());
            map.put("occupied", occupied);
            map.put("percent", (occupied * 100 / (room.getTotalSeats() == 0 ? 1 : room.getTotalSeats())));

            result.add(map);
        }
        return Result.success(result);
    }
}