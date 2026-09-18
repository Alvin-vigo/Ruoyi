package com.ruoyi.framework.security;

import com.alibaba.fastjson.JSON;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.framework.web.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 登出成功处理：删除 Redis 会话，返回成功 JSON
 */
@Component
public class LogoutSuccessHandlerImpl implements LogoutSuccessHandler
{
    @Autowired
    private TokenService tokenService;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                                Authentication authentication) throws IOException
    {
        // 从请求头提取 token 并删除会话
        String header = request.getHeader(Constants.TOKEN);
        if (header == null)
        {
            header = request.getHeader("Authorization");
        }
        if (header != null && header.startsWith(Constants.TOKEN_PREFIX))
        {
            tokenService.deleteToken(header.substring(Constants.TOKEN_PREFIX.length()));
        }
        else if (header != null && !header.isEmpty())
        {
            tokenService.deleteToken(header);
        }

        response.setStatus(200);
        response.setContentType("application/json;charset=utf-8");
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "退出成功");
        response.getWriter().write(JSON.toJSONString(result));
    }
}
