package com.ruoyi.framework.web.service;

import com.ruoyi.common.core.domain.model.LoginUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 登录校验服务（若依标准）：校验账号密码 -> 生成 token -> 写入 Redis 会话
 */
@Component
public class SysLoginService
{
    private static final Logger log = LoggerFactory.getLogger(SysLoginService.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenService tokenService;

    /**
     * 登录验证，返回 token
     *
     * @param username 用户名
     * @param password 密码
     * @return 令牌 token
     */
    public String login(String username, String password)
    {
        // 用户验证（UserDetailsServiceImpl 加载用户，BCrypt 校验密码）
        Authentication authentication;
        try
        {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));
        }
        catch (BadCredentialsException e)
        {
            log.warn("用户 {} 登录失败：密码错误", username);
            throw new RuntimeException("用户名或密码错误");
        }
        catch (Exception e)
        {
            log.warn("用户 {} 登录失败：{}", username, e.getMessage());
            throw new RuntimeException(e.getMessage());
        }

        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        // 生成 UUID token
        String token = UUID.randomUUID().toString().replace("-", "");
        loginUser.setToken(token);
        tokenService.setLoginUser(loginUser);
        return token;
    }

    /**
     * 退出登录：删除 Redis 会话
     */
    public void logout(String token)
    {
        if (token != null && !token.isEmpty())
        {
            tokenService.deleteToken(token);
        }
    }
}
