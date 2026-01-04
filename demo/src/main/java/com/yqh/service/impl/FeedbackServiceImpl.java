package com.yqh.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yqh.entity.Feedback;
import com.yqh.mapper.FeedbackMapper;
import com.yqh.service.IFeedbackService;
import org.springframework.stereotype.Service;

@Service
public class FeedbackServiceImpl extends ServiceImpl<FeedbackMapper, Feedback> implements IFeedbackService {
}