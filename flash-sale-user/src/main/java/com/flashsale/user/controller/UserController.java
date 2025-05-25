package com.flashsale.user.controller;

import com.flashsale.common.result.Result;
import com.flashsale.user.dto.LoginDTO;
import com.flashsale.user.dto.UserDTO;
import com.flashsale.user.service.UserService;
import com.flashsale.user.vo.LoginVO;
import com.flashsale.user.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 用户控制器
 * @author 21311
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<UserVO> register(@RequestBody @Valid UserDTO userDTO) {
        log.info("用户注册: {}", userDTO.getUsername());
        return userService.register(userDTO);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody @Valid LoginDTO loginDTO) {
        log.info("用户登录: {}", loginDTO.getUsername());
        return userService.login(loginDTO);
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/profile/{id}")
    public Result<UserVO> getUserInfo(@PathVariable Long id) {
        log.info("获取用户信息: {}", id);
        return userService.getUserInfo(id);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/{id}")
    public Result<Void> updateUserInfo(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        log.info("更新用户信息: {}", id);
        return userService.updateUserInfo(id, userDTO);
    }

    /**
     * 修改密码
     */
    @PutMapping("/{id}/password")
    public Result<Void> changePassword(@PathVariable Long id, 
                                       @RequestParam String oldPassword, 
                                       @RequestParam String newPassword) {
        log.info("修改密码: {}", id);
        return userService.changePassword(id, oldPassword, newPassword);
    }

    /**
     * 检查用户名是否存在
     */
    @GetMapping("/check/username/{username}")
    public Result<Boolean> checkUsername(@PathVariable String username) {
        log.info("检查用户名是否存在: {}", username);
        return userService.checkUsername(username);
    }

    /**
     * 检查手机号是否存在
     */
    @GetMapping("/check/phone/{phone}")
    public Result<Boolean> checkPhone(@PathVariable String phone) {
        log.info("检查手机号是否存在: {}", phone);
        return userService.checkPhone(phone);
    }

    /**
     * 通过用户名获取用户信息
     */
    @GetMapping("/username/{username}")
    public Result<UserVO> getUserByUsername(@PathVariable String username) {
        log.info("通过用户名获取用户信息: {}", username);
        return userService.getUserByUsername(username);
    }
} 