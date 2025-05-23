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

-- 优惠券表
DROP TABLE IF EXISTS `coupon`;
CREATE TABLE `coupon` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '优惠券ID',
    `name` varchar(100) NOT NULL COMMENT '优惠券名称',
    `type` tinyint NOT NULL COMMENT '类型：1-满减券，2-折扣券',
    `discount_amount` decimal(10,2) DEFAULT NULL COMMENT '减免金额',
    `discount_rate` decimal(3,2) DEFAULT NULL COMMENT '折扣比例',
    `min_amount` decimal(10,2) DEFAULT NULL COMMENT '最低消费金额',
    `total_count` int NOT NULL COMMENT '发放总数',
    `used_count` int NOT NULL DEFAULT 0 COMMENT '已使用数量',
    `start_time` datetime NOT NULL COMMENT '开始时间',
    `end_time` datetime NOT NULL COMMENT '结束时间',
    `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_start_time` (`start_time`),
    KEY `idx_end_time` (`end_time`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='优惠券表';

-- 用户优惠券表
DROP TABLE IF EXISTS `user_coupon`;
CREATE TABLE `user_coupon` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `coupon_id` bigint NOT NULL COMMENT '优惠券ID',
    `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0-未使用，1-已使用，2-已过期',
    `used_time` datetime DEFAULT NULL COMMENT '使用时间',
    `order_no` varchar(50) DEFAULT NULL COMMENT '使用订单号',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_coupon_id` (`coupon_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户优惠券表';

-- 用户访问日志表（防刷）
DROP TABLE IF EXISTS `user_access_log`;
CREATE TABLE `user_access_log` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id` bigint DEFAULT NULL COMMENT '用户ID',
    `ip` varchar(50) NOT NULL COMMENT 'IP地址',
    `uri` varchar(200) NOT NULL COMMENT '请求URI',
    `method` varchar(10) NOT NULL COMMENT '请求方法',
    `user_agent` varchar(500) DEFAULT NULL COMMENT 'User-Agent',
    `access_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '访问时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_ip` (`ip`),
    KEY `idx_access_time` (`access_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户访问日志表';

-- 限流记录表
DROP TABLE IF EXISTS `rate_limit_record`;
CREATE TABLE `rate_limit_record` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `key_name` varchar(100) NOT NULL COMMENT '限流键',
    `count` int NOT NULL DEFAULT 1 COMMENT '访问次数',
    `window_start` bigint NOT NULL COMMENT '时间窗口开始时间戳',
    `expire_time` datetime NOT NULL COMMENT '过期时间',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_key_window` (`key_name`, `window_start`),
    KEY `idx_expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='限流记录表';

-- 插入测试数据

-- 商品分类数据
INSERT INTO `product_category` (`name`, `parent_id`, `level`, `sort_order`) VALUES
('数码电器', 0, 1, 1),
('手机通讯', 1, 2, 1),
('电脑办公', 1, 2, 2),
('家用电器', 1, 2, 3),
('服装鞋帽', 0, 1, 2),
('男装', 5, 2, 1),
('女装', 5, 2, 2),
('运动鞋', 5, 2, 3);

-- 商品数据
INSERT INTO `product` (`category_id`, `name`, `description`, `image`, `price`, `stock`) VALUES
(2, 'iPhone 15 Pro', '苹果iPhone 15 Pro 256GB 原色钛金属', 'iphone15pro.jpg', 8999.00, 1000),
(2, '华为Mate60 Pro', '华为Mate60 Pro 12GB+256GB 雅川青', 'mate60pro.jpg', 6999.00, 800),
(3, 'MacBook Pro 14', 'Apple MacBook Pro 14英寸 M3芯片', 'macbookpro.jpg', 14999.00, 500),
(3, '联想ThinkPad X1', '联想ThinkPad X1 Carbon 2024款', 'thinkpad.jpg', 12999.00, 300),
(4, '小米电视65寸', '小米电视A65 2025款 4K超高清', 'xiaomi_tv.jpg', 2999.00, 200);

-- 秒杀活动数据
INSERT INTO `flash_sale_activity` (`name`, `description`, `start_time`, `end_time`, `status`) VALUES
('双十一狂欢节', '双十一全场大促销', '2024-11-11 00:00:00', '2024-11-11 23:59:59', 1),
('年货节大促销', '新年特惠，年货节大促销', '2024-01-15 00:00:00', '2024-01-31 23:59:59', 0);

-- 秒杀商品数据
INSERT INTO `flash_sale_product` (`activity_id`, `product_id`, `flash_sale_price`, `flash_sale_stock`, `limit_per_user`, `start_time`, `end_time`) VALUES
(1, 1, 4999.00, 100, 1, '2024-11-11 10:00:00', '2024-11-11 12:00:00'),
(1, 2, 3999.00, 50, 1, '2024-11-11 14:00:00', '2024-11-11 16:00:00'),
(1, 3, 9999.00, 20, 1, '2024-11-11 20:00:00', '2024-11-11 22:00:00'),
(2, 5, 1999.00, 100, 2, '2024-01-15 10:00:00', '2024-01-31 22:00:00');

-- 优惠券数据
INSERT INTO `coupon` (`name`, `type`, `discount_amount`, `min_amount`, `total_count`, `start_time`, `end_time`) VALUES
('满1000减100券', 1, 100.00, 1000.00, 10000, '2024-01-01 00:00:00', '2024-12-31 23:59:59'),
('满500减50券', 1, 50.00, 500.00, 20000, '2024-01-01 00:00:00', '2024-12-31 23:59:59'),
('9折优惠券', 2, NULL, NULL, 5000, '2024-01-01 00:00:00', '2024-12-31 23:59:59');

-- 创建索引优化查询性能
CREATE INDEX idx_user_phone ON `user`(`phone`);
CREATE INDEX idx_product_category_status ON `product`(`category_id`, `status`);
CREATE INDEX idx_flash_sale_product_activity_time ON `flash_sale_product`(`activity_id`, `start_time`, `end_time`);
CREATE INDEX idx_order_user_status ON `flash_sale_order`(`user_id`, `status`);
CREATE INDEX idx_payment_order_status ON `payment_info`(`order_no`, `pay_status`);

-- 创建分表（支持亿级数据）
-- 按月份分表的示例（可以根据实际需要调整分表策略）
-- DROP TABLE IF EXISTS `flash_sale_order_202401`;
-- CREATE TABLE `flash_sale_order_202401` LIKE `flash_sale_order`;

-- DROP TABLE IF EXISTS `flash_sale_order_202402`;
-- CREATE TABLE `flash_sale_order_202402` LIKE `flash_sale_order`;

-- 可以继续创建更多分表... 