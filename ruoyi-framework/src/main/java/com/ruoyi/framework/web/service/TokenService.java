package com.ruoyi.framework.web.service;

import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.framework.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Token 会话服务（标准若依：token -> Redis 中存储 LoginUser）。
 * 会话存 Redis，支持自动续期与主动失效（踢人）。
 */
@Component
public class TokenService
{
    @Autowired
    private RedisCache redisCache;

    /**
     * 将登录用户存入 Redis，设置过期时间
     */
    public void setLoginUser(LoginUser loginUser)
    {
        String key = Constants.LOGIN_TOKEN_KEY + loginUser.getToken();
        redisCache.setTokenObject(key, loginUser, Constants.TOKEN_EXPIRE_MINUTES, TimeUnit.MINUTES);
    }

    /**
     * 根据 token 取登录用户
     */
    public LoginUser getLoginUser(String token)
    {
        if (token == null || token.isEmpty())
        {
            return null;
        }
        return redisCache.getTokenObject(Constants.LOGIN_TOKEN_KEY + token, LoginUser.class);
    }

    /**
     * 刷新会话过期时间（每次请求自动续期）
     */
    public void refreshToken(LoginUser loginUser)
    {
        redisCache.expire(Constants.LOGIN_TOKEN_KEY + loginUser.getToken(),
                Constants.TOKEN_EXPIRE_MINUTES, TimeUnit.MINUTES);
    }

    /**
     * 删除会话（登出 / 踢人）
     */
    public void deleteToken(String token)
    {
        if (token != null && !token.isEmpty())
        {
            redisCache.deleteTokenObject(Constants.LOGIN_TOKEN_KEY + token);
        }
    }

    /**
     * 从当前请求上下文取登录用户（供控制器使用）
     */
    public LoginUser getLoginUser()
    {
        return SecurityUtils.getLoginUser();
    }
}
