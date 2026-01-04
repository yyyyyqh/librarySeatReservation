package com.yqh.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yqh.entity.User;
import org.apache.ibatis.annotations.Select;

public interface UserMapper extends BaseMapper<User> {
    // MyBatis-Plus 已经内置了基础 CRUD，这里可以留空
    // 如果需要自定义查询，可以使用 @Select
}