package com.yqh.controller;

import com.yqh.common.Result;
import com.yqh.entity.Reservation;
import com.yqh.entity.Seat;
import com.yqh.service.IReservationService;
import com.yqh.service.ISeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reservation")
@CrossOrigin // 允许跨域，方便Vue前端调试
public class ReservationController {

    @Autowired
    private IReservationService reservationService;

    /**
     * 提交座位预约
     * POST /api/reservation/book
     * Body: { "userId": 1, "seatId": 5, "startTime": "2026-01-05 08:00:00", "endTime": "2026-01-05 10:00:00" }
     */
    @PostMapping("/book")
    public Result<Boolean> bookSeat(@RequestBody Map<String, Object> params) {
        try {
            Long userId = Long.valueOf(params.get("userId").toString());
            Long seatId = Long.valueOf(params.get("seatId").toString());
            String startTime = params.get("startTime").toString();
            String endTime = params.get("endTime").toString();

            boolean success = reservationService.reserveSeat(userId, seatId, startTime, endTime);
            return Result.success(success);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 查询某用户的预约记录
     * GET /api/reservation/list?userId=1
     */
    @GetMapping("/list")
    public Result<?> getUserReservations(@RequestParam Long userId) {
        return Result.success(
                reservationService.lambdaQuery()
                        .eq(Reservation::getUserId, userId)
                        .orderByDesc(Reservation::getStartTime)
                        .list()
        );
    }

    @PostMapping("/checkin")
    public Result<Boolean> checkIn(@RequestBody Map<String, Long> params) {
        try {
            reservationService.checkIn(params.get("resId"));
            return Result.success(true);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/cancel")
    public Result<Boolean> cancel(@RequestBody Map<String, Long> params) {
        try {
            reservationService.cancelReservation(params.get("resId"));
            return Result.success(true);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }


}