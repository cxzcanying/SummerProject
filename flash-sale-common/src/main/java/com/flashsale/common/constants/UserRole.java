package com.flashsale.common.constants;

/**
 * 用户角色常量
 * @author 21311
 */
public class UserRole {
    
    /**
     * 普通买家
     */
    public static final Integer BUYER = 1;
    
    /**
     * VIP买家
     */
    public static final Integer VIP_BUYER = 2;
    
    /**
     * 商家
     */
    public static final Integer MERCHANT = 3;
    
    /**
     * 管理员
     */
    public static final Integer ADMIN = 4;
    
    /**
     * 获取角色名称
     */
    public static String getRoleName(Integer role) {
        if (role == null) {
            return "未知";
        }
        return switch (role) {
            case 1 -> "普通买家";
            case 2 -> "VIP买家";
            case 3 -> "商家";
            case 4 -> "管理员";
            default -> "未知";
        };
    }
    
    /**
     * 检查是否为管理员
     */
    public static boolean isAdmin(Integer role) {
        return ADMIN.equals(role);
    }
    
    /**
     * 检查是否为商家
     */
    public static boolean isMerchant(Integer role) {
        return MERCHANT.equals(role);
    }
    
    /**
     * 检查是否为VIP买家
     */
    public static boolean isVip(Integer role) {
        return VIP_BUYER.equals(role);
    }
    
    /**
     * 检查是否有秒杀权限
     */
    public static boolean canSeckill(Integer role) {
        return BUYER.equals(role) || VIP_BUYER.equals(role);
    }
} 