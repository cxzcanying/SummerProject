package com.flashsale.user.mapper;

import com.flashsale.user.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper接口
 * @author 21311
 */
@Mapper
public interface UserMapper {
    
    /**
     * 根据ID查找用户
     * @param id 用户ID
     * @return 用户对象
     */
    User findById(Long id);

    /**
     * 根据用户名查找用户
     */
    User findByUsername(String username);
    
    /**
     * 根据手机号查找用户
     */
    User findByPhone(String phone);
    
    /**
     * 插入用户
     */
    int insert(User user);
    
    /**
     * 根据ID更新用户
     */
    int updateById(User user);
    
    /**
     * 根据ID删除用户
     */
    int deleteById(Long id);
} 