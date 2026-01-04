package com.yqh;

import com.yqh.service.IReservationService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ReservationServiceTest {

    @Autowired
    private IReservationService reservationService;

    @Test
    void testConflictReservation() {
        Long userId = 1L; // 数据库里预埋的用户
        Long seatId = 1L; // 数据库里预埋的座位

        // 场景 1: 用户 A 预约 14:00 - 16:00 (应该成功)
        System.out.println(">>> 开始第1次预约 (14:00 - 16:00)...");
        boolean result1 = reservationService.reserveSeat(
                userId, seatId, "2026-01-06 14:00:00", "2026-01-06 16:00:00"
        );
        Assertions.assertTrue(result1, "第1次预约应该成功");
        System.out.println(">>> 第1次预约成功！");

        // 场景 2: 用户 B 尝试预约 15:00 - 17:00 (时间重叠，应该失败)
        System.out.println(">>> 开始第2次预约 (15:00 - 17:00) - 期望失败...");
        Exception exception = Assertions.assertThrows(RuntimeException.class, () -> {
            reservationService.reserveSeat(
                    userId, seatId, "2026-01-06 15:00:00", "2026-01-06 17:00:00"
            );
        });

        System.out.println(">>> 捕获异常信息: " + exception.getMessage());
        Assertions.assertEquals("该时间段座位已被占用", exception.getMessage());
        System.out.println(">>> 冲突检测测试通过！");
    }
}