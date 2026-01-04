package com.yqh.controller;

import com.yqh.common.Result;
import com.yqh.entity.Feedback;
import com.yqh.service.IFeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    @Autowired
    private IFeedbackService feedbackService;

    @PostMapping("/add")
    public Result<String> add(@RequestBody Feedback feedback) {
        feedback.setCreateTime(LocalDateTime.now());
        feedback.setStatus(0);
        feedbackService.save(feedback);
        return Result.success("反馈提交成功");
    }
}