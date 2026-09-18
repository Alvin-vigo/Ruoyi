package com.ruoyi.framework.security;

import com.ruoyi.common.core.domain.model.LoginUser;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全服务工具类（若依标准，精简版）
 */
public class SecurityUtils
{
    /**
     * 获取当前登录用户（从 SecurityContext 取 LoginUser）
     */
    public static LoginUser getLoginUser()
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken))
        {
            Object principal = authentication.getPrincipal();
            if (principal instanceof LoginUser)
            {
                return (LoginUser) principal;
            }
        }
        return null;
    }

    /**
     * 获取当前登录用户ID
     */
    public static Long getUserId()
    {
        LoginUser loginUser = getLoginUser();
        return loginUser == null ? null : loginUser.getUserId();
    }

    /**
     * 获取当前登录用户名
     */
    public static String getUsername()
    {
        LoginUser loginUser = getLoginUser();
        return loginUser == null ? null : loginUser.getUserName();
    }
}
