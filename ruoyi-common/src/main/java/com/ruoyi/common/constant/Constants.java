package com.ruoyi.common.constant;

/**
 * 通用常量信息（若依标准）
 */
public class Constants
{
    /** 登录用户 Redis 会话 key 前缀 */
    public static final String LOGIN_TOKEN_KEY = "login_tokens:";

    /** 验证码 Redis key 前缀（预留，验证码当前未启用） */
    public static final String CAPTCHA_CODE_KEY = "captcha_codes:";

    /** 令牌有效期（分钟） */
    public static final Integer TOKEN_EXPIRE_MINUTES = 30;

    /** 令牌自定义标识（请求头中携带的字段名） */
    public static final String TOKEN = "token";

    /** 令牌前缀 */
    public static final String TOKEN_PREFIX = "Bearer ";

    /** 登录失败计数前缀（预留） */
    public static final String LOGIN_FAIL_KEY = "login_fail:";
}
