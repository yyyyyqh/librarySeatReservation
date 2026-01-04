package com.yqh.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yqh.common.Result;
import com.yqh.entity.ReadingRoom;
import com.yqh.entity.Seat;
import com.yqh.entity.User;
import com.yqh.mapper.ReadingRoomMapper;
import com.yqh.mapper.SeatMapper;
import com.yqh.mapper.UserMapper;
import com.yqh.service.IReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理员专属控制器
 * 职责：阅览室管理、座位管理、用户信用管理
 */
@RestController
@RequestMapping("/api/admin")
@CrossOrigin
public class AdminController {

    @Autowired
    private ReadingRoomMapper roomMapper;
    @Autowired
    private SeatMapper seatMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private IReservationService reservationService; // 注入预约服务

    // ================= 阅览室管理 =================

    /**
     * 新增或更新阅览室
     * 如果传入 roomId 则为更新，否则为新增
     */
    @PostMapping("/room/save")
    public Result<String> saveRoom(@RequestBody ReadingRoom room) {
        if (room.getRoomId() == null) {
            roomMapper.insert(room);
            return Result.success("阅览室添加成功");
        } else {
            roomMapper.updateById(room);
            return Result.success("阅览室更新成功");
        }
    }

    /**
     * 删除阅览室
     */
    @DeleteMapping("/room/{id}")
    public Result<String> deleteRoom(@PathVariable Long id) {
        // 逻辑删除前应该检查是否有预约，这里大作业简化直接删
        roomMapper.deleteById(id);
        // 级联删除该阅览室下的座位
        seatMapper.delete(new LambdaQueryWrapper<Seat>().eq(Seat::getRoomId, id));
        return Result.success("删除成功");
    }

    // ================= 座位管理 =================

    /**
     * 添加座位
     * 用于 SVG 地图编辑时保存新座位
     */
    @PostMapping("/seat/add")
    public Result<String> addSeat(@RequestBody Seat seat) {
        // 检查编号是否重复
        Long count = seatMapper.selectCount(new LambdaQueryWrapper<Seat>()
                .eq(Seat::getRoomId, seat.getRoomId())
                .eq(Seat::getSeatNum, seat.getSeatNum()));

        if (count > 0) {
            return Result.error("该阅览室下已存在同名座位");
        }

        seatMapper.insert(seat);
        return Result.success("座位添加成功");
    }

    /**
     * 删除座位
     */
    @DeleteMapping("/seat/{id}")
    public Result<String> deleteSeat(@PathVariable Long id) {
        seatMapper.deleteById(id);
        return Result.success("座位删除成功");
    }

    // ================= 用户管理 =================

    /**
     * 重置用户信用分
     * 场景：学生来办公室申诉，管理员手动恢复其信用分
     */
    @PostMapping("/user/reset-credit")
    public Result<String> resetCredit(@RequestBody Map<String, Object> params) {
        Long userId = Long.valueOf(params.get("userId").toString());

        User user = userMapper.selectById(userId);
        if (user == null) return Result.error("用户不存在");

        user.setCreditScore(100); // 恢复满分
        userMapper.updateById(user);

        return Result.success("信用分已重置为100");
    }



    /**
     * 管理员强制清退座位
     * 场景：管理员巡查发现占座，或者清理未离开的记录
     * 逻辑：将当前该座位所有 "未结束" (状态0或1) 的订单强制改为 "2(已结束)"，并释放资源
     */
    @PostMapping("/seat/clear")
    public Result<String> clearSeat(@RequestBody Map<String, Object> params) {
        Long seatId = Long.valueOf(params.get("seatId").toString());

        // 调用 Service 层逻辑（我们需要去 Service 实现这个具体逻辑）
        boolean result = reservationService.forceReleaseSeat(seatId);

        if (result) {
            return Result.success("座位已强制释放");
        } else {
            return Result.error("该座位当前没有正在进行的预约");
        }
    }

    /**
     * 获取所有用户列表
     */
    @GetMapping("/user/list")
    public Result<java.util.List<User>> listUsers() {
        return Result.success(userMapper.selectList(null));
    }
}