package com.quanxiaoha.xiaohashu.auth.constant;

public class RedisConstants {
    /**
     * 验证码 KEY 前缀
     */
    private static final String VERIFICATION_CODE_KEY_PREFIX = "verification_code:";
    public static final String XIAOHASHU_ID_GENERATOR_KEY = "xiaohashu_id_generator";
    private static final String XIAOHASHU_ID_PREFIX = "xiaohashu:";
    private static final String USER_ROLES_KEY_PREFIX = "user:roles:";
    /**
     * 角色对应的权限集合 KEY 前缀
     */
    private static final String ROLE_PERMISSIONS_KEY_PREFIX = "role:permissions:";

    /**
     * 构建验证码 KEY
     *
     * @param phone
     * @return
     */
    public static String buildVerificationCodeKey(String phone) {
        return VERIFICATION_CODE_KEY_PREFIX + phone;
    }
    public static String buildNickName(String xiaohashuId){
        return XIAOHASHU_ID_PREFIX+xiaohashuId;
    }
    /**
     * 构建用户-角色 Key
     * @param userId
     * @return
     */
    public static String buildUserRoleKey(Long userId) {
        return USER_ROLES_KEY_PREFIX + userId;
    }
    public static String buildRolePermissionsKey(String roleKey) {
        return ROLE_PERMISSIONS_KEY_PREFIX + roleKey;
    }
}
