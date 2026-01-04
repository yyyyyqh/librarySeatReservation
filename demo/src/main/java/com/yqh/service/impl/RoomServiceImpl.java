package com.yqh.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yqh.entity.ReadingRoom;
import com.yqh.mapper.ReadingRoomMapper;
import com.yqh.service.IRoomService;
import org.springframework.stereotype.Service;

@Service
public class RoomServiceImpl extends ServiceImpl<ReadingRoomMapper, ReadingRoom> implements IRoomService {
}