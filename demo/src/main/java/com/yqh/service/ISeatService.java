package com.yqh.service;

import com.baomidou.mybatisplus.extension.service.IService; // 👈 必须导这个包
import com.yqh.entity.Seat;

// 继承 IService<Seat> 后，saveBatch 就自动有了
public interface ISeatService extends IService<Seat> {
    // 这里可以留空，除非您有自定义的业务方法
}