package com.ruoyi.framework.security;

import com.alibaba.fastjson.JSON;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证失败处理：返回 401 JSON（HTTP 仍置 200，便于前端响应拦截器按 code 处理并重定向登录页）
 */
@Component
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint
{
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException
    {
        response.setStatus(200);
        response.setContentType("application/json;charset=utf-8");
        Map<String, Object> result = new HashMap<>();
        result.put("code", 401);
        result.put("msg", "认证失败，请重新登录");
        response.getWriter().write(JSON.toJSONString(result));
    }
}
