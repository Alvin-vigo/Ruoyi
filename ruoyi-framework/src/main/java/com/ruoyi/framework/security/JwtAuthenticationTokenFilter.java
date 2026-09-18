package com.ruoyi.framework.security;

import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.framework.web.service.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JWT/Token 认证过滤器：从请求头取 token -> Redis 取登录用户 -> 写入 SecurityContext
 */
@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter
{
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationTokenFilter.class);

    @Autowired
    private TokenService tokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException
    {
        String token = extractToken(request);
        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null)
        {
            LoginUser loginUser = tokenService.getLoginUser(token);
            if (loginUser != null)
            {
                // 每次请求自动续期会话
                tokenService.refreshToken(loginUser);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            else
            {
                // token 无效或会话已过期（Redis 中无记录）
                log.debug("token 无效或会话已过期：{}", token);
            }
        }
        chain.doFilter(request, response);
    }

    /**
     * 从请求头提取 token（格式：Bearer <token>），兼容不带 Bearer 前缀的情况
     */
    private String extractToken(HttpServletRequest request)
    {
        String header = request.getHeader(Constants.TOKEN);
        if (header == null)
        {
            header = request.getHeader("Authorization");
        }
        if (header != null && header.startsWith(Constants.TOKEN_PREFIX))
        {
            return header.substring(Constants.TOKEN_PREFIX.length());
        }
        if (header != null && !header.isEmpty())
        {
            return header;
        }
        return null;
    }
}
