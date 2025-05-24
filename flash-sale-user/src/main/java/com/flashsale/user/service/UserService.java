package com.flashsale.user.service;

import com.flashsale.common.result.Result;
import com.flashsale.user.dto.LoginDTO;
import com.flashsale.user.dto.UserDTO;
import com.flashsale.user.vo.UserVO;

/**
 * 用户服务接口
 * @author 21311
 */
public interface UserService{

    /**
     * 用户注册
     *
     * @param userDTO 用户信息
     * @return 注册结果
     */
    Result<Void> register(UserDTO userDTO);

    /**
     * 用户登录
     *
     * @param loginDTO 登录信息
     * @return 登录结果
     */
    Result<String> login(LoginDTO loginDTO);

    /**
     * 获取用户信息
     *
     * @param id 用户ID
     * @return 用户信息
     */
    Result<UserVO> getUserInfo(Long id);

    /**
     * 更新用户信息
     *
     * @param id      用户ID
     * @param userDTO 用户信息
     * @return 更新结果
     */
    Result<Void> updateUserInfo(Long id, UserDTO userDTO);

    /**
     * 修改密码
     *
     * @param id          用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 修改结果
     */
    Result<Void> changePassword(Long id, String oldPassword, String newPassword);

    /**
     * 检查用户名是否存在
     *
     * @param username 用户名
     * @return 检查结果
     */
    Result<Boolean> checkUsername(String username);

    /**
     * 检查手机号是否存在
     *
     * @param phone 手机号
     * @return 检查结果
     */
    Result<Boolean> checkPhone(String phone);

    /**
     * 通过用户名获取用户信息
     *
     * @param username 用户名
     * @return 用户信息
     */
    Result<UserVO> getUserByUsername(String username);

    /**
     * 用户登出
     *
     * @param token 用户token
     * @return 登出结果
     */
    Result<Void> logout(String token);
} 