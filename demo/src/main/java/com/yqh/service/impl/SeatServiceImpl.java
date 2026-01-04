package com.yqh.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl; // 👈 必须导这个包
import com.yqh.entity.Seat;
import com.yqh.mapper.SeatMapper;
import com.yqh.service.ISeatService;
import org.springframework.stereotype.Service;

// 继承 ServiceImpl 后，saveBatch 的具体实现逻辑也就自动有了
@Service
public class SeatServiceImpl extends ServiceImpl<SeatMapper, Seat> implements ISeatService {
}