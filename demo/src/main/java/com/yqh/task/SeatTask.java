package com.yqh.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yqh.entity.Reservation;
import com.yqh.entity.User;
import com.yqh.mapper.UserMapper;
import com.yqh.service.IReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class SeatTask {

    @Autowired
    private IReservationService reservationService;
    @Autowired
    private UserMapper userMapper;

    /**
     * 每分钟执行一次：检查是否有“超时未签到”的订单
     * 逻辑：如果 当前时间 > 开始时间 + 15分钟 且 状态仍为 0(待签到)
     * 动作：标记为 4(违约)，并扣除信用分
     */
    @Scheduled(cron = "0 * * * * ?") // 每分钟的第0秒执行
    @Transactional
    public void autoReleaseSeats() {
        // 1. 找出违约记录
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(15);

        List<Reservation> violations = reservationService.list(new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getStatus, 0) // 待签到
                .lt(Reservation::getStartTime, threshold)); // 开始时间早于15分钟前

        if (violations.isEmpty()) return;

        System.out.println(">>> 扫描到违约记录: " + violations.size() + " 条");

        for (Reservation res : violations) {
            // 2. 更新订单状态为违约
            res.setStatus(4);
            reservationService.updateById(res);

            // 3. 扣除用户信用分 (比如扣10分)
            User user = userMapper.selectById(res.getUserId());
            if (user != null) {
                user.setCreditScore(user.getCreditScore() - 10);
                userMapper.updateById(user);
            }
        }
    }
}