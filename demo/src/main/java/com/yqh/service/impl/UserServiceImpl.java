package com.yqh.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yqh.entity.User;
import com.yqh.mapper.UserMapper;
import com.yqh.service.IUserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Override
    public User login(String username, String password) {
        // 1. 构建查询条件
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username)
                // 【关键修改】显式指定要查询的列，必须包含 getPassword
                // 这里我们顺便查出登录后前端需要的字段，避免查出 update_time 等无用字段
                .select(User::getUserId, User::getUsername, User::getPassword,
                        User::getRealName, User::getIdentityType, User::getCreditScore);

        User user = this.getOne(wrapper);

        // 2. 校验用户是否存在
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 3. 校验密码
        // 此时 user.getPassword() 不再是 null，不会报空指针了
        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("密码错误");
        }

        // 4. 登录成功后，为了安全，将内存中的密码擦除，不返回给前端
        user.setPassword(null);

        return user;
    }
}