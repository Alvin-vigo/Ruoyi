package com.ruoyi.framework.config;

import com.ruoyi.framework.security.AuthenticationEntryPointImpl;
import com.ruoyi.framework.security.JwtAuthenticationTokenFilter;
import com.ruoyi.framework.security.LogoutSuccessHandlerImpl;
import com.ruoyi.framework.web.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Spring Security 配置（标准若依，无状态会话 + Redis Token）。
 * 登录/登出/验证码/错误页放行，其余请求必须携带有效 token。
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig
{
    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;

    @Autowired
    private AuthenticationEntryPointImpl unauthorizedHandler;

    @Autowired
    private LogoutSuccessHandlerImpl logoutSuccessHandler;

    /** 密码编码器（BCrypt，与 sys_user 中加密密码一致） */
    @Bean
    public PasswordEncoder passwordEncoder()
    {
        return new BCryptPasswordEncoder();
    }

    /** 认证管理器：用于登录时校验用户名密码 */
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception
    {
        AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
        builder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
        return builder.build();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception
    {
        http
            // 关闭 csrf（前后端分离，token 鉴权）
            .csrf().disable()
            // 基于 token，不创建 session
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
            // 认证失败处理（返回 401 JSON，供前端拦截器识别）
            .exceptionHandling().authenticationEntryPoint(unauthorizedHandler).and()
            // 放行与鉴权规则
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    new AntPathRequestMatcher("/login"),
                    new AntPathRequestMatcher("/logout"),
                    new AntPathRequestMatcher("/captchaImage"),
                    new AntPathRequestMatcher("/error"),
                    new AntPathRequestMatcher("/favicon.ico"))
                .permitAll()
                .anyRequest().authenticated())
            // JWT 过滤器在用户名密码过滤器之前
            .addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class)
            // 登出处理
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessHandler(logoutSuccessHandler))
            // 允许 iframe（若将来嵌入页面用）
            .headers().frameOptions().disable();

        return http.build();
    }
}
