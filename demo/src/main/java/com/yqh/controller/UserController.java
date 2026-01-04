package com.yqh.controller;

import com.yqh.common.Result;
import com.yqh.entity.User;
import com.yqh.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@CrossOrigin
public class UserController {

    @Autowired
    // ✅ 修正：注入 Service 而不是 Mapper
    private IUserService userService;

    @PostMapping("/login")
    public Result<User> login(@RequestBody User loginUser) {
        try {
            // ✅ 修正：调用 Service 层封装好的业务逻辑
            // 这样 ServiceImpl 里的密码校验、异常处理才会生效
            User user = userService.login(loginUser.getUsername(), loginUser.getPassword());
            return Result.success(user);
        } catch (Exception e) {
            // 捕获 Service 层抛出的 "用户不存在" 或 "密码错误" 异常
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/info")
    public Result<User> getUserInfo(@RequestParam Long userId) {
        // IUserService 继承了 MyBatis-Plus 的 IService，所以直接有 getById 方法
        User user = userService.getById(userId);
        return Result.success(user);
    }

    @PostMapping("/register")
    public Result<String> register(@RequestBody com.yqh.entity.dto.RegisterDTO registerDTO) {
        try {
            userService.register(registerDTO);
            return Result.success("注册成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}