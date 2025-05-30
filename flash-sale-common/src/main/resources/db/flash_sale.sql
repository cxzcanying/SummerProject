-- 秒杀系统数据库脚本
-- 创建数据库
CREATE DATABASE IF NOT EXISTS flash_sale DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE flash_sale;

-- 用户表
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` varchar(50) NOT NULL COMMENT '用户名',
    `password` varchar(100) NOT NULL COMMENT '密码（加密后）',
    `salt` varchar(50) NOT NULL COMMENT '盐值',
    `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
    `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
    `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
    `avatar` varchar(255) DEFAULT NULL COMMENT '头像URL',
    `gender` tinyint DEFAULT 0 COMMENT '性别：0-未知，1-男，2-女',
    `birthday` date DEFAULT NULL COMMENT '生日',
    `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
    `last_login_ip` varchar(50) DEFAULT NULL COMMENT '最后登录IP',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_phone` (`phone`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 商品分类表
DROP TABLE IF EXISTS `product_category`;
CREATE TABLE `product_category` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `name` varchar(100) NOT NULL COMMENT '分类名称',
    `parent_id` bigint DEFAULT 0 COMMENT '父分类ID',
    `level` tinyint NOT NULL DEFAULT 1 COMMENT '分类级别',
    `sort_order` int DEFAULT 0 COMMENT '排序',
    `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_level` (`level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类表';

-- 商品表
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '商品ID',
    `category_id` bigint NOT NULL COMMENT '分类ID',
    `name` varchar(200) NOT NULL COMMENT '商品名称',
    `description` text COMMENT '商品描述',
    `image` varchar(500) DEFAULT NULL COMMENT '主图URL',
    `images` text COMMENT '商品图片JSON',
    `price` decimal(10,2) NOT NULL COMMENT '商品价格',
    `stock` int NOT NULL DEFAULT 0 COMMENT '库存数量',
    `sold_count` int NOT NULL DEFAULT 0 COMMENT '销售数量',
    `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-下架，1-上架',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

-- 秒杀活动表
DROP TABLE IF EXISTS `flash_sale_activity`;
CREATE TABLE `flash_sale_activity` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '活动ID',
    `name` varchar(200) NOT NULL COMMENT '活动名称',
    `description` text COMMENT '活动描述',
    `start_time` datetime NOT NULL COMMENT '开始时间',
    `end_time` datetime NOT NULL COMMENT '结束时间',
    `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0-未开始，1-进行中，2-已结束',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_start_time` (`start_time`),
    KEY `idx_end_time` (`end_time`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='秒杀活动表';

-- 秒杀商品表
DROP TABLE IF EXISTS `flash_sale_product`;
CREATE TABLE `flash_sale_product` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '秒杀商品ID',
    `activity_id` bigint NOT NULL COMMENT '活动ID',
    `product_id` bigint NOT NULL COMMENT '商品ID',
    `flash_sale_price` decimal(10,2) NOT NULL COMMENT '秒杀价格',
    `flash_sale_stock` int NOT NULL COMMENT '秒杀库存',
    `limit_per_user` int NOT NULL DEFAULT 1 COMMENT '每人限购数量',
    `sold_count` int NOT NULL DEFAULT 0 COMMENT '已售数量',
    `start_time` datetime NOT NULL COMMENT '开始时间',
    `end_time` datetime NOT NULL COMMENT '结束时间',
    `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_activity_id` (`activity_id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_start_time` (`start_time`),
    KEY `idx_end_time` (`end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='秒杀商品表';

-- 秒杀订单表（分表设计）
DROP TABLE IF EXISTS `flash_sale_order`;
CREATE TABLE `flash_sale_order` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    `order_no` varchar(50) NOT NULL COMMENT '订单编号',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `activity_id` bigint DEFAULT NULL COMMENT '活动ID',
    `product_id` bigint DEFAULT NULL COMMENT '商品ID',
    `flash_sale_product_id` bigint NOT NULL COMMENT '秒杀商品ID',
    `product_name` varchar(200) NOT NULL COMMENT '商品名称',
    `product_image` varchar(500) DEFAULT NULL COMMENT '商品主图',
    `original_price` decimal(10,2) NOT NULL COMMENT '原价',
    `flash_sale_price` decimal(10,2) NOT NULL COMMENT '秒杀价格',
    `quantity` int NOT NULL DEFAULT 1 COMMENT '购买数量',
    `coupon_id` bigint DEFAULT NULL COMMENT '优惠券ID',
    `discount_amount` decimal(10,2) DEFAULT 0.00 COMMENT '优惠金额',
    `pay_amount` decimal(10,2) NOT NULL COMMENT '实付金额',
    `status` tinyint NOT NULL DEFAULT 0 COMMENT '订单状态：0-待支付，1-已支付，2-已取消，3-已退款，4-已完成',
    `pay_type` tinyint DEFAULT NULL COMMENT '支付方式：1-支付宝，2-微信，3-银行卡',
    `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
    `expire_time` datetime NOT NULL COMMENT '过期时间',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_flash_sale_product_id` (`flash_sale_product_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='秒杀订单表';

-- 支付信息表
DROP TABLE IF EXISTS `payment_info`;
CREATE TABLE `payment_info` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '支付ID',
    `order_no` varchar(50) NOT NULL COMMENT '订单编号',
    `pay_no` varchar(100) DEFAULT NULL COMMENT '支付流水号',
    `pay_type` tinyint NOT NULL COMMENT '支付方式：1-支付宝，2-微信，3-银行卡',
    `pay_amount` decimal(10,2) NOT NULL COMMENT '支付金额',
    `pay_status` tinyint NOT NULL DEFAULT 0 COMMENT '支付状态：0-待支付，1-支付成功，2-支付失败',
    `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_pay_no` (`pay_no`),
    KEY `idx_pay_status` (`pay_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付信息表';
