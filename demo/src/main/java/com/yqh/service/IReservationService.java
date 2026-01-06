package com.yqh.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yqh.entity.Reservation;

public interface IReservationService extends IService<Reservation> {
    /**
     * 提交座位预约
     * @param userId 用户ID
     * @param seatId 座位ID
     * @param startTime 开始时间字符串 (yyyy-MM-dd HH:mm:ss)
     * @param endTime 结束时间字符串 (yyyy-MM-dd HH:mm:ss)
     * @return 是否成功
     */
    boolean reserveSeat(Long userId, Long seatId, String startTime, String endTime);

    /**
     * 签到
     * @param resId 预约记录ID
     */
    void checkIn(Long resId);

    /**
     * 取消预约
     * @param resId 预约记录ID
     */
    void cancelReservation(Long resId);

    /**
     * 座位清退
     */
    boolean forceReleaseSeat(Long seatId);

    /**
     * 用户主动离座/结账
     * @param resId 预约记录ID
     */
    void leaveSeat(Long resId);
}