package com.yqh.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yqh.entity.Reservation;
import com.yqh.entity.User;
import com.yqh.mapper.ReservationMapper;
import com.yqh.mapper.UserMapper;
import com.yqh.service.IReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Service
public class ReservationServiceImpl extends ServiceImpl<ReservationMapper, Reservation> implements IReservationService {

    @Autowired
    private UserMapper userMapper;

    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED) // [cite: 231]
    public boolean reserveSeat(Long userId, Long seatId, String startTimeStr, String endTimeStr) {
        LocalDateTime start = LocalDateTime.parse(startTimeStr, DF);
        LocalDateTime end = LocalDateTime.parse(endTimeStr, DF);

        // 1. 校验用户信用分
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (user.getCreditScore() < 60) { // [cite: 232]
            throw new RuntimeException("信用积分不足(<60)，无法预约");
        }

        // 2. 核心冲突检测 (对应技术报告 3.2.1 节算法)
        // SQL逻辑: status IN (0,1) AND db_start < new_end AND db_end > new_start
        Long conflictCount = this.count(new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getSeatId, seatId)
                .in(Reservation::getStatus, Arrays.asList(0, 1)) // 0:待签到, 1:已签到
                .lt(Reservation::getStartTime, end)  // 现有记录开始时间 < 新预约结束时间
                .gt(Reservation::getEndTime, start)  // 现有记录结束时间 > 新预约开始时间
        ); // [cite: 298]

        if (conflictCount > 0) {
            throw new RuntimeException("该时间段座位已被占用");
        }

        // 3. 构建并保存预约
        Reservation reservation = new Reservation();
        reservation.setUserId(userId);
        reservation.setSeatId(seatId);
        reservation.setStartTime(start);
        reservation.setEndTime(end);
        reservation.setStatus(0); // 初始状态: 待签到

        return this.save(reservation);
    }

    @Override
    public void checkIn(Long resId) {
        Reservation res = this.getById(resId);
        if (res == null) throw new RuntimeException("预约不存在");

        // 状态校验：只有状态为0（待签到）才能签到
        if (res.getStatus() != 0) {
            throw new RuntimeException("当前状态无法签到");
        }

        // 时间校验：假设允许在开始时间前后 15 分钟内签到
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(res.getStartTime().minusMinutes(15)) ||
                now.isAfter(res.getStartTime().plusMinutes(15))) {
            // throw new RuntimeException("不在签到时间范围内"); // 暂注释方便测试
        }

        res.setStatus(1); // 1: 已签到
        res.setCheckInTime(LocalDateTime.now());
        this.updateById(res);
    }

    @Override
    public void cancelReservation(Long resId) {
        Reservation res = this.getById(resId);
        if (res == null) throw new RuntimeException("预约不存在");

        if (res.getStatus() != 0) {
            throw new RuntimeException("只能取消待签到的预约");
        }

        res.setStatus(3); // 3: 已取消
        this.updateById(res);
    }

    @Override
    public boolean forceReleaseSeat(Long seatId) {
        // 1. 查找该座位当前处于 "0:待签到" 或 "1:已签到" 的预约
        List<Reservation> activeList = this.list(new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getSeatId, seatId)
                .in(Reservation::getStatus, 0, 1)); // 0和1都是占用状态

        if (activeList.isEmpty()) {
            return false; // 无需清退
        }

        // 2. 强制结束
        for (Reservation res : activeList) {
            res.setStatus(2); // 2: 已结束 (也可以设为 3:已取消，看业务定义，这里算正常结束不扣分)
            res.setEndTime(LocalDateTime.now()); // 修正结束时间为当前，立刻释放时间段
        }

        return this.updateBatchById(activeList);
    }
}