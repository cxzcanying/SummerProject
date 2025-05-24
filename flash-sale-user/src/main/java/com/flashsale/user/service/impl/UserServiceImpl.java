package com.flashsale.user.service.impl;

import com.flashsale.common.result.Result;
import com.flashsale.common.result.ResultCode;
import com.flashsale.user.dto.LoginDTO;
import com.flashsale.user.dto.UserDTO;
import com.flashsale.user.entity.User;
import com.flashsale.user.mapper.UserMapper;
import com.flashsale.user.service.UserService;
import com.flashsale.user.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 用户服务实现类
 * @author 21311
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String USER_TOKEN_PREFIX = "user:token:";
    private static final long TOKEN_EXPIRE_TIME = 24 * 60 * 60; // 24小时

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> register(UserDTO userDTO) {
        // 检查用户名是否已存在
        User existUser = userMapper.findByUsername(userDTO.getUsername());
        if (existUser != null) {
            return Result.error(ResultCode.USER_ALREADY_EXIST.getCode(), "用户名已存在");
        }

        // 检查手机号是否已存在
        existUser = userMapper.findByPhone(userDTO.getPhone());
        if (existUser != null) {
            return Result.error(ResultCode.USER_ALREADY_EXIST.getCode(), "手机号已被注册");
        }

        // 创建用户
        User user = new User();
        BeanUtils.copyProperties(userDTO, user);

        // 生成盐值
        String salt = UUID.randomUUID().toString().replace("-", "");
        user.setSalt(salt);

        // 密码加密
        String encryptedPassword = md5Encrypt(userDTO.getPassword() + salt);
        user.setPassword(encryptedPassword);

        // 设置默认值
        user.setStatus(1);
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());

        // 保存用户
        int rows = userMapper.insert(user);
        if (rows != 1) {
            return Result.error(ResultCode.ERROR.getCode(), "注册失败");
        }

        return Result.success();
    }

    @Override
    public Result<String> login(LoginDTO loginDTO) {
        // 查询用户
        User user = userMapper.findByUsername(loginDTO.getUsername());

        if (user == null) {
            return Result.error(ResultCode.USER_NOT_EXIST.getCode(), ResultCode.USER_NOT_EXIST.getMessage());
        }

        // 检查用户状态
        if (user.getStatus() == 0) {
            return Result.error(ResultCode.FORBIDDEN.getCode(), "账号已被禁用");
        }

        // 验证密码
        String encryptedPassword = md5Encrypt(loginDTO.getPassword() + user.getSalt());
        if (!encryptedPassword.equals(user.getPassword())) {
            return Result.error(ResultCode.PASSWORD_ERROR.getCode(), ResultCode.PASSWORD_ERROR.getMessage());
        }

        // 更新登录信息
        user.setLastLoginTime(new Date());
        userMapper.updateById(user);

        // 生成token
        String token = UUID.randomUUID().toString().replace("-", "");
        
        // 将用户信息存入Redis
        String tokenKey = USER_TOKEN_PREFIX + token;
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        redisTemplate.opsForValue().set(tokenKey, userVO, TOKEN_EXPIRE_TIME, TimeUnit.SECONDS);

        return Result.success(token);
    }

    @Override
    public Result<UserVO> getUserInfo(Long id) {
        User user = userMapper.findById(id);
        if (user == null) {
            return Result.error(ResultCode.USER_NOT_EXIST.getCode(), ResultCode.USER_NOT_EXIST.getMessage());
        }

        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);

        return Result.success(userVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> updateUserInfo(Long id, UserDTO userDTO) {
        User user = userMapper.findById(id);
        if (user == null) {
            return Result.error(ResultCode.USER_NOT_EXIST.getCode(), ResultCode.USER_NOT_EXIST.getMessage());
        }

        // 更新用户信息
        if (userDTO.getNickname() != null) {
            user.setNickname(userDTO.getNickname());
        }
        if (userDTO.getEmail() != null) {
            user.setEmail(userDTO.getEmail());
        }
        user.setUpdateTime(new Date());

        int rows = userMapper.updateById(user);
        if (rows != 1) {
            return Result.error(ResultCode.ERROR.getCode(), "更新失败");
        }

        return Result.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> changePassword(Long id, String oldPassword, String newPassword) {
        User user = userMapper.findById(id);
        if (user == null) {
            return Result.error(ResultCode.USER_NOT_EXIST.getCode(), ResultCode.USER_NOT_EXIST.getMessage());
        }

        // 验证旧密码
        String encryptedOldPassword = md5Encrypt(oldPassword + user.getSalt());
        if (!encryptedOldPassword.equals(user.getPassword())) {
            return Result.error(ResultCode.PASSWORD_ERROR.getCode(), "原密码错误");
        }

        // 更新密码
        String encryptedNewPassword = md5Encrypt(newPassword + user.getSalt());
        user.setPassword(encryptedNewPassword);
        user.setUpdateTime(new Date());

        int rows = userMapper.updateById(user);
        if (rows != 1) {
            return Result.error(ResultCode.ERROR.getCode(), "修改密码失败");
        }

        return Result.success();
    }

    @Override
    public Result<Boolean> checkUsername(String username) {
        User user = userMapper.findByUsername(username);
        boolean exists = user != null;
        return Result.success(exists);
    }

    @Override
    public Result<Boolean> checkPhone(String phone) {
        User user = userMapper.findByPhone(phone);
        boolean exists = user != null;
        return Result.success(exists);
    }

    @Override
    public Result<UserVO> getUserByUsername(String username) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            return Result.error(ResultCode.USER_NOT_EXIST.getCode(), ResultCode.USER_NOT_EXIST.getMessage());
        }

        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);

        return Result.success(userVO);
    }

    @Override
    public Result<Void> logout(String token) {
        String tokenKey = USER_TOKEN_PREFIX + token;
        redisTemplate.delete(tokenKey);
        return Result.success();
    }

    /**
     * MD5加密
     */
    private String md5Encrypt(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5加密失败", e);
        }
    }
} 